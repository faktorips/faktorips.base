/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.FieldPropertyMapping;
import org.faktorips.devtools.core.ui.controller.FieldPropertyMappingByPropertyDescriptor;
import org.faktorips.devtools.core.ui.controller.ProblemMarkerPropertyMapping;
import org.faktorips.devtools.core.ui.controller.fields.IntegerField;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class BindingContextTest extends AbstractIpsPluginTest {

    private BindingContext bindingContext;
    private TestPMO pmo;
    private EditField<?> editField;
    private Control controlMock;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        bindingContext = new BindingContext();
        pmo = new TestPMO();

        editField = Mockito.mock(EditField.class);
        controlMock = mock(Control.class);
        when(editField.getControl()).thenReturn(controlMock);
    }

    @Test
    public void applyAllBindingsOnContentChange() throws CoreException {
        ControlPropertyBinding binding = new TestBinding(controlMock, pmo, TestPMO.PROPERTY_ENABLED, null);
        binding = spy(binding);
        bindingContext.add(binding);

        IIpsObjectPartContainer ipsObject = newProductCmpt(newIpsProject(), "ProdCmpt");
        ContentChangeEvent contentChangeEventMock = mock(ContentChangeEvent.class);
        when(contentChangeEventMock.isAffected(ipsObject)).thenReturn(true);

        // force property change
        pmo.fireChange();
        verify(binding, times(1)).updateUI();
    }

    @Test
    public void applyAllBindingsOnPropertyChange() {
        ControlPropertyBinding binding = new TestBinding(controlMock, pmo, TestPMO.PROPERTY_ENABLED, null);
        binding = spy(binding);
        bindingContext.add(binding);

        pmo.setEnabled(true);
        verify(binding, times(1)).updateUI(TestPMO.PROPERTY_ENABLED);
    }

    @Test
    public void updateAllMappingsOnContentChange() throws CoreException {
        IProductCmpt prodCmpt = newProductCmpt(newIpsProject(), "ProdCmpt");
        FieldPropertyMapping mapping = bindingContext.createMapping(editField, prodCmpt, "name"); // some
                                                                                                  // valid
                                                                                                  // property
        FieldPropertyMapping spyMapping = spy(mapping);
        bindingContext.add(spyMapping);

        // force property change
        prodCmpt.newGeneration();
        verify(spyMapping, times(1)).setControlValue();
    }

    @Test
    public void updateMappingsOnPropertyChangeForSameProperty() {
        updateMappingsOnPropertyChange(TestPMO.PROPERTY_ENABLED);
    }

    @Test
    public void updateMappingsOnPropertyChangeForDifferentProperty() {
        updateMappingsOnPropertyChange(TestPMO.PROPERTY_OTHER_PROPERTY);
    }

    @Test
    public void removeListenerFromPMO() {
        ControlPropertyBinding binding = mock(ControlPropertyBinding.class);
        pmo = mock(TestPMO.class);
        when(binding.getObject()).thenReturn(pmo);
        bindingContext.add(binding);

        ControlPropertyBinding binding2 = mock(ControlPropertyBinding.class);
        when(binding2.getObject()).thenReturn(pmo);
        bindingContext.add(binding);

        assertEquals(2, bindingContext.getNumberOfMappingsAndBindings());
        verify(pmo, times(2)).addPropertyChangeListener(any(PropertyChangeListener.class));
        bindingContext.removeBindings(pmo);
        assertEquals(0, bindingContext.getNumberOfMappingsAndBindings());
        verify(pmo).removePropertyChangeListener(any(PropertyChangeListener.class));
    }

    @Test
    public void notRemoveBindingsForUnrelatedObject() {
        ControlPropertyBinding binding = mock(ControlPropertyBinding.class);
        pmo = mock(TestPMO.class);
        when(binding.getObject()).thenReturn(pmo);
        bindingContext.add(binding);

        ControlPropertyBinding binding2 = mock(ControlPropertyBinding.class);
        when(binding2.getObject()).thenReturn(pmo);
        bindingContext.add(binding2);

        String string = new String();
        ControlPropertyBinding binding3 = mock(ControlPropertyBinding.class);
        when(binding3.getObject()).thenReturn(string);
        bindingContext.add(binding3);

        assertEquals(3, bindingContext.getNumberOfMappingsAndBindings());
        verify(pmo, times(2)).addPropertyChangeListener(any(PropertyChangeListener.class));
        bindingContext.removeBindings(string);
        assertEquals(2, bindingContext.getNumberOfMappingsAndBindings());
        verify(pmo, never()).removePropertyChangeListener(any(PropertyChangeListener.class));
    }

    @Test
    public void removeBindingsForControlAndObject() {
        Text textControl = mock(Text.class);
        Text textControl2 = mock(Text.class);
        pmo = mock(TestPMO.class);

        ControlPropertyBinding binding = mock(ControlPropertyBinding.class);
        when(binding.getObject()).thenReturn(pmo);
        when(binding.getControl()).thenReturn(textControl);
        bindingContext.add(binding);

        ControlPropertyBinding binding2 = mock(ControlPropertyBinding.class);
        when(binding2.getObject()).thenReturn(pmo);
        when(binding2.getControl()).thenReturn(textControl2);
        bindingContext.add(binding2);

        assertEquals(2, bindingContext.getNumberOfMappingsAndBindings());
        verify(pmo, times(2)).addPropertyChangeListener(any(PropertyChangeListener.class));
        bindingContext.removeBindings(textControl);
        assertEquals(1, bindingContext.getNumberOfMappingsAndBindings());
        // listener keeps listening to pmo as long a at least on binding exists!
        verify(pmo, never()).removePropertyChangeListener(any(PropertyChangeListener.class));
        bindingContext.removeBindings(textControl2);
        assertEquals(0, bindingContext.getNumberOfMappingsAndBindings());
        // listener removed when last mapping removed
        verify(pmo).removePropertyChangeListener(any(PropertyChangeListener.class));
    }

    @Test
    public void removeMappingAndControlListeners() {
        Text textControl = mock(Text.class);

        EditField field = mockField();
        FieldPropertyMapping mapping = mock(FieldPropertyMapping.class);
        when(mapping.getField()).thenReturn(field);
        when(mapping.getObject()).thenReturn(pmo);
        when(field.getControl()).thenReturn(textControl);
        bindingContext.add(mapping);

        assertEquals(1, bindingContext.getNumberOfMappingsAndBindings());
        verify(textControl).addFocusListener(any(FocusListener.class));
        bindingContext.removeBindings(textControl);
        assertEquals(0, bindingContext.getNumberOfMappingsAndBindings());
        verify(textControl).removeFocusListener(any(FocusListener.class));
    }

    @Test
    public void removeMappingAndControlListeners2() {
        Text textControl = mock(Text.class);

        EditField field = mockField();
        FieldPropertyMapping mapping = mock(FieldPropertyMapping.class);
        when(mapping.getField()).thenReturn(field);
        when(mapping.getObject()).thenReturn(pmo);
        when(field.getControl()).thenReturn(textControl);
        bindingContext.add(mapping);

        assertEquals(1, bindingContext.getNumberOfMappingsAndBindings());
        verify(textControl).addFocusListener(any(FocusListener.class));
        bindingContext.removeBindings(pmo);
        assertEquals(0, bindingContext.getNumberOfMappingsAndBindings());
        verify(textControl).removeFocusListener(any(FocusListener.class));
    }

    protected void updateMappingsOnPropertyChange(String propertyName) {
        FieldPropertyMapping mapping = bindingContext.createMapping(editField, pmo, propertyName);
        FieldPropertyMapping spyMapping = spy(mapping);
        bindingContext.add(spyMapping);

        pmo.setEnabled(true);
        verify(spyMapping, times(1)).setControlValue();
    }

    public class TestPMO extends PresentationModelObject {
        public static final String PROPERTY_ENABLED = "enabled";
        public static final String PROPERTY_OTHER_PROPERTY = "otherProperty";

        private boolean isEnabled = false;

        public boolean isEnabled() {
            return isEnabled;
        }

        public void fireChange() {
            notifyListeners();
        }

        public void setEnabled(boolean enabled) {
            boolean oldValue = isEnabled();
            isEnabled = enabled;
            notifyListeners(new PropertyChangeEvent(this, PROPERTY_ENABLED, oldValue, isEnabled()));
        }

        public void setOtherProperty(String value) {

        }

        public String getOtherProperty() {
            return null;
        }

    }

    public class TestBinding extends ControlPropertyBinding {

        public TestBinding(Control control, Object object, String propertyName, Class<?> exptectedType) {
            super(control, object, propertyName, exptectedType);
        }

        @Override
        public void updateUiIfNotDisposed(String nameOfChangedProperty) {
            // TODO Auto-generated method stub

        }

    }

    @Test
    public void removeBindingIfControlIsDisposed() {
        ControlPropertyBinding binding = mock(ControlPropertyBinding.class);
        Control control = mock(Control.class);

        when(binding.getControl()).thenReturn(control);
        when(control.isDisposed()).thenReturn(true);

        bindingContext.add(binding);
        assertEquals(1, bindingContext.getNumberOfMappingsAndBindings());
        bindingContext.updateUI();
        assertEquals(0, bindingContext.getNumberOfMappingsAndBindings());
    }

    @Test
    public void removeMappingIfControlIsDisposed() {
        @SuppressWarnings("unchecked")
        // Class
        FieldPropertyMappingByPropertyDescriptor<Integer> mapping = mock(FieldPropertyMappingByPropertyDescriptor.class);
        IntegerField editField = mock(IntegerField.class);

        when(mapping.getField()).thenReturn(editField);
        Control control = mock(Control.class);
        when(editField.getControl()).thenReturn(control);
        when(control.isDisposed()).thenReturn(true);

        bindingContext.add(mapping);
        assertEquals(1, bindingContext.getNumberOfMappingsAndBindings());
        bindingContext.updateUI();
        assertEquals(0, bindingContext.getNumberOfMappingsAndBindings());
    }

    @Test
    public void testErrorBinding_WithValidatable() throws CoreException {
        Validatable validatable = mockValidatable();
        EditField<?> field = mockField();
        Validatable validatableWithError = mockValidatable();
        EditField<?> fieldWithError = mockField();

        MessageList list = new MessageList();
        MessageList errorList = new MessageList();
        errorList.add(new Message("code", "messageText", Message.ERROR, validatableWithError,
                IValueHolder.PROPERTY_VALUE));
        when(validatable.validate(any(IIpsProject.class))).thenReturn(list);
        when(validatableWithError.validate(any(IIpsProject.class))).thenReturn(errorList);

        bindingContext.bindProblemMarker(field, validatable, IValueHolder.PROPERTY_VALUE);
        bindingContext.bindProblemMarker(fieldWithError, validatableWithError, IValueHolder.PROPERTY_VALUE);

        bindingContext.updateUI();

        verify(field).setMessages(list);
        verify(field, never()).setMessages(errorList);
        verify(fieldWithError).setMessages(errorList);
        verify(fieldWithError, never()).setMessages(list);
    }

    protected Validatable mockValidatable() {
        Validatable av = mock(IValueHolder.class);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(av.getIpsProject()).thenReturn(ipsProject);
        return av;
    }

    protected EditField<?> mockField() {
        EditField<?> field = mock(EditField.class);
        Control control = mock(Control.class);
        when(field.getControl()).thenReturn(control);
        when(field.isTextContentParsable()).thenReturn(true);
        return field;
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * Two fields bind the same ipsObjectPart.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Both fields have the same message list set.
     */
    @Test
    public void testErrorBinding_WithIpsObject() throws CoreException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IIpsObject ipsObject = mock(IIpsObject.class);
        IIpsObjectPartContainer ipsObjectPart = mock(IAttributeValue.class);
        when(ipsObject.getIpsProject()).thenReturn(ipsProject);
        when(ipsObjectPart.getIpsObject()).thenReturn(ipsObject);
        when(ipsObjectPart.getIpsProject()).thenReturn(ipsProject);

        EditField<?> field = mockField();
        EditField<?> field2 = mockField();

        MessageList errorList = new MessageList();
        errorList.add(new Message("code", "messageText", Message.ERROR, ipsObjectPart,
                IAttributeValue.PROPERTY_VALUE_HOLDER));
        when(ipsObject.validate(any(IIpsProject.class))).thenReturn(errorList);

        bindingContext.bindProblemMarker(field, ipsObjectPart, IAttributeValue.PROPERTY_VALUE_HOLDER);
        bindingContext.bindProblemMarker(field2, ipsObjectPart, IAttributeValue.PROPERTY_VALUE_HOLDER);

        bindingContext.updateUI();

        verify(ipsObject).validate(any(IIpsProject.class));
        verify(field).setMessages(errorList);
        verify(field2).setMessages(errorList);
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * Two fields bind different ipsObjectParts of the same ipsObject.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Both fields receive the message directed to them.
     */
    @Test
    public void testErrorBinding_WithIpsObject_DifferentParts() throws CoreException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IIpsObject ipsObject = mock(IIpsObject.class);
        IIpsObjectPartContainer ipsObjectPart = mock(IAttributeValue.class);
        IIpsObjectPartContainer ipsObjectPart2 = mock(IAttributeValue.class);
        when(ipsObject.getIpsProject()).thenReturn(ipsProject);
        when(ipsObjectPart.getIpsObject()).thenReturn(ipsObject);
        when(ipsObjectPart.getIpsProject()).thenReturn(ipsProject);
        when(ipsObjectPart2.getIpsObject()).thenReturn(ipsObject);
        when(ipsObjectPart2.getIpsProject()).thenReturn(ipsProject);

        EditField<?> field = mockField();
        EditField<?> field2 = mockField();

        MessageList errorList = new MessageList();
        Message msg1 = new Message("code", "messageText", Message.ERROR, ipsObjectPart,
                IAttributeValue.PROPERTY_VALUE_HOLDER);
        errorList.add(msg1);
        Message msg2 = new Message("code2", "messageText2", Message.ERROR, ipsObjectPart2,
                IAttributeValue.PROPERTY_VALUE_HOLDER);
        errorList.add(msg2);
        when(ipsObject.validate(any(IIpsProject.class))).thenReturn(errorList);
        ArgumentCaptor<MessageList> messageListCaptor = ArgumentCaptor.forClass(MessageList.class);
        ArgumentCaptor<MessageList> messageListCaptor2 = ArgumentCaptor.forClass(MessageList.class);

        bindingContext.bindProblemMarker(field, ipsObjectPart, IAttributeValue.PROPERTY_VALUE_HOLDER);
        bindingContext.bindProblemMarker(field2, ipsObjectPart2, IAttributeValue.PROPERTY_VALUE_HOLDER);

        bindingContext.updateUI();

        verify(ipsObject).validate(any(IIpsProject.class));
        verify(field).setMessages(messageListCaptor.capture());
        verify(field2).setMessages(messageListCaptor2.capture());
        assertEquals(1, messageListCaptor.getValue().size());
        assertSame(msg1, messageListCaptor.getValue().getMessage(0));
        assertEquals(1, messageListCaptor2.getValue().size());
        assertSame(msg2, messageListCaptor2.getValue().getMessage(0));

    }

    @Test
    public void testValidatableBelongsToMapping_IpsObjectPart() {
        IIpsObject ipsObject = mock(IIpsObject.class);
        IIpsObject ipsObject2 = mock(IIpsObject.class);
        ProblemMarkerPropertyMapping<?> mapping = mock(ProblemMarkerPropertyMapping.class);
        IIpsObjectPartContainer ipsObjectPart = mock(IIpsObjectPartContainer.class);

        when(mapping.getObject()).thenReturn(ipsObjectPart);
        when(ipsObjectPart.getIpsObject()).thenReturn(ipsObject);

        assertTrue(bindingContext.validatableBelongsToMapping(ipsObject, mapping));
        assertFalse(bindingContext.validatableBelongsToMapping(ipsObject2, mapping));
    }

    @Test
    public void testValidatableBelongsToMapping_Validatable() {
        Validatable validatable = mock(IValueHolder.class);
        Validatable validatable2 = mock(IValueHolder.class);
        ProblemMarkerPropertyMapping<?> mapping = mock(ProblemMarkerPropertyMapping.class);

        when(mapping.getObject()).thenReturn(validatable);

        assertTrue(bindingContext.validatableBelongsToMapping(validatable, mapping));
        assertFalse(bindingContext.validatableBelongsToMapping(validatable2, mapping));

    }

    @Test
    public void testValidatableBelongsToMapping_OtherObject() {
        Validatable validatable = mock(IValueHolder.class);
        String string = "String";
        ProblemMarkerPropertyMapping<?> mapping = mock(ProblemMarkerPropertyMapping.class);

        when(mapping.getObject()).thenReturn(string);

        assertFalse(bindingContext.validatableBelongsToMapping(validatable, mapping));
    }
}
