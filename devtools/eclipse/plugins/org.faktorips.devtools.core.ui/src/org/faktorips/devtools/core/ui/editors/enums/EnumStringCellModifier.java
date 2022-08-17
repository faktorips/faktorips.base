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

import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.controls.tableedit.IElementModifier;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.value.ValueFactory;

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
        if (enumAttributeValue.isEnumLiteralNameAttributeValue()
                && IpsPlugin.getDefault().getIpsPreferences().isRefactoringModeDirect()) {
            applyRenameLiteralNameRefactoring(value, (IEnumLiteralNameAttributeValue)enumAttributeValue);
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
