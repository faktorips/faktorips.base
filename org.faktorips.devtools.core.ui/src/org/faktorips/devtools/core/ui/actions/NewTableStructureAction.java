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
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.tablestructure.OpenNewTableStructureWizardAction;

/**
 * Opens the wizard for creating a new TableStructure.
 * 
 * @author Joerg Ortmann
 */
public class NewTableStructureAction extends Action {

    private IWorkbenchWindow window;

    public NewTableStructureAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setText(Messages.NewTableStructureAction_name);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewTableStructureWizard.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        IWorkbenchWindowActionDelegate openAction = new OpenNewTableStructureWizardAction();
        openAction.init(window);
        openAction.run(this);
    }

}
