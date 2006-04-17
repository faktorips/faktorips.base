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

package org.faktorips.devtools.core.ui.actions;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.faktorips.devtools.core.FaktorIpsClasspathVariableInitializer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObjectPath;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;

/**
 * An action that adds the ips nature to a project.
 * 
 * @author Jan Ortmann
 */
public class AddIpsNatureAction extends ActionDelegate {

	private IStructuredSelection selection = StructuredSelection.EMPTY;

	/**
	 * {@inheritDoc}
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
			IpsStatus status = new IpsStatus(IpsStatus.WARNING, 0, NLS.bind(Messages.AddIpsNatureAction_noJavaProject, selection), null);
			ErrorDialog.openError(getShell(), Messages.AddIpsNatureAction_errorTitle, null, status);
			return;
		}
		IProjectDescription description;
		try {
			description = javaProject.getProject().getDescription();
			String[] natures = description.getNatureIds();
			for (int i = 0; i < natures.length; i++) {
				if (natures[i].equals(IIpsProject.NATURE_ID)) {
					MessageDialog.openInformation(getShell(), Messages.AddIpsNatureAction_titleAddFaktorIpsNature, Messages.AddIpsNatureAction_msgIPSNatureAlreadySet);
					return;
				}
			}
		} catch (CoreException e1) {
			IpsPlugin.log(e1);
			return;
		}
		try {
			IFolder javaSrcFolder = javaProject.getProject().getFolder("src"); //$NON-NLS-1$
			IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].getKind()==IPackageFragmentRoot.K_SOURCE ) {
					if (roots[i].getCorrespondingResource() instanceof IProject) {
						IpsStatus status = new IpsStatus(Messages.AddIpsNatureAction_msgSourceInProjectImpossible);
						ErrorDialog.openError(getShell(), Messages.AddIpsNatureAction_titleAddFaktorIpsNature, null, status);
						return;
					}
					javaSrcFolder = (IFolder)roots[i].getCorrespondingResource();	
					break;
				}
			}
			addIpsRuntimeLibraries(javaProject);
			IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().createIpsProject(javaProject);
			IIpsProjectProperties props = ipsProject.getProperties();
			props.setProductDefinitionProject(true);
			props.setModelProject(true);
			props.setPredefinedDatatypesUsed(IpsPlugin.getDefault().getIpsModel().getPredefinedValueDatatypes());
			ipsProject.setProperties(props);
			IFolder ipsModelFolder = ipsProject.getProject().getFolder("model"); //$NON-NLS-1$
			if (!ipsModelFolder.exists()) {
				ipsModelFolder.create(true, true, null);
			}
			IpsObjectPath path = new IpsObjectPath();
			path.setOutputDefinedPerSrcFolder(false);
			path.setBasePackageNameForGeneratedJavaClasses("org.faktorips.sample.model"); //$NON-NLS-1$
			path.setOutputFolderForGeneratedJavaFiles(javaSrcFolder);
			path.setBasePackageNameForExtensionJavaClasses("org.faktorips.sample.model"); //$NON-NLS-1$
			path.newSourceFolderEntry(ipsModelFolder);
			ipsProject.setIpsObjectPath(path);
			
		} catch (CoreException e) {
			IpsStatus status = new IpsStatus(Messages.AddIpsNatureAction_msgErrorCreatingIPSProject + javaProject, e);
			ErrorDialog.openError(getShell(), Messages.AddIpsNatureAction_titleAddFaktorIpsNature, null, status);
			IpsPlugin.log(e);
		}
	}
	
	private void addIpsRuntimeLibraries(IJavaProject javaProject) throws JavaModelException {
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		int numOfJars = FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_BIN.length;
		IClasspathEntry[] entries = new IClasspathEntry[oldEntries.length + numOfJars];
		System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);
		for (int i = 0; i < numOfJars; i++) {
			Path jarPath = new Path(FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_BIN[i]);
			Path srcZipPath = null;
			if (StringUtils.isNotEmpty(FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_SRC[i])) {
				srcZipPath = new Path(FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_SRC[i]);
			}
			entries[oldEntries.length+i] = JavaCore.newVariableEntry(jarPath, srcZipPath, null);
		}
		javaProject.setRawClasspath(entries, null);
	}

	/**
	 * Returns the active shell.
	 */
	protected Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
	
}
