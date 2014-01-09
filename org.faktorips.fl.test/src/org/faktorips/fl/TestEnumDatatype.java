/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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

    public final static EnumDatatype INSTANCE = new TestEnumDatatype();

    private TestEnumDatatype() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAllValueIds(boolean includeNull) {
        return new String[] { "MONTH", "YEAR" };
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSupportingNames() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getValueName(String id) {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isParsable(String value) {
        return value.equals("MONTH") || value.equals(("YEAR"));
    }

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
    public String getName() {
        return "TestEnumDatatype";
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        return "TestEnumDatatype";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAbstract() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValueDatatype() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return TestEnum.class.getName();
    }

    public boolean isNull(String value) {
        return false;
    }

    public boolean supportsCompare() {
        return false;
    }

    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        return 0;
    }

    public boolean areValuesEqual(String valueA, String valueB) {
        return false;
    }

    public boolean isImmutable() {
        return true;
    }

    public boolean isMutable() {
        return false;
    }

}
