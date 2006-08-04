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

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.ui.views.testrunner.IpsTestRunnerViewPart;

/**
 * Action to run ips test depending on the selecion.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestAction extends IpsAction {
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
		IIpsElement element= (IIpsElement) selection.getFirstElement();
		
		IIpsPackageFragmentRoot root = null;
		// TODO Joerg: get repository package name and suite name from selection
		String repositoryPackage = "de.qv.produkt.produktdaten.hp.internal";
		String testSuite = "hp";		
		
		if (element instanceof IIpsPackageFragmentRoot) {
			root = (IIpsPackageFragmentRoot) element;
			
			//root.getIpsDefaultPackageFragment();
		} else if (element instanceof IIpsPackageFragment) {
			
		}
		
		if (root != null){
			try {
				showTestCaseResultView(IpsTestRunnerViewPart.EXTENSION_ID);
			} catch (PartInitException e) {
				IpsPlugin.logAndShowErrorDialog(e);
				return;
			}
			// run the test
			IIpsTestRunner testRunner = IpsPlugin.getDefault().getIpsTestRunner();
			testRunner.setJavaProject(root.getIpsProject().getJavaProject());
			TestRunnerJob job = new TestRunnerJob(testRunner, repositoryPackage, testSuite);
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			job.setRule(workspace.getRoot());
			job.schedule();
		}
	}
	
	/*
	 * Job class to run the selected tests.
	 */
	private class TestRunnerJob extends WorkspaceJob {
		private IIpsTestRunner testRunner;
		private String classpathRepository;
		private String testsuite;
		
		public TestRunnerJob(IIpsTestRunner testRunner, String classpathRepository, String testsuite) {
			super("FaktorIps Test Job");
			this.testRunner = testRunner;
			this.classpathRepository = classpathRepository;
			this.testsuite = testsuite;
		}
		
		public IStatus runInWorkspace(IProgressMonitor monitor) {
			try {
				testRunner.run(classpathRepository, testsuite);
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
			return Status.OK_STATUS;
		}
	}	
    
	/*
	 * Displays the ips test run result view.
	 */
	private IViewPart showTestCaseResultView(String viewId) throws PartInitException {
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page= window.getActivePage();
		return page.showView(viewId);
	}	
}
