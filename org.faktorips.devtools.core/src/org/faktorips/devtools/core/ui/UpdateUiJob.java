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
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * @author Joerg Ortmann
 */
public class UpdateUiJob {
    private int refreshInterval = 300;
    
    private HashMap updateQueue = new HashMap(20);
    
    boolean queueDrainRequestOutstanding = false;

    private volatile long lastContentChange = 0;

    private Runnable command;
    
    private Display display;
    
    public UpdateUiJob(Display display, Runnable command) {
        ArgumentCheck.notNull(display, command);
        this.display = display;
        this.command = command;
    }

    public UpdateUiJob(Display display, Runnable command, int refreshInterval) {
        this(display, command);
        ArgumentCheck.isTrue(refreshInterval > 0);
        this.refreshInterval = refreshInterval;
    }
    
    /*
     * UIJob to refresh the content of the tree.
     */
    private class UpdateUIJobInternal extends UIJob {
        private boolean fRunning = true;

        public UpdateUIJobInternal(String name) {
            super(name);
            setSystem(true);
        }

        public IStatus runInUIThread(IProgressMonitor monitor) {
            Object object = null;
            synchronized (updateQueue) {
                if (updateQueue.isEmpty() || display.isDisposed()) {
                    queueDrainRequestOutstanding = false;
                    stop();
                }
                if (queueDrainRequestOutstanding) {
                    if ((System.currentTimeMillis() - lastContentChange) > refreshInterval) {
                        object = (Object)updateQueue.values().iterator().next();
                        if (object != updateQueue.remove(object)) {
                            // ignore entry which was not found in the update queue
                            object = null;
                        }
                    }
                }
            }
            if (object != null && command != null) {
                command.run();
            }
            schedule(refreshInterval / 2);
            return Status.OK_STATUS;
        }

        public void stop() {
            fRunning = false;
        }

        public boolean shouldSchedule() {
            return fRunning;
        }
    }
    public void update(Object element) {
        synchronized (updateQueue) {
            lastContentChange = System.currentTimeMillis();
            updateQueue.put(element, element);
            if (!queueDrainRequestOutstanding) {
                queueDrainRequestOutstanding = true;
                if (!display.isDisposed()) {
                    UpdateUIJobInternal fUpdateJob = new UpdateUIJobInternal("UI Update Job"); //$NON-NLS-1$
                    fUpdateJob.schedule(refreshInterval);
                }
            }
        }
    }
}
