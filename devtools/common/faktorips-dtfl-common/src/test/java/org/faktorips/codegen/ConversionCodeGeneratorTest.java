/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
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
        assertTrue(codeGenerator.canConvert(Datatype.INTEGER, Datatype.INTEGER));
        assertTrue(codeGenerator.canConvert(Datatype.BOOLEAN, Datatype.PRIMITIVE_BOOLEAN));
        assertTrue(codeGenerator.canConvert(Datatype.PRIMITIVE_BOOLEAN, Datatype.BOOLEAN));
        assertTrue(codeGenerator.canConvert(Datatype.INTEGER, Datatype.BIG_DECIMAL));
        assertTrue(codeGenerator.canConvert(Datatype.INTEGER, Datatype.DECIMAL));
        assertTrue(codeGenerator.canConvert(Datatype.INTEGER, Datatype.PRIMITIVE_INT));
        assertTrue(codeGenerator.canConvert(Datatype.INTEGER, Datatype.LONG));
        assertTrue(codeGenerator.canConvert(Datatype.LONG, Datatype.BIG_DECIMAL));
        assertTrue(codeGenerator.canConvert(Datatype.LONG, Datatype.DECIMAL));
        assertTrue(codeGenerator.canConvert(Datatype.LONG, Datatype.INTEGER));
        assertTrue(codeGenerator.canConvert(Datatype.PRIMITIVE_INT, Datatype.BIG_DECIMAL));
        assertTrue(codeGenerator.canConvert(Datatype.PRIMITIVE_INT, Datatype.DECIMAL));
        assertTrue(codeGenerator.canConvert(Datatype.PRIMITIVE_INT, Datatype.INTEGER));
        assertTrue(codeGenerator.canConvert(Datatype.PRIMITIVE_INT, Datatype.LONG));
        assertTrue(codeGenerator.canConvert(Datatype.PRIMITIVE_INT, Datatype.PRIMITIVE_LONG));
        assertTrue(codeGenerator.canConvert(Datatype.PRIMITIVE_LONG, Datatype.BIG_DECIMAL));
        assertTrue(codeGenerator.canConvert(Datatype.PRIMITIVE_LONG, Datatype.LONG));
        assertTrue(codeGenerator.canConvert(Datatype.PRIMITIVE_LONG, Datatype.PRIMITIVE_INT));
        assertTrue(codeGenerator.canConvert(Datatype.BIG_DECIMAL, Datatype.DECIMAL));
        assertTrue(codeGenerator.canConvert(Datatype.DECIMAL, Datatype.BIG_DECIMAL));
        assertTrue(codeGenerator.canConvert(Datatype.DOUBLE, Datatype.DECIMAL));
        assertTrue(codeGenerator.canConvert(Datatype.DECIMAL, Datatype.DOUBLE));
        assertTrue(codeGenerator.canConvert(LocalDateDatatype.DATATYPE, Datatype.GREGORIAN_CALENDAR));
        assertTrue(codeGenerator.canConvert(LocalDateTimeDatatype.DATATYPE, Datatype.GREGORIAN_CALENDAR));
        assertTrue(codeGenerator.canConvert(Datatype.STRING, Datatype.STRING));
        assertTrue(codeGenerator.canConvert(Datatype.STRING, AnyDatatype.INSTANCE));
        assertFalse(codeGenerator.canConvert(AnyDatatype.INSTANCE, Datatype.STRING));

        assertFalse(codeGenerator.canConvert(Datatype.BIG_DECIMAL, Datatype.GREGORIAN_CALENDAR));
        assertFalse(codeGenerator.canConvert(LocalDateDatatype.DATATYPE, Datatype.PRIMITIVE_INT));
    }

    @Test
    public void testCanConvert_ListOfDatatype() {
        assertTrue(codeGenerator.canConvert(createList(Datatype.DECIMAL), createList(AnyDatatype.INSTANCE)));

        assertFalse(codeGenerator.canConvert(createList(Datatype.DECIMAL), createList(Datatype.INTEGER)));
        assertFalse(codeGenerator.canConvert(createList(Datatype.DECIMAL), Datatype.INTEGER));
        assertFalse(codeGenerator.canConvert(Datatype.DECIMAL, createList(Datatype.INTEGER)));
        assertFalse(
                codeGenerator.canConvert(createList(Datatype.BIG_DECIMAL), createList(Datatype.GREGORIAN_CALENDAR)));
    }

    private ListOfTypeDatatype createList(Datatype basicType) {
        return new ListOfTypeDatatype(basicType);
    }

    @Test
    public void testGetConversionCode() {

        assertNotNull(codeGenerator.getConversionCode(Datatype.BOOLEAN, Datatype.PRIMITIVE_BOOLEAN,
                new JavaCodeFragment("TRUE"))); //$NON-NLS-1$

        assertNotNull(codeGenerator.getConversionCode(Datatype.PRIMITIVE_BOOLEAN, Datatype.BOOLEAN,
                new JavaCodeFragment("true"))); //$NON-NLS-1$

        assertNull(codeGenerator.getConversionCode(AnyDatatype.INSTANCE, Datatype.STRING, new JavaCodeFragment(
                "FromValue"))); //$NON-NLS-1$
        assertNotNull(codeGenerator.getConversionCode(Datatype.STRING, AnyDatatype.INSTANCE, new JavaCodeFragment(
                "FromValue"))); //$NON-NLS-1$

        assertNotNull(codeGenerator.getConversionCode(Datatype.STRING, Datatype.STRING, new JavaCodeFragment(
                "FromValue"))); //$NON-NLS-1$

        assertNull(codeGenerator.getConversionCode(null, Datatype.INTEGER, new JavaCodeFragment("true"))); //$NON-NLS-1$
        assertNull(codeGenerator.getConversionCode(Datatype.PRIMITIVE_BOOLEAN, null, new JavaCodeFragment("true"))); //$NON-NLS-1$
        assertNull(codeGenerator.getConversionCode(Datatype.BIG_DECIMAL, Datatype.GREGORIAN_CALENDAR,
                new JavaCodeFragment("FromValue"))); //$NON-NLS-1$
    }

    @Test
    public void testGetConversionCode_ListOfDatatype() {
        assertNotNull(codeGenerator.getConversionCode(createList(Datatype.DECIMAL), createList(AnyDatatype.INSTANCE),
                new JavaCodeFragment("Decimal.valueOf(2.3)"))); //$NON-NLS-1$

        assertNull(codeGenerator.getConversionCode(createList(Datatype.DECIMAL), createList(Datatype.INTEGER),
                new JavaCodeFragment("Decimal.valueOf(2.3)"))); //$NON-NLS-1$

        assertNull(codeGenerator.getConversionCode(createList(Datatype.DECIMAL), Datatype.INTEGER,
                new JavaCodeFragment("Decimal.valueOf(2.3)"))); //$NON-NLS-1$

        assertNull(codeGenerator.getConversionCode(Datatype.DECIMAL, createList(Datatype.INTEGER),
                new JavaCodeFragment("Decimal.valueOf(2.3)"))); //$NON-NLS-1$

        assertNull(codeGenerator.getConversionCode(createList(Datatype.BIG_DECIMAL),
                createList(Datatype.GREGORIAN_CALENDAR), new JavaCodeFragment("BigDecimal.valueOf(2.3)"))); //$NON-NLS-1$
    }

}
