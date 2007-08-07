 package org.faktorips.devtools.core.ui.binding;

import java.beans.PropertyDescriptor;

import org.eclipse.swt.widgets.Control;

/**
 * Abstract base class for a binding between a property of a SWT control and a
 * property of an abitrary object, usually a domain model object or presentation model
 * object.
 * 
 * @author     $Author: ortmann $
 * @version    $Revision: 1.1 $
 */
public abstract class ControlPropertyBinding {

    private Control control;
    private Object object;
    
    private PropertyDescriptor property;
    
    public ControlPropertyBinding(Control control, Object object, String propertyName, Class exptectedType) {
        super();
        this.control = control;
        this.object = object;
        property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);
        if (exptectedType!=null && !exptectedType.equals(property.getPropertyType())) {
            throw new IllegalArgumentException("Das Property " + propertyName + " der Klasse " + object.getClass() + " ist nicht vom Type " + exptectedType);
        }
    }
    
    public Control getControl() {
        return control;
    }
    
    public Object getObject() {
        return object;
    }

    public PropertyDescriptor getProperty() {
        return property;
    }
    
    public abstract void updateUI();
    
}
