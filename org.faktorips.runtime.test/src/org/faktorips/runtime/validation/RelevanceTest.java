package org.faktorips.runtime.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.runtime.data.TestEnum;
import org.faktorips.runtime.data.TestPolicyWithVisitor;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.values.Money;
import org.faktorips.valueset.IntegerRange;
import org.faktorips.valueset.LongRange;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

public class RelevanceTest {

    @Test
    public void testIsIrrelevantIModelObjectString() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.isIrrelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));
    }

    @Test
    public void testIsIrrelevantIModelObjectPolicyAttribute() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);

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
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.isMandatory(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.isMandatory(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.isMandatory(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.isMandatory(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.isMandatory(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>(false));
        assertThat(Relevance.isMandatory(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.isMandatory(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10, 2, true));
        assertThat(Relevance.isMandatory(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.isMandatory(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));
    }

    @Test
    public void testIsMandatoryIModelObjectPolicyAttribute() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);

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
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.isOptional(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.isOptional(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.isOptional(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.isOptional(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.isOptional(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>(false));
        assertThat(Relevance.isOptional(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.isOptional(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10, 2, true));
        assertThat(Relevance.isOptional(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.isOptional(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));
    }

    @Test
    public void testIsOptionalIModelObjectPolicyAttribute() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);

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
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.isRelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.isRelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.isRelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.isRelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.isRelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.isRelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(true));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.isRelevant(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(false));
    }

    @Test
    public void testIsRelevantIModelObjectPolicyAttribute() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);

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
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();

        modelObject.setAllowedValuesForIntegerAttribute(null);
        assertThat(Relevance.of(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE),
                is(Relevance.IRRELEVANT));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.empty());
        assertThat(Relevance.of(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE),
                is(Relevance.IRRELEVANT));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));
        assertThat(Relevance.of(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE),
                is(Relevance.MANDATORY));

        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, null, 3));
        assertThat(Relevance.of(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(Relevance.OPTIONAL));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>());
        assertThat(Relevance.of(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(Relevance.OPTIONAL));

        modelObject.setAllowedValuesForIntegerAttribute(new UnrestrictedValueSet<>(false));
        assertThat(Relevance.of(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE),
                is(Relevance.MANDATORY));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10));
        assertThat(Relevance.of(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE),
                is(Relevance.MANDATORY));

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(0, 10, 2, true));
        assertThat(Relevance.of(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE), is(Relevance.OPTIONAL));

        modelObject.setAllowedValuesForIntegerAttribute(new IntegerRange());
        assertThat(Relevance.of(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE),
                is(Relevance.IRRELEVANT));
    }

    @Test
    public void testOfIModelObjectPolicyAttribute() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);

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

    @Test
    public void testAsValueSetFor_Irrelevant() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        assertThat(Relevance.IRRELEVANT.asValueSetFor(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE),
                is(new OrderedValueSet<>(false, null)));
        assertThat(Relevance.IRRELEVANT.asValueSetFor(modelObject, TestPolicyWithVisitor.PROPERTY_ENUM_ATTRIBUTE),
                is(new OrderedValueSet<>(false, null)));
        assertThat(Relevance.IRRELEVANT.asValueSetFor(modelObject, TestPolicyWithVisitor.PROPERTY_RANGE_ATTRIBUTE),
                is(emptyRange()));
        assertThat(Relevance.IRRELEVANT.asValueSetFor(modelObject, TestPolicyWithVisitor.PROPERTY_MONEY_ATTRIBUTE),
                // the null-value is not compared when containsNull is false, so this matches even
                // though it actually is an OrderedValueSet<>(false, null);
                is(new OrderedValueSet<>(false, Money.NULL)));
    }

    private Matcher<ValueSet<?>> emptyRange() {
        return new TypeSafeMatcher<ValueSet<?>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("an empty Range");
            }

            @Override
            protected boolean matchesSafely(ValueSet<?> valueSet) {
                return valueSet.isRange() && valueSet.isEmpty();
            }
        };
    }

    @Test
    public void testAsValueSetFor_Mandatory() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        PolicyAttribute policyAttribute_Unrestricted = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        PolicyAttribute policyAttribute_Boolean = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_BOOLEAN_ATTRIBUTE);
        PolicyAttribute policyAttribute_Enum = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_ENUM_ATTRIBUTE);
        PolicyAttribute policyAttribute_Range = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_RANGE_ATTRIBUTE);

        assertThat(Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Unrestricted),
                is(new UnrestrictedValueSet<Integer>(false)));

        assertThat(Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Boolean),
                is(new OrderedValueSet<>(false, null, Boolean.TRUE, Boolean.FALSE)));

        assertThat(Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Enum),
                is(new OrderedValueSet<>(false, null, TestEnum.values())));

        assertThat(Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Range),
                is(LongRange.valueOf(2L, 5L, 1L, false)));

    }

    @Test
    public void testAsValueSetFor_Optional() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        PolicyAttribute policyAttribute_Unrestricted = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        PolicyAttribute policyAttribute_Boolean = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_BOOLEAN_ATTRIBUTE);
        PolicyAttribute policyAttribute_Enum = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_ENUM_ATTRIBUTE);
        PolicyAttribute policyAttribute_Range = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_RANGE_ATTRIBUTE);

        assertThat(Relevance.OPTIONAL.asValueSetFor(modelObject, policyAttribute_Unrestricted),
                is(new UnrestrictedValueSet<Integer>(true)));
        assertThat(Relevance.OPTIONAL.asValueSetFor(modelObject, TestPolicyWithVisitor.PROPERTY_MONEY_ATTRIBUTE),
                is(new UnrestrictedValueSet<Money>(true)));

        assertThat(Relevance.OPTIONAL.asValueSetFor(modelObject, policyAttribute_Boolean),
                is(new OrderedValueSet<>(true, null, Boolean.TRUE, Boolean.FALSE)));

        assertThat(Relevance.OPTIONAL.asValueSetFor(modelObject, policyAttribute_Enum),
                is(new OrderedValueSet<>(true, null, TestEnum.values())));

        assertThat(Relevance.OPTIONAL.asValueSetFor(modelObject, policyAttribute_Range),
                is(LongRange.valueOf(2L, 5L, 1L, true)));

    }
}
