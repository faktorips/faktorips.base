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
 * Datatype for <code>Boolean</code>.
 * 
 * @author Jan Ortmann
 */
public class BooleanDatatype extends ValueClassDatatype {

	public BooleanDatatype() {
		super(Boolean.class);
	}

	public BooleanDatatype(String name) {
		super(Boolean.class, name);
	}
	
	/**
     * {@inheritDoc}
	 */
	public Object getValue(String s) {
        if (s == null) {
            return null;
        }
        if (s.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
		throw new IllegalArgumentException("Can't parse " + s + " to Boolean!");
	}

    public boolean supportsCompare() {
        return false;
    }

}
