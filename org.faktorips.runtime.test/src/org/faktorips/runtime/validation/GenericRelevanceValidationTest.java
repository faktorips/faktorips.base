package org.faktorips.runtime.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;
import org.junit.Test;

public class GenericRelevanceValidationTest {

    @Test
    public void testValidate_MissingMandatoryValue() {
        TestPolicy modelObject = new TestPolicy();
        ValueSet<Integer> valueSet = new UnrestrictedValueSet<>(false);
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject, policyAttribute);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.size(), is(1));
        assertThat(messageList.getMessage(0).getCode(),
                startsWith(GenericRelevanceValidation.ERROR_MANDATORY_MSG_CODE_PREFIX));
        assertThat(messageList.getMessage(0).getText(), is("Integer Attribute is a mandatory field."));

    }

    @Test
    public void testValidate_ValuePresentForIgnoredAttribute_Null() {
        TestPolicy modelObject = new TestPolicy();
        modelObject.setIntegerAttribute(1);
        ValueSet<Integer> valueSet = null;
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject, policyAttribute);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.size(), is(1));
        assertThat(messageList.getMessage(0).getCode(),
                startsWith(GenericRelevanceValidation.ERROR_IRRELEVANT_MSG_CODE_PREFIX));
        assertThat(messageList.getMessage(0).getText(), is("No value can be specified for Integer Attribute."));
    }

    @Test
    public void testValidate_ValuePresentForIgnoredAttribute_Empty() {
        TestPolicy modelObject = new TestPolicy();
        modelObject.setIntegerAttribute(1);
        ValueSet<Integer> valueSet = OrderedValueSet.empty();
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject, policyAttribute);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.size(), is(1));
        assertThat(messageList.getMessage(0).getCode(),
                startsWith(GenericRelevanceValidation.ERROR_IRRELEVANT_MSG_CODE_PREFIX));
        assertThat(messageList.getMessage(0).getText(), is("No value can be specified for Integer Attribute."));
    }

    @Test
    public void testValidate_ValueNotInAllowedValueSet() {
        TestPolicy modelObject = new TestPolicy();
        modelObject.setIntegerAttribute(1);
        ValueSet<Integer> valueSet = OrderedValueSet.of(2, 3, 4);
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject, policyAttribute);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.size(), is(1));
        assertThat(messageList.getMessage(0).getCode(),
                startsWith(GenericRelevanceValidation.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(messageList.getMessage(0).getText(), is("The value for Integer Attribute is out of range."));
    }

    @IpsPolicyCmptType(name = "TestPolicy")
    @IpsAttributes({ "IntegerAttribute" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.validation.GenericRelevanceValidationTest", defaultLocale = "de")
    public static class TestPolicy implements IModelObject {

        public static final String PROPERTY_INTEGER_ATTRIBUTE = "IntegerAttribute";

        private Integer integerAttribute;
        private ValueSet<Integer> allowedValuesForIntegerAttribute = new UnrestrictedValueSet<>();

        @IpsAttribute(name = "IntegerAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
        @IpsConfiguredAttribute(changingOverTime = true)
        public Integer getIntegerAttribute() {
            return integerAttribute;
        }

        @IpsAttributeSetter("IntegerAttribute")
        public void setIntegerAttribute(Integer i) {
            integerAttribute = i;
        }

        @IpsAllowedValues("IntegerAttribute")
        public ValueSet<Integer> getAllowedValuesForIntegerAttribute() {
            return allowedValuesForIntegerAttribute;
        }

        public void setAllowedValuesForIntegerAttribute(ValueSet<Integer> allowedValuesForIntegerAttribute) {
            this.allowedValuesForIntegerAttribute = allowedValuesForIntegerAttribute;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
    }
}
