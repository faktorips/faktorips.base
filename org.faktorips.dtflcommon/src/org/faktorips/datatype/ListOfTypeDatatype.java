/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.datatype;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
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

    private Datatype basicType;

    /**
     * Constructs a new List type based on the given underlying basic type.
     */
    public ListOfTypeDatatype(Datatype basicType) {
        super();
        ArgumentCheck.notNull(basicType);
        this.basicType = basicType;
    }

    /**
     * Returns the List type's basic type. E.g. for a list of policy values, <code>Policy</code> is
     * the basic type.
     */
    public Datatype getBasicDatatype() {
        return basicType;
    }

    public boolean isImmutable() {
        return true;
    }

    public boolean isMutable() {
        return false;
    }

    public String getName() {
        StringBuffer buffer = new StringBuffer("List<"); //$NON-NLS-1$
        buffer.append('<');
        buffer.append(basicType.getName());
        buffer.append('>');
        return buffer.toString();
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

    public String getDefaultValue() {
        return null;
    }

    public boolean isValueDatatype() {
        return true;
    }

    public String getJavaClassName() {
        StringBuffer buffer = new StringBuffer(List.class.getName());
        buffer.append('<');
        buffer.append(basicType.getName());
        buffer.append('>');
        return buffer.toString();
    }

    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * <strong>Not supported yet.</strong> Always returns {@code false}.
     */
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
    public Object getValue(String value) {
        throw new UnsupportedOperationException("Not supported yet."); //$NON-NLS-1$
    }

    public boolean isNull(String value) {
        return value == null;
    }

    public boolean supportsCompare() {
        if (basicType.isValueDatatype() && ((ValueDatatype)basicType).supportsCompare()) {
            return true;
        }
        return false;
    }

    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        if (!supportsCompare()) {
            throw new UnsupportedOperationException("The basicType " + basicType.getQualifiedName() //$NON-NLS-1$
                    + " does not support comparison for values."); //$NON-NLS-1$
        }

        return ((ValueDatatype)basicType).compare(valueA, valueB);
    }

    public boolean areValuesEqual(String valueA, String valueB) {
        if (basicType.isValueDatatype()) {
            return ((ValueDatatype)basicType).areValuesEqual(valueA, valueB);
        }
        return ObjectUtils.equals(valueA, valueB);
    }

}
