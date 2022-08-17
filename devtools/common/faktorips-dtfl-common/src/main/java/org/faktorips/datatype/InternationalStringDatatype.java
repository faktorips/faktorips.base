/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.InternationalString;

/**
 * This is the datatype for international strings. This datatype is not implemented for direct use.
 * It is only used internally for code generation.
 * 
 * @see InternationalString
 * @see DefaultInternationalString
 * 
 * @author dirmeier
 */
public class InternationalStringDatatype extends AbstractDatatype implements ValueDatatype {

    /**
     * Creates a new instance of {@link InternationalStringDatatype}.
     */
    public InternationalStringDatatype() {
    }

    @Override
    public String getName() {
        return "InternationalString"; //$NON-NLS-1$
    }

    @Override
    public String getQualifiedName() {
        return getName();
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
    public ValueDatatype getWrapperType() {
        return null;
    }

    @Override
    public boolean isParsable(String value) {
        if (value == null) {
            return true;
        }
        // parsing of array value datatypes is not supported yet.
        return false;
    }

    @Override
    public boolean isNull(String value) {
        return false;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public Object getValue(String value) {
        throw new UnsupportedOperationException("Not supported yet."); //$NON-NLS-1$
    }

    @Override
    public boolean supportsCompare() {
        return false;
    }

    @Override
    public int compare(String valueA, String valueB) {
        return 0;
    }

    @Override
    public boolean areValuesEqual(String valueA, String valueB) {
        return false;
    }

}
