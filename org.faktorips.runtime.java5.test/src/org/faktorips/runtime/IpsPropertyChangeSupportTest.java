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

package org.faktorips.runtime;

import static org.mockito.Mockito.mock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ConcurrentModificationException;

import org.junit.Test;

public class IpsPropertyChangeSupportTest {

    /**
     * In FIPS-942 a {@link ConcurrentModificationException} was reported. This test does reproduce
     * this case.
     */
    @Test
    public void testFireChildPropertyChange_concurrentException() throws Exception {
        Object sourceBean = mock(Object.class);
        IpsPropertyChangeSupport propertyChangeSupport = new IpsPropertyChangeSupport(sourceBean);

        PropertyChangeListener listener = new TestConcurrentPropertyChangeListener(propertyChangeSupport);
        propertyChangeSupport.addPropertyChangeListener(listener, true);
        propertyChangeSupport.addPropertyChangeListener(mock(PropertyChangeListener.class), true);

        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(mock(Object.class), null, null, null));

    }

    /**
     * This test property change listener tries to change the list of property change listeners in
     * property change event.
     */
    private static class TestConcurrentPropertyChangeListener implements PropertyChangeListener {

        private final IpsPropertyChangeSupport propertyChangeSupport;

        public TestConcurrentPropertyChangeListener(IpsPropertyChangeSupport propertyChangeSupport) {
            this.propertyChangeSupport = propertyChangeSupport;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            PropertyChangeListener listener = mock(PropertyChangeListener.class);
            propertyChangeSupport.addPropertyChangeListener(listener, true);
        }

    }

}
