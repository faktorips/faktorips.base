/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
import org.faktorips.datatype.joda.LocalTimeDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.inputformat.DateISOStringFormatFactory;
import org.faktorips.devtools.core.ui.inputformat.DateTimeISOStringFormatFactory;
import org.faktorips.devtools.core.ui.inputformat.IDatatypeInputFormatFactory;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.core.ui.inputformat.TimeISOStringFormatFactory;
import org.faktorips.devtools.core.ui.inputformat.parse.Messages;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UIDatatypeFormatterTest {

    @Mock
    private IIpsProject ipsProject;

    private static Locale savedFormattingLocale;

    private UIDatatypeFormatter formatter;

    @BeforeClass
    public static void saveLocale() {
        savedFormattingLocale = IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormattingLocale();
    }

    @Before
    public void setUp() {
        IpsPlugin.getDefault().getIpsPreferences().setDatatypeFormattingLocale(Locale.GERMAN);
        formatter = new UIDatatypeFormatter();
    }

    @AfterClass
    public static void cleanUp() {
        IpsPlugin.getDefault().getIpsPreferences().setDatatypeFormattingLocale(savedFormattingLocale);
    }

    @Test
    public void testFormatDecimal() {
        assertValueDatatypeFormatting(Datatype.DECIMAL, "-1000.2", "-1.000,2");
        assertValueDatatypeFormatting(Datatype.BIG_DECIMAL, "-1000.2", "-1.000,2");
        assertValueDatatypeFormatting(Datatype.DOUBLE, "-1000.2", "-1.000,2");
    }

    @Test
    public void testFormatInt() {
        assertValueDatatypeFormatting(Datatype.INTEGER, "-42", "-42");
        assertValueDatatypeFormatting(Datatype.LONG, "-42", "-42");
        assertValueDatatypeFormatting(Datatype.PRIMITIVE_INT, "-42", "-42");
        assertValueDatatypeFormatting(Datatype.PRIMITIVE_LONG, "-42", "-42");
    }

    @Test
    public void testFormatMoney() {
        assertValueDatatypeFormatting(Datatype.MONEY, "-1000.25 EUR", "-1.000,25 EUR");
    }

    @Test
    public void testFormatDate() {
        assertValueDatatypeFormatting(Datatype.GREGORIAN_CALENDAR, "2013-11-01", "01.11.2013");
        assertValueDatatypeFormatting(new DateDatatype(), "2013-11-01", "01.11.2013");
    }

    @Test
    public void testFormatLocalDate_validInput() {
        String validInput = "2013-11-13";
        assertValueDataTypeAndInputFormatFactoryFormatting(new DateISOStringFormatFactory(), new LocalDateDatatype(),
                validInput);
    }

    @Test
    public void testFormatLocalDate_invalidInput() {
        String invalidInput = "2013-11-40";
        assertValueDataTypeAndInputFormatFactoryFormatting(new DateISOStringFormatFactory(), new LocalDateDatatype(),
                invalidInput);
    }

    @Test
    public void testFormatLocalTime_validInput() {
        String invalidInput = "09:30:30";
        assertValueDataTypeAndInputFormatFactoryFormatting(new TimeISOStringFormatFactory(), new LocalTimeDatatype(),
                invalidInput);
    }

    @Test
    public void testFormatLocalTime_invalidInput() {
        String validInput = "09.90.30";
        assertValueDataTypeAndInputFormatFactoryFormatting(new TimeISOStringFormatFactory(), new LocalTimeDatatype(),
                validInput);
    }

    @Test
    public void testFormatLocalDateTime_validInput() {
        String validInput = "2013-11-13 09:30:30";
        assertValueDataTypeAndInputFormatFactoryFormatting(new DateTimeISOStringFormatFactory(),
                new LocalDateTimeDatatype(), validInput);
    }

    @Test
    public void testFormatLocalDateTime_invalidInput() {
        String invalidInput = "2013-11-40 80:30:30";
        assertValueDataTypeAndInputFormatFactoryFormatting(new DateTimeISOStringFormatFactory(),
                new LocalDateTimeDatatype(), invalidInput);
    }

    private void assertValueDataTypeAndInputFormatFactoryFormatting(IDatatypeInputFormatFactory factory,
            ValueDatatype datatype,
            String expected) {
        IInputFormat<String> localeTimeFormat = factory.newInputFormat(datatype, ipsProject);
        String actual = localeTimeFormat.format(expected);
        assertValueDatatypeFormatting(datatype, expected, actual);
    }

    private void assertValueDatatypeFormatting(ValueDatatype datatype, String value, String expectedFormattedValue) {
        String formattedValue = formatter.formatValue(datatype, value);
        assertEquals(expectedFormattedValue, formattedValue);
    }

    @Test
    public void testDefaultInputFormat() {
        assertValueDatatypeFormatting(Datatype.STRING, "anyString", "anyString");
    }

    @Test
    public void testFormatEnumValueSet() throws Exception {
        assertEnumValuesetFormatting(new String[] { "1", "two", "three" }, "1 | two | three");
    }

    @Test
    public void testFormatEmptyEnumValueSet() throws Exception {
        assertEnumValuesetFormatting(new String[0], "{}");
    }

    private void assertEnumValuesetFormatting(String[] enumValues, String expectedFormat) throws Exception {
        IEnumValueSet enumValueSet = Mockito.mock(IEnumValueSet.class);
        when(enumValueSet.isEnum()).thenReturn(true);
        when(enumValueSet.getValues()).thenReturn(enumValues);
        IValueSetOwner valueSetOwner = mock(IValueSetOwner.class);
        when(enumValueSet.getValueSetOwner()).thenReturn(valueSetOwner);
        when(valueSetOwner.findValueDatatype(ipsProject)).thenReturn(Datatype.STRING);

        UIDatatypeFormatter formatter = new UIDatatypeFormatter();
        String formatString = formatter.formatValueSet(enumValueSet);
        assertEquals(expectedFormat, formatString);
    }

    @Test
    public void testFormatRangeValueSet() throws Exception {
        RangeValueSet rangeValueSet = mockRangeValueSet();
        Mockito.when(rangeValueSet.getLowerBound()).thenReturn("1");
        Mockito.when(rangeValueSet.getUpperBound()).thenReturn("11");
        Mockito.when(rangeValueSet.getStep()).thenReturn("5");
        Mockito.when(rangeValueSet.isContainsNull()).thenReturn(true);

        UIDatatypeFormatter formatter = new UIDatatypeFormatter();
        String formatString = formatter.formatValueSet(rangeValueSet);
        String message = NLS.bind(Messages.RangeValueSetFormat_includingNull, IpsPlugin.getDefault()
                .getIpsPreferences().getNullPresentation());
        assertEquals("[1 ... 11 / 5] " + message, formatString);
    }

    @Test
    public void testFormatRangeValueSetUnlimited() throws Exception {
        RangeValueSet rangeValueSet = mockRangeValueSet();
        Mockito.when(rangeValueSet.getLowerBound()).thenReturn("1");
        Mockito.when(rangeValueSet.getUpperBound()).thenReturn(null);
        Mockito.when(rangeValueSet.getStep()).thenReturn("5");
        Mockito.when(rangeValueSet.isContainsNull()).thenReturn(false);

        UIDatatypeFormatter formatter = new UIDatatypeFormatter();
        String formatString = formatter.formatValueSet(rangeValueSet);
        assertEquals("[1 ... * / 5]", formatString);
    }

    @Test
    public void testFormatRangeValueSet_noStep() {
        RangeValueSet rangeValueSet = mockRangeValueSet();
        Mockito.when(rangeValueSet.getLowerBound()).thenReturn("1");
        Mockito.when(rangeValueSet.getUpperBound()).thenReturn("10");
        Mockito.when(rangeValueSet.getStep()).thenReturn(null);
        Mockito.when(rangeValueSet.isContainsNull()).thenReturn(false);

        UIDatatypeFormatter formatter = new UIDatatypeFormatter();
        String formatString = formatter.formatValueSet(rangeValueSet);
        assertEquals("[1 ... 10]", formatString);
    }

    private RangeValueSet mockRangeValueSet() {
        RangeValueSet rangeValueSet = mock(RangeValueSet.class);
        when(rangeValueSet.isRange()).thenReturn(true);
        IValueSetOwner valueSetOwner = mock(IValueSetOwner.class);
        when(rangeValueSet.getValueSetOwner()).thenReturn(valueSetOwner);
        when(valueSetOwner.findValueDatatype(ipsProject)).thenReturn(Datatype.INTEGER);
        return rangeValueSet;
    }

}
