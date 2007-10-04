package org.faktorips.devtools.core.ui.binding;

import org.eclipse.swt.widgets.Control;

/**
 * Binding between the enable property of a SWT control and a boolean
 * property of an abitrary object, usually a domain model object or presentation model
 * object.
 * 
 * @author Jan Ortmann
 */
public class EnableBinding extends ControlPropertyBinding {

    private boolean enabledIfObjectPropertyIsTrue;
    
    public EnableBinding(Control control, Object object, String propertyName, boolean enabledIfTrue) {
        super(control, object, propertyName, Boolean.TYPE);
        this.enabledIfObjectPropertyIsTrue = enabledIfTrue;
    }
    
    public void updateUI() {
        try {
            Boolean value = (Boolean)getProperty().getReadMethod().invoke(getObject(), new Object[0]);
            boolean enabled = value.booleanValue() == enabledIfObjectPropertyIsTrue;
            getControl().setEnabled(enabled);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
