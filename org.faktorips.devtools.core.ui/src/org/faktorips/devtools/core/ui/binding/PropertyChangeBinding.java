/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.binding;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Control;

/**
 * Implement this binding if you need to do anything on property change event. You can register this
 * binding using {@link BindingContext#add(ControlPropertyBinding)}. You only need to override the
 * method {@link #propertyChanged(Object, Object)}. The method is called when the binding context
 * recognize any changes done to the given object (for example a {@link PresentationModelObject}) at
 * the specified property. The method is also called if the binding context does not know which
 * property has changed (property name is <code>null</code>).
 *
 * @param <T> The expected type of the property's value.
 * 
 * @author dirmeier
 */
public abstract class PropertyChangeBinding<T> extends ControlPropertyBinding {
    /**
     * Comment for <code>instance</code>
     */
    private T oldValue;

    /**
     * Create a new {@link PropertyChangeBinding}
     * 
     * @param control A control that may change with the event. Used to check whether the control is
     *            disposed or already there.
     * @param object The object that is checked by the binding context
     * @param propertyName The property that has to change
     * @param exptectedType The expected type of the property
     */
    public PropertyChangeBinding(Control control, Object object, String propertyName, Class<T> exptectedType) {
        super(control, object, propertyName, exptectedType);
    }

    @Override
    public void updateUiIfNotDisposed(String propertyName) {
        if (StringUtils.isEmpty(propertyName) || getPropertyName().equals(propertyName)) {
            @SuppressWarnings("unchecked")
            T newValue = (T)readProperty();
            if (oldValue != newValue && (oldValue == null || !(oldValue.equals(newValue)))) {
                propertyChanged(oldValue, newValue);
                oldValue = newValue;
            }
        }
    }

    /**
     * Implement this method to do your action when property has changed. This method is called if
     * there was a change event concerning the specified property or a change event without property
     * name but only if the value of the specified property has changed.
     * 
     * @param oldValue The old value of the property before change
     * @param newValue The new value of the property that has been changed
     */
    protected abstract void propertyChanged(T oldValue, T newValue);

}