package org.faktorips.runtime.validation;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Locale;

import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;
import org.junit.Test;

public class GenericRelevanceValidationTest {

    @Test
    public void testValidate_MissingMandatoryValue() {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        TestPolicy modelObject = new TestPolicy();
        ValueSet<Integer> valueSet = new UnrestrictedValueSet<>(false);
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject, policyAttribute,
                config);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.size(), is(1));

        Message message = messageList.getMessage(0);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_MANDATORY_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" must contain a value."));
    }

    @Test
    public void testValidate_ValuePresentForIgnoredAttribute_Null() {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setIntegerAttribute(1);
        ValueSet<Integer> valueSet = null;
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject, policyAttribute,
                config);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.size(), is(1));

        Message message = messageList.getMessage(0);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_IRRELEVANT_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" must not contain a value."));
    }

    @Test
    public void testValidate_ValuePresentForIgnoredAttribute_Empty() {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setIntegerAttribute(1);
        ValueSet<Integer> valueSet = OrderedValueSet.empty();
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject, policyAttribute,
                config);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.size(), is(1));

        Message message = messageList.getMessage(0);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_IRRELEVANT_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" must not contain a value."));
    }

    @Test
    public void testValidate_ValueNotInAllowedValueSet() {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setIntegerAttribute(1);
        ValueSet<Integer> valueSet = OrderedValueSet.of(2, 3, 4);
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject, policyAttribute,
                config);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.size(), is(1));

        Message message = messageList.getMessage(0);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" contains an invalid value."));
    }
}
