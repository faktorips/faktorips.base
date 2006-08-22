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
import org.faktorips.devtools.core.ui.wizards.policycmpttype.OpenNewPcTypeWizardAction;

/**
 * Opens the wizard for creating a new PolicyCmptType.
 * 
 * @author Stefan Widmaier
 */
public class NewPolicyComponentTypeAction extends Action {
	private IWorkbenchWindow window;
	
	public NewPolicyComponentTypeAction(IWorkbenchWindow window){
		super();
		this.window = window;
		setText(Messages.NewPolicyComponentTypeAction_name);
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("NewPolicyCmptTypeWizard.gif")); //$NON-NLS-1$
	}
	
	public void run(){
		OpenNewPcTypeWizardAction openAction = new OpenNewPcTypeWizardAction();
		openAction.init(window);
		openAction.run(this);
	}
	
}
