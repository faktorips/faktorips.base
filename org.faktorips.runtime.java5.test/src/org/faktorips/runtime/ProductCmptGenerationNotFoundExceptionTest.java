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

package org.faktorips.runtime;

import java.util.GregorianCalendar;

import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenerationNotFoundExceptionTest {

    @Test
    public void testToString() {
        ProductCmptGenerationNotFoundException e = new ProductCmptGenerationNotFoundException("MotorProducts",
                "MotorProduct", new GregorianCalendar(2007, 0, 1), false);
        e.toString();
    }

}
