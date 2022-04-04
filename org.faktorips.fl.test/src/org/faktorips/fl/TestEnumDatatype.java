/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;

/**
 * Datatype for the test enum.
 * 
 * @author Jan Ortmann
 */
public class TestEnumDatatype extends AbstractDatatype implements EnumDatatype {

    public static final EnumDatatype INSTANCE = new TestEnumDatatype();

    private TestEnumDatatype() {
        super();
    }

    @Override
    public String[] getAllValueIds(boolean includeNull) {
        return new String[] { "MONTH", "YEAR" };
    }

    @Override
    public boolean isSupportingNames() {
        return false;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public String getValueName(String id) {
        return id;
    }

    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    @Override
    public boolean isParsable(String value) {
        return "MONTH".equals(value) || "YEAR".equals(value);
    }

    @Override
    public Object getValue(String value) {
        if ("MONTH".equals(value)) {
            return TestEnum.MONTH;
        }
        if ("YEAR".equals(value)) {
            return TestEnum.YEAR;
        }
        return null;
    }

    public String valueToString(Object value) {
        return value.toString();
    }

    public boolean isNull(Object value) {
        return value == null;
    }

    @Override
    public String getName() {
        return "TestEnumDatatype";
    }

    @Override
    public String getQualifiedName() {
        return "TestEnumDatatype";
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
    public boolean isNull(String value) {
        return false;
    }

    @Override
    public boolean supportsCompare() {
        return false;
    }

    @Override
    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        return 0;
    }

    @Override
    public boolean areValuesEqual(String valueA, String valueB) {
        return false;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public String getIdByName(String valueName) {
        return Arrays.stream(getAllValueIds(false))
                .filter(id -> StringUtils.equals(getValueName(id), valueName))
                .findFirst()
                .orElse(null);
    }
}
