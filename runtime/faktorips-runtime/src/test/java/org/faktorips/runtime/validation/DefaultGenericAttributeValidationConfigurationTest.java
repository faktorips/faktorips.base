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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

import org.faktorips.runtime.IMarker;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.data.TestPolicyWithVisitor;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.valueset.IntegerRange;
import org.junit.Test;

public class DefaultGenericAttributeValidationConfigurationTest {

    @Test
    public void testShouldValidate() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicyWithVisitor();

        assertTrue(config.shouldValidate(policyAttribute, modelObject));
    }

    @Test
    public void testGetLabelFor() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicyWithVisitor();

        assertThat(config.getLabelFor(policyAttribute, modelObject), is("\"Integer-Attribut\""));
    }

    @Test
    public void testCreateMessageForMissingMandatoryValue_UsesGetLabelFor() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY) {
            @Override
            protected String getLabelFor(PolicyAttribute policyAttribute, IModelObject modelObject) {
                return '»' + policyAttribute.getName() + '«';
            }
        };
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicyWithVisitor();

        Message message = config.createMessageForMissingMandatoryValue(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.MandatoryValueMissing.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("Das Feld »IntegerAttribute« muss einen Wert enthalten."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForMissingMandatoryValue_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicyWithVisitor();

        Message message = config.createMessageForMissingMandatoryValue(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.MandatoryValueMissing.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("Das Feld \"Integer-Attribut\" muss einen Wert enthalten."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForMissingMandatoryValue_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicyWithVisitor();

        Message message = config.createMessageForMissingMandatoryValue(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.MandatoryValueMissing.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" must contain a value."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForMissingMandatoryValue_SetsMarker() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY, MandatoryMarker.INSTANCE);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicyWithVisitor();

        Message message = config.createMessageForMissingMandatoryValue(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.hasMarker(MandatoryMarker.INSTANCE), is(true));
        assertThat(config.getMissingMandatoryValueMarker(), is(MandatoryMarker.INSTANCE));
    }

    @Test
    public void testCreateMessageForValuePresentForIrrelevantAttribute_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicyWithVisitor();

        Message message = config.createMessageForValuePresentForIrrelevantAttribute(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.IrrelevantValuePresent.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("Das Feld \"Integer-Attribut\" darf keinen Wert enthalten."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValuePresentForIrrelevantAttribute_UsesGetLabelFor() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY) {
            @Override
            protected String getLabelFor(PolicyAttribute policyAttribute, IModelObject modelObject) {
                return '»' + policyAttribute.getName() + '«';
            }
        };
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicyWithVisitor();

        Message message = config.createMessageForValuePresentForIrrelevantAttribute(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.IrrelevantValuePresent.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("Das Feld »IntegerAttribute« darf keinen Wert enthalten."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValuePresentForIrrelevantAttribute_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicyWithVisitor();

        Message message = config.createMessageForValuePresentForIrrelevantAttribute(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.IrrelevantValuePresent.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" must not contain a value."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSet_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicyWithVisitor();

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSet_UsesGetLabelFor() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY) {
            @Override
            protected String getLabelFor(PolicyAttribute policyAttribute, IModelObject modelObject) {
                return '»' + policyAttribute.getName() + '«';
            }
        };
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicyWithVisitor();

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("Das Feld »IntegerAttribute« enthält einen ungültigen Wert."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSet_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicyWithVisitor();

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" contains an invalid value."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRange_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(10, 20, null, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is(
                "Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert. Der Wert muss zwischen 10 und 20 liegen."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRange_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(10, 20, null, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\" contains an invalid value. The value must be between 10 and 20."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeStep_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(10, 20, 5, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is(
                "Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert. Der Wert muss zwischen 10 und 20 (Schrittweite 5) liegen."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeStep_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(10, 20, 5, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\" contains an invalid value. The value must be between 10 and 20 (step 5)."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeLowerStep_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(3, null, 2, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is(
                "Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert. Der Wert muss mindestens 3 (Schrittweite 2) sein."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeLowerStep_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(3, null, 2, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\" contains an invalid value. The value must be at least 3 (step 2)."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeLower_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(3, null, null, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is(
                "Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert. Der Wert muss mindestens 3 sein."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeLower_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(3, null, null, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\" contains an invalid value. The value must be at least 3."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeUpperStep_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(null, 100, 10, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is(
                "Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert. Der Wert darf höchstens 100 (Schrittweite 10) sein."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeUpperStep_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(null, 100, 10, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\" contains an invalid value. The value must be at most 100 (step 10)."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeUpper_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(null, 42, null, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is(
                "Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert. Der Wert darf höchstens 42 sein."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeUpper_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(null, 42, null, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.ValueNotInValueSet.getId()));
        assertThat(message.getCode(), containsString("TestPolicyWithVisitor"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\" contains an invalid value. The value must be at most 42."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testOverrides() throws Exception {
        IGenericAttributeValidationConfiguration config = new CustomGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();

        Message message = config.createMessageForMissingMandatoryValue(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.MandatoryValueMissing.getId().toUpperCase()));
        assertThat(message.getCode(), containsString("TESTPOLICYWITHVISITOR"));
        assertThat(message.getCode(), containsString(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE.toUpperCase()));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\"(contains a number) on Test Policy must contain a value."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.hasMarker(MandatoryMarker.INSTANCE), is(true));
    }

    @Test
    public void testOverwrittenGetMessages() throws Exception {

        IGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US) {

            class SimpleResourceBundle extends ListResourceBundle {
                @Override
                public Locale getLocale() {
                    return Locale.US;
                }

                @Override
                protected Object[][] getContents() {
                    return new Object[][] {
                            { ERROR_MANDATORY_MSG_CODE_PREFIX, "Overwritten Message for %s" }
                    };
                }
            }

            @Override
            public ResourceBundle getMessages() {
                return new SimpleResourceBundle();
            }
        };
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicyWithVisitor.class)
                .getAttribute(TestPolicyWithVisitor.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicyWithVisitor modelObject = new TestPolicyWithVisitor();

        Message message = config.createMessageForMissingMandatoryValue(policyAttribute, modelObject,
                TestPolicyWithVisitor.class);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(GenericRelevanceValidation.Error.MandatoryValueMissing.getId().toUpperCase()));
        assertThat(message.getText(),
                is("Overwritten Message for \"Integer Attribute\""));
    }

    private static final class CustomGenericAttributeValidationConfiguration
            extends DefaultGenericAttributeValidationConfiguration {
        private CustomGenericAttributeValidationConfiguration(Locale locale) {
            super(ResourceBundle.getBundle(
                    "org.faktorips.runtime.validation.CustomGenericAttributeValidationConfiguration", locale), locale);
        }

        @Override
        protected String createMsgCode(GenericRelevanceValidation.Error error,
                PolicyAttribute policyAttribute,
                Class<? extends IModelObject> definingModelObjectClass) {
            return super.createMsgCode(error, policyAttribute, definingModelObjectClass).toUpperCase();
        }

        @Override
        protected String getLabelFor(PolicyAttribute policyAttribute, IModelObject modelObject) {
            String attributeLabel = super.getLabelFor(policyAttribute, modelObject);
            String attributeDescription = policyAttribute.getDescription(getLocale());
            if (IpsStringUtils.isBlank(attributeDescription)) {
                return attributeLabel;
            } else {
                return attributeLabel + "(" + attributeDescription + ")";
            }
        }

        @Override
        public Message createMessageForMissingMandatoryValue(PolicyAttribute policyAttribute,
                IModelObject modelObject,
                Class<? extends IModelObject> definingModelObjectClass) {
            String typeLabel = IpsModel.getPolicyCmptType(modelObject).getLabel(getLocale());
            Message errorMessage = createErrorMessage(policyAttribute, modelObject,
                    GenericRelevanceValidation.Error.MandatoryValueMissing, definingModelObjectClass,
                    format(ERROR_MANDATORY_MSG_CODE_PREFIX, getLabelFor(policyAttribute, modelObject), typeLabel));
            return new Message.Builder(errorMessage).markers(MandatoryMarker.INSTANCE).create();
        }
    }

    private enum MandatoryMarker implements IMarker {

        INSTANCE;

        @Override
        public boolean isRequiredInformationMissing() {
            return true;
        }

        @Override
        public boolean isTechnicalConstraintViolated() {
            return false;
        }
    }

}
