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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        policyCmptType.getPersistenceTypeInfo().setEnabled(true);
    }

    public void testValidate_InvalidTableNames() throws CoreException {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();

        persTypeInfo.setTableName("validTableName01");
        MessageList msgList = persTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));

        persTypeInfo.setTableName("invalid TableName-01");
        msgList = persTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));

        // secondary table name is only validated if it is necessary
        // persTypeInfo.setInheritanceStrategy(InheritanceStrategy.MIXED);
        // persTypeInfo.setSecondaryTableName("VaL_id");
        // msgList = persTypeInfo.validate(ipsProject);
        // assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_SECONDARY_TABLE_NAME_INVALID));
        //
        // persTypeInfo.setSecondaryTableName("42invalid");
        // msgList = persTypeInfo.validate(ipsProject);
        // assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_SECONDARY_TABLE_NAME_INVALID));
    }

    public void testSetEnabled() {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persTypeInfo.getIpsSrcFile().markAsClean();
        persTypeInfo.setEnabled(!persTypeInfo.isEnabled());
        assertTrue(persTypeInfo.getIpsSrcFile().isDirty());
    }

    public void testValidate_DiscriminatorInvalid() throws CoreException {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persTypeInfo.setDefinesDiscriminatorColumn(true);
        persTypeInfo.setDiscriminatorValue("value");
        persTypeInfo.setDiscriminatorColumnName("invali?");
        MessageList msgList = persTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));

        persTypeInfo.setDiscriminatorColumnName("dtype");
        msgList = persTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));

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

        // validate empty valueSuper
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
        Message message = msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID);

        return message;
    }

    public void testInitFromXml() {
        NodeList nodeList = getTestDocument().getElementsByTagName(IPersistentTypeInfo.XML_TAG);
        assertEquals(1, nodeList.getLength());

        Element element = (Element)nodeList.item(0);
        IPersistentTypeInfo persistenceTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persistenceTypeInfo.initFromXml(element);

        assertTrue(persistenceTypeInfo.isEnabled());
        assertTrue(persistenceTypeInfo.isDefinesDiscriminatorColumn());
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
        persTypeInfo.setDefinesDiscriminatorColumn(false);
        persTypeInfo.setDescription("persistence descr");
        persTypeInfo.setDiscriminatorColumnName("D_COLUMN");
        persTypeInfo.setDiscriminatorDatatype(DiscriminatorDatatype.CHAR);
        persTypeInfo.setDiscriminatorValue("A");
        persTypeInfo.setInheritanceStrategy(InheritanceStrategy.JOINED_SUBCLASS);
        persTypeInfo.setTableName("Table1");
        persTypeInfo.setSecondaryTableName("SecondaryTable1");
        persTypeInfo.setEnabled(false);
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
        assertFalse(copy.isEnabled());
        assertFalse(copy.isDefinesDiscriminatorColumn());
    }

    public void testFindBaseEntity() throws CoreException {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();
        assertEquals(policyCmptType, persTypeInfo.findBaseEntity());

        policyCmptType.getPersistenceTypeInfo().setEnabled(false);
        assertNull(persTypeInfo.findBaseEntity());

        policyCmptType.getPersistenceTypeInfo().setEnabled(true);
        policyCmptType.setAbstract(true);
        assertEquals(policyCmptType, persTypeInfo.findBaseEntity());

        PolicyCmptType superPcType = newPolicyCmptType(ipsProject, "SuperPolicy1");
        policyCmptType.setSupertype(superPcType.getQualifiedName());

        // with supertype but supertype should not persist
        assertEquals(policyCmptType, persTypeInfo.findBaseEntity());

        superPcType.getPersistenceTypeInfo().setEnabled(true);
        assertEquals(superPcType, persTypeInfo.findBaseEntity());
        assertEquals(superPcType, superPcType.getPersistenceTypeInfo().findBaseEntity());

        // doesn't matter the type is abstract
        superPcType.setAbstract(true);
        assertEquals(superPcType, persTypeInfo.findBaseEntity());
    }

    public void testValidateDiscriminatorValue() throws CoreException {
        MessageList msgList = null;
        PolicyCmptType superPcType = newPolicyCmptType(ipsProject, "SuperPolicy1");
        PolicyCmptType policyCmptType2 = newPolicyCmptType(ipsProject, "Policy2");
        policyCmptType.setSupertype(superPcType.getQualifiedName());
        policyCmptType2.setSupertype(superPcType.getQualifiedName());

        IPersistentTypeInfo persistenceTypeInfoSuper = superPcType.getPersistenceTypeInfo();
        IPersistentTypeInfo persistenceTypeInfo1 = policyCmptType.getPersistenceTypeInfo();
        IPersistentTypeInfo persistenceTypeInfo2 = policyCmptType2.getPersistenceTypeInfo();
        persistenceTypeInfoSuper.setEnabled(true);
        persistenceTypeInfo1.setEnabled(true);
        persistenceTypeInfo2.setEnabled(true);

        persistenceTypeInfoSuper.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);
        persistenceTypeInfo1.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);
        persistenceTypeInfo2.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);

        msgList = persistenceTypeInfoSuper.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING));

        msgList = persistenceTypeInfo1.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING));
        msgList = persistenceTypeInfo2.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING));

        // super type without and with discriminator value

        persistenceTypeInfoSuper.setDefinesDiscriminatorColumn(true);
        persistenceTypeInfoSuper.setDiscriminatorColumnName("DTYPE");
        persistenceTypeInfoSuper.setDiscriminatorDatatype(DiscriminatorDatatype.STRING);
        persistenceTypeInfoSuper.setDiscriminatorValue("");
        superPcType.setAbstract(true);// if abstract then the discriminator is not necessary
        msgList = persistenceTypeInfoSuper.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING));
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
        superPcType.setAbstract(false);
        msgList = persistenceTypeInfoSuper.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
        persistenceTypeInfoSuper.setDiscriminatorValue("S");
        msgList = persistenceTypeInfoSuper.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));

        // test subtype

        msgList = persistenceTypeInfo1.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
        msgList = persistenceTypeInfo2.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));

        persistenceTypeInfo1.setDiscriminatorValue("1");
        persistenceTypeInfo2.setDiscriminatorValue("1");
        msgList = persistenceTypeInfo1.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
        msgList = persistenceTypeInfo2.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));

        // same discriminator value

        persistenceTypeInfoSuper.setDiscriminatorValue("1");
        msgList = persistenceTypeInfo1.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
        msgList = persistenceTypeInfo2.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
        // supertype value not checked
        msgList = superPcType.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));

        // with only one subtype

        policyCmptType2.setSupertype("");
        persistenceTypeInfo2.setDiscriminatorValue("");

        persistenceTypeInfoSuper.setDiscriminatorValue("1");
        msgList = persistenceTypeInfo1.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
        msgList = persistenceTypeInfo2.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
        msgList = superPcType.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));

        // with join table

        persistenceTypeInfoSuper.setInheritanceStrategy(InheritanceStrategy.JOINED_SUBCLASS);
        persistenceTypeInfo1.setInheritanceStrategy(InheritanceStrategy.JOINED_SUBCLASS);

        msgList = persistenceTypeInfo1.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
        msgList = superPcType.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));

        persistenceTypeInfoSuper.setDiscriminatorValue("S");
        msgList = persistenceTypeInfo1.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
        msgList = superPcType.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
    }
}
