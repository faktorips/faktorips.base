/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.values.LocalizedString;

/**
 * {@link EditField} for editing multilingual strings. The text edited by the text control will
 * always return a {@link LocalizedString} in the locale given to this class.
 */
public class LocalizedStringEditField extends AbstractTextField<LocalizedString> {

    private Locale localeOfEditField;

    public LocalizedStringEditField(Text control) {
        super(control);
    }

    @Override
    public LocalizedString parseContent() {
        String text = StringValueEditField.prepareObjectForGet(getTextControl().getText(),
                supportsNullStringRepresentation());
        return new LocalizedString(localeOfEditField, text);
    }

    @Override
    public void setValue(LocalizedString newValue) {
        if (newValue == null) {
            setText(StringUtils.EMPTY);
            return;
        }
        localeOfEditField = newValue.getLocale();
        setText(StringValueEditField.prepareObjectForSet(newValue.getValue(), supportsNullStringRepresentation()));
    }

    @Override
    public boolean supportsNullStringRepresentation() {
        return false;
    }

    @Override
    public String getText() {
        return getTextControl().getText();
    }

    @Override
    public void setText(String newText) {
        getTextControl().setText(newText);
    }

    @Override
    public void insertText(String text) {
        getTextControl().insert(text);
    }

    @Override
    public void selectAll() {
        getTextControl().selectAll();
    }

}
