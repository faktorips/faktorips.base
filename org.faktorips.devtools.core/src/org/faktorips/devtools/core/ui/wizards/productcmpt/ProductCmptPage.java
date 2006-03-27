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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.ProductCmptTypeRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public class ProductCmptPage extends IpsObjectPage {
    
    private ProductCmptTypeRefControl typeRefControl;
    
    public ProductCmptPage(IStructuredSelection selection) throws JavaModelException {
        super(selection, Messages.ProductCmptPage_title);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        addNameLabelField(toolkit);
        toolkit.createFormLabel(nameComposite, Messages.ProductCmptPage_labelName);
        
        typeRefControl = new ProductCmptTypeRefControl(null, nameComposite, toolkit);
        TextButtonField pcTypeField = new TextButtonField(typeRefControl);
        pcTypeField.addChangeListener(this);
    }
    
    /**
	 * {@inheritDoc}
	 */
	protected void setDefaults(IResource selectedResource) {
		super.setDefaults(selectedResource);
		try {
			IIpsObject obj = getSelectedIpsObject();
			if (!(obj instanceof IProductCmpt)) {
				return;
			}
			IProductCmpt productCmpt = (IProductCmpt)obj;
			String defaultName;
			defaultName = productCmpt.getIpsProject().getProductCmptNamingStratgey().getNextName(productCmpt);
			setIpsObjectName(defaultName);
			typeRefControl.setText(productCmpt.getPolicyCmptType());
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
	}

	String getPolicyCmptType() {
    	String policyCmptTypeName = ""; //$NON-NLS-1$
    	try {
        	String productCmptTypeName = typeRefControl.getText();
			IProductCmptType type = typeRefControl.getPdProject().findProductCmptType(productCmptTypeName);
			policyCmptTypeName = type.getPolicyCmptyType();
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
    	return policyCmptTypeName;
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getPdPackageFragmentRoot();
        if (root!=null) {
        	typeRefControl.setPdProject(root.getIpsProject());
        } else {
        	typeRefControl.setPdProject(null);
        }
    }
    
    protected void validatePage() throws CoreException {
        super.validatePage();
        if (getErrorMessage()!=null) {
            return;
        }
	    if (typeRefControl.findProductCmptType()==null) {
	        setErrorMessage("Product template " + typeRefControl.getText() + " does not exists!");
	    }
        updatePageComplete();
    }
    

	/**
	 * {@inheritDoc}
	 */
	protected void validateName() {
		super.validateName();
		if (getErrorMessage()!=null) {
			return;
		}
		try {
			MessageList list = getIpsProject().getProductCmptNamingStratgey().validate(getIpsObjectName());
			if (!list.isEmpty()) {
				setErrorMessage(list.getMessage(0).getText());
			}
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
	}    
    
}
