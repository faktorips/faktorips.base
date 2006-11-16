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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.faktorips.devtools.core.IpsStatus;

/**
 * An <code>IRunnableWithProgress</code> that adapts and  <code>IWorkspaceRunnable</code>
 * so that is can be executed inside <code>IRunnableContext</code>. <code>OperationCanceledException</code> 
 * thrown by the adapted runnable are caught and re-thrown as a <code>InterruptedException</code>.
 * 
 * @author Joerg Ortmann
 */
public class WorkbenchRunnableAdapter implements IRunnableWithProgress {

    private IWorkspaceRunnable fWorkspaceRunnable;
    private ISchedulingRule fRule;
    
    /**
     * Runs a workspace runnable with the workspace lock.
     */
    public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable) {
        this(runnable, ResourcesPlugin.getWorkspace().getRoot());
    }
    
    /**
     * Runs a workspace runnable with the given lock or <code>null</code> to run with no lock at all.
     */
    public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable, ISchedulingRule rule) {
        fWorkspaceRunnable= runnable;
        fRule= rule;
    }    
    
    public ISchedulingRule getSchedulingRule() {
        return fRule;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            JavaCore.run(fWorkspaceRunnable, fRule, monitor);
        } catch (OperationCanceledException e) {
            throw new InterruptedException(e.getMessage());
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        }
    }

    public void runAsUserJob(String name, final Object jobFamiliy) {
        Job buildJob = new Job(name){ 
            /**
             * {@inheritDoc}
             */
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    WorkbenchRunnableAdapter.this.run(monitor);
                } catch (InvocationTargetException e) {
                    Throwable cause= e.getCause();
                    if (cause instanceof CoreException) {
                        return ((CoreException) cause).getStatus();
                    } else {
                        return new IpsStatus(e);
                    }
                } catch (InterruptedException e) {
                    return Status.CANCEL_STATUS;
                } finally {
                    monitor.done();
                }
                return Status.OK_STATUS;
            }
            public boolean belongsTo(Object family) {
                return jobFamiliy == family;
            }
        };
        buildJob.setRule(fRule);
        buildJob.setUser(true); 
        buildJob.schedule();
    }
}
