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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.data.TestEnum;
import org.faktorips.runtime.data.TestPolicyWithVisitor;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAllowedValuesSetter;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.faktorips.sample.model.TestConcreteExtensibleEnum;
import org.faktorips.sample.model.TestConcreteJavaEnum;
import org.faktorips.values.Money;
import org.faktorips.valueset.IntegerRange;
import org.faktorips.valueset.LongRange;
import org.faktorips.valueset.MoneyRange;
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

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.empty());
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

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.empty());
        assertThat(Relevance.isIrrelevant(modelObject, policyAttribute), is(true));
    }

    @Test
    public void testIsIrrelevantValueSet() {
        assertThat(Relevance.isIrrelevant(null), is(true));
        assertThat(Relevance.isIrrelevant(OrderedValueSet.empty()), is(true));
        assertThat(Relevance.isIrrelevant(OrderedValueSet.of(1, 2, 3)), is(false));
        assertThat(Relevance.isIrrelevant(OrderedValueSet.of(1, null, 3)), is(false));
        assertThat(Relevance.isIrrelevant(new UnrestrictedValueSet<>()), is(false));
        assertThat(Relevance.isIrrelevant(IntegerRange.valueOf(0, 10)), is(false));
        assertThat(Relevance.isIrrelevant(IntegerRange.empty()), is(true));
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

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.empty());
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

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.empty());
        assertThat(Relevance.isMandatory(modelObject, policyAttribute), is(false));
    }

    @Test
    public void testIsMandatoryValueSet() {
        assertThat(Relevance.isMandatory(null), is(false));
        assertThat(Relevance.isMandatory(OrderedValueSet.empty()), is(false));
        assertThat(Relevance.isMandatory(OrderedValueSet.of(1, 2, 3)), is(true));
        assertThat(Relevance.isMandatory(OrderedValueSet.of(1, null, 3)), is(false));
        assertThat(Relevance.isMandatory(new UnrestrictedValueSet<>()), is(false));
        assertThat(Relevance.isMandatory(new UnrestrictedValueSet<>(false)), is(true));
        assertThat(Relevance.isMandatory(IntegerRange.valueOf(0, 10)), is(true));
        assertThat(Relevance.isMandatory(IntegerRange.valueOf(0, 10, 2, true)), is(false));
        assertThat(Relevance.isMandatory(IntegerRange.empty()), is(false));
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

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.empty());
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

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.empty());
        assertThat(Relevance.isOptional(modelObject, policyAttribute), is(false));
    }

    @Test
    public void testIsOptionalValueSet() {
        assertThat(Relevance.isOptional(null), is(false));
        assertThat(Relevance.isOptional(OrderedValueSet.empty()), is(false));
        assertThat(Relevance.isOptional(OrderedValueSet.of(1, 2, 3)), is(false));
        assertThat(Relevance.isOptional(OrderedValueSet.of(1, null, 3)), is(true));
        assertThat(Relevance.isOptional(new UnrestrictedValueSet<>()), is(true));
        assertThat(Relevance.isOptional(new UnrestrictedValueSet<>(false)), is(false));
        assertThat(Relevance.isOptional(IntegerRange.valueOf(0, 10)), is(false));
        assertThat(Relevance.isOptional(IntegerRange.valueOf(0, 10, 2, true)), is(true));
        assertThat(Relevance.isOptional(IntegerRange.empty()), is(false));
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

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.empty());
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

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.empty());
        assertThat(Relevance.isRelevant(modelObject, policyAttribute), is(false));
    }

    @Test
    public void testIsRelevantValueSet() {
        assertThat(Relevance.isRelevant(null), is(false));
        assertThat(Relevance.isRelevant(OrderedValueSet.empty()), is(false));
        assertThat(Relevance.isRelevant(OrderedValueSet.of(1, 2, 3)), is(true));
        assertThat(Relevance.isRelevant(OrderedValueSet.of(1, null, 3)), is(true));
        assertThat(Relevance.isRelevant(new UnrestrictedValueSet<>()), is(true));
        assertThat(Relevance.isRelevant(IntegerRange.valueOf(0, 10)), is(true));
        assertThat(Relevance.isRelevant(IntegerRange.empty()), is(false));
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

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.empty());
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

        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.empty());
        assertThat(Relevance.of(modelObject, policyAttribute), is(Relevance.IRRELEVANT));
    }

    @Test
    public void testOfValueSet() {
        assertThat(Relevance.of(null), is(Relevance.IRRELEVANT));
        assertThat(Relevance.of(OrderedValueSet.empty()), is(Relevance.IRRELEVANT));
        assertThat(Relevance.of(OrderedValueSet.of(1, 2, 3)), is(Relevance.MANDATORY));
        assertThat(Relevance.of(OrderedValueSet.of(1, null, 3)), is(Relevance.OPTIONAL));
        assertThat(Relevance.of(new UnrestrictedValueSet<>()), is(Relevance.OPTIONAL));
        assertThat(Relevance.of(new UnrestrictedValueSet<>(false)), is(Relevance.MANDATORY));
        assertThat(Relevance.of(IntegerRange.valueOf(0, 10)), is(Relevance.MANDATORY));
        assertThat(Relevance.of(IntegerRange.valueOf(0, 10, 2, true)), is(Relevance.OPTIONAL));
        assertThat(Relevance.of(IntegerRange.empty()), is(Relevance.IRRELEVANT));
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

    @Test
    public void testAsValueSetFor_Irrelevant_ValueSetTypeRange() {
        assertThat(
                Relevance.IRRELEVANT.asValueSetFor(new TestPolicyWithIntegerRange(),
                        TestPolicyWithIntegerRange.PROPERTY_INTEGER_ATTRIBUTE_WITH_RANGE),
                is(IntegerRange.empty()));
        assertThat(
                Relevance.IRRELEVANT.asValueSetFor(new TestPolicyWithMoneyRange(),
                        TestPolicyWithMoneyRange.PROPERTY_MONEY_ATTRIBUTE_WITH_RANGE),
                is(MoneyRange.empty()));
    }

    private Matcher<ValueSet<?>> emptyRange() {
        return new TypeSafeMatcher<>() {

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
                is(new UnrestrictedValueSet<>(false)));

        assertThat(Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Boolean),
                is(new OrderedValueSet<>(false, null, Boolean.TRUE, Boolean.FALSE)));

        assertThat(Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Enum),
                is(new OrderedValueSet<>(false, null, TestEnum.values())));

        assertThat(Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Range),
                is(LongRange.valueOf((Long)null, null, null, false)));

    }

    @Test
    public void testAsValueSetFor_Mandatory_Derived() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithDerivedMandatoryValueSet();

        assertThat(Relevance.MANDATORY.asValueSetFor(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE),
                is(new UnrestrictedValueSet<>(false)));
    }

    @Test
    public void testAsValueSetFor_Mandatory_WithValueSet() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        PolicyAttribute policyAttribute_Unrestricted = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        PolicyAttribute policyAttribute_Boolean = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_BOOLEAN_ATTRIBUTE);
        PolicyAttribute policyAttribute_Enum = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_ENUM_ATTRIBUTE);
        PolicyAttribute policyAttribute_Range = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_RANGE_ATTRIBUTE);

        assertThat(
                Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Unrestricted,
                        new OrderedValueSet<>(true, null, 1, 2, 3)),
                is(new OrderedValueSet<>(false, null, 1, 2, 3)));

        assertThat(
                Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Boolean,
                        new OrderedValueSet<>(true, null, Boolean.TRUE)),
                is(new OrderedValueSet<>(false, null, Boolean.TRUE)));

        assertThat(
                Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Enum,
                        new OrderedValueSet<>(true, null, TestEnum.TEST_A, TestEnum.TEST_B)),
                is(new OrderedValueSet<>(false, null, TestEnum.TEST_A, TestEnum.TEST_B)));

        assertThat(
                Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Range,
                        LongRange.valueOf(2L, 5L, 1L, true)),
                is(LongRange.valueOf(2L, 5L, 1L, false)));

    }

    @Test
    public void testAsValueSetFor_Mandatory_WithValues() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        PolicyAttribute policyAttribute_Unrestricted = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        PolicyAttribute policyAttribute_Boolean = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_BOOLEAN_ATTRIBUTE);
        PolicyAttribute policyAttribute_Enum = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_ENUM_ATTRIBUTE);

        assertThat(
                Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Unrestricted,
                        Arrays.asList(1, null, 2, 3)),
                is(new OrderedValueSet<>(false, null, 1, 2, 3)));

        assertThat(
                Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Boolean,
                        Arrays.asList(null, Boolean.TRUE)),
                is(new OrderedValueSet<>(false, null, Boolean.TRUE)));

        assertThat(
                Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Enum,
                        Arrays.asList(null, TestEnum.TEST_A, TestEnum.TEST_B)),
                is(new OrderedValueSet<>(false, null, TestEnum.TEST_A, TestEnum.TEST_B)));
    }

    @Test
    public void testAsValueSetFor_Mandatory_PrimitiveBoolean() {
        TestPolicyWithPrimitiveBoolean modelObject = new TestPolicyWithPrimitiveBoolean();
        PolicyAttribute policyAttribute_Enum = IpsModel.getPolicyCmptType(TestPolicyWithPrimitiveBoolean.class)
                .getAttribute(TestPolicyWithPrimitiveBoolean.PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM);

        assertThat(Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Enum),
                is(new OrderedValueSet<>(false, null, true, false)));

        assertThat(
                Relevance.MANDATORY.asValueSetFor(modelObject, policyAttribute_Enum,
                        new OrderedValueSet<>(false, null, false)),
                is(new OrderedValueSet<>(false, null, false)));
    }

    @Test
    public void testAsValueSetFor_Mandatory_ValueSetTypeEnum() {
        assertThat(
                Relevance.MANDATORY.asValueSetFor(new TestPolicyWithIntegerEnum(),
                        TestPolicyWithIntegerEnum.PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM),
                is(new OrderedValueSet<>(false, null)));
        assertThat(
                Relevance.MANDATORY.asValueSetFor(new TestPolicyWithMoneyEnum(),
                        TestPolicyWithMoneyEnum.PROPERTY_MONEY_ATTRIBUTE_WITH_ENUM),
                is(new OrderedValueSet<>(false, Money.NULL)));
    }

    @Test
    public void testAsValueSetFor_Mandatory_DatatypeEnum() {
        assertThat(
                Relevance.MANDATORY.asValueSetFor(new TestPolicyWithUnrestrictedEnum(),
                        TestPolicyWithUnrestrictedEnum.PROPERTY_NON_IPS_ENUM_ATTRIBUTE),
                is(new OrderedValueSet<>(false, null, TestEnum.values())));
        assertThat(
                Relevance.MANDATORY.asValueSetFor(new TestPolicyWithUnrestrictedEnum(),
                        TestPolicyWithUnrestrictedEnum.PROPERTY_IPS_ENUM_ATTRIBUTE),
                is(new OrderedValueSet<>(false, null, TestConcreteJavaEnum.values())));
    }

    @Test
    public void testAsValueSetFor_Mandatory_ValueSetTypeRange() {
        assertThat(
                Relevance.MANDATORY.asValueSetFor(new TestPolicyWithIntegerRange(),
                        TestPolicyWithIntegerRange.PROPERTY_INTEGER_ATTRIBUTE_WITH_RANGE),
                is(IntegerRange.valueOf((Integer)null, null, null, false)));
        assertThat(
                Relevance.MANDATORY.asValueSetFor(new TestPolicyWithMoneyRange(),
                        TestPolicyWithMoneyRange.PROPERTY_MONEY_ATTRIBUTE_WITH_RANGE),
                is(MoneyRange.valueOf((Money)null, null, null, false)));
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
                is(new UnrestrictedValueSet<>(true)));
        assertThat(Relevance.OPTIONAL.asValueSetFor(modelObject, TestPolicyWithVisitor.PROPERTY_MONEY_ATTRIBUTE),
                is(new UnrestrictedValueSet<>(true)));

        assertThat(Relevance.OPTIONAL.asValueSetFor(modelObject, policyAttribute_Boolean),
                is(new OrderedValueSet<>(true, null, Boolean.TRUE, Boolean.FALSE)));

        assertThat(Relevance.OPTIONAL.asValueSetFor(modelObject, policyAttribute_Enum),
                is(new OrderedValueSet<>(true, null, TestEnum.values())));

        assertThat(Relevance.OPTIONAL.asValueSetFor(modelObject, policyAttribute_Range),
                is(LongRange.valueOf((Long)null, null, null, true)));

    }

    @Test
    public void testAsValueSetFor_Optional_Derived() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithDerivedOptionalValueSet();

        assertThat(Relevance.OPTIONAL.asValueSetFor(modelObject, TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE),
                is(new UnrestrictedValueSet<>(true)));
    }

    @Test
    public void testAsValueSetFor_Optional_PrimitiveBoolean() {
        TestPolicyWithPrimitiveBoolean modelObject = new TestPolicyWithPrimitiveBoolean();
        PolicyAttribute policyAttribute_Enum = IpsModel.getPolicyCmptType(TestPolicyWithPrimitiveBoolean.class)
                .getAttribute(TestPolicyWithPrimitiveBoolean.PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM);

        assertThat(Relevance.OPTIONAL.asValueSetFor(modelObject, policyAttribute_Enum),
                is(new OrderedValueSet<>(true, null, true, false)));

        assertThat(
                Relevance.OPTIONAL.asValueSetFor(modelObject, policyAttribute_Enum,
                        new OrderedValueSet<>(false, null, false)),
                is(new OrderedValueSet<>(true, null, false)));
    }

    @Test
    public void testAsValueSetFor_Optional_ValueSetTypeEnum() {
        assertThat(
                Relevance.OPTIONAL.asValueSetFor(new TestPolicyWithIntegerEnum(),
                        TestPolicyWithIntegerEnum.PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM),
                is(new OrderedValueSet<>(true, null)));
        assertThat(
                Relevance.OPTIONAL.asValueSetFor(new TestPolicyWithMoneyEnum(),
                        TestPolicyWithMoneyEnum.PROPERTY_MONEY_ATTRIBUTE_WITH_ENUM),
                is(new OrderedValueSet<>(true, Money.NULL)));
    }

    @Test
    public void testAsValueSetFor_Optional_ValueSetTypeRange() {
        assertThat(
                Relevance.OPTIONAL.asValueSetFor(new TestPolicyWithIntegerRange(),
                        TestPolicyWithIntegerRange.PROPERTY_INTEGER_ATTRIBUTE_WITH_RANGE),
                is(IntegerRange.valueOf((Integer)null, null, null, true)));
        assertThat(
                Relevance.OPTIONAL.asValueSetFor(new TestPolicyWithMoneyRange(),
                        TestPolicyWithMoneyRange.PROPERTY_MONEY_ATTRIBUTE_WITH_RANGE),
                is(MoneyRange.valueOf((Money)null, null, null, true)));
    }

    @Test
    public void testAsValueSetFor_Mandatory_ExtensibleEnum() {
        assertThat(
                Relevance.MANDATORY.asValueSetFor(new TestPolicyWithUnrestrictedEnum(),
                        TestPolicyWithUnrestrictedEnum.PROPERTY_IPS_EXTENSIBLE_ENUM_ATTRIBUTE),
                is(new UnrestrictedValueSet<>(false)));

        assertThat(Relevance.MANDATORY.asValueSetFor(new TestPolicyWithUnrestrictedEnum(),
                TestPolicyWithUnrestrictedEnum.PROPERTY_IPS_EXTENSIBLE_ENUM_ATTRIBUTE,
                new ArrayList<>()),
                is(new OrderedValueSet<>(new ArrayList<>(), false, null)));

        assertThat(Relevance.MANDATORY.asValueSetFor(new TestPolicyWithUnrestrictedEnum(),
                TestPolicyWithUnrestrictedEnum.PROPERTY_IPS_EXTENSIBLE_ENUM_ATTRIBUTE,
                TestConcreteExtensibleEnum.VALUES),
                is(new OrderedValueSet<>(TestConcreteExtensibleEnum.VALUES, false, null)));
    }

    @IpsPolicyCmptType(name = "TestPolicyWithDerivedMandatoryValueSet")
    @IpsAttributes({ "IntegerAttribute" })
    public static class TestPolicyWithDerivedMandatoryValueSet extends TestPolicyWithVisitor {

        @IpsAllowedValues("IntegerAttribute")
        @Override
        public ValueSet<Integer> getSetOfAllowedValuesForIntegerAttribute() {
            return Relevance.MANDATORY.asValueSetFor(this, PROPERTY_INTEGER_ATTRIBUTE);
        }
    }

    @IpsPolicyCmptType(name = "TestPolicyWithDerivedOptionalValueSet")
    @IpsAttributes({ "IntegerAttribute" })
    public static class TestPolicyWithDerivedOptionalValueSet extends TestPolicyWithVisitor {

        @IpsAllowedValues("IntegerAttribute")
        @Override
        public ValueSet<Integer> getSetOfAllowedValuesForIntegerAttribute() {
            return Relevance.OPTIONAL.asValueSetFor(this, PROPERTY_INTEGER_ATTRIBUTE);
        }
    }

    @IpsPolicyCmptType(name = "TestPolicyWithIntegerEnum")
    @IpsAttributes({ "IntegerAttributeWithEnum" })
    @IpsAssociations({})
    static class TestPolicyWithIntegerEnum implements IModelObject {

        public static final String PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM = "IntegerAttributeWithEnum";

        private Integer integerAttributeWithEnum;

        private ValueSet<Integer> setOfAllowedValuesIntegerAttributeWithEnum = new OrderedValueSet<>(true, null, 1,
                2, 3);

        @IpsAllowedValues(PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM)
        public ValueSet<Integer> getSetOfAllowedValuesForIntegerAttributeWithEnum() {
            return setOfAllowedValuesIntegerAttributeWithEnum;
        }

        @IpsAllowedValuesSetter(PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM)
        public void setAllowedValuesForIntegerAttributeWithEnum(
                ValueSet<Integer> setOfAllowedValuesIntegerAttributeWithEnum) {
            this.setOfAllowedValuesIntegerAttributeWithEnum = setOfAllowedValuesIntegerAttributeWithEnum;
        }

        @IpsAttribute(name = PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM, kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        public Integer getIntegerAttributeWithEnum() {
            return integerAttributeWithEnum;
        }

        @IpsAttributeSetter(PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM)
        public void setIntegerAttributeWithEnum(Integer newValue) {
            integerAttributeWithEnum = newValue;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return new MessageList();
        }
    }

    @IpsPolicyCmptType(name = "TestPolicyWithIntegerRange")
    @IpsAttributes({ "IntegerAttributeWithRange" })
    @IpsAssociations({})
    static class TestPolicyWithIntegerRange implements IModelObject {

        public static final String PROPERTY_INTEGER_ATTRIBUTE_WITH_RANGE = "IntegerAttributeWithRange";

        private Integer integerAttributeWithRange;

        private ValueSet<Integer> setOfAllowedValuesIntegerAttributeWithRange = IntegerRange.valueOf(0, 10);

        @IpsAllowedValues(PROPERTY_INTEGER_ATTRIBUTE_WITH_RANGE)
        public ValueSet<Integer> getSetOfAllowedValuesForIntegerAttributeWithRange() {
            return setOfAllowedValuesIntegerAttributeWithRange;
        }

        @IpsAllowedValuesSetter(PROPERTY_INTEGER_ATTRIBUTE_WITH_RANGE)
        public void setAllowedValuesForIntegerAttributeWithRange(
                ValueSet<Integer> setOfAllowedValuesIntegerAttributeWithRange) {
            this.setOfAllowedValuesIntegerAttributeWithRange = setOfAllowedValuesIntegerAttributeWithRange;
        }

        @IpsAttribute(name = PROPERTY_INTEGER_ATTRIBUTE_WITH_RANGE, kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
        public Integer getIntegerAttributeWithRange() {
            return integerAttributeWithRange;
        }

        @IpsAttributeSetter(PROPERTY_INTEGER_ATTRIBUTE_WITH_RANGE)
        public void setIntegerAttributeWithRange(Integer newValue) {
            integerAttributeWithRange = newValue;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return new MessageList();
        }
    }

    @IpsPolicyCmptType(name = "TestPolicyWithMoneyEnum")
    @IpsAttributes({ "MoneyAttributeWithEnum" })
    @IpsAssociations({})
    static class TestPolicyWithMoneyEnum implements IModelObject {

        public static final String PROPERTY_MONEY_ATTRIBUTE_WITH_ENUM = "MoneyAttributeWithEnum";

        private Money moneyAttributeWithEnum;

        private ValueSet<Money> setOfAllowedValuesMoneyAttributeWithEnum = new OrderedValueSet<>(true, null,
                Money.euro(5), Money.euro(10));

        @IpsAllowedValues(PROPERTY_MONEY_ATTRIBUTE_WITH_ENUM)
        public ValueSet<Money> getSetOfAllowedValuesForMoneyAttributeWithEnum() {
            return setOfAllowedValuesMoneyAttributeWithEnum;
        }

        @IpsAllowedValuesSetter(PROPERTY_MONEY_ATTRIBUTE_WITH_ENUM)
        public void setAllowedValuesForMoneyAttributeWithEnum(
                ValueSet<Money> setOfAllowedValuesMoneyAttributeWithEnum) {
            this.setOfAllowedValuesMoneyAttributeWithEnum = setOfAllowedValuesMoneyAttributeWithEnum;
        }

        @IpsAttribute(name = PROPERTY_MONEY_ATTRIBUTE_WITH_ENUM, kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        public Money getMoneyAttributeWithEnum() {
            return moneyAttributeWithEnum;
        }

        @IpsAttributeSetter(PROPERTY_MONEY_ATTRIBUTE_WITH_ENUM)
        public void setMoneyAttributeWithEnum(Money newValue) {
            moneyAttributeWithEnum = newValue;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return new MessageList();
        }
    }

    @IpsPolicyCmptType(name = "TestPolicyWithMoneyRange")
    @IpsAttributes({ "MoneyAttributeWithRange" })
    @IpsAssociations({})
    static class TestPolicyWithMoneyRange implements IModelObject {

        public static final String PROPERTY_MONEY_ATTRIBUTE_WITH_RANGE = "MoneyAttributeWithRange";

        private Money moneyAttributeWithRange;

        private ValueSet<Money> setOfAllowedValuesMoneyAttributeWithRange = MoneyRange.valueOf(Money.euro(0),
                Money.euro(10), Money.euro(1));

        @IpsAllowedValues(PROPERTY_MONEY_ATTRIBUTE_WITH_RANGE)
        public ValueSet<Money> getSetOfAllowedValuesForMoneyAttributeWithRange() {
            return setOfAllowedValuesMoneyAttributeWithRange;
        }

        @IpsAllowedValuesSetter(PROPERTY_MONEY_ATTRIBUTE_WITH_RANGE)
        public void setAllowedValuesForMoneyAttributeWithRange(
                ValueSet<Money> setOfAllowedValuesMoneyAttributeWithRange) {
            this.setOfAllowedValuesMoneyAttributeWithRange = setOfAllowedValuesMoneyAttributeWithRange;
        }

        @IpsAttribute(name = PROPERTY_MONEY_ATTRIBUTE_WITH_RANGE, kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
        public Money getMoneyAttributeWithRange() {
            return moneyAttributeWithRange;
        }

        @IpsAttributeSetter(PROPERTY_MONEY_ATTRIBUTE_WITH_RANGE)
        public void setMoneyAttributeWithRange(Money newValue) {
            moneyAttributeWithRange = newValue;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return new MessageList();
        }
    }

    @IpsPolicyCmptType(name = "TestPolicyWithPrimitiveBoolean")
    @IpsAttributes({ "booleanAttribute" })
    static class TestPolicyWithPrimitiveBoolean implements IModelObject {

        public static final String PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM = "booleanAttribute";

        private boolean integerAttribute;

        private ValueSet<Boolean> setOfAllowedValuesBooleanAttribute = new OrderedValueSet<>(true, null, true);

        @IpsAllowedValues(PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM)
        public ValueSet<Boolean> getSetOfAllowedValuesForBooleanAttribute() {
            return setOfAllowedValuesBooleanAttribute;
        }

        @IpsAllowedValuesSetter(PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM)
        public void setAllowedValuesForBooleanAttribute(
                ValueSet<Boolean> setOfAllowedValuesBooleanAttribute) {
            this.setOfAllowedValuesBooleanAttribute = setOfAllowedValuesBooleanAttribute;
        }

        @IpsAttribute(name = PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM, kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public boolean isBooleanAttribute() {
            return integerAttribute;
        }

        @IpsAttributeSetter(PROPERTY_INTEGER_ATTRIBUTE_WITH_ENUM)
        public void setBooleanAttribute(boolean newValue) {
            integerAttribute = newValue;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return new MessageList();
        }
    }

    @IpsPolicyCmptType(name = "TestPolicyWithUnrestrictedEnum")
    @IpsAttributes({ "NonIpsEnumAttribute", "IpsEnumAttribute", "IpsExtensibleEnumAttribute" })
    @IpsAssociations({})
    static class TestPolicyWithUnrestrictedEnum implements IModelObject {

        public static final String PROPERTY_NON_IPS_ENUM_ATTRIBUTE = "NonIpsEnumAttribute";
        public static final String PROPERTY_IPS_ENUM_ATTRIBUTE = "IpsEnumAttribute";
        public static final String PROPERTY_IPS_EXTENSIBLE_ENUM_ATTRIBUTE = "IpsExtensibleEnumAttribute";

        private TestEnum nonIpsEnumAttribute;
        private TestConcreteJavaEnum ipsEnumAttribute;
        private TestConcreteExtensibleEnum ipsExtensibleEnumAttribute;

        private ValueSet<TestEnum> setOfAllowedValuesNonIpsEnumAttribute = new OrderedValueSet<>(true, null,
                TestEnum.values());

        private ValueSet<TestConcreteJavaEnum> setOfAllowedValuesIpsEnumAttribute = new OrderedValueSet<>(true, null,
                TestConcreteJavaEnum.values());

        private ValueSet<TestConcreteExtensibleEnum> setOfAllowedValuesipsExtensibleEnumAttribute = new OrderedValueSet<>(
                TestConcreteExtensibleEnum.VALUES, true, null);

        @IpsAllowedValues(PROPERTY_NON_IPS_ENUM_ATTRIBUTE)
        public ValueSet<TestEnum> getSetOfAllowedValuesForNonIpsEnumAttribute() {
            return setOfAllowedValuesNonIpsEnumAttribute;
        }

        @IpsAllowedValuesSetter(PROPERTY_NON_IPS_ENUM_ATTRIBUTE)
        public void setAllowedValuesForNonIpsEnumAttribute(
                ValueSet<TestEnum> setOfAllowedValuesNonIpsEnumAttribute) {
            this.setOfAllowedValuesNonIpsEnumAttribute = setOfAllowedValuesNonIpsEnumAttribute;
        }

        @IpsAttribute(name = PROPERTY_NON_IPS_ENUM_ATTRIBUTE, kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public TestEnum getNonIpsEnumAttribute() {
            return nonIpsEnumAttribute;
        }

        @IpsAttributeSetter(PROPERTY_NON_IPS_ENUM_ATTRIBUTE)
        public void setNonIpsEnumAttribute(TestEnum newValue) {
            nonIpsEnumAttribute = newValue;
        }

        @IpsAllowedValues(PROPERTY_IPS_ENUM_ATTRIBUTE)
        public ValueSet<TestConcreteJavaEnum> getSetOfAllowedValuesForIpsEnumAttribute() {
            return setOfAllowedValuesIpsEnumAttribute;
        }

        @IpsAllowedValuesSetter(PROPERTY_IPS_ENUM_ATTRIBUTE)
        public void setAllowedValuesForIpsEnumAttribute(
                ValueSet<TestConcreteJavaEnum> setOfAllowedValuesIpsEnumAttribute) {
            this.setOfAllowedValuesIpsEnumAttribute = setOfAllowedValuesIpsEnumAttribute;
        }

        @IpsAttribute(name = PROPERTY_IPS_ENUM_ATTRIBUTE, kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public TestConcreteJavaEnum getIpsEnumAttribute() {
            return ipsEnumAttribute;
        }

        @IpsAttributeSetter(PROPERTY_IPS_ENUM_ATTRIBUTE)
        public void setIpsEnumAttribute(TestConcreteJavaEnum newValue) {
            ipsEnumAttribute = newValue;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return new MessageList();
        }

        @IpsAttribute(name = PROPERTY_IPS_EXTENSIBLE_ENUM_ATTRIBUTE, kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public TestConcreteExtensibleEnum getIpsExtensibleEnumAttribute() {
            return ipsExtensibleEnumAttribute;
        }

        @IpsAttributeSetter(PROPERTY_IPS_EXTENSIBLE_ENUM_ATTRIBUTE)
        public void setIpsExtensibleEnumAttribute(TestConcreteExtensibleEnum ipsExtensibleEnumAttribute) {
            this.ipsExtensibleEnumAttribute = ipsExtensibleEnumAttribute;
        }

        @IpsAllowedValues(PROPERTY_IPS_EXTENSIBLE_ENUM_ATTRIBUTE)
        public ValueSet<TestConcreteExtensibleEnum> getSetOfAllowedValuesipsExtensibleEnumAttribute() {
            return setOfAllowedValuesipsExtensibleEnumAttribute;
        }

        @IpsAllowedValuesSetter(PROPERTY_IPS_EXTENSIBLE_ENUM_ATTRIBUTE)
        public void setSetOfAllowedValuesipsExtensibleEnumAttribute(
                ValueSet<TestConcreteExtensibleEnum> setOfAllowedValuesipsExtensibleEnumAttribute) {
            this.setOfAllowedValuesipsExtensibleEnumAttribute = setOfAllowedValuesipsExtensibleEnumAttribute;
        }
    }
}
