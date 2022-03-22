/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A control to edit a reference to a package fragment root containing source code.
 */
public class IpsProjectRefControl extends TextButtonControl {

    private boolean onlyProductDefinitionProjects = false;

    public IpsProjectRefControl(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit, Messages.IpsProjectRefControl_labelBrowse);
    }

    public void setIpsProject(IIpsProject project) {
        if (project == null) {
            setText(""); //$NON-NLS-1$
        } else {
            setText(project.getName());
        }
    }

    public IIpsProject getIpsProject() {
        IIpsProject project = IIpsModel.get().getIpsProject(getText());
        if (project.exists()) {
            return project;
        }
        return null;
    }

    @Override
    protected void buttonClicked() {
        try {
            ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
                    new WorkbenchLabelProvider());
            dialog.setElements(collectIpsProjects());
            dialog.setMultipleSelection(false);
            dialog.setMessage(getDialogMessage());
            dialog.setEmptyListMessage(getDialogMessageEmptyList());
            dialog.setEmptySelectionMessage(getDialogMessageEmptySelection());
            dialog.setTitle(getDialogTitle());
            if (dialog.open() == Window.OK) {
                IIpsProject selectedProject = (IIpsProject)dialog.getResult()[0];
                setIpsProject(selectedProject);
            }
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        // CSON: IllegalCatch
    }

    /**
     * Returns the title that is shown in the title bar of the dialog that allows the user to select
     * projects.
     * <p>
     * <strong>Subclassing:</strong> Subclasses may override as necessary.
     */
    protected String getDialogTitle() {
        return Messages.IpsProjectRefControl_labelDialogTitle;
    }

    /**
     * Returns the message that is shown in the dialog that allows the user to select projects in
     * the situation that no project is selected.
     * <p>
     * <strong>Subclassing:</strong> Subclasses may override as necessary.
     */
    protected String getDialogMessageEmptySelection() {
        return Messages.IpsProjectRefControl_msgNoProjectSelected;
    }

    /**
     * Returns the message that is shown in the dialog that allows the user to select projects in
     * the situation that no projects are available for selection.
     * <p>
     * <strong>Subclassing:</strong> Subclasses may override as necessary.
     */
    protected String getDialogMessageEmptyList() {
        return Messages.IpsProjectRefControl_msgNoProjectsFound;
    }

    /**
     * Returns the message that is shown in the dialog that allows the user to select projects.
     * <p>
     * <strong>Subclassing:</strong> Subclasses may override as necessary.
     */
    protected String getDialogMessage() {
        return Messages.IpsProjectRefControl_labelDialogMessage;
    }

    /**
     * Returns the list of {@link IIpsProject IPS projects} available for selection.
     * <p>
     * <strong>Subclassing:</strong> This implementation returns all {@link IIpsProject IPS
     * projects} if {@link #isOnlyProductDefinitionProjects()} returns {@code false}. On the other
     * hand, if {@link #isOnlyProductDefinitionProjects()} returns {@code true}, only product
     * definition projects are returned.
     * 
     * @throws IpsException if an error occurs while collecting the {@link IIpsProject IPS projects}
     */
    protected IIpsProject[] collectIpsProjects() {
        if (isOnlyProductDefinitionProjects()) {
            return IIpsModel.get().getIpsProductDefinitionProjects();
        } else {
            return IIpsModel.get().getIpsProjects();
        }
    }

    /**
     * @param onlyProductDefinitionProjects The onlyProductDefinitionProjects to set.
     */
    public void setOnlyProductDefinitionProjects(boolean onlyProductDefinitionProjects) {
        this.onlyProductDefinitionProjects = onlyProductDefinitionProjects;
    }

    /**
     * @return Returns the onlyProductDefinitionProjects.
     */
    public boolean isOnlyProductDefinitionProjects() {
        return onlyProductDefinitionProjects;
    }

}
