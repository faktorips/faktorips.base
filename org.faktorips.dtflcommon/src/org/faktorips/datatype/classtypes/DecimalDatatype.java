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

import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueClassDatatype;
import org.faktorips.values.Decimal;

/**
 * Datatype for <code>Decimal</code>.
 * 
 * @author Jan Ortmann
 */
public class DecimalDatatype extends ValueClassDatatype implements NumericDatatype {

	public DecimalDatatype() {
		super(Decimal.class);
	}
	
	public DecimalDatatype(String name) {
	    super(Decimal.class, name);
	}

	/**
     * {@inheritDoc}
	 */
	public Object getValue(String s) {
		return Decimal.valueOf(s);
	}

    /**
     * {@inheritDoc}
     */
    public boolean hasNullObject() {
        return true;
    }

    
}
