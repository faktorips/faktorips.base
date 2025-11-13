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

import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.InternationalStringControl;
import org.faktorips.devtools.core.ui.controls.InternationalStringDialogHandler;
import org.faktorips.devtools.core.ui.controls.tableedit.FormattedCellEditingSupport;
import org.faktorips.devtools.core.ui.table.InternationalStringCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.values.LocalizedString;

public class EnumInternationalStringEditingSupport extends FormattedCellEditingSupport<IEnumValue, LocalizedString> {

    private final EnumInternationalStringCellModifier elementModifier;
    private final UIToolkit toolkit;

    public EnumInternationalStringEditingSupport(TableViewer viewer, UIToolkit toolkit,
            EnumInternationalStringCellModifier elementModifier, EditCondition editCondition) {
        super(viewer, elementModifier, editCondition);
        this.toolkit = toolkit;
        this.elementModifier = elementModifier;
    }

    @Override
    public TableViewer getViewer() {
        return (TableViewer)super.getViewer();
    }

    @Override
    public String getFormattedValue(IEnumValue element) {
        return elementModifier.getValue(element).getValue();
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(IEnumValue element) {
        InternationalStringDialogHandler handler = elementModifier.getDialogHandler(getViewer().getTable().getShell(),
                element);
        InternationalStringControl control = new InternationalStringControl(getViewer().getTable(), toolkit, handler);
        control.setHeightHint(getViewer().getTable().getItemHeight());
        bindTableRefresh(element);
        return new InternationalStringCellEditor(control);
    }

    private void bindTableRefresh(final IEnumValue enumValue) {
        final org.faktorips.devtools.model.IIpsModel ipsModel = org.faktorips.devtools.model.IIpsModel.get();
        final org.faktorips.devtools.model.ContentsChangeListener contentChangeListener = event -> {
            if (event.isAffected(enumValue)) {
                getViewer().refresh();
            }
        };
        ipsModel.addChangeListener(contentChangeListener);
        getViewer().getTable().addDisposeListener($ -> ipsModel.removeChangeListener(contentChangeListener));
    }

    @Override
    protected void setValue(Object element, Object value) {
        super.setValue(element, value);
        getViewer().refresh(element, true);
    }

}
