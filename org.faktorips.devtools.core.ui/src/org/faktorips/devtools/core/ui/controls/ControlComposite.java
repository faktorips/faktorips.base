/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A composite that is used to build complex controls.
 * <p>
 * Provides the <code>listenToControl()</code> method that complex controls use to register their
 * primitive controls. This allows events triggered by the primitive controls to be broadcasted to
 * listeners of the complex control.
 * 
 * @author Jan Ortmann
 */
public abstract class ControlComposite extends Composite {

    private List<ControlTypeStruct> listeningInfo = null;
    private BroadcastListener broadcastListener = null;

    public ControlComposite(Composite parent, int style) {
        super(parent, style);
    }

    protected void listenToControl(Control control, int eventType) {
        if (control == null) {
            throw new IllegalArgumentException("Control mustn't be null, eventType=" + eventType); //$NON-NLS-1$
        }
        if (listeningInfo == null) {
            listeningInfo = new ArrayList<>();
            broadcastListener = new BroadcastListener();
        }
        for (ControlTypeStruct each : listeningInfo) {
            if (each.control == control && each.eventType == eventType) {
                return;
            }
        }
        control.addListener(eventType, broadcastListener);
        listeningInfo.add(new ControlTypeStruct(control, eventType));
    }

    private class BroadcastListener implements Listener {

        @Override
        public void handleEvent(Event event) {
            notifyListeners(event.type, event);
        }

    }

    private static class ControlTypeStruct {
        Control control;
        int eventType;

        ControlTypeStruct(Control c, int type) {
            control = c;
            eventType = type;
        }
    }

}
