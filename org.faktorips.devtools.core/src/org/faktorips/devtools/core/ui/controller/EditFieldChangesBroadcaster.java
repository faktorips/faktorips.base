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

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

/**
 * Post change events to several listeners.<br>
 * The events will be broadcasted after a specified delay. If the edit field which initiates this
 * event sends a new change event, but the previous events wasn't broadcated jet - because the delay
 * wasn't reached - then the delay will be retriggered again.<br>
 * If an edit field sends a change event but there is an outstanding event from another edit field,
 * then the outstanding event will be directly broadcast (flushed) and the new event will be delayed.
 * 
 * @author Jan Ortmann
 */
public class EditFieldChangesBroadcaster {
    private static DateFormat DEBUG_FORMAT;
    private static int DELAY_TIME = 200;

    // last event data
    private FieldValueChangedEvent lastEvent;
    private ValueChangeListener[] lastListeners;
    private long lastEventTime = 0;

    // contains the current broadcast event
    private FieldValueChangedEvent currentEvent = null;
    
    // mutex for synchronize reason
    private Boolean mutex = Boolean.TRUE;
    
    // indicates if the internal delay job is running
    private boolean running = false;

    // debug counter for accrued events
    private int eventCounter = 0;

    /*
     * Internal system ui job to check when it is time to broadcast the delayed event.
     */
    private class BroadcastDelayedUIJob extends UIJob {
        public BroadcastDelayedUIJob() {
            super("BroadcastDelayedUIJob");
            setSystem(true);
        }
        
        /**
         * {@inheritDoc}
         */
        public IStatus runInUIThread(IProgressMonitor monitor) {
            synchronized (mutex){
                if ((System.currentTimeMillis() - lastEventTime) > DELAY_TIME){
                    // it is time to broadcast the event, afterwards stop this job
                    try {
                        broadcastLastEvent();
                    }
                    catch (RuntimeException e) {
                        logTrace("Error: " + e.getMessage());
                    }
                    running = false;
                }
            }
            
            // schedule to check the next event delay time
            // or to get discarded by the job manager (if running is false)
            schedule(DELAY_TIME / 2);
            return Status.OK_STATUS;
        }
        
        /**
         * {@inheritDoc}
         */
        public boolean shouldSchedule() {
            return running;
        }        
    }

    /**
     * Broadcastes the given event to the listener after a specified delay time. 
     */
    public void broadcastDelayed(FieldValueChangedEvent event, ValueChangeListener[] listeners) {
        synchronized (mutex){
            if (lastEvent!=null && lastEvent.field != event.field) {
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
     *  Broadcasts the occured event immediately.
     */
    public void broadcastLastEvent() {
        if (lastEvent!=null) {
            broadcastImmediately(lastEvent, lastListeners);
            lastEvent = null;
        }
    }
    
    /**
     *  Broadcasts the given event to the listeners immediately.
     */
    public void broadcastImmediately(FieldValueChangedEvent event, ValueChangeListener[] listeners) {
        synchronized (mutex) {
            if (currentEvent != null && currentEvent.field == event.field) {
                logTrace("Skip current broadcast event");
                return;
            }
            if (isDebugOn()){
                logTrace("Start broadcast." + (eventCounter==1?"":" Accrued events " + eventCounter));
            }
            
            currentEvent = event;

            resetCounter();
            for (int i = 0; i < listeners.length; i++) {
                try {
                    listeners[i].valueChanged(event);
                }
                catch (RuntimeException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
            currentEvent = null;
        }
        logTrace("Finished broadcast");
    }

    /*
     * Start the delay broadcast job if the job is currently not running. The job stops
     * automatically after broadcasting the last occured event.
     */
    private void startBroadcastDelayedJobIfNecessary() {
        if (!running){
            logTrace("Start new job");
            running = true;
            BroadcastDelayedUIJob fUpdateJob = new BroadcastDelayedUIJob();
            fUpdateJob.schedule(DELAY_TIME);
        }
    }
    
    //
    // Methods for debugging (tracing)
    //
    
    /*
     * Trace the given log message if <code>IpsModel.TRACE_MODEL_CHANGE_LISTENERS</code> is <code>true</code>
     */
    private void logTrace(String message) {
        if (!isDebugOn()) {
            return;
        }
        StringBuffer msgBuf = new StringBuffer(message.length() + 40);
        msgBuf.append("EditFieldChangesBroadcaster "); //$NON-NLS-1$
        if (DEBUG_FORMAT == null){
            DEBUG_FORMAT = new SimpleDateFormat("(HH:mm:ss.SSS): "); //$NON-NLS-1$
        }
        DEBUG_FORMAT.format(new Date(), msgBuf, new FieldPosition(0));
        msgBuf.append(message);
        System.out.println(msgBuf.toString());
    }

    private void resetCounter(){
        if (isDebugOn()){
            eventCounter = 1;
        }
    }
    
    private void incrementCounter(){
        if (isDebugOn()){
            eventCounter ++;
        }
    }
    
    private boolean isDebugOn(){
        return IpsModel.TRACE_MODEL_CHANGE_LISTENERS;
    }
}
