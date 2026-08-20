/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal.filter;

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.filter.IProductCmptPropertyFilter;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class PropertyVisibleControllerTest {

    @Mock
    private Control outerControl;

    private MockitoSession mockito;

    private PropertyVisibleController controller;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        controller = new PropertyVisibleController();
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        mockito.finishMocking();
    }

    @Test
    public void testUpdateUI() {
        IProductCmptProperty p1 = mock(IProductCmptProperty.class);
        IProductCmptProperty p2 = mock(IProductCmptProperty.class);
        Composite parent = mock(Composite.class);
        GridData p1C1LayoutData = new GridData();
        GridData p1C2LayoutData = new GridData();
        GridData p2C1LayoutData = new GridData();
        GridData p2C2LayoutData = new GridData();
        Control p1C1 = mockControl(parent, p1C1LayoutData);
        Control p1C2 = mockControl(parent, p1C2LayoutData);
        Control p2C1 = mockControl(parent, p2C1LayoutData);
        Control p2C2 = mockControl(parent, p2C2LayoutData);
        IProductCmptPropertyFilter filter1 = mock(IProductCmptPropertyFilter.class);
        IProductCmptPropertyFilter filter2 = mock(IProductCmptPropertyFilter.class);

        lenient().when(filter1.isFiltered(p1)).thenReturn(false);
        lenient().when(filter2.isFiltered(p1)).thenReturn(true);
        lenient().when(filter1.isFiltered(p2)).thenReturn(false);
        lenient().when(filter2.isFiltered(p2)).thenReturn(false);

        controller.addFilter(filter1);
        controller.addFilter(filter2);

        controller.addPropertyControlMapping(outerControl, p1, p1C1, p1C2);
        controller.addPropertyControlMapping(outerControl, p2, p2C1, p2C2);

        // Check visibility
        verify(p1C1).setVisible(false);
        verify(p1C2).setVisible(false);
        verify(p2C1).setVisible(true);
        verify(p2C2).setVisible(true);

        // Check grid data exclusion
        assertTrue(p1C1LayoutData.exclude);
        assertTrue(p1C2LayoutData.exclude);
        assertFalse(p2C1LayoutData.exclude);
        assertFalse(p2C2LayoutData.exclude);
    }

    @Test
    public void testIsFiltered() {
        IProductCmptProperty p1 = mock(IProductCmptProperty.class);
        IProductCmptProperty p2 = mock(IProductCmptProperty.class);
        IProductCmptPropertyFilter filter1 = mock(IProductCmptPropertyFilter.class);
        IProductCmptPropertyFilter filter2 = mock(IProductCmptPropertyFilter.class);

        lenient().when(filter1.isFiltered(p1)).thenReturn(false);
        lenient().when(filter2.isFiltered(p1)).thenReturn(true);
        lenient().when(filter1.isFiltered(p2)).thenReturn(false);
        lenient().when(filter2.isFiltered(p2)).thenReturn(false);

        controller.addFilter(filter1);
        controller.addFilter(filter2);

        assertTrue(controller.isFiltered(p1));
        assertFalse(controller.isFiltered(p2));
    }

    @Test
    public void testUpdateUI_RelayoutSectionParent() {
        Runnable callback = mock(Runnable.class);
        controller.setRefreshCallback(callback);

        controller.updateUI(true);

        verify(callback).run();
    }

    @Test
    public void testAddPropertyControlMapping() {
        IProductCmptProperty property = mock(IProductCmptProperty.class);
        Control control1 = mockControl(null, new GridData());
        Control control2 = mockControl(null, new GridData());

        assertTrue(controller.addPropertyControlMapping(outerControl, property, control1));
        assertFalse(controller.addPropertyControlMapping(outerControl, property, control1));
        assertTrue(controller.addPropertyControlMapping(outerControl, property, control2));
        assertTrue(controller.addPropertyControlMapping(outerControl, property, control1, control2));
    }

    @Test
    public void testAddPropertyControlMapping_settingVisibleState() {
        IProductCmptProperty property = mock(IProductCmptProperty.class);
        Control control1 = mockControl(null, new GridData());
        Control control2 = mockControl(null, new GridData());

        IProductCmptPropertyFilter filter = mock(IProductCmptPropertyFilter.class);
        when(filter.isFiltered(property)).thenReturn(true);
        controller.addFilter(filter);

        controller.addPropertyControlMapping(outerControl, property, control1);
        verify(control1).setVisible(false);

        control1 = mockControl(null, new GridData());

        controller.addPropertyControlMapping(outerControl, property, control2);
        verifyNoInteractions(control1);
        verify(control2).setVisible(false);

        control2 = mockControl(null, new GridData());

        controller.addPropertyControlMapping(outerControl, property, control1, control2);
        verify(control1).setVisible(false);
        verify(control2).setVisible(false);
    }

    @Test
    public void testAddPropertyControlMapping_NoControlProvided() {
        assertThrows(IllegalArgumentException.class, () -> {
            IProductCmptProperty property = mock(IProductCmptProperty.class);
            controller.addPropertyControlMapping(outerControl, property);
        });
    }

    /**
     * <strong>Scenario:</strong><br>
     * A property control mapping is added, but one of the provided controls does not provide
     * {@link GridData} as layout data.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * An {@link IllegalArgumentException} should be thrown as only controls using {@link GridData}
     * can be fully excluded from the UI.
     */
    @Test
    public void testAddPropertyControlMapping_ControlWithNoGridDataProvided() {
        assertThrows(IllegalArgumentException.class, () -> {
            IProductCmptProperty property = mock(IProductCmptProperty.class);
            Control c1 = mockControl(null, new GridData());
            Control c2 = mockControl(null, new RowData());

            controller.addPropertyControlMapping(outerControl, property, c1, c2);
        });
    }

    @Test
    public void testRemovePropertyControlMapping() {
        IProductCmptProperty property = mock(IProductCmptProperty.class);
        Control control = mockControl(null, new GridData());

        controller.addPropertyControlMapping(outerControl, property, control);
        assertTrue(controller.removePropertyControlMapping(outerControl));
        assertFalse(controller.removePropertyControlMapping(outerControl));
    }

    @Test
    public void testAddFilter() {
        IProductCmptPropertyFilter filter = mock(IProductCmptPropertyFilter.class);
        assertTrue(controller.addFilter(filter));
        assertFalse(controller.addFilter(filter));
    }

    @Test
    public void testRemoveFilter() {
        IProductCmptPropertyFilter filter = mock(IProductCmptPropertyFilter.class);
        controller.addFilter(filter);
        assertTrue(controller.removeFilter(filter));
        assertFalse(controller.removeFilter(filter));
    }

    private Control mockControl(Composite parent, Object layoutData) {
        Control control = mock(Control.class);
        if (parent == null) {
            parent = mock(Composite.class);
        }

        lenient().when(control.getParent()).thenReturn(parent);
        when(control.getLayoutData()).thenReturn(layoutData);

        return control;
    }

}
