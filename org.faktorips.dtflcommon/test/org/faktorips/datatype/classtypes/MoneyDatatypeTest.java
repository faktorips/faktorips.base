/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import junit.framework.TestCase;

/**
 * 
 * @author Thorsten Guenther
 */
public class MoneyDatatypeTest extends TestCase {
    
    public void testDivisibleWithoutRemainder() {
        MoneyDatatype datatype = new MoneyDatatype();
        assertTrue(datatype.divisibleWithoutRemainder("10 EUR", "2 EUR"));
        assertFalse(datatype.divisibleWithoutRemainder("10 EUR", "3 EUR"));
    }

}
