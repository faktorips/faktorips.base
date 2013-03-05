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
