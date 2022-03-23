/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.RefactoringTickProvider;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsCompositeRefactoring;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * @author Alexander Weickmann
 */
public abstract class IpsCompositeRefactoring extends Refactoring implements IIpsCompositeRefactoring {

    private final Set<IIpsElement> elements;

    private final Set<IIpsElement> skippedElements;

    /**
     * @param elements The elements to refactor in the composite refactoring (set will be copied
     *            defensively)
     * 
     * @throws IllegalArgumentException If the size of the given set is &le; 0 or if different types
     *             of elements shall be refactored (e.g. attributes and methods)
     */
    protected IpsCompositeRefactoring(Set<IIpsElement> elements) {
        checkElements(elements);
        this.elements = new HashSet<>(elements.size());
        this.elements.addAll(elements);
        this.skippedElements = new HashSet<>(elements.size());
    }

    // TODO AW 22-04-2011: Only IPS elements from the same IPS project allowed?
    private void checkElements(Set<IIpsElement> elements) {
        ArgumentCheck.isTrue(elements.size() > 0);
        Set<Class<?>> classes = new HashSet<>(2);
        for (IIpsElement element : elements) {
            if (element instanceof IIpsObject) {
                classes.add(IIpsObject.class);
            } else {
                classes.add(element.getClass());
            }
        }
        ArgumentCheck.isTrue(classes.size() == 1);
    }

    /*
     * IMPORTANT: The contained refactoring instances are always re-created before performing any
     * actions. Creating the instances once and then performing all actions on them does not work as
     * a refactoring instance may become invalid if another refactoring is executed.
     */

    @Override
    public final boolean isSourceFilesSavedRequired() {
        for (IIpsElement element : elements) {
            if (skippedElements.contains(element)) {
                continue;
            }
            IIpsRefactoring refactoring = createRefactoring(element);
            if (refactoring.isSourceFilesSavedRequired()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final RefactoringStatus checkInitialConditions(IProgressMonitor pm) {

        pm.beginTask("", getNumberOfRefactorings()); //$NON-NLS-1$
        pm.setTaskName(Messages.IpsCompositeRefactoring_taskCheckInitialConditions);

        RefactoringStatus status = new RefactoringStatus();
        int i = 0;
        for (IIpsElement element : elements) {
            i++;
            pm.subTask(NLS.bind(Messages.IpsCompositeRefactoring_subTaskCheckInitialConditionsForElement, i,
                    getNumberOfRefactorings()));

            if (skippedElements.contains(element)) {
                pm.worked(1);
                continue;
            }

            checkConditions(element, status, CheckConditionsOperation.INITIAL_CONDITONS);

            pm.worked(1);
        }
        return status;
    }

    private void checkConditions(IIpsElement element, RefactoringStatus status, int checkConditionOperation) {
        IIpsRefactoring refactoring = createRefactoring(element);
        CheckConditionsOperation checkConditionsOperation = new CheckConditionsOperation(
                refactoring.toLtkRefactoring(), checkConditionOperation);
        try {
            checkConditionsOperation.run(new NullProgressMonitor());
        } catch (CoreException e) {
            throw new IpsException(e);
        }
        status.merge(checkConditionsOperation.getStatus());
    }

    @Override
    public final RefactoringStatus checkFinalConditions(IProgressMonitor pm) {

        pm.beginTask("", getNumberOfRefactorings()); //$NON-NLS-1$
        pm.setTaskName(Messages.IpsCompositeRefactoring_taskCheckFinalConditions);

        RefactoringStatus status = new RefactoringStatus();
        int i = 0;
        for (IIpsElement element : elements) {
            i++;
            pm.subTask(NLS.bind(Messages.IpsCompositeRefactoring_subTaskCheckFinalConditionsForElement, i,
                    getNumberOfRefactorings()));

            if (skippedElements.contains(element)) {
                pm.worked(1);
                continue;
            }

            checkConditions(element, status, CheckConditionsOperation.FINAL_CONDITIONS);

            pm.worked(1);
        }
        return status;

    }

    @Override
    public final Change createChange(IProgressMonitor pm) {
        pm.beginTask("", getNumberOfRefactorings()); //$NON-NLS-1$
        pm.setTaskName(Messages.IpsCompositeRefactoring_taskProcessElements);

        int i = 0;
        for (IIpsElement element : elements) {
            if (pm.isCanceled()) {
                break;
            }
            i++;
            pm.subTask(NLS.bind(Messages.IpsCompositeRefactoring_subTaskProcessElement, i, getNumberOfRefactorings()));

            if (skippedElements.contains(element)) {
                pm.worked(1);
                continue;
            }

            createChange(element);

            pm.worked(1);
        }

        return new NullChange();
    }

    private void createChange(IIpsElement element) {
        IIpsRefactoring refactoring = createRefactoring(element);
        /*
         * In fact we would not need to check the final conditions again at this point but the LTK
         * processor-based refactoring only loads the refactoring participants during final
         * condition checking which means that an internal LTK variable will be null at change
         * execution if this step is skipped. The LTK life cycle also formally requires the final
         * condition checking to be executed exactly once for each refactoring instance.
         */
        PerformRefactoringOperation performRefactoringOperation = new PerformRefactoringOperation(
                refactoring.toLtkRefactoring(), CheckConditionsOperation.FINAL_CONDITIONS);
        try {
            performRefactoringOperation.run(new NullProgressMonitor());
        } catch (CoreException e) {
            throw new IpsException(e);
        }

        for (RefactoringStatusEntry entry : performRefactoringOperation.getConditionStatus().getEntries()) {
            if (entry.getSeverity() == RefactoringStatus.OK) {
                continue;
            }
            logMessage(entry);
        }
    }

    /**
     * Log the given entry's message to the error log.
     * <p>
     * For messages with severity {@link RefactoringStatus#ERROR} the user is informed that a part
     * of the composite refactoring was not executed due to an error.
     */
    private void logMessage(RefactoringStatusEntry entry) {
        String message = entry.getMessage();
        int severity = getEclipseSeverityFromLtkSeverity(entry.getSeverity());
        if (severity == IStatus.ERROR) {
            message = NLS.bind(Messages.IpsCompositeRefactoring_childRefactoringNotExecuted, getName(), message);
        }
        IpsPlugin.log(new Status(severity, IpsPlugin.PLUGIN_ID, message));
    }

    private int getEclipseSeverityFromLtkSeverity(int ltkSeverity) {
        switch (ltkSeverity) {
            case RefactoringStatus.OK:
                return IStatus.OK;
            case RefactoringStatus.INFO:
                return IStatus.INFO;
            case RefactoringStatus.WARNING:
                return IStatus.WARNING;
            case RefactoringStatus.ERROR:
            case RefactoringStatus.FATAL:
                return IStatus.ERROR;
            default:
                throw new RuntimeException("Unknown status severity."); //$NON-NLS-1$
        }
    }

    @Override
    public final Refactoring toLtkRefactoring() {
        return this;
    }

    @Override
    public final int getNumberOfRefactorings() {
        return elements.size();
    }

    /**
     * Subclasses are responsible of creating an instance of the correct {@link IIpsRefactoring} for
     * the given element.
     * 
     * @param element The element to create the refactoring for
     */
    protected abstract IIpsRefactoring createRefactoring(IIpsElement element);

    /**
     * Allows subclasses to configure the composite refactoring to exclude the indicated element
     * from being refactored.
     * <p>
     * Returns true if the indicated element is not already excluded from being refactored.
     * 
     * @param element The element that should not be refactored
     */
    protected final boolean skipElement(IIpsElement element) {
        return skippedElements.add(element);
    }

    /**
     * Allows subclasses to clear the set of elements that are not refactored.
     */
    protected final void clearSkippedElements() {
        skippedElements.clear();
    }

    @Override
    protected final RefactoringTickProvider doGetRefactoringTickProvider() {
        return new RefactoringTickProvider(getNumberOfRefactorings(), getNumberOfRefactorings() * 10,
                getNumberOfRefactorings() * 10, getNumberOfRefactorings());
    }

    @Override
    public final boolean isCancelable() {
        return getNumberOfRefactorings() > 1;
    }

    @Override
    public final IIpsProject getIpsProject() {
        return ((IIpsElement)elements.toArray()[0]).getIpsProject();
    }

    @Override
    public final Set<IIpsElement> getIpsElements() {
        return new HashSet<>(elements);
    }

}
