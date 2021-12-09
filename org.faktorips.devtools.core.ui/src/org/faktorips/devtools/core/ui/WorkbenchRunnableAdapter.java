/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.faktorips.devtools.model.plugin.IpsStatus;

/**
 * An <code>IRunnableWithProgress</code> that adapts an <code>ICoreRunnable</code> so that is
 * can be executed inside <code>IRunnableContext</code>. <code>OperationCanceledException</code>
 * thrown by the adapted runnable are caught and re-thrown as a <code>InterruptedException</code>.
 * 
 * Copied from the class with the same name from the internal Eclipse packages.
 * 
 * @author Joerg Ortmann
 */
public class WorkbenchRunnableAdapter implements IRunnableWithProgress {

    private ICoreRunnable workspaceRunnable;
    private ISchedulingRule rule;

    /**
     * Runs a workspace runnable with the workspace lock.
     */
    public WorkbenchRunnableAdapter(ICoreRunnable runnable) {
        this(runnable, ResourcesPlugin.getWorkspace().getRoot());
    }

    /**
     * Runs a workspace runnable with the given lock or <code>null</code> to run with no lock at
     * all.
     */
    public WorkbenchRunnableAdapter(ICoreRunnable runnable, ISchedulingRule rule) {
        workspaceRunnable = runnable;
        this.rule = rule;
    }

    public ISchedulingRule getSchedulingRule() {
        return rule;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            JavaCore.run(workspaceRunnable, rule, monitor);
        } catch (OperationCanceledException e) {
            throw new InterruptedException(e.getMessage());
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        }
    }

    public void runAsUserJob(String name, final Object jobFamiliy) {
        Job buildJob = new Job(name) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                return runJob(monitor);
            }

            @Override
            public boolean belongsTo(Object family) {
                return jobFamiliy == family;
            }
        };
        buildJob.setRule(rule);
        buildJob.setUser(true);
        buildJob.schedule();
    }

    private IStatus runJob(IProgressMonitor monitor) {
        try {
            WorkbenchRunnableAdapter.this.run(monitor);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CoreException) {
                return ((CoreException)cause).getStatus();
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

}
