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

package org.faktorips.runtime;

import java.util.GregorianCalendar;

import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenerationNotFoundExceptionTest extends TestCase {

    public void testToString() {
        ProductCmptGenerationNotFoundException e = new ProductCmptGenerationNotFoundException("MotorProducts", "MotorProduct", 
                new GregorianCalendar(2007, 0, 1), false);
        e.toString();
    }
}
