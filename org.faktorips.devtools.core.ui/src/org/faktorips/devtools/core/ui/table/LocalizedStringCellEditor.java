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

import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.ui.controller.fields.TextField;

/**
 * A cell editor using for {@link ILocalizedString localized strings} to enter a value in the
 * specified language.
 */
public class LocalizedStringCellEditor extends IpsCellEditor {
    private final Text textControl;
    private final TextField textField;

    public LocalizedStringCellEditor(Text textControl) {
        super(textControl);
        this.textControl = textControl;
        textField = new TextField(textControl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doGetValue() {
        return textField.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetFocus() {
        textControl.selectAll();
        textControl.setFocus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetValue(Object value) {
        if (value instanceof LocalizedString) {
            textField.setText(((LocalizedString)value).getValue());
        } else if (value instanceof String) {
            textField.setText((String)value);
        }
    }

    @Override
    public boolean isMappedValue() {
        return false;
    }
}
