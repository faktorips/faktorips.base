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

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getAllValueIds(boolean includeNull) {
        return new String[] { "MONTH", "YEAR" };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupportingNames() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueName(String id) {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isParsable(String value) {
        return value.equals("MONTH") || value.equals(("YEAR"));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "TestEnumDatatype";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifiedName() {
        return "TestEnumDatatype";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAbstract() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
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

}
