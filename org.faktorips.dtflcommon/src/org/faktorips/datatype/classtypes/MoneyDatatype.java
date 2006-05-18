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
import org.faktorips.values.Money;

/**
 * Datatype for <code>Money</code>.
 * 
 * @author Jan Ortmann
 */
public class MoneyDatatype extends ValueClassDatatype {

	public MoneyDatatype() {
		super(Money.class);
	}

	public MoneyDatatype(String name) {
		super(Money.class, name);
	}
	
	/**
     * {@inheritDoc}
	 */
	public Object getValue(String s) {
		return Money.valueOf(s);
	}

    /**
     * {@inheritDoc}
     */
    public boolean hasNullObject() {
        return true;
    }
}
