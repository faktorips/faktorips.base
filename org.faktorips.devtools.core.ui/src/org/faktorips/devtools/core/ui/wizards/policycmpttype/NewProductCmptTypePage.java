/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.policycmpttype;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.NewWizardUtil;
import org.faktorips.devtools.core.ui.wizards.type.NewTypePage;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeValidations;
import org.faktorips.devtools.model.internal.type.TypeValidations;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.plugin.IpsValidation;
import org.faktorips.devtools.model.plugin.IpsValidationTask;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.Message;

public class NewProductCmptTypePage extends NewTypePage {

    private TextButtonField pcTypeField;

    public NewProductCmptTypePage(IStructuredSelection selection, NewPcTypePage pcTypePage) {
        super(IpsObjectType.PRODUCT_CMPT_TYPE, selection, Messages.NewProductCmptTypePage_title);
        setPageOfAssociatedType(pcTypePage);
        setImageDescriptor(
                IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/NewProductCmptTypeWizard.png")); //$NON-NLS-1$
    }

    @Override
    protected IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit) {
        return toolkit.createProductCmptTypeRefControl(null, container, false);
    }

    /**
     * Sets default values to the fields of this page.
     */
    @Override
    public void pageEntered() throws CoreRuntimeException {
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
            setAbstract(getPageOfAssociatedType().isAbstract());
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

    private IPolicyCmptType getPolicyCmptType() {
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
    protected void valueChangedExtension(FieldValueChangedEvent e) throws CoreRuntimeException {
        super.valueChangedExtension(e);

        if (e.field == pcTypeField) {
            IPolicyCmptType policyCmptType = getPolicyCmptType();
            if (policyCmptType != null) {
                setAbstract(policyCmptType.isAbstract());
            }
        }
    }

    @Override
    protected void validateName() throws CoreRuntimeException {
        super.validateName();

        if (getIpsProject() == null) {
            return;
        }

        setErrorMessage(TypeValidations.validateUniqueQualifiedName(IpsObjectType.POLICY_CMPT_TYPE,
                getQualifiedIpsObjectName(), getIpsProject())
                .getMessageWithHighestSeverity());
    }

    @Override
    protected void finishIpsObjectsExtension(IIpsObject newIpsObject, Set<IIpsObject> modifiedIpsObjects)
            throws CoreRuntimeException {

        super.finishIpsObjectsExtension(newIpsObject, modifiedIpsObjects);

        IProductCmptType productCmptType = (IProductCmptType)newIpsObject;

        NewWizardUtil.createDefaultCategoriesIfNecessary(productCmptType);

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
    protected void validatePageExtensionThis(IpsValidation validation) throws CoreRuntimeException {
        // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
        validation.addTask(new ValidateSupertype());
        validation.addTask(new ValidateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract());
        validation.addTask(new ValidatePolicyCmptSuperTypeNeedsToBeX());
    }

    // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
    private class ValidateSupertype extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) throws CoreRuntimeException {
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

    // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
    private class ValidateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) throws CoreRuntimeException {
            return ProductCmptTypeValidations.validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(
                    getPageOfAssociatedType().isAbstract(), isAbstract(), null);
        }

    }

    // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
    private class ValidatePolicyCmptSuperTypeNeedsToBeX extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) throws CoreRuntimeException {
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
