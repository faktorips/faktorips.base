/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.util.RefactorUtil;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.ArgumentCheck;

/**
 * Because all refactoring participants must be derived from an abstract LTK base class we can't use
 * inheritance ourselves to provide common functionality for the standard builder refactoring
 * participants.
 * <p>
 * Instead of using inheritance every participant may hold a <tt>RefactoringParticipantHelper</tt>.
 * 
 * @author Alexander Weickmann
 */
public abstract class RefactoringParticipantHelper {

    /**
     * List containing the <tt>IJavaElement</tt>s generated originally for the <tt>IIpsElement</tt>
     * to be refactored.
     */
    private List<IJavaElement> originalJavaElements;

    /**
     * List containing the <tt>IJavaElement</tt>s generated for the <tt>IIpsElement</tt> to be
     * refactored as they will be after the refactoring. This information is needed to be able to
     * provide the JDT refactorings with the correct new names for the <tt>IJavaElement</tt>s.
     */
    private List<IJavaElement> targetJavaElements;

    /**
     * Checks the conditions of the JDT refactorings to be performed.
     * 
     * @see RefactoringParticipant#checkConditions(IProgressMonitor, CheckConditionsContext)
     */
    public final RefactoringStatus checkConditions(IProgressMonitor pm) throws OperationCanceledException {
        RefactoringStatus status = new RefactoringStatus();

        for (int i = 0; i < originalJavaElements.size(); i++) {
            IJavaElement javaElement = originalJavaElements.get(i);

            // The refactoring may be executed without present Java code.
            if (!(javaElement.exists())) {
                continue;
            }

            try {
                Refactoring jdtRefactoring = createJdtRefactoring(javaElement, targetJavaElements.get(i), status);
                if (jdtRefactoring != null) {
                    status.merge(jdtRefactoring.checkAllConditions(pm));
                }
            } catch (CoreException e) {
                RefactoringStatus errorStatus = new RefactoringStatus();
                errorStatus.addFatalError(e.getLocalizedMessage());
                return errorStatus;
            }
        }

        // Assure that every message is only contained and thus shown once.
        RefactoringStatus finalStatus = new RefactoringStatus();
        List<String> messages = new ArrayList<String>(status.getEntries().length);
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
     * Iterates over all generated <tt>IJavaElement</tt>s and performs the appropriate JDT
     * refactorings.
     * 
     * @see RefactoringParticipant#createChange(IProgressMonitor)
     */
    public final Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        for (int i = 0; i < originalJavaElements.size(); i++) {
            IJavaElement originalJavaElement = originalJavaElements.get(i);

            /*
             * Do not try to refactor non-existing Java elements as the user may want to try to
             * start the refactoring when there is no source code at all. This also solves the
             * problem of what should happen when there is a Java element that occurs in the
             * implementation as well as in the published interface. If for example a setter method
             * occurs in the implementation as well as in the published interface then the first
             * encountered will be refactored. The second no longer exists then because the JDT
             * rename method refactoring renamed it already.
             */
            if (!(originalJavaElement.exists())) {
                continue;
            }

            /*
             * We can't use the refactoring instances created during condition checking because the
             * Java references are build upon creation of the refactoring instance. These might
             * become invalid when refactorings are performed. Because of that new instances are
             * created here.
             */
            IJavaElement targetJavaElement = targetJavaElements.get(i);
            Refactoring jdtRefactoring = createJdtRefactoring(originalJavaElement, targetJavaElement,
                    new RefactoringStatus());
            if (jdtRefactoring != null) {
                performRefactoring(jdtRefactoring, pm);
            }
        }

        return new NullChange();
    }

    /** Executes the given <tt>Refactoring</tt>. */
    private void performRefactoring(final Refactoring refactoring, final IProgressMonitor pm) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                IWorkspaceRunnable operation = new PerformRefactoringOperation(refactoring,
                        CheckConditionsOperation.FINAL_CONDITIONS);
                try {
                    operation.run(pm);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * This implementation initializes the list of generated <tt>IJavaElement</tt>s for the provided
     * <tt>IIpsElement</tt>.
     * <p>
     * Returns <tt>false</tt> in case the element passed to this operation is not an
     * <tt>IIpsElement</tt>. Else the subclass implementation is called to initialize the
     * <tt>IJavaElement</tt>s that will be generated for the <tt>IIpsElement</tt> after the
     * refactoring has finished and <tt>true</tt> is returned.
     */
    public final boolean initialize(Object element) {
        if (!(element instanceof IIpsElement)) {
            return false;
        }

        IIpsElement ipsElement = (IIpsElement)element;
        StandardBuilderSet builderSet = (StandardBuilderSet)ipsElement.getIpsProject().getIpsArtefactBuilderSet();

        boolean success = initializeOriginalJavaElements(ipsElement, builderSet);
        if (success) {
            success = initializeTargetJavaElements(ipsElement, builderSet);
        }
        return success;
    }

    /**
     * Initializes the original Java elements generated for the given <tt>IIpsElement</tt>. This
     * implementation asks the builder set for the generated elements of the given
     * <tt>IIpsElement</tt>. This behavior may be overwritten by subclasses.
     */
    protected boolean initializeOriginalJavaElements(IIpsElement ipsElement, StandardBuilderSet builderSet) {
        originalJavaElements = builderSet.getGeneratedJavaElements(ipsElement);
        return true;
    }

    /**
     * Subclass implementation responsible for initializing the <tt>IJavaElement</tt>s that will be
     * generated for the <tt>IIpsElement</tt> after the refactoring has finished.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be refactored.
     * @param builderSet A reference to the <tt>StandardBuilderSet</tt> to ask for generated Java
     *            elements.
     */
    protected abstract boolean initializeTargetJavaElements(IIpsElement ipsElement, StandardBuilderSet builderSet);

    /**
     * Initializes the target <tt>IJavaElement</tt>s for the given <tt>IIpsObject</tt>.
     * 
     * @param ipsObject The <tt>IIpsObject</tt> to be refactored.
     * @param targetIpsPackageFragment The new <tt>IIpsPackageFragment</tt> of the <tt>IType</tt>.
     * @param newName The new name of the <tt>IType</tt>.
     * @param builderSet A reference to the <tt>StandardBuilderSet</tt> to ask for generated Java
     *            elements.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected final boolean initTargetJavaElements(IIpsObject ipsObject,
            IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            StandardBuilderSet builderSet) {

        ArgumentCheck.notNull(new Object[] { ipsObject, targetIpsPackageFragment, newName, builderSet });

        Change undoDeleteChange = new NullChange();
        try {
            // 1) Create temporary copy with time stamp to avoid file system problems.
            IIpsSrcFile tempSrcFile = RefactorUtil.copyIpsSrcFileToTemporary(ipsObject.getIpsSrcFile(),
                    targetIpsPackageFragment, newName, null);
            IPath originalResourcePath = ipsObject.getIpsSrcFile().getCorrespondingResource().getFullPath();

            // 2) Delete original source file with delete refactoring for undo possibility.
            DeleteResourceChange deleteResourceChange = new DeleteResourceChange(originalResourcePath, true);
            undoDeleteChange = deleteResourceChange.perform(null);

            // 3) Copy the temporary file to create the target file.
            IIpsSrcFile targetSrcFile = RefactorUtil.copyIpsSrcFile(tempSrcFile, targetIpsPackageFragment, newName,
                    null);

            // 4) Delete the temporary file, we don't need it any longer.
            tempSrcFile.getCorrespondingResource().delete(true, null);

            // 5) Obtain the generated Java elements for the target IPS object.
            IIpsObject targetIpsObject = targetSrcFile.getIpsObject();
            targetJavaElements = builderSet.getGeneratedJavaElements(targetIpsObject);

            // 6) Clean up by deleting the target source file.
            targetSrcFile.getCorrespondingResource().delete(true, null);

        } catch (CoreException e) {
            // The participant won't be initialized if a CoreException occurs.
            IpsPlugin.log(e);
            return false;

        } finally {
            // Roll-back by performing the undo change of the delete refactoring.
            try {
                undoDeleteChange.perform(new NullProgressMonitor());
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    /**
     * Allows subclasses to set the list of <tt>IJavaElement</tt>s that will be generated for the
     * <tt>IIpsElement</tt> after the refactoring has finished.
     * 
     * @param targetJavaElements The list of <tt>IJavaElement</tt>s.
     * 
     * @throws NullPointerException If <tt>targetJavaElements</tt> is <tt>null</tt>.
     */
    protected final void setTargetJavaElements(List<IJavaElement> targetJavaElements) {
        ArgumentCheck.notNull(targetJavaElements);
        this.targetJavaElements = targetJavaElements;
    }

    protected final List<IJavaElement> getOriginalJavaElements() {
        return originalJavaElements;
    }

    /**
     * Subclass implementation that is responsible for returning an appropriate JDT refactoring for
     * the given Java element. May return <tt>null</tt> if no refactoring has to be performed.
     * 
     * @param originalJavaElement The original Java element as it is before the refactoring is
     *            performed.
     * @param targetJavaElement The target Java element as it shall be after the refactoring was
     *            performed.
     * @param status A <tt>RefactoringStatus</tt> to report any problems to.
     * 
     * @throws CoreException If an error occurs during creation of the refactoring instance.
     */
    protected abstract Refactoring createJdtRefactoring(IJavaElement originalJavaElement,
            IJavaElement targetJavaElement,
            RefactoringStatus status) throws CoreException;

}
