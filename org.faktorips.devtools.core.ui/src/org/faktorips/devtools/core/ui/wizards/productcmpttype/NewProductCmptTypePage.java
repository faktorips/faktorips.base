/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.NewWizardUtil;
import org.faktorips.devtools.core.ui.wizards.type.NewTypePage;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeValidations;
import org.faktorips.devtools.model.internal.type.TypeValidations;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.plugin.IpsValidation;
import org.faktorips.devtools.model.plugin.IpsValidationTask;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.runtime.Message;

public class NewProductCmptTypePage extends NewTypePage {

    private TextButtonField policyCmptTypeField;

    public NewProductCmptTypePage(IStructuredSelection selection) {
        super(IpsObjectType.PRODUCT_CMPT_TYPE, selection, Messages.NewProductCmptTypePage_title);
        setImageDescriptor(
                IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/NewProductCmptTypeWizard.png")); //$NON-NLS-1$
    }

    @Override
    protected IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit) {
        return toolkit.createProductCmptTypeRefControl(null, container, false);
    }

    /**
     * Adds the setting of the selected PolicyCmptType.
     */
    @Override
    protected void setDefaults(IResource selectedResource) {
        super.setDefaults(selectedResource);

        IIpsObject ipsObject = getSelectedIpsObject();
        if (ipsObject instanceof IPolicyCmptType) {
            IPolicyCmptType selectedPcType = (IPolicyCmptType)ipsObject;
            if (StringUtils.isEmpty(selectedPcType.getProductCmptType())) {
                policyCmptTypeField.setValue(selectedPcType.getQualifiedName());
                getAbstractField().setValue(selectedPcType.isAbstract());
            }
        }
    }

    @Override
    public boolean canCreateIpsSrcFile() {
        return true;
    }

    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);

        toolkit.createFormLabel(nameComposite, Messages.NewProductCmptTypePage_labelConfigures);
        IpsObjectRefControl pcTypeControl = toolkit.createPcTypeRefControl(null, nameComposite);
        policyCmptTypeField = new TextButtonField(pcTypeControl);
        policyCmptTypeField.addChangeListener(this);
        addAbstractField(nameComposite, toolkit);
    }

    private IPolicyCmptType getPolicyCmptType() {
        String pcTypeQualifiedName = getPolicyCmptTypeName();
        if (getIpsProject() != null) {
            IPolicyCmptType policyCmptType = getIpsProject().findPolicyCmptType(pcTypeQualifiedName);
            if (policyCmptType != null) {
                return policyCmptType;
            }
        }
        return null;
    }

    protected String getPolicyCmptTypeName() {
        return policyCmptTypeField.getText();
    }

    @Override
    protected void valueChangedExtension(FieldValueChangedEvent e) {
        super.valueChangedExtension(e);

        if (e.field == policyCmptTypeField) {
            IPolicyCmptType policyCmptType = getPolicyCmptType();
            if (policyCmptType != null) {
                setAbstract(policyCmptType.isAbstract());
            }
        }
    }

    @Override
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();

        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root != null) {
            ((IpsObjectRefControl)policyCmptTypeField.getControl()).setIpsProjects(Arrays.asList(root.getIpsProject()));
        } else {
            ((IpsObjectRefControl)policyCmptTypeField.getControl()).setIpsProjects(new ArrayList<IIpsProject>());
        }
    }

    @Override
    protected void validateName() {
        super.validateName();

        if (getIpsProject() == null) {
            return;
        }

        setErrorMessage(
                TypeValidations.validateUniqueQualifiedName(IpsObjectType.POLICY_CMPT_TYPE, getQualifiedIpsObjectName(),
                        getIpsProject()).getMessageWithHighestSeverity());
    }

    @Override
    protected void finishIpsObjectsExtension(IIpsObject newIpsObject, Set<IIpsObject> modifiedIpsObjects) {

        super.finishIpsObjectsExtension(newIpsObject, modifiedIpsObjects);

        IProductCmptType productCmptType = (IProductCmptType)newIpsObject;

        NewWizardUtil.createDefaultCategoriesIfNecessary(productCmptType);

        associatePolicyCmptType(modifiedIpsObjects, productCmptType);

        setChangingOverTimeAsInSupertype(productCmptType);
    }

    private void setChangingOverTimeAsInSupertype(IProductCmptType productCmptType) {
        String superTypeName = getSuperType();
        if (!StringUtils.isBlank(superTypeName)) {
            IProductCmptType superType = getIpsProject().findProductCmptType(superTypeName);
            productCmptType.setChangingOverTime(superType.isChangingOverTime());
        }
    }

    private void associatePolicyCmptType(Set<IIpsObject> modifiedIpsObjects, IProductCmptType productCmptType) {

        String policyCmptTypeQualifiedName = getPolicyCmptTypeName();
        if (StringUtils.isEmpty(policyCmptTypeQualifiedName)) {
            productCmptType.setConfigurationForPolicyCmptType(false);
            return;
        }

        IPolicyCmptType policyCmptType = getIpsProject().findPolicyCmptType(policyCmptTypeQualifiedName);
        if (policyCmptType == null) {
            productCmptType.setConfigurationForPolicyCmptType(false);
            return;
        }

        boolean configuresSamePolicyCmptTypeAsSupertype = policyCmptTypeQualifiedName
                .equals(findNextConfiguredSuperTypeQualifiedName());

        if (!configuresSamePolicyCmptTypeAsSupertype) {
            policyCmptType.setConfigurableByProductCmptType(true);
            policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
            modifiedIpsObjects.add(policyCmptType);
        }
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        productCmptType.setAbstract(policyCmptType.isAbstract());
    }

    private String findNextConfiguredSuperTypeQualifiedName() {
        String superTypeQualifiedName = getSuperType();
        if (StringUtils.isBlank(superTypeQualifiedName)) {
            return null;
        }
        IIpsProject ipsProject = getIpsProject();
        FindNextConfiguredSuperType finder = new FindNextConfiguredSuperType(ipsProject);
        IProductCmptType superType = ipsProject.findProductCmptType(superTypeQualifiedName);
        finder.start(superType);
        return finder.qualifiedNameOfConfiguredType;
    }

    /**
     * Validates if the supertype of the product component type is within the super type hierarchy
     * of the product component type of the supertype of the policy component type.
     */
    @Override
    protected void validatePageExtensionThis(IpsValidation validation) {
        // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
        validation.addTask(new ValidatePcTypeDoesNotExist());
        validation.addTask(new ValidatePcTypeAlreadyConfigured());
        validation.addTask(new ValidatePolicyCmptSuperTypeNeedsToBeX());
        validation.addTask(new ValidateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract());
    }

    // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
    private class ValidatePcTypeDoesNotExist extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) {
            if (StringUtils.isEmpty(policyCmptTypeField.getValue())) {
                return null;
            }

            IPolicyCmptType configuableType = ipsProject.findPolicyCmptType(policyCmptTypeField.getValue());
            if (configuableType == null) {
                return new Message("", Messages.NewProductCmptTypePage_msgPcTypeDoesNotExist, Message.ERROR); //$NON-NLS-1$
            }

            return null;
        }

    }

    // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
    private class ValidatePcTypeAlreadyConfigured extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) {
            if (StringUtils.isEmpty(policyCmptTypeField.getValue())) {
                return null;
            }
            String nextConfiguredSuperTypeQualifiedName = findNextConfiguredSuperTypeQualifiedName();
            if (StringUtils.isNotBlank(nextConfiguredSuperTypeQualifiedName)
                    && nextConfiguredSuperTypeQualifiedName.equals(policyCmptTypeField.getValue())) {
                return new Message("", Messages.NewProductCmptTypePage_msgPcTypeConfiguredBySuperType, Message.INFO); //$NON-NLS-1$
            }

            IPolicyCmptType configuredType = ipsProject.findPolicyCmptType(policyCmptTypeField.getValue());
            if (!StringUtils.isEmpty(configuredType.getProductCmptType())
                    && !configuredType.getProductCmptType().equals(getQualifiedIpsObjectName())) {
                return new Message("", Messages.NewProductCmptTypePage_msgPcTypeAlreadyConfigured, Message.ERROR); //$NON-NLS-1$
            }

            return null;
        }

    }

    // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
    private class ValidatePolicyCmptSuperTypeNeedsToBeX extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) {
            if (StringUtils.isEmpty(policyCmptTypeField.getValue())) {
                return null;
            }

            IPolicyCmptType configuredType = ipsProject.findPolicyCmptType(policyCmptTypeField.getValue());
            String superTypePolicyCmptTypeQualifiedName = findNextConfiguredSuperTypeQualifiedName();
            if (superTypePolicyCmptTypeQualifiedName != null) {
                IPolicyCmptType superTypePolicyCmptType = ipsProject
                        .findPolicyCmptType(superTypePolicyCmptTypeQualifiedName);
                if (superTypePolicyCmptType != null) {
                    IPolicyCmptType superPcType = (IPolicyCmptType)configuredType.findSupertype(ipsProject);
                    if (!configuredType.equals(superTypePolicyCmptType)
                            && (superPcType == null || !superPcType.equals(superTypePolicyCmptType))) {
                        String text = NLS.bind(Messages.NewProductCmptTypePage_msgPolicyCmptSuperTypeNeedsToBeX,
                                superTypePolicyCmptType.getQualifiedName());
                        return new Message("", text, Message.ERROR); //$NON-NLS-1$
                    }
                }
            }

            return null;
        }

    }

    // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
    private class ValidateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) {
            if (StringUtils.isEmpty(policyCmptTypeField.getValue())) {
                return null;
            }

            IPolicyCmptType configuableType = ipsProject.findPolicyCmptType(policyCmptTypeField.getValue());
            return ProductCmptTypeValidations.validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(
                    configuableType.isAbstract(), isAbstract(), null);
        }

    }

    private static class FindNextConfiguredSuperType extends TypeHierarchyVisitor<IProductCmptType> {

        private String qualifiedNameOfConfiguredType;

        public FindNextConfiguredSuperType(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) {
            if (!StringUtils.isEmpty(currentType.getPolicyCmptType())) {
                qualifiedNameOfConfiguredType = currentType.getPolicyCmptType();
                return false;
            }
            return true;
        }

    }

}
