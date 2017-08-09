/*******************************************************************************
 * 
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import static org.faktorips.abstracttest.matcher.Matchers.hasMessageCode;
import static org.faktorips.abstracttest.matcher.Matchers.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ValueConverterIntegrationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IProductCmptType productCmptType;

    private IProductCmpt productCmpt;

    private IProductCmptTypeAttribute attribute;

    private IAttributeValue attrValue;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "Product");
        productCmpt = newProductCmpt(productCmptType, "ProductA");

        attribute = (IProductCmptTypeAttribute)productCmptType.newAttribute();
        attribute.setName("attribute");
        attribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        productCmpt.fixAllDifferencesToModel(ipsProject);
        attrValue = productCmpt.getLatestProductCmptGeneration().getAttributeValue("attribute");
    }

    @Test
    public void testValidateDecimalToMoney() throws CoreException {
        SingleValueHolder valueHolder = new SingleValueHolder(attrValue, "10.00");
        attrValue.setValueHolder(valueHolder);

        MessageList messageListForDecimal = attrValue.validate(ipsProject);
        assertThat(messageListForDecimal, isEmpty());

        attribute.setDatatype(Datatype.MONEY.getQualifiedName());

        MessageList messageListForMoney = attrValue.validate(ipsProject);
        assertThat(messageListForMoney,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        productCmpt.fixAllDifferencesToModel(ipsProject);

        MessageList messageListForFixedMoney = attrValue.validate(ipsProject);
        assertThat(messageListForFixedMoney, isEmpty());
        assertEquals("10.00 EUR", valueHolder.getStringValue());
    }

    @Test
    public void testValidateDecimalToMoneyWithNotConvertibleInput() throws CoreException {
        SingleValueHolder valueHolder = new SingleValueHolder(attrValue, "10 00");
        attrValue.setValueHolder(valueHolder);

        attribute.setDatatype(Datatype.MONEY.getQualifiedName());
        productCmpt.fixAllDifferencesToModel(ipsProject);

        MessageList messageListForFixedMoney = attrValue.validate(ipsProject);
        assertThat(messageListForFixedMoney,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
        assertEquals("10 00", valueHolder.getStringValue());
    }

    @Test
    public void testValidateDecimalToMoneyMultiValueAllConvertible() throws CoreException {
        attribute.setMultiValueAttribute(true);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        MultiValueHolder valueHolder = new MultiValueHolder(attrValue,
                Arrays.asList(new SingleValueHolder(attrValue, "10.00"), new SingleValueHolder(attrValue, "42"),
                        new SingleValueHolder(attrValue, "1.23")));
        attrValue.setValueHolder(valueHolder);

        MessageList messageListForDecimal = attrValue.validate(ipsProject);
        assertThat(messageListForDecimal, isEmpty());

        attribute.setDatatype(Datatype.MONEY.getQualifiedName());

        MessageList messageListForMoney = attrValue.validate(ipsProject);
        assertThat(messageListForMoney,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        productCmpt.fixAllDifferencesToModel(ipsProject);

        MessageList messageListForFixedMoney = attrValue.validate(ipsProject);
        assertThat(messageListForFixedMoney, isEmpty());
        assertEquals("[10.00 EUR, 42.00 EUR, 1.23 EUR]", valueHolder.getStringValue());
    }

    @Test
    public void testValidateDecimalToMoneyMultiValueSomeNotConvertible() throws CoreException {
        attribute.setMultiValueAttribute(true);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        MultiValueHolder valueHolder = new MultiValueHolder(attrValue,
                Arrays.asList(new SingleValueHolder(attrValue, "10.00"), new SingleValueHolder(attrValue, "42"),
                        new SingleValueHolder(attrValue, "1.234")));
        attrValue.setValueHolder(valueHolder);

        MessageList messageListForDecimal = attrValue.validate(ipsProject);
        assertThat(messageListForDecimal, isEmpty());

        attribute.setDatatype(Datatype.MONEY.getQualifiedName());

        MessageList messageListForMoney = attrValue.validate(ipsProject);
        assertThat(messageListForMoney,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        productCmpt.fixAllDifferencesToModel(ipsProject);

        MessageList messageListForFixedMoney = attrValue.validate(ipsProject);
        assertThat(messageListForFixedMoney,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
        assertEquals("[10.00 EUR, 42.00 EUR, 1.234]", valueHolder.getStringValue());
    }

}
