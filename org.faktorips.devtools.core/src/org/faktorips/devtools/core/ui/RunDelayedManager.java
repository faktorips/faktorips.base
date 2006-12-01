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

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.faktorips.devtools.core.internal.model.IpsModel;

/**
 * System job manager which runs several runnable in asynchronous manner.<br>
 * Each runnable will be processed after a specified delay wich is stored for each runnable.<br>
 * All runnable are queued inside a list, this queue will be processed in specified refresh time,
 * Each runnable is identified by an identifier object, therfore if the same identifier object post
 * more than one runnable in a time which is less than the specified refresh time, only the latest will
 * be processed.<br>
 * <br>
 * Behaviour:
 * <ul>
 * <li> The runnable itselfs contains a delay time, if the delay time is reached the runnable will be processed.
 * <li> If the same identifier post a second runnable in a specified time then the
 * runnable is not processed after the schedule delay, instead the runnable will be delay again (retriggered).
 * <li> The manager itselfs specifies a schedule time (normaly a short time) in which the next to be executed runnable
 * will be evaluated.
 * </ol>
 * 
 * @author Joerg Ortmann
 */
public class RunDelayedManager {
    private static DateFormat DEBUG_FORMAT;
    private static int DEFAULT_SCHEDULE_TIME = 200;
    
    // display the ui job will be related to
    private Display display;

    // indicates if there are outstanding requests in the queue
    // means the queue is active or not
    private boolean queueDrainRequestOutstanding = false;
    
    // the schedule time for the manager
    private int scheduleTime;

    // the queue where all runnable will be stored
    private final Queue queue;
    
    private class Queue{
        private HashMap lastRunnableIssueTimes = new HashMap(20);
        private HashMap queuedRunnable = new HashMap(20);
        
        public void put(IIdentifiableDelayedRunnable runnable){
            queuedRunnable.put(runnable.getId(), runnable);
            lastRunnableIssueTimes.put(runnable.getId(), new Long(System.currentTimeMillis()));
        }

        /**
         * Returns the next to be executed runnable in the queue.<br>
         * If the refresh time of the runnable is reached then this runnable will be returned.
         * Return <code>null</code> if there is no runnable which should be executed jet.
         */
        public IIdentifiableDelayedRunnable getNextRunnable() {
            IIdentifiableDelayedRunnable runnable;
            long lastEventTime = 0;
            // retrieve and remove the next relevant runnable in the queue
            for (Iterator iter = queuedRunnable.values().iterator(); iter.hasNext();) {
                runnable = (IIdentifiableDelayedRunnable)iter.next();
                Long lastEventTimeLong = (Long)lastRunnableIssueTimes.get(runnable.getId());
                if (lastEventTimeLong != null){
                    lastEventTime = lastEventTimeLong.longValue();
                }
                if (((System.currentTimeMillis() - lastEventTime) > runnable.getDelayTime())) {
                    // the runnable delay time is reached return this runnable
                    if (runnable != queuedRunnable.remove(runnable.getId())) {
                        // should never happen
                        logTrace("Warning: couldn't be removed from the queue!", runnable.getId()); //$NON-NLS-1$
                    }
                    return runnable;
                }
            }
            return null;
            
        }

        /**
         * <code>true</code> if there are no more runnable in the queued list.
         */
        public boolean isEmpty() {
            return queuedRunnable.isEmpty();
        }
        
        /**
         * Resets the queue, remove all elements in the queue and cleanup the queue object.
         */
        public void resetQueue(){
            lastRunnableIssueTimes.clear();
            queuedRunnable.clear();
        }
    }

    /*
     * Internal UIJob to perform the notification of listener in a serialized queue.
     */
    private class UpdateUIJobInternal extends UIJob {
        // indicates that the job is running
        private boolean running = true;

        public UpdateUIJobInternal(String name) {
            super(name);
            // system jobs are never visible
            setSystem(true);
        }

        /**
         * {@inheritDoc}
         */
        public IStatus runInUIThread(IProgressMonitor monitor) {
            IIdentifiableDelayedRunnable runnable = null;
            synchronized (queue) {
                if (queue.isEmpty() || display.isDisposed()) {
                    // stop the ui job, because no more events in queue or display is disposed
                    queueDrainRequestOutstanding = false;
                    stop();
                }
                if (queueDrainRequestOutstanding) {
                    // check if it is time to process the next event in the queue or schedule again
                    runnable = queue.getNextRunnable();
                    if (runnable != null){
                        logTrace("Start runnable", runnable.getId()); //$NON-NLS-1$
                        // there is an event to proceed, start notification of the corresponding
                        // listeners stored in this event
                        try {
                            runnable.run();
                        }
                        catch (RuntimeException e) {
                            logTrace("Error durring run: " + e.getMessage(), runnable.getId());
                        }
                        logTrace("Finished runnable", runnable.getId()); //$NON-NLS-1$
                    }
                }
            }
            
            // schedule again, the scheduling time is shorter, because within the working method we
            // check the time manually to decide if it is time to process the next outstanding event
            schedule(scheduleTime);
            return Status.OK_STATUS;
        }

        /*
         * Inform the job manager that the job could be stopped
         */
        private void stop() {
            // indicate the internal job to stop
            running = false;
            logTrace("No more runnable, job will discarded.", null); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         */
        public boolean shouldSchedule() {
            return running;
        }
    }

    /**
     * New manager with a given refresh delay time.
     */
    public RunDelayedManager(int scheduleTime){
        this();
        this.scheduleTime = scheduleTime;
    }
    
    /**
     * New manager with a default refresh delay time.
     */
    public RunDelayedManager(){
        display = Display.getCurrent();
        queue = new Queue();
        scheduleTime = DEFAULT_SCHEDULE_TIME;
    }

    /**
     * Update the queue by inserting or updating the given event in the queue. The event will be
     * processed after a specified delay.
     */
    public void update(IIdentifiableDelayedRunnable runnable) {
        synchronized (queue) {
            if (isDebugOn()){
                logTrace("Update runnable with delay " + runnable.getDelayTime() + " ms", runnable.getId()); //$NON-NLS-1$
            }
            if (!queueDrainRequestOutstanding) {
                // the queue is deactive (no queue job is running)
                // start the queue by instantiating a new ui system job
                if (!display.isDisposed()) {
                    logTrace("Start new job", null); //$NON-NLS-1$
                    queueDrainRequestOutstanding = true;
                    queue.resetQueue();
                    queue.put(runnable);
                    UpdateUIJobInternal fUpdateJob = new UpdateUIJobInternal("QueueJob"); //$NON-NLS-1$
                    // start job after a specified delay
                    fUpdateJob.schedule(runnable.getDelayTime());
                }
            } else {
                queue.put(runnable);
            }
        }
    }

    /*
     * Returns if <code>IpsModel.TRACE_MODEL_CHANGE_LISTENERS</code> is true
     */
    private boolean isDebugOn(){
        return IpsModel.TRACE_MODEL_CHANGE_LISTENERS;
    }
    
    /*
     * Trace the given log message if <code>IpsModel.TRACE_MODEL_CHANGE_LISTENERS</code> is <code>true</code>
     */
    private void logTrace(String message, String id) {
        if (!isDebugOn()) {
            return;
        }
        StringBuffer msgBuf = new StringBuffer(message.length() + 40);
        msgBuf.append("RunInQueueManager "); //$NON-NLS-1$
        if (DEBUG_FORMAT == null){
            DEBUG_FORMAT = new SimpleDateFormat("(HH:mm:ss.SSS): "); //$NON-NLS-1$
        }
        DEBUG_FORMAT.format(new Date(), msgBuf, new FieldPosition(0));
        msgBuf.append(message);
        if (id != null){
            msgBuf.append(" ["); //$NON-NLS-1$
            msgBuf.append(id);
            msgBuf.append(']');
        }
        System.out.println(msgBuf.toString());
    }
}
