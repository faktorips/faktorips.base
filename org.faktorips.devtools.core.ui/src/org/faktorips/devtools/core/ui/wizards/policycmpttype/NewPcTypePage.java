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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsValidation;
import org.faktorips.devtools.core.IpsValidationTask;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.TypeValidations;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.type.NewTypePage;
import org.faktorips.util.message.Message;

public class NewPcTypePage extends NewTypePage {

    private CheckboxField configurableField;

    public NewPcTypePage(IStructuredSelection selection) {
        super(IpsObjectType.POLICY_CMPT_TYPE, selection, Messages.NewPcTypePage_title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/NewPolicyCmptTypeWizard.png")); //$NON-NLS-1$
    }

    @Override
    protected IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit) {
        return toolkit.createPcTypeRefControl(null, container);
    }

    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);

        toolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        configurableField = new CheckboxField(toolkit.createCheckbox(nameComposite,
                Messages.NewPcTypePage_configuredByProductCmptType));
        configurableField.setValue(Boolean.valueOf(getSettings().getBoolean(
                IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE)));
        configurableField.addChangeListener(this);
        addAbstractField(nameComposite, toolkit);
    }

    /**
     * Returns true if the page is complete and the policy component type is configurable.
     */
    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete() && isPolicyCmptTypeConfigurable();
    }

    /**
     * Returns the value of the configurable field.
     */
    public boolean isPolicyCmptTypeConfigurable() {
        return Boolean.TRUE.equals(configurableField.getValue());
    }

    /**
     * Returns true if the page is complete and the policy component type is not configurable.
     */
    @Override
    public boolean finishWhenThisPageIsComplete() {
        return isPageComplete() && !isPolicyCmptTypeConfigurable();
    }

    private IDialogSettings getSettings() {
        IDialogSettings settings = IpsPlugin.getDefault().getDialogSettings().getSection("NewPcTypeWizard.PcTypePage"); //$NON-NLS-1$
        if (settings == null) {
            return IpsPlugin.getDefault().getDialogSettings().addNewSection("NewPcTypeWizard.PcTypePage"); //$NON-NLS-1$
        }
        return settings;
    }

    @Override
    protected void valueChangedExtension(FieldValueChangedEvent e) throws CoreException {
        super.valueChangedExtension(e);
        if (e.field == configurableField) {
            IDialogSettings settings = getSettings();
            settings.put(IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE,
                    (configurableField.getValue()).booleanValue());
        }
    }

    /**
     * Sets the configurable property to true if the supertype is also configurable and disables it.
     */
    @Override
    protected void supertypeChanged(TextButtonField supertypeField) throws CoreException {
        String qualifiedName = supertypeField.getValue();
        IPolicyCmptType superPcType = getIpsProject().findPolicyCmptType(qualifiedName);
        if (superPcType != null) {
            if (superPcType.isConfigurableByProductCmptType()) {
                configurableField.setValue(Boolean.TRUE);
                configurableField.getCheckbox().setEnabled(false);
            } else {
                configurableField.setValue(Boolean.FALSE);
                configurableField.getCheckbox().setEnabled(false);
            }
        } else {
            configurableField.getCheckbox().setEnabled(true);
        }
    }

    @Override
    protected void validateName() throws CoreException {
        super.validateName();
        if (getIpsProject() == null) {
            return;
        }
        setErrorMessage(TypeValidations.validateOtherTypeWithSameNameTypeInIpsObjectPath(
                IpsObjectType.PRODUCT_CMPT_TYPE, getQualifiedIpsObjectName(), getIpsProject(), null));
    }

    @Override
    protected void finishIpsObjectsExtension(IIpsObject newIpsObject, Set<IIpsObject> modifiedIpsObjects)
            throws CoreException {

        super.finishIpsObjectsExtension(newIpsObject, modifiedIpsObjects);

        IPolicyCmptType type = (IPolicyCmptType)newIpsObject;
        if (isPolicyCmptTypeConfigurable()) {
            type.setConfigurableByProductCmptType(true);
            type.setProductCmptType(getPageOfAssociatedType().getQualifiedIpsObjectName());
        }
    }

    @Override
    protected void validatePageExtensionThis(IpsValidation validation) throws CoreException {
        validation.addTask(new ValidateInstancesWillBeConfiguredByProductCmptType());
    }

    private class ValidateInstancesWillBeConfiguredByProductCmptType extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) throws CoreException {
            // Super-type may not be set after all
            if (StringUtils.isEmpty(getSuperType())) {
                return null;
            }

            /*
             * Info message: configured by product components, because instances of the superclass
             * are configured by product components
             */
            IPolicyCmptType superPcType = ipsProject.findPolicyCmptType(getSuperType());
            if (superPcType != null && superPcType.isConfigurableByProductCmptType()) {
                return new Message("", Messages.NewPcTypePage_infoConfigurateByProductCmptType, Message.INFO); //$NON-NLS-1$
            }

            return null;
        }

    }

}
