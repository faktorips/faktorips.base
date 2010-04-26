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

/**
 * The datatype void representing <code>java.lang.Void</code>.
 * 
 * @author Jan Ortmann
 */
public class Void extends AbstractDatatype implements ValueDatatype {

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "void";
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        return "void";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVoid() {
        return true;
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
    public boolean isImmutable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMutable() {
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
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return "void";
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultValue() {
        throw new UnsupportedOperationException("Can't get a default value for Datatype void.");
    }

    public Object getValue(String value) {
        throw new UnsupportedOperationException("Can't get a value for Datatype void.");
    }

    public String valueToXmlString(Object value) {
        return "void";
    }

    /**
     * {@inheritDoc}
     */
    public String valueToString(Object value) {
        return "void";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isParsable(String value) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNullObject() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNull(String value) {
        return value == null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The datatype " + getQualifiedName()
                + " does not support comparison for values");
    }

    /**
     * {@inheritDoc}
     */
    public boolean areValuesEqual(String valueA, String valueB) {
        return ObjectUtils.equals(valueA, valueB);
    }

}
