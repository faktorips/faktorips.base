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

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.util.ArgumentCheck;

/**
 * System job class which notifies several listener about change events in asynchronous manner.<br>
 * The listener will be informed after a specified delay.<br>
 * All change events are queued inside a list, this queue will be processed in serialized order,
 * after each processed event in the queue, the job will be schedule for a specified delay.<br>
 * The job stops if the queue is empty and the job restarts again if a new change event occurres.<br>
 * Each event is identified by an identifier object, therfore if the same identifier object post
 * more than one change events in a time which is less than the schedule time, only the latest will
 * be processed.<br>
 * <br>
 * Special behaviour:
 * <ol>
 * <li> If the same identifier post a further change event in a specified time then the
 * corresponding listeners are not informed after the schedule delay, instead of the schedule time
 * will be retriggered again without notifing the listeners.
 * <li>If an identifier, which was not in the queue before, posts an change event then the handling
 * of the retriggering the schedule time is skipped, instead of the change event will be fired
 * directly after the schedule time.
 * </ol>
 * 
 * @author Joerg Ortmann
 */
public class NotifyChangeListenerQueueManager {
    // display the ui job will be related to
    private Display display;
    
    // default refresh (schedule) intervall
    private int refreshInterval = 200;
    
    // the map which implements the queue
    private HashMap updateQueue = new HashMap(20);
    
    // indicates if there are outstanding requests in the queue
    // means the queue is active or not
    boolean queueDrainRequestOutstanding = false;

    // time of last event
    private volatile long lastEventTime = 0;

    // indicates that the next event skips the delay retrigger
    private boolean forceEvent = false;
    
    // stores the last event initiator
    private Object lastInitiator;
    
    /*
     * Internal UIJob to perform the notification of listener in a serialized queue.
     */
    private class UpdateUIJobInternal extends UIJob {
        // indicates if the job is running
        private boolean running = true;

        public UpdateUIJobInternal(String name) {
            super(name);
            setSystem(true);
            logTrace("NotifyChangeListenerQueueManager$UpdateUIJobInternal.runInUIThread(): Start scheduling");  //$NON-NLS-1$           
        }

        public IStatus runInUIThread(IProgressMonitor monitor) {
            NotifyChangeListenerEvent event = null;
            synchronized (updateQueue) {
                if (updateQueue.isEmpty() || display.isDisposed()) {
                    // stop the ui job, because no more events in queue or display is disposed
                    queueDrainRequestOutstanding = false;
                    stop();
                }
                if (queueDrainRequestOutstanding) {
                    logTrace("NotifyChangeListenerQueueManager$UpdateUIJobInternal.runInUIThread(): Proccess outstanding queued request."); //$NON-NLS-1$
                    
                    // check if it is time to process the next event in the queue or schedule again
                    if (((System.currentTimeMillis() - lastEventTime) > refreshInterval) || forceEvent) {
                        // retrieve and remove the next event element in the queue
                        event = (NotifyChangeListenerEvent)updateQueue.values().iterator().next();
                        if (event != updateQueue.remove(event.initiator)) {
                            // ignore entry which was not found in the update queue
                            // this should never happen
                            event = null;
                        }
                    } else {
                        logTrace("NotifyChangeListenerQueueManager$UpdateUIJobInternal.runInUIThread(): Don't notify listeners because request was retriggered."); //$NON-NLS-1$
                    }
                }
            }
            if (event != null) {
                logTrace("NotifyChangeListenerQueueManager$UpdateUIJobInternal.runInUIThread(): Start notifying of change event listeners: " + event.initiator); //$NON-NLS-1$
                // there is an event to proceed, start notification of the corresponding
                // listeners stored in this event
                for (int i = 0; i < event.listeners.length; i++) {
                    event.listeners[i].valueChanged(event.fieldValueChangedEvent);
                }
                logTrace("NotifyChangeListenerQueueManager$UpdateUIJobInternal.runInUIThread(): Finished notifying of change event listeners: " + event.initiator); //$NON-NLS-1$
            }
            // schedule again, the scheduling time is shorter, because within the working method we
            // check the time manually to decide if it is time to process the next outstanding event
            int nextScheduleTime = refreshInterval / 2;
            logTrace("NotifyChangeListenerQueueManager$UpdateUIJobInternal.runInUIThread(): Schedule: " + nextScheduleTime + " ms"); //$NON-NLS-1$
            schedule(nextScheduleTime);
            return Status.OK_STATUS;
        }

        public void stop() {
            // indicate the internal job to stop
            running = false;
            logTrace("NotifyChangeListenerQueueManager$UpdateUIJobInternal.runInUIThread(): Stop scheduling"); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         */
        public boolean shouldSchedule() {
            return running;
        }
    }
    
    /**
     * New manager with a default refresh delay time.
     */
    public NotifyChangeListenerQueueManager(){
        display = Display.getCurrent();
    }
    
    /**
     * New manager with a given refresh delay time.
     */
    public NotifyChangeListenerQueueManager(int refreshInterval){
        this();
        ArgumentCheck.isTrue(refreshInterval > 0);
        this.refreshInterval = refreshInterval;
    }
    
    /**
     * Sets the new refresh interval, time to schedule the queue in ms. The new time will be updated
     * when the queue is empty and a new change event is post to the manager.
     */
    public synchronized void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    } 
    
    /**
     * Update the queue by inserting or updating the given event in the queue. The event will be
     * processed after a specified delay.
     */
    public void update(NotifyChangeListenerEvent event) {
        synchronized (updateQueue) {
            if (event.initiator != lastInitiator){
                // event is from new initiator
                // skip retriggering of queue, fire the change event
                // directly after the schedule time
                forceEvent = true;
                lastInitiator = event.initiator;
            } else {
                forceEvent = false;
            }
            
            // store time of event, to evaluate the time of the next notification
            lastEventTime = System.currentTimeMillis();
            updateQueue.put(event.initiator, event);
            if (!queueDrainRequestOutstanding) {
                // the queue is deactive (no queue job is running)
                // start the queue by instantiating a new ui system job
                if (!display.isDisposed()) {
                    queueDrainRequestOutstanding = true;
                    UpdateUIJobInternal fUpdateJob = new UpdateUIJobInternal("UI Update Job"); //$NON-NLS-1$
                    // start job after a specified delay
                    fUpdateJob.schedule(refreshInterval);
                }
            }
        }
    }
    
    /*
     * Trace the given log message if <code>IpsModel.TRACE_MODEL_CHANGE_LISTENERS</code> is <code>true</code>
     */
    private void logTrace(String message) {
        if (IpsModel.TRACE_MODEL_CHANGE_LISTENERS) {
            System.out.println(message);
        }
    }
}
