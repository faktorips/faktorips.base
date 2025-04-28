/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.validation;

import static org.faktorips.runtime.data.TestPolicyWithVisitor.PROPERTY_BIGDECIMAL_ATTRIBUTE;
import static org.faktorips.runtime.data.TestPolicyWithVisitor.PROPERTY_DOUBLE_ATTRIBUTE;
import static org.faktorips.runtime.data.TestPolicyWithVisitor.PROPERTY_FLOAT_ATTRIBUTE;
import static org.faktorips.runtime.data.TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE;
import static org.faktorips.runtime.data.TestPolicyWithVisitor.PROPERTY_INT_ATTRIBUTE;
import static org.faktorips.runtime.data.TestPolicyWithVisitor.PROPERTY_LONG_ATTRIBUTE;
import static org.faktorips.runtime.data.TestPolicyWithVisitor.PROPERTY_SHORT_ATTRIBUTE;
import static org.faktorips.runtime.data.TestPolicyWithVisitor.PROPERTY_STRING_ATTRIBUTE;
import static org.faktorips.runtime.validation.DefaultDatabaseLengthValidationConfiguration.MSGCODE_NUMBER_EXCEEDS_PRECISION;
import static org.faktorips.runtime.validation.DefaultDatabaseLengthValidationConfiguration.MSGCODE_NUMBER_EXCEEDS_SCALE;
import static org.faktorips.runtime.validation.DefaultDatabaseLengthValidationConfiguration.MSGCODE_STRING_TOO_LONG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import java.util.Locale;

import org.faktorips.runtime.IMarker;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.data.TestDeckungWithVisitor;
import org.faktorips.runtime.data.TestPolicyWithVisitor;
import org.faktorips.values.Decimal;
import org.junit.Before;
import org.junit.Test;

public class DatabaseLengthValidationTest {
    private TestPolicyWithVisitor testPolicy;

    @Before
    public void setUp() {
        testPolicy = new TestPolicyWithVisitor();
    }

    @Test
    public void testValidateWithChildren_String() {
        testPolicy.setStringAttribute("12345678901234567890");
        TestDeckungWithVisitor coverage = testPolicy.newTestDeckung();
        coverage.setStringAttribute("123456789012345");
        TestDeckungWithVisitor coverage2 = testPolicy.newTestDeckung();
        coverage2.setStringAttribute("1234567890123456");
        DatabaseLengthValidation validation = DatabaseLengthValidation
                .with(new DefaultDatabaseLengthValidationConfiguration(Locale.ENGLISH)
                        .withStringLengthConstraint(10));

        MessageList ml = validation.validateWithChildren(testPolicy);

        assertThat(ml.size(), is(3));
        assertThat(ml.getMessage(0).getCode(),
                is(MSGCODE_STRING_TOO_LONG));
        assertThat(ml.getMessage(0).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(0).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(0).getInvalidObjectProperties().get(0).getProperty(), is(PROPERTY_STRING_ATTRIBUTE));
        assertThat(ml.getMessage(0).getText(), containsString(PROPERTY_STRING_ATTRIBUTE));
        assertThat(ml.getMessage(0).getText(), containsString("20"));
        assertThat(ml.getMessage(0).getText(), containsString("10"));
        assertThat(ml.getMessage(1).getCode(), is(MSGCODE_STRING_TOO_LONG));
        assertThat(ml.getMessage(1).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(1).getInvalidObjectProperties().get(0).getObject(), is(coverage));
        assertThat(ml.getMessage(1).getInvalidObjectProperties().get(0).getProperty(),
                is(TestDeckungWithVisitor.PROPERTY_STRING_ATTRIBUTE));
        assertThat(ml.getMessage(1).getText(), containsString(TestDeckungWithVisitor.PROPERTY_STRING_ATTRIBUTE));
        assertThat(ml.getMessage(1).getText(), containsString("15"));
        assertThat(ml.getMessage(1).getText(), containsString("10"));
        assertThat(ml.getMessage(2).getCode(), is(MSGCODE_STRING_TOO_LONG));
        assertThat(ml.getMessage(2).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(2).getInvalidObjectProperties().get(0).getObject(), is(coverage2));
        assertThat(ml.getMessage(2).getInvalidObjectProperties().get(0).getProperty(),
                is(TestDeckungWithVisitor.PROPERTY_STRING_ATTRIBUTE));
        assertThat(ml.getMessage(2).getText(), containsString(TestDeckungWithVisitor.PROPERTY_STRING_ATTRIBUTE));
        assertThat(ml.getMessage(2).getText(), containsString("16"));
        assertThat(ml.getMessage(2).getText(), containsString("10"));
    }

    @Test
    public void testValidateWithChildren_Decimal_CoverageExceedsScale() {
        TestDeckungWithVisitor coverage = testPolicy.newTestDeckung();
        coverage.setDecimalAttribute(Decimal.valueOf("1.5622"));
        DatabaseLengthValidation validation = DatabaseLengthValidation
                .with(new DefaultDatabaseLengthValidationConfiguration(Locale.ENGLISH)
                        .withNumericConstraint(Decimal.class, 10, 2));

        MessageList ml = validation.validateWithChildren(testPolicy);

        assertThat(ml.size(), is(1));
        assertThat(ml.getMessage(0).getCode(), is(MSGCODE_NUMBER_EXCEEDS_SCALE));
        assertThat(ml.getMessage(0).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(0).getInvalidObjectProperties().get(0).getObject(), is(coverage));
        assertThat(ml.getMessage(0).getInvalidObjectProperties().get(0).getProperty(),
                is(TestDeckungWithVisitor.PROPERTY_DECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(0).getText(), containsString(TestDeckungWithVisitor.PROPERTY_DECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(0).getText(), containsString("2"));
        assertThat(ml.getMessage(0).getText(), containsString("4"));
    }

    @Test
    public void testValidateOnly_AllNumeric_WithinLimit() {
        testPolicy.setIntAttribute(1234);
        testPolicy.setIntegerAttribute(1234);
        testPolicy.setDecimalAttribute(Decimal.valueOf("1234.1234"));
        testPolicy.setBigDecimalAttribute(new BigDecimal("1234.1234"));
        testPolicy.setDoubleAttribute(12.2);
        testPolicy.setShortAttribute((short)0);
        testPolicy.setFloatAttribute(12.22f);
        testPolicy.setLongAttribute(1L);
        TestDeckungWithVisitor child = testPolicy.newTestDeckung();
        child.setDecimalAttribute(Decimal.valueOf("1234567890123456.123456"));
        DatabaseLengthValidation validation = DatabaseLengthValidation
                .with(new DefaultDatabaseLengthValidationConfiguration(Locale.ENGLISH)
                        .withNumericConstraintForAllNumbers(10, 10));

        MessageList ml = validation.validateOnly(testPolicy);

        assertThat(ml.size(), is(0));
    }

    @Test
    public void testValidateWithChildren_AllNumeric_ExceedLimit() {
        testPolicy.setIntAttribute(12345);
        testPolicy.setIntegerAttribute(1234);
        testPolicy.setBigDecimalAttribute(new BigDecimal("1234.12345"));
        testPolicy.setDoubleAttribute(1234.0);
        testPolicy.setShortAttribute((short)1234);
        testPolicy.setFloatAttribute(12.22f);
        testPolicy.setLongAttribute(1000L);

        TestDeckungWithVisitor child = testPolicy.newTestDeckung();
        child.setDecimalAttribute(Decimal.valueOf("12345.123456"));

        DatabaseLengthValidation validation = DatabaseLengthValidation
                .with(new DefaultDatabaseLengthValidationConfiguration(Locale.ENGLISH)
                        .withNumericConstraintForAllNumbers(3, 1));

        MessageList ml = validation.validateWithChildren(testPolicy);

        assertThat(ml.size(), is(11));
        assertThat(ml.getMessage(0).getCode(), is(MSGCODE_NUMBER_EXCEEDS_PRECISION));
        assertThat(ml.getMessage(0).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(0).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(0).getInvalidObjectProperties().get(0).getProperty(),
                is(PROPERTY_INT_ATTRIBUTE));
        assertThat(ml.getMessage(0).getText(),
                containsString(PROPERTY_INT_ATTRIBUTE));
        assertThat(ml.getMessage(0).getText(), containsString("5"));
        assertThat(ml.getMessage(0).getText(), containsString("3"));
        assertThat(ml.getMessage(1).getCode(), is(MSGCODE_NUMBER_EXCEEDS_PRECISION));
        assertThat(ml.getMessage(1).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(1).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(1).getInvalidObjectProperties().get(0).getProperty(), is(PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(ml.getMessage(1).getText(), containsString("Integer Attribute"));
        assertThat(ml.getMessage(1).getText(), containsString("4"));
        assertThat(ml.getMessage(1).getText(), containsString("3"));
        assertThat(ml.getMessage(2).getCode(), is(MSGCODE_NUMBER_EXCEEDS_PRECISION));
        assertThat(ml.getMessage(2).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(2).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(2).getInvalidObjectProperties().get(0).getProperty(),
                is(PROPERTY_BIGDECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(2).getText(), containsString(PROPERTY_BIGDECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(2).getText(), containsString("9"));
        assertThat(ml.getMessage(2).getText(), containsString("3"));
        assertThat(ml.getMessage(3).getCode(), is(MSGCODE_NUMBER_EXCEEDS_SCALE));
        assertThat(ml.getMessage(3).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(3).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(3).getInvalidObjectProperties().get(0).getProperty(),
                is(PROPERTY_BIGDECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(3).getText(), containsString(PROPERTY_BIGDECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(3).getText(), containsString("5"));
        assertThat(ml.getMessage(3).getText(), containsString("1"));
        assertThat(ml.getMessage(4).getCode(), is(MSGCODE_NUMBER_EXCEEDS_PRECISION));
        assertThat(ml.getMessage(4).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(4).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(4).getInvalidObjectProperties().get(0).getProperty(), is(PROPERTY_SHORT_ATTRIBUTE));
        assertThat(ml.getMessage(4).getText(), containsString(PROPERTY_SHORT_ATTRIBUTE));
        assertThat(ml.getMessage(4).getText(), containsString("4"));
        assertThat(ml.getMessage(4).getText(), containsString("3"));
        assertThat(ml.getMessage(5).getCode(), is(MSGCODE_NUMBER_EXCEEDS_PRECISION));
        assertThat(ml.getMessage(5).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(5).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(5).getInvalidObjectProperties().get(0).getProperty(), is(PROPERTY_LONG_ATTRIBUTE));
        assertThat(ml.getMessage(5).getText(), containsString(PROPERTY_LONG_ATTRIBUTE));
        assertThat(ml.getMessage(5).getText(), containsString("4"));
        assertThat(ml.getMessage(5).getText(), containsString("3"));
        assertThat(ml.getMessage(6).getCode(), is(MSGCODE_NUMBER_EXCEEDS_PRECISION));
        assertThat(ml.getMessage(6).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(6).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(6).getInvalidObjectProperties().get(0).getProperty(), is(PROPERTY_FLOAT_ATTRIBUTE));
        assertThat(ml.getMessage(6).getText(), containsString(PROPERTY_FLOAT_ATTRIBUTE));
        assertThat(ml.getMessage(6).getText(), containsString("4"));
        assertThat(ml.getMessage(6).getText(), containsString("3"));
        assertThat(ml.getMessage(7).getCode(), is(MSGCODE_NUMBER_EXCEEDS_SCALE));
        assertThat(ml.getMessage(7).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(7).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(7).getInvalidObjectProperties().get(0).getProperty(), is(PROPERTY_FLOAT_ATTRIBUTE));
        assertThat(ml.getMessage(7).getText(), containsString(PROPERTY_FLOAT_ATTRIBUTE));
        assertThat(ml.getMessage(7).getText(), containsString("2"));
        assertThat(ml.getMessage(7).getText(), containsString("1"));
        assertThat(ml.getMessage(8).getCode(), is(MSGCODE_NUMBER_EXCEEDS_PRECISION));
        assertThat(ml.getMessage(8).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(8).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(8).getInvalidObjectProperties().get(0).getProperty(), is(PROPERTY_DOUBLE_ATTRIBUTE));
        assertThat(ml.getMessage(8).getText(), containsString(PROPERTY_DOUBLE_ATTRIBUTE));
        assertThat(ml.getMessage(8).getText(), containsString("5"));
        assertThat(ml.getMessage(8).getText(), containsString("3"));
        assertThat(ml.getMessage(9).getCode(), is(MSGCODE_NUMBER_EXCEEDS_PRECISION));
        assertThat(ml.getMessage(9).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(9).getInvalidObjectProperties().get(0).getObject(), is(child));
        assertThat(ml.getMessage(9).getInvalidObjectProperties().get(0).getProperty(),
                is(TestDeckungWithVisitor.PROPERTY_DECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(9).getText(), containsString(TestDeckungWithVisitor.PROPERTY_DECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(9).getText(), containsString("11"));
        assertThat(ml.getMessage(9).getText(), containsString("3"));
        assertThat(ml.getMessage(10).getCode(), is(MSGCODE_NUMBER_EXCEEDS_SCALE));
        assertThat(ml.getMessage(10).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(10).getInvalidObjectProperties().get(0).getObject(), is(child));
        assertThat(ml.getMessage(10).getInvalidObjectProperties().get(0).getProperty(),
                is(TestDeckungWithVisitor.PROPERTY_DECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(10).getText(), containsString(TestDeckungWithVisitor.PROPERTY_DECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(10).getText(), containsString("6"));
        assertThat(ml.getMessage(10).getText(), containsString("1"));
    }

    @Test
    public void testValidateOnly_DecimalAndStringExceedLimit() {
        testPolicy.setStringAttribute("12345678901234567890");
        testPolicy.setBigDecimalAttribute(new BigDecimal("123.456"));

        DatabaseLengthValidation validation = DatabaseLengthValidation
                .with(new DefaultDatabaseLengthValidationConfiguration(Locale.ENGLISH)
                        .withStringLengthConstraint(10)
                        .withNumericConstraint(BigDecimal.class, 2, 1));

        MessageList ml = validation.validateOnly(testPolicy);

        assertThat(ml.size(), is(3));
        assertThat(ml.getMessage(0).getCode(), is(MSGCODE_STRING_TOO_LONG));
        assertThat(ml.getMessage(0).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(0).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(0).getInvalidObjectProperties().get(0).getProperty(),
                is(PROPERTY_STRING_ATTRIBUTE));
        assertThat(ml.getMessage(0).getText(), containsString(PROPERTY_STRING_ATTRIBUTE));
        assertThat(ml.getMessage(0).getText(), containsString("20"));
        assertThat(ml.getMessage(0).getText(), containsString("10"));
        assertThat(ml.getMessage(1).getCode(), is(MSGCODE_NUMBER_EXCEEDS_PRECISION));
        assertThat(ml.getMessage(1).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(1).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(1).getInvalidObjectProperties().get(0).getProperty(),
                is(PROPERTY_BIGDECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(1).getText(), containsString(PROPERTY_BIGDECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(1).getText(), containsString("6"));
        assertThat(ml.getMessage(1).getText(), containsString("2"));
        assertThat(ml.getMessage(2).getCode(), is(MSGCODE_NUMBER_EXCEEDS_SCALE));
        assertThat(ml.getMessage(2).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(2).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(2).getInvalidObjectProperties().get(0).getProperty(),
                is(PROPERTY_BIGDECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(2).getText(), containsString(PROPERTY_BIGDECIMAL_ATTRIBUTE));
        assertThat(ml.getMessage(2).getText(), containsString("3"));
        assertThat(ml.getMessage(2).getText(), containsString("1"));
    }

    @Test
    public void testValidateWithChildren_FiltersAttributes() {
        testPolicy.setIntAttribute(12345);
        testPolicy.setIntegerAttribute(1234);

        DatabaseLengthValidation validation = DatabaseLengthValidation
                .with(new DefaultDatabaseLengthValidationConfiguration(Locale.ENGLISH)
                        .withNumericConstraint(Integer.class, 3, 1)
                        .withAttributeFilter(
                                (a, m) -> a.getName().equals(PROPERTY_INT_ATTRIBUTE)));

        MessageList ml = validation.validateWithChildren(testPolicy);

        assertThat(ml.size(), is(1));
        assertThat(ml.getMessage(0).getCode(),
                is(MSGCODE_NUMBER_EXCEEDS_PRECISION));
        assertThat(ml.getMessage(0).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(0).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(0).getInvalidObjectProperties().get(0).getProperty(),
                is(PROPERTY_INT_ATTRIBUTE));
        assertThat(ml.getMessage(0).getText(), containsString(PROPERTY_INT_ATTRIBUTE));
        assertThat(ml.getMessage(0).getText(), containsString("5"));
        assertThat(ml.getMessage(0).getText(), containsString("3"));
    }

    @Test
    public void testValidateWithChildren_TranslatesLabel() {
        testPolicy.setIntegerAttribute(1234);

        DatabaseLengthValidation validation = DatabaseLengthValidation
                .with(new DefaultDatabaseLengthValidationConfiguration(Locale.GERMAN)
                        .withNumericConstraint(Integer.class, 3, 1));

        MessageList ml = validation.validateWithChildren(testPolicy);

        assertThat(ml.size(), is(1));
        assertThat(ml.getMessage(0).getCode(),
                is(MSGCODE_NUMBER_EXCEEDS_PRECISION));
        assertThat(ml.getMessage(0).getNumOfInvalidObjectProperties(), is(1));
        assertThat(ml.getMessage(0).getInvalidObjectProperties().get(0).getObject(), is(testPolicy));
        assertThat(ml.getMessage(0).getInvalidObjectProperties().get(0).getProperty(),
                is(PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(ml.getMessage(0).getText(), containsString("Integer-Attribut"));
        assertThat(ml.getMessage(0).getText(), containsString("\u00FCberschreitet"));
        assertThat(ml.getMessage(0).getText(), containsString("4"));
        assertThat(ml.getMessage(0).getText(), containsString("3"));
    }

    @Test
    public void testValidateOnly_SetsMarker() {
        testPolicy.setStringAttribute("12345678901234567890");
        testPolicy.setBigDecimalAttribute(new BigDecimal("123.456"));

        DatabaseLengthValidation validation = DatabaseLengthValidation
                .with(new DefaultDatabaseLengthValidationConfiguration(Locale.ENGLISH)
                        .withStringLengthConstraint(10)
                        .withNumericConstraint(BigDecimal.class, 2, 1)
                        .withTechnicalConstraintViolationMarker(MyMarker.TechnicalConstraintViolated));

        MessageList ml = validation.validateOnly(testPolicy);

        assertThat(ml.size(), is(3));
        assertThat(ml.getMessage(0).getCode(), is(MSGCODE_STRING_TOO_LONG));
        assertThat(ml.getMessage(0).getMarkers(), contains(MyMarker.TechnicalConstraintViolated));
        assertThat(ml.getMessage(1).getCode(), is(MSGCODE_NUMBER_EXCEEDS_PRECISION));
        assertThat(ml.getMessage(1).getMarkers(), contains(MyMarker.TechnicalConstraintViolated));
        assertThat(ml.getMessage(2).getCode(), is(MSGCODE_NUMBER_EXCEEDS_SCALE));
        assertThat(ml.getMessage(2).getMarkers(), contains(MyMarker.TechnicalConstraintViolated));
    }

    private enum MyMarker implements IMarker {
        TechnicalConstraintViolated {
            @Override
            public boolean isRequiredInformationMissing() {
                return false;
            }

            @Override
            public boolean isTechnicalConstraintViolated() {
                return true;
            }
        };

    }

}
