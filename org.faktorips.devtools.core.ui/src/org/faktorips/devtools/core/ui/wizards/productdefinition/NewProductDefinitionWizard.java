/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;
import org.faktorips.devtools.core.ui.wizards.productcmpt.Messages;

public abstract class NewProductDefinitionWizard extends Wizard implements INewWizard {

    private final NewProductDefinitionPMO pmo;

    public NewProductDefinitionWizard(NewProductDefinitionPMO pmo) {
        super();
        this.pmo = pmo;

        IDialogSettings settings = IpsUIPlugin.getDefault().getDialogSettings().getSection(getDialogId());
        if (settings == null) {
            settings = IpsUIPlugin.getDefault().getDialogSettings().addNewSection(getDialogId());
        }
        setDialogSettings(settings);

        loadDialogSettings(getDialogSettings());
    }

    /**
     * Getting the dialog ID. The id should never change and identifies the wizard. It is used to
     * load the dialog settings.
     * 
     * @return the id string of this dialog
     */
    protected abstract String getDialogId();

    protected NewProductDefinitionPMO getPmo() {
        return pmo;
    }

    @Override
    public abstract void addPages();

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        Object element = selection.getFirstElement();
        if (element instanceof IAdaptable) {
            IAdaptable adaptableObject = (IAdaptable)element;
            try {
                IIpsElement ipsElement = (IIpsElement)adaptableObject.getAdapter(IIpsElement.class);
                if (ipsElement instanceof IIpsPackageFragmentRoot) {
                    IIpsPackageFragmentRoot ipsPackageRoot = (IIpsPackageFragmentRoot)ipsElement;
                    initDefaults(ipsPackageRoot.getDefaultIpsPackageFragment(), null);
                } else if (ipsElement instanceof IIpsPackageFragment) {
                    IIpsPackageFragment packageFragment = (IIpsPackageFragment)ipsElement;
                    initDefaults(packageFragment, null);
                } else if (ipsElement instanceof IIpsSrcFile) {
                    IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
                    IIpsObject ipsObject = ((IIpsSrcFile)ipsElement).getIpsObject();
                    initDefaults(ipsSrcFile.getIpsPackageFragment(), ipsObject);
                } else {
                    IResource resource = (IResource)adaptableObject.getAdapter(IResource.class);
                    if (resource != null) {
                        IProject project = resource.getProject();
                        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project);
                        IIpsPackageFragmentRoot ipsPackageFragmentRoot = ipsProject.getSourceIpsPackageFragmentRoots()[0];
                        initDefaults(ipsPackageFragmentRoot.getDefaultIpsPackageFragment(), null);
                    }
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    /**
     * Initialize the defaults get from selection. The selectedPackage may never be null but may be
     * the default package of the default package fragment root. The selectedIpsObject may be null.
     * 
     * @param selectedPackage The package of the selected context or default package.
     * @param selectedIpsObject the selected {@link IIpsObject}, may be null.
     */
    protected abstract void initDefaults(IIpsPackageFragment selectedPackage, IIpsObject selectedIpsObject);

    @Override
    public boolean performFinish() {
        IWorkspaceRunnable op = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
                monitor.beginTask(Messages.NewProductCmptWizard_title, 4);
                IIpsSrcFile ipsSrcFile = createIpsSrcFile(monitor);
                finishIpsSrcFile(ipsSrcFile, monitor);
                ipsSrcFile.save(true, new SubProgressMonitor(monitor, 1));
                postProcess(ipsSrcFile, monitor);
                monitor.done();
            }

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
        afterFinishPerformed();
        return true;
    }

    /**
     * Create a new {@link IIpsSrcFile} using the information given from the user. This method
     * returns the newly created file.
     * 
     * @param monitor A progress monitor to show your progress to the user
     * @return A new {@link IIpsSrcFile} file
     * 
     * @throws CoreException In case of exceptions while creating the new file
     */
    protected IIpsSrcFile createIpsSrcFile(IProgressMonitor monitor) throws CoreException {
        IIpsSrcFile ipsSrcFile = pmo.getIpsPackage().createIpsFile(getPmo().getIpsObjectType(), pmo.getName(), true,
                new SubProgressMonitor(monitor, 1));
        return ipsSrcFile;
    }

    /**
     * Finishing the new {@link IIpsSrcFile} means to fill all information given from the user into
     * the new created object. You may copy the content from an old object or something like that.
     * You do not need to save the file after change anything.
     * 
     * @param ipsSrcFile The {@link IIpsSrcFile} you want to manipulate
     * @param monitor A progress monitor to show your progress
     * 
     * @throws CoreException A {@link CoreException} thrown in case of exceptions
     */
    protected abstract void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) throws CoreException;

    /**
     * This method may put the {@link IIpsSrcFile} in context to other objects. It is called after
     * the {@link IIpsSrcFile} is created and saved successfully.
     * 
     * @param ipsSrcFile The newly created {@link IIpsSrcFile}
     * @param monitor The progress monitor to show your progress
     */
    protected abstract void postProcess(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor);

    /**
     * This method is called after the wizard has been finished successfully. You may open the
     * editor of the created object or you want to save some dialog settings here.
     */
    protected void afterFinishPerformed() {
        if (getPmo().isOpenEditor()) {
            IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(getPmo().getEffectiveDate());
            IIpsSrcFile srcFile = getPmo().getIpsPackage().getIpsSrcFile(
                    getPmo().getIpsObjectType().getFileName(getPmo().getName()));
            IpsUIPlugin.getDefault().openEditor(srcFile);
        }
        safeDialogSettings(getDialogSettings());
    }

    /**
     * Loading some values from wizard settings.
     * <p>
     * May be extended by client.
     * 
     * @param settings the dialog settings for this dialog
     */
    protected void loadDialogSettings(IDialogSettings settings) {
        boolean openEditor = settings.getBoolean(NewProductDefinitionPMO.PROPERTY_OPEN_EDITOR);
        getPmo().setOpenEditor(openEditor);
    }

    /**
     * Safe settings for the dialog to get them loaded when next opening this wizard.
     * <p>
     * May be extended by client.
     * 
     * @param settings the settings you should safe your options to
     */
    protected void safeDialogSettings(IDialogSettings settings) {
        settings.put(NewProductDefinitionPMO.PROPERTY_OPEN_EDITOR, getPmo().isOpenEditor());
    }
}