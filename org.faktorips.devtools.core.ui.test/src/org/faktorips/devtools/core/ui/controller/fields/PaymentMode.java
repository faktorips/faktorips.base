/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;

public class PaymentMode extends AbstractDatatype implements EnumDatatype {

    public static final String ANNUAL_ID = "1";
    public static final String MONTHLY_ID = "12";

    public static final String ANNUAL_NAME = "annual";
    public static final String MONTHLY_NAME = "monthly";

    @Override
    public String[] getAllValueIds(boolean includeNull) {
        if (includeNull) {
            return new String[] { null, ANNUAL_ID, MONTHLY_ID };
        }
        return new String[] { ANNUAL_ID, MONTHLY_ID };
    }

    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    @Override
    public boolean isParsable(String value) {
        if (value == null) {
            return true;
        }
        return value.equals(ANNUAL_ID) || value.equals(MONTHLY_ID);
    }

    @Override
    public String getName() {
        return "PaymentMode";
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public String getQualifiedName() {
        return null;
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
    public boolean isSupportingNames() {
        return true;
    }

    @Override
    public String getValueName(String id) {
        if (id == null) {
            return null;
        }
        if (id.equals(ANNUAL_ID)) {
            return ANNUAL_NAME;
        }
        if (id.equals(MONTHLY_ID)) {
            return MONTHLY_NAME;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Object getValue(String value) {
        throw new RuntimeException("not supported");
    }

    @Override
    public boolean isNull(String value) {
        if (value == null) {
            return true;
        }
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
                .filter(x -> StringUtils.equals(getValueName(x), valueName))
                .findFirst()
                .orElse(null);
    }

}
