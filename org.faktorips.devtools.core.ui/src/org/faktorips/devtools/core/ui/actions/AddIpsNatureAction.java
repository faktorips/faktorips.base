/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.dialogs.AddIpsNatureDialog;
import org.faktorips.devtools.core.util.ProjectUtil;

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
            IIpsModel ipsModel = IpsPlugin.getDefault().getIpsModel();
            IIpsProject ipsProject = ipsModel.getIpsProject(javaProject.getProject());
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
                    ProjectUtil.addIpsNature(ipsProject.getProject());
                }
                return;
            }
        } catch (CoreException e1) {
            IpsPlugin.log(e1);
            return;
        }
        try {
            AddIpsNatureDialog dialog = new AddIpsNatureDialog(getShell(), javaProject);
            if (dialog.open() == Window.CANCEL) {
                return;
            }
            IFolder javaSrcFolder = javaProject.getProject().getFolder("src"); //$NON-NLS-1$
            IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
            for (IPackageFragmentRoot root : roots) {
                if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                    if (root.getCorrespondingResource() instanceof IProject) {
                        IpsStatus status = new IpsStatus(Messages.AddIpsNatureAction_msgSourceInProjectImpossible);
                        ErrorDialog.openError(getShell(), Messages.AddIpsNatureAction_titleAddFaktorIpsNature, null,
                                status);
                        return;
                    }
                    javaSrcFolder = (IFolder)root.getCorrespondingResource();
                    break;
                }
            }

            IIpsProject ipsProject = ProjectUtil.createIpsProject(javaProject, dialog.getRuntimeIdPrefix(),
                    dialog.isProductDefinitionProject(), dialog.isModelProject(), dialog.isPersistentProject(),
                    dialog.getLocales());
            IFolder ipsModelFolder = ipsProject.getProject().getFolder(dialog.getSourceFolderName());
            if (!ipsModelFolder.exists()) {
                ipsModelFolder.create(true, true, null);
            }
            IpsObjectPath path = new IpsObjectPath(ipsProject);
            path.setOutputDefinedPerSrcFolder(false);
            path.setBasePackageNameForMergableJavaClasses(dialog.getBasePackageName());
            path.setOutputFolderForMergableSources(javaSrcFolder);
            path.setBasePackageNameForDerivedJavaClasses(dialog.getBasePackageName());
            if (javaSrcFolder.exists()) {
                IFolder derivedsrcFolder = javaSrcFolder.getParent().getFolder(new Path("derived")); //$NON-NLS-1$
                derivedsrcFolder.create(true, true, new NullProgressMonitor());
                IClasspathEntry derivedsrc = JavaCore.newSourceEntry(derivedsrcFolder.getFullPath());
                IClasspathEntry[] rawClassPath = javaProject.getRawClasspath();
                IClasspathEntry[] newClassPath = new IClasspathEntry[rawClassPath.length + 1];
                System.arraycopy(rawClassPath, 0, newClassPath, 0, rawClassPath.length);
                newClassPath[newClassPath.length - 1] = derivedsrc;
                javaProject.setRawClasspath(newClassPath, new NullProgressMonitor());
                path.setOutputFolderForDerivedSources(derivedsrcFolder);
            }
            path.newSourceFolderEntry(ipsModelFolder);
            ipsProject.setIpsObjectPath(path);

        } catch (CoreException e) {
            IpsStatus status = new IpsStatus(Messages.AddIpsNatureAction_msgErrorCreatingIPSProject + javaProject, e);
            ErrorDialog.openError(getShell(), Messages.AddIpsNatureAction_titleAddFaktorIpsNature, null, status);
            IpsPlugin.log(e);
        }
    }

    /**
     * Returns the active shell.
     */
    protected Shell getShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

}
