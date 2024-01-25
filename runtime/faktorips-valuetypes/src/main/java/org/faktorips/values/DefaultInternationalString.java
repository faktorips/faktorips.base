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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * An {@link DefaultInternationalString} could be used for string properties that could be
 * translated in different languages. The {@link DefaultInternationalString} consists of a set of
 * {@link LocalizedString localized strings}.
 */
public class DefaultInternationalString implements InternationalString {

    /**
     * An empty {@link InternationalString}.
     * 
     * @since 24.1
     **/
    public static final DefaultInternationalString EMPTY = new DefaultInternationalString(List.of(),
            Locale.ROOT);

    private static final long serialVersionUID = -4599838166284499045L;

    private final Map<Locale, LocalizedString> localizedStringMap;

    private final Locale defaultLocale;

    /**
     * Creates a new {@link DefaultInternationalString} with the given {@link LocalizedString
     * localized strings}.
     * 
     * @param localizedStrings the localized strings making up this DefaultInternationalString.
     */
    public DefaultInternationalString(Collection<LocalizedString> localizedStrings, Locale defaultLocale) {
        Map<Locale, LocalizedString> initialMap = new LinkedHashMap<>();
        for (LocalizedString localizedString : localizedStrings) {
            initialMap.put(localizedString.getLocale(), localizedString);
        }
        localizedStringMap = Collections.unmodifiableMap(initialMap);
        this.defaultLocale = defaultLocale;
    }

    @Override
    public String get(Locale locale) {
        LocalizedString localizedString = localizedStringMap.get(locale);
        if (localizedString == null && !"".equals(locale.getCountry()) && !"".equals(locale.getLanguage())) {
            localizedString = localizedStringMap.get(new Locale(locale.getLanguage()));
        }
        if (localizedString == null) {
            localizedString = localizedStringMap.get(defaultLocale);
        }
        return localizedString == null ? null : localizedString.getValue();
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Returning all values of this {@link DefaultInternationalString} ordered by insertion. Note
     * that a copy of the original values is returned. Modifying the returned collection will not
     * modify this DefaultInternationalString.
     */
    public Collection<LocalizedString> getLocalizedStrings() {
        return new LinkedHashSet<>(localizedStringMap.values());
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultLocale, localizedStringMap);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof DefaultInternationalString)) {
            return false;
        }
        DefaultInternationalString other = (DefaultInternationalString)obj;
        if (!Objects.equals(defaultLocale, other.defaultLocale)) {
            return false;
        }
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
        if ((otherLocalizedStringMapValues == null) || (values.size() != otherLocalizedStringMapValues.size())) {
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
        builder.append("InternationalString defaultLocale="); //$NON-NLS-1$
        builder.append(defaultLocale);
        builder.append(" ["); //$NON-NLS-1$
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
