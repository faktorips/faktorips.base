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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.faktorips.devtools.core.IpsPlugin;

/**
 * Base class for presentation model objects implementing IPropertyChangeListenerSupport.
 * 
 * @author Jan Ortmann
 */
public class PresentationModelObject {

    private Set<PropertyChangeListener> propertyChangeListeners = new HashSet<PropertyChangeListener>(1);

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
        List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>(
                propertyChangeListeners); // copy
        // to be thread-safe
        for (PropertyChangeListener listener : listeners) {
            try {
                listener.propertyChange(event);
            } catch (Exception e) {
                // catch all exception and only log them to notify the other listeners
                IpsPlugin.log(e);
            }
        }
    }

    /*
     * For testing purposes
     */
    public int getListenerCount() {
        return propertyChangeListeners.size();
    }
}
