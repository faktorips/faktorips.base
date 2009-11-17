/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal;

import java.util.Observable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Content provider with the ability to collect data in an asynchrony job. The collection job is
 * called in method {@link #inputChanged(Viewer, Object, Object)}. Until the job have finished the
 * method {@link #getElements(Object)} only returns a single object containing the
 * {@link #getWaitingLabel()}. After the job have finished the view is refreshed.
 * 
 * @author Cornelius Dirmeier
 */
public abstract class DeferredStructuredContentProvider extends Observable implements IStructuredContentProvider {

    private static final Object[] EMPTY_ARRAY = new Object[0];

    // the elements collected by the job
    private Object[] elements;

    private Job collector;

    /**
     * A label showing the user that a job is collecting data. Normally with '...' at the end. e.g.
     * "Collecting data..."
     * 
     * @return a string showing that the user have to wait
     */
    protected abstract String getWaitingLabel();

    /**
     * This method is called by the job to collect the elements asynchrony. Make sure to use the
     * progress monitor to show your actual state of work. Also have a look for the cancel state of
     * the progress monitor. You have to stop collecting data if monitor is canceled.
     * 
     * @param inputElement The input element set to the view
     * @param monitor the progress monitor to visualize your state of work
     * @return the collected objects
     */
    protected abstract Object[] collectElements(Object inputElement, IProgressMonitor monitor);

    public final Object[] getElements(Object inputElement) {
        if (collector.getState() != Job.NONE) {
            return new Object[] { getWaitingLabel() };
        } else {
            if (elements == null) {
                return EMPTY_ARRAY;
            } else {
                return elements;
            }
        }
    }

    public final void inputChanged(final Viewer viewer, Object oldInput, final Object newInput) {
        if (newInput == null || viewer == null || viewer.getControl().isDisposed()) {
            elements = new Object[0];
            return;
        }
        if (collector != null) {
            collector.cancel();
        }
        collector = new Job(getWaitingLabel()) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                elements = collectElements(newInput, monitor);
                monitor.done();
                return new Status(IStatus.OK, IpsUIPlugin.PLUGIN_ID, null);
            }
        };
        collector.addJobChangeListener(new IJobChangeListener() {

            public void sleeping(IJobChangeEvent event) {
            }

            public void scheduled(IJobChangeEvent event) {
            }

            public void running(IJobChangeEvent event) {
            }

            public void done(IJobChangeEvent event) {
                if (viewer != null && !viewer.getControl().isDisposed()) {
                    viewer.getControl().getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            viewer.refresh();
                            setChanged();
                            notifyObservers();
                        }
                    });
                }
            }

            public void awake(IJobChangeEvent event) {
            }

            public void aboutToRun(IJobChangeEvent event) {
            }
        });
        collector.schedule();
    }

    public void addCollectorFinishedListener(ICollectorFinishedListener listener) {
        addObserver(listener);
    }

    public void removeCollectorFinishedListener(ICollectorFinishedListener listener) {
        deleteObserver(listener);
    }

}
