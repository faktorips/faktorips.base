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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * A control to edit a reference to a package fragment root containing source
 * code.
 */
public class IpsProjectRefControl extends TextButtonControl {
    
    public IpsProjectRefControl(
            Composite parent,
            UIToolkit toolkit) {
        super(parent, toolkit, Messages.IpsProjectRefControl_labelBrowse);
    }
    
    public void setIpsProject(IIpsProject project) {
        if (project==null) {
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
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.TextButtonControl#buttonClicked()
     */ 
    protected void buttonClicked() {
        try {
            ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new ListLabelProvider());
            dialog.setElements(IpsPlugin.getDefault().getIpsModel().getIpsProjects());
            dialog.setMultipleSelection(false);
            dialog.setMessage(Messages.IpsProjectRefControl_labelDialogMessage);
            dialog.setEmptyListMessage(Messages.IpsProjectRefControl_msgNoProjectsFound);
            dialog.setEmptySelectionMessage(Messages.IpsProjectRefControl_msgNoProjectSelected);
            dialog.setTitle(Messages.IpsProjectRefControl_labelDialogTitle);
            if (dialog.open()==Window.OK) {
                IIpsProject selectedProject = (IIpsProject)dialog.getResult()[0];
                setIpsProject(selectedProject);
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    private static class ListLabelProvider extends LabelProvider {
        
        /**
         * {@inheritDoc}
         */
        public String getText(Object element) {
            return element == null ? "" : ((IIpsProject) element).getName(); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         */
        public Image getImage(Object element) {
            return element == null ? null : ((IIpsProject) element).getImage();
        }
        
    }
}
