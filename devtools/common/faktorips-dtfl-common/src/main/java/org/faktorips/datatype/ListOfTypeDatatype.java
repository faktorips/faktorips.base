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

import java.util.Objects;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.util.ArgumentCheck;

/**
 * Datatype representing a list of objects of a basic datatype.
 * <p>
 * This datatype is currently only used when navigating associations in expressions. Therefore no
 * {@link DatatypeHelper} exists and this datatype is not usable in attributes.
 * </p>
 * 
 * @since 3.6
 * @author schwering
 */
public class ListOfTypeDatatype extends AbstractDatatype implements ValueDatatype {

    private final Datatype basicType;

    /**
     * Constructs a new List type based on the given underlying basic type.
     */
    public ListOfTypeDatatype(Datatype basicType) {
        super();
        ArgumentCheck.notNull(basicType);
        if (basicType.isPrimitive() && basicType instanceof ValueDatatype) {
            this.basicType = ((ValueDatatype)basicType).getWrapperType();
        } else {
            this.basicType = basicType;
        }
    }

    /**
     * Returns the List type's basic type. E.g. for a list of policy values, <code>Policy</code> is
     * the basic type.
     */
    public Datatype getBasicDatatype() {
        return basicType;
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
    public String getName() {
        StringBuilder sb = new StringBuilder("List"); //$NON-NLS-1$
        sb.append('<');
        sb.append(basicType.getName());
        sb.append('>');
        return sb.toString();
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
        return basicType.isAbstract();
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public boolean isValueDatatype() {
        return true;
    }

    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * <strong>Not supported yet.</strong> Always returns {@code false}.
     */
    @Override
    public boolean isParsable(String value) {
        if (value == null) {
            return true;
        }
        // parsing of array value datatypes is not supported yet.
        return false;
    }

    /**
     * <strong>Not supported yet.</strong>
     * 
     * @throws UnsupportedOperationException always
     */
    @Override
    public Object getValue(String value) {
        throw new UnsupportedOperationException("Not supported yet."); //$NON-NLS-1$
    }

    @Override
    public boolean matchDatatype(Datatype datatype) {
        if (super.matchDatatype(datatype)) {
            return true;
        }
        if (datatype instanceof ListOfTypeDatatype otherListOfTypeDatatype) {
            if (otherListOfTypeDatatype.getBasicDatatype() instanceof AnyDatatype) {
                return true;
            } else {
                return otherListOfTypeDatatype.getBasicDatatype().equals(getBasicDatatype());
            }
        }
        return false;
    }

    @Override
    public boolean isNull(String value) {
        return value == null;
    }

    @Override
    public boolean supportsCompare() {
        if (basicType.isValueDatatype() && ((ValueDatatype)basicType).supportsCompare()) {
            return true;
        }
        return false;
    }

    @Override
    public int compare(String valueA, String valueB) {
        if (!supportsCompare()) {
            throw new UnsupportedOperationException("The basicType " + basicType.getQualifiedName() //$NON-NLS-1$
                    + " does not support comparison for values."); //$NON-NLS-1$
        }

        return ((ValueDatatype)basicType).compare(valueA, valueB);
    }

    @Override
    public boolean areValuesEqual(String valueA, String valueB) {
        if (basicType.isValueDatatype()) {
            return ((ValueDatatype)basicType).areValuesEqual(valueA, valueB);
        }
        return Objects.equals(valueA, valueB);
    }

}
