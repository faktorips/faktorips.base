/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.binding;

import java.beans.PropertyDescriptor;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.util.BeanUtil;

/**
 * Abstract base class for a binding between a property of a SWT control and a property of an
 * arbitrary object, usually a domain model object or presentation model object.
 * 
 */
public abstract class ControlPropertyBinding {

    private Control control;
    private Object object;

    private PropertyDescriptor property;

    /**
     * Checks the given propertyName and expectedType and creates a {@link ControlPropertyBinding}
     * if valid.
     * 
     * @param control the control to be bound to
     * @param object the model object containing the property that is bound to the given control
     * @param propertyName the name of the property that is bound to the given control
     * @param exptectedType the data type (class) of the model object's property. This information
     *            is used to check the validity of a binding. <code>null</code> can be given to
     *            bypass this type-check e.g. for primitive types.
     * @throws IllegalArgumentException if the bound property's type/class does not match the
     *             expectedType.
     */
    public ControlPropertyBinding(Control control, Object object, String propertyName, Class<?> exptectedType) {
        super();
        this.control = control;
        this.object = object;
        property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);
        if (exptectedType != null && !exptectedType.equals(property.getPropertyType())) {
            throw new IllegalArgumentException(
                    "Property " + propertyName + " of type " + object.getClass() + " is not of type " + exptectedType); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

    public String getPropertyName() {
        return getProperty().getName();
    }

    public final void updateUI(String propertyName) {
        if (!control.isDisposed()) {
            updateUiIfNotDisposed(propertyName);
        }
    }

    public final void updateUI() {
        updateUI(null);
    }

    /**
     * Updates the UI for this binding. This method is called only if this binding's control is not
     * disposed.
     * <p>
     * When implementing updates that depend on the given property, consider the case
     * <code>propertyName==null</code> explicitly. In most cases <code>null</code> (a change of the
     * model object as a whole) require an update of the UI. The code could then look like this:
     * <p>
     * <code>
     * if(nameOfChangedProperty==null || nameOfChangedProperty.equals(getPropertyName())){
     * //update UI
     * }
     * </code>
     * 
     * @param nameOfChangedProperty the name of the changed property or <code>null</code> if no
     *            specific property could be determined (i.e. the object as a whole has changed).
     */
    public abstract void updateUiIfNotDisposed(String nameOfChangedProperty);

    @Override
    public String toString() {
        return "Binding " + object.toString() + "#" + property.getName() + " to control " + control; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
