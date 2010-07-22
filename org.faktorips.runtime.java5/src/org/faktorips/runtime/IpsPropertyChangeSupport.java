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
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Extension of {@link PropertyChangeSupport} providing special methods to fire
 * {@link AssociationChangedEvent}s.
 * 
 * @author Daniel Hohenberger
 */
public class IpsPropertyChangeSupport extends PropertyChangeSupport {

    /**
     * Generated <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -236185652576376253L;

    private final Object sourceBean;

    private Vector<PropertyChangeListener> childChangeListeners;

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
            if (childChangeListeners == null) {
                childChangeListeners = new Vector<PropertyChangeListener>();
            }
            childChangeListeners.add(listener);
        }
    }

    /**
     * This will be called if any child of this object fires a change event. Any listeners
     * subscribed to this object interested in child property changes will be notified in turn.
     */
    public void fireChildPropertyChange(PropertyChangeEvent evt) {
        List<PropertyChangeListener> targets = null;
        synchronized (this) {
            if (childChangeListeners != null) {
                targets = Collections.unmodifiableList(childChangeListeners);
            }
        }
        if (targets != null) {
            for (PropertyChangeListener target : targets) {
                target.propertyChange(evt);
            }
        }
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        super.removePropertyChangeListener(listener);
        if (childChangeListeners != null && childChangeListeners.contains(listener)) {
            childChangeListeners.remove(listener);
        }
    }

}
