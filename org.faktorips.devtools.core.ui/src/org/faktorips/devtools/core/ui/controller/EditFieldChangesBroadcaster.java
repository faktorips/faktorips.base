/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.model.internal.IpsModel;

/**
 * Posts change events to several listeners. The events will be broadcasted after a specified delay.
 * If the edit field which initiates this event sends a new change event, but the previous events
 * wasn't broadcated yet - because the treshold wasn't reached - then the wait time will be reset to
 * zero again.<br>
 * If an edit field sends a change event but there is an outstanding event from another edit field,
 * then the outstanding event will be directly broadcasted (flushed) and the new event will be
 * delayed.
 * 
 * @author Jan Ortmann
 */
public class EditFieldChangesBroadcaster {

    /** delay time for change events */
    private static final int DELAY_TIME = IpsPlugin.getDefault().getIpsPreferences().getChangeEventDelayTime();

    private DateFormat debugFormat;
    private FieldValueChangedEvent lastEvent;
    private ValueChangeListener[] lastListeners;
    private long lastEventTime = 0;

    /** contains the current broadcast event */
    private FieldValueChangedEvent currentEvent = null;

    /** mutex for synchronize reason */
    private Object mutex = new Object();

    /** indicates if the internal delay job is running */
    private boolean running = false;

    /** debug counter for accrued events */
    private int eventCounter = 0;

    /**
     * Broadcastes the given event to the listener after a specified delay time.
     */
    public void broadcastDelayed(FieldValueChangedEvent event, ValueChangeListener[] listeners) {
        synchronized (mutex) {
            if (lastEvent != null && lastEvent.field != event.field) {
                broadcastLastEvent();
            }
            incrementCounter();

            lastEvent = event;
            lastListeners = listeners;
            lastEventTime = System.currentTimeMillis();

            startBroadcastDelayedJobIfNecessary();
        }
    }

    /**
     * Broadcasts the occurred event immediately.
     */
    public void broadcastLastEvent() {
        if (lastEvent != null) {
            broadcastImmediately(lastEvent, lastListeners);
            lastEvent = null;
        }
    }

    /**
     * Broadcasts the given event to the listeners immediately.
     */
    public void broadcastImmediately(FieldValueChangedEvent event, ValueChangeListener[] listeners) {
        synchronized (mutex) {
            if (currentEvent != null && currentEvent.field == event.field) {
                logTrace("Skip current broadcast event"); //$NON-NLS-1$
                return;
            }
            if (isDebugOn()) {
                logTrace("Start broadcast." + (eventCounter == 1 ? "" : " Accrued events " + eventCounter)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

            currentEvent = event;

            resetCounter();
            for (ValueChangeListener listener : listeners) {
                try {
                    if (event.field.getControl() != null && event.field.getControl().isDisposed()) {
                        // don't notifiy listeners if the control is disposed
                        continue;
                    }
                    listener.valueChanged(event);
                    // CSOFF: IllegalCatch
                } catch (RuntimeException e) {
                    // CSON: IllegalCatch
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
            currentEvent = null;
        }
        logTrace("Finished broadcast"); //$NON-NLS-1$
    }

    /**
     * Start the delay broadcast job if the job is currently not running. The job stops
     * automatically after broadcasting the last occurred event.
     */
    private void startBroadcastDelayedJobIfNecessary() {
        if (!running) {
            logTrace("Start new job"); //$NON-NLS-1$
            running = true;
            BroadcastDelayedUIJob fUpdateJob = new BroadcastDelayedUIJob();
            fUpdateJob.schedule(DELAY_TIME);
        }
    }

    //
    // Methods for debugging (tracing)
    //

    /**
     * Trace the given log message if <code>IpsModel.TRACE_MODEL_CHANGE_LISTENERS</code> is
     * <code>true</code>
     */
    private void logTrace(String message) {
        if (!isDebugOn()) {
            return;
        }
        StringBuffer msgBuf = new StringBuffer(message.length() + 40);
        msgBuf.append("EditFieldChangesBroadcaster "); //$NON-NLS-1$
        if (debugFormat == null) {
            debugFormat = new SimpleDateFormat("(HH:mm:ss.SSS): "); //$NON-NLS-1$
        }
        debugFormat.format(new Date(), msgBuf, new FieldPosition(0));
        msgBuf.append(message);
        System.out.println(msgBuf.toString());
    }

    private void resetCounter() {
        if (isDebugOn()) {
            eventCounter = 1;
        }
    }

    private void incrementCounter() {
        if (isDebugOn()) {
            eventCounter++;
        }
    }

    private boolean isDebugOn() {
        return IpsModel.TRACE_MODEL_CHANGE_LISTENERS;
    }

    /**
     * Internal system ui job to check when it is time to broadcast the delayed event.
     */
    private class BroadcastDelayedUIJob extends UIJob {

        public BroadcastDelayedUIJob() {
            super("BroadcastDelayedUIJob"); //$NON-NLS-1$
            setSystem(true);
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            synchronized (mutex) {
                if ((System.currentTimeMillis() - lastEventTime) > DELAY_TIME) {
                    // it is time to broadcast the event, afterwards stop this job
                    try {
                        broadcastLastEvent();
                        // CSOFF: IllegalCatch
                    } catch (RuntimeException e) {
                        // CSON: IllegalCatch
                        logTrace("Error: " + e.getMessage()); //$NON-NLS-1$
                    }
                    running = false;
                }
            }

            // schedule to check the next event delay time
            // or to get discarded by the job manager (if running is false)
            schedule(DELAY_TIME / 2);
            return Status.OK_STATUS;
        }

        @Override
        public boolean shouldSchedule() {
            return running;
        }
    }
}
