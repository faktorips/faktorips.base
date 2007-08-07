/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.binding;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IExtensionPropertyAccess;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.FieldExtensionPropertyMapping;
import org.faktorips.devtools.core.ui.controller.FieldPropertyMapping;
import org.faktorips.devtools.core.ui.controller.FieldPropertyMappingByPropertyDescriptor;
import org.faktorips.devtools.core.ui.controller.Messages;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.IntegerField;
import org.faktorips.devtools.core.ui.controller.fields.LabelField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.AbstractCheckbox;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;


/**
 * Binding between the user interface and a (domain or presentation) model.
 * <p>
 * Currently the context provides the following types of binding methods:
 * <ul>
 * <li>bindContent: binds the content shown in a control to a model object property.</li>
 * <li>bindEnable: binds a control's enabled property to a model object property of type boolean.</li>
 * <li>bindVisible: binds a control's visible property to a model object property of type boolean.</li>
 * </ul>
 * <p>
 * Currently the bindContent methods work for IpsObjectPartContainers only.
 * The bindVisible and bindEnabled methods for work IpsObjectPartContainers and objects implementing
 * {@link IPropertyChangeListenerSupport}, e.g. presentation model objects. 
 * 
 * @author Jan Ortmann
 */
public class BindingContext {

    // listener for changes and focus losts. Instance of an inner class is used to avoid poluting this class' interface.
    private Listener listener = new Listener();
    
    // list of mappings between edit fields and properties of model objects.
    private List mappings = new ArrayList();
    
    // a list of the ips objects containing at least one binded ips part container
    // each container is contained in the list only once, so it is actually used as a set, not
    // we still use the list, because once binded, we need to access all binded containers, and
    // this is faster with a list, than a hashset or treeset.
    private List ipsObjects = new ArrayList(1);
    
    private List controlBindings = new ArrayList(2);
    
    /**
     * Updates the UI with information from the model.
     */
    public void updateUI() {
        List copy = new ArrayList(mappings); // defensive copy to avoid concurrent modification exceptions
        for (Iterator it = copy.iterator(); it.hasNext();) {
            FieldPropertyMapping mapping = (FieldPropertyMapping) it.next();
            try {
                mapping.setControlValue();
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error updating control for property " + mapping.getPropertyName() //$NON-NLS-1$
                        + " of object " + mapping.getObject(), e)); //$NON-NLS-1$
            }
        }
        showValidationStatus();
        applyControlBindings();
    }

    /**
     * Binds the given text control to the given ips object's property.
     * 
     * @return the edit field created to access the value in the text control.
     * 
     * @throws IllegalArgumentException if the property is not of type String.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public EditField bindContent(Text text, IIpsObjectPartContainer object, String propertyName) {
        EditField field = null;
        PropertyDescriptor property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);
        if (String.class==property.getPropertyType()) {
            field = new TextField(text);
        } else if (Integer.class==property.getPropertyType() || Integer.TYPE==property.getPropertyType()) {
            field = new IntegerField(text);
        }
        if (field==null) {
            throwWrongPropertyTypeException(property, new Class[]{String.class, Integer.class});
        }
        bindContent(field, object, propertyName);
        return field;
    }
    
    /**
     * Binds the given label to the given ips object's property.
     * 
     * @return the edit field created to access the value in the label.
     * 
     * @throws IllegalArgumentException if the property is not of type String.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public EditField bindContent(Label label, IIpsObjectPartContainer object, String property) {
        checkPropertyType(object, property, String.class);
        EditField field = new LabelField(label);
        bindContent(field, object, property);
        return field;
    }
    
    /**
     * Binds the given checkbox to the given ips object's property.
     * 
     * @return the edit field created to access the value in the text control.
     * 
     * @throws IllegalArgumentException if the property is not of type Boolean or boolean.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public EditField bindContent(AbstractCheckbox checkbox, IIpsObjectPartContainer object, String propertyName) {
        PropertyDescriptor property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);
        if (Boolean.class!=property.getPropertyType() && Boolean.TYPE!=property.getPropertyType()) {
            throwWrongPropertyTypeException(property, new Class[]{Boolean.class, Boolean.TYPE});
        }
        EditField field = new CheckboxField(checkbox);
        bindContent(field, object, propertyName);
        return field;
    }
    
    /**
     * Binds the given text-button control to the given ips object's property.
     * 
     * @return the edit field created to access the value in the text control.
     * 
     * @throws IllegalArgumentException if the property is not of type String.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public EditField bindContent(TextButtonControl control, IIpsObjectPartContainer object, String property) {
        checkPropertyType(object, property, String.class);
        EditField field = new TextButtonField(control);
        bindContent(field, object, property);
        return field;
    }
    
    /**
     * Binds the given combo to the given ips object's property.
     * 
     * @return the edit field created to access the value in the text control.
     * 
     * @throws IllegalArgumentException if the property's type is not a subclass of DefaultEnumValue.
     * @throws NullPointerException if any argument is <code>null</code>.
     * 
     * @see DefaultEnumValue
     */
    public EnumValueField bindContent(Combo combo, IIpsObjectPartContainer object, String property, EnumType enumType) {
        checkPropertyType(object, property, DefaultEnumValue.class);
        EnumValueField field = new EnumValueField(combo, enumType);
        bindContent(field, object, property);
        return field;
    }
    
    /**
     * Binds the given edit field to the given ips object's property.
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public void bindContent(EditField field, IIpsObjectPartContainer object, String property) {
        add(createMapping(field, object, property));
    }

    protected FieldPropertyMapping createMapping(EditField editField, Object object, String propertyName) {
        if (object instanceof IExtensionPropertyAccess) {
            IExtensionPropertyDefinition extProperty = IpsPlugin.getDefault().getIpsModel().getExtensionPropertyDefinition(object.getClass(), propertyName, true);
            if (extProperty!=null) {
                return new FieldExtensionPropertyMapping(editField, (IExtensionPropertyAccess)object, propertyName);
            }
        }
        PropertyDescriptor property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);
        return new FieldPropertyMappingByPropertyDescriptor(editField, object, property); 
    }
    
    private void checkPropertyType(Object object, String propertyName, Class expectedType) {
        PropertyDescriptor property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);
        if (!expectedType.isAssignableFrom(property.getPropertyType())) {
            throw new IllegalArgumentException("Expected property " + property.getName() + " to be of type " + expectedType 
                    + ", but is of type " + property.getPropertyType());
        }
    }
    
    private void throwWrongPropertyTypeException(PropertyDescriptor property, Class[] expectedTypes) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < expectedTypes.length; i++) {
            if (i>0) {
                buffer.append(", ");
            }
            buffer.append(expectedTypes[i]);
        }
        throw new IllegalArgumentException("Property " + property.getName() + " is of type " + property.getPropertyType() 
                + ", but is expected to of one of the types " + buffer.toString());
    }
    
    /**
     * Binds the control's enabled property to the given part container's property.
     * 
     * @throws IllegalArgumentException if the object's property is not of type boolean.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public void bindEnabled(Control control, IIpsObjectPartContainer object, String property) {
        add(new EnableBinding(control, object, property));
    }
    
    /**
     * Binds the control's enabled property to the given model object's property.
     * 
     * @throws IllegalArgumentException if the object's property is not of type boolean.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public void bindEnabled(Control control, IPropertyChangeListenerSupport modelObject, String property) {
        add(new EnableBinding(control, modelObject, property));
    }
    
    /**
     * Binds the control's visible property to the given part container's property.
     * 
     * @throws IllegalArgumentException if the object's property is not of type boolean.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public void bindVisible(Control control, IIpsObjectPartContainer object, String property) {
        add(new VisibleBinding(control, object, property));
    }
    
    /**
     * Binds the control's visible property to the given model object's property.
     * 
     * @throws IllegalArgumentException if the object's property is not of type boolean.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public void bindVisible(Composite control, IPropertyChangeListenerSupport object, String property) {
        add(new VisibleBinding(control, object, property));
    }

    private void add(FieldPropertyMapping mapping) {
        registerChangeListener();
        mapping.getField().addChangeListener(listener);
        mapping.getField().getControl().addFocusListener(listener);
        mappings.add(mapping);
        if (mapping.getObject() instanceof IIpsObjectPartContainer) {
            IIpsObjectPartContainer container = (IIpsObjectPartContainer)mapping.getObject();
            IIpsObject ipsObject = container.getIpsObject();
            if (!ipsObjects.contains(ipsObject)) {
                ipsObjects.add(ipsObject);
            }
        }
    }
    
    private void add(ControlPropertyBinding binding) {
        registerChangeListener();
        controlBindings.add(binding);
    }

    private void registerChangeListener() {
        if (mappings.size()==0 && controlBindings.size()==0) {
            IpsPlugin.getDefault().getIpsModel().addChangeListener(listener);
        }
    }
    
    /**
     * Removes the registered listener.
     */
    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(listener);
        List copy = new ArrayList(mappings); // defensive copy to avoid concurrent modification exceptions
        for (Iterator it = copy.iterator(); it.hasNext();) {
            FieldPropertyMapping mapping = (FieldPropertyMapping) it.next();
            if (mapping.getField().removeChangeListener(listener));
            if (!mapping.getField().getControl().isDisposed()) {
                mapping.getField().getControl().removeFocusListener(listener);
            }
        }
    }

    /**
     * Validates all binded the part containers and updates the fields that are associated with properties of the IpsPartContainer.
     * It returns the MessageList which is the result of the validation. This return value 
     * can be evaluated when overriding this method.
     * 
     * @return the validation message list. Never returns <code>null</code>.
     */
    protected void showValidationStatus() {
        for (Iterator it = ipsObjects.iterator(); it.hasNext(); ) {
            showValidationStatus((IIpsObject)it.next());
        }
    }
    
    /**
     * Validates the part container and updates the fields that are associated with attributes of the IpsPartContainer.
     * It returns the MessageList which is the result of the validation. This return value 
     * can be evaluated when overriding this method.
     * 
     * @return the validation message list. Never returns <code>null</code>.
     */
    protected MessageList showValidationStatus(IIpsObject ipsObject) {
        try {
            MessageList list = ipsObject.validate();
            for (Iterator it=mappings.iterator(); it.hasNext();) {
                FieldPropertyMapping mapping = (FieldPropertyMapping)it.next();
                Control c = mapping.getField().getControl();
                if (c==null || c.isDisposed()) {
                    continue;
                }
                MessageList fieldMessages;
                if (mapping.getField().isTextContentParsable()) {
                    fieldMessages = list.getMessagesFor(mapping.getObject(), mapping.getPropertyName());
                } else {
                    fieldMessages = new MessageList();
                    fieldMessages.add(Message.newError(EditField.INVALID_VALUE,
                            Messages.IpsObjectPartContainerUIController_invalidValue));
                }
                mapping.getField().setMessages(fieldMessages);
            }
            return list;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return new MessageList();
        }
    }
    
    private void applyControlBindings() {
        List copy = new ArrayList(controlBindings);
        for (Iterator it = copy.iterator(); it.hasNext();) {
            ControlPropertyBinding binding = (ControlPropertyBinding) it.next();
            try {
                binding.updateUI();
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error updating ui with control binding " + binding)); //$NON-NLS-1$
            }
        }
        
    }

    class Listener implements ContentsChangeListener, ValueChangeListener, FocusListener {
        
        public void valueChanged(FieldValueChangedEvent e) {
            List copy = new ArrayList(mappings); // defensive copy to avoid concurrent modification exceptions
            for (Iterator it = copy.iterator(); it.hasNext();) {
                FieldPropertyMapping mapping = (FieldPropertyMapping) it.next();
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

        public void focusGained(FocusEvent e) {
            // nothing to do        
        }

        public void focusLost(FocusEvent e) {
            // broadcast outstanding change events
            IpsPlugin.getDefault().getEditFieldChangeBroadcaster().broadcastLastEvent();
        }
        
        public void contentsChanged(ContentChangeEvent event) {
            List copy = new ArrayList(mappings); // defensive copy to avoid concurrent modification exceptions
            for (Iterator it = copy.iterator(); it.hasNext();) {
                FieldPropertyMapping mapping = (FieldPropertyMapping) it.next();
                if (mapping.getObject() instanceof IIpsObjectPartContainer) {
                    if (event.isAffected((IIpsObjectPartContainer)mapping.getObject())) {
                        try {
                            mapping.setControlValue();
                        } catch (Exception ex) {
                            IpsPlugin.log(new IpsStatus("Error updating model property " + mapping.getPropertyName() //$NON-NLS-1$
                                    + " of object " + mapping.getObject(), ex)); //$NON-NLS-1$
                        }
                    }
                }
            }
            showValidationStatus();
            applyControlBindings();
        }

    }
}
