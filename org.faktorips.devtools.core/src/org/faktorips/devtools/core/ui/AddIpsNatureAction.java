package org.faktorips.devtools.core.ui;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObjectPath;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 * An action that adds the ips nature to a project.
 * 
 * @author Jan Ortmann
 */
public class AddIpsNatureAction extends ActionDelegate {

	private IStructuredSelection selection = StructuredSelection.EMPTY;

	/**
	 * Overridden.
	 */
	public void selectionChanged(IAction action, ISelection newSelection) {
		if (newSelection instanceof IStructuredSelection)
			selection= (IStructuredSelection)newSelection;
		else
			selection= StructuredSelection.EMPTY;
	}

	private IJavaProject getJavaProject() {
		if (selection.size()!=1) {
			return null;
		}
		if (selection.getFirstElement() instanceof IJavaProject) {
			return (IJavaProject)selection.getFirstElement();
		}
		return null;
	}
	
	public void runWithEvent(IAction action, Event event) {
		IJavaProject javaProject = getJavaProject();
		if (javaProject==null) {
			IpsStatus status = new IpsStatus(IpsStatus.WARNING, 0, "Tried to execute addIpsNatureAction on selection " + selection + " which does not contain one single IJavaProject", null);
			ErrorDialog.openError(getShell(), "Error creating IPS project.", null, status);
			return;
		}
		try {
			IFolder javaSrcFolder = javaProject.getProject().getFolder("src");
			IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].getKind()==IPackageFragmentRoot.K_SOURCE ) {
					if (roots[i].getCorrespondingResource() instanceof IProject) {
						IpsStatus status = new IpsStatus("Java project must keep it's source files in folders. Keeping the Java sourcec files in the project iself, is not supported.");
						ErrorDialog.openError(getShell(), "Error creating IPS project.", null, status);
						return;
					}
					javaSrcFolder = (IFolder)roots[i].getCorrespondingResource();	
					break;
				}
			}
			IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().createIpsProject(javaProject);
			ipsProject.setValueDatatypes(IpsPlugin.getDefault().getIpsModel().getPredefinedValueDatatypes());
			IFolder ipsModelFolder = ipsProject.getProject().getFolder("model");
			if (!ipsModelFolder.exists()) {
				ipsModelFolder.create(true, true, null);
			}
			IpsObjectPath path = new IpsObjectPath();
			path.setOutputDefinedPerSrcFolder(false);
			path.setBasePackageNameForGeneratedJavaClasses("org.faktorips.sample.model");
			path.setOutputFolderForGeneratedJavaFiles(javaSrcFolder);
			path.setBasePackageNameForExtensionJavaClasses("org.faktorips.sample.model");
			path.newSourceFolderEntry(ipsModelFolder);
			ipsProject.setIpsObjectPath(path);
		} catch (CoreException e) {
			IpsStatus status = new IpsStatus("Couldn't create IPS project based on Java project " + javaProject, e);
			ErrorDialog.openError(getShell(), "Error creating IPS project.", null, status);
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
