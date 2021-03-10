package org.faktorips.runtime.validation;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.ResourceBundle;

import org.faktorips.runtime.IMarker;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.Message;
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
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicy();

        assertTrue(config.shouldValidate(policyAttribute, modelObject));
    }

    @Test
    public void testGetLabelFor() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicy();

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
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicy();

        Message message = config.createMessageForMissingMandatoryValue(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_MANDATORY_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("Das Feld »IntegerAttribute« muss einen Wert enthalten."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForMissingMandatoryValue_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicy();

        Message message = config.createMessageForMissingMandatoryValue(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_MANDATORY_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("Das Feld \"Integer-Attribut\" muss einen Wert enthalten."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForMissingMandatoryValue_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicy();

        Message message = config.createMessageForMissingMandatoryValue(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_MANDATORY_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" must contain a value."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
        public void testCreateMessageForValuePresentForIrrelevantAttribute_DE() throws Exception {
            DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                    Locale.GERMANY);
            PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                    .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
            IModelObject modelObject = new TestPolicy();
    
            Message message = config.createMessageForValuePresentForIrrelevantAttribute(policyAttribute, modelObject);
    
            assertNotNull(message);
            assertThat(message.getCode(),
                    startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_IRRELEVANT_MSG_CODE_PREFIX));
            assertThat(message.getCode(), containsString("TestPolicy"));
            assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
            assertThat(message.getText(), is("Das Feld \"Integer-Attribut\" darf keinen Wert enthalten."));
            assertThat(message.getNumOfInvalidObjectProperties(), is(1));
            assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
            assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                    is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
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
            PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                    .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
            IModelObject modelObject = new TestPolicy();
    
            Message message = config.createMessageForValuePresentForIrrelevantAttribute(policyAttribute, modelObject);
    
            assertNotNull(message);
            assertThat(message.getCode(),
                    startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_IRRELEVANT_MSG_CODE_PREFIX));
            assertThat(message.getCode(), containsString("TestPolicy"));
            assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
            assertThat(message.getText(), is("Das Feld »IntegerAttribute« darf keinen Wert enthalten."));
            assertThat(message.getNumOfInvalidObjectProperties(), is(1));
            assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
            assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                    is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        }

    @Test
        public void testCreateMessageForValuePresentForIrrelevantAttribute_EN() throws Exception {
            DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                    Locale.US);
            PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                    .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
            IModelObject modelObject = new TestPolicy();
    
            Message message = config.createMessageForValuePresentForIrrelevantAttribute(policyAttribute, modelObject);
    
            assertNotNull(message);
            assertThat(message.getCode(),
                    startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_IRRELEVANT_MSG_CODE_PREFIX));
            assertThat(message.getCode(), containsString("TestPolicy"));
            assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
            assertThat(message.getText(), is("The field \"Integer Attribute\" must not contain a value."));
            assertThat(message.getNumOfInvalidObjectProperties(), is(1));
            assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
            assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                    is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSet_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicy();

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
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
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicy();

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("Das Feld »IntegerAttribute« enthält einen ungültigen Wert."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSet_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        IModelObject modelObject = new TestPolicy();

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is("The field \"Integer Attribute\" contains an invalid value."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRange_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(10, 20, null, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is(
                "Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert. Der Wert muss zwischen 10 und 20 liegen."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRange_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(10, 20, null, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\" contains an invalid value. The value must be between 10 and 20."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeStep_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(10, 20, 5, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is(
                "Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert. Der Wert muss zwischen 10 und 20 (Schrittweite 5) liegen."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeStep_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(10, 20, 5, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\" contains an invalid value. The value must be between 10 and 20 (step 5)."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeLowerStep_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(3, null, 2, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is(
                "Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert. Der Wert muss mindestens 3 (Schrittweite 2) sein."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeLowerStep_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(3, null, 2, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\" contains an invalid value. The value must be at least 3 (step 2)."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeLower_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(3, null, null, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is(
                "Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert. Der Wert muss mindestens 3 sein."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeLower_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(3, null, null, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\" contains an invalid value. The value must be at least 3."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeUpperStep_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(null, 100, 10, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is(
                "Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert. Der Wert darf höchstens 100 (Schrittweite 10) sein."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeUpperStep_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(null, 100, 10, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\" contains an invalid value. The value must be at most 100 (step 10)."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeUpper_DE() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.GERMANY);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(null, 42, null, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(), is(
                "Das Feld \"Integer-Attribut\" enthält einen ungültigen Wert. Der Wert darf höchstens 42 sein."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testCreateMessageForValueNotInAllowedValueSetRangeUpper_EN() throws Exception {
        DefaultGenericAttributeValidationConfiguration config = new DefaultGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();
        modelObject.setAllowedValuesForIntegerAttribute(IntegerRange.valueOf(null, 42, null, false));

        Message message = config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(DefaultGenericAttributeValidationConfiguration.ERROR_INVALID_MSG_CODE_PREFIX));
        assertThat(message.getCode(), containsString("TestPolicy"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\" contains an invalid value. The value must be at most 42."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
    }

    @Test
    public void testOverrides() throws Exception {
        IGenericAttributeValidationConfiguration config = new CustomGenericAttributeValidationConfiguration(
                Locale.US);
        PolicyAttribute policyAttribute = IpsModel.getPolicyCmptType(TestPolicy.class)
                .getAttribute(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE);
        TestPolicy modelObject = new TestPolicy();

        Message message = config.createMessageForMissingMandatoryValue(policyAttribute, modelObject);

        assertNotNull(message);
        assertThat(message.getCode(),
                startsWith(
                        DefaultGenericAttributeValidationConfiguration.ERROR_MANDATORY_MSG_CODE_PREFIX.toUpperCase()));
        assertThat(message.getCode(), containsString("TESTPOLICY"));
        assertThat(message.getCode(), containsString(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE.toUpperCase()));
        assertThat(message.getText(),
                is("The field \"Integer Attribute\"(contains a number) on Test Policy must contain a value."));
        assertThat(message.getNumOfInvalidObjectProperties(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(modelObject));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(),
                is(TestPolicy.PROPERTY_INTEGER_ATTRIBUTE));
        assertThat(message.hasMarker(MandatoryMarker.INSTANCE), is(true));
    }

    private static final class CustomGenericAttributeValidationConfiguration
            extends DefaultGenericAttributeValidationConfiguration {
        private CustomGenericAttributeValidationConfiguration(Locale locale) {
            super(ResourceBundle.getBundle(
                    "org.faktorips.runtime.validation.CustomGenericAttributeValidationConfiguration", locale), locale);
        }

        @Override
        protected String createMsgCode(String msgCodePrefix,
                IModelObject modelObject,
                PolicyAttribute policyAttribute) {
            return super.createMsgCode(msgCodePrefix, modelObject, policyAttribute).toUpperCase();
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
                IModelObject modelObject) {
            String typeLabel = IpsModel.getPolicyCmptType(modelObject).getLabel(getLocale());
            Message errorMessage = createErrorMessage(policyAttribute, modelObject, ERROR_MANDATORY_MSG_CODE_PREFIX,
                    format(ERROR_MANDATORY_MSG_CODE_PREFIX, getLabelFor(policyAttribute, modelObject), typeLabel));
            return new Message.Builder(errorMessage).markers(MandatoryMarker.INSTANCE).create();
        }
    }

    private static enum MandatoryMarker implements IMarker {

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
