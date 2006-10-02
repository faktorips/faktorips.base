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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Test runner failure pane shows all errors or failures in a table.
 * 
 * @author Joerg Ortmann
 */
public class FailurePane {
    private static final String TEST_ERROR_MESSAGE_INDICATOR = ">>>"; //$NON-NLS-1$
    private static final String TEST_ERROR_STACK_INDICATOR = "---"; //$NON-NLS-1$
    
	private Table fTable;
	
    // Action
    private Action fShowStackTraceAction;

    // Indicates if the stacktrace elemets will be shown or not
    private boolean fShowStackTrace = false;
    
    // Contains the last reported failures in this pane
    private String[] fLastFailures = new String[0];
    
    /*
     * Action class to filter the stack trace elements
     */
    private class ShowStackTraceAction extends Action {
        public ShowStackTraceAction() {
            super("", AS_RADIO_BUTTON); //$NON-NLS-1$
            setText(Messages.IpsTestRunnerViewPart_Action_ShowStackTrace); 
            setToolTipText(Messages.IpsTestRunnerViewPart_Action_ShowStackTraceToolTip); 
            setDisabledImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("dlcl16/cfilter.gif")); //$NON-NLS-1$
            setHoverImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("elcl16/cfilter.gif")); //$NON-NLS-1$
            setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("elcl16/cfilter.gif")); //$NON-NLS-1$
            setEnabled(fShowStackTrace);
        }
        public void run(){
            fShowStackTrace = ! fShowStackTrace;
            fShowStackTraceAction.setChecked(fShowStackTrace);
            showFailureDetails(fLastFailures);
        }
    }
    
	public FailurePane(Composite parent, ToolBar toolBar, final IpsTestRunnerViewPart viewPart) {
		fTable = new Table(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        
        fTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = fTable.getSelection();
                if(items.length>0){
                    viewPart.setStatusBarMessage(items[0].getText());
                }
            }
        });
        
        // fill the failure trace viewer toolbar
        ToolBarManager failureToolBarmanager= new ToolBarManager(toolBar);
        fShowStackTraceAction = new ShowStackTraceAction();
        failureToolBarmanager.add(fShowStackTraceAction);    
        failureToolBarmanager.update(true);
	}
    
	/**
	 * Returns the composite used to present the failures.
	 */
	public Composite getComposite(){
		return fTable;
	}
    
	/**
	 * Inserts the given test case failure details in the table. One row for each given failure.
     * If showStackTrace is <code>false</code> and the given failure details contains stack trace elements, then
     * these elements will be hidden.
	 */
	public void showFailureDetails(String[] testCaseFailures) {
        fShowStackTraceAction.setEnabled(false);
        fLastFailures = testCaseFailures;
        fTable.removeAll();
		for (int i = 0; i < testCaseFailures.length; i++) {
            if (testCaseFailures[i].startsWith(TEST_ERROR_MESSAGE_INDICATOR)){
                String text = testCaseFailures[i].substring(TEST_ERROR_MESSAGE_INDICATOR.length());
                if (text.trim().length()>0){
                    TableItem tableItem = new TableItem(fTable, SWT.NONE);
                    tableItem.setText(text);
                    tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/stkfrm_msg.gif")); //$NON-NLS-1$
                }
            } else if (testCaseFailures[i].startsWith(TEST_ERROR_STACK_INDICATOR)) {
                fShowStackTraceAction.setEnabled(true);
                if (fShowStackTrace){
                    TableItem tableItem = new TableItem(fTable, SWT.NONE);
                    tableItem.setText(testCaseFailures[i].substring(TEST_ERROR_STACK_INDICATOR.length()));
                    tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/stkfrm_obj.gif")); //$NON-NLS-1$
                }
            } else {
                if (testCaseFailures[i].trim().length()>0){
                    TableItem tableItem = new TableItem(fTable, SWT.NONE);
                    tableItem.setText(testCaseFailures[i]);
                    tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testfail.gif")); //$NON-NLS-1$
                }
            }
		}
	}

	/**
	 * A new test run will be started.
	 */
	public void aboutToStart() {
		fTable.removeAll();
	}    
}
