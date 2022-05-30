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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Extension of {@link PropertyChangeSupport} providing special methods to fire
 * {@link AssociationChangedEvent}s.
 * 
 * @author Daniel Hohenberger
 */
public class IpsPropertyChangeSupport extends PropertyChangeSupport {

    private static final long serialVersionUID = -236185652576376253L;

    private final Object sourceBean;

    private final List<PropertyChangeListener> childChangeListeners = new CopyOnWriteArrayList<>();

    public IpsPropertyChangeSupport(Object sourceBean) {
        super(sourceBean);
        this.sourceBean = sourceBean;
    }

    /**
     * Fire an existing AssociationChangedEvent to any registered listeners. No event is fired if
     * the given event's added and removed targets are equal or both null.
     * 
     * @param evt The AssociationChangedEvent object.
     */
    public void fireAssociationChange(AssociationChangedEvent evt) {
        Object addedAssociationTarget = evt.getAddedAssociationTarget();
        Object removedAssociationTarget = evt.getRemovedAssociationTarget();
        if (addedAssociationTarget == null && removedAssociationTarget == null) {
            return;
        }
        firePropertyChange(evt);
    }

    /**
     * Events with the same source as the source registered as {@code sourceBean} are fired as
     * usual, others are fired using
     * {@link IpsPropertyChangeSupport#fireChildPropertyChange(PropertyChangeEvent)}. {@inheritDoc}
     */
    @Override
    public void firePropertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == sourceBean) {
            super.firePropertyChange(evt);
        } else {
            fireChildPropertyChange(evt);
        }
    }

    /**
     * Report a bound association property update to any registered listeners. No event is fired if
     * the added association target is null.
     * 
     * @param associationName The programmatic name of the association that was changed.
     * @param addedAssociationTarget The target added to the association.
     */
    public void fireAssociationAdded(String associationName, Object addedAssociationTarget) {
        if (addedAssociationTarget == null) {
            return;
        }
        fireAssociationChange(new AssociationChangedEvent(sourceBean, associationName, null, addedAssociationTarget));
    }

    /**
     * Report a bound association property update to any registered listeners. No event is fired if
     * the removed association target is null.
     * 
     * @param associationName The programmatic name of the association that was changed.
     * @param removedAssociationTarget The target removed from the association.
     */
    public void fireAssociationRemoved(String associationName, Object removedAssociationTarget) {
        if (removedAssociationTarget == null) {
            return;
        }
        fireAssociationChange(new AssociationChangedEvent(sourceBean, associationName, removedAssociationTarget, null));
    }

    /**
     * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
     * 
     * @param propagateEventsFromChildren if set to {@code true}, this object's change listeners
     *            will also be notified when one of this object's children fires a change event.
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener,
            boolean propagateEventsFromChildren) {
        super.addPropertyChangeListener(listener);
        if (propagateEventsFromChildren) {
            childChangeListeners.add(listener);
        }
    }

    /**
     * This will be called if any child of this object fires a change event. Any listeners
     * subscribed to this object interested in child property changes will be notified in turn.
     */
    public void fireChildPropertyChange(PropertyChangeEvent evt) {
        for (PropertyChangeListener target : childChangeListeners) {
            target.propertyChange(evt);
        }
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        super.removePropertyChangeListener(listener);
        if (childChangeListeners.contains(listener)) {
            childChangeListeners.remove(listener);
        }
    }

}
