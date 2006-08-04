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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.testcase.IpsTestRunner;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.views.testrunner.IpsTestRunnerViewPart;
import org.faktorips.runtime.test.CmdLineIpsTestRunner;

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
		
		// TODO Joerg: handle all allowed selection types
		if (element instanceof IIpsPackageFragmentRoot) {
			IIpsPackageFragmentRoot root = (IIpsPackageFragmentRoot) element;
			IpsTestRunner testRunner = new IpsTestRunner();
			testRunner.setJavaProject(root.getIpsProject().getJavaProject());
			try {
				showTestCaseResultView();
				String repositoryPackage = "de.qv.produkt.produktdaten.hp.internal"; //root.getIpsDefaultPackageFragment().toString();
				// testRunner.run(, "tests");
				CmdLineIpsTestRunner cmdRunner = new CmdLineIpsTestRunner(repositoryPackage);
				
				cmdRunner.setClassloader(((IpsModel) root.getIpsModel())
						.getClassLoaderProvider(root.getIpsProject())
						.getClassLoader());
				
				
				cmdRunner.run("hp.tests");
			} catch (Exception e) {
				IpsPlugin.logAndShowErrorDialog(e);
			}
		}
	}
	
	/*
	 * Displays the ips test run result view.
	 */
	private void showTestCaseResultView() throws PartInitException {
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page= window.getActivePage();
		page.showView(IpsTestRunnerViewPart.EXTENSION_ID);
	}	
}
