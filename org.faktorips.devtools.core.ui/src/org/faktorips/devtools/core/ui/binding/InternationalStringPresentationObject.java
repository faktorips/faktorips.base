/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.binding;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ILocalizedString;

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
        ILocalizedString localizedString = internationalString.get(locale);
        if (localizedString != null) {
            text = localizedString.getValue();
        } else {
            text = StringUtils.EMPTY;
        }
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
