/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A composite that is used to build complex controls. 
 * >p>
 * Provides the <code>listenToControl()</code> method that complex controls use to register their primitive controls.
 * This allows events triggered by the primitive controls to be broadcasted to listeners of the complex control.
 * 
 * @author Jan Ortmann
 */
public abstract class ControlComposite extends Composite {

    private List listeningInfo = null;
    private BroadcastListener broadcastListener = null;

    
    public ControlComposite(Composite parent, int style) {
        super(parent, style);
    }

    protected void listenToControl(Control control, int eventType) {
        if (listeningInfo==null) {
            listeningInfo = new ArrayList();
            broadcastListener = new BroadcastListener();
        }
        for (Iterator it=listeningInfo.iterator(); it.hasNext();) {
            ControlTypeStruct each = (ControlTypeStruct)it.next();
            if (each.control==control && each.eventType==eventType) {
                return;
            }
        }
        control.addListener(eventType, broadcastListener);
        listeningInfo.add(new ControlTypeStruct(control, eventType));
    }
    
    private class BroadcastListener implements Listener {

        public void handleEvent(Event event) {
            notifyListeners(event.type, event);
        }
        
    }
    
    private class ControlTypeStruct {
        Control control;
        int eventType;
        
        ControlTypeStruct(Control c, int type) {
            control = c;
            eventType = type;
        }
    }
    
}
