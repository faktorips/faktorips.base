/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.codegen.conversion;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class PrimitiveIntToIntegerCgTest extends AbstractSingleConversionCgTest {

    private PrimitiveIntToIntegerCg converter;

    @Before
    public void setUp() throws Exception {
        converter = new PrimitiveIntToIntegerCg();
    }

    @Test
    public void testGetConversionCode() throws Exception {
        assertEquals("new Integer(intValue)", getConversionCode(converter, "intValue"));
    }

}
