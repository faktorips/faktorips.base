package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 *
 */
public class ProductCmptTypeRefControl extends IpsObjectRefControl {

    public ProductCmptTypeRefControl(
            IIpsProject project, 
            Composite parent, 
            UIToolkit toolkit) {
        super(project, parent, toolkit, "Product Component Type Selection", "Select a component (?=any character, *=any string)");
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.IpsObjectRefControl#getPdObjects()
     */
    protected IIpsObject[] getPdObjects() throws CoreException {
        return getPdProject().findIpsObjects(IpsObjectType.PRODUCT_CMPT_TYPE);
    }

}
