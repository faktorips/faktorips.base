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
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Content provider with the ability to collect data in an asynchrony job. The collection job is
 * called in method {@link #inputChanged(Viewer, Object, Object)}. Until the job have finished the
 * method {@link #getElements(Object)} return the objects received by get
 * {@link #getWaitingElements()}. Per default this is only the String {@link #getWaitingLabel()}. If
 * you do not override {@link #getWaitingElements()} you have to make sure that your label provider
 * and any selection handler are able to handle the string element. After the job have finished the
 * viewer is refreshed and all registered listeners are notified.
 * <p>
 * Note that do not reference any content of your context class because it could change during
 * operation. To be sure either implement your deferred content provider in a separate class or in a
 * private static inner class.
 * 
 * @author Cornelius Dirmeier
 */
public abstract class DeferredStructuredContentProvider extends Observable implements IStructuredContentProvider {

    private static final Object[] EMPTY_ARRAY = new Object[0];

    /** the elements collected by the job */
    private volatile Object[] elements;

    private Job collector;

    /**
     * A label showing the user that a job is collecting data. Normally with '...' at the end. e.g.
     * "Collecting data..."
     * 
     * @return a string showing that the user have to wait
     */
    protected abstract String getWaitingLabel();

    /**
     * Get the elements returned by this content provider while the job collecting the content. Per
     * default this is only one String object received from {@link #getWaitingLabel()}
     * 
     * @return The objects shown while job is active
     */
    protected Object[] getWaitingElements() {
        return new Object[] { getWaitingLabel() };
    }

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

    @Override
    public final Object[] getElements(Object inputElement) {
        if (collector.getState() != Job.NONE) {
            return getWaitingElements();
        } else {
            if (elements == null) {
                return EMPTY_ARRAY;
            } else {
                return elements;
            }
        }
    }

    @Override
    public final void inputChanged(final Viewer viewer, Object oldInput, final Object newInput) {
        if (newInput == null || viewer == null || viewer.getControl().isDisposed()) {
            elements = new Object[0];
            return;
        }
        if (collector != null) {
            collector.cancel();
        }
        collector = new CollectorJob(newInput);
        collector.addJobChangeListener(new JobDoneAdapter(viewer));
        collector.schedule();
    }

    public void addCollectorFinishedListener(ICollectorFinishedListener listener) {
        addObserver(listener);
    }

    public void removeCollectorFinishedListener(ICollectorFinishedListener listener) {
        deleteObserver(listener);
    }

    private class CollectorJob extends Job {

        private final Object inputElement;

        public CollectorJob(Object inputElement) {
            super(getWaitingLabel());
            this.inputElement = inputElement;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                monitor.beginTask(getWaitingLabel(), 1);
                SubProgressMonitor collectMonitor = new SubProgressMonitor(monitor, 1);
                Object[] result = collectElements(inputElement, collectMonitor);
                // only set the result if this is still the active job!
                if (this == collector) {
                    elements = result;
                }
            } finally {
                monitor.done();
            }
            return new Status(IStatus.OK, IpsUIPlugin.PLUGIN_ID, null);
        }
    }

    private class JobDoneAdapter extends JobChangeAdapter {

        private final Viewer viewerToRefresh;

        public JobDoneAdapter(Viewer viewerToRefresh) {
            this.viewerToRefresh = viewerToRefresh;
        }

        @Override
        public void done(IJobChangeEvent event) {
            if (viewerToRefresh != null && !viewerToRefresh.getControl().isDisposed()) {
                viewerToRefresh.getControl().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        viewerToRefresh.refresh();
                        setChanged();
                        notifyObservers();
                    }
                });
            }
        }
    }

    @Override
    public void dispose() {
        elements = null;
        deleteObservers();
        collector.cancel();
        collector = null;
    }

}
