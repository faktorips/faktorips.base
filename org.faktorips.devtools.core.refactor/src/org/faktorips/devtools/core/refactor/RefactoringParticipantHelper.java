/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.model.ipsobject.refactor.IIpsMoveRenameIpsObjectProcessor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.builder.IJavaBuilderSet;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Because all refactoring participants must be derived from an abstract LTK base class we can't use
 * inheritance ourselves to provide common functionality for the standard builder refactoring
 * participants.
 * <p>
 * Instead of using inheritance every participant may hold a {@link RefactoringParticipantHelper}.
 * <p>
 * A {@link RefactoringParticipantHelper} is able to refactor the Java source code in terms of the
 * Faktor-IPS refactoring by successively calling JDT refactorings on the {@link IJavaElement}
 * generated by the code generator for the {@link IIpsObjectPartContainer} to be refactored.
 * 
 * @author Alexander Weickmann
 */
public abstract class RefactoringParticipantHelper {

    /**
     * List containing the {@link IJavaElement}s generated originally for the
     * {@link IIpsObjectPartContainer} to be refactored.
     */
    private List<IJavaElement> originalJavaElements;

    /**
     * List containing the {@link IJavaElement}s generated for the {@link IIpsObjectPartContainer}
     * to be refactored as they will be after the refactoring. This information is needed to be able
     * to provide the JDT refactorings with the correct new names for the {@link IJavaElement}s.
     */
    private List<IJavaElement> targetJavaElements;

    private Map<IType, List<IMember>> originalJavaMembersByType;

    /**
     * Checks the conditions of the JDT refactorings to be performed.
     * 
     * @see RefactoringParticipant#checkConditions(IProgressMonitor, CheckConditionsContext)
     */
    public final RefactoringStatus checkConditions(IProgressMonitor pm) throws OperationCanceledException {
        RefactoringStatus status = new RefactoringStatus();

        for (int i = 0; i < getNumberOfJavaElementsToRefactor(); i++) {
            IJavaElement originalJavaElement = originalJavaElements.get(i);

            /*
             * The refactoring may be executed without present Java code or the Java element might
             * already have been refactored as a side-effect of the refactoring of a previous
             * element.
             */
            if (!(originalJavaElement.exists())) {
                continue;
            }

            // Ignore constructors as they will not be refactored
            if (originalJavaElement instanceof IMethod) {
                try {
                    if (((IMethod)originalJavaElement).isConstructor()) {
                        continue;
                    }
                } catch (JavaModelException e) {
                    RefactoringStatus errorStatus = new RefactoringStatus();
                    errorStatus.addFatalError(e.getLocalizedMessage());
                    return errorStatus;
                }
            }

            IJavaElement targetJavaElement = getTargetJavaElementForOriginalJavaElement(originalJavaElement);
            if (targetJavaElement == null) {
                // The element does not need to be refactored
                continue;
            }

            try {
                JavaRefactoring javaRefactoring = createJavaRefactoring(originalJavaElement, targetJavaElement, status,
                        pm);
                if (javaRefactoring != null) {
                    RefactoringStatus conditionsStatus = javaRefactoring.checkAllConditions(pm);
                    status.merge(conditionsStatus);
                }
            } catch (CoreException e) {
                RefactoringStatus errorStatus = new RefactoringStatus();
                errorStatus.addFatalError(e.getLocalizedMessage());
                return errorStatus;
            }
        }

        // Assure that every status message is only contained and thus shown once
        RefactoringStatus finalStatus = new RefactoringStatus();
        List<String> messages = new ArrayList<>(status.getEntries().length);
        for (RefactoringStatusEntry entry : status.getEntries()) {
            if (messages.contains(entry.getMessage())) {
                continue;
            }
            messages.add(entry.getMessage());
            finalStatus.addEntry(entry);
        }

        return finalStatus;
    }

    /**
     * Iterates over all generated {@link IJavaElement}s and performs the appropriate JDT
     * refactorings.
     * 
     * @see RefactoringParticipant#createChange(IProgressMonitor)
     */
    public final Change createChange(IProgressMonitor pm) throws IpsException, OperationCanceledException {
        /*
         * The Java elements need to be sorted in such a way that types are processed after
         * everything else because otherwise members may not be found anymore (because the type they
         * refer to does no longer exist).
         */
        List<IJavaElement> sortedOriginalJavaElements = sortJavaElements(originalJavaElements);
        try {
            for (int i = 0; i < getNumberOfJavaElementsToRefactor(); i++) {
                IJavaElement originalJavaElement = sortedOriginalJavaElements.get(i);

                /*
                 * Do not try to refactor non-existing Java elements as the user may want to try to
                 * start the refactoring when there is no source code at all. This also solves the
                 * problem of what should happen when there is a Java element that occurs in the
                 * implementation as well as in the published interface. If for example a setter
                 * method occurs in the implementation as well as in the published interface then
                 * the first encountered will be refactored. The second no longer exists then
                 * because the JDT rename method refactoring renamed it already.
                 */
                if (!(originalJavaElement.exists())) {
                    continue;
                }

                // Do not refactor constructors
                if (originalJavaElement instanceof IMethod) {
                    if (((IMethod)originalJavaElement).isConstructor()) {
                        continue;
                    }
                }

                IJavaElement targetJavaElement = getTargetJavaElementForOriginalJavaElement(originalJavaElement);
                if (targetJavaElement == null) {
                    // The element does not need to be refactored
                    continue;
                }

                /*
                 * We can't use the refactoring instances created during condition checking because
                 * the Java references are build upon creation of the refactoring instance. These
                 * might become invalid when refactorings are performed. Because of that new
                 * instances are created here.
                 */
                JavaRefactoring javaRefactoring = createJavaRefactoring(originalJavaElement, targetJavaElement,
                        new RefactoringStatus(), pm);
                if (javaRefactoring != null) {
                    javaRefactoring.perform(pm);
                }
            }
        } catch (CoreException e) {
            throw new IpsException(e);
        }
        return new NullChange();
    }

    /**
     * Creates and returns a sorted version of the given list of Java elements.
     * <p>
     * The elements will be sorted in such a way that types are located at the very end of the list.
     */
    private List<IJavaElement> sortJavaElements(List<IJavaElement> javaElements) {
        List<IType> collectedTypes = new ArrayList<>(2);
        List<IJavaElement> sortedJavaElements = new ArrayList<>(getNumberOfJavaElementsToRefactor());
        for (int i = 0; i < getNumberOfJavaElementsToRefactor(); i++) {
            if (javaElements.get(i).getElementType() == IJavaElement.TYPE) {
                collectedTypes.add((IType)javaElements.get(i));
            } else {
                sortedJavaElements.add(javaElements.get(i));
            }
        }
        sortedJavaElements.addAll(collectedTypes);
        return sortedJavaElements;
    }

    /**
     * This implementation initializes the list of generated {@link IJavaElement}s for the provided
     * {@link IIpsObjectPartContainer}.
     * <p>
     * Returns false in case the element passed to this operation is not an
     * {@link IIpsObjectPartContainer} or if the parent {@link IIpsProject}'s builder set is not a
     * {@link IJavaBuilderSet}. Otherwise the subclass implementation is called to initialize the
     * {@link IJavaElement}s that will be generated for the {@link IIpsObjectPartContainer} after
     * the refactoring has finished and true is returned.
     */
    public final boolean initialize(IpsRefactoringProcessor processor, Object element) {
        if (!(element instanceof IIpsObjectPartContainer)) {
            return false;
        }

        IIpsObjectPartContainer ipsObjectPartContainer = (IIpsObjectPartContainer)element;
        IIpsArtefactBuilderSet ipsArtefactBuilderSet = ipsObjectPartContainer.getIpsProject()
                .getIpsArtefactBuilderSet();
        if (!(ipsArtefactBuilderSet instanceof IJavaBuilderSet)) {
            return false;
        }
        IJavaBuilderSet javaBuilderSet = (IJavaBuilderSet)ipsArtefactBuilderSet;

        initializeOriginalJavaElements(ipsObjectPartContainer, javaBuilderSet);
        initializeTargetJavaElements(processor, javaBuilderSet);

        initializeOriginalJavaMembersByType();

        return true;
    }

    private void initializeOriginalJavaMembersByType() {
        originalJavaMembersByType = new HashMap<>();
        for (IJavaElement originalJavaElement : originalJavaElements) {
            if (originalJavaElement instanceof IField || originalJavaElement instanceof IMethod) {
                IType type = (IType)originalJavaElement.getParent();
                List<IMember> members = originalJavaMembersByType.computeIfAbsent(type, $ -> new ArrayList<>());
                members.add((IMember)originalJavaElement);
            }
        }
    }

    protected Map<IType, List<IMember>> getOriginalJavaMembersByType() {
        return originalJavaMembersByType;
    }

    /**
     * Responsible for returning whether the target Java element maps to the given original Java
     * element.
     * <p>
     * Returns null if there is no target Java element for the given original Java element, which
     * means that the original Java element does not need to be refactored.
     * <p>
     * The default implementation just compares the index of the original Java element to the index
     * of the target Java element.
     * <p>
     * Subclasses should override this method as necessary.
     * 
     * @param originalJavaElement The original Java element to find the corresponding target Java
     *            element for
     */
    protected IJavaElement getTargetJavaElementForOriginalJavaElement(IJavaElement originalJavaElement) {
        int originalIndex = getOriginalJavaElements().indexOf(originalJavaElement);
        if (getTargetJavaElements().size() <= originalIndex) {
            return null;
        }
        return getTargetJavaElements().get(originalIndex);
    }

    /**
     * Initializes the original Java elements generated for the given
     * {@link IIpsObjectPartContainer}.
     * <p>
     * This implementation asks the builder set for the generated elements of the given
     * {@link IIpsObjectPartContainer}. This behavior may be overwritten by subclasses.
     */
    private void initializeOriginalJavaElements(IIpsObjectPartContainer ipsObjectPartContainer,
            IJavaBuilderSet builderSet) {
        originalJavaElements = initializeJavaElements(ipsObjectPartContainer, builderSet);
    }

    /**
     * Initialize the Java elements that will be generated after the current refactoring.
     */
    private void initializeTargetJavaElements(IpsRefactoringProcessor processor, IJavaBuilderSet javaBuilderSet) {
        if (processor instanceof IIpsMoveRenameIpsObjectProcessor) {
            targetJavaElements = ((IIpsMoveRenameIpsObjectProcessor)processor).getTargetJavaElements();
        } else {
            IpsRefactoringModificationSet modificationSet = null;
            try {
                modificationSet = processor.refactorIpsModel(new NullProgressMonitor());
                targetJavaElements = initializeJavaElements(
                        (IIpsObjectPartContainer)modificationSet.getTargetElement(), javaBuilderSet);
            } finally {
                if (modificationSet != null) {
                    modificationSet.undo();
                }
            }
        }
    }

    protected List<IJavaElement> initializeJavaElements(IIpsObjectPartContainer ipsObjectPartContainer,
            IJavaBuilderSet builderSet) {
        return builderSet.getGeneratedJavaElements(ipsObjectPartContainer);
    }

    /**
     * Allows subclasses to set the list of {@link IJavaElement}s that will be generated for the
     * {@link IIpsElement} after the refactoring has finished.
     * 
     * @param targetJavaElements The list of generated {@link IJavaElement}s after the refactoring
     *            has finished
     * 
     * @throws NullPointerException If the given list is null
     */
    protected final void setTargetJavaElements(List<IJavaElement> targetJavaElements) {
        ArgumentCheck.notNull(targetJavaElements);
        this.targetJavaElements = targetJavaElements;
    }

    private final List<IJavaElement> getOriginalJavaElements() {
        return originalJavaElements;
    }

    private final List<IJavaElement> getTargetJavaElements() {
        return targetJavaElements;
    }

    /**
     * Subclass implementation that is responsible for instantiating and returning an appropriate
     * Java refactoring for the given original and target Java element.
     * <p>
     * If no refactoring has to be performed, null may be returned.
     * 
     * @param originalJavaElement The original Java element as it is before the refactoring is
     *            performed
     * @param targetJavaElement The target Java element as it shall be after the refactoring was
     *            performed
     * @param status A {@link RefactoringStatus} to report problems to
     * @param progressMonitor The {@link IProgressMonitor} to report progress to should that be
     *            necessary
     * 
     * @throws IpsException If an error occurs during creation of the refactoring instance
     */
    protected abstract JavaRefactoring createJavaRefactoring(IJavaElement originalJavaElement,
            IJavaElement targetJavaElement,
            RefactoringStatus status,
            IProgressMonitor progressMonitor) throws CoreException;

    private int getNumberOfJavaElementsToRefactor() {
        return originalJavaElements.size();
    }

}
