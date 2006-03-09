/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.datatype;

/**
 * A value datatype representing an enumeration of values.
 * 
 * @author Jan Ortmann, Peter Erzberger
 */
public interface EnumDatatype extends ValueDatatype {

	/**
	 * Returns the ids of all values defined in the enum type.
	 */
	public String[] getAllValueIds();

	/**
	 * Returns true of an implementation of this interface supports names that
	 * describe the datatype. If this method returns false a call to the
	 * getName(String) method is supposed to throw a runtime exception.
	 */
	public boolean isSupportingNames();

	/**
	 * Returns a short description of the value of this enumeration datatype specified by the id. 
	 * @throws an IllegalArgumentException if the id is not a valid id of this  enumeration datatype
	 */
	public String getValueName(String id);
}
