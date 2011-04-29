/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.bf.OpenNewBFWizardAction;

/**
 * Opens the wizard for creating a new business function.
 * 
 * @author Peter Erzberger
 */
public class NewBusinessFunctionAction extends Action {

    private IWorkbenchWindow window;

    /**
     * Creates a new <code>NewBusinessFunctionAction</code>.
     */
    public NewBusinessFunctionAction(IWorkbenchWindow window) {
        super();

        this.window = window;
        setText(Messages.NewBusinessFunctionAction_title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewBusinessFunction.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        OpenNewBFWizardAction action = new OpenNewBFWizardAction();
        action.init(window);
        action.run(this);
    }

}
