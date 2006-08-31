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

package org.faktorips.devtools.core.ui.wizards.testcase.transform;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Import wizard to import runtime test cases as model test cases.<br>
 * The user can select the target package, the test case type (where the new imported test cases
 * belongs to) and optional a name extension which will be concatenated to the new imported test case name.
 * 
 * @author Joerg Ortmann
 */
public class TransformRuntimeTestCaseWizard extends Wizard implements IImportWizard {

	private UIToolkit uiToolkit;

	private IStructuredSelection selectionSource;
	
	private IIpsPackageFragment targetIpsPackageFragment;
	
	private String testCaseTypeName;
	
	private String newTestCaseNameExtension;
	
	private SelectTestCaseTypePage selectTestCaseTypePage;
	
	public TransformRuntimeTestCaseWizard(){
		super();
		setWindowTitle(Messages.TransformWizard_title);
        this.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewTestCaseWizard.gif"));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selectionSource = selection;
		uiToolkit = new UIToolkit(null);		
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addPages() {
		addPage(new SelectTargetPackagePage(this));
		selectTestCaseTypePage = new SelectTestCaseTypePage(this);
		addPage(selectTestCaseTypePage);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean canFinish(){
		boolean testCaseTypeFound = false;
		if (StringUtils.isNotEmpty(testCaseTypeName) && targetIpsPackageFragment != null){
			try {
				if (targetIpsPackageFragment.getIpsProject().findIpsObject(IpsObjectType.TEST_CASE_TYPE, testCaseTypeName) !=  null){
					testCaseTypeFound = true;
					selectTestCaseTypePage.setErrorMsg(""); //$NON-NLS-1$
				} else {
					selectTestCaseTypePage.setErrorMsg(
							NLS.bind(Messages.TransformRuntimeTestCaseWizard_Error_TestCaseTypeNotExists, testCaseTypeName));
				}
			} catch (CoreException e) {
				// ignore exception
			}
		}
		return testCaseTypeFound;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		try {
			TestCaseTransformer transformer = new TestCaseTransformer();
			transformer.startTestRunnerJob(selectionSource, targetIpsPackageFragment, 
					testCaseTypeName, newTestCaseNameExtension);
		} catch (Exception e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
		return true;
	}
	
	/**
	 * Returns the ui toolkit for this wizard.
	 */
	UIToolkit getUiToolkit(){
		return uiToolkit;
	}
	
	/**
	 * Sets the target of the new imported test cases.
	 */
	void setTargetIpsPackageFragment(IIpsPackageFragment targetIpsPackageFragment) {
		this.targetIpsPackageFragment = targetIpsPackageFragment;
		getContainer().updateButtons();
		selectTestCaseTypePage.createTestCaseTypeControl();
		testCaseTypeName = ""; //$NON-NLS-1$
	}
	
	/**
	 * Returns the target of the new imported test cases.
	 */
	IIpsPackageFragment getTargetIpsPackageFragment() {
		return targetIpsPackageFragment;
	}

	/**
	 * Sets the test case type to which the new imported test cases will belong to.
	 * @param testCaseTypeName
	 */
	public void setTestCaseTypeName(String testCaseTypeName) {
		this.testCaseTypeName = testCaseTypeName;
		getContainer().updateButtons();
	}

	/**
	 * Sets the name extension of the new imported test cases.
	 */
	public void setNewTestCaseNameExtension(String newTestCaseNameExtension) {
		this.newTestCaseNameExtension = newTestCaseNameExtension;
		getContainer().updateButtons();
	}

	/**
	 * Returns all ips package fragments of all projects in the current workspace.
	 */
	IIpsPackageFragment[] getPackageFragments(){
		ArrayList packageFragmentList = new ArrayList();
		try {
			IIpsProject[] ipsProjects;
			ipsProjects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
			for (int i = 0; i < ipsProjects.length; i++) {
				IIpsPackageFragmentRoot[] roots = ipsProjects[i].getIpsPackageFragmentRoots();
				for (int j = 0; j < roots.length; j++) {
					IIpsPackageFragment[] childs = roots[j].getIpsPackageFragments();
					for (int k = 0; k < childs.length; k++) {
						packageFragmentList.add(childs[k]);
					}
				}
			}
			return (IIpsPackageFragment[]) packageFragmentList.toArray(new IIpsPackageFragment[0]);
		} catch (Exception e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
		return new IIpsPackageFragment[0];
	}
}
