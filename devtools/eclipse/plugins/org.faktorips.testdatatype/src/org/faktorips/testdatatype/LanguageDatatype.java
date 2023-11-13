/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.testdatatype;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.MessageLists;

/**
 * Provides language ISO code Strings as a datatype.
 *
 * Only implements {@link EnumDatatype} without extending {@link GenericValueDatatype} to test
 * FIPS-10840.
 */
public class LanguageDatatype implements EnumDatatype {

    @Override
    public String[] getAllValueIds(boolean includeNull) {
        return Locale.getISOLanguages();
    }

    @Override
    public boolean isParsable(String languageCode) {
        return Arrays.asList(Locale.getISOLanguages()).contains(languageCode);
    }

    @Override
    public boolean isSupportingNames() {
        return true;
    }

    @Override
    public String getValueName(String id) {
        return new Locale(id, "").getDisplayLanguage();
    }

    @Override
    public MessageList checkReadyToUse() {
        return MessageLists.emptyMessageList();
    }

    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public Object getValue(String value) {
        return Locale.forLanguageTag(value).getISO3Language();
    }

    @Override
    public boolean supportsCompare() {
        return true;
    }

    @Override
    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        return valueA == null ? valueB == null ? 0 : -1 : valueB == null ? 1 : valueA.compareTo(valueB);
    }

    @Override
    public boolean areValuesEqual(String valueA, String valueB) {
        return Objects.equals(valueA, valueB);
    }

    @Override
    public String getName() {
        return "Language";
    }

    @Override
    public String getQualifiedName() {
        return "Language";
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isValueDatatype() {
        return true;
    }

    @Override
    public boolean isEnum() {
        return true;
    }

    @Override
    public boolean hasNullObject() {
        return false;
    }

    @Override
    public int compareTo(Datatype o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Datatype)) {
            return false;
        }
        return getQualifiedName().equals(((Datatype)o).getQualifiedName());
    }
}
