/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;


/**
 * Base class for wizards to create a new ips object.
 */
public abstract class NewIpsObjectWizard extends Wizard implements INewIpsObjectWizard {
    
    private IStructuredSelection selection;
    
    // first page
    private IpsObjectPage objectPage;
    
    private IpsObjectType ipsObjectType;
    
    public NewIpsObjectWizard(IpsObjectType type) {
        ipsObjectType = type;
        setWindowTitle(Messages.NewIpsObjectWizard_title + ipsObjectType.getName());
        this.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/IpsElementWizard.png")); //$NON-NLS-1$
    }
    
    /** 
     * {@inheritDoc}
     */
    public final void addPages() {
        try {
            objectPage = createFirstPage(selection); 
            addPage(objectPage);
            createAdditionalPages();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    protected abstract IpsObjectPage createFirstPage(IStructuredSelection selection) throws Exception;
    
    protected abstract void createAdditionalPages() throws Exception;
    
    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return ipsObjectType;
    }
    
    /** 
     * {@inheritDoc}
     */
    public final boolean performFinish() {
        final IIpsPackageFragment pack = objectPage.getIpsPackageFragment();
        IWorkspaceRunnable op= new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
                monitor.beginTask("Creating object", 2); //$NON-NLS-1$
                IIpsSrcFile srcFile = pack.createIpsFile(ipsObjectType, objectPage.getIpsObjectName(), true, null);
                monitor.worked(1);
                finishIpsObject(srcFile.getIpsObject());
                srcFile.save(true, null);
                monitor.worked(1);
                monitor.done();
            }
        };
        try {
            ISchedulingRule rule= null;
            Job job= Platform.getJobManager().currentJob();
            if (job != null)
                rule= job.getRule();
            IRunnableWithProgress runnable= null;
            if (rule != null) {
                runnable= new WorkbenchRunnableAdapter(op, rule);
            } else {
                runnable= new WorkbenchRunnableAdapter(op, ResourcesPlugin.getWorkspace().getRoot());
            }
            getContainer().run(false, true, runnable);
        } catch (InvocationTargetException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        } catch  (InterruptedException e) {
            return false;
        }
        IIpsSrcFile srcFile = pack.getIpsSrcFile(ipsObjectType.getFileName(objectPage.getIpsObjectName()));
        IpsPlugin.getDefault().openEditor(srcFile);
        return true;
    }
    
    protected abstract void finishIpsObject(IIpsObject ipsObject) throws CoreException;

    /** 
     * {@inheritDoc}
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

	
}