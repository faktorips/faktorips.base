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

package org.faktorips.devtools.core.ui.controller;

import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

/**
 * 
 * @author Jan Ortmann
 */
public class EditFieldChangesBroadcaster {

    private FieldValueChangedEvent lastEvent;
    
    public EditFieldChangesBroadcaster() {
        super();
    }

    /**
     * Broadcastes the given event. 
     *  
     * @param event
     * @param listeners
     */
    public void broadcastDelayed(FieldValueChangedEvent event, ValueChangeListener[] listeners) {
        if (lastEvent!=null && lastEvent.field != event.field) {
            broadcastImmediatly(event, listeners);
        }
        lastEvent = event;
        // todo delayed handling
    }
    
    public void broadcastLastEvent(ValueChangeListener[] listeners) {
        if (lastEvent!=null) {
            broadcastImmediatly(lastEvent, listeners);
            lastEvent = null;
        }
    }

    public void broadcastImmediatly(FieldValueChangedEvent event, ValueChangeListener[] listeners) {
        
    }

}
