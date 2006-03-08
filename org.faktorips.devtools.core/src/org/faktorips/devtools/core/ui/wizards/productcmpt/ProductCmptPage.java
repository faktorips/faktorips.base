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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.ProductCmptTypeRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;


/**
 *
 */
public class ProductCmptPage extends IpsObjectPage {
    
    private ProductCmptTypeRefControl productCmptRefControl;
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public ProductCmptPage(IStructuredSelection selection) throws JavaModelException {
        super(selection, Messages.ProductCmptPage_title);
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.IpsObjectPage#fillNameComposite(org.eclipse.swt.widgets.Composite, UIToolkit)
     */
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        addNameLabelField(toolkit);
        toolkit.createFormLabel(nameComposite, Messages.ProductCmptPage_labelName);
        
        productCmptRefControl = new ProductCmptTypeRefControl(null, nameComposite, toolkit);
        TextButtonField pcTypeField = new TextButtonField(productCmptRefControl);
        pcTypeField.addChangeListener(this);
    }
    
    String getPolicyCmptType() {
    	String policyCmptTypeName = "";
    	try {
        	String productCmptTypeName = productCmptRefControl.getText();
			IProductCmptType type = productCmptRefControl.getPdProject().findProductCmptType(productCmptTypeName);
			policyCmptTypeName = type.getPolicyCmptyType();
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
    	return policyCmptTypeName;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.IpsObjectPage#sourceFolderChanged()
     */
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getPdPackageFragmentRoot();
        if (root!=null) {
        	productCmptRefControl.setPdProject(root.getIpsProject());
        } else {
        	productCmptRefControl.setPdProject(null);
        }
    }    
}
