/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
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
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewPolicyCmptTypeWizard.png")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit) {
        return toolkit.createPcTypeRefControl(null, container);
    }

    /**
     * Associates the product component type page
     */
    public void setProductCmptTypePage(ProductCmptTypePage page){
        this.pageOfAssociatedType = page;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);
        
        toolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        configurableField = new CheckboxField(toolkit.createCheckbox(nameComposite, Messages.PcTypePage_configuredByProductCmptType)); 
        configurableField.setValue(Boolean.FALSE);
        configurableField.addChangeListener(this);
    }

    /**
     * Returns true if the page is complete and the policy component type is configurable.
     */
    public boolean canFlipToNextPage() {
        return isPageComplete() && isPolicyCmptTypeConfigurable();
    }
    
    /**
     * Returns the value of the configurable field.
     */
    public boolean isPolicyCmptTypeConfigurable(){
        return Boolean.TRUE.equals(configurableField.getValue());
    }

    /**
     * Returns true if the page is complete and the policy component type is not configurable.
     */
    public boolean finishWhenThisPageIsComplete() {
        return isPageComplete() && !isPolicyCmptTypeConfigurable();
    }

    /**
     * Sets the configurable property to true if the supertype is also configurable and disables it.
     */
    protected void supertypeChanged(TextButtonField supertypeField) throws CoreException{
        String qualifiedName = (String)supertypeField.getValue();
        IPolicyCmptType superPcType = getIpsProject().findPolicyCmptType(qualifiedName);
        if(superPcType != null){
            if(superPcType.isConfigurableByProductCmptType()){
                configurableField.setValue(Boolean.TRUE);
                configurableField.getCheckbox().setEnabled(false);
            }
        } else {
            configurableField.getCheckbox().setEnabled(true);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void validatePageExtension() throws CoreException {
        if(isPolicyCmptTypeConfigurable()){
            super.validatePageExtension();
        }
    }

    /** 
     * {@inheritDoc}
     */
    protected void finishIpsObject(IIpsObject ipsObject) throws CoreException {
        super.finishIpsObject(ipsObject);
        if(isPolicyCmptTypeConfigurable()){
            IPolicyCmptType type = (IPolicyCmptType)ipsObject;
            type.setConfigurableByProductCmptType(true);
            type.setProductCmptType(pageOfAssociatedType.getQualifiedIpsObjectName());
        }
    }
}
