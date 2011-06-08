/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpttype;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsValidation;
import org.faktorips.devtools.core.IpsValidationTask;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeValidations;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
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

    public NewProductCmptTypePage(IStructuredSelection selection) {
        super(IpsObjectType.PRODUCT_CMPT_TYPE, selection, Messages.NewProductCmptTypePage_title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/NewProductCmptTypeWizard.png")); //$NON-NLS-1$
    }

    @Override
    protected IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit) {
        return toolkit.createProductCmptTypeRefControl(null, container, false);
    }

    /**
     * Adds the setting of the selected PolicyCmptType.
     */
    @Override
    protected void setDefaults(IResource selectedResource) throws CoreException {
        super.setDefaults(selectedResource);

        IIpsObject ipsObject = getSelectedIpsObject();
        if (ipsObject instanceof IPolicyCmptType) {
            IPolicyCmptType selectedPcType = (IPolicyCmptType)ipsObject;
            if (StringUtils.isEmpty(selectedPcType.getProductCmptType())) {
                pcTypeField.setValue(selectedPcType.getQualifiedName());
                abstractField.setValue(new Boolean(selectedPcType.isAbstract()));
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
        pcTypeField = new TextButtonField(pcTypeControl);
        pcTypeField.addChangeListener(this);
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
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();

        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root != null) {
            ((IpsObjectRefControl)pcTypeField.getControl()).setIpsProject(root.getIpsProject());
        } else {
            ((IpsObjectRefControl)pcTypeField.getControl()).setIpsProject(null);
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
        String configuredpcType = pcTypeField.getValue();
        if (!StringUtils.isEmpty(configuredpcType)) {
            IPolicyCmptType policyCmptType = getIpsProject().findPolicyCmptType(configuredpcType);
            if (policyCmptType != null) {
                policyCmptType.setConfigurableByProductCmptType(true);
                policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
                modifiedIpsObjects.add(policyCmptType);
                productCmptType.setConfigurationForPolicyCmptType(true);
                productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
                productCmptType.setAbstract(policyCmptType.isAbstract());
            }
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
        // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
        validation.addTask(new ValidatePcTypeDoesNotExist());
        validation.addTask(new ValidatePcTypeAlreadyConfigured());
        validation.addTask(new ValidatePolicyCmptSuperTypeNeedsToBeX());
        validation.addTask(new ValidateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract());
    }

    // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
    private class ValidatePcTypeDoesNotExist extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) throws CoreException {
            if (StringUtils.isEmpty(pcTypeField.getValue())) {
                return null;
            }

            IPolicyCmptType configuableType = ipsProject.findPolicyCmptType(pcTypeField.getValue());
            if (configuableType == null) {
                return new Message("", Messages.NewProductCmptTypePage_msgPcTypeDoesNotExist, Message.ERROR); //$NON-NLS-1$
            }

            return null;
        }

    }

    // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
    private class ValidatePcTypeAlreadyConfigured extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) throws CoreException {
            if (StringUtils.isEmpty(pcTypeField.getValue())) {
                return null;
            }

            IPolicyCmptType configuableType = ipsProject.findPolicyCmptType(pcTypeField.getValue());
            if (!StringUtils.isEmpty(configuableType.getProductCmptType())
                    && !configuableType.getProductCmptType().equals(getQualifiedIpsObjectName())) {
                return new Message("", Messages.NewProductCmptTypePage_msgPcTypeAlreadyConfigured, Message.ERROR); //$NON-NLS-1$
            }

            return null;
        }

    }

    // TODO AW 08-06-2011: Experimental usage of validation concept, see FIPS-571
    private class ValidatePolicyCmptSuperTypeNeedsToBeX extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) throws CoreException {
            if (StringUtils.isEmpty(pcTypeField.getValue())) {
                return null;
            }

            IPolicyCmptType configuableType = ipsProject.findPolicyCmptType(pcTypeField.getValue());

            FindNextConfiguredSuperType finder = new FindNextConfiguredSuperType(ipsProject);
            IProductCmptType superType = ipsProject.findProductCmptType(getSuperType());
            finder.start(superType);
            if (finder.nextConfiguringSupertype != null) {
                IPolicyCmptType superTypePolicyCmptType = ipsProject
                        .findPolicyCmptType(finder.qualifiedNameOfConfiguredType);
                if (superTypePolicyCmptType != null) {
                    IPolicyCmptType superPcType = (IPolicyCmptType)configuableType.findSupertype(ipsProject);
                    if (superPcType == null || !superPcType.equals(superTypePolicyCmptType)) {
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
        public Message execute(IIpsProject ipsProject) throws CoreException {
            if (StringUtils.isEmpty(pcTypeField.getValue())) {
                return null;
            }

            IPolicyCmptType configuableType = ipsProject.findPolicyCmptType(pcTypeField.getValue());
            return ProductCmptTypeValidations.validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(
                    configuableType.isAbstract(), getAbstract(), null);
        }

    }

    private static class FindNextConfiguredSuperType extends TypeHierarchyVisitor<IProductCmptType> {

        private IProductCmptType nextConfiguringSupertype;

        private String qualifiedNameOfConfiguredType;

        public FindNextConfiguredSuperType(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            if (!StringUtils.isEmpty(currentType.getPolicyCmptType())) {
                nextConfiguringSupertype = currentType;
                qualifiedNameOfConfiguredType = currentType.getPolicyCmptType();
                return false;
            }
            return true;
        }

    }

}
