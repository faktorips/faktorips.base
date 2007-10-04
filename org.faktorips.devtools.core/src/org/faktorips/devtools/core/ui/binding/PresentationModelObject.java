/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.binding.BindingContext.Listener;

/**
 * Base class for presentation model objects implementing IPropertyChangeListenerSupport.
 * 
 * @author Jan Ortmann
 */
public class PresentationModelObject  {

    private Set propertyChangeListeners = new HashSet(1); 
    
    public PresentationModelObject() {
    }

    /**
     * {@inheritDoc}
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener==null) {
            return;
        }
        propertyChangeListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeListeners.remove(listener);
    }

    /**
     * Notifies all listeners that the object has chnaged. No detailed information about property and
     * old and new value is passed to the listeners.
     * <p> 
     * If a listener throws an exception while dealing with the exception, the exception
     * is logged, but NOT rethrown. Instead the method continues to notify the remaining listeners.
     */
    protected void notifyListeners() {
        notifyListeners(new PropertyChangeEvent(this, null, null, null));
    }

    /**
     * Notifies all listeners that the given event has occured.
     * If a listener throws an exception while dealing with the exception, the exception
     * is logged, but NOT rethrown. Instead the method continues to notify the remaining listeners.
     */
    protected void notifyListeners(PropertyChangeEvent event) {
        List listeners = new ArrayList(propertyChangeListeners); // copy to be thread-safe
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            PropertyChangeListener listener = (PropertyChangeListener)it.next();
            try {
                listener.propertyChange(event);
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
    }

}
