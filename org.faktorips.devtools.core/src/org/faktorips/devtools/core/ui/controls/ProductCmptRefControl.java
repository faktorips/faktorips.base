package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 *
 */
public class ProductCmptRefControl extends IpsObjectRefControl {

    private String qualifiedTypeName;
    private boolean includeCmptsForSubtypes = true;
    
    
    public ProductCmptRefControl(
            IIpsProject project, 
            Composite parent, 
            UIToolkit toolkit) {
        super(project, parent, toolkit, Messages.ProductCmptRefControl_title, Messages.ProductCmptRefControl_description);
    }
    
    /**
     * @param qPcTypeNmae The policy component type for which product components should be selectable.
     * @param includeCmptsForSubtypes <code>true</code> if also product components for subtypes should be selectable.
     */
    public void setPolicyCmptType(String qTypeName, boolean includeCmptsForSubtypes) {
        this.qualifiedTypeName = qTypeName;
        this.includeCmptsForSubtypes = includeCmptsForSubtypes;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.IpsObjectRefControl#getPdObjects()
     */
    protected IIpsObject[] getPdObjects() throws CoreException {
        return getPdProject().findProductCmpts(qualifiedTypeName, includeCmptsForSubtypes);
    }

}
