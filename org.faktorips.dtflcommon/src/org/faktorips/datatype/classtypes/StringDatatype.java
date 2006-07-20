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

package org.faktorips.datatype.classtypes;

import org.faktorips.datatype.ValueClassDatatype;

/**
 * Datatype for Strings.
 * 
 * @author Jan Ortmann
 */
public class StringDatatype extends ValueClassDatatype {

	public StringDatatype() {
		super(String.class);
	}

	public StringDatatype(String name) {
		super(String.class, name);
	}
	
	/**
	 * Overridden Method.
	 * @see org.faktorips.datatype.ValueDatatype#getValue(java.lang.String)
	 */
	public Object getValue(String value) {
		return value;
	}


    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return true;
    }
}
