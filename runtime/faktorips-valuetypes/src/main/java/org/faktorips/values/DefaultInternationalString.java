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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import edu.umd.cs.findbugs.annotations.CheckForNull;

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
            localizedString = localizedStringMap.get(Locale.of(locale.getLanguage()));
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
        if ((obj == null) || !(obj instanceof DefaultInternationalString other)
                || !Objects.equals(defaultLocale, other.defaultLocale)) {
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

    /**
     * Returns a new {@link DefaultInternationalString} with the given locale and value added or
     * updated.
     * 
     * @param locale the new/updated locale
     * @param value the new/updated value for the given locale
     * @return the updated {@link DefaultInternationalString}
     */
    public DefaultInternationalString with(Locale locale, String value) {
        List<LocalizedString> updated = new ArrayList<>(getLocalizedStrings());
        updated.removeIf(ls -> ls.getLocale().equals(locale));
        updated.add(new LocalizedString(locale, value));
        return new DefaultInternationalString(updated, defaultLocale);
    }

    /**
     * Returns an updated {@link DefaultInternationalString} that includes the given
     * {@link LocalizedString}.
     * <p>
     * If {@code description} is {@code null}, a new {@link DefaultInternationalString} is created.
     * If it's already a {@link DefaultInternationalString}, the entry for the same locale is
     * replaced or added. If it's another type, an {@link UnsupportedOperationException} is thrown.
     *
     * @param description the original international string, may be {@code null}
     * @param locale the new/updated locale
     * @param value the new/updated value for the given locale
     * @return the updated {@link DefaultInternationalString}
     * @throws UnsupportedOperationException if the description is not a
     *             {@link DefaultInternationalString}
     */
    public static DefaultInternationalString updateWith(@CheckForNull InternationalString description,
            Locale locale,
            String value) {

        if (description == null) {
            return new DefaultInternationalString(List.of(new LocalizedString(locale, value)), locale);
        }

        if (description instanceof DefaultInternationalString defaultInternationString) {
            return defaultInternationString.with(locale, value);
        }

        throw new UnsupportedOperationException(
                "Cannot modify description: current instance is not a modifiable DefaultInternationalString.");

    }
}
