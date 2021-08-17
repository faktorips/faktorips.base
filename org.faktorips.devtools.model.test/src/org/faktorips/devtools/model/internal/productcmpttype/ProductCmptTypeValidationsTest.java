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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductCmptTypeValidationsTest {

    private String policyCmptType = "pcType1";

    private String superPolicyCmptType = "superPcType1";

    @Mock
    private IProductCmptType superProductCmptType;

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private IPolicyCmptType foundSuperPolicyCmptType;

    @Mock
    private IIpsProject ipsProject;

    @Before
    public void setUpIpsProject() {
        when(ipsProject.findPolicyCmptType(superPolicyCmptType)).thenReturn(foundSuperPolicyCmptType);
    }

    @Test
    public void testIsConsistentHierarchy_noSuperPolicyType() throws Exception {
        String policyCmptTypeOfSupertype = "";
        when(superProductCmptType.isConfigurationForPolicyCmptType()).thenReturn(true);

        boolean constistent = ProductCmptTypeValidations.isConsistentHierarchy(policyCmptType, superPolicyCmptType,
                policyCmptTypeOfSupertype, superProductCmptType, ipsProject);

        assertFalse(constistent);
    }

    @Test
    public void testIsConsistentHierarchy_configuredSuperPolicyType() throws Exception {
        String policyCmptTypeOfProductCmptSupertype = superPolicyCmptType;
        when(superProductCmptType.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(foundSuperPolicyCmptType.isConfigurableByProductCmptType()).thenReturn(true);

        boolean constistent = ProductCmptTypeValidations.isConsistentHierarchy(policyCmptType, superPolicyCmptType,
                policyCmptTypeOfProductCmptSupertype, superProductCmptType, ipsProject);

        assertTrue(constistent);
    }

    @Test
    public void testIsConsistentHierarchy_configuredPolicyType() throws Exception {
        String policyCmptTypeOfProductCmptSupertype = policyCmptType;
        when(superProductCmptType.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(foundSuperPolicyCmptType.isConfigurableByProductCmptType()).thenReturn(true);

        boolean constistent = ProductCmptTypeValidations.isConsistentHierarchy(policyCmptType, superPolicyCmptType,
                policyCmptTypeOfProductCmptSupertype, superProductCmptType, ipsProject);

        assertTrue(constistent);
    }

    @Test
    public void testIsConsistentHierarchy_superTypesDontConfigureEachOther() throws Exception {
        String policyCmptTypeOfProductCmptSupertype = "";
        superPolicyCmptType = "";
        when(superProductCmptType.isConfigurationForPolicyCmptType()).thenReturn(false);

        boolean constistent = ProductCmptTypeValidations.isConsistentHierarchy(policyCmptType, superPolicyCmptType,
                policyCmptTypeOfProductCmptSupertype, superProductCmptType, ipsProject);

        assertTrue(constistent);
    }

    @Test
    public void testIsConsistentHierarchy_superTypeConfiguresSameType() throws Exception {
        String policyCmptTypeOfProductCmptSupertype = policyCmptType;

        boolean constistent = ProductCmptTypeValidations.isConsistentHierarchy(policyCmptType, superPolicyCmptType,
                policyCmptTypeOfProductCmptSupertype, superProductCmptType, ipsProject);

        assertTrue(constistent);
    }

    @Test
    public void testValidateSuperProductCmptTypeHasSameChangingOverTimeSetting_returnEmptyListIfSuperTypeIsNull() {
        MessageList messageList = new MessageList();

        ProductCmptTypeValidations.validateSuperProductCmptTypeHasSameChangingOverTimeSetting(messageList,
                productCmptType, null);

        assertNull(messageList
                .getMessageByCode(IProductCmptType.MSGCODE_SETTING_CHANGING_OVER_TIME_DIFFERS_FROM_SUPERTYPE));

    }

    @Test
    public void testValidateSuperProductCmptTypeHasSameChangingOverTimeSetting_returnEmptyListIfChangingOverTimeSettingsAreBothTrue() {
        MessageList messageList = new MessageList();
        when(productCmptType.isChangingOverTime()).thenReturn(true);
        when(superProductCmptType.isChangingOverTime()).thenReturn(true);

        ProductCmptTypeValidations.validateSuperProductCmptTypeHasSameChangingOverTimeSetting(messageList,
                productCmptType, superProductCmptType);

        assertNull(messageList
                .getMessageByCode(IProductCmptType.MSGCODE_SETTING_CHANGING_OVER_TIME_DIFFERS_FROM_SUPERTYPE));
    }

    @Test
    public void testValidateSuperProductCmptTypeHasSameChangingOverTimeSetting_returnEmptyListIfChangingOverTimeSettingsAreBothFalse() {
        MessageList messageList = new MessageList();
        when(productCmptType.isChangingOverTime()).thenReturn(false);
        when(superProductCmptType.isChangingOverTime()).thenReturn(false);

        ProductCmptTypeValidations.validateSuperProductCmptTypeHasSameChangingOverTimeSetting(messageList,
                productCmptType, superProductCmptType);

        assertNull(messageList
                .getMessageByCode(IProductCmptType.MSGCODE_SETTING_CHANGING_OVER_TIME_DIFFERS_FROM_SUPERTYPE));
    }

    @Test
    public void testValidateSuperProductCmptTypeHasSameChangingOverTimeSetting_returnMessageListIfChangingOverTimeSettingsBySubtypeFalseAndSupertypeTrue() {
        MessageList messageList = new MessageList();
        when(productCmptType.isChangingOverTime()).thenReturn(false);
        when(superProductCmptType.isChangingOverTime()).thenReturn(true);

        ProductCmptTypeValidations.validateSuperProductCmptTypeHasSameChangingOverTimeSetting(messageList,
                productCmptType, superProductCmptType);

        Message message = messageList
                .getMessageByCode(IProductCmptType.MSGCODE_SETTING_CHANGING_OVER_TIME_DIFFERS_FROM_SUPERTYPE);
        assertEquals(productCmptType, message.getInvalidObjectProperties().get(0).getObject());
        assertEquals(IProductCmptType.PROPERTY_CHANGING_OVER_TIME,
                message.getInvalidObjectProperties().get(0).getProperty());
        assertEquals(Message.ERROR, message.getSeverity());
    }

    @Test
    public void testValidateSuperProductCmptTypeHasSameChangingOverTimeSetting_returnMessageListIfChangingOverTimeSettingsBySubtypeTrueAndSupertypeFalse() {
        MessageList messageList = new MessageList();
        when(productCmptType.isChangingOverTime()).thenReturn(true);
        when(superProductCmptType.isChangingOverTime()).thenReturn(false);

        ProductCmptTypeValidations.validateSuperProductCmptTypeHasSameChangingOverTimeSetting(messageList,
                productCmptType, superProductCmptType);

        Message message = messageList
                .getMessageByCode(IProductCmptType.MSGCODE_SETTING_CHANGING_OVER_TIME_DIFFERS_FROM_SUPERTYPE);
        assertEquals(productCmptType, message.getInvalidObjectProperties().get(0).getObject());
        assertEquals(IProductCmptType.PROPERTY_CHANGING_OVER_TIME,
                message.getInvalidObjectProperties().get(0).getProperty());
        assertEquals(Message.ERROR, message.getSeverity());
    }
}
