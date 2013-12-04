/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.values;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;

/**
 * An {@link InternationalString} could be used for string properties that could be translated in
 * different languages. The {@link InternationalString} consists of a set of {@link LocalizedString
 * localized strings}.
 */
public class InternationalString implements IInternationalString {

    private static final long serialVersionUID = -4599838166284499045L;

    private final Map<Locale, LocalizedString> localizedStringMap;

    /**
     * Creates a new {@link InternationalString} with the given {@link LocalizedString localized
     * strings}.
     * 
     * @param localizedStrings the localized strings making up this InternationalString.
     */
    public InternationalString(Collection<LocalizedString> localizedStrings) {
        Map<Locale, LocalizedString> initialMap = new LinkedHashMap<Locale, LocalizedString>();
        for (LocalizedString localizedString : localizedStrings) {
            initialMap.put(localizedString.getLocale(), localizedString);
        }
        localizedStringMap = Collections.unmodifiableMap(initialMap);
    }

    /**
     * {@inheritDoc}
     */
    public String get(Locale locale) {
        LocalizedString localizedString = localizedStringMap.get(locale);
        return localizedString == null ? null : localizedString.getValue();
    }

    /**
     * Returning all values of this {@link InternationalString} ordered by insertion. Note that a
     * copy of the original values is returned. Modifying the returned collection will not modify
     * this InternationalString.
     */
    public Collection<LocalizedString> getLocalizedStrings() {
        return new LinkedHashSet<LocalizedString>(localizedStringMap.values());
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 1;
        result = prime * result + ((localizedStringMap == null) ? 0 : localizedStringMap.hashCode());
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
        if (!(obj instanceof InternationalString)) {
            return false;
        }
        InternationalString other = (InternationalString)obj;
        if (localizedStringMap == null) {
            if (other.localizedStringMap != null) {
                return false;
            }
        } else {
            return equalLocalizedMapValues(other.getLocalizedStrings());
        }
        return true;
    }

    private boolean equalLocalizedMapValues(Collection<LocalizedString> otherLocalizedStringMapValues) {
        Collection<LocalizedString> values = getLocalizedStrings();
        if (otherLocalizedStringMapValues == null) {
            return false;
        }
        if (values.size() != otherLocalizedStringMapValues.size()) {
            return false;
        }
        for (LocalizedString localizedString : values) {
            if (!(otherLocalizedStringMapValues.contains(localizedString))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("InternationalString ["); //$NON-NLS-1$
        if (localizedStringMap != null) {
            for (LocalizedString localizedString : getLocalizedStrings()) {
                builder.append(localizedString.toString());
                builder.append(" "); //$NON-NLS-1$
            }
        }
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }
}
