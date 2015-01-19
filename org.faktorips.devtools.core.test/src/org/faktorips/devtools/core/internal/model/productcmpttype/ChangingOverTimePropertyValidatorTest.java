/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ChangingOverTimePropertyValidatorTest extends AbstractIpsPluginTest {

    private ChangingOverTimePropertyValidator propertyValidator;
    private IIpsProject ipsProject;
    private IProductCmptTypeAttribute productAttribute;
    private IProductCmptProperty productCmptProperty;
    private IProductCmptType productCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();

        productCmptType = newProductCmptType(ipsProject, "Product");
        productCmptType.setPolicyCmptType("Policy");

        productAttribute = productCmptType.newProductCmptTypeAttribute();
        productAttribute.setName("productAttribute");
        productCmptProperty = productAttribute;
    }

    @Test
    public void testValidateTypeDoesNotAcceptChangingOverTime() {
        productCmptType.setChangingOverTime(true);
        productAttribute.setChangingOverTime(false);

        propertyValidator = new ChangingOverTimePropertyValidator(productCmptProperty, productCmptType);
        MessageList ml = new MessageList();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);
        assertNull(ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(true);
        productAttribute.setChangingOverTime(true);

        ml = new MessageList();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);
        assertNull(ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(false);
        productAttribute.setChangingOverTime(false);

        ml = new MessageList();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);
        assertNull(ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(false);
        productAttribute.setChangingOverTime(true);

        ml = new MessageList();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);
        assertNotNull(ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateTypeDoesNotAcceptChangingOverTime_NameIsEmpty() {
        productAttribute.setName("");
        productCmptType.setChangingOverTime(false);
        productAttribute.setChangingOverTime(true);

        propertyValidator = new ChangingOverTimePropertyValidator(productCmptProperty, productCmptType);
        MessageList ml = new MessageList();
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(ml);

        assertNull(ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

}
