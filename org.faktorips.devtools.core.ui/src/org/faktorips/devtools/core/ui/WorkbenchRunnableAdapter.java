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
 * An <code>IRunnableWithProgress</code> that adapts an <code>IWorkspaceRunnable</code> so that is
 * can be executed inside <code>IRunnableContext</code>. <code>OperationCanceledException</code>
 * thrown by the adapted runnable are caught and re-thrown as a <code>InterruptedException</code>.
 * 
 * Copied from the class with the same name from the internal Eclipse packages.
 * 
 * @author Joerg Ortmann
 */
public class WorkbenchRunnableAdapter implements IRunnableWithProgress {

    private IWorkspaceRunnable workspaceRunnable;
    private ISchedulingRule rule;

    /**
     * Runs a workspace runnable with the workspace lock.
     */
    public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable) {
        this(runnable, ResourcesPlugin.getWorkspace().getRoot());
    }

    /**
     * Runs a workspace runnable with the given lock or <code>null</code> to run with no lock at
     * all.
     */
    public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable, ISchedulingRule rule) {
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

            @Override
            public boolean belongsTo(Object family) {
                return jobFamiliy == family;
            }
        };
        buildJob.setRule(rule);
        buildJob.setUser(true);
        buildJob.schedule();
    }

}
