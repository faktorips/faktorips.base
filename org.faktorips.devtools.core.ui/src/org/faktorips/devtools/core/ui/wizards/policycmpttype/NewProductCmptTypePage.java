/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.policycmpttype;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsValidation;
import org.faktorips.devtools.core.IpsValidationTask;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeValidations;
import org.faktorips.devtools.core.model.type.TypeValidations;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.type.NewTypePage;
import org.faktorips.util.message.Message;

public class NewProductCmptTypePage extends NewTypePage {

    private TextButtonField pcTypeField;

    public NewProductCmptTypePage(IStructuredSelection selection, NewPcTypePage pcTypePage) {
        super(IpsObjectType.PRODUCT_CMPT_TYPE, selection, Messages.NewProductCmptTypePage_title);
        setPageOfAssociatedType(pcTypePage);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/NewProductCmptTypeWizard.png")); //$NON-NLS-1$
    }

    @Override
    protected IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit) {
        return toolkit.createProductCmptTypeRefControl(null, container, false);
    }

    /**
     * Sets default values to the fields of this page.
     */
    @Override
    public void pageEntered() throws CoreException {
        if (!isAlreadyBeenEntered()) {
            if (StringUtils.isEmpty(getPackage())) {
                setPackage(getPageOfAssociatedType().getPackage());
            }
            if (StringUtils.isEmpty(getSourceFolder())) {
                setSourceFolder(getPageOfAssociatedType().getSourceFolder());
            }
            if (StringUtils.isEmpty(getSuperType())) {
                if (!StringUtils.isEmpty(getPageOfAssociatedType().getSuperType())) {
                    IPolicyCmptType superPcType = getIpsProject().findPolicyCmptType(
                            getPageOfAssociatedType().getSuperType());
                    if (superPcType != null) {
                        setSuperType(superPcType.getProductCmptType());
                    }
                }
            }
            if (StringUtils.isEmpty(getIpsObjectName())) {
                String postfix = IpsPlugin.getDefault().getIpsPreferences().getDefaultProductCmptTypePostfix();
                if (!StringUtils.isEmpty(postfix)) {
                    setIpsObjectName(getPageOfAssociatedType().getIpsObjectName() + postfix);
                }
            }
            setAbstract(getPageOfAssociatedType().getAbstract());
        }
        super.pageEntered();
    }

    /**
     * Returns true if the policy component type is configurable.
     */
    @Override
    public boolean canCreateIpsSrcFile() {
        return ((NewPcTypePage)getPageOfAssociatedType()).isPolicyCmptTypeConfigurable();
    }

    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);
        addAbstractField(nameComposite, toolkit);
    }

    private IPolicyCmptType getPolicyCmptType() throws CoreException {
        String pcTypeQualifiedName = pcTypeField.getText();
        if (getIpsProject() != null) {
            IPolicyCmptType policyCmptType = getIpsProject().findPolicyCmptType(pcTypeQualifiedName);
            if (policyCmptType != null) {
                return policyCmptType;
            }
        }
        return null;
    }

    @Override
    protected void valueChangedExtension(FieldValueChangedEvent e) throws CoreException {
        super.valueChangedExtension(e);

        if (e.field == pcTypeField) {
            IPolicyCmptType policyCmptType = getPolicyCmptType();
            if (policyCmptType != null) {
                setAbstract(policyCmptType.isAbstract());
            }
        }
    }

    @Override
    protected void validateName() throws CoreException {
        super.validateName();

        if (getIpsProject() == null) {
            return;
        }

        setErrorMessage(TypeValidations.validateOtherTypeWithSameNameTypeInIpsObjectPath(
                IpsObjectType.POLICY_CMPT_TYPE, getQualifiedIpsObjectName(), getIpsProject(), null));
    }

    @Override
    protected void finishIpsObjectsExtension(IIpsObject newIpsObject, Set<IIpsObject> modifiedIpsObjects)
            throws CoreException {

        super.finishIpsObjectsExtension(newIpsObject, modifiedIpsObjects);

        IProductCmptType productCmptType = (IProductCmptType)newIpsObject;
        if (((NewPcTypePage)getPageOfAssociatedType()).isPolicyCmptTypeConfigurable()) {
            productCmptType.setConfigurationForPolicyCmptType(true);
            productCmptType.setPolicyCmptType(getPageOfAssociatedType().getQualifiedIpsObjectName());
            return;
        }
        productCmptType.setConfigurationForPolicyCmptType(false);
    }

    /**
     * Validates if the supertype of the product component type is within the super type hierarchy
     * of the product component type of the supertype of the policy component type.
     */
    @Override
    protected void validatePageExtensionThis(IpsValidation validation) throws CoreException {
        validation.addTask(new ValidateSupertype());
        validation.addTask(new ValidateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract());
        validation.addTask(new ValidatePolicyCmptSuperTypeNeedsToBeX());
    }

    private class ValidateSupertype extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) throws CoreException {
            if (StringUtils.isEmpty(getPageOfAssociatedType().getSuperType())) {
                return null;
            }

            IProductCmptType superProductCmptType = null;
            if (!StringUtils.isEmpty(getSuperType())) {
                superProductCmptType = ipsProject.findProductCmptType(getSuperType());
            }
            return ProductCmptTypeValidations.validateSupertype(null, superProductCmptType, getPageOfAssociatedType()
                    .getQualifiedIpsObjectName(), getPageOfAssociatedType().getSuperType(), ipsProject);
        }

    }

    private class ValidateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) throws CoreException {
            return ProductCmptTypeValidations.validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(
                    getPageOfAssociatedType().getAbstract(), getAbstract(), null);
        }

    }

    private class ValidatePolicyCmptSuperTypeNeedsToBeX extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) throws CoreException {
            if (StringUtils.isEmpty(getSuperType())) {
                return null;
            }

            IProductCmptType superType = ipsProject.findProductCmptType(getSuperType());
            if (superType != null && superType.isConfigurationForPolicyCmptType()) {
                String text = NLS.bind(Messages.NewProductCmptTypePage_msgPolicyCmptSuperTypeNeedsToBeX,
                        superType.getPolicyCmptType());
                Message msg = new Message("", text, Message.ERROR); //$NON-NLS-1$
                if (StringUtils.isEmpty(getPageOfAssociatedType().getSuperType())) {
                    return msg;
                }
                IPolicyCmptType policyCmptType = ipsProject
                        .findPolicyCmptType(getPageOfAssociatedType().getSuperType());
                if (policyCmptType == null) {
                    return msg;
                }
                if (!superType.getPolicyCmptType().equals(policyCmptType.getQualifiedName())) {
                    return msg;
                }
            }

            return null;
        }

    }

}
