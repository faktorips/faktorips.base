/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.ui.controller.fields.MultilingualEditField;
import org.faktorips.devtools.core.ui.controls.MultilingualValueAttributeControl;

/**
 * A cell editor using the {@link MultilingualValueAttributeControl} to enter values in different
 * languages.
 */
public class MultilingualTextCellEditor extends IpsCellEditor {
    private final MultilingualValueAttributeControl textControl;
    private final MultilingualEditField textField;
    private final TableViewer tableViewer;

    public MultilingualTextCellEditor(MultilingualValueAttributeControl textControl, TableViewer tableViewer) {
        super(textControl);
        this.textControl = textControl;
        textField = new MultilingualEditField(textControl);
        this.tableViewer = tableViewer;
    }

    public MultilingualEditField getTextField() {
        return textField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doGetValue() {
        return textField.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetFocus() {
        if (textControl.getTextControl().getEditable()) {
            textControl.getTextControl().selectAll();
        }
        textControl.setFocus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetValue(Object value) {
        if (value instanceof IInternationalString) {
            textField.setValue(((IInternationalString)value).get(IpsPlugin.getDefault().getUsedLanguagePackLocale()));
        } else if (value instanceof String) {
            textField.setText((String)value);
        }
    }

    @Override
    public boolean isMappedValue() {
        return false;
    }

    @Override
    protected void fireApplyEditorValue() {
        super.fireApplyEditorValue();
        tableViewer.refresh();
    }
}
