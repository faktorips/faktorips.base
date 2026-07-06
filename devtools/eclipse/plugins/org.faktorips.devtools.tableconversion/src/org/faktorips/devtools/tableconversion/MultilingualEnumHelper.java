/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.SequencedMap;
import java.util.SequencedSet;

import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.LocalizedString;

/**
 * Shared utility methods for multilingual enum import/export operations.
 */
public final class MultilingualEnumHelper {

    private MultilingualEnumHelper() {
    }

    /**
     * Generates a column-to-locale mapping based on enum attributes and supported locales. Used as
     * fallback when no locale headers are present in the import file.
     */
    public static SequencedMap<Integer, SequencedMap<Integer, Locale>> generateLocalesByColumn(
            List<IEnumAttribute> enumAttributes, SequencedSet<Locale> locales) {
        LinkedHashMap<Integer, SequencedMap<Integer, Locale>> localesByColumn = new LinkedHashMap<>();
        int columnIndex = 0;
        for (IEnumAttribute attribute : enumAttributes) {
            if (attribute.isMultilingual()) {
                LinkedHashMap<Integer, Locale> localeColumns = new LinkedHashMap<>();
                int firstColumn = columnIndex;
                for (Locale locale : locales) {
                    localeColumns.put(columnIndex++, locale);
                }
                localesByColumn.put(firstColumn, localeColumns);
            } else {
                columnIndex++;
            }
        }
        return localesByColumn;
    }

    /**
     * Returns the locale tag string as written in export headers/cells, e.g. {@code "[de]"} or
     * {@code "[en-US]"}.
     */
    public static String formatLocaleTag(Locale locale) {
        return "[" + locale.toLanguageTag() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Sets an international string value on an enum attribute from a list of localized strings.
     * Filters out entries with null values before constructing the international string.
     */
    public static void setInternationalStringValue(IEnumAttributeValue enumAttribute,
            List<LocalizedString> localizedStrings) {
        List<LocalizedString> nonNullStrings = localizedStrings.stream()
                .filter(ls -> ls != null && !IpsStringUtils.isBlank(ls.getValue()))
                .toList();

        if (nonNullStrings.isEmpty()) {
            enumAttribute.setValue(null);
            return;
        }

        IValue<?> internationalStringValue = ValueFactory.createValue(true, null);
        IInternationalString content = (IInternationalString)internationalStringValue.getContent();

        for (LocalizedString localizedString : nonNullStrings) {
            content.add(localizedString);
        }

        enumAttribute.setValue(internationalStringValue);
    }

}
