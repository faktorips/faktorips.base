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
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ui.internal.Workbench;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
// XXX AW: - REFACOTRING SUPPORT PROTOTYPE -
@SuppressWarnings("restriction")
public class RenamePolicyCmptTypeAttributeParticipant extends RenameParticipant {

    private IPolicyCmptTypeAttribute policyCmptTypeAttribute;

    private List<IJavaElement> generatedJavaElements;

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws OperationCanceledException {
        return new RefactoringStatus();
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        executeRenameField(pm);
        executeRenameGetter(pm);
        return null;
    }

    private void executeRenameField(IProgressMonitor pm) throws OperationCanceledException, CoreException {
        RefactoringContribution contribution = RefactoringCore
                .getRefactoringContribution(IJavaRefactorings.RENAME_FIELD);
        RenameJavaElementDescriptor descriptor = (RenameJavaElementDescriptor)contribution.createDescriptor();
        descriptor.setJavaElement(generatedJavaElements.get(0));

        // TODO AW: naming convention duplicate
        descriptor.setNewName(JavaNamingConvention.ECLIPSE_STANDARD.getMemberVarName(getArguments().getNewName()));
        descriptor.setUpdateReferences(getArguments().getUpdateReferences());
        RefactoringStatus status = new RefactoringStatus();
        Refactoring renameFieldRefactoring = descriptor.createRefactoring(status);
        if (status.isOK()) {
            final PerformRefactoringOperation operation = new PerformRefactoringOperation(renameFieldRefactoring,
                    CheckConditionsOperation.ALL_CONDITIONS);
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        operation.run(null);
                    } catch (CoreException e) {
                        e.printStackTrace();
                    }
                }
            };
            Workbench.getInstance().getDisplay().asyncExec(runnable);
        }
    }

    private void executeRenameGetter(IProgressMonitor pm) throws OperationCanceledException, CoreException {
        RefactoringContribution contribution = RefactoringCore
                .getRefactoringContribution(IJavaRefactorings.RENAME_METHOD);
        RenameJavaElementDescriptor descriptor = (RenameJavaElementDescriptor)contribution.createDescriptor();
        descriptor.setJavaElement(generatedJavaElements.get(1));

        // TODO AW: naming convention duplicate
        descriptor.setNewName(JavaNamingConvention.ECLIPSE_STANDARD.getGetterMethodName(getArguments().getNewName(),
                policyCmptTypeAttribute.getIpsProject().findDatatype(policyCmptTypeAttribute.getDatatype())));
        descriptor.setUpdateReferences(getArguments().getUpdateReferences());
        RefactoringStatus status = descriptor.validateDescriptor();
        Refactoring renameMethodRefactoring = descriptor.createRefactoring(status);
        if (status.isOK()) {
            final PerformRefactoringOperation operation = new PerformRefactoringOperation(renameMethodRefactoring,
                    CheckConditionsOperation.ALL_CONDITIONS);
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        operation.run(null);
                    } catch (CoreException e) {
                        e.printStackTrace();
                    }
                }
            };
            Workbench.getInstance().getDisplay().asyncExec(runnable);
        }
    }

    @Override
    protected boolean initialize(Object element) {
        if (!(element instanceof IPolicyCmptTypeAttribute)) {
            return false;
        }
        policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)element;
        StandardBuilderSet builderSet = (StandardBuilderSet)policyCmptTypeAttribute.getIpsProject()
                .getIpsArtefactBuilderSet();
        generatedJavaElements = builderSet.getGeneratedJavaElements(policyCmptTypeAttribute);
        return true;
    }

    @Override
    public String getName() {
        return "RenamePolicyCmptTypeAttributeParticipant";
    }

}
