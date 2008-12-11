/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.bf.OpenNewBFWizardAction;

/**
 * Opens the wizard for creating a new BusinessFunction.
 * 
 * @author Peter Erzberger
 */
public class NewBusinessFunctionAction extends Action {
    private IWorkbenchWindow window;

    public NewBusinessFunctionAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setText(Messages.NewBusinessFunctionAction_title);
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/NewBusinessFunction.gif")); //$NON-NLS-1$
    }

    public void run() {
        OpenNewBFWizardAction action = new OpenNewBFWizardAction();
        action.init(window);
        action.run(this);
    }

}
