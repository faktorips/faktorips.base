/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.GregorianCalendar;

import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NewGenerationPMOTest {

    private final SingletonMockHelper singletonHelper = new SingletonMockHelper();

    private GregorianCalendar defaultValidityDate;

    private PMOChangeListener pmoChangeListener;

    private NewGenerationPMO pmo;

    @Before
    public void setUp() {
        defaultValidityDate = new GregorianCalendar(1986, 04, 16);

        IpsUIPlugin ipsUIPlugin = mock(IpsUIPlugin.class);
        when(ipsUIPlugin.getDefaultValidityDate()).thenReturn(defaultValidityDate);
        singletonHelper.setSingletonInstance(IpsUIPlugin.class, ipsUIPlugin);

        pmoChangeListener = new PMOChangeListener();
        pmo = createPMO();
    }

    private NewGenerationPMO createPMO() {
        NewGenerationPMO pmo = new NewGenerationPMO();
        pmo.addPropertyChangeListener(pmoChangeListener);
        return pmo;
    }

    @After
    public void tearDown() {
        singletonHelper.reset();
    }

    @Test
    public void testSetValidFrom() {
        GregorianCalendar validFrom = new GregorianCalendar(1986, 4, 16);
        pmo.setValidFrom(validFrom);

        assertPropertyChangeEvent(pmo, NewGenerationPMO.PROPERTY_VALID_FROM, defaultValidityDate, validFrom);
        assertEquals(validFrom, pmo.getValidFrom());
    }

    @Test
    public void testGetValidFrom_IsDefaultValidityDateAfterInitialization() {
        assertEquals(defaultValidityDate, pmo.getValidFrom());
    }

    @Test
    public void testIsSkipExistingGenerations() {
        pmo.setSkipExistingGenerations(true);
        assertPropertyChangeEvent(pmo, NewGenerationPMO.PROPERTY_SKIP_EXISTING_GENERATIONS, false, true);
        assertTrue(pmo.isSkipExistingGenerations());

        pmo.setSkipExistingGenerations(false);
        assertPropertyChangeEvent(pmo, NewGenerationPMO.PROPERTY_SKIP_EXISTING_GENERATIONS, true, false);
        assertFalse(pmo.isSkipExistingGenerations());
    }

    @Test
    public void testIsSkipExistingGenerations_IsFalseAfterInitialization() {
        assertFalse(pmo.isSkipExistingGenerations());
    }

    private void assertPropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue) {
        assertEquals(source, pmoChangeListener.lastEvent.getSource());
        assertEquals(propertyName, pmoChangeListener.lastEvent.getPropertyName());
        assertEquals(oldValue, pmoChangeListener.lastEvent.getOldValue());
        assertEquals(newValue, pmoChangeListener.lastEvent.getNewValue());
    }

    private static class PMOChangeListener implements PropertyChangeListener {

        private PropertyChangeEvent lastEvent;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            lastEvent = evt;
        }

    }

}
