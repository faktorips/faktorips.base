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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.binding.BindingContext.Listener;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.FieldPropertyMapping;
import org.faktorips.devtools.core.ui.controller.FieldPropertyMappingByPropertyDescriptor;
import org.faktorips.devtools.core.ui.controller.fields.IntegerField;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.Validatable;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BindingContextTest extends AbstractIpsPluginTest {

    private BindingContext bindingContext;

    private TestPMO pmo;

    private EditField<?> editField;

    private Control controlMock;

    private Listener listener = mock(BindingContext.Listener.class);

    @Mock
    private IIpsSrcFile ipsSrcFile;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        bindingContext = new BindingContext();
        pmo = new TestPMO();

        editField = Mockito.mock(EditField.class);
        controlMock = mock(Control.class);
        when(editField.getControl()).thenReturn(controlMock);

        when(ipsSrcFile.exists()).thenReturn(true);
    }

    @Test
    public void applyAllBindingsOnContentChange() {
        ControlPropertyBinding binding = new TestBinding(controlMock, pmo, TestPMO.PROPERTY_ENABLED, null);
        binding = spy(binding);
        bindingContext.add(binding);

        ContentChangeEvent contentChangeEventMock = ContentChangeEvent.newWholeContentChangedEvent(ipsSrcFile);
        ((IpsModel)IIpsModel.get()).notifyChangeListeners(contentChangeEventMock);

        verify(binding, times(1)).updateUI();
    }

    @Test
    public void applyPropertyChange() {
        bindingContext = new BindingContext(listener);
        ControlPropertyBinding binding = new TestBinding(controlMock, pmo, TestPMO.PROPERTY_ENABLED, null);
        bindingContext.add(binding);

        pmo.setEnabled(true);

        verify(listener, times(1)).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void updateAllMappingsOnContentChange() {
        IProductCmpt prodCmpt = newProductCmpt(newIpsProject(), "ProdCmpt");
        FieldPropertyMapping<?> mapping = bindingContext.createMapping(editField, prodCmpt, "name"); // some
        // valid
        // property
        FieldPropertyMapping<?> spyMapping = spy(mapping);
        bindingContext.add(spyMapping);

        // force property change
        prodCmpt.newGeneration();
        verify(spyMapping, times(1)).setControlValue();
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

        EditField<Object> field = mockField();
        @SuppressWarnings("unchecked")
        FieldPropertyMapping<Object> mapping = mock(FieldPropertyMapping.class);
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

        EditField<Object> field = mockField();
        @SuppressWarnings("unchecked")
        FieldPropertyMapping<Object> mapping = mock(FieldPropertyMapping.class);
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

    public static class DummyControlPropertyBinding extends ControlPropertyBinding {
        private DummyControlPropertyBinding(Control control) {
            super(control, null, null, null);
        }

        @Override
        public void updateUiIfNotDisposed(String nameOfChangedProperty) {
            // don't
        }
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

        public void setOtherProperty(@SuppressWarnings("unused") String value) {
            //
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
            //
        }

    }

    @Test
    public void removeBindingIfControlIsDisposed() {
        Control control = mock(Control.class);
        ControlPropertyBinding binding = spy(new DummyControlPropertyBinding(control));

        doReturn(control).when(binding).getControl();
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
        FieldPropertyMappingByPropertyDescriptor<Integer> mapping = mock(
                FieldPropertyMappingByPropertyDescriptor.class);
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
    public void testErrorBinding_WithValidatable() {
        Validatable validatable = mockValidatable();
        EditField<?> field = mockField();
        Validatable validatableWithError = mockValidatable();
        EditField<?> fieldWithError = mockField();

        MessageList list = new MessageList();
        MessageList msgList = new MessageList();
        msgList.add(
                new Message("code", "messageText", Message.ERROR, validatableWithError, IValueHolder.PROPERTY_VALUE));
        when(validatable.validate(any(IIpsProject.class))).thenReturn(list);
        when(validatableWithError.validate(any(IIpsProject.class))).thenReturn(msgList);

        bindingContext.bindProblemMarker(field, validatable, IValueHolder.PROPERTY_VALUE);
        bindingContext.bindProblemMarker(fieldWithError, validatableWithError, IValueHolder.PROPERTY_VALUE);

        bindingContext.updateUI();

        ArgumentCaptor<MessageList> captor = ArgumentCaptor.forClass(MessageList.class);

        verify(field).setMessages(list);
        verify(field).setMessages(captor.capture());
        assertEquals(0, captor.getValue().size());

        captor = ArgumentCaptor.forClass(MessageList.class);
        verify(fieldWithError).setMessages(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertNotNull(captor.getValue().getMessageByCode("code"));
    }

    protected Validatable mockValidatable() {
        Validatable av = mock(IValueHolder.class);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(av.getIpsProject()).thenReturn(ipsProject);
        return av;
    }

    protected EditField<Object> mockField() {
        @SuppressWarnings("unchecked")
        EditField<Object> field = mock(EditField.class);
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
    public void testErrorBinding_WithIpsObject() {
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
    public void testErrorBinding_WithIpsObject_DifferentParts() {
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
    public void testMultipleProblemToOneField() throws Exception {
        Validatable validatable1 = mockValidatable();
        EditField<?> field = mockField();
        Validatable validatable2 = mockValidatable();

        MessageList list1 = new MessageList();
        MessageList list2 = new MessageList();
        list1.add(new Message("code1", "messageText", Message.ERROR, validatable1, IValueHolder.PROPERTY_VALUE));
        list2.add(new Message("code2", "messageText", Message.ERROR, validatable2, IValueHolder.PROPERTY_VALUE));
        when(validatable1.validate(any(IIpsProject.class))).thenReturn(list1);
        when(validatable2.validate(any(IIpsProject.class))).thenReturn(list2);

        bindingContext.bindProblemMarker(field, validatable1, IValueHolder.PROPERTY_VALUE);
        bindingContext.bindProblemMarker(field, validatable2, IValueHolder.PROPERTY_VALUE);

        bindingContext.updateUI();

        ArgumentCaptor<MessageList> captor = ArgumentCaptor.forClass(MessageList.class);
        verify(field).setMessages(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertNotNull(captor.getValue().getMessageByCode("code1"));
        assertNotNull(captor.getValue().getMessageByCode("code2"));
    }

    @Test
    public void testMultipleProblemsInOneObjectPropertyToOneField() throws Exception {
        Validatable validatable1 = mockValidatable();
        EditField<?> field = mockField();
        Validatable validatable2 = mockValidatable();

        MessageList list1 = new MessageList();
        MessageList list2 = new MessageList();
        list1.add(new Message("code1", "messageText", Message.ERROR, validatable1, IValueHolder.PROPERTY_VALUE));
        list2.add(new Message("code2", "messageText2", Message.ERROR, validatable1, IValueHolder.PROPERTY_VALUE));
        when(validatable1.validate(any(IIpsProject.class))).thenReturn(list1);
        when(validatable2.validate(any(IIpsProject.class))).thenReturn(list2);

        bindingContext.bindProblemMarker(field, validatable1, IValueHolder.PROPERTY_VALUE);
        bindingContext.bindProblemMarker(field, validatable2, IValueHolder.PROPERTY_VALUE);

        bindingContext.updateUI();

        ArgumentCaptor<MessageList> captor = ArgumentCaptor.forClass(MessageList.class);
        verify(field).setMessages(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertNotNull(captor.getValue().getMessageByCode("code1"));
        assertNotNull(captor.getValue().getMessageByCode("code2"));
    }

    @Test
    public void testGetMappedPart_ipsObjectPartContainer() throws Exception {
        IIpsObjectPartContainer ipsObjectPartContainer = mock(IIpsObjectPartContainer.class);

        IIpsObjectPartContainer mappedPart = bindingContext.getMappedPart(ipsObjectPartContainer);

        assertSame(ipsObjectPartContainer, mappedPart);
    }

    @Test
    public void testGetMappedPart_ipsObjectPartPmo() throws Exception {
        IIpsObjectPartContainer ipsObjectPartContainer = mock(IIpsObjectPartContainer.class);
        IpsObjectPartPmo pmo = mock(IpsObjectPartPmo.class);
        when(pmo.getIpsObjectPartContainer()).thenReturn(ipsObjectPartContainer);

        IIpsObjectPartContainer mappedPart = bindingContext.getMappedPart(pmo);

        assertSame(ipsObjectPartContainer, mappedPart);
    }

    @Test
    public void testGetMappedPart_adaptable() throws Exception {
        IIpsObjectPartContainer ipsObjectPartContainer = mock(IIpsObjectPartContainer.class);
        IAdaptable adaptable = mock(IAdaptable.class);
        when(adaptable.getAdapter(IIpsObjectPartContainer.class)).thenReturn(ipsObjectPartContainer);

        IIpsObjectPartContainer mappedPart = bindingContext.getMappedPart(adaptable);

        assertSame(ipsObjectPartContainer, mappedPart);
    }

}
