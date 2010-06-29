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

package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseEditor;
import org.faktorips.devtools.core.ui.views.testrunner.IpsTestRunnerViewPart;

/**
 * Action to run ips test depending on the selecion.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestAction extends IpsAction {

    private static final String SEPARATOR = "#|#"; //$NON-NLS-1$

    private String mode;

    private ILaunch launch;

    public IpsTestAction(ISelectionProvider selectionProvider) {
        super(selectionProvider);
        super.setText(Messages.IpsTestCaseAction_name);
        super.setDescription(Messages.IpsTestCaseAction_description);
        super.setToolTipText(Messages.IpsTestCaseAction_tooltip);
        super.setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("TestCaseRun.gif")); //$NON-NLS-1$
    }

    public IpsTestAction(ISelectionProvider selectionProvider, String mode) {
        super(selectionProvider);
        this.mode = mode;
    }

    /**
     * Adds all test path elements depending on the given object
     */
    private IIpsPackageFragmentRoot addPathElementFromObject(List<String> pathElements, Object object)
            throws CoreException {

        IIpsPackageFragmentRoot root = null;
        if (object instanceof IIpsPackageFragmentRoot) {
            root = (IIpsPackageFragmentRoot)object;
            if (root.exists()) {
                IIpsProject project = root.getIpsProject();
                String tocFilePackage = getRepPckNameFromPckFrgmtRoot(root);
                if (tocFilePackage != null) {
                    pathElements.add(project.getName() + SEPARATOR + tocFilePackage + SEPARATOR + ""); //$NON-NLS-1$
                }
            } else {
                root = null;
            }
        } else if (object instanceof IIpsPackageFragment) {
            IIpsPackageFragment child = (IIpsPackageFragment)object;
            root = child.getRoot();
            String name = child.getName();
            addElement(pathElements, root, name);
        } else if (object instanceof ITestCase) {
            ITestCase testCase = (ITestCase)object;
            root = testCase.getIpsPackageFragment().getRoot();
            String name = testCase.getQualifiedName();
            addElement(pathElements, root, name);
        } else if (object instanceof IIpsProject) {
            root = ipsProjectSelected((IIpsProject)object, pathElements);
        } else if (object instanceof IJavaProject) {
            // e.g. if selected from the standard package explorer
            IJavaProject javaProject = (IJavaProject)object;
            IProject project = javaProject.getProject();
            if (project.hasNature(IIpsProject.NATURE_ID)) {
                IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project.getName());
                root = ipsProjectSelected(ipsProject, pathElements);
            }
        } else if (object instanceof IProductCmpt) {
            IProductCmpt productCmpt = (IProductCmpt)object;
            root = productCmpt.getIpsPackageFragment().getRoot();
            String name = productCmpt.getQualifiedName();
            addElement(pathElements, root, name);
        } else if (object instanceof IResource) {
            IIpsElement ipsElem = IpsPlugin.getDefault().getIpsModel().getIpsElement((IResource)object);
            root = addPathElementFromObject(pathElements, ipsElem);
        } else if (object instanceof IIpsSrcFile) {
            IIpsObject ipsObject = ((IIpsSrcFile)object).getIpsObject();
            root = addPathElementFromObject(pathElements, ipsObject);
        }
        return root;
    }

    private void addElement(List<String> pathElements, IIpsPackageFragmentRoot root, String name) throws CoreException {
        if (root.exists()) {
            IIpsProject project = root.getIpsProject();
            pathElements.add(project.getName() + SEPARATOR + getRepPckNameFromPckFrgmtRoot(root) + SEPARATOR + name);
        }
    }

    @Override
    public void run(IStructuredSelection selection) {
        try {
            List<String> selectedPathElements = new ArrayList<String>(1);

            /*
             * Contains the root of the selected element, only one root is necessary to obtain the
             * java project for the test runner, if more elements selected the root of the last
             * selected entry will be used
             */
            IIpsPackageFragmentRoot root = null;

            // evaluate the test path depending on the selection to run the test
            for (Iterator<Object> iter = getSelectionIterator(selection); iter.hasNext();) {
                Object element = iter.next();
                if (element instanceof StructuredSelection) {
                    for (Iterator<Object> iterator = getSelectionIterator((StructuredSelection)element); iterator
                            .hasNext();) {
                        Object selStructObj = iterator.next();
                        IIpsPackageFragmentRoot currRoot = addPathElementFromObject(selectedPathElements, selStructObj);
                        if (currRoot != null) {
                            root = currRoot;
                        }
                    }
                } else {
                    root = addPathElementFromObject(selectedPathElements, element);
                }
            }

            if (root == null || selectedPathElements.size() == 0) {
                MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
                        Messages.IpsTestAction_RunMessageDialogNoTestsFound_Title,
                        Messages.IpsTestAction_RunMessageDialogNoTestsFound_Text);
            } else {
                selectedPathElements = removeDuplicatEntries(selectedPathElements);
                if (assertSelectedElemsInSameProject(selectedPathElements)) {
                    /*
                     * get the ips project from the first root, currently it is not possible to
                     * select more than one root from different projects (means different java
                     * projects)
                     */
                    runTest(selectedPathElements, root.getIpsProject());
                }
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
    }

    public void run(String tocFile, String testCasePackages) {
        try {
            showTestCaseResultView(IpsTestRunnerViewPart.EXTENSION_ID);

            IIpsTestRunner testRunner = IpsPlugin.getDefault().getIpsTestRunner();
            testRunner.startTestRunnerJob(tocFile, testCasePackages, mode, launch);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Add all package fragment roots of the selected IpsProject - including the project name - to
     * the given list.
     */
    private IIpsPackageFragmentRoot ipsProjectSelected(IIpsProject ipsProject, List<String> selectedPathElements)
            throws CoreException {

        IIpsPackageFragmentRoot root = null;
        IIpsPackageFragmentRoot[] rootsFromProject;
        rootsFromProject = ipsProject.getIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot element : rootsFromProject) {
            root = element;
            IIpsProject project = root.getIpsProject();
            String tocFilePackage = getRepPckNameFromPckFrgmtRoot(root);
            if (tocFilePackage != null) {
                selectedPathElements.add(project.getName() + SEPARATOR + tocFilePackage + SEPARATOR + ""); //$NON-NLS-1$
            }
        }
        return root;
    }

    /**
     * Remove duplicate entries and already containing sub path elements from the given list.<br>
     * Example: 1) hp.Test 2) hp.Test.Test1 => entry 2) will be removed because it is implicit in
     * entry 1)
     */
    private List<String> removeDuplicatEntries(List<String> selectedPathElements) {
        List<String> uniqueList = new ArrayList<String>(selectedPathElements.size());
        Collections.sort(selectedPathElements);

        String previousElement = "#none#"; //$NON-NLS-1$
        for (String currElement : selectedPathElements) {
            // add element only if it is not included in the previous element
            if (!currElement.startsWith(previousElement)) {
                previousElement = currElement;
                uniqueList.add(currElement);
            }
        }
        return uniqueList;
    }

    /**
     * Assert that only one project ist selected. Return <code>true</code> is only one project was
     * selected. Return <code>false</code> if more than one project was selected. If more than one
     * project was selected show an error dialog to inform the user.
     */
    private boolean assertSelectedElemsInSameProject(List<String> selectedPathElements) {
        // assert that the selection is in the same project
        if (!(selectedPathElements.size() >= 0)) {
            return true;
        }

        String previousElement = selectedPathElements.get(0);
        for (String currElement : selectedPathElements) {
            String prevProject = previousElement.substring(0, previousElement.indexOf(SEPARATOR));
            if (!currElement.startsWith(prevProject)) {
                MessageDialog.openInformation(null, Messages.IpsTestAction_titleCantRunTest,
                        Messages.IpsTestAction_msgCantRunTest);
                return false;
            }
            previousElement = currElement;
        }
        return true;
    }

    /**
     * Gets the package name from the given ips package fragment root.
     */
    public static String getRepPckNameFromPckFrgmtRoot(IIpsPackageFragmentRoot root) throws CoreException {
        IIpsArtefactBuilderSet builderSet = root.getIpsProject().getIpsArtefactBuilderSet();
        return builderSet.getRuntimeRepositoryTocResourceName(root);
    }

    /**
     * Run the test.
     */
    private void runTest(List<String> selectedPathElements, IIpsProject ipsProject) throws CoreException {
        if (selectedPathElements.size() > 0) {
            String testRootsString = ""; //$NON-NLS-1$
            String testPackagesString = ""; //$NON-NLS-1$
            // create the strings containing the roots and packages format for the root string:
            // {root1}{root2}{...}{rootn}
            // format for the packages string: {pck1}{pck2}{...}{pckn}
            for (String selectedPathElement : selectedPathElements) {
                // cut the project from the front of the given string
                String withoutProject = selectedPathElement.substring(selectedPathElement.indexOf(SEPARATOR)
                        + SEPARATOR.length());
                testRootsString += "{" + withoutProject.substring(0, withoutProject.indexOf(SEPARATOR)) + "}"; //$NON-NLS-1$ //$NON-NLS-2$
                testPackagesString += "{" + withoutProject.substring(withoutProject.indexOf(SEPARATOR) + SEPARATOR.length()) + "}"; //$NON-NLS-1$ //$NON-NLS-2$
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
            testRunner.setIpsProject(ipsProject);
            testRunner.startTestRunnerJob(testRootsString, testPackagesString, mode, launch);
        }
    }

    /**
     * Displays the ips test run result view.
     */
    private void showTestCaseResultView(String viewId) throws PartInitException {
        IWorkbenchPage wp = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart wpart = wp.getActivePart();
        // activate the test case result view only if the test case editor is not the active part
        if (wpart instanceof TestCaseEditor) {
            wp.showView(viewId, null, IWorkbenchPage.VIEW_VISIBLE);
        } else {
            wp.showView(viewId, null, IWorkbenchPage.VIEW_ACTIVATE);
        }
    }

    public void setLauch(ILaunch launch) {
        this.launch = launch;
    }

}
