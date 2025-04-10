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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.math.BigDecimal;
import java.util.Locale;

import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ValidationContext;
import org.faktorips.runtime.data.TestDeckungWithVisitor;
import org.faktorips.runtime.data.TestPolicyWithVisitor;
import org.faktorips.values.Decimal;
import org.junit.Before;
import org.junit.Test;

public class DatabaseLengthValidationUtilTest {
    private TestPolicyWithVisitor testPolicy;
    private MessageList messageList;
    private ValidationContext context;

    @Before
    public void setUp() {
        testPolicy = new TestPolicyWithVisitor();
        messageList = new MessageList();
        context = new ValidationContext(Locale.ENGLISH);
    }

    @Test
    public void testValidateStringLength_PolicyWithCoveragesUsingVisitor() {

        testPolicy.setStringAttribute("12345678901234567890");

        TestDeckungWithVisitor coverage = testPolicy.newTestDeckung();
        coverage.setStringAttribute("123456789012345");

        DatabaseLengthValidationUtil.validateStringLength(messageList, context, testPolicy, 10);
        DatabaseLengthValidationUtil.validateStringLength(messageList, context, coverage, 12);
        assertThat(messageList.size(), is(2));
    }

    @Test
    public void testValidateDecimal_CoverageExceedsScale() {
        TestDeckungWithVisitor coverage = testPolicy.newTestDeckung();
        coverage.setDecimalAttribute(Decimal.valueOf("1.5622"));

        DatabaseLengthValidationUtil.validateAttributes(messageList, context, coverage, Decimal.class, 10, 2);

        assertThat(messageList.size(), is(1));
        Message decimalError = messageList
                .getMessageByCode(NumericValidation.MSGCODE_NUMBER_EXCEEDS_PRECISION_OR_SCALE);
        assertThat(decimalError, is(not(nullValue())));
    }

    @Test
    public void testValidateAllNumeric_WithinLimit() {

        testPolicy.setIntAttribute(1234);
        testPolicy.setIntegerAttribute(1234);
        testPolicy.setDecimalAttribute(Decimal.valueOf("1234.1234"));
        testPolicy.setBigDecimalAttribute(new BigDecimal("1234.1234"));
        testPolicy.setDoubleAttribute(12.2);
        testPolicy.setShortAttribute((short)0);
        testPolicy.setByteAttribute((byte)0);
        testPolicy.setFloatAttribute(12.22f);
        testPolicy.setLongAttribute(1L);
        DatabaseLengthValidationUtil.validateAllNumeric(messageList, context, testPolicy, 10, 10);

        assertThat(messageList.size(), is(0));
    }

    @Test
    public void testValidateAllNumeric_ExceedLimit() {

        testPolicy.setIntegerAttribute(1234);
        testPolicy.setDecimalAttribute(Decimal.valueOf("1234.1234"));
        testPolicy.setBigDecimalAttribute(new BigDecimal("1234.1234"));
        testPolicy.setDoubleAttribute(12.2);
        testPolicy.setShortAttribute((short)23);
        testPolicy.setByteAttribute((byte)23);
        testPolicy.setFloatAttribute(12212.22f);
        testPolicy.setLongAttribute(121L);

        DatabaseLengthValidationUtil.validateAllNumeric(messageList, context, testPolicy, 0, 0);

        assertThat(messageList.size(), is(8));

        Message error = messageList
                .getMessageByCode(NumericValidation.MSGCODE_NUMBER_EXCEEDS_PRECISION_OR_SCALE);
        assertThat(error, is(not(nullValue())));

        error = messageList
                .getMessageByCode(NumericValidation.MSGCODE_INTEGERTYPE_EXCEEDS_PRECISION);
        assertThat(error, is(not(nullValue())));
    }

    @Test
    public void testValidateAttributes_BigDecimalExceedsPrecision() {

        testPolicy.setBigDecimalAttribute(new BigDecimal("12346789.56"));

        DatabaseLengthValidationUtil.validateAttributes(messageList, context, testPolicy, BigDecimal.class, 5, 2);

        assertThat(messageList.size(), is(1));
        Message error = messageList
                .getMessageByCode(NumericValidation.MSGCODE_NUMBER_EXCEEDS_PRECISION_OR_SCALE);
        assertThat(error, is(not(nullValue())));

    }

    @Test
    public void testValidateAttributes_BigDecimalExceedsScale() {

        testPolicy.setBigDecimalAttribute(new BigDecimal("1.5622"));

        DatabaseLengthValidationUtil.validateAttributes(messageList, context, testPolicy, BigDecimal.class, 10, 2);

        assertThat(messageList.size(), is(1));
        Message error = messageList
                .getMessageByCode(NumericValidation.MSGCODE_NUMBER_EXCEEDS_PRECISION_OR_SCALE);
        assertThat(error, is(not(nullValue())));

    }

    @Test
    public void testValidate_DecimalAndStringExceedLimit() {

        testPolicy.setStringAttribute("12345678901234567890");
        testPolicy.setBigDecimalAttribute(new BigDecimal("1.5622"));

        DatabaseLengthValidationUtil.validate(messageList, context, testPolicy, 10, 0, 0);

        assertThat(messageList.size(), is(2));
        Message error = messageList
                .getMessageByCode(NumericValidation.MSGCODE_NUMBER_EXCEEDS_PRECISION_OR_SCALE);
        assertThat(error, is(not(nullValue())));

        error = messageList.getMessageByCode(StringLengthValidation.MSGCODE_STRING_TOO_LONG);
        assertThat(error, is(not(nullValue())));
    }

    @Test
    public void testValidate_BigDecimalAndStringWithinLimit() {

        testPolicy.setStringAttribute("12");
        testPolicy.setBigDecimalAttribute(new BigDecimal("1.5622"));

        DatabaseLengthValidationUtil.validate(messageList, context, testPolicy, 10, 10, 10);

        assertThat(messageList.size(), is(0));
    }

}
