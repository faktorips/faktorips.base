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

/**
 * A value datatype representing an enumeration of values.
 * 
 * @author Jan Ortmann
 * @author Peter Erzberger
 */
public interface EnumDatatype extends ValueDatatype {

    /**
     * Returns the ids of all values defined in the enum type.
     * 
     * @param includeNull <code>true</code> to get the id for the null-Value included,
     *            <code>false</code> for not include the null-Value. Note that the null-Value can be
     *            the Java <code>null</code> or the special case NULL-value id.
     */
    public String[] getAllValueIds(boolean includeNull);

    /**
     * Returns <code>true</code> if an implementation of this interface supports names that describe
     * the datatype's value. E.g. an enum datatype PaymentMode might return the name "annual" for
     * the annual payment mode with id "1". If this method returns <code>false</code> a call to the
     * getName(String id) method is supposed to throw a runtime exception.
     */
    public boolean isSupportingNames();

    /**
     * Returns a short description of the value of this enumeration datatype specified by the id.
     * 
     * @throws IllegalArgumentException if the id is not a valid id of this enumeration datatype
     * @throws RuntimeException if the datatype does not support names for the id.
     */
    public String getValueName(String id);
}
