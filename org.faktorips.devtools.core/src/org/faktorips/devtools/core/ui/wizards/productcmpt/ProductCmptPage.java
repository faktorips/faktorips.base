package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.ProductCmptRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;


/**
 *
 */
public class ProductCmptPage extends IpsObjectPage {
    
    private ProductCmptRefControl productCmptRefControl;
    
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
        
        productCmptRefControl = new ProductCmptRefControl(null, nameComposite, toolkit);
        TextButtonField pcTypeField = new TextButtonField(productCmptRefControl);
        pcTypeField.addChangeListener(this);
    }
    
    String getPolicyCmptType() {
        return productCmptRefControl.getText();
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
