package org.faktorips.runtime.validation;

import static org.hamcrest.CoreMatchers.is;
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
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.faktorips.valueset.IntegerRange;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;
import org.junit.Test;

public class RelevanceTest {

    @Test
    public void testIsIrrelevantIModelObjectString() {
        TestPolicy modelObject = new TestPolicy();

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));
    }

    @Test
    public void testIsIrrelevantIModelObjectPolicyAttribute() {
        TestPolicy modelObject = new TestPolicy();
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.isIrrelevant(modelObject, policyAttribute), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.isIrrelevant(modelObject, policyAttribute), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.isIrrelevant(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.isIrrelevant(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.isIrrelevant(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.isIrrelevant(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.isIrrelevant(modelObject, policyAttribute), is(true));
    }

    @Test
    public void testIsMandatoryIModelObjectString() {
        TestPolicy modelObject = new TestPolicy();

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.isMandatory(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.isMandatory(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.isMandatory(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.isMandatory(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.isMandatory(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>(false));
        assertThat(Relevance.isMandatory(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.isMandatory(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10, 2, true));
        assertThat(Relevance.isMandatory(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.isMandatory(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));
    }

    @Test
    public void testIsMandatoryIModelObjectPolicyAttribute() {
        TestPolicy modelObject = new TestPolicy();
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.isMandatory(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.isMandatory(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.isMandatory(modelObject, policyAttribute), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.isMandatory(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.isMandatory(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>(false));
        assertThat(Relevance.isMandatory(modelObject, policyAttribute), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.isMandatory(modelObject, policyAttribute), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10, 2, true));
        assertThat(Relevance.isMandatory(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.isMandatory(modelObject, policyAttribute), is(false));
    }

    @Test
    public void testIsOptionalIModelObjectString() {
        TestPolicy modelObject = new TestPolicy();

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.isOptional(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.isOptional(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.isOptional(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.isOptional(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.isOptional(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>(false));
        assertThat(Relevance.isOptional(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.isOptional(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10, 2, true));
        assertThat(Relevance.isOptional(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.isOptional(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));
    }

    @Test
    public void testIsOptionalIModelObjectPolicyAttribute() {
        TestPolicy modelObject = new TestPolicy();
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.isOptional(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.isOptional(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.isOptional(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.isOptional(modelObject, policyAttribute), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.isOptional(modelObject, policyAttribute), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>(false));
        assertThat(Relevance.isOptional(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.isOptional(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10, 2, true));
        assertThat(Relevance.isOptional(modelObject, policyAttribute), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.isOptional(modelObject, policyAttribute), is(false));
    }

    @Test
    public void testIsRelevantIModelObjectString() {
        TestPolicy modelObject = new TestPolicy();

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.isRelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.isRelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.isRelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.isRelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.isRelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.isRelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.isRelevant(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(false));
    }

    @Test
    public void testIsRelevantIModelObjectPolicyAttribute() {
        TestPolicy modelObject = new TestPolicy();
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.isRelevant(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.isRelevant(modelObject, policyAttribute), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.isRelevant(modelObject, policyAttribute), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.isRelevant(modelObject, policyAttribute), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.isRelevant(modelObject, policyAttribute), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.isRelevant(modelObject, policyAttribute), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.isRelevant(modelObject, policyAttribute), is(false));
    }

    @Test
    public void testOfIModelObjectString() {
        TestPolicy modelObject = new TestPolicy();

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.of(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(Relevance.IRRELEVANT));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.of(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(Relevance.IRRELEVANT));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.of(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(Relevance.MANDATORY));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.of(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(Relevance.OPTIONAL));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.of(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(Relevance.OPTIONAL));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>(false));
        assertThat(Relevance.of(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(Relevance.MANDATORY));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.of(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(Relevance.MANDATORY));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10, 2, true));
        assertThat(Relevance.of(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(Relevance.OPTIONAL));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.of(modelObject, TestPolicy.PROPERTY_INTEGER_ATTRIBUTE), is(Relevance.IRRELEVANT));
    }

    @Test
    public void testOfIModelObjectPolicyAttribute() {
        TestPolicy modelObject = new TestPolicy();
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.of(modelObject, policyAttribute), is(Relevance.IRRELEVANT));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.of(modelObject, policyAttribute), is(Relevance.IRRELEVANT));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.of(modelObject, policyAttribute), is(Relevance.MANDATORY));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.of(modelObject, policyAttribute), is(Relevance.OPTIONAL));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.of(modelObject, policyAttribute), is(Relevance.OPTIONAL));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>(false));
        assertThat(Relevance.of(modelObject, policyAttribute), is(Relevance.MANDATORY));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.of(modelObject, policyAttribute), is(Relevance.MANDATORY));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10, 2, true));
        assertThat(Relevance.of(modelObject, policyAttribute), is(Relevance.OPTIONAL));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.of(modelObject, policyAttribute), is(Relevance.IRRELEVANT));
    }

    @IpsPolicyCmptType(name = "TestPolicy")
    @IpsAttributes({ "IntegerAttribute" })
    public static class TestPolicy implements IModelObject {

        public static final String PROPERTY_INTEGER_ATTRIBUTE = "IntegerAttribute";

        private Integer integerAttribute;
        private ValueSet<Integer> allowedValuesForIntegerAttribute = new UnrestrictedValueSet<>();

        @IpsAttribute(name = "IntegerAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        @IpsConfiguredAttribute(changingOverTime = false)
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
