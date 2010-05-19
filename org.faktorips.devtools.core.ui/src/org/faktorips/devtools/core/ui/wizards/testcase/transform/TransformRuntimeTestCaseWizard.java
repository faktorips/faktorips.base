/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.testcase.transform;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;

/**
 * Import wizard to import runtime test cases as model test cases.<br>
 * The user can select the target package, the test case type (where the new imported test cases
 * belongs to) and optional a name extension which will be concatenated to the new imported test
 * case name.
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

    public TransformRuntimeTestCaseWizard() {
        super();
        setWindowTitle(Messages.TransformWizard_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewTestCaseWizard.png")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        selectionSource = selection;
        uiToolkit = new UIToolkit(null);
        setNeedsProgressMonitor(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void addPages() {
        addPage(new SelectTargetPackagePage(this));
        selectTestCaseTypePage = new SelectTestCaseTypePage(this);
        addPage(selectTestCaseTypePage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canFinish() {
        if (StringUtils.isNotEmpty(testCaseTypeName) && targetIpsPackageFragment != null) {
            try {
                if (targetIpsPackageFragment.getIpsProject().findIpsObject(IpsObjectType.TEST_CASE_TYPE,
                        testCaseTypeName) != null) {
                    selectTestCaseTypePage.setErrorMsg(""); //$NON-NLS-1$
                    return true;
                } else {
                    selectTestCaseTypePage.setErrorMsg(NLS.bind(
                            Messages.TransformRuntimeTestCaseWizard_Error_TestCaseTypeNotExists, testCaseTypeName));
                }
            } catch (CoreException ignored) {
            }
            return false;
        }
        // the test case type is optinal
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performFinish() {
        try {
            TestCaseTransformer transformer = new TestCaseTransformer(selectionSource, targetIpsPackageFragment,
                    testCaseTypeName, newTestCaseNameExtension);
            WorkbenchRunnableAdapter workbenchRunnableAdapter = new WorkbenchRunnableAdapter(transformer);
            try {
                getContainer().run(true, true, workbenchRunnableAdapter);
            } catch (InterruptedException e) {
                return false;
            } catch (InvocationTargetException e) {
                IpsPlugin.logAndShowErrorDialog(new IpsStatus(e.getCause()));
                return true;
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
                return true;
            }
            return true;
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return true;
    }

    /**
     * Returns the ui toolkit for this wizard.
     */
    UIToolkit getUiToolkit() {
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
     * 
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
    IIpsPackageFragment[] getPackageFragments() {
        ArrayList<IIpsPackageFragment> packageFragmentList = new ArrayList<IIpsPackageFragment>();
        try {
            IIpsProject[] ipsProjects;
            ipsProjects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
            for (IIpsProject ipsProject : ipsProjects) {
                IIpsPackageFragmentRoot[] roots = ipsProject.getIpsPackageFragmentRoots();
                for (int j = 0; j < roots.length; j++) {
                    if (!roots[j].isBasedOnSourceFolder()) {
                        continue;
                    }
                    IIpsPackageFragment[] childs = roots[j].getIpsPackageFragments();
                    for (IIpsPackageFragment child : childs) {
                        packageFragmentList.add(child);
                    }
                }
            }
            return packageFragmentList.toArray(new IIpsPackageFragment[0]);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return new IIpsPackageFragment[0];
    }
}
