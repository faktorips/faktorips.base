/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ValidationUtilsTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
    }

    @Test
    public void testCheckValue() throws Exception {
        MessageList ml = new MessageList();

        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "policyCmptType");
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("attribute");
        ValidationUtils.checkValue("Integer", "1", attribute, IAttribute.PROPERTY_DEFAULT_VALUE, ml);
        assertNull(ml
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        ValidationUtils.checkValue("Integer", "x", attribute, IAttribute.PROPERTY_DEFAULT_VALUE, ml);
        assertNotNull(ml
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        ValidationUtils.checkValue("x", "x", attribute, IAttribute.PROPERTY_DEFAULT_VALUE, ml);
        assertNotNull(ml
                .getMessageByCode(
                        IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND));
    }
}
