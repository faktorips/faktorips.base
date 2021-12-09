/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.hasSize;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ValueConverterIntegrationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IProductCmptType productCmptType;

    private PolicyCmptType policyCmptType;

    private IProductCmpt productCmpt;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "policyType", "productType");
        productCmptType = ipsProject.findProductCmptType("productType");
        productCmpt = newProductCmpt(productCmptType, "ProductA");
    }

    private IProductCmptTypeAttribute createProductAttribute() throws CoreRuntimeException {
        IProductCmptTypeAttribute productAttribute = (IProductCmptTypeAttribute)productCmptType.newAttribute();
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        productCmpt.fixAllDifferencesToModel(ipsProject);
        return productAttribute;
    }

    private IPolicyCmptTypeAttribute createPolicyAttribute() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute policyAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyAttribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        policyAttribute.setValueSetConfiguredByProduct(true);
        policyAttribute.setChangingOverTime(false);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        return policyAttribute;
    }

    @Test
    public void testValidateDecimalToMoney() throws CoreRuntimeException {
        IProductCmptTypeAttribute productAttribute = createProductAttribute();
        IAttributeValue attrValue = productCmpt.getLatestProductCmptGeneration().getAttributeValue("productAttribute");
        SingleValueHolder valueHolder = new SingleValueHolder(attrValue, "10.00");
        attrValue.setValueHolder(valueHolder);

        MessageList messageListForDecimal = attrValue.validate(ipsProject);
        assertThat(messageListForDecimal, isEmpty());

        productAttribute.setDatatype(Datatype.MONEY.getQualifiedName());

        MessageList messageListForMoney = attrValue.validate(ipsProject);
        assertThat(messageListForMoney,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        productCmpt.fixAllDifferencesToModel(ipsProject);

        MessageList messageListForFixedMoney = attrValue.validate(ipsProject);
        assertThat(messageListForFixedMoney, isEmpty());
        assertThat(valueHolder.getStringValue(), is("10.00 EUR"));
    }

    @Test
    public void testValidateDecimalToMoneyWithNotConvertibleInput() throws CoreRuntimeException {
        IProductCmptTypeAttribute productAttribute = createProductAttribute();
        IAttributeValue attrValue = productCmpt.getLatestProductCmptGeneration().getAttributeValue("productAttribute");
        SingleValueHolder valueHolder = new SingleValueHolder(attrValue, "10 00");
        attrValue.setValueHolder(valueHolder);

        productAttribute.setDatatype(Datatype.MONEY.getQualifiedName());
        productCmpt.fixAllDifferencesToModel(ipsProject);

        MessageList messageListForFixedMoney = attrValue.validate(ipsProject);
        assertThat(messageListForFixedMoney,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
        assertThat(valueHolder.getStringValue(), is("10 00"));
    }

    @Test
    public void testValidateDecimalToMoneyMultiValueAllConvertible() throws CoreRuntimeException {
        IProductCmptTypeAttribute productAttribute = createProductAttribute();
        IAttributeValue attrValue = productCmpt.getLatestProductCmptGeneration().getAttributeValue("productAttribute");
        productAttribute.setMultiValueAttribute(true);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        MultiValueHolder valueHolder = new MultiValueHolder(attrValue,
                Arrays.asList(new SingleValueHolder(attrValue, "10.00"), new SingleValueHolder(attrValue, "42"),
                        new SingleValueHolder(attrValue, "1.23")));
        attrValue.setValueHolder(valueHolder);

        MessageList messageListForDecimal = attrValue.validate(ipsProject);
        assertThat(messageListForDecimal, isEmpty());

        productAttribute.setDatatype(Datatype.MONEY.getQualifiedName());

        MessageList messageListForMoney = attrValue.validate(ipsProject);
        assertThat(messageListForMoney,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        productCmpt.fixAllDifferencesToModel(ipsProject);

        MessageList messageListForFixedMoney = attrValue.validate(ipsProject);
        assertThat(messageListForFixedMoney, isEmpty());
        assertThat(valueHolder.getStringValue(), is("[10.00 EUR, 42.00 EUR, 1.23 EUR]"));
    }

    @Test
    public void testValidateDecimalToMoneyMultiValueSomeNotConvertible() throws CoreRuntimeException {
        IProductCmptTypeAttribute productAttribute = createProductAttribute();
        IAttributeValue attrValue = productCmpt.getLatestProductCmptGeneration().getAttributeValue("productAttribute");
        productAttribute.setMultiValueAttribute(true);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        MultiValueHolder valueHolder = new MultiValueHolder(attrValue,
                Arrays.asList(new SingleValueHolder(attrValue, "10.00"), new SingleValueHolder(attrValue, "42"),
                        new SingleValueHolder(attrValue, "1.234")));
        attrValue.setValueHolder(valueHolder);

        MessageList messageListForDecimal = attrValue.validate(ipsProject);
        assertThat(messageListForDecimal, isEmpty());

        productAttribute.setDatatype(Datatype.MONEY.getQualifiedName());

        MessageList messageListForMoney = attrValue.validate(ipsProject);
        assertThat(messageListForMoney,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        productCmpt.fixAllDifferencesToModel(ipsProject);

        MessageList messageListForFixedMoney = attrValue.validate(ipsProject);
        assertThat(messageListForFixedMoney,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
        assertThat(valueHolder.getStringValue(), is("[10.00 EUR, 42.00 EUR, 1.234]"));
    }

    @Test
    public void testValidateDecimalToMoneyConfiguredDefault() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute policyAttribute = createPolicyAttribute();
        IConfiguredDefault configuredDefault = productCmpt.getPropertyValue(policyAttribute, IConfiguredDefault.class);
        configuredDefault.setValue("10.00");
        MessageList messageListDecimal = configuredDefault.validate(ipsProject);

        assertThat(messageListDecimal, isEmpty());

        policyAttribute.setDatatype(Datatype.MONEY.getQualifiedName());
        productCmpt.fixAllDifferencesToModel(ipsProject);
        MessageList messageListMoney = configuredDefault.validate(ipsProject);

        assertThat(messageListMoney, isEmpty());
        assertThat(configuredDefault.getValue(), is("10.00 EUR"));

    }

    @Test
    public void testValidateDecimalToMoneyConfiguredDefaultNonConvertableValues() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute policyAttribute = createPolicyAttribute();
        IConfiguredDefault configuredDefault = productCmpt.getPropertyValue(policyAttribute, IConfiguredDefault.class);
        configuredDefault.setValue("10.99999");
        MessageList messageListDecimal = configuredDefault.validate(ipsProject);

        assertThat(messageListDecimal, isEmpty());

        policyAttribute.setDatatype(Datatype.MONEY.getQualifiedName());
        productCmpt.fixAllDifferencesToModel(ipsProject);
        MessageList messageListMoney = configuredDefault.validate(ipsProject);

        assertThat(messageListMoney, hasSize(1));
        assertThat(messageListMoney,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
        assertThat(configuredDefault.getValue(), is("10.99999"));

    }

    @Test
    public void testValidateDecimalToMoneyConfiguredValueSetEnum() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute policyAttribute = createPolicyAttribute();
        IConfiguredValueSet configuredValueSet = productCmpt.getPropertyValue(policyAttribute,
                IConfiguredValueSet.class);
        List<String> valueList = Arrays.asList("1", "10.99");
        EnumValueSet values = new EnumValueSet(configuredValueSet, valueList,
                ipsProject.getIpsModel().getNextPartId(configuredValueSet));
        configuredValueSet.setValueSetType(ValueSetType.ENUM);
        configuredValueSet.setValueSet(values);

        MessageList messageListDecimal = configuredValueSet.validate(ipsProject);
        assertThat(messageListDecimal, isEmpty());

        policyAttribute.setDatatype(Datatype.MONEY.getQualifiedName());
        productCmpt.fixAllDifferencesToModel(ipsProject);
        MessageList messageListMoney = configuredValueSet.validate(ipsProject);

        assertThat(messageListMoney, isEmpty());

        assertThat(((EnumValueSet)configuredValueSet.getValueSet()).getValue(0), is("1.00 EUR"));
        assertThat(((EnumValueSet)configuredValueSet.getValueSet()).getValue(1), is("10.99 EUR"));
    }

    @Test
    public void testValidateDecimalToMoneyConfiguredValueSetRange() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute policyAttribute = createPolicyAttribute();
        IConfiguredValueSet configuredValueSet = productCmpt.getPropertyValue(policyAttribute,
                IConfiguredValueSet.class);
        RangeValueSet values = new RangeValueSet(configuredValueSet,
                ipsProject.getIpsModel().getNextPartId(configuredValueSet), "0", "10", "1.00", true);
        configuredValueSet.setValueSetType(ValueSetType.RANGE);
        configuredValueSet.setValueSet(values);

        MessageList messageListDecimal = configuredValueSet.validate(ipsProject);
        assertThat(messageListDecimal, isEmpty());

        policyAttribute.setDatatype(Datatype.MONEY.getQualifiedName());
        productCmpt.fixAllDifferencesToModel(ipsProject);
        MessageList messageListMoney = configuredValueSet.validate(ipsProject);

        assertThat(messageListMoney, isEmpty());

        assertThat(((RangeValueSet)configuredValueSet.getValueSet()).getLowerBound(), is("0.00 EUR"));
        assertThat(((RangeValueSet)configuredValueSet.getValueSet()).getUpperBound(), is("10.00 EUR"));
        assertThat(((RangeValueSet)configuredValueSet.getValueSet()).getStep(), is("1.00 EUR"));
    }
}
