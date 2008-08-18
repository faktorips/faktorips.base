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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.wizards.testcasecopy.TestCaseCopyWizard;

public class IpsTestCaseCopyAction extends IpsAction {
    private Shell shell;

    public IpsTestCaseCopyAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
        setText(Messages.IpsTestCaseCopyAction_name);
        setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("NewTestCaseCopyWizard.gif")); //$NON-NLS-1$
    }

    public void run(IStructuredSelection selection) {
        Object selected  = selection.getFirstElement();
        if (!(selected instanceof ITestCase)) {
            return;
        }
        WizardDialog wd = new WizardDialog(shell, new TestCaseCopyWizard(((ITestCase)selected)));
        wd.setBlockOnOpen(true);
        wd.open();
    }
}
