/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.dialogs.AddIpsNatureDialog;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.ProjectUtil;

/**
 * An action that adds the ips nature to a project.
 * 
 * @author Jan Ortmann, Daniel Hohenberger
 */
public class AddIpsNatureAction extends ActionDelegate {

    private IJavaProject javaProject = null;

    @Override
    public void selectionChanged(IAction action, ISelection newSelection) {
        javaProject = null;
        if (newSelection instanceof IStructuredSelection) {
            Object selected = ((IStructuredSelection)newSelection).getFirstElement();
            IProject prj = null;
            if (selected instanceof IAdaptable) {
                Object adapted = ((IAdaptable)selected).getAdapter(IProject.class);
                if (adapted == null) {
                    action.setEnabled(false);
                }
                prj = (IProject)adapted;
            } else if (selected instanceof IProject) {
                prj = (IProject)selected;
            }

            if (prj == null || !prj.isOpen()) {
                action.setEnabled(false);
                return;
            }

            // only work with Java projects that are not IPS Projects at the same time
            try {
                IJavaProject jPrj = (IJavaProject)prj.getNature(JavaCore.NATURE_ID);
                if (!ProjectUtil.hasIpsNature(prj) && jPrj != null) {
                    javaProject = jPrj;
                }
                action.setEnabled(javaProject != null);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
    }

    @Override
    public void runWithEvent(IAction action, Event event) {
        if (javaProject == null) {
            MessageDialog.openInformation(getShell(), Messages.AddIpsNatureAction_titleAddFaktorIpsNature,
                    Messages.AddIpsNatureAction_needToSelectOneSingleJavaProject);
            return;
        }
        if (javaProject == null) {
            MessageDialog.openInformation(getShell(), Messages.AddIpsNatureAction_titleAddFaktorIpsNature,
                    Messages.AddIpsNatureAction_mustSelectAJavaProject);
            return;
        }
        try {
            if (ProjectUtil.hasIpsNature(javaProject)) {
                MessageDialog.openInformation(getShell(), Messages.AddIpsNatureAction_titleAddFaktorIpsNature,
                        Messages.AddIpsNatureAction_msgIPSNatureAlreadySet);
                return;
            }
            IIpsModel ipsModel = IIpsModel.get();
            IIpsProject ipsProject = ipsModel.getIpsProject(wrap(javaProject.getProject()).as(AProject.class));
            if (ipsProject.getIpsProjectPropertiesFile().exists()) {
                /*
                 * re-add the IPS Nature. For example when using SAP-NWDS, the project file is
                 * created by NWDS when checking out the Development Component from the Design Time
                 * Repository (DTR). The .project file is not stored in the DTR. With this action,
                 * the user can re-add the IPS Nature after the check out.
                 */
                boolean answer = MessageDialog.openConfirm(getShell(),
                        Messages.AddIpsNatureAction_titleAddFaktorIpsNature, Messages.AddIpsNatureAction_readdNature);
                if (answer) {
                    ProjectUtil.addIpsNature(ipsProject.getProject().unwrap());
                }
                return;
            }
        } catch (CoreRuntimeException e) {
            IpsPlugin.log(e);
            return;
        }
        AddIpsNatureDialog dialog = new AddIpsNatureDialog(getShell(), javaProject);
        if (dialog.open() == Window.CANCEL) {
            return;
        }

        try {
            ProjectUtil.createIpsProject(javaProject, dialog.getIpsProjectCreationProperties());
        } catch (CoreException e) {
            IpsPlugin.log(e);
            ErrorDialog.openError(getShell(),
                    Messages.AddIpsNatureAction_msgErrorCreatingIPSProject + ": " + javaProject.getElementName(), //$NON-NLS-1$
                    e.getMessage(),
                    e.getStatus());
        }
    }

    /**
     * Returns the active shell.
     */
    protected Shell getShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

}
