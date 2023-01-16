package org.faktorips.testextensions.customdatatypes;

import java.util.Arrays;
import java.util.Locale;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.MessageLists;

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
        return new Locale("", id).getDisplayCountry();
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
