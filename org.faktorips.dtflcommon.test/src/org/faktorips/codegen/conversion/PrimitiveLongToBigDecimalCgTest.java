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

public class PrimitiveLongToBigDecimalCgTest extends AbstractSingleConversionCgTest {

    private PrimitiveLongToBigDecimalCg converter;

    @Before
    public void setUp() throws Exception {
        converter = new PrimitiveLongToBigDecimalCg();
    }

    @Test
    public void testGetConversionCode() throws Exception {
        assertEquals("BigDecimal.valueOf(longValue, 0)", getConversionCode(converter, "longValue"));
    }

}
