/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.DiscriminatorDatatype;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PersistentTypeInfoTest extends PersistenceIpsTest {

    public void testValidate_InvalidTableNames() throws CoreException {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();

        persTypeInfo.setTableName("validTableName01");
        MessageList msgList = persTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));

        persTypeInfo.setTableName("invalid TableName-01");
        msgList = persTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));

        // secondary table name is only validated if it is necessary
        persTypeInfo.setInheritanceStrategy(InheritanceStrategy.MIXED);
        persTypeInfo.setSecondaryTableName("VaL_id");
        msgList = persTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_SECONDARY_TABLE_NAME_INVALID));

        persTypeInfo.setSecondaryTableName("42invalid");
        msgList = persTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_SECONDARY_TABLE_NAME_INVALID));
    }

    public void testValidate_DiscriminatorInvalid() throws CoreException {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();

        persTypeInfo.setDiscriminatorColumnName("invali?");
        MessageList msgList = persTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_INVALID));

        persTypeInfo.setDiscriminatorColumnName("dtype");
        msgList = persTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_INVALID));

        // validate discriminator value which has the datatype String
        persTypeInfo.setDiscriminatorValue("value");
        assertNotNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.VOID));
        assertNotNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.CHAR));
        assertNotNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.INTEGER));
        assertNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.STRING));

        // validate discriminator value which has the datatype Integer
        persTypeInfo.setDiscriminatorValue("123455");
        assertNotNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.VOID));
        assertNotNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.CHAR));
        assertNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.INTEGER));
        assertNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.STRING));

        // validate discriminator value which has the datatype Char
        persTypeInfo.setDiscriminatorValue("X");
        assertNotNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.VOID));
        assertNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.CHAR));
        assertNotNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.INTEGER));
        assertNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.STRING));

        // validate empty value
        persTypeInfo.setDiscriminatorValue("");
        assertNotNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.VOID));
        assertNotNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.CHAR));
        assertNotNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.INTEGER));
        assertNotNull(getValidationMessageForDiscriminator(persTypeInfo, DiscriminatorDatatype.STRING));

        try {
            persTypeInfo.setDiscriminatorValue(null);
            fail();
        } catch (Exception expected) {
        }
    }

    private Message getValidationMessageForDiscriminator(IPersistentTypeInfo persTypeInfo,
            DiscriminatorDatatype datatype) throws CoreException {
        persTypeInfo.setDiscriminatorDatatype(datatype);
        MessageList msgList = persTypeInfo.validate(ipsProject);
        Message message = msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_INVALID);

        return message;
    }

    public void testInitFromXml() {
        NodeList nodeList = getTestDocument().getElementsByTagName(IPersistentTypeInfo.XML_TAG);
        assertEquals(1, nodeList.getLength());

        Element element = (Element)nodeList.item(0);
        IPersistentTypeInfo persistenceTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persistenceTypeInfo.initFromXml(element);

        assertEquals("persistence descr", persistenceTypeInfo.getDescription());;
        assertEquals("D_COLUMN", persistenceTypeInfo.getDiscriminatorColumnName());
        assertEquals(DiscriminatorDatatype.INTEGER, persistenceTypeInfo.getDiscriminatorDatatype());
        assertEquals("422", persistenceTypeInfo.getDiscriminatorValue());
        assertEquals(InheritanceStrategy.SINGLE_TABLE, persistenceTypeInfo.getInheritanceStrategy());
        assertEquals("POLICY1", persistenceTypeInfo.getTableName());
        assertEquals("", persistenceTypeInfo.getSecondaryTableName());
    }

    public void testToXml() throws CoreException {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persTypeInfo.setDescription("persistence descr");
        persTypeInfo.setDiscriminatorColumnName("D_COLUMN");
        persTypeInfo.setDiscriminatorDatatype(DiscriminatorDatatype.CHAR);
        persTypeInfo.setDiscriminatorValue("A");
        persTypeInfo.setInheritanceStrategy(InheritanceStrategy.JOINED_SUBCLASS);
        persTypeInfo.setTableName("Table1");
        persTypeInfo.setSecondaryTableName("SecondaryTable1");

        Element element = policyCmptType.toXml(newDocument());

        PolicyCmptType copyOfPcType = (PolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "Copy");
        copyOfPcType.initFromXml(element);
        IPersistentTypeInfo copy = copyOfPcType.getPersistenceTypeInfo();

        assertEquals("persistence descr", copy.getDescription());
        assertEquals("D_COLUMN", copy.getDiscriminatorColumnName());
        assertEquals(DiscriminatorDatatype.CHAR, copy.getDiscriminatorDatatype());
        assertEquals("A", copy.getDiscriminatorValue());
        assertEquals(InheritanceStrategy.JOINED_SUBCLASS, copy.getInheritanceStrategy());
        assertEquals("Table1", copy.getTableName());
        assertEquals("SecondaryTable1", copy.getSecondaryTableName());
    }
}
