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

import java.util.GregorianCalendar;

/**
 * An enum datatype with values that are valid for a period of time.
 * <p>
 * {@link TimedEnumDatatypeUtil} provides utility methods to check if a value is valid in a given
 * period. These methods are not defined in this interface to avoid forcing all subclasses to
 * implement them.
 * 
 * @author Jan Ortmann
 */
public interface TimedEnumDatatype extends EnumDatatype {

    /**
     * Returns since when the given value is valid. <code>null</code> means unmlimited. If the given
     * string does not identify a value, the method returns <code>null</code>. Returns
     * <code>null</code> if value is <code>null</code>.
     */
    public GregorianCalendar getValidFrom(String value);

    /**
     * Returns up to which date the given value is valid. <code>null</code> means unmlimited. If the
     * given string does not identify a value, the method returns <code>null</code>. Returns
     * <code>null</code> if value is <code>null</code>.
     */
    public GregorianCalendar getValidTo(String value);

}
