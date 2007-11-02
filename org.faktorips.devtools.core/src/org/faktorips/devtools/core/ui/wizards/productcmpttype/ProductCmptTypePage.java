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

package org.faktorips.devtools.core.ui.wizards.productcmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.policycmpttype.PcTypePage;
import org.faktorips.devtools.core.ui.wizards.type.TypePage;


/**
 *
 */
public class ProductCmptTypePage extends TypePage {
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public ProductCmptTypePage(IStructuredSelection selection, PcTypePage pcTypePage) throws JavaModelException {
        super(IpsObjectType.PRODUCT_CMPT_TYPE_V2, selection, "New Product Component Type");
        this.pageOfAssociatedType = pcTypePage;
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewProductCmptTypeWizard.png"));
    }
    

    protected IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit) {
        return toolkit.createProductCmptTypeRefControl(null, container, false);
    }
    
    public void pageEntered(){
        if(pageOfAssociatedType == null){
            return;
        }
        if(StringUtils.isEmpty(getPackage())){
            setPackage(pageOfAssociatedType.getPackage());
        }
        if(StringUtils.isEmpty(getSourceFolder())){
            setSourceFolder(pageOfAssociatedType.getSourceFolder());
        }
        if(StringUtils.isEmpty(getIpsObjectName())){
            String postfix = IpsPlugin.getDefault().getIpsPreferences().getDefaultProductCmptTypePostfix();
            setIpsObjectName(pageOfAssociatedType.getIpsObjectName() + postfix);
        }
    }
    
    public boolean canCreateIpsSrcFile(){
        if(pageOfAssociatedType == null){
            return true;
        }
        return ((PcTypePage)pageOfAssociatedType).isPolicyCmptTypeConfigurable();
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void finishIpsObject(IIpsObject ipsObject) throws CoreException {
        super.finishIpsObject(ipsObject);
        IProductCmptType productCmptType = (IProductCmptType)ipsObject;
        if(pageOfAssociatedType != null){
            if(((PcTypePage)pageOfAssociatedType).isPolicyCmptTypeConfigurable()){
                productCmptType.setConfigurationForPolicyCmptType(true);
                productCmptType.setPolicyCmptType(pageOfAssociatedType.getQualifiedIpsObjectName());
                return;
            }
        }
        productCmptType.setConfigurationForPolicyCmptType(false);
    }
    
}
