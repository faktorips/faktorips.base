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
 * An event that signals that a new event for the given listener is available.
 * This class contains a nested event for the several listener.
 *
 * @author Joerg Ortmann
 */
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

public class NotifyChangeListenerEvent {
    /** The listener the event should be notified to */
    public ValueChangeListener[] listeners;

    /** The event which occurred */
    public FieldValueChangedEvent fieldValueChangedEvent;
    
    /** The initiator of the event */
    public Object initiator;
    
    /**
     * An event which contains a nested event which is related to several listeners. The initiator 
     * specifies the initiator of this even.
     * 
     * @param listeners Listeners which are interested in the given event
     * @param fieldValueChangedEvent The nested event which for the given listener
     * @param initiator Initiator object of this event, indicates the initiator which has created and fired this event
     */
    public NotifyChangeListenerEvent(ValueChangeListener[] listeners, FieldValueChangedEvent fieldValueChangedEvent,
            Object initiator) {
        this.listeners = listeners;
        this.fieldValueChangedEvent = fieldValueChangedEvent;
        this.initiator = initiator;
    }
}
