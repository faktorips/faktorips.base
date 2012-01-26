/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;

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
        IIpsProject project = IpsPlugin.getDefault().getIpsModel().getIpsProject(getText());
        if (project.exists()) {
            return project;
        }
        return null;
    }

    @Override
    protected void buttonClicked() {
        try {
            ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new WorkbenchLabelProvider());
            IIpsProject[] projects;
            if (isOnlyProductDefinitionProjects()) {
                projects = IpsPlugin.getDefault().getIpsModel().getIpsProductDefinitionProjects();
            } else {
                projects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
            }
            dialog.setElements(projects);
            dialog.setMultipleSelection(false);
            dialog.setMessage(Messages.IpsProjectRefControl_labelDialogMessage);
            dialog.setEmptyListMessage(Messages.IpsProjectRefControl_msgNoProjectsFound);
            dialog.setEmptySelectionMessage(Messages.IpsProjectRefControl_msgNoProjectSelected);
            dialog.setTitle(Messages.IpsProjectRefControl_labelDialogTitle);
            if (dialog.open() == Window.OK) {
                IIpsProject selectedProject = (IIpsProject)dialog.getResult()[0];
                setIpsProject(selectedProject);
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
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
