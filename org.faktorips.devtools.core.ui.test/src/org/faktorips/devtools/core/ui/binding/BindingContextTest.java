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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyChangeEvent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Control;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
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
    public void applyingAllBindingsOnContentChange() throws CoreException {
        ControlPropertyBinding binding = new TestBinding(controlMock, pmo, TestPMO.PROPERTY_ENABLED, null);
        binding = spy(binding);
        bindingContext.add(binding);

        IIpsObjectPartContainer ipsObject = newProductCmpt(newIpsProject(), "ProdCmpt");
        ContentChangeEvent contentChangeEventMock = mock(ContentChangeEvent.class);
        when(contentChangeEventMock.isAffected(ipsObject)).thenReturn(true);

        bindingContext.getListener().contentsChanged(contentChangeEventMock);

        verify(binding, times(1)).updateUI();
    }

    @Test
    public void applyingAllBindingsOnPropertyChange() {
        ControlPropertyBinding binding = new TestBinding(controlMock, pmo, TestPMO.PROPERTY_ENABLED, null);
        binding = spy(binding);
        bindingContext.add(binding);

        pmo.setEnabled(true);
        verify(binding, times(1)).updateUI(TestPMO.PROPERTY_ENABLED);
    }

    @Test
    public void updatingAllMappingsOnContentChange() throws CoreException {
        IIpsObjectPartContainer ipsObject = newProductCmpt(newIpsProject(), "ProdCmpt");
        FieldPropertyMapping mapping = bindingContext.createMapping(editField, ipsObject, "name"); // some
                                                                                                   // valid
                                                                                                   // property
        FieldPropertyMapping spyMapping = spy(mapping);
        bindingContext.add(spyMapping);

        ContentChangeEvent contentChangeEventMock = mock(ContentChangeEvent.class);
        when(contentChangeEventMock.isAffected(ipsObject)).thenReturn(true);

        bindingContext.getListener().contentsChanged(contentChangeEventMock);
        verify(spyMapping, times(1)).setControlValue();
    }

    @Test
    public void updatingMappingsOnPropertyChangeForSameProperty() {
        updatingMappingsOnPropertyChange(TestPMO.PROPERTY_ENABLED);
    }

    @Test
    public void updatingMappingsOnPropertyChangeForDifferentProperty() {
        updatingMappingsOnPropertyChange(TestPMO.PROPERTY_OTHER_PROPERTY);
    }

    protected void updatingMappingsOnPropertyChange(String propertyName) {
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
