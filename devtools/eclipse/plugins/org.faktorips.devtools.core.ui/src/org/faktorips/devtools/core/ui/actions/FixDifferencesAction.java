/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.fixdifferences.OpenFixDifferencesToModelWizardAction;

/**
 * 
 * @author Daniel Hohenberger
 */
public class FixDifferencesAction extends Action {
    private IWorkbenchWindow window;
    private IStructuredSelection selection;

    public FixDifferencesAction(IWorkbenchWindow window, IStructuredSelection selection) {
        super();
        this.window = window;
        this.selection = selection;
        setText(Messages.FixDifferencesAction_text);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("FixDifferencesToModel.gif")); //$NON-NLS-1$
        setEnabled(true);
    }

    @Override
    public void run() {
        OpenFixDifferencesToModelWizardAction action = new OpenFixDifferencesToModelWizardAction();
        action.init(window);
        action.selectionChanged(this, selection);
        action.run(this);
    }

}
