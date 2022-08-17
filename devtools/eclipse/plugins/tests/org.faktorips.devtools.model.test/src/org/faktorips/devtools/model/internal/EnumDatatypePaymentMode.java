/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;

public class EnumDatatypePaymentMode extends AbstractDatatype implements EnumDatatype {

    public static final String ANNUAL = "annual";
    public static final String MONTHLY = "monthly";

    @Override
    public String[] getAllValueIds(boolean includeNull) {
        return new String[] { ANNUAL, MONTHLY };
    }

    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    @Override
    public boolean isParsable(String value) {
        return false;
    }

    @Override
    public String getName() {
        return null;
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
        return false;
    }

    @Override
    public boolean isSupportingNames() {
        return false;
    }

    @Override
    public String getValueName(String id) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object getValue(String value) {
        throw new UnsupportedOperationException("Not supported");
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
    public Object getValueByName(String name) {
        throw new UnsupportedOperationException("Not supported");
    }

}
