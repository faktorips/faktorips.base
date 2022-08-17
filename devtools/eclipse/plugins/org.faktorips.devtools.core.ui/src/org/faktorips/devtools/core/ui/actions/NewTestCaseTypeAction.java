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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.testcasetype.OpenNewTestCaseTypeWizardAction;

/**
 * Opens the wizard for creating a new TestCaseType.
 * 
 * @author Joerg Ortmann
 */
public class NewTestCaseTypeAction extends Action {

    private IWorkbenchWindow window;

    public NewTestCaseTypeAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setText(Messages.NewTestCaseTypeAction_name);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewTestCaseType.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        IWorkbenchWindowActionDelegate openAction = new OpenNewTestCaseTypeWizardAction();
        openAction.init(window);
        openAction.run(this);
    }

}
