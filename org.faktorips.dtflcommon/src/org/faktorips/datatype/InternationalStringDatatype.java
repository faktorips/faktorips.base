/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import org.faktorips.values.IInternationalString;

/**
 * This is the datatype for international strings. This datatype is not implemented for using
 * directly. It is only used internally for code generation.
 * 
 * @see IInternationalString
 * 
 * @author dirmeier
 */
public class InternationalStringDatatype extends AbstractDatatype implements ValueDatatype {

    public String getName() {
        return IInternationalString.class.getName();
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
        return IInternationalString.class.getCanonicalName();
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
