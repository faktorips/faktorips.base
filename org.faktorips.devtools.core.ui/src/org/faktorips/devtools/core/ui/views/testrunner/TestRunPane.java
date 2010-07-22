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

package org.faktorips.devtools.core.ui.views.testrunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsProductDefinitionPerspectiveFactory;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.devtools.core.ui.actions.IpsTestAction;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestRunPane {
    private final static String[] EMPTY_STRING_ARRAY = new String[0];

    private Table fTable;

    private boolean showErrorsOrFailureOnly = false;

    private List<TableItem> tableFailureItems = new ArrayList<TableItem>();

    private int currErrorOrFailure = 0;

    private IpsTestRunnerViewPart testRunnerViewPart;

    private List<String> missingTestEntries = new ArrayList<String>();

    // Maps test Ids to the stored table items.
    private Map<String, TestTableEntry> fTableItemMap = new HashMap<String, TestTableEntry>();

    private final ImageDescriptor testImageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
            "obj16/test.gif");

    public TestRunPane(Composite parent, final IpsTestRunnerViewPart testRunnerViewPart) {
        this.testRunnerViewPart = testRunnerViewPart;
        fTable = new Table(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        fTable.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.item instanceof TableItem) {
                    TableItem selectedTestCase = (TableItem)e.item;
                    showDetailsInFailurePane(selectedTestCase);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });
        fTable.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (fTable.getSelectionCount() > 0) {
                    if (getSelectedTestFullPath() != null) {
                        new OpenTestInEditorAction(testRunnerViewPart, getSelectedTestFullPath(),
                                getSelectedTestQualifiedName(), null).run();
                    }
                }
            }
        });

        buildContextMenu();
    }

    private void buildContextMenu() {
        TableMenu menuMgr = new TableMenu();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(menuMgr);

        Menu menu = menuMgr.createContextMenu(fTable);
        fTable.setMenu(menu);
    }

    private class TableMenu extends MenuManager implements IMenuListener {
        @Override
        public void menuAboutToShow(IMenuManager manager) {
            if (!(fTable.getItemCount() > 0) && getSelectedItem() != null) {
                return;
            }

            Action open = new Action("actionOpen", IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
                @Override
                public void run() {
                    new OpenTestInEditorAction(testRunnerViewPart, testRunnerViewPart.getSelectedTestFullPath(),
                            testRunnerViewPart.getSelectedTestQualifiedName(), null).run();
                }
            };
            open.setText(Messages.TestRunPane_Menu_GoToFile);
            manager.add(open);

            manager.add(new Separator());

            Action runAction = new Action("actionRun", IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
                @Override
                public void run() {
                    startSelectedTest(ILaunchManager.RUN_MODE);
                }
            };
            runAction.setText(Messages.TestRunPane_Menu_Run);
            manager.add(runAction);

            // show debug entry only if the current perspective is not the produduct definition
            // perspective
            IPerspectiveDescriptor pd = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().getPerspective();
            if (pd != null) {
                String perspective = pd.getId();
                if (!IpsProductDefinitionPerspectiveFactory.PRODUCTDEFINITIONPERSPECTIVE_ID.equals(perspective)) {
                    Action debugAction = new Action("actionDebug", IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
                        @Override
                        public void run() {
                            startSelectedTest(ILaunchManager.DEBUG_MODE);
                        }
                    };
                    debugAction.setText(Messages.TestRunPane_MenuDebug);
                    manager.add(debugAction);
                }
            }
        }
    }

    private void startSelectedTest(String mode) {
        String selectedTestQualifiedName = getSelectedTestQualifiedName();
        if (StringUtils.isEmpty(selectedTestQualifiedName)) {
            return;
        }

        IIpsObject ipsObject = null;
        try {
            IIpsProject ipsProject = IpsPlugin.getDefault().getIpsTestRunner().getIpsProject();
            IIpsPackageFragmentRoot[] ipsPackageFragmentRoots = ipsProject.getIpsPackageFragmentRoots();
            for (IIpsPackageFragmentRoot ipsPackageFragmentRoot : ipsPackageFragmentRoots) {
                // currently two types of ips objects supports test:
                // 1. product cmpts
                // 2. isp test cases
                ipsObject = ipsPackageFragmentRoot.findIpsObject(IpsObjectType.TEST_CASE, selectedTestQualifiedName);
                if (ipsObject != null) {
                    break;
                }
                ipsObject = ipsPackageFragmentRoot.findIpsObject(IpsObjectType.PRODUCT_CMPT, selectedTestQualifiedName);
                if (ipsObject != null) {
                    break;
                }
            }
            if (ipsObject != null) {
                String repositoryPackage = null;
                repositoryPackage = IpsTestAction.getRepPckNameFromPckFrgmtRoot(ipsObject.getIpsPackageFragment()
                        .getRoot());
                IpsPlugin.getDefault().getIpsTestRunner().startTestRunnerJob(repositoryPackage,
                        selectedTestQualifiedName, mode, null);
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
    }

    /*
     * Display the error or failure details in the failure pane.
     */
    private void showDetailsInFailurePane(TableItem tableItem) {
        TestTableEntry testEntry = (TestTableEntry)tableItem.getData();
        String details[] = EMPTY_STRING_ARRAY;
        if (testEntry.isFailure()) {
            details = testEntry.getFailureDetails().toArray(new String[0]);
        } else if (testEntry.isError()) {
            details = testEntry.getErrorDetails();
        }

        testRunnerViewPart.selectionOfTestCaseChanged(details);
    }

    /**
     * Returns the composite used to present the test runs.
     */
    public Composite getComposite() {
        return fTable;
    }

    /**
     * Select the first failure or error table entry. If there was no failure or error then nothing
     * will selected.
     */
    public void selectFirstFailureOrError() {
        if (tableFailureItems.size() == 0) {
            return;
        }

        fTable.setSelection(fTable.indexOf(tableFailureItems.get(0)));
        showDetailsInFailurePane(tableFailureItems.get(0));
    }

    /**
     * Select the next failure or error, does nothing if no error or failure exists.
     */
    public void selectNextFailureOrError() {
        if (tableFailureItems.size() == 0) {
            return;
        }

        currErrorOrFailure++;
        if (currErrorOrFailure > (tableFailureItems.size() - 1)) {
            currErrorOrFailure = 0;
        }

        fTable.setSelection(fTable.indexOf(tableFailureItems.get(currErrorOrFailure)));
        showDetailsInFailurePane(tableFailureItems.get(currErrorOrFailure));
    }

    /**
     * Select the previous failure or error, does nothing if no error or failure exists.
     */
    public void selectPreviousFailureOrError() {
        if (tableFailureItems.size() == 0) {
            return;
        }

        currErrorOrFailure--;

        if (currErrorOrFailure < 0) {
            currErrorOrFailure = tableFailureItems.size() - 1;
        }

        fTable.setSelection(fTable.indexOf(tableFailureItems.get(currErrorOrFailure)));
        showDetailsInFailurePane(tableFailureItems.get(currErrorOrFailure));
    }

    public String[] getFailureDetailsOfSelectedTestCase(int failureIndex) {
        TableItem ti = getSelectedItem();
        if (ti == null) {
            return EMPTY_STRING_ARRAY;
        }
        TestTableEntry entry = (TestTableEntry)ti.getData();
        return entry.getDetailsOfSingleFailure(failureIndex);
    }

    public List<String[]> getAllFailureDetailsOfSelectedTestCase() {
        TableItem ti = getSelectedItem();
        if (ti == null) {
            return new ArrayList<String[]>(0);
        }
        TestTableEntry entry = (TestTableEntry)ti.getData();
        return entry.getFailureOrErrorDetailList();
    }

    //
    // Methods to inform this pane about test run, end and failures
    //

    /**
     * A new test run will be stared.
     */
    public void aboutToStart() {
        missingTestEntries.clear();
        fTable.removeAll();
        fTableItemMap.clear();
        tableFailureItems.clear();
        currErrorOrFailure = 0;
    }

    /**
     * The given test is about to be started.
     */
    public void newTableEntry(String testId, String qualifiedTestName, String fullPath) {
        if (fTableItemMap.get(testId) != null) {
            // test table entry is already in the table, ignore entry
            return;
        }

        TableItem tableItem = new TableItem(fTable, SWT.NONE);
        tableItem.setText(qualifiedTestName);
        tableItem.setImage((Image)testRunnerViewPart.getResourceManager().get(testImageDescriptor));

        TestTableEntry testTableEntry = new TestTableEntry(qualifiedTestName, tableItem);
        fTableItemMap.put(testId, testTableEntry);
        tableItem.setData(testTableEntry);
        // stores the given full path of the test case file
        testTableEntry.setFullPath(fullPath);
    }

    /**
     * The given test ends.
     */
    public void endTest(String testId, String qualifiedTestName) {
        TestTableEntry testTableEntry = fTableItemMap.get(testId);
        if (testTableEntry == null) {
            missingTestEntries.add(testId);
            return;
        }
        updateTestEntryStatusImage(testTableEntry);
    }

    /*
     * Update the image of the given test entry.
     */
    private void updateTestEntryStatusImage(TestTableEntry testTableEntry) {
        TableItem tableItem = testTableEntry.getTableItem();
        Image baseImage = (Image)testRunnerViewPart.getResourceManager().get(testImageDescriptor);
        ImageDescriptor overlay = null;
        if (testTableEntry.isError()) {
            overlay = OverlayIcons.ERROR_OVR_DESC;
        } else if (testTableEntry.isFailure()) {
            overlay = OverlayIcons.FAILURE_OVR_DESC;
        } else if (testTableEntry.isOk()) {
            overlay = OverlayIcons.SUCCESS_OVR_DESC;
        }
        ImageDescriptor imageDescriptor = new DecorationOverlayIcon(baseImage, overlay, IDecoration.BOTTOM_LEFT);
        tableItem.setImage((Image)testRunnerViewPart.getResourceManager().get(imageDescriptor));

        if (showErrorsOrFailureOnly && !(testTableEntry.isError() || testTableEntry.isFailure())) {
            // remove successfully tested element from table,
            // because of show only errors or failures filter
            fTable.remove(fTable.indexOf(tableItem));
            testTableEntry.setTableItem(null);
        }
    }

    /**
     * The given test has started.
     * 
     * @param scrollLocked
     */
    public void startTest(String testId, String qualifiedTestName, boolean scrollLocked) {
        TestTableEntry testTableEntry = fTableItemMap.get(testId);
        if (testTableEntry == null) {
            return;
        }

        TableItem tableItem = testTableEntry.getTableItem();
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/testrun.gif");
        tableItem.setImage((Image)testRunnerViewPart.getResourceManager().get(imageDescriptor));

        // select current item and scroll
        if (!scrollLocked) {
            fTable.setSelection(new TableItem[] { tableItem });
        }
    }

    /**
     * The given test fails with the given details.
     */
    public void failureTest(String testId, String failure, String[] failureDetails) {
        TestTableEntry testTableEntry = fTableItemMap.get(testId);
        if (testTableEntry == null) {
            return;
        }

        testTableEntry.setStatus(TestTableEntry.FAILURE);
        testTableEntry.addFailure(failure);
        testTableEntry.addFailureDetails(failureDetails);

        if (!tableFailureItems.contains(testTableEntry.getTableItem())) {
            tableFailureItems.add(testTableEntry.getTableItem());
        }
    }

    /**
     * There was an error while running the given test.
     */
    public void errorInTest(String testId, String qualifiedTestName, String[] errorDetails) {
        TestTableEntry testTableEntry = fTableItemMap.get(testId);
        if (testTableEntry == null) {
            // in case of an error before starting a single test
            // create an error entry
            newTableEntry(testId, Messages.TestRunPane_ErrorStartingTest_Entry, ""); //$NON-NLS-1$
            testTableEntry = fTableItemMap.get(testId);
        }
        TableItem tableItem = testTableEntry.getTableItem();
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/testerr.gif");
        tableItem.setImage((Image)testRunnerViewPart.getResourceManager().get(imageDescriptor));
        testTableEntry.setErrorDetails(errorDetails);
        testTableEntry.setStatus(TestTableEntry.ERROR);

        tableFailureItems.add(tableItem);
    }

    /**
     * Returns the full path of the selected test case or an empty string if no test case is
     * selected
     */
    String getSelectedTestFullPath() {
        TableItem item = getSelectedItem();
        if (item == null) {
            return ""; //$NON-NLS-1$
        }

        TestTableEntry entry = (TestTableEntry)item.getData();
        return entry.getFullPath();
    }

    String getSelectedTestQualifiedName() {
        TableItem item = getSelectedItem();
        if (item == null) {
            return ""; //$NON-NLS-1$
        }

        TestTableEntry entry = (TestTableEntry)item.getData();
        return entry.getQualifiedTestName();
    }

    /*
     * Returns the selected item in the table or <code>null</code> if no item is selected
     */
    private TableItem getSelectedItem() {
        int index = fTable.getSelectionIndex();
        if (index == -1) {
            return null;
        }
        return fTable.getItem(index);
    }

    /*
     * Inner class to store a test case.
     */
    private class TestTableEntry {
        public static final int ERROR = -1;
        public static final int FAILURE = 0;
        public static final int UNKNOWN = 2;
        private String qualifiedTestName;
        private String fullPath;
        private TableItem tableItem;
        private int status = UNKNOWN;
        private List<String> failures = new ArrayList<String>();
        private List<String[]> failureDetailList = new ArrayList<String[]>();
        private String[] errorDetails = EMPTY_STRING_ARRAY;

        TestTableEntry(String qualifiedTestName, TableItem tableItem) {
            this.qualifiedTestName = qualifiedTestName;
            this.tableItem = tableItem;
        }

        public String getQualifiedTestName() {
            return qualifiedTestName;
        }

        public TableItem getTableItem() {
            return tableItem;
        }

        public String getFullPath() {
            return fullPath;
        }

        public void setFullPath(String fullPath) {
            this.fullPath = fullPath;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public boolean isError() {
            return ERROR == status;
        }

        public boolean isFailure() {
            return FAILURE == status;
        }

        public boolean isOk() {
            return status != ERROR && status != FAILURE;
        }

        public void addFailure(String failure) {
            failures.add(failure);
        }

        public List<String> getFailureDetails() {
            return failures;
        }

        public String[] getErrorDetails() {
            return errorDetails;
        }

        public void setErrorDetails(String[] errorDetails) {
            this.errorDetails = errorDetails;
        }

        public void addFailureDetails(String[] failureDetails) {
            failureDetailList.add(failureDetails);
        }

        public String[] getDetailsOfSingleFailure(int idx) {
            if (idx >= failureDetailList.size()) {
                return EMPTY_STRING_ARRAY;
            } else {
                return failureDetailList.get(idx);
            }
        }

        public List<String[]> getFailureOrErrorDetailList() {
            if (errorDetails.length > 0) {
                ArrayList<String[]> result = new ArrayList<String[]>(1);
                result.add(errorDetails);
                return result;
            } else {
                return failureDetailList;
            }
        }

        public void removeTableItem() {
            tableItem = null;
        }

        public void setTableItem(TableItem tableItem) {
            this.tableItem = tableItem;
        }
    }

    void checkMissingEntries() {
        if (missingTestEntries.size() == 0) {
            return;
        }
        synchronized (missingTestEntries) {
            for (String element : missingTestEntries) {
                TestTableEntry testTableEntry = fTableItemMap.get(element);
                if (testTableEntry == null) {
                    missingTestEntries.add(element);
                    return;
                }
                updateTestEntryStatusImage(testTableEntry);
            }
        }
    }

    public void toggleShowErrorsOrFailuresOnly() {
        showErrorsOrFailureOnly = !showErrorsOrFailureOnly;
        setShowErrorsOrFailuresOnly(showErrorsOrFailureOnly);
    }

    public void setShowErrorsOrFailuresOnly(boolean showErrorsOrFailuresOnly) {
        if (showErrorsOrFailuresOnly) {
            removeNonErrorsAndFailuresFromTable();
        } else {
            showAllElementsInTable();
        }
    }

    private void showAllElementsInTable() {
        for (TestTableEntry tableEntry : fTableItemMap.values()) {
            if (tableEntry.getTableItem() == null) {
                TableItem tableItem = new TableItem(fTable, SWT.NONE);
                tableItem.setText(tableEntry.getQualifiedTestName());
                // test was successful before, because it was not visible <- show only error or
                // failure filter
                ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                        "obj16/testok.gif");
                tableItem.setImage((Image)testRunnerViewPart.getResourceManager().get(imageDescriptor));
                tableItem.setData(tableEntry);
                tableEntry.setTableItem(tableItem);
            }
        }
    }

    private void removeNonErrorsAndFailuresFromTable() {
        List<Integer> nonErrorsOrFailuresIndices = new ArrayList<Integer>();
        for (TestTableEntry testTableEntry : fTableItemMap.values()) {
            if (!(testTableEntry.isError() || testTableEntry.isFailure())) {
                TableItem tableItem = testTableEntry.getTableItem();
                if (tableItem == null) {
                    continue;
                }
                nonErrorsOrFailuresIndices.add(fTable.indexOf(tableItem));
                testTableEntry.removeTableItem();
            }
        }
        if (nonErrorsOrFailuresIndices.size() == 0) {
            return;
        }
        int[] indicesToRemove = new int[nonErrorsOrFailuresIndices.size()];
        for (int i = 0; i < nonErrorsOrFailuresIndices.size(); i++) {
            indicesToRemove[i] = nonErrorsOrFailuresIndices.get(i);
        }
        fTable.remove(indicesToRemove);
    }
}
