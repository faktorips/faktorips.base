/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpttype;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ChangingOverTimePropertyValidatorTest extends AbstractIpsPluginTest {

    private ChangingOverTimePropertyValidator propertyValidator;
    private IIpsProject ipsProject;
    private IProductCmptTypeAttribute productAttribute;
    private IProductCmptProperty productCmptProperty;
    private IProductCmptType productCmptType;
    private PolicyCmptType policyCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();

        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = ipsProject.findProductCmptType("Product");

        productAttribute = productCmptType.newProductCmptTypeAttribute();
        productAttribute.setName("productAttribute");
        productCmptProperty = productAttribute;
    }

    @Test
    public void testValidateTypeDoesNotAcceptChangingOverTime() {
        productCmptType.setChangingOverTime(true);
        productAttribute.setChangingOverTime(false);

        propertyValidator = new ChangingOverTimePropertyValidator(productCmptProperty);
        MessageList ml = new MessageList();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);
        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(true);
        productAttribute.setChangingOverTime(true);

        ml = new MessageList();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);
        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(false);
        productAttribute.setChangingOverTime(false);

        ml = new MessageList();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);
        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(false);
        productAttribute.setChangingOverTime(true);

        ml = new MessageList();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);
        assertNotNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateTypeDoesNotAcceptChangingOverTime_NameIsEmpty() {
        productAttribute.setName("");
        productCmptType.setChangingOverTime(false);
        productAttribute.setChangingOverTime(true);

        propertyValidator = new ChangingOverTimePropertyValidator(productCmptProperty);
        MessageList ml = new MessageList();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);

        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateTypeDoesNotAcceptChangingOverTime_ProductCmptTypeIsNull() throws CoreException {
        IPolicyCmptType policyWithoutProductCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject,
                "PolicyWithoutProductCmptType");
        policyWithoutProductCmptType.setConfigurableByProductCmptType(true);
        IValidationRule validationRule = policyWithoutProductCmptType.newRule();
        validationRule.setName("vRule");
        validationRule.setConfigurableByProductComponent(true);

        propertyValidator = new ChangingOverTimePropertyValidator(validationRule);
        MessageList ml = new MessageList();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);

        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateTypeDoesNotAcceptChangingOverTime_RuleChangingOverTimeFlag() {
        IValidationRule policyRule = policyCmptType.newRule();
        policyRule.setName("rule");
        propertyValidator = new ChangingOverTimePropertyValidator(policyRule);

        policyRule.setChangingOverTime(true);
        productCmptType.setChangingOverTime(false);

        MessageList ml = new MessageList();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);
        assertThat(null, is(not(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME))));

        policyRule.setChangingOverTime(true);
        productCmptType.setChangingOverTime(true);

        ml.clear();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);
        assertThat(null, is(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME)));

        policyRule.setChangingOverTime(false);
        productCmptType.setChangingOverTime(true);

        ml.clear();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);
        assertThat(null, is(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME)));

        policyRule.setChangingOverTime(false);
        productCmptType.setChangingOverTime(false);

        ml.clear();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);
        assertThat(null, is(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME)));
    }
}
