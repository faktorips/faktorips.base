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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * 
 * @author Joerg Ortmann
 */
public class FailurePane {

	private Table fTable;
	
	public FailurePane(Composite parent) {
		fTable = new Table(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	/**
	 * Returns the composite used to present the failures.
	 */
	public Composite getComposite(){
		return fTable;
	}

	/**
	 * Inserts the given test case failure details in the table. One row for each given failure.
	 */
	public void showFailureDetails(String[] testCaseFailures) {
		fTable.removeAll();
		for (int i = 0; i < testCaseFailures.length; i++) {
			TableItem tableItem = new TableItem(fTable, SWT.NONE);
			tableItem.setText(testCaseFailures[i]);
			tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/stkfrm_obj.gif")); //$NON-NLS-1$
		}
	}

	/**
	 * A new test run will be started.
	 */
	public void aboutToStart() {
		fTable.removeAll();
	}
}
