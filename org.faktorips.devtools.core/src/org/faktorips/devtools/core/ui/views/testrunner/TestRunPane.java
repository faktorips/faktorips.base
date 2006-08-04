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

	private Table fTable;
	
	// Maps test Ids to the stored table items. 
	private Map fTableItemMap = new HashMap();
	
	public TestRunPane(Composite parent, final IpsTestRunnerViewPart testRunnerViewPart) {
		fTable = new Table(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		fTable.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
            	if (e.item instanceof TableItem){
	            	TableItem selectedTestCase = (TableItem) e.item;
	            	TestTableEnty testEntry = (TestTableEnty) selectedTestCase.getData();
	            	testRunnerViewPart.selectionOfTestCaseChanged((String[])testEntry.getFailureDetails().toArray(new String[0]));
            	}
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });
	}

	/**
	 * Returns the composite used to present the test runs.
	 */
	public Composite getComposite(){
		return fTable;
	}
	
	//
	// Methods to inform this pane about test run, end and failures
	//
	
	/**
	 * A new test run will be stared.
	 */
	public void aboutToStart() {
		fTable.removeAll();
		fTableItemMap = new HashMap();
	}
	
	/**
	 * The given test is about to be started.
	 */
	public void newTableEntry(String testId, String treeEntry) {
		TableItem tableItem = new TableItem(fTable, SWT.NONE);
		tableItem.setText(treeEntry);
		tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/test.gif")); //$NON-NLS-1$

		TestTableEnty testTableEntry = new TestTableEnty(testId, treeEntry, tableItem);
		fTableItemMap.put(testId, testTableEntry);
		tableItem.setData(testTableEntry);
	}

	/**
	 * The given test ends.
	 */
	public void endTest(String testId, String qualifiedTestName) {
		TestTableEnty testTableEntry = (TestTableEnty) fTableItemMap.get(testId);
		if (testTableEntry == null)
			return;
		
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
		TestTableEnty testTableEntry = (TestTableEnty) fTableItemMap.get(testId);
		if (testTableEntry == null)
			return;
		
		TableItem tableItem = testTableEntry.getTableItem();
		tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testrun.gif")); //$NON-NLS-1$
	}
	
	/**
	 * The given test fails with the given details. 
	 */
	public void failureTest(String testId, String failure) {
		TestTableEnty testTableEntry = (TestTableEnty) fTableItemMap.get(testId);
		if (testTableEntry == null)
			return;
		
		testTableEntry.setStatus(TestTableEnty.FAILURE);
		testTableEntry.addFailure(failure);
	}
	
	
	/*
	 * Inner class to store a test case.
	 */
	private class TestTableEnty{
		public static final int ERROR = -1;
		public static final int FAILURE = 0;
		public static final int UNKNOWN = 2;
		private String testId;
		private String qualifiedTestName;
		private TableItem tableItem;
		private int status = UNKNOWN;
		private List failures = new ArrayList();
		
		TestTableEnty(String testId, String qualifiedTestName, TableItem tableItem){
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
	}
}
