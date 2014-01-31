/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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

    private final Class<? extends InternationalString> wrappedClass;

    /**
     * Creates a new instance of {@link InternationalStringDatatype} and specifies whether to use
     * the interface {@link InternationalString} or the implementation
     * {@link DefaultInternationalString} as wrapped class.
     * 
     */
    public InternationalStringDatatype(boolean useInterface) {
        if (useInterface) {
            wrappedClass = InternationalString.class;
        } else {
            wrappedClass = DefaultInternationalString.class;
        }
    }

    public String getName() {
        return wrappedClass.getName();
    }

    public String getQualifiedName() {
        return getJavaClassName();
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isValueDatatype() {
        return true;
    }

    public String getJavaClassName() {
        return wrappedClass.getCanonicalName();
    }

    public ValueDatatype getWrapperType() {
        return null;
    }

    public boolean isParsable(String value) {
        if (value == null) {
            return true;
        }
        // parsing of array value datatypes is not supported yet.
        return false;
    }

    public boolean isNull(String value) {
        return false;
    }

    public boolean isMutable() {
        return true;
    }

    public boolean isImmutable() {
        return false;
    }

    public String getDefaultValue() {
        return null;
    }

    public Object getValue(String value) {
        throw new UnsupportedOperationException("Not supported yet."); //$NON-NLS-1$
    }

    public boolean supportsCompare() {
        return false;
    }

    public int compare(String valueA, String valueB) {
        return 0;
    }

    public boolean areValuesEqual(String valueA, String valueB) {
        return false;
    }

}
