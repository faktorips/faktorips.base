/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.Locale;

import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controls.InternationalStringControl;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.LocalizedString;

/**
 * {@link EditField} for editing multilingual strings. The text edited by the text control will
 * always return a {@link LocalizedString} in the locale given to this class.
 */
public class LocalizedStringEditField extends AbstractTextButtonField<LocalizedString> {

    private Locale localeOfEditField;

    public LocalizedStringEditField(InternationalStringControl control) {
        super(control);
    }

    private Text getTextControl() {
        return getControl().getTextControl();
    }

    @Override
    public LocalizedString parseContent() {
        String text = getTextControl().getText();
        return new LocalizedString(localeOfEditField, text);
    }

    @Override
    public void setValue(LocalizedString newValue) {
        if (newValue == null) {
            setText(IpsStringUtils.EMPTY);
            return;
        }
        localeOfEditField = newValue.getLocale();
        setText(newValue.getValue());
    }

    @Override
    public boolean supportsNullStringRepresentation() {
        return false;
    }

}
