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
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.policycmpttype.PcTypePage;
import org.faktorips.devtools.core.ui.wizards.type.TypePage;


/**
 * An IpsObjectPage for the IpsObjectType ProductCmptType. 
 */
public class ProductCmptTypePage extends TypePage {
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public ProductCmptTypePage(IStructuredSelection selection, PcTypePage pcTypePage) throws JavaModelException {
        super(IpsObjectType.PRODUCT_CMPT_TYPE_V2, selection, Messages.ProductCmptTypePage_title);
        this.pageOfAssociatedType = pcTypePage;
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewProductCmptTypeWizard.png")); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit) {
        return toolkit.createProductCmptTypeRefControl(null, container, false);
    }
    
    /**
     * Sets default values to the fields of this page.
     */
    public void pageEntered() throws CoreException{
        if(pageOfAssociatedType == null){
            return;
        }
        if(StringUtils.isEmpty(getPackage())){
            setPackage(pageOfAssociatedType.getPackage());
        }
        if(StringUtils.isEmpty(getSourceFolder())){
            setSourceFolder(pageOfAssociatedType.getSourceFolder());
        }
        if(StringUtils.isEmpty(getSuperType())){
            if(!StringUtils.isEmpty(pageOfAssociatedType.getSuperType())){
                IPolicyCmptType superPcType = getIpsProject().findPolicyCmptType(pageOfAssociatedType.getSuperType());
                setSuperType(superPcType.getProductCmptType());
            }
        }
        if(StringUtils.isEmpty(getIpsObjectName())){
            String postfix = IpsPlugin.getDefault().getIpsPreferences().getDefaultProductCmptTypePostfix();
            if(!StringUtils.isEmpty(postfix)){
                setIpsObjectName(pageOfAssociatedType.getIpsObjectName() + postfix);
            }
        }
        super.pageEntered();
    }
    
    /**
     * Returns true if the policy component type is configurable
     */
    public boolean canCreateIpsSrcFile(){
        if(pageOfAssociatedType == null){
            return true;
        }
        return ((PcTypePage)pageOfAssociatedType).isPolicyCmptTypeConfigurable();
    }
    
    
    /**
     * Calles the super class method and additionally validates if the supertype of the product
     * component type is within the super type hierarchy of the product component type of the
     * supertype of the policy component type.
     */
    protected void validatePageExtension() throws CoreException {
        super.validatePageExtension();
        if (pageOfAssociatedType != null && !StringUtils.isEmpty(getSuperType())) {
            final IProductCmptType superType = getIpsProject().findProductCmptType(getSuperType());
            if (superType != null && !StringUtils.isEmpty(pageOfAssociatedType.getSuperType())) {
                IPolicyCmptType superPcType = getIpsProject().findPolicyCmptType(pageOfAssociatedType.getSuperType());
                final IProductCmptType policyCmptProductCmptSuperType = superPcType.findProductCmptType(getIpsProject());
                final Boolean[] holder = new Boolean[]{Boolean.FALSE};
                if(policyCmptProductCmptSuperType != null){
                    new TypeHierarchyVisitor(getIpsProject()){
                        protected boolean visit(IType currentType) throws CoreException {
                            if(currentType.equals(policyCmptProductCmptSuperType)){
                                holder[0] = Boolean.TRUE;
                                return false;
                            }
                            return true;
                        }
                        
                    }.start(superType);
                    if(Boolean.FALSE.equals(holder[0])){
                        setErrorMessage("The super type must be the same or a subtype of " + superPcType.getProductCmptType());
                    }
                }
            }
        }
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
