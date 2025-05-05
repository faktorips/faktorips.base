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

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.MessageLists;

/**
 * Provides country ISO code Strings as a datatype.
 */
public class CountryDatatype extends GenericValueDatatype implements EnumDatatype {

    public CountryDatatype() {
        setQualifiedName("Country");
        setValueOfMethodName(null);
        setIsParsableMethodName(null);
    }

    @Override
    public String[] getAllValueIds(boolean includeNull) {
        return Locale.getISOCountries();
    }

    @Override
    public boolean isParsable(String countryCode) {
        return Arrays.asList(Locale.getISOCountries()).contains(countryCode);
    }

    @Override
    public boolean isSupportingNames() {
        return true;
    }

    @Override
    public String getValueName(String id) {
        return Locale.of("", id).getDisplayCountry();
    }

    @Override
    public Class<?> getAdaptedClass() {
        return String.class;
    }

    @Override
    public String getAdaptedClassName() {
        return String.class.getName();
    }

    @Override
    public MessageList checkReadyToUse() {
        return MessageLists.emptyMessageList();
    }
}
