/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.wizards.productcmpt.OpenNewProductCmptWizardAction;

/**
 * Open the new product component wizard.
 * 
 * @author Thorsten Guenther
 */
public class NewProductComponentAction extends Action {

	private IWorkbenchWindow window;
	
	public NewProductComponentAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setText(Messages.NewProductComponentAction_name);
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("NewProductCmptWizard.gif")); //$NON-NLS-1$
	}

	/** 
	 * {@inheritDoc}
	 */
	public void run() {
		OpenNewProductCmptWizardAction o = new OpenNewProductCmptWizardAction();
		o.init(window);
		o.run(this);
	}
}
