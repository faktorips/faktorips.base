package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;

public abstract class TimedIpsSection extends IpsSection {

    public TimedIpsSection(
            Composite parent, 
            int style, 
            int layoutData, 
            UIToolkit toolkit) {
        super(parent, style, layoutData, toolkit);
    }

    /**
     * Sets the given generation as active one.
     */
    public abstract void setActiveGeneration(IProductCmptGeneration generation);
    
}
