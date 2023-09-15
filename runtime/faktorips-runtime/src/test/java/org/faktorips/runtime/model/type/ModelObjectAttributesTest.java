/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import java.util.List;
import java.util.stream.Collectors;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.data.TestDeckungWithVisitor;
import org.faktorips.runtime.data.TestPolicyWithVisitor;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.faktorips.valueset.OrderedValueSet;
import org.junit.Test;

public class ModelObjectAttributesTest {

    @Test
    public void testResetIrrelevantAttributes_Irrelevant_Nullable_Empty() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(null);
        modelObject.setIntegerAttribute(null);

        assertThat(ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(), is(true));
    }

    @Test
    public void testResetIrrelevantAttributes_Irrelevant_Nullable_WithValue() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(null);
        modelObject.setIntegerAttribute(1);

        assertAttributeIsReset(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject),
                List.of(modelObject),
                List.of(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(modelObject.getIntegerAttribute(), is(nullValue()));
    }

    @Test
    public void testResetIrrelevantAttributes_Manditory_Nullable() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setIntegerAttribute(1);
        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, 3));

        assertThat(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(),
                is(true));
    }

    @Test
    public void testResetIrrelevantAttributes_Optional_Nullable() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setIntegerAttribute(1);
        modelObject.setAllowedValuesForIntegerAttribute(OrderedValueSet.of(1, 2, null));

        assertThat(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(),
                is(true));
    }

    @Test
    public void testResetIrrelevantAttributes_Irrelevant_String_Empty() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForStringAttribute(null);
        modelObject.setStringAttribute("");

        assertThat(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(),
                is(true));
    }

    @Test
    public void testResetIrrelevantAttributes_Irrelevant_String_Blank() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForStringAttribute(null);
        modelObject.setStringAttribute("   ");

        assertThat(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(),
                is(true));
    }

    @Test
    public void testResetIrrelevantAttributes_Irrelevant_String_WithValue() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForStringAttribute(null);
        modelObject.setStringAttribute("A String");

        assertAttributeIsReset(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject),
                List.of(modelObject),
                List.of(TestPolicyWithVisitor.PROPERTY_STRING_ATTRIBUTE));
        assertThat(modelObject.getStringAttribute(), is(IpsStringUtils.EMPTY));
    }

    @Test
    public void testResetIrrelevantAttributes_Manditory_String() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setStringAttribute("A String");
        modelObject.setAllowedValuesForStringAttribute(OrderedValueSet.of("1", "2"));

        assertThat(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(),
                is(true));
    }

    @Test
    public void testResetIrrelevantAttributes_Optional_String() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setStringAttribute("A String");
        modelObject.setAllowedValuesForStringAttribute(OrderedValueSet.of("1", "2", null));

        assertThat(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(),
                is(true));
    }

    @Test
    public void testResetIrrelevantAttributes_Irrelevant_Money_Empty() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForMoneyAttribute(null);
        modelObject.setMoneyAttribute(Money.NULL);

        assertThat(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(),
                is(true));
    }

    @Test
    public void testResetIrrelevantAttributes_Irrelevant_Money_WithValue() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForMoneyAttribute(null);
        modelObject.setMoneyAttribute(Money.euro(1));

        assertAttributeIsReset(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject),
                List.of(modelObject),
                List.of(TestPolicyWithVisitor.PROPERTY_MONEY_ATTRIBUTE));
        assertThat(modelObject.getMoneyAttribute(), is(Money.NULL));
    }

    @Test
    public void testResetIrrelevantAttributes_Manditory_Money() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setMoneyAttribute(Money.euro(1));
        modelObject.setAllowedValuesForMoneyAttribute(OrderedValueSet.of(Money.euro(1), Money.euro(2)));

        assertThat(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(),
                is(true));
    }

    @Test
    public void testResetIrrelevantAttributes_Optional_Money() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setMoneyAttribute(Money.euro(1));
        modelObject.setAllowedValuesForMoneyAttribute(OrderedValueSet.of(Money.euro(1), Money.euro(2), Money.NULL));

        assertThat(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(),
                is(true));
    }

    @Test
    public void testResetIrrelevantAttributes_Irrelevant_Decimal_Empty() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForDecimalAttribute(null);
        modelObject.setDecimalAttribute(Decimal.NULL);

        assertThat(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(),
                is(true));
    }

    @Test
    public void testResetIrrelevantAttributes_Irrelevant_Decimal_WithValue() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForDecimalAttribute(null);
        modelObject.setDecimalAttribute(Decimal.valueOf(1.0));

        assertAttributeIsReset(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject),
                List.of(modelObject),
                List.of(TestPolicyWithVisitor.PROPERTY_DECIMAL_ATTRIBUTE));
        assertThat(modelObject.getDecimalAttribute(), is(Decimal.NULL));
    }

    @Test
    public void testResetIrrelevantAttributes_Manditory_Decimal() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setDecimalAttribute(Decimal.valueOf(1.0));
        modelObject.setAllowedValuesForDecimalAttribute(OrderedValueSet.of(Decimal.valueOf(1.0), Decimal.valueOf(1.5)));

        assertThat(ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(), is(true));
    }

    @Test
    public void testResetIrrelevantAttributes_Optional_Decimal() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setDecimalAttribute(Decimal.valueOf(1.0));
        modelObject.setAllowedValuesForDecimalAttribute(
                OrderedValueSet.of(Decimal.valueOf(1.0), Decimal.valueOf(1.5), Decimal.NULL));

        assertThat(
                ModelObjectAttributes.resetIrrelevantAttributes(modelObject).isEmpty(),
                is(true));
    }

    @Test
    public void testResetAttributesWithPredicate() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setDecimalAttribute(Decimal.valueOf(1.0));
        modelObject.setIntegerAttribute(1);
        modelObject.setStringAttribute("some String");
        modelObject.setMoneyAttribute(Money.NULL);

        assertAttributeIsReset(ModelObjectAttributes.resetAttributes(modelObject,
                ModelObjectAttribute::isValuePresent),
                List.of(modelObject),
                List.of(TestPolicyWithVisitor.PROPERTY_DECIMAL_ATTRIBUTE,
                        TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE,
                        TestPolicyWithVisitor.PROPERTY_STRING_ATTRIBUTE));
        assertThat(modelObject.getDecimalAttribute(), is(Decimal.NULL));
        assertThat(modelObject.getIntegerAttribute(), is(nullValue()));
        assertThat(modelObject.getStringAttribute(), is(""));
    }

    @Test
    public void testResetAttributesWithPredicateCombined() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setIntegerAttribute(1);
        modelObject.setAllowedValuesForIntegerAttribute(null);
        modelObject.setStringAttribute("some String");
        modelObject.setAllowedValuesForStringAttribute(null);
        modelObject.setDecimalAttribute(Decimal.valueOf(1.0));
        modelObject.setAllowedValuesForDecimalAttribute(OrderedValueSet.of(Decimal.valueOf(1.0), null));
        modelObject.setMoneyAttribute(Money.euro(1));
        modelObject.setAllowedValuesForMoneyAttribute(OrderedValueSet.of(Money.euro(1)));

        assertAttributeIsReset(ModelObjectAttributes.resetAttributes(modelObject,
                ModelObjectAttributes.IS_IRRELEVANT_BUT_NOT_EMPTY.or(ModelObjectAttribute::isMandatory)),
                List.of(modelObject),
                List.of(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE,
                        TestPolicyWithVisitor.PROPERTY_STRING_ATTRIBUTE,
                        TestPolicyWithVisitor.PROPERTY_MONEY_ATTRIBUTE));
    }

    @Test
    public void testResetIrrelevantAttributesWithVisitor() {
        TestPolicyWithVisitor policy = new TestPolicyWithVisitor();
        TestDeckungWithVisitor deckung = new TestDeckungWithVisitor();
        policy.addTestDeckung(deckung);
        policy.setAllowedValuesForDecimalAttribute(null);
        policy.setDecimalAttribute(Decimal.valueOf(1.0));
        deckung.setAllowedValuesForMoneyAttribute(null);
        deckung.setMoneyAttribute(Money.euro(1));

        List<ModelObjectAttribute> moas = ModelObjectAttributes.resetIrrelevantAttributes(policy);

        assertAttributeIsReset(moas,
                List.of(policy, deckung),
                List.of(TestPolicyWithVisitor.PROPERTY_DECIMAL_ATTRIBUTE,
                        TestDeckungWithVisitor.PROPERTY_MONEY_ATTRIBUTE));
    }

    @Test
    public void testResetIrrelevantAttributesOnTheFly() {
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForOnTheFly(new OrderedValueSet<>(false, null));

        ModelObjectAttributes.resetIrrelevantAttributes(modelObject);
        // did not throw an exception
    }

    private void assertAttributeIsReset(List<ModelObjectAttribute> moa,
            List<IModelObject> modelObject,
            List<String> attributes) {
        assertThat(moa.size(), is(attributes.size()));
        assertThat(modelObject.containsAll(moa.stream()
                .map(ModelObjectAttribute::getModelObject)
                .collect(Collectors.toList())),
                is(true));
        assertThat(attributes.containsAll(moa.stream()
                .map(m -> m.getPolicyAttribute().getName())
                .collect(Collectors.toList())),
                is(true));
    }
}
