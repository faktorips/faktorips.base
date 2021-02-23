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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((locale == null) ? 0 : locale.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        LocalizedString other = (LocalizedString)obj;
        if (locale == null) {
            if (other.locale != null) {
                return false;
            }
        } else if (!locale.equals(other.locale)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
