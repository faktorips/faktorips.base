/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Locale;

import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((locale == null) ? 0 : locale.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SupportedLanguage other = (SupportedLanguage)obj;
        if (locale == null) {
            if (other.locale != null) {
                return false;
            }
        } else if (!locale.equals(other.locale)) {
            return false;
        }
        return true;
    }

    @Override
    public void initFromXml(Element element) {
        locale = new Locale(element.getAttribute("locale")); //$NON-NLS-1$
        defaultLanguage = Boolean.valueOf(element.getAttribute("defaultLanguage")); //$NON-NLS-1$
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
