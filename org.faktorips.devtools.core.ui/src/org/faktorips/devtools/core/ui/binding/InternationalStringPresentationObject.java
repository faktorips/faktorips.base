/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.binding;

import java.util.Locale;

import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.values.LocalizedString;

/**
 * Use this presentation model object to handle {@link IInternationalString} in UI. For example you
 * could use a single text field to enter the localized text and have a combo box to select the
 * locale of the text. Binding the combo with the locale of this presentation model object and the
 * text with the text property would automatically switch the text when changing the locale in
 * combo. The {@link IInternationalString} set by constructor is also updated automatically.
 * 
 * 
 * @author dirmeier
 */
public class InternationalStringPresentationObject extends PresentationModelObject {

    public static final String PROPERTY_LOCALE = "locale"; //$NON-NLS-1$

    public static final String PROPERTY_TEXT = "text"; //$NON-NLS-1$

    private Locale locale;

    private String text;

    private final IInternationalString internationalString;

    /**
     * The constructor getting the {@link IInternationalString} that is managed by this
     * {@link InternationalStringPresentationObject}
     * 
     * @param internationalString the {@link IInternationalString} you want to bind
     */
    public InternationalStringPresentationObject(IInternationalString internationalString) {
        this.internationalString = internationalString;
    }

    /**
     * @param locale The locale to set.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
        LocalizedString localizedString = internationalString.get(locale);
        text = localizedString.getValue();
        notifyListeners();
    }

    /**
     * @return Returns the locale.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @param text The text to set.
     */
    public void setText(String text) {
        this.text = text;
        internationalString.add(new LocalizedString(locale, text));
        notifyListeners();
    }

    /**
     * @return Returns the text.
     */
    public String getText() {
        return text;
    }

    /**
     * @return Returns the internationalString.
     */
    public IInternationalString getInternationalString() {
        return internationalString;
    }

}
