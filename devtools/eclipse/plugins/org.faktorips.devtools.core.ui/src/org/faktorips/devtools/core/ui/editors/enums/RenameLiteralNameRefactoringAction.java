/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enums;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsProcessorBasedRefactoring;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRenameRefactoringWizard;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.util.ArgumentCheck;

/**
 * This action is used by the {@link EnumValuesSection} for renaming
 * {@link IEnumLiteralNameAttributeValue}s by refactoring.
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 3.0
 */
public class RenameLiteralNameRefactoringAction extends Action {

    private TableViewer tableViewer;

    /**
     * @param tableViewer The table viewer linking the table widget with the model data.
     * 
     * @throws NullPointerException If <code>tableViewer</code> is <code>null</code>.
     */
    public RenameLiteralNameRefactoringAction(TableViewer tableViewer) {
        super();
        ArgumentCheck.notNull(tableViewer);

        this.tableViewer = tableViewer;

        setText(Messages.EnumValuesSection_labelRenameLiteralName);
    }

    @Override
    public void run() {
        IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
        if (selection == null) {
            return;
        }

        IEnumValue enumValue = (IEnumValue)selection.getFirstElement();
        IEnumLiteralNameAttributeValue enumLiteralNameAttributeValue = enumValue.getEnumLiteralNameAttributeValue();

        IIpsProcessorBasedRefactoring ipsRenameRefactoring = IpsPlugin.getIpsRefactoringFactory()
                .createRenameRefactoring(enumLiteralNameAttributeValue);
        Shell shell = Display.getDefault().getActiveShell();
        IpsRefactoringOperation refactoringOperation = new IpsRefactoringOperation(ipsRenameRefactoring, shell);
        refactoringOperation.runWizardInteraction(new IpsRenameRefactoringWizard(ipsRenameRefactoring));

        tableViewer.refresh();
    }

}
