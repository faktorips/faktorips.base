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
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.productcmpttype.ProductCmptTypePage;
import org.faktorips.devtools.core.ui.wizards.type.TypePage;


/**
 *
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
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewPolicyCmptTypeWizard.png"));
    }

    protected IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit) {
        return toolkit.createPcTypeRefControl(null, container);
    }
    
    public void setProductCmptTypePage(ProductCmptTypePage page){
        this.pageOfAssociatedType = page;
    }
    
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);
        
        toolkit.createLabel(nameComposite, "");
        configurableField = new CheckboxField(toolkit.createCheckbox(nameComposite, "Configured by product component type")); 
        configurableField.setValue(Boolean.FALSE);
        configurableField.addChangeListener(this);
    }
    
    public boolean canFlipToNextPage() {
        return isPageComplete() && isPolicyCmptTypeConfigurable();
    }
    
    public boolean isPolicyCmptTypeConfigurable(){
        return Boolean.TRUE.equals(configurableField.getValue());
    }
    
    public boolean finishWhenThisPageIsComplete() {
        return isPageComplete() && !isPolicyCmptTypeConfigurable();
    }

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
