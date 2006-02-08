package org.faktorips.devtools.core.ui.controller;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
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
			for (int i = 0; i < properties.length; i++) {
				if (properties[i].getName().equals(propertyName)) {
					property = properties[i];
					break;
				}
			}
		} catch (IntrospectionException e) {
			throw new RuntimeException("Exception while introspection class "
					+ object.getClass(), e);
		}
		if (property == null) {
			throw new IllegalArgumentException("Class " + object.getClass()
					+ " does not have a property " + propertyName);
		}
		addMapping(new FieldPropertyMappingByPropertyDescriptor(field, object,
				property));
	}

	/**
	 * Creates a TextField to wrap the given Text-Object. If this Text-Object 
	 * displays a property which is not of type String the method 
	 * <code>add(EditField field, Object object, String propertyName)</code>
	 * with the appropriate EditField has to be used. 
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

	protected void addMapping(FieldPropertyMapping mapping) {
		mappings.add(mapping);
		mapping.getField().addChangeListener(this);
	}

	public void updateModel() {
		for (Iterator it = mappings.iterator(); it.hasNext();) {
			FieldPropertyMapping mapping = (FieldPropertyMapping) it.next();
			mapping.setPropertyValue();
		}
	}

	public void updateUI() {
		for (Iterator it = mappings.iterator(); it.hasNext();) {
			FieldPropertyMapping mapping = (FieldPropertyMapping) it.next();
			mapping.setControlValue();
		}
	}

	/**
	 * Overridden.
	 */
	public void valueChanged(FieldValueChangedEvent e) {
		for (Iterator it = mappings.iterator(); it.hasNext();) {
			FieldPropertyMapping mapping = (FieldPropertyMapping) it.next();
			if (e.field == mapping.getField()) {
				mapping.setPropertyValue();
			}
		}
	}

	/**
	 * Removes the given field. After this method has returned, the field is no
	 * longer controlled by this controller, all listeners set by this
	 * controller are removed.
	 * 
	 * @param field
	 *            The field to remove.
	 */
	public void remove(EditField field) {
		ArrayList secureCopy = new ArrayList(mappings);

		for (Iterator it = secureCopy.iterator(); it.hasNext();) {
			FieldPropertyMapping mapping = (FieldPropertyMapping) it.next();
			if (mapping.getField().equals(field)) {
				mappings.remove(mapping);
				field.removeChangeListener(this);
			}
		}
	}

}
