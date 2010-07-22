/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Interface for classes that allow listeners for their properties.
 * 
 * @author Daniel Hohenberger
 */
public interface INotificationSupport {

    /**
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
     * 
     * @param propagateEventsFromChildren if set to {@code true}, this object's change listeners
     *            will also be notified when one of this object's children fires a change event.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener, boolean propagateEventsFromChildren);

    /**
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(String,
     *      PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(String,
     *      PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * @see java.beans.PropertyChangeSupport#hasListeners(String)
     */
    public boolean hasListeners(String propertyName);

    /**
     * @see java.beans.PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)
     */
    public void notifyChangeListeners(PropertyChangeEvent event);

}
