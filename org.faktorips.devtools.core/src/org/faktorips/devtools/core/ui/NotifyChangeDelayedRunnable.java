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

package org.faktorips.devtools.core.ui;

/**
 * A runnable that signals value changes to several listeners after a specified delayed.
 * This class contains a nested event for the several listener.
 *
 * @author Joerg Ortmann
 */
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

public class NotifyChangeDelayedRunnable implements IIdentifiableDelayedRunnable {
    /* The listener the event should be notified to */
    private ValueChangeListener[] listeners;

    /* The event which occurred */
    private FieldValueChangedEvent fieldValueChangedEvent;
    
    /** The initiator of the event */
    private String id;
    
    /**
     * An runnable which contains a nested event which is related to several listeners. The id 
     * specifies the initiator of this even.
     * 
     * @param listeners Listeners which are interested in the given event
     * @param fieldValueChangedEvent The nested event which for the given listener
     * @param id Identifier of the initiator object of this event, 
     * indicates the initiator which has created and fired this event
     */
    public NotifyChangeDelayedRunnable(ValueChangeListener[] listeners, FieldValueChangedEvent fieldValueChangedEvent,
            String id) {
        this.listeners = listeners;
        this.fieldValueChangedEvent = fieldValueChangedEvent;
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].valueChanged(fieldValueChangedEvent);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public int getDelayTime() {
        return 200;
    }
}
