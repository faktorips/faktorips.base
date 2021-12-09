/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Property page for configuring the IPS object path.
 * <p>
 * This class is derived from JDT's BuildPathsPropertyPage.
 * 
 * @author Roman Grutza
 */
public class IpsObjectPathPropertyPage extends PropertyPage {

    private static final String PAGE_SETTINGS = "IpsObjectPathPropertyPage"; //$NON-NLS-1$
    private static final String INDEX = "pageIndex"; //$NON-NLS-1$

    private IpsObjectPathContainer objectPathsContainer;

    @Override
    protected Control createContents(Composite parent) {
        // ensure the page has no special buttons
        noDefaultAndApplyButton();

        IIpsProject ipsProject = getIpsProject();
        if (ipsProject == null) {
            return null;
        }

        Control result;
        if (!ipsProject.getProject().isOpen()) {
            result = createForClosedProject(parent);
        } else {
            try {
                result = createForIpsProject(parent, ipsProject);
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
                return null;
            }
        }
        Dialog.applyDialogFont(result);
        return result;
    }

    private Control createForClosedProject(Composite parent) {
        Label label = new Label(parent, SWT.LEFT);
        label.setText(
                org.faktorips.devtools.core.ui.preferencepages.Messages.IpsObjectPathsPropertyPage_closed_project_message);
        objectPathsContainer = null;
        setValid(true);
        return label;
    }

    private Control createForIpsProject(Composite parent, IIpsProject ipsProject) throws CoreRuntimeException {
        objectPathsContainer = new IpsObjectPathContainer(getSettings().getInt(INDEX));
        objectPathsContainer.init(ipsProject);
        return objectPathsContainer.createControl(parent);
    }

    /**
     * Determine IPS project instance for which the property page has to be created
     */
    private IIpsProject getIpsProject() {
        IProject project = getProject();
        IIpsProject ipsProject = null;

        if (project != null) {
            ipsProject = IIpsModel.get().getIpsProject(project);
        }
        return ipsProject;
    }

    private IProject getProject() {
        IAdaptable adaptable = getElement();
        IProject project = null;

        if (adaptable instanceof IProject) {
            project = (IProject)adaptable;
        } else {
            IJavaElement elem = adaptable.getAdapter(IJavaElement.class);
            if (elem instanceof IJavaProject) {
                project = ((IJavaProject)elem).getProject();
            }
        }
        return project;
    }

    private IDialogSettings getSettings() {
        IDialogSettings dialogSettings = IpsPlugin.getDefault().getDialogSettings();
        IDialogSettings pageSettings = dialogSettings.getSection(PAGE_SETTINGS);
        if (pageSettings == null) {
            pageSettings = dialogSettings.addNewSection(PAGE_SETTINGS);
            pageSettings.put(INDEX, 0);
        }
        return pageSettings;
    }

    @Override
    public void setVisible(boolean visible) {
        if (objectPathsContainer != null) {
            if (!visible) {
                if (objectPathsContainer.hasChangesInDialog()) {
                    String title = Messages.IpsObjectPathPropertyPage_changes_in_dialog_title;
                    String message = Messages.IpsObjectPathPropertyPage_apply_discard_applyLater_message;
                    String[] buttonLabels = new String[] { Messages.IpsObjectPathPropertyPage_apply_button,
                            Messages.IpsObjectPathPropertyPage_discard_button,
                            Messages.IpsObjectPathPropertyPage_apply_later_button };

                    MessageDialog dialog = new MessageDialog(getShell(), title, null, message, MessageDialog.QUESTION,
                            buttonLabels, 0);
                    int res = dialog.open();
                    if (res == 0) {
                        performOk();
                    } else if (res == 1) {
                        // discard
                        objectPathsContainer.init(getIpsProject());
                    }
                }
            }
        }
        super.setVisible(visible);
    }

    @Override
    public boolean performOk() {
        if (objectPathsContainer.hasChangesInDialog()) {
            objectPathsContainer.saveToIpsProjectFile();
            objectPathsContainer.init(getIpsProject());
        }
        return super.performOk();
    }

    @Override
    public void dispose() {
        objectPathsContainer.dispose();
        super.dispose();
    }

}
