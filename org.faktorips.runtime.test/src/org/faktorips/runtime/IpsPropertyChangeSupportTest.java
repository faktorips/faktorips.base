/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            PropertyChangeListener listener = mock(PropertyChangeListener.class);
            propertyChangeSupport.addPropertyChangeListener(listener, true);
        }

    }

}
