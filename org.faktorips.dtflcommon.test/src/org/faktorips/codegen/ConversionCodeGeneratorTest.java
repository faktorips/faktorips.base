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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.junit.Before;
import org.junit.Test;

public class ConversionCodeGeneratorTest {

    ConversionCodeGenerator<JavaCodeFragment> codeGenerator;

    @Before
    public void setUp() {
        codeGenerator = ConversionCodeGenerator.getDefault();
    }

    @Test
    public void testCanConvert() {
        boolean booleanToPrimitiveBoolean = codeGenerator.canConvert(Datatype.BOOLEAN, Datatype.PRIMITIVE_BOOLEAN);
        boolean primitiveBooleanToBoolean = codeGenerator.canConvert(Datatype.PRIMITIVE_BOOLEAN, Datatype.BOOLEAN);
        assertTrue(booleanToPrimitiveBoolean);
        assertTrue(primitiveBooleanToBoolean);

        boolean decimalToInteger = codeGenerator.canConvert(Datatype.DECIMAL, Datatype.INTEGER);
        boolean integerToDecimal = codeGenerator.canConvert(Datatype.INTEGER, Datatype.DECIMAL);
        assertTrue(integerToDecimal);
        assertTrue(decimalToInteger);

        boolean primitiveLongToPrimitiveInt = codeGenerator.canConvert(Datatype.PRIMITIVE_LONG, Datatype.PRIMITIVE_INT);
        boolean primitiveIntToPrimitiveLong = codeGenerator.canConvert(Datatype.PRIMITIVE_INT, Datatype.PRIMITIVE_LONG);
        assertTrue(primitiveLongToPrimitiveInt);
        assertTrue(primitiveIntToPrimitiveLong);

        boolean localDateToLocalDate = codeGenerator.canConvert(LocalDateDatatype.DATATYPE, LocalDateDatatype.DATATYPE);
        assertTrue(localDateToLocalDate);

        boolean localDateToPrimitiveInt = codeGenerator.canConvert(LocalDateDatatype.DATATYPE, Datatype.PRIMITIVE_INT);
        assertTrue(!localDateToPrimitiveInt);
    }

    @Test
    public void testGetConversionCode() {
        JavaCodeFragment conversionCode = codeGenerator.getConversionCode(Datatype.BOOLEAN, Datatype.PRIMITIVE_BOOLEAN,
                new JavaCodeFragment("TRUE"));
        assertNotNull(conversionCode);

        conversionCode = codeGenerator.getConversionCode(Datatype.PRIMITIVE_BOOLEAN, Datatype.BOOLEAN,
                new JavaCodeFragment("true"));
        assertNotNull(conversionCode);

        DecimalDatatype decimalDatatype = new DecimalDatatype("22");
        String string = decimalDatatype.toString();
        conversionCode = codeGenerator.getConversionCode(Datatype.DECIMAL, Datatype.PRIMITIVE_INT,
                new JavaCodeFragment(string));
        assertNotNull(conversionCode);

        conversionCode = codeGenerator.getConversionCode(null, Datatype.INTEGER, new JavaCodeFragment("true"));
        assertTrue(conversionCode == null);

        conversionCode = codeGenerator
                .getConversionCode(Datatype.PRIMITIVE_BOOLEAN, null, new JavaCodeFragment("true"));
        assertTrue(conversionCode == null);
    }
}
