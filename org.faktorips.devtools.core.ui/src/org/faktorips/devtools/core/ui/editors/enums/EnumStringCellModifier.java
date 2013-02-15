/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.controls.tableedit.IElementModifier;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;

/**
 * The cell modifier for string values in {@link IEnumValue enum values}
 */
public class EnumStringCellModifier implements IElementModifier<IEnumValue, String> {

    private final int columnIndex;

    private final Shell shell;

    public EnumStringCellModifier(Shell shell, int columnIndex) {
        this.shell = shell;
        this.columnIndex = columnIndex;
    }

    @Override
    public String getValue(IEnumValue element) {
        return element.getEnumAttributeValues().get(columnIndex).getValue().getContentAsString();
    }

    @Override
    public void setValue(IEnumValue element, String value) {
        IEnumAttributeValue enumAttributeValue = element.getEnumAttributeValues().get(columnIndex);
        if (enumAttributeValue.isEnumLiteralNameAttributeValue()) {
            if (IpsPlugin.getDefault().getIpsPreferences().isRefactoringModeDirect()) {
                applyRenameLiteralNameRefactoring(value, (IEnumLiteralNameAttributeValue)enumAttributeValue);
            } else {
                enumAttributeValue.setValue(ValueFactory.createStringValue(value));
            }
        } else {
            enumAttributeValue.setValue(ValueFactory.createStringValue(value));
        }
    }

    private void applyRenameLiteralNameRefactoring(String newName,
            IEnumLiteralNameAttributeValue enumLiteralNameAttributeValue) {

        IIpsRefactoring ipsRenameRefactoring = IpsPlugin.getIpsRefactoringFactory().createRenameRefactoring(
                enumLiteralNameAttributeValue, newName, null, false);
        IpsRefactoringOperation refactorOp = new IpsRefactoringOperation(ipsRenameRefactoring, shell);
        refactorOp.runDirectExecution();
    }
}