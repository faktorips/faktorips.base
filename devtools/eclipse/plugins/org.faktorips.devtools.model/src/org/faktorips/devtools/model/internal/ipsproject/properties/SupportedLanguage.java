/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.properties;

import java.util.Locale;
import java.util.Objects;

import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link ISupportedLanguage}.
 * 
 * @see ISupportedLanguage
 * 
 * @author Alexander Weickmann
 */
public class SupportedLanguage implements ISupportedLanguage {

    private Locale locale;

    private boolean defaultLanguage;

    public SupportedLanguage() {
        super();
    }

    public SupportedLanguage(Locale locale) {
        this(locale, false);
    }

    public SupportedLanguage(Locale locale, boolean defaultLanguage) {
        this.locale = locale;
        this.defaultLanguage = defaultLanguage;
    }

    @Override
    public String getLanguageName() {
        return getLocale().getDisplayLanguage();
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public boolean isDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Sets whether this supported language is also the "default language".
     * 
     * @param defaultLanguage Flag indicating whether this language is the "default language".
     */
    public void setDefaultLanguage(boolean defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        SupportedLanguage other = (SupportedLanguage)obj;
        return Objects.equals(locale, other.locale);
    }

    @Override
    public void initFromXml(Element element) {
        locale = Locale.of(element.getAttribute("locale")); //$NON-NLS-1$
        defaultLanguage = Boolean.parseBoolean(element.getAttribute("defaultLanguage")); //$NON-NLS-1$
    }

    @Override
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_TAG_NAME);
        element.setAttribute("locale", locale.getLanguage()); //$NON-NLS-1$
        if (defaultLanguage) {
            element.setAttribute("defaultLanguage", String.valueOf(defaultLanguage)); //$NON-NLS-1$
        }
        return element;
    }

    @Override
    public String toString() {
        return locale + (defaultLanguage ? "*" : ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
