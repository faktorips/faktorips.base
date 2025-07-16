/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ResizableWizard;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;

public abstract class NewProductDefinitionWizard extends ResizableWizard implements INewWizard {

    private static final int DEFAULT_HEIGHT = 400;
    private static final int DEFAULT_WIDTH = 600;
    private final NewProductDefinitionPMO pmo;

    /**
     * @param pmo The model containing the settings changeable in the wizard.
     */
    public NewProductDefinitionWizard(NewProductDefinitionPMO pmo) {
        // to keep compatible API the dialogId is specified by overriding getDialogId()
        super(null, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.pmo = pmo;

        loadDialogSettings(getDialogSettings());
    }

    /**
     * Getting the dialog ID. The id should never change and identifies the wizard. It is used to
     * load the dialog settings.
     *
     * @return the id string of this dialog
     */
    @Override
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
            if (element instanceof IAdaptable adaptableObject) {
                IIpsElement ipsElement = adaptableObject.getAdapter(IIpsElement.class);
                switch (ipsElement) {
                    case IIpsPackageFragmentRoot ipsPackageRoot -> initDefaults(
                            ipsPackageRoot.getDefaultIpsPackageFragment(), null);
                    case IIpsPackageFragment packageFragment -> initDefaults(packageFragment, null);
                    case IIpsObject ipsObject -> initDefaults(ipsObject.getIpsPackageFragment(), ipsObject);
                    case IIpsSrcFile ipsSrcFile -> initDefaults(ipsSrcFile.getIpsPackageFragment(),
                            ipsSrcFile.getIpsObject());
                    case null, default -> initFallback(adaptableObject);
                }
            }
        }
    }

    private void initFallback(IAdaptable adaptableObject) {
        IResource resource = adaptableObject.getAdapter(IResource.class);
        if (resource != null) {
            IProject project = resource.getProject();
            if (project != null) {
                IIpsProject ipsProject = IIpsModel.get().getIpsProject(Wrappers.wrap(project).as(AProject.class));
                if (ipsProject.exists()) {
                    IIpsPackageFragmentRoot root = ipsProject.getSourceIpsPackageFragmentRoots()[0];
                    initDefaults(root.getDefaultIpsPackageFragment(), null);
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
        setNeedsProgressMonitor(true);
        IIpsModel.get().runAndQueueChangeEvents(monitor -> {
            var operation = getOperation();
            if (operation != null) {
                var container = getContainer();
                if (container != null) {
                    try {
                        container.run(false, true, operation);
                    } catch (InvocationTargetException | InterruptedException e) {
                        throw new CoreException(new IpsStatus(e));
                    }
                } else {
                    Abstractions.getWorkspace().run(m -> {
                        try {
                            operation.execute(m);
                        } catch (InvocationTargetException | InterruptedException e) {
                            throw new CoreException(new IpsStatus(e));
                        }
                    }, monitor);
                }
            }
        }, new NullProgressMonitor());
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
            IIpsSrcFile srcFile = getPmo().getIpsPackage()
                    .getIpsSrcFile(getPmo().getIpsObjectType().getFileName(getPmo().getName()));
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
        String openEditorSetting = settings.get(NewProductDefinitionPMO.PROPERTY_OPEN_EDITOR);
        if (IpsStringUtils.isNotEmpty(openEditorSetting)) {
            boolean openEditor = Boolean.parseBoolean(openEditorSetting);
            getPmo().setOpenEditor(openEditor);
        } else {
            getPmo().setOpenEditor(true);
        }
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
