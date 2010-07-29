/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.enums;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRenameRefactoringWizard;
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
     * @throws NullPointerException If <tt>tableViewer</tt> is <tt>null</tt>.
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
        ProcessorBasedRefactoring refactoring = enumLiteralNameAttributeValue.getRenameRefactoring();
        Shell shell = Display.getDefault().getActiveShell();
        IpsRefactoringOperation refactoringOperation = new IpsRefactoringOperation(refactoring, shell);
        refactoringOperation.runWizardInteraction(new IpsRenameRefactoringWizard(refactoring,
                enumLiteralNameAttributeValue));
    }

}
