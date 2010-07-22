/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

    public String getName() {
        return "void"; //$NON-NLS-1$
    }

    public String getQualifiedName() {
        return "void"; //$NON-NLS-1$
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isImmutable() {
        return true;
    }

    public boolean isMutable() {
        return false;
    }

    public boolean isValueDatatype() {
        return true;
    }

    public ValueDatatype getWrapperType() {
        return null;
    }

    public String getJavaClassName() {
        return "void"; //$NON-NLS-1$
    }

    public String getDefaultValue() {
        throw new UnsupportedOperationException("Can't get a default value for Datatype void."); //$NON-NLS-1$
    }

    public boolean isParsable(String value) {
        return false;
    }

    @Override
    public boolean hasNullObject() {
        return false;
    }

    public boolean isNull(String value) {
        return value == null;
    }

    public boolean supportsCompare() {
        return false;
    }

    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The datatype " + getQualifiedName() //$NON-NLS-1$
                + " does not support comparison for values"); //$NON-NLS-1$
    }

    public boolean areValuesEqual(String valueA, String valueB) {
        return ObjectUtils.equals(valueA, valueB);
    }

}
