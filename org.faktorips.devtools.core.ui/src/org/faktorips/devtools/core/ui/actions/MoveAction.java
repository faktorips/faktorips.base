/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.MoveResourceAction;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;

/**
 * Opens the move wizard to allow the user to move a product component or package fragment.
 * 
 * @author Thorsten Guenther
 */
public class MoveAction extends IpsAction implements IShellProvider {

    private Shell shell;

    public MoveAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
        setText(Messages.MoveAction_name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        if (selected instanceof IIpsElement) {
            MoveWizard move = new MoveWizard(selection, MoveWizard.OPERATION_MOVE);
            WizardDialog wd = new WizardDialog(shell, move);
            wd.open();
        } else if (selected instanceof IResource) {
            MoveResourceAction action = new MoveResourceAction(this);
            action.selectionChanged(selection);
            action.run();
        }
    }

    /**
     * Implementation of {@link IShellProvider#getShell()}.
     */
    public Shell getShell() {
        return shell;
    }

}
