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

import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.InternationalStringControl;
import org.faktorips.devtools.core.ui.controls.InternationalStringDialogHandler;
import org.faktorips.devtools.core.ui.controls.tableedit.FormattedCellEditingSupport;
import org.faktorips.devtools.core.ui.table.InternationalStringCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;

public class EnumInternationalStringEditingSupport extends FormattedCellEditingSupport<IEnumValue, ILocalizedString> {

    private final EnumInternationalStringCellModifier elementModifier;
    private final UIToolkit toolkit;

    public EnumInternationalStringEditingSupport(TableViewer viewer, UIToolkit toolkit,
            EnumInternationalStringCellModifier elementModifier) {
        super(viewer, elementModifier);
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
        return new InternationalStringCellEditor(control);
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

}
