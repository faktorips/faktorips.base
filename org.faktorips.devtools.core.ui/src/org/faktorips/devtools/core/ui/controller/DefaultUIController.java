/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.model.plugin.IpsStatus;

/**
 * @deprecated deprecated since 3.6, use {@link BindingContext} instead
 */
@Deprecated
public class DefaultUIController implements ValueChangeListener, UIController, FocusListener {

    /** list of mappings between edit fields and properties of model objects. */
    private List<FieldPropertyMapping<?>> mappings = new ArrayList<>();

    protected List<FieldPropertyMapping<?>> getMappings() {
        return mappings;
    }

    /**
     * Adds an edit-field to this controller. The property with the given name has to be get- and
     * setable at the given object.
     * 
     * @param field The field to link.
     * @param object The object to get and set the property
     * @param propertyName The name of the property
     */
    public <T> void add(EditField<T> field, Object object, String propertyName) {
        PropertyDescriptor property = null;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertie : properties) {
                if (propertie.getName().equals(propertyName)) {
                    property = propertie;
                    break;
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException("Exception while introspection class " //$NON-NLS-1$
                    + object.getClass(), e);
        }
        if (property == null) {
            throw new IllegalArgumentException("Class " + object.getClass() //$NON-NLS-1$
                    + " does not have a property " + propertyName); //$NON-NLS-1$
        }

        FieldPropertyMappingByPropertyDescriptor<T> mapping = new FieldPropertyMappingByPropertyDescriptor<>(field,
                object, property);
        addMapping(mapping);

    }

    /**
     * Creates a TextField to wrap the given Text-Object. If this Text-Object displays a property
     * which is not of type String the method
     * <code>add(EditField field, Object object, String propertyName)</code> with the appropriate
     * EditField has to be used.
     * 
     * @param text The text to link with.
     * @param object The Object to link with.
     * @param propertyName The name of the property to link with.
     */
    public TextField add(Text text, Object object, String propertyName) {
        TextField field = new TextField(text);
        add(field, object, propertyName);
        return field;
    }

    protected void addMapping(FieldPropertyMapping<?> mapping) {
        mappings.add(mapping);
        mapping.getField().addChangeListener(this);
        mapping.getField().getControl().addFocusListener(this);
    }

    @Override
    public void updateModel() {
        // defensive copy to avoid concurrent modification exceptions
        List<FieldPropertyMapping<?>> copy = new CopyOnWriteArrayList<>(mappings);
        for (FieldPropertyMapping<?> mapping : copy) {
            try {
                mapping.setPropertyValue();
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error updating model property " + mapping.getPropertyName() //$NON-NLS-1$
                        + " of object " + mapping.getObject(), e)); //$NON-NLS-1$
                // CSON: IllegalCatch
            }
        }
    }

    @Override
    public void updateUI() {
        List<FieldPropertyMapping<?>> copy = new CopyOnWriteArrayList<>(mappings);
        // defensive copy to avoid concurrent modification exceptions
        for (FieldPropertyMapping<?> mapping : copy) {
            try {
                mapping.setControlValue();
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error updating control for property " + mapping.getPropertyName() //$NON-NLS-1$
                        + " of object " + mapping.getObject(), e)); //$NON-NLS-1$
                // CSON: IllegalCatch
            }
        }
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        List<FieldPropertyMapping<?>> copy = new CopyOnWriteArrayList<>(mappings);
        // defensive copy to avoid concurrent modification exceptions
        for (FieldPropertyMapping<?> mapping : copy) {
            if (e.field == mapping.getField()) {
                try {
                    mapping.setPropertyValue();
                } catch (Exception ex) {
                    IpsPlugin.log(new IpsStatus("Error updating model property " + mapping.getPropertyName() //$NON-NLS-1$
                            + " of object " + mapping.getObject(), ex)); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Removes the given field. After this method has returned, the field is no longer controlled by
     * this controller, all listeners set by this controller are removed.
     * 
     * @param field The field to remove.
     */
    public void remove(EditField<?> field) {
        ArrayList<FieldPropertyMapping<?>> secureCopy = new ArrayList<>(mappings);

        for (FieldPropertyMapping<?> mapping : secureCopy) {
            if (mapping.getField().equals(field)) {
                mappings.remove(mapping);
                field.removeChangeListener(this);
            }
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        // nothing to do
    }

    @Override
    public void focusLost(FocusEvent e) {
        // broadcast outstanding change events
        IpsUIPlugin.getDefault().getEditFieldChangeBroadcaster().broadcastLastEvent();
    }

}
