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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.refactoring.structure.PullUpRefactoringProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.IpsPullUpArguments;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

// There's no other way to perform the Java pull up refactoring.
@SuppressWarnings("restriction")
public class PullUpRefactoringParticipant extends RefactoringParticipant {

    private PullUpParticipantHelper refactoringHelper;

    private IpsPullUpArguments arguments;

    public PullUpRefactoringParticipant() {
        refactoringHelper = new PullUpParticipantHelper();
    }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws OperationCanceledException {

        return refactoringHelper.checkConditions(pm);
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return refactoringHelper.createChange(pm);
    }

    @Override
    public String getName() {
        return "StandardBuilder Pull Up Participant"; //$NON-NLS-1$
    }

    @Override
    protected boolean initialize(Object element) {
        return refactoringHelper.initialize(element);
    }

    @Override
    protected void initialize(RefactoringArguments arguments) {
        this.arguments = (IpsPullUpArguments)arguments;
    }

    private class PullUpParticipantHelper extends RefactoringParticipantHelper {

        @Override
        protected Refactoring createJdtRefactoring(IJavaElement originalJavaElement,
                IJavaElement targetJavaElement,
                RefactoringStatus status) throws CoreException {

            if (!(originalJavaElement instanceof IMember && targetJavaElement instanceof IMember)) {
                throw new RuntimeException(
                        "This kind of Java element is not supported by the pull up refactoring participant."); //$NON-NLS-1$
            }

            IMember originalJavaMember = (IMember)originalJavaElement;
            IMember targetJavaMember = (IMember)targetJavaElement;
            if (originalJavaMember.getParent().equals(targetJavaMember.getParent())) {
                return null;
            }

            PullUpRefactoringProcessor processor = new PullUpRefactoringProcessor(new IMember[] { originalJavaMember },
                    JavaPreferencesSettings.getCodeGenerationSettings(originalJavaElement.getJavaProject()));
            processor.resetEnvironment();
            processor.setMembersToMove(new IMember[] { originalJavaMember });
            processor.setDestinationType(targetJavaMember.getDeclaringType());
            processor.setReplace(true);
            if (originalJavaMember.getElementType() == IJavaElement.METHOD) {
                processor.setDeletedMethods(new IMethod[] { (IMethod)originalJavaMember });
            }
            return new ProcessorBasedRefactoring(processor);
        }

        @Override
        protected boolean initializeTargetJavaElements(IIpsObjectPartContainer ipsObjectPartContainer,
                StandardBuilderSet builderSet) {

            boolean success = false;
            if (ipsObjectPartContainer instanceof IAttribute) {
                success = initializeTargetJavaElementsForAttribute((IAttribute)ipsObjectPartContainer, builderSet);
            }
            return success;
        }

        private boolean initializeTargetJavaElementsForAttribute(IAttribute attribute, StandardBuilderSet builderSet) {
            try {
                IType originalType = attribute.getType();
                IType targetType = originalType.findSupertype(originalType.getIpsProject());
                if (targetType == null) {
                    throw new RuntimeException("There is no supertype to pull up to."); //$NON-NLS-1$
                }
                IAttribute targetAttribute = targetType.newAttribute();
                // Temporary copy.
                copyAttributeToSupertype(attribute, targetAttribute);
                setTargetJavaElements(builderSet.getGeneratedJavaElements(targetAttribute));
                targetAttribute.delete();
                return true;
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        // TODO AW: Will cause errors if properties of attributes are changed.
        private void copyAttributeToSupertype(IAttribute attribute, IAttribute targetAttribute) {
            targetAttribute.setName(attribute.getName());
            targetAttribute.setDatatype(attribute.getDatatype());
            targetAttribute.setDefaultValue(attribute.getDefaultValue());
            for (IDescription description : attribute.getDescriptions()) {
                IDescription newDescription = targetAttribute.newDescription();
                newDescription.setLocale(description.getLocale());
                newDescription.setText(description.getText());
            }
            targetAttribute.setModifier(attribute.getModifier());
            if (attribute instanceof IPolicyCmptTypeAttribute) {
                IPolicyCmptTypeAttribute originalPolicyAttribute = (IPolicyCmptTypeAttribute)attribute;
                IPolicyCmptTypeAttribute targetPolicyAttribute = (IPolicyCmptTypeAttribute)targetAttribute;
                targetPolicyAttribute.setAttributeType(originalPolicyAttribute.getAttributeType());
                targetPolicyAttribute.setComputationMethodSignature(originalPolicyAttribute
                        .getComputationMethodSignature());
                targetPolicyAttribute.setOverwrite(originalPolicyAttribute.isOverwrite());
                targetPolicyAttribute.setProductRelevant(originalPolicyAttribute.isProductRelevant());
                targetPolicyAttribute.setValueSetType(originalPolicyAttribute.getValueSet().getValueSetType());
            } else if (attribute instanceof IProductCmptTypeAttribute) {
                IProductCmptTypeAttribute originalProductAttribute = (IProductCmptTypeAttribute)attribute;
                IProductCmptTypeAttribute targetProductAttribute = (IProductCmptTypeAttribute)targetAttribute;
                targetProductAttribute.setValueSetType(originalProductAttribute.getValueSet().getValueSetType());
            }
        }

    }

}
