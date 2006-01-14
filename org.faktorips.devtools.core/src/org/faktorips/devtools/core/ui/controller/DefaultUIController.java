package org.faktorips.devtools.core.ui.controller;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;


/**
 *
 */
public class DefaultUIController implements ValueChangeListener, UIController {
    
    // list of mappings between edit fields and properties of model objects.
    protected List mappings = new ArrayList();

    /**
     * 
     */
    public DefaultUIController() {
        super();
    }
    
    public void add(EditField field, Object object, String propertyName) {
        PropertyDescriptor property = null;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
            for (int i=0; i<properties.length; i++) {
                if (properties[i].getName().equals(propertyName)) {
                    property = properties[i];
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException("Exception while introspection class " + object.getClass(), e);
        }
        if (property==null) {
            throw new IllegalArgumentException("Class " + object.getClass() + " does not have a property " + propertyName);
        }
        addMapping(new FieldPropertyMappingByPropertyDescriptor(field, object, property));
    }
    
    protected void addMapping(FieldPropertyMapping mapping) {
        mappings.add(mapping);
        mapping.getField().addChangeListener(this);
    }
    
    public void updateModel() {
        for (Iterator it=mappings.iterator(); it.hasNext();) {
            FieldPropertyMapping mapping = (FieldPropertyMapping)it.next();
            mapping.setPropertyValue();
        }
    }
    
    public void updateUI() {
        for (Iterator it=mappings.iterator(); it.hasNext();) {
            FieldPropertyMapping mapping = (FieldPropertyMapping)it.next();
            mapping.setControlValue();
        }
    }

    /** 
     * Overridden.
     */
    public void valueChanged(FieldValueChangedEvent e) {
        for (Iterator it=mappings.iterator(); it.hasNext();) {
            FieldPropertyMapping mapping = (FieldPropertyMapping)it.next();
            if (e.field== mapping.getField()) {
                mapping.setPropertyValue();
            }
        }
    }


}
