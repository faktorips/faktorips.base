/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * A localized string represents a string in a specified language. This object is immutable. Two
 * {@link LocalizedString}s containing the same text for the same locale are equal.
 * 
 * @author dirmeier
 */
public class LocalizedString implements Serializable {

    private static final long serialVersionUID = -1854355695939887678L;

    private final Locale locale;

    private final String value;

    public LocalizedString(Locale locale, String value) {
        this.locale = locale;
        this.value = value;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return locale + " = " + value; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        LocalizedString other = (LocalizedString)obj;
        return Objects.equals(locale, other.locale)
                && Objects.equals(value, other.value);
    }

}
