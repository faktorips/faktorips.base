/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.views.testrunner.IpsTestRunnerViewPart;

/**
 * Action to run ips test depending on the selecion.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestAction extends IpsAction {
	private static final String SEPARATOR = "#|#"; //$NON-NLS-1$
	/**
	 * @param selectionProvider
	 */
	public IpsTestAction(ISelectionProvider selectionProvider) {
		super(selectionProvider);
        super.setText(Messages.IpsTestCaseAction_name);
        super.setDescription(Messages.IpsTestCaseAction_description);
        super.setToolTipText(Messages.IpsTestCaseAction_tooltip);
	}

	/**
	 * {@inheritDoc}
	 */
	public void run(IStructuredSelection selection) {
		try {
			List selectedElements = selection.toList();
			List selectedPathElements = new ArrayList(1);
			IIpsPackageFragmentRoot root = null;

			for (Iterator iter = selectedElements.iterator(); iter.hasNext();) {
				Object element = iter.next();
				if (element instanceof IIpsPackageFragmentRoot) {
					root = (IIpsPackageFragmentRoot) element;
					IIpsProject project = root.getIpsProject();
					selectedPathElements.add(project.getName() + SEPARATOR + getRepPckNameFromPckFrgmtRoot(root) + SEPARATOR + ""); //$NON-NLS-1$
				} else if (element instanceof IIpsPackageFragment) {
					IIpsPackageFragment child = (IIpsPackageFragment) element;
					root = (IIpsPackageFragmentRoot) child.getRoot();
					IIpsProject project = root.getIpsProject();
					selectedPathElements.add(project.getName() + SEPARATOR + getRepPckNameFromPckFrgmtRoot(root)+ SEPARATOR + child.getName());
				} else if (element instanceof ITestCase) {
					ITestCase testCase = (ITestCase) element;
					root = testCase.getIpsPackageFragment().getRoot();
					IIpsProject project = root.getIpsProject();
					selectedPathElements.add(project.getName() + SEPARATOR + getRepPckNameFromPckFrgmtRoot(root) + SEPARATOR + testCase.getQualifiedName());
				} else if (element instanceof IIpsProject) {
					root = ipsProjectSelected((IIpsProject) element, selectedPathElements);
				} else if (element instanceof IJavaProject) {
					// e.g. if selected from the standard package explorer
					IJavaProject javaProject = (IJavaProject) element;
					IProject project = javaProject.getProject();
					if (project.hasNature(IIpsProject.NATURE_ID)){
						IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project.getName());
						root = ipsProjectSelected(ipsProject, selectedPathElements);
					}
				}
			}
			
			if (root!=null){
				selectedPathElements = removeDuplicatEntries(selectedPathElements);
				if (assertSelectedElemsInSameProject(selectedPathElements))
					runTest(selectedPathElements, root.getIpsProject().getJavaProject());
			}
		} catch (CoreException e) {
			IpsPlugin.logAndShowErrorDialog(e);
			return;
		}
	}
	
	/*
	 * Add all package fragment roots of the selected IpsProject - including the project name - to the given list.
	 */
	private IIpsPackageFragmentRoot ipsProjectSelected(IIpsProject ipsProject, List selectedPathElements) throws CoreException {
		IIpsPackageFragmentRoot root = null;	
		IIpsPackageFragmentRoot[] rootsFromProject;
		rootsFromProject = ipsProject.getIpsPackageFragmentRoots();
		for (int i = 0; i < rootsFromProject.length; i++) {
			root = rootsFromProject[i];
			IIpsProject project = root.getIpsProject();
			selectedPathElements.add(project.getName() + SEPARATOR + getRepPckNameFromPckFrgmtRoot(root) + SEPARATOR + ""); //$NON-NLS-1$
		}
		return root;
	}

	/*
	 * Remove duplicate entries and already containing sub path elements from the given list.<br>
	 * Example:
	 * 1) hp.Test
	 * 2) hp.Test.Test1
	 * => entry 2) will be removed because it is implicit in entry 1)
	 */
	private List removeDuplicatEntries(List selectedPathElements) throws CoreException{
		List uniqueList = new ArrayList(selectedPathElements.size());
		Collections.sort(selectedPathElements);
		
		String previousElement = "#none#"; //$NON-NLS-1$
		for (Iterator iter = selectedPathElements.iterator(); iter.hasNext();) {
			String currElement = (String) iter.next();
			// add element only if it is not included in the previous element
			if (! currElement.startsWith(previousElement)){
				previousElement = currElement;
				uniqueList.add(currElement);
			}
		}
		return uniqueList;
	}
	
	/*
	 * Assert that only one project ist selected. Return <code>true</code> is only one project was selected.
	 * Return <code>false</code> if more than one project was selected. If more than one project was selected
	 * show an error dialog to inform the user.
	 */
	private boolean assertSelectedElemsInSameProject(List selectedPathElements){
		// assert that the selection is in the same project
		if (!(selectedPathElements.size()>=0))
			return true;
		
		String previousElement = (String )selectedPathElements.get(0);
		for (Iterator iter = selectedPathElements.iterator(); iter.hasNext();) {
			String currElement = (String) iter.next();
			String prevProject = previousElement.substring(0, previousElement.indexOf(SEPARATOR));
			if (! currElement.startsWith(prevProject)){
				MessageDialog.openError(null, Messages.IpsTestAction_titleCantRunTest, Messages.IpsTestAction_msgCantRunTest);
				return false;
			}
			previousElement = currElement;
		}
		return true;
	}
	
	/*
	 * Gets the package name from the given ips package fragment root.
	 */
	private String getRepPckNameFromPckFrgmtRoot(IIpsPackageFragmentRoot root) throws CoreException {
		IIpsArtefactBuilderSet builderSet = root.getIpsProject().getArtefactBuilderSet();
		return ((DefaultBuilderSet) builderSet).getInternalBasePackageName(root);
	}
	
	/*
	 * Run the test.
	 */
	private void runTest(List selectedPathElements, IJavaProject javaProject) {
		if (selectedPathElements.size() > 0){
			String testRootsString= ""; //$NON-NLS-1$
			String testPackagesString= ""; //$NON-NLS-1$
			
			// create the strings containing the roots and packages
			for (Iterator iter = selectedPathElements.iterator(); iter.hasNext();) {
				String selectedPathElement = (String) iter.next();
				String withoutProject = selectedPathElement.substring(selectedPathElement.indexOf(SEPARATOR) + SEPARATOR.length());
				testRootsString += "{" + withoutProject.substring(0, withoutProject.indexOf(SEPARATOR)) + "}"; //$NON-NLS-1$ //$NON-NLS-2$
				testPackagesString += "{" + withoutProject.substring(withoutProject.indexOf(SEPARATOR) + SEPARATOR.length()) + "}";  //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			// show view
			try {
				showTestCaseResultView(IpsTestRunnerViewPart.EXTENSION_ID);
			} catch (PartInitException e) {
				IpsPlugin.logAndShowErrorDialog(e);
				return;
			}
			
			// run the test
			IIpsTestRunner testRunner = IpsPlugin.getDefault().getIpsTestRunner();
			// get the java project from the first root, currently it is not possible
			// to select more than one root from different projects (means different java projects)
			testRunner.setJavaProject(javaProject);
			testRunner.startTestRunnerJob(testRootsString, testPackagesString);
		}
	}
    
	/*
	 * Displays the ips test run result view.
	 */
	private IViewPart showTestCaseResultView(String viewId) throws PartInitException {
		return IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId);
	}	
}
