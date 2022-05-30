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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ValidationContext;
import org.faktorips.runtime.data.TestPolicyWithVisitor;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;
import org.junit.Test;

public class GenericRelevanceValidationTest {

    @Test
    public void testOf() {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        ValueSet<Integer> valueSet = new UnrestrictedValueSet<>(false);
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        MessageList messageList = GenericRelevanceValidation.of(modelObject, TestPolicyWithVisitor.class,
                TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE,
                new ValidationContext(Locale.GERMANY, getClass().getClassLoader(), config));
        assertThat(messageList.size(), is(1));

        Message message = messageList.getMessage(0);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.MandatoryValueMissing.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("Das Feld \"Integer-Attribut\" muss einen Wert enthalten."));
    }

    @Test
    public void testOf_LocaleFromGenericAttributeValidationConfigurationIsUsed() {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        ValueSet<Integer> valueSet = new UnrestrictedValueSet<>(false);
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        MessageList messageList = GenericRelevanceValidation.of(modelObject, TestPolicyWithVisitor.class,
                TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE,
                // ValidationContext has different Locale, which is ignored.
                new ValidationContext(Locale.ENGLISH, getClass().getClassLoader(), config));
        assertThat(messageList.size(), is(1));

        Message message = messageList.getMessage(0);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.MandatoryValueMissing.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("Das Feld \"Integer-Attribut\" muss einen Wert enthalten."));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_AttributeNotFound() {
        GenericRelevanceValidation.of(new TestPolicyWithVisitor(), TestPolicyWithVisitor.class, "Foobar",
                new ValidationContext());
    }

    @Test
    public void testValidate_MissingMandatoryValue() {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        ValueSet<Integer> valueSet = new UnrestrictedValueSet<>(false);
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject,
                TestPolicyWithVisitor.class, policyAttribute,
                config);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.size(), is(1));

        Message message = messageList.getMessage(0);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.MandatoryValueMissing.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" must contain a value."));
    }

    @Test
    public void testValidate_ValuePresentForIgnoredAttribute_Null() {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setIntegerAttribute(1);
        ValueSet<Integer> valueSet = null;
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject,
                TestPolicyWithVisitor.class, policyAttribute,
                config);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.size(), is(1));

        Message message = messageList.getMessage(0);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.IrrelevantValuePresent.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" must not contain a value."));
    }

    @Test
    public void testValidate_ValuePresentForIgnoredAttribute_Empty() {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setIntegerAttribute(1);
        ValueSet<Integer> valueSet = OrderedValueSet.empty();
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject,
                TestPolicyWithVisitor.class, policyAttribute,
                config);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.size(), is(1));

        Message message = messageList.getMessage(0);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.IrrelevantValuePresent.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" must not contain a value."));
    }

    @Test
    public void testValidate_ValueNotInAllowedValueSet() {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setIntegerAttribute(1);
        ValueSet<Integer> valueSet = OrderedValueSet.of(2, 3, 4);
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject,
                TestPolicyWithVisitor.class, policyAttribute,
                config);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.size(), is(1));

        Message message = messageList.getMessage(0);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" contains an invalid value."));
    }

    @Test
    public void testValidate_ShouldNotValidate() {
        AtomicBoolean called = new AtomicBoolean(false);
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US) {
            @Override
            public boolean shouldValidate(PolicyAttribute policyAttribute, IModelObject modelObject) {
                called.set(true);
                return false;
            }
        };
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        ValueSet<Integer> valueSet = new UnrestrictedValueSet<>(false);
        modelObject.setAllowedValuesForIntegerAttribute(valueSet);

        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);

        GenericRelevanceValidation relevanceValidation = new GenericRelevanceValidation(modelObject,
                TestPolicyWithVisitor.class, policyAttribute, config);

        MessageList messageList = relevanceValidation.validate();
        assertThat(messageList.isEmpty(), is(true));
        assertThat(called.get(), is(true));
    }
}
