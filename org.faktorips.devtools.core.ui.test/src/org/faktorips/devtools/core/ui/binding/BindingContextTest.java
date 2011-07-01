/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.FieldPropertyMapping;
import org.junit.Before;
import org.junit.Test;
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

        EditField field = mock(EditField.class);
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

        EditField field = mock(EditField.class);
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
}
