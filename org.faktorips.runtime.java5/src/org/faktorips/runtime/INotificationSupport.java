/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.runtime;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Interface for classes that allow listeners for their properties.
 * 
 * @author Daniel Hohenberger
 */
public interface INotificationSupport {

    /**
     * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
     * 
     * @param propagateEventsFromChildren if set to {@code true}, this object's change listeners
     *            will also be notified when one of this object's children fires a change event.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener, boolean propagateEventsFromChildren);

    /**
     * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * @see PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * @see PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * @see PropertyChangeSupport#hasListeners(String)
     */
    public boolean hasListeners(String propertyName);

    /**
     * @see PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)
     */
    public void notifyChangeListeners(PropertyChangeEvent event);

}
