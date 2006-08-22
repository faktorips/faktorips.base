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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.wizards.testcase.OpenNewTestCaseWizardAction;

/**
 * Opens the wizard for creating a new TestCase.
 * 
 * @author Joerg Ortmann
 */
public class NewTestCaseAction extends Action {
	private IWorkbenchWindow window;
	
	public NewTestCaseAction(IWorkbenchWindow window){
		super();
		this.window = window;
		setText(Messages.NewTestCaseAction_name);
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("NewTestCase.gif")); //$NON-NLS-1$
	}
	
	public void run(){
		OpenNewTestCaseWizardAction openAction = new OpenNewTestCaseWizardAction();
		openAction.init(window);
		openAction.run(this);
	}
}
