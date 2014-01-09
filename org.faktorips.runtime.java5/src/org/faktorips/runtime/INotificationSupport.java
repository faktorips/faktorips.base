/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
