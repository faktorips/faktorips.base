/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.enums;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.enums.EnumTypeValidations;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class EnumTypeValidationsTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IEnumType abstractEnum;
    private IEnumType abstractSubEnum;
    private IEnumType paymentMode;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");

        abstractEnum = newEnumType(ipsProject, "AbstractEnum");
        abstractEnum.setAbstract(true);
        abstractEnum.setExtensible(false);
        abstractEnum.setEnumContentName("enumcontents");
        IEnumAttribute id = abstractEnum.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setUnique(true);
        id.setName("id");

        abstractSubEnum = newEnumType(ipsProject, "AbstractSubEnum");
        abstractSubEnum.setSuperEnumType(abstractEnum.getQualifiedName());
        abstractSubEnum.setAbstract(true);
        abstractSubEnum.setExtensible(false);
        abstractSubEnum.setEnumContentName("enumcontents");
        IEnumAttribute shortText = abstractSubEnum.newEnumAttribute();
        shortText.setDatatype(Datatype.STRING.getQualifiedName());
        shortText.setInherited(false);
        shortText.setUnique(false);
        shortText.setName("shortText");

        paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setSuperEnumType(abstractSubEnum.getQualifiedName());
        paymentMode.setAbstract(false);
        paymentMode.setExtensible(false);
        paymentMode.setEnumContentName("enumcontents");
        paymentMode.newEnumLiteralNameAttribute();

        id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(true);
        id.setUnique(true);
        id.setName("id");

        shortText = paymentMode.newEnumAttribute();
        shortText.setDatatype(Datatype.STRING.getQualifiedName());
        shortText.setInherited(true);
        shortText.setUnique(false);
        shortText.setName("shortText");

        IEnumValue value1 = paymentMode.newEnumValue();
        IEnumAttributeValue value1id = value1.getEnumAttributeValues().get(0);
        value1id.setValue(ValueFactory.createStringValue("monthly"));
        IEnumAttributeValue value1Text = value1.getEnumAttributeValues().get(1);
        value1Text.setValue(ValueFactory.createStringValue("Monthly Payment"));

        IEnumValue value2 = paymentMode.newEnumValue();
        IEnumAttributeValue value2id = value2.getEnumAttributeValues().get(0);
        value2id.setValue(ValueFactory.createStringValue("annually"));
        IEnumAttributeValue value2Text = value2.getEnumAttributeValues().get(1);
        value2Text.setValue(ValueFactory.createStringValue("Annual Payment"));
    }

    @Test
    public void testValidateSuperTypeHierarchyValidHierarchy() throws Exception {
        MessageList msgList = new MessageList();
        EnumTypeValidations.validateSuperTypeHierarchy(msgList, paymentMode, ipsProject);
        assertNull(msgList.getMessageByCode(IEnumType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY));
        assertNull(msgList.getMessageByCode(IEnumType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));
    }

    @Test
    public void testValidateSuperTypeHierarchyCycleHierarchy() throws Exception {
        abstractEnum.setSuperEnumType(paymentMode.getQualifiedName());
        MessageList msgList = new MessageList();
        EnumTypeValidations.validateSuperTypeHierarchy(msgList, paymentMode, ipsProject);
        assertNotNull(msgList.getMessageByCode(IEnumType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY));
    }

    @Test
    public void testValidateSuperTypeHierarchyInconsistentHierarchy() throws Exception {
        abstractEnum.setAbstract(false);
        MessageList msgList = new MessageList();
        EnumTypeValidations.validateSuperTypeHierarchy(msgList, paymentMode, ipsProject);
        assertNotNull(msgList.getMessageByCode(IEnumType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

        abstractEnum.setAbstract(true);
        abstractEnum.setSuperEnumType("DoesNotExist");
        msgList = new MessageList();
        EnumTypeValidations.validateSuperTypeHierarchy(msgList, paymentMode, ipsProject);
        assertNotNull(msgList.getMessageByCode(IEnumType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));
    }

    @Test
    public void testValidateEnumContentName() {
        MessageList msgList = new MessageList();
        EnumTypeValidations.validateEnumContentName(msgList, paymentMode, false, true, "");
        assertNotNull(msgList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_ENUM_CONTENT_NAME_EMPTY));

        msgList = new MessageList();
        EnumTypeValidations.validateEnumContentName(msgList, paymentMode, true, true, "");
        assertNull(msgList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_ENUM_CONTENT_NAME_EMPTY));
    }

}
