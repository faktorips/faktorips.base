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

package org.faktorips.codegen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;
import org.junit.Test;
import org.mockito.Mock;

public class ConversionCodeGeneratorTest {

    @Mock
    private SingleConversionCg<JavaCodeFragment> singleConversionCg;

    @Test
    public void testCanConvert_ReturnTrueIfFromDatatypeIsEqualsToDatatype() {
        ConversionCodeGenerator<JavaCodeFragment> conversionCodeGenerator = ConversionCodeGenerator.getDefault();

        assertTrue(conversionCodeGenerator.canConvert(Datatype.STRING, Datatype.STRING));
    }

    @Test
    public void testCanConvert_ReturnTrueIfToDatatypeIsInstanceOfAnyDatatype() {
        ConversionCodeGenerator<JavaCodeFragment> conversionCodeGenerator = ConversionCodeGenerator.getDefault();

        assertTrue(conversionCodeGenerator.canConvert(Datatype.STRING, AnyDatatype.INSTANCE));
    }

    @Test
    public void testCanConvert_ReturnTrueIfFromDatatypeIsInstanceOfAnyDatatype() {
        ConversionCodeGenerator<JavaCodeFragment> conversionCodeGenerator = ConversionCodeGenerator.getDefault();

        assertTrue(conversionCodeGenerator.canConvert(AnyDatatype.INSTANCE, Datatype.STRING));
    }

    @Test
    public void testCanConvert_ReturnFalseIfSingleConversionOfDatatypePairNotExist() {
        ConversionCodeGenerator<JavaCodeFragment> conversionCodeGenerator = ConversionCodeGenerator.getDefault();

        assertFalse(conversionCodeGenerator.canConvert(Datatype.BIG_DECIMAL, Datatype.GREGORIAN_CALENDAR));
    }

    @Test
    public void testGetConversionCode_ReturnSourceCodeIfFromDatatypeIsEqualsToDatatype() {
        ConversionCodeGenerator<JavaCodeFragment> conversionCodeGenerator = ConversionCodeGenerator.getDefault();
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("FromValue");

        assertEquals("FromValue",
                conversionCodeGenerator.getConversionCode(Datatype.STRING, Datatype.STRING, javaCodeFragment)
                        .getSourcecode());
    }

    @Test
    public void testCanConvert_ReturSourceCodeIfToDatatypeIsInstanceOfAnyDatatype() {
        ConversionCodeGenerator<JavaCodeFragment> conversionCodeGenerator = ConversionCodeGenerator.getDefault();
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("FromValue");

        assertEquals("FromValue",
                conversionCodeGenerator.getConversionCode(Datatype.STRING, AnyDatatype.INSTANCE, javaCodeFragment)
                        .getSourcecode());
    }

    @Test
    public void testCanConvert_ReturnSourceCodeIfFromDatatypeIsIsInstanceOfAnyDatatype() {
        ConversionCodeGenerator<JavaCodeFragment> conversionCodeGenerator = ConversionCodeGenerator.getDefault();
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("FromValue");

        assertEquals("String.valueOf(FromValue)",
                conversionCodeGenerator.getConversionCode(AnyDatatype.INSTANCE, Datatype.STRING, javaCodeFragment)
                        .getSourcecode());
    }

    @Test
    public void testCanConvert_ReturnNullIfSingleConversionOfDatatypePairNotExist() {
        ConversionCodeGenerator<JavaCodeFragment> conversionCodeGenerator = ConversionCodeGenerator.getDefault();
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("FromValue");

        assertNull(conversionCodeGenerator.getConversionCode(Datatype.BIG_DECIMAL, Datatype.GREGORIAN_CALENDAR,
                javaCodeFragment));
    }
}
