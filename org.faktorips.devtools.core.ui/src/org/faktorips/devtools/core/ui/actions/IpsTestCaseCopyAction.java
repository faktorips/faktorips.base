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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.testcasecopy.TestCaseCopyWizard;
import org.faktorips.devtools.model.testcase.ITestCase;

public class IpsTestCaseCopyAction extends IpsAction {

    private Shell shell;

    public IpsTestCaseCopyAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
        setText(Messages.IpsTestCaseCopyAction_name);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewTestCaseCopyWizard.gif")); //$NON-NLS-1$
    }

    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        if (!(selected instanceof ITestCase)) {
            return;
        }
        WizardDialog wd = new WizardDialog(shell, new TestCaseCopyWizard(((ITestCase)selected)));
        wd.setBlockOnOpen(true);
        wd.open();
    }

}
