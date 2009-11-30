/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.refactor;

import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class RenameParticipant extends org.eclipse.ltk.core.refactoring.participants.RenameParticipant {

    private List<IJavaElement> generatedJavaElements;

    private List<IJavaElement> newJavaElements;

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws OperationCanceledException {

        RefactoringStatus status = new RefactoringStatus();
        for (IJavaElement javaElement : generatedJavaElements) {
            if (!(javaElement.exists())) {
                status.addFatalError("Missing Java source code element for this IPS element: " + javaElement);
            }
        }
        return status;
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        for (int i = 0; i < generatedJavaElements.size(); i++) {
            String javaRefactoringContributionId;
            switch (generatedJavaElements.get(i).getElementType()) {
                case IJavaElement.FIELD:
                    javaRefactoringContributionId = IJavaRefactorings.RENAME_FIELD;
                    break;
                case IJavaElement.METHOD:
                    javaRefactoringContributionId = IJavaRefactorings.RENAME_METHOD;
                    break;
                default:
                    throw new RuntimeException("This kind of Java element is not supported by the refactoring.");
            }
            rename(generatedJavaElements.get(i), newJavaElements.get(i).getElementName(),
                    javaRefactoringContributionId, pm);
        }

        return null;
    }

    private void rename(IJavaElement javaElement,
            String newName,
            String javaRefactoringContributionId,
            final IProgressMonitor pm) throws OperationCanceledException, CoreException {

        RefactoringContribution contribution = RefactoringCore
                .getRefactoringContribution(javaRefactoringContributionId);
        RenameJavaElementDescriptor descriptor = (RenameJavaElementDescriptor)contribution.createDescriptor();
        descriptor.setJavaElement(javaElement);
        descriptor.setNewName(newName);
        descriptor.setUpdateReferences(getArguments().getUpdateReferences());

        RefactoringStatus status = new RefactoringStatus();
        Refactoring renameRefactoring = descriptor.createRefactoring(status);
        if (status.isOK()) {
            final PerformRefactoringOperation operation = new PerformRefactoringOperation(renameRefactoring,
                    CheckConditionsOperation.ALL_CONDITIONS);
            Display display = (Display.getCurrent() != null) ? Display.getCurrent() : Display.getDefault();
            display.syncExec(new Runnable() {
                public void run() {
                    try {
                        ResourcesPlugin.getWorkspace().run(operation, pm);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    @Override
    protected boolean initialize(Object element) {
        if (!(element instanceof IIpsElement)) {
            return false;
        }

        IIpsElement ipsElement = (IIpsElement)element;
        StandardBuilderSet builderSet = (StandardBuilderSet)ipsElement.getIpsProject().getIpsArtefactBuilderSet();
        generatedJavaElements = builderSet.getGeneratedJavaElements(ipsElement);

        String oldName = ipsElement.getName();
        ipsElement.setName(getArguments().getNewName());
        newJavaElements = builderSet.getGeneratedJavaElements(ipsElement);
        ipsElement.setName(oldName);

        return true;
    }

    @Override
    public String getName() {
        return "RenameParticipant";
    }

}
