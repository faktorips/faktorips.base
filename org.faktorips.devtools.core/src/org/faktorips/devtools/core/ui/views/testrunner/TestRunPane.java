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

package org.faktorips.devtools.core.ui.views.testrunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestRunPane {
    private final static String[] EMPTY_STRING_ARRAY = new String[0];
    
	private Table fTable;
	
	private TableItem firstFailureOrError;

	private IpsTestRunnerViewPart testRunnerViewPart;
	
    private List missingTestEntries = new ArrayList();
    
	// Maps test Ids to the stored table items. 
	private Map fTableItemMap = new HashMap();
	
	public TestRunPane(Composite parent, final IpsTestRunnerViewPart testRunnerViewPart) {
		this.testRunnerViewPart = testRunnerViewPart;
		fTable = new Table(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		fTable.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
            	if (e.item instanceof TableItem){
	            	TableItem selectedTestCase = (TableItem) e.item;
	            	showDetailsInFailurePane(selectedTestCase);
            	}
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });
		fTable.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				if (fTable.getSelectionCount() > 0)
					if (getTestFullPath() != null)
						new OpenTestInEditorAction(testRunnerViewPart, getTestFullPath()).run();
			}
		});
	}

	/*
	 * Display the error or failure details in the failure pane.
	 */
	private void showDetailsInFailurePane(TableItem tableItem) {
		TestTableEntry testEntry = (TestTableEntry) tableItem.getData();
		String details[] = EMPTY_STRING_ARRAY;
		if (testEntry.isFailure())
			details = (String[])testEntry.getFailureDetails().toArray(new String[0]);
		else if (testEntry.isError())
			details = testEntry.getErrorDetails();
		
		testRunnerViewPart.selectionOfTestCaseChanged(details);
	}
	
	/**
	 * Returns the composite used to present the test runs.
	 */
	public Composite getComposite(){
		return fTable;
	}
	
	/**
	 * Select the last failure or error table enty.
	 * If there was no failure or error then nothing will selected.
	 */
	public void selectFailureOrError(){
		if (firstFailureOrError != null){
			fTable.select(fTable.indexOf(firstFailureOrError));
			showDetailsInFailurePane(firstFailureOrError);
		}
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
        
		firstFailureOrError = null;
	}
	
	/**
	 * The given test is about to be started.
	 */
	public void newTableEntry(String testId, String tableEntry, String fullPath) {
        if (fTableItemMap.get(testId) != null)
            // test table entry is already in the table, ignore entry
            return;
        
        TableItem tableItem = new TableItem(fTable, SWT.NONE);
		tableItem.setText(tableEntry);
		tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/test.gif")); //$NON-NLS-1$

		TestTableEntry testTableEntry = new TestTableEntry(testId, tableEntry, tableItem);
		fTableItemMap.put(testId, testTableEntry);
        tableItem.setData(testTableEntry);
		// stores the given full path of the test case file
		testTableEntry.setFullPath(fullPath);
	}

	/**
	 * The given test ends.
	 */
	public void endTest(String testId, String qualifiedTestName) {
		TestTableEntry testTableEntry = (TestTableEntry) fTableItemMap.get(testId);
		if (testTableEntry == null){
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
		if (testTableEntry.isError()){
			tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testerr.gif")); //$NON-NLS-1$
		}else if (testTableEntry.isFailure()){
			tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testfail.gif")); //$NON-NLS-1$
		}else if (testTableEntry.isOk()){
			tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testok.gif")); //$NON-NLS-1$
		}else{
			tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/test.gif")); //$NON-NLS-1$
		}
    }

	/**
	 * The given test has started.
	 */
	public void startTest(String testId, String qualifiedTestName) {
		TestTableEntry testTableEntry = (TestTableEntry) fTableItemMap.get(testId);
		if (testTableEntry == null)
			return;
		
		TableItem tableItem = testTableEntry.getTableItem();
		tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testrun.gif")); //$NON-NLS-1$
	}
	
	/**
	 * The given test fails with the given details. 
	 */
	public void failureTest(String testId, String failure) {
		TestTableEntry testTableEntry = (TestTableEntry) fTableItemMap.get(testId);
		if (testTableEntry == null)
			return;
		
		testTableEntry.setStatus(TestTableEntry.FAILURE);
		testTableEntry.addFailure(failure);
		
		if (firstFailureOrError == null)
			firstFailureOrError = testTableEntry.getTableItem();
	}
	
	/**
	 * There was an error while running the given test.
	 */
	public void errorInTest(String testId, String qualifiedTestName, String[] errorDetails) {
		TestTableEntry testTableEntry = (TestTableEntry) fTableItemMap.get(testId);
		if (testTableEntry == null){
			// in case of an error before starting a single test
			// create an error entry
			newTableEntry(testId, Messages.TestRunPane_ErrorStartingTest_Entry, ""); //$NON-NLS-1$
			testTableEntry = (TestTableEntry) fTableItemMap.get(testId);
		}
		TableItem tableItem = testTableEntry.getTableItem();
		tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testerr.gif")); //$NON-NLS-1$
		testTableEntry.setErrorDetails(errorDetails);
		testTableEntry.setStatus(TestTableEntry.ERROR);
		
		if (firstFailureOrError == null)
			firstFailureOrError = tableItem;		
	}
	
	private String getTestFullPath() {
		TableItem item= getSelectedItem();
		TestTableEntry entry= (TestTableEntry) item.getData();
		return entry.getFullPath();
	}
	
	private TableItem getSelectedItem() {
		int index= fTable.getSelectionIndex();
		if (index == -1)
			return null;
		return fTable.getItem(index);
	}
	
	
	/*
	 * Inner class to store a test case.
	 */
	private class TestTableEntry{
		public static final int ERROR = -1;
		public static final int FAILURE = 0;
		public static final int UNKNOWN = 2;
		private String testId;
		private String qualifiedTestName;
		private String fullPath;
		private TableItem tableItem;
		private int status = UNKNOWN;
		private List failures = new ArrayList();
		private String[] errorDetails = EMPTY_STRING_ARRAY;
		
		TestTableEntry(String testId, String qualifiedTestName, TableItem tableItem){
			this.testId = testId;
			this.qualifiedTestName = qualifiedTestName;
			this.tableItem = tableItem;
		}
		public String getTestId() {
			return testId;
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
		public boolean isError(){
			return ERROR == status;
		}
		public boolean isFailure(){
			return FAILURE == status;
		}
		public boolean isOk(){
			return status != ERROR && status != FAILURE;
		}
		public void addFailure(String failure){
			failures.add(failure);
		}
		public List getFailureDetails(){
			return failures;
		}
		public String[] getErrorDetails() {
			return errorDetails;
		}
		public void setErrorDetails(String[] errorDetails) {
			this.errorDetails = errorDetails;
		}
	}

    // TODO Joerg: synchronize test runner
    public void checkMissingEntries() {
        if (missingTestEntries.size() == 0)
            return;
        synchronized (missingTestEntries) {
            for (Iterator iter = missingTestEntries.iterator(); iter.hasNext();) {
                String element = (String )iter.next();
                TestTableEntry testTableEntry = (TestTableEntry) fTableItemMap.get(element);
                if (testTableEntry == null){
                    missingTestEntries.add(element);
                    return;
                }
                updateTestEntryStatusImage(testTableEntry);
            }
        }
    }
}
