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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.ui.controller.EditField;

/**
 * {@link EditField} for editing multilingual strings. The text edited by the text control will
 * always return an {@link ILocalizedString} in the locale given to this class.
 */
public class LocalizedStringEditField extends AbstractTextField<ILocalizedString> {

    private final Locale localeOfEditField;

    public LocalizedStringEditField(Text control, Locale localeOfEditField) {
        super(control);
        this.localeOfEditField = localeOfEditField;
    }

    @Override
    public ILocalizedString parseContent() {
        String text = StringValueEditField.prepareObjectForGet(getTextControl().getText(),
                supportsNullStringRepresentation());
        return new LocalizedString(localeOfEditField, text);
    }

    @Override
    public void setValue(ILocalizedString newValue) {
        if (newValue == null) {
            if (supportsNullStringRepresentation()) {
                setText(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
                return;
            } else {
                setText(StringUtils.EMPTY);
                return;
            }
        }
        setText(StringValueEditField.prepareObjectForSet(newValue.getValue(), supportsNullStringRepresentation()));
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
