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

package org.faktorips.devtools.core.ui.wizards.policycmpttype;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.TypeValidations;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.productcmpttype.ProductCmptTypePage;
import org.faktorips.devtools.core.ui.wizards.type.TypePage;

/**
 * An IpsObjectPage for the IpsObjectType PolicyCmptType.
 */
public class PcTypePage extends TypePage {

    private CheckboxField configurableField;

    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public PcTypePage(IStructuredSelection selection) throws JavaModelException {
        super(IpsObjectType.POLICY_CMPT_TYPE, selection, Messages.PcTypePage_title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/NewPolicyCmptTypeWizard.png")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit) {
        return toolkit.createPcTypeRefControl(null, container);
    }

    /**
     * Associates the product component type page
     */
    public void setProductCmptTypePage(ProductCmptTypePage page) {
        pageOfAssociatedType = page;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);

        toolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        configurableField = new CheckboxField(toolkit.createCheckbox(nameComposite,
                Messages.PcTypePage_configuredByProductCmptType));
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
        return isPolicyCmptTypeConfigurable();
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
            settings.put(IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE, ((Boolean)configurableField
                    .getValue()).booleanValue());
        }
    }

    /**
     * Sets the configurable property to true if the supertype is also configurable and disables it.
     */
    @Override
    protected void supertypeChanged(TextButtonField supertypeField) throws CoreException {
        String qualifiedName = (String)supertypeField.getValue();
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validatePageExtension() throws CoreException {
        super.validatePageExtension();
        if (isPolicyCmptTypeConfigurable()) {
            if (getErrorMessage() == null && pageOfAssociatedType != null
                    && pageOfAssociatedType.isAlreadyBeenEntered()) {
                pageOfAssociatedType.validatePage();
                if (!StringUtils.isEmpty(pageOfAssociatedType.getErrorMessage())) {
                    setErrorMessage(pageOfAssociatedType.getErrorMessage());
                    return;
                }
            }
        }
        // show info message:
        // configured by product components,
        // because instances of the superclass are configured by product components
        IPolicyCmptType superPcType = getIpsProject().findPolicyCmptType(getSuperType());
        if (superPcType != null) {
            if (superPcType.isConfigurableByProductCmptType()) {
                setMessage(Messages.PcTypePage_infoConfigurateByProductCmptType, INFORMATION);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finishIpsObjects(IIpsObject newIpsObject, List<IIpsObject> modifiedIpsObjects) throws CoreException {
        super.finishIpsObjects(newIpsObject, modifiedIpsObjects);
        IPolicyCmptType type = (IPolicyCmptType)newIpsObject;
        if (isPolicyCmptTypeConfigurable()) {
            type.setConfigurableByProductCmptType(true);
            type.setProductCmptType(pageOfAssociatedType.getQualifiedIpsObjectName());
        }
        type.initPersistentTypeInfo();
    }
}
