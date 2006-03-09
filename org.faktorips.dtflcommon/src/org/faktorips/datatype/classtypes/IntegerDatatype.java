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

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueClassDatatype;

/**
 * Datatype for <code>Integer</code>.
 * 
 * @author Jan Ortmann
 */
public class IntegerDatatype extends ValueClassDatatype {

	public IntegerDatatype() {
		super(Integer.class);
	}

	public IntegerDatatype(String name) {
		super(Integer.class, name);
	}
	
	/**
	 * Overridden Method.
	 * @see org.faktorips.datatype.ValueDatatype#getValue(java.lang.String)
	 */
	public Object getValue(String s) {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        if (s.equals("")) {
            return Integer.valueOf("0");
        }
		return Integer.valueOf(s);
	}

}
