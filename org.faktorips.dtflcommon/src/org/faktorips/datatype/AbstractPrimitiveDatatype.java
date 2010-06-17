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
import org.apache.commons.lang.StringUtils;

/**
 * Abstract base class for datatypes representing a Java primtive like boolean.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractPrimitiveDatatype extends AbstractDatatype implements ValueDatatype {

    public AbstractPrimitiveDatatype() {
        super();
    }

    public boolean isPrimitive() {
        return true;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isValueDatatype() {
        return true;
    }

    public String valueToString(Object value) {
        return "" + value; //$NON-NLS-1$
    }

    /**
     * If the value is <code>null</code> or an empty string, <code>false</code> is returned.
     * 
     * {@inheritDoc}
     */
    public boolean isParsable(String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        try {
            getValue(value);
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean hasNullObject() {
        return false;
    }

    public abstract Object getValue(String value);

    public boolean areValuesEqual(String valueA, String valueB) {
        return ObjectUtils.equals(getValue(valueA), getValue(valueB));
    }

    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        if (!supportsCompare()) {
            throw new UnsupportedOperationException("Datatype " + getQualifiedName() //$NON-NLS-1$
                    + " does not support comparison of values"); //$NON-NLS-1$
        }
        return ((Comparable)getValue(valueA)).compareTo(getValue(valueB));
    }

    public boolean isNull(String value) {
        return false;
    }

    public boolean isMutable() {
        return false;
    }

    public boolean isImmutable() {
        return true;
    }

}
