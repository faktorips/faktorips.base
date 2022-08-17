/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.plugin.IpsStatus;

/**
 * Base class for wizards to create a new IPS object.
 */
public abstract class NewIpsObjectWizard extends Wizard implements INewIpsObjectWizard, IPageChangedListener {

    private IStructuredSelection selection;

    // first page
    private AbstractIpsObjectNewWizardPage objectPage;

    public NewIpsObjectWizard() {
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/IpsElementWizard.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(false);
    }

    @Override
    public final void addPages() {
        try {
            objectPage = createFirstPage(selection);
            addPage(objectPage);
            IWizardPage[] additionalPages = createAdditionalPages(selection);
            for (int i = 0; additionalPages != null && i < additionalPages.length; i++) {
                addPage(additionalPages[i]);
            }
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        // CSON: IllegalCatch
    }

    /**
     * In addition the default behavior of the super class implementation it calls the
     * {@link AbstractIpsObjectNewWizardPage#finishWhenThisPageIsComplete()
     * finishWhenThisPageIsComplete()} method on IpsObjectPages. When the
     * finishWhenThisPageIsComplete() method of one of the pages returns {@code true} this method
     * returns {@code true}.
     */
    @Override
    public boolean canFinish() {
        // Default implementation is to check if all pages are complete.
        IWizardPage[] pages = getPages();
        for (IWizardPage page : pages) {
            if (!page.isPageComplete()) {
                return false;
            }
            if (page instanceof AbstractIpsObjectNewWizardPage) {
                if (((AbstractIpsObjectNewWizardPage)page).finishWhenThisPageIsComplete()) {
                    return true;
                }
            }
        }
        return true;
    }

    protected abstract AbstractIpsObjectNewWizardPage createFirstPage(IStructuredSelection selection) throws Exception;

    /**
     * To create additional pages for this wizard this method need to be overridden and the
     * additional pages are to return by it.
     * 
     * @param selection the current selection within the workbench if any
     * @return the new additional wizard pages or <code>null</code> if no additional page exists
     * @throws Exception exceptions that are thrown by this method will be logged and shown to the
     *             user
     */
    protected IWizardPage[] createAdditionalPages(IStructuredSelection selection) throws Exception {
        return null;
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return objectPage.getIpsObjectType();
    }

    @Override
    public final boolean performFinish() {
        final IIpsPackageFragment pack = objectPage.getIpsPackageFragment();
        ICoreRunnable op = monitor -> {
            IWizardPage[] pages = getPages();
            if (pages.length > 1) {
                monitor.beginTask(Messages.NewIpsObjectWizard_creatingObjects, pages.length * 4);
            } else {
                monitor.beginTask(Messages.NewIpsObjectWizard_creatingObject, 4);
            }
            for (IWizardPage page : pages) {
                if (page instanceof AbstractIpsObjectNewWizardPage) {
                    AbstractIpsObjectNewWizardPage newWizardPage = (AbstractIpsObjectNewWizardPage)page;
                    if (newWizardPage.canCreateIpsSrcFile()) {
                        createIpsObject(newWizardPage, monitor);
                    }
                }
            }
            monitor.done();
        };
        try {
            ISchedulingRule rule = null;
            Job job = Job.getJobManager().currentJob();
            if (job != null) {
                rule = job.getRule();
            }
            IRunnableWithProgress runnable = null;
            if (rule != null) {
                runnable = new WorkbenchRunnableAdapter(op, rule);
            } else {
                runnable = new WorkbenchRunnableAdapter(op, ResourcesPlugin.getWorkspace().getRoot());
            }
            getContainer().run(false, true, runnable);
        } catch (InvocationTargetException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        IIpsSrcFile srcFile = pack.getIpsSrcFile(getIpsObjectType().getFileName(objectPage.getIpsObjectName()));
        IpsUIPlugin.getDefault().openEditor(srcFile);
        return true;
    }

    private void createIpsObject(AbstractIpsObjectNewWizardPage page, IProgressMonitor monitor) {
        IIpsSrcFile srcFile = page.createIpsSrcFile(SubMonitor.convert(monitor, 2));
        if (srcFile == null) {
            IpsPlugin.logAndShowErrorDialog(new IpsStatus(
                    Messages.NewIpsObjectWizard_error_unableToCreateIpsSrcFile));
        } else {
            Set<IIpsObject> modifiedIpsObjects = new HashSet<>(0);
            page.finishIpsObjects(srcFile.getIpsObject(), modifiedIpsObjects);
            srcFile.save(SubMonitor.convert(monitor, 1));
            for (IIpsObject modifiedIpsObject : modifiedIpsObjects) {
                modifiedIpsObject.getIpsSrcFile().save(SubMonitor.convert(monitor, 1));
            }
        }
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    /**
     * This wizard registers itself as an {@link IPageChangedListener} on the
     * {@link IWizardContainer} if it implements the {@link IPageChangeProvider} interface. Hence
     * the IpsObjectsPages will be informed via the
     * {@link AbstractIpsObjectNewWizardPage#pageEntered() pageEntered} method when the page is
     * about to be entered (will be shown to the user).
     */
    @Override
    public void pageChanged(PageChangedEvent event) {
        IWizardPage page = (IWizardPage)event.getSelectedPage();
        setWindowTitle(page.getTitle());
        getContainer().updateWindowTitle();
        if (page instanceof AbstractIpsObjectNewWizardPage) {
            try {
                ((AbstractIpsObjectNewWizardPage)page).pageEntered();
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                IpsPlugin.log(e);
            } // CSON: IllegalCatch
        }
    }

    /**
     * Overrides the super class method and registers this wizard as an {@link IPageChangedListener}
     * on the provided {@link IWizardContainer} if it implements the {@link IPageChangeProvider}
     * interface.
     */
    @Override
    public void setContainer(IWizardContainer wizardContainer) {
        super.setContainer(wizardContainer);
        if (wizardContainer instanceof IPageChangeProvider) {
            // in case this listener has already been added remove it first and then add it again
            // this might happen if the setContainer-Method is called several times during the life
            // cycle of this wizard
            ((IPageChangeProvider)wizardContainer).removePageChangedListener(this);
            ((IPageChangeProvider)wizardContainer).addPageChangedListener(this);
        }
    }

}
