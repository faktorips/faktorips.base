/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.datatype;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.util.ArgumentCheck;

/**
 * Datatype representing an array of primitive Datatypes with a dimension.
 * 
 * @author Jan Ortmann
 */
public class ArrayOfValueDatatype extends AbstractDatatype implements ValueDatatype {

    /** The primitive datatype. */
    private Datatype datatype;

    /** The dimension of the array. */
    private int dimension;

    /**
     * Returns the number of dimensions specified in the given datatypeName.
     * <p>
     * Examples:<br>
     * "Money" specifies 0 dimensions. "Money[]" specifies 1 dimension. "Money[][]" specifies 2
     * dimensions.
     */
    public final static int getDimension(String datatypeName) {
        if (datatypeName == null) {
            return 0;
        }
        int dimension = 0;
        while (datatypeName.endsWith("[]")) { //$NON-NLS-1$
            dimension++;
            datatypeName = datatypeName.substring(0, datatypeName.length() - 2);
        }
        return dimension;
    }

    /**
     * Returns the basic datatype name specified in the given datatypeName.
     * <p>
     * Examples:<br>
     * "Money" specifies basic datatype Money. "Money[]" specifies basic datatype Money. "Money[][]"
     * specifies basic datatype Money.
     */
    public final static String getBasicDatatypeName(String datatypeName) {
        while (datatypeName.endsWith("[]")) { //$NON-NLS-1$
            datatypeName = datatypeName.substring(0, datatypeName.length() - 2);
        }
        return datatypeName;
    }

    /**
     * Returns if the provided string represents an ArrayDatatype.
     */
    public final static boolean isArrayDatatype(String datatypeName) {
        return getDimension(datatypeName) != 0;
    }

    /**
     * Constructs a new array datatype based on the given underlying datatype and the dimension.
     */
    public ArrayOfValueDatatype(Datatype datatype, int dimension) {
        super();
        ArgumentCheck.notNull(datatype);
        this.datatype = datatype;
        this.dimension = dimension;
    }

    /**
     * Returns the array datatype's basic datatype. E.g. for an array of Money values,
     * <code>Datatype.MONEY</code> is the basic datatype.
     */
    public Datatype getBasicDatatype() {
        return datatype;
    }

    /**
     * Returns the array's dimension.
     */
    public int getDimension() {
        return dimension;
    }

    public boolean isImmutable() {
        return true;
    }

    public boolean isMutable() {
        return false;
    }

    public String getName() {
        StringBuffer buffer = new StringBuffer(datatype.getName());
        for (int i = 0; i < dimension; i++) {
            buffer.append("[]"); //$NON-NLS-1$
        }
        return buffer.toString();
    }

    public String getQualifiedName() {
        StringBuffer buffer = new StringBuffer(datatype.getQualifiedName());
        for (int i = 0; i < dimension; i++) {
            buffer.append("[]"); //$NON-NLS-1$
        }
        return buffer.toString();
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
        StringBuffer buffer = new StringBuffer(datatype.getJavaClassName());
        for (int i = 0; i < dimension; i++) {
            buffer.append("[]"); //$NON-NLS-1$
        }
        return buffer.toString();
    }

    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * Null is parsable. Other values are not supported yet. {@inheritDoc}
     */
    public boolean isParsable(String value) {
        if (value == null) {
            return true;
        }
        // parsing of array value datatypes is not supported yet.
        return false;
    }

    /**
     * If the value is null, null will be returned. Other values are not supported yet.
     */
    public Object getValue(String value) {
        if (value == null) {
            return null;
        }
        throw new RuntimeException("No supported yet."); //$NON-NLS-1$
    }

    /**
     * If the value is null, null will be returned. Other values are not supported yet.
     */
    public String valueToString(Object value) {
        if (value == null) {
            return null;
        }
        throw new RuntimeException("No supported yet."); //$NON-NLS-1$
    }

    public boolean isNull(String value) {
        return value == null;
    }

    public boolean supportsCompare() {
        if (datatype.isValueDatatype() && ((ValueDatatype)datatype).supportsCompare()) {
            return true;
        }
        return false;
    }

    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        if (!supportsCompare()) {
            throw new UnsupportedOperationException("The datatype " + datatype.getQualifiedName() //$NON-NLS-1$
                    + " does not support comparison for values."); //$NON-NLS-1$
        }

        return ((ValueDatatype)datatype).compare(valueA, valueB);
    }

    public boolean areValuesEqual(String valueA, String valueB) {
        if (datatype.isValueDatatype()) {
            return ((ValueDatatype)datatype).areValuesEqual(valueA, valueB);
        }
        return ObjectUtils.equals(valueA, valueB);
    }

}
