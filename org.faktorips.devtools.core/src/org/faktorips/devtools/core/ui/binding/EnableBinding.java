package org.faktorips.devtools.core.ui.binding;

import org.eclipse.swt.widgets.Control;

/**
 * Binding between the enable property of a SWT control and a boolean
 * property of an abitrary object, usually a domain model object or presentation model
 * object.
 *  
 * 
 * @author     $Author: ortmann $
 * @version    $Revision: 1.1 $
 */
public class EnableBinding extends ControlPropertyBinding {

    public EnableBinding(Control control, Object object, String propertyName) {
        super(control, object, propertyName, Boolean.TYPE);
    }
    
    public void updateUI() {
        try {
            Boolean value = (Boolean)getProperty().getReadMethod().invoke(getObject(), new Object[0]);
            getControl().setEnabled(value.booleanValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
