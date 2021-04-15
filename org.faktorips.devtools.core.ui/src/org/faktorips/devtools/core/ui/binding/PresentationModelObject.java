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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.faktorips.devtools.core.IpsPlugin;

/**
 * Base class for presentation model objects implementing IPropertyChangeListenerSupport.
 * 
 * @author Jan Ortmann
 */
public class PresentationModelObject {

    private final Set<PropertyChangeListener> propertyChangeListeners = new CopyOnWriteArraySet<>();

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        propertyChangeListeners.add(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeListeners.remove(listener);
    }

    /**
     * Notifies all listeners that the object has changed. No detailed information about property
     * and old and new value is passed to the listeners.
     * <p>
     * If a listener throws an exception while dealing with the exception, the exception is logged,
     * but NOT re-thrown. Instead the method continues to notify the remaining listeners.
     */
    protected void notifyListeners() {
        notifyListeners(new PropertyChangeEvent(this, null, null, null));
    }

    /**
     * Notifies all listeners that the given event has occurred. If a listener throws an exception
     * while dealing with the exception, the exception is logged, but NOT re-thrown. Instead the
     * method continues to notify the remaining listeners.
     */
    protected void notifyListeners(PropertyChangeEvent event) {
        for (PropertyChangeListener listener : propertyChangeListeners) {
            try {
                listener.propertyChange(event);
            } catch (Exception e) {
                // catch all exception and only log them to notify the other listeners
                IpsPlugin.log(e);
            }
        }
    }
}
