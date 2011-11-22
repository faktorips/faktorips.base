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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.enums.DefaultEnumValue;
import org.faktorips.devtools.core.enums.EnumType;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.FieldExtensionPropertyMapping;
import org.faktorips.devtools.core.ui.controller.FieldPropertyMapping;
import org.faktorips.devtools.core.ui.controller.FieldPropertyMappingByPropertyDescriptor;
import org.faktorips.devtools.core.ui.controller.Messages;
import org.faktorips.devtools.core.ui.controller.ProblemMarkerPropertyMapping;
import org.faktorips.devtools.core.ui.controller.fields.ButtonField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.IntegerField;
import org.faktorips.devtools.core.ui.controller.fields.LabelField;
import org.faktorips.devtools.core.ui.controller.fields.StructuredViewerField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.AbstractCheckbox;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.devtools.core.util.BeanUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * A <code>BindingContext</code> provides binding between the user interface and a (domain or
 * presentation) model.
 * <p>
 * Currently the context provides the following types of binding methods:
 * <ul>
 * <li><strong>bindContent</strong> Binds the content shown in a control to a model object property.
 * </li>
 * <li><strong>bindEnable</strong> Binds a control's enabled property to a model object property of
 * type boolean.</li>
 * <li><strong>bindVisible</strong> Binds a control's visible property to a model object property of
 * type boolean.</li>
 * </ul>
 * 
 * @author Jan Ortmann
 */
public class BindingContext {

    /**
     * listener for changes and focus losts. Instance of an inner class is used to avoid poluting
     * this class' interface.
     */
    private Listener listener = new Listener();

    /** list of mappings between edit fields and properties of model objects. */
    private List<FieldPropertyMapping> mappings = new ArrayList<FieldPropertyMapping>();

    /**
     * a list of the ips objects containing at least one binded ips part container each container is
     * contained in the list only once, so it is actually used as a set, not we still use the list,
     * because once binded, we need to access all binded containers, and this is faster with a list,
     * than a hashset or treeset.
     */
    private List<IIpsObject> ipsObjects = new ArrayList<IIpsObject>(1);

    private List<ControlPropertyBinding> controlBindings = new ArrayList<ControlPropertyBinding>(2);

    private Set<String> ignoredMessageCodes = new HashSet<String>(2);

    /**
     * Updates the UI with information from the model.
     */
    public void updateUI() {
        // defensive copy to avoid concurrent modification exceptions
        List<FieldPropertyMapping> copy = new ArrayList<FieldPropertyMapping>(mappings);
        for (FieldPropertyMapping mapping : copy) {
            try {
                mapping.setControlValue();
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error updating control for property " + mapping.getPropertyName() //$NON-NLS-1$
                        + " of object " + mapping.getObject(), e)); //$NON-NLS-1$
            }
        }

        showValidationStatus(copy);
        applyControlBindings();
    }

    /**
     * Binds the given text control to the given ips object's property.
     * 
     * @return The edit field created to access the value in the text control.
     * 
     * @throws IllegalArgumentException If the property is not of type String.
     * @throws NullPointerException If any argument is <code>null</code>.
     */
    public EditField<?> bindContent(Text text, Object object, String propertyName) {
        EditField<?> field = null;
        PropertyDescriptor property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);

        if (String.class == property.getPropertyType()) {
            field = new TextField(text);
        } else if (Integer.class == property.getPropertyType() || Integer.TYPE == property.getPropertyType()) {
            field = new IntegerField(text);
        }

        if (field == null) {
            throwWrongPropertyTypeException(property, new Class[] { String.class, Integer.class });
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
    public EditField<String> bindContent(Label label, Object object, String property) {
        checkPropertyType(object, property, String.class);
        EditField<String> field = new LabelField(label);
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
    public EditField<Boolean> bindContent(AbstractCheckbox checkbox, Object object, String propertyName) {
        PropertyDescriptor property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);
        if (Boolean.class != property.getPropertyType() && Boolean.TYPE != property.getPropertyType()) {
            throwWrongPropertyTypeException(property, new Class[] { Boolean.class, Boolean.TYPE });
        }

        EditField<Boolean> field = new CheckboxField(checkbox);
        bindContent(field, object, propertyName);
        return field;
    }

    /**
     * Binds the selection state of the given button to the given ips object property
     * 
     * @return the edit field created to access the value in the text control.
     * 
     * @throws IllegalArgumentException if the property is not of type Boolean or boolean.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public EditField<Boolean> bindContent(Button button, Object object, String propertyName) {
        PropertyDescriptor property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);
        if (Boolean.class != property.getPropertyType() && Boolean.TYPE != property.getPropertyType()) {
            throwWrongPropertyTypeException(property, new Class[] { Boolean.class, Boolean.TYPE });
        }

        EditField<Boolean> field = new ButtonField(button);
        bindContent(field, object, propertyName);
        return field;
    }

    /**
     * Binds the given text-button control to the given ips object's property.
     * <p>
     * This method setting the fields property <code>supportNull<code> to false. This is the default
     * to prevent the fields returning null or setting the null string representation.
     * If you want to create a field that supports null and the null string representation, use
     * {@link #bindContent(TextButtonControl, Object, String, boolean) )}
     * 
     * @return the edit field created to access the value in the text control.
     * 
     * @throws IllegalArgumentException if the property is not of type String.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public EditField<String> bindContent(TextButtonControl control, Object object, String property) {
        return bindContent(control, object, property, false);
    }

    /**
     * Binds the given text-button control to the given ips object's property.
     * 
     * @return the edit field created to access the value in the text control.
     * 
     * @throws IllegalArgumentException if the property is not of type String.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public EditField<String> bindContent(TextButtonControl control, Object object, String property, boolean supportsNull) {
        checkPropertyType(object, property, String.class);
        TextButtonField field = new TextButtonField(control);
        field.setSupportsNull(supportsNull);
        bindContent(field, object, property);

        return field;
    }

    /**
     * Binds the given combo to the given ips object's property.
     * 
     * @return the edit field created to access the value in the text control.
     * 
     * @throws IllegalArgumentException if the property's type is not a subclass of
     *             DefaultEnumValue.
     * @throws NullPointerException if any argument is <code>null</code>.
     * 
     * @see DefaultEnumValue
     */
    public EnumValueField bindContent(Combo combo, Object object, String property, EnumType enumType) {
        checkPropertyType(object, property, DefaultEnumValue.class);
        EnumValueField field = new EnumValueField(combo, enumType);
        bindContent(field, object, property);

        return field;
    }

    /**
     * Binds the given combo to the given ips object's property.
     * 
     * @return the edit field created to access the value in the text control.
     * 
     * @throws IllegalArgumentException if the property's type is not a subclass of
     *             DefaultEnumValue.
     * @throws NullPointerException if any argument is <code>null</code>.
     * 
     * @see DefaultEnumValue
     */
    public <E extends Enum<E>> EnumField<E> bindContent(Combo combo, Object object, String property, Class<E> enumType) {
        checkPropertyType(object, property, Enum.class);
        EnumField<E> field = new EnumField<E>(combo, enumType);
        bindContent(field, object, property);
        return field;
    }

    /**
     * Binds the given combo to the given ips object's property.
     * 
     * @return the edit field created to access the value in the text control.
     * 
     * @throws IllegalArgumentException if the property's type is not a subclass of
     *             DefaultEnumValue.
     * @throws NullPointerException if any argument is <code>null</code>.
     * 
     * @see DefaultEnumValue
     */
    public <E extends Enum<E>> EnumField<E> bindContent(Combo combo, Object object, String property, E[] values) {
        EnumField<E> field = new EnumField<E>(combo, values);
        bindContent(field, object, property);
        return field;
    }

    public void bindContent(StructuredViewer viewer, Class<?> elementType, Object object, String propertyName) {
        bindContent(StructuredViewerField.newInstance(viewer, elementType), object, propertyName);
    }

    /**
     * Binds the given edit field to the given ips object's property.
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public void bindContent(EditField<?> field, Object object, String property) {
        add(createMapping(field, object, property));
    }

    /**
     * Binds the given object's property to the given field to show the problem markers. This
     * binding will not update neither the control nor the object's property. This binding could not
     * be used for object type of {@link IExtensionPropertyAccess}
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public <T> void bindProblemMarker(EditField<T> field, Object object, String propertyName) {
        PropertyDescriptor property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);
        ProblemMarkerPropertyMapping<T> problemMarkerPropertyMapping = new ProblemMarkerPropertyMapping<T>(field,
                object, property);
        add(problemMarkerPropertyMapping);
    }

    protected <T> FieldPropertyMapping createMapping(EditField<T> editField, Object object, String propertyName) {
        if (object instanceof IExtensionPropertyAccess) {
            IExtensionPropertyDefinition extProperty = IpsPlugin.getDefault().getIpsModel()
                    .getExtensionPropertyDefinition(object.getClass(), propertyName, true);
            if (extProperty != null) {
                return new FieldExtensionPropertyMapping<T>(editField, (IExtensionPropertyAccess)object, propertyName);
            }
        }

        PropertyDescriptor property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);
        return new FieldPropertyMappingByPropertyDescriptor<T>(editField, object, property);
    }

    private void checkPropertyType(Object object, String propertyName, Class<?> expectedType) {
        PropertyDescriptor property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);
        if (!expectedType.isAssignableFrom(property.getPropertyType())) {
            throw new IllegalArgumentException(
                    "Expected property " + property.getName() + " to be of type " + expectedType //$NON-NLS-1$ //$NON-NLS-2$
                            + ", but is of type " + property.getPropertyType()); //$NON-NLS-1$
        }
    }

    private void throwWrongPropertyTypeException(PropertyDescriptor property, Class<?>[] expectedTypes) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < expectedTypes.length; i++) {
            if (i > 0) {
                buffer.append(", "); //$NON-NLS-1$
            }
            buffer.append(expectedTypes[i]);
        }

        throw new IllegalArgumentException(
                "Property " + property.getName() + " is of type " + property.getPropertyType() //$NON-NLS-1$ //$NON-NLS-2$
                        + ", but is expected to of one of the types " + buffer.toString()); //$NON-NLS-1$
    }

    /**
     * Binds the control's enabled property to the given part container's property.
     * 
     * @param control The control which enabled property is bound
     * @param object The object the control is bound to
     * @param property The name of the object's property the control is bound to.
     * 
     * @throws IllegalArgumentException if the object's property is not of type boolean.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public void bindEnabled(Control control, Object object, String property) {
        add(new EnableBinding(control, object, property, true));
    }

    /**
     * Binds the control's enabled property to the given part container's property.
     * 
     * @param control The control which enabled property is bound
     * @param object The object the control is bound to
     * @param property The name of the object's property the control is bound to.
     * @param enabledIfTrue <code>true</code> if the control should be enabled if the object's
     *            property is <code>true</code>, <code>false</code> if it should be enabled if the
     *            object's property is <code>false</code>.
     * 
     * @throws IllegalArgumentException if the object's property is not of type boolean.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public void bindEnabled(Control control, Object object, String property, boolean enabledIfTrue) {
        add(new EnableBinding(control, object, property, enabledIfTrue));
    }

    /**
     * Binds the control's enabled property to the given part container's property.
     * 
     * @param control The control which enabled property is bound
     * @param object The object the control is bound to
     * @param property The name of the object's property the control is bound to.
     * @param expectedValue the control is enabled when the value given by the property is equal to
     *            the expected value
     * 
     * @throws IllegalArgumentException if the object's property is not of type boolean.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public void bindEnabled(Control control, Object object, String property, Object expectedValue) {
        add(new EnableBinding(control, object, property, expectedValue));
    }

    /**
     * Binds the control's visible property to the given part container's property.
     * 
     * @throws IllegalArgumentException if the object's property is not of type boolean.
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public void bindVisible(Control control, Object object, String property) {
        add(new VisibleBinding(control, object, property));
    }

    protected void add(FieldPropertyMapping mapping) {
        registerIpsModelChangeListener();
        mapping.getField().addChangeListener(listener);
        mapping.getField().getControl().addFocusListener(listener);
        mappings.add(mapping);
        Object object = mapping.getObject();
        if (object instanceof IIpsObjectPartContainer) {
            IIpsObjectPartContainer container = (IIpsObjectPartContainer)object;
            IIpsObject ipsObject = container.getIpsObject();
            if (!ipsObjects.contains(ipsObject)) {
                ipsObjects.add(ipsObject);
            }
        }
        addListenerToObject(object);
    }

    protected void addListenerToObject(Object object) {
        if (object instanceof PresentationModelObject) {
            PresentationModelObject pmo = (PresentationModelObject)object;
            pmo.addPropertyChangeListener(listener);
        }
    }

    private void removeListenerFromObject(Object object) {
        if (object instanceof PresentationModelObject) {
            PresentationModelObject pmo = (PresentationModelObject)object;
            pmo.removePropertyChangeListener(listener);
        }
    }

    public void add(ControlPropertyBinding binding) {
        registerIpsModelChangeListener();
        controlBindings.add(binding);
        addListenerToObject(binding.getObject());
    }

    private void registerIpsModelChangeListener() {
        if (mappings.size() == 0 && controlBindings.size() == 0) {
            IpsPlugin.getDefault().getIpsModel().addChangeListener(listener);
        }
    }

    /**
     * Removes all bindings for the given control. Use this method if you need to unbind a specific
     * control from an object without affecting other controls that are bound to the same object.
     * This is useful for for objects that stay the same but individual UI controls need to be
     * detached on user interaction.
     */
    /*
     * TODO AW: It would be nice to have this more fine granular - remove enabledState binding,
     * content binding etc.
     */
    public void removeBindings(Control control) {
        List<Object> listenerRemoveCandidates = new ArrayList<Object>();
        for (Iterator<ControlPropertyBinding> it = controlBindings.iterator(); it.hasNext();) {
            ControlPropertyBinding binding = it.next();
            if (binding.getControl() == control) {
                it.remove();
                listenerRemoveCandidates.add(binding.getObject());
            }
        }
        for (Iterator<FieldPropertyMapping> it = mappings.iterator(); it.hasNext();) {
            FieldPropertyMapping mapping = it.next();
            if (mapping.getField().getControl() == control) {
                it.remove();
                mapping.getField().removeChangeListener(listener);
                if (!mapping.getField().getControl().isDisposed()) {
                    mapping.getField().getControl().removeFocusListener(listener);
                }
                listenerRemoveCandidates.add(mapping.getObject());
            }
        }
        for (Object listenerRemoveCandidate : listenerRemoveCandidates) {
            if (!existsMappingOrBindingFor(listenerRemoveCandidate)) {
                removeListenerFromObject(listenerRemoveCandidate);
            }
        }
    }

    private boolean existsMappingOrBindingFor(Object candidate) {
        for (ControlPropertyBinding binding : controlBindings) {
            if (binding.getObject() == candidate) {
                return true;
            }
        }
        for (FieldPropertyMapping mapping : mappings) {
            if (mapping.getObject() == candidate) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all bindings/mappings that are connected to the given object. Use this method if
     * multiple controls are bound to a specific object and you want to remove all of them at once.
     * This is useful when binding controls to an object that is created or deleted by user
     * interaction (e.g. an IpsObjectPart).
     * 
     * @param object the bound object.
     */
    public void removeBindings(Object object) {
        for (Iterator<ControlPropertyBinding> it = controlBindings.iterator(); it.hasNext();) {
            ControlPropertyBinding binding = it.next();
            if (binding.getObject() == object) {
                it.remove();
            }
        }
        for (Iterator<FieldPropertyMapping> it = mappings.iterator(); it.hasNext();) {
            FieldPropertyMapping mapping = it.next();
            if (mapping.getObject() == object) {
                it.remove();
                mapping.getField().removeChangeListener(listener);
                mapping.getField().getControl().removeFocusListener(listener);
            }
        }
        removeListenerFromObject(object);
    }

    /**
     * Removes all bindings.
     */
    public void clear() {
        controlBindings.clear();
        for (FieldPropertyMapping mapping : mappings) {
            mapping.getField().setMessages(new MessageList());
        }
        mappings.clear();
    }

    /**
     * Removes the registered listener.
     */
    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(listener);
        // defensive copy to avoid concurrent modification
        List<FieldPropertyMapping> mappingsCopy = new CopyOnWriteArrayList<FieldPropertyMapping>(mappings);

        // exceptions
        Set<Object> disposedPmos = new HashSet<Object>();
        for (FieldPropertyMapping mapping : mappingsCopy) {
            mapping.getField().removeChangeListener(listener);
            if (!mapping.getField().getControl().isDisposed()) {
                mapping.getField().getControl().removeFocusListener(listener);
            }
            disposeObjectIfNeccessary(disposedPmos, mapping);
        }

        // defensive copy to avoid concurrent
        List<ControlPropertyBinding> controlsCopy = new CopyOnWriteArrayList<ControlPropertyBinding>(controlBindings);

        // modification exceptions
        for (ControlPropertyBinding mapping : controlsCopy) {
            disposeObjectIfNeccessary(disposedPmos, mapping);
        }
    }

    private void disposeObjectIfNeccessary(Set<Object> disposedPmos, Object object) {
        if (object instanceof IpsObjectPartPmo) {
            if (!disposedPmos.contains(object)) {
                ((IpsObjectPartPmo)object).dispose();
                disposedPmos.add(object);
            }
        }
    }

    /**
     * Validates all bound part containers and updates the fields that are associated with their
     * properties.
     */
    protected void showValidationStatus(List<FieldPropertyMapping> propertyMappings) {
        List<IIpsObject> copy = new CopyOnWriteArrayList<IIpsObject>(ipsObjects);
        for (IIpsObject iIpsObject : copy) {
            showValidationStatus(iIpsObject, propertyMappings);
        }
    }

    /**
     * Validates the part container and updates the fields that are associated with attributes of
     * the {@link IIpsObject}.
     * <p>
     * Returns the {@link MessageList} which is the result of the validation. This return value can
     * be evaluated when overriding this method.
     * 
     * @return the validation message list (never returns null)
     */
    protected MessageList showValidationStatus(IIpsObject ipsObject, List<FieldPropertyMapping> propertyMappings) {
        MessageList allValidationMessages;
        try {
            allValidationMessages = ipsObject.validate(ipsObject.getIpsProject());
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return new MessageList();
        }

        Map<EditField<?>, MessageList> messagesPerField = new HashMap<EditField<?>, MessageList>();
        for (FieldPropertyMapping mapping : propertyMappings) {
            if (mapping.getField().getControl() == null || mapping.getField().getControl().isDisposed()) {
                continue;
            }

            MessageList fieldMessages = messagesPerField.get(mapping.getField());
            if (fieldMessages == null) {
                fieldMessages = new MessageList();
            }
            fieldMessages.add(getMappingMessages(mapping, allValidationMessages));
            messagesPerField.put(mapping.getField(), fieldMessages);
        }

        setFieldMessages(messagesPerField);

        return allValidationMessages;
    }

    private MessageList getMappingMessages(FieldPropertyMapping mapping, MessageList allValidationMessages) {
        MessageList mappingMessages = new MessageList();
        if (mapping.getField().isTextContentParsable()) {
            for (Message message : allValidationMessages.getMessagesFor(mapping.getObject(), mapping.getPropertyName())) {
                if (!(ignoredMessageCodes.contains(message.getCode()))) {
                    mappingMessages.add(message);
                }
            }
        } else {
            mappingMessages.add(Message.newError(EditField.INVALID_VALUE,
                    Messages.IpsObjectPartContainerUIController_invalidValue));
        }
        return mappingMessages;
    }

    private void setFieldMessages(Map<EditField<?>, MessageList> messagesPerField) {
        for (EditField<?> field : messagesPerField.keySet()) {
            field.setMessages(messagesPerField.get(field));
        }
    }

    private void applyControlBindings() {
        applyControlBindings(null);
    }

    /**
     * Applies all bindings in this context and provides them with the given property name.
     * 
     * @param propertyName the name of the changed property, or <code>null</code> if all properties
     *            of an object may have changed.
     */
    private void applyControlBindings(String propertyName) {
        List<ControlPropertyBinding> copy = new CopyOnWriteArrayList<ControlPropertyBinding>(controlBindings);
        for (ControlPropertyBinding binding : copy) {
            try {
                if (propertyName == null) {
                    binding.updateUI();
                } else {
                    binding.updateUI(propertyName);
                }
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error updating ui with control binding " + binding)); //$NON-NLS-1$
            }
        }
    }

    class Listener implements ContentsChangeListener, ValueChangeListener, FocusListener, PropertyChangeListener {

        @Override
        public void valueChanged(FieldValueChangedEvent e) {
            // defensive copy to avoid concurrent modification
            List<FieldPropertyMapping> copy = new CopyOnWriteArrayList<FieldPropertyMapping>(mappings);

            // exceptions
            for (FieldPropertyMapping mapping : copy) {
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

        @Override
        public void focusGained(FocusEvent e) {
            // nothing to do
        }

        @Override
        public void focusLost(FocusEvent e) {
            // broadcast outstanding change events
            IpsUIPlugin.getDefault().getEditFieldChangeBroadcaster().broadcastLastEvent();
        }

        @Override
        public void contentsChanged(ContentChangeEvent event) {
            // defensive copy to avoid concurrent modification
            List<FieldPropertyMapping> copy = new CopyOnWriteArrayList<FieldPropertyMapping>(mappings);

            // exceptions
            for (FieldPropertyMapping mapping : copy) {
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

            showValidationStatus(copy);
            applyControlBindings();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // defensive copy to avoid concurrent modification
            List<FieldPropertyMapping> copy = new CopyOnWriteArrayList<FieldPropertyMapping>(mappings);

            // exceptions
            for (FieldPropertyMapping mapping : copy) {
                if (mapping.getObject() == evt.getSource()) {
                    try {
                        mapping.setControlValue();
                    } catch (Exception ex) {
                        IpsPlugin.log(new IpsStatus("Error updating model property " + mapping.getPropertyName() //$NON-NLS-1$
                                + " of object " + mapping.getObject(), ex)); //$NON-NLS-1$
                    }
                }
            }

            applyControlBindings(evt.getPropertyName());
        }
    }

    /**
     * Adds the given message code to the set of message codes that will be ignored during
     * validation.
     * <p>
     * Returns true if the message code was not already ignored, false otherwise.
     * 
     * @param messageCode The message code to ignore during validation
     * 
     * @throws NullPointerException If the parameter is null
     */
    public final boolean addIgnoredMessageCode(String messageCode) {
        ArgumentCheck.notNull(messageCode);
        return ignoredMessageCodes.add(messageCode);
    }

    /**
     * Removes the given message code from the set of messages that will be ignored during
     * validation.
     * <p>
     * Returns true if the message code was really ignored, false otherwise.
     * 
     * @param messageCode The message code to no longer ignore during validation
     * 
     */
    public final boolean removeIgnoredMessageCode(String messageCode) {
        return ignoredMessageCodes.remove(messageCode);
    }

    /**
     * Returns an unmodifiable view on the set of message codes that are ignored during validation.
     */
    public final Set<String> getIgnoredMessageCodes() {
        return Collections.unmodifiableSet(ignoredMessageCodes);
    }

    /**
     * Returns whether the given message code is ignored during validation.
     * 
     * @param messageCode The message code to check whether it is ignored
     * 
     * @throws NullPointerException If the parameter is null
     */
    public final boolean isIgnoredMessageCode(String messageCode) {
        ArgumentCheck.notNull(messageCode);
        return ignoredMessageCodes.contains(messageCode);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Ctx["); //$NON-NLS-1$
        for (int i = 0; i < ipsObjects.size(); i++) {
            if (i > 0) {
                sb.append(", "); //$NON-NLS-1$
            }
            IIpsObject ipsObject = ipsObjects.get(i);
            sb.append(ipsObject.getName());
        }
        sb.append(']');

        return sb.toString();
    }

    /*
     * For testing purposes
     */
    protected int getNumberOfMappingsAndBindings() {
        return mappings.size() + controlBindings.size();
    }

}
