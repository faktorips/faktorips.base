/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
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
import org.faktorips.util.message.MessageList;

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
        if (selection != null) {
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
                            IIpsPackageFragmentRoot ipsPackageFragmentRoot = ipsProject
                                    .getSourceIpsPackageFragmentRoots()[0];
                            initDefaults(ipsPackageFragmentRoot.getDefaultIpsPackageFragment(), null);
                        }
                    }
                    return;
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
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

    /**
     * Returns the operation that should be executed by this wizard.
     */
    protected abstract NewProductDefinitionOperation<? extends NewProductDefinitionPMO> getOperation();

    @Override
    public boolean canFinish() {
        MessageList messageList = getPmo().getValidator().validateAllPages();
        return super.canFinish() && !messageList.containsErrorMsg();
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, true, getOperation());
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
     * This method is called after the wizard has been finished successfully. You may open the
     * editor of the created object or you want to save some dialog settings here.
     */
    protected void afterFinishPerformed() {
        // Update default validity date
        IpsUIPlugin.getDefault().setDefaultValidityDate(getPmo().getEffectiveDate());

        if (getPmo().isOpenEditor()) {
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