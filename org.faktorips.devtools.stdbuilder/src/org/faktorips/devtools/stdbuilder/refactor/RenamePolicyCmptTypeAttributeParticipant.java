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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
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
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
// XXX AW: - REFACOTRING SUPPORT PROTOTYPE -
public class RenamePolicyCmptTypeAttributeParticipant extends RenameParticipant {

    private IPolicyCmptTypeAttribute policyCmptTypeAttribute;

    private IJavaElement propertyConstant;

    private IJavaElement setterMethod;

    private IJavaElement getterMethod;

    private IJavaElement implAttribute;

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws OperationCanceledException {

        RefactoringStatus status = new RefactoringStatus();
        if (!(propertyConstant.exists() && setterMethod.exists() && getterMethod.exists() && implAttribute.exists())) {
            status.addFatalError("Missing java source code for this attribute.");
        }
        return status;
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        // TODO AW: naming convention duplicate
        rename(propertyConstant, JavaNamingConvention.ECLIPSE_STANDARD.getConstantClassVarName("PROPERTY_"
                + getArguments().getNewName()), IJavaRefactorings.RENAME_FIELD, pm);
        rename(getterMethod, JavaNamingConvention.ECLIPSE_STANDARD.getGetterMethodName(getArguments().getNewName(),
                policyCmptTypeAttribute.getIpsProject().findDatatype(policyCmptTypeAttribute.getDatatype())),
                IJavaRefactorings.RENAME_METHOD, pm);
        rename(setterMethod, JavaNamingConvention.ECLIPSE_STANDARD.getSetterMethodName(getArguments().getNewName(),
                policyCmptTypeAttribute.getIpsProject().findDatatype(policyCmptTypeAttribute.getDatatype())),
                IJavaRefactorings.RENAME_METHOD, pm);
        rename(implAttribute, JavaNamingConvention.ECLIPSE_STANDARD.getMemberVarName(getArguments().getNewName()),
                IJavaRefactorings.RENAME_FIELD, pm);
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
        if (!(element instanceof IPolicyCmptTypeAttribute)) {
            return false;
        }

        policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)element;
        StandardBuilderSet builderSet = (StandardBuilderSet)policyCmptTypeAttribute.getIpsProject()
                .getIpsArtefactBuilderSet();
        for (IJavaElement javaElement : builderSet.getGeneratedJavaElements(policyCmptTypeAttribute)) {
            String javaElementName = javaElement.getElementName();
            if (javaElement instanceof IMethod) {
                if (javaElementName.startsWith("set")) {
                    setterMethod = javaElement;
                } else if (javaElementName.startsWith("get")) {
                    getterMethod = javaElement;
                }
            } else if (javaElement instanceof IField) {
                if (javaElementName.startsWith("PROPERTY_")) {
                    propertyConstant = javaElement;
                } else {
                    implAttribute = javaElement;
                }
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return "RenamePolicyCmptTypeAttributeParticipant";
    }

}
