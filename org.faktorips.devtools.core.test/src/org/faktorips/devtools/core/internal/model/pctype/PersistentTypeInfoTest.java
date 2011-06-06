/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.DiscriminatorDatatype;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.PersistentType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PersistentTypeInfoTest extends PersistenceIpsTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        policyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
    }

    @Test
    public void testValidateMustUseTableFromRootEntity() throws CoreException {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();
        PolicyCmptType subPcType = newPolicyCmptType(ipsProject, "subtype");
        subPcType.setSupertype(policyCmptType.getQualifiedName());
        IPersistentTypeInfo perTypeInfoSub = subPcType.getPersistenceTypeInfo();

        persTypeInfo.setPersistentType(PersistentType.ENTITY);
        perTypeInfoSub.setPersistentType(PersistentType.ENTITY);
        persTypeInfo.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);
        perTypeInfoSub.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);

        persTypeInfo.setUseTableDefinedInSupertype(false);
        perTypeInfoSub.setUseTableDefinedInSupertype(true);

        MessageList msgList = persTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_MUST_USE_TABLE_FROM_ROOT_ENTITY));
        msgList = perTypeInfoSub.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_MUST_USE_TABLE_FROM_ROOT_ENTITY));

        perTypeInfoSub.setUseTableDefinedInSupertype(false);
        msgList = persTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_MUST_USE_TABLE_FROM_ROOT_ENTITY));
        msgList = perTypeInfoSub.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_MUST_USE_TABLE_FROM_ROOT_ENTITY));
    }

    @Test
    public void testValidate_InvalidTableNames() throws CoreException {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();

        persTypeInfo.setTableName("validTableName01");
        MessageList msgList = persTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));

        persTypeInfo.setTableName("invalid TableName-01");
        msgList = persTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));

        // test table name necessary either type is abstract or not abstract
        persTypeInfo.setInheritanceStrategy(InheritanceStrategy.JOINED_SUBCLASS);
        persTypeInfo.getPolicyCmptType().setAbstract(true);
        persTypeInfo.setTableName("validTableName01");
        msgList = persTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));

        // test in single table strategy the name must only be specified in the root entity type
        PolicyCmptType subPcType = newPolicyCmptType(ipsProject, "subtype");
        subPcType.setSupertype(policyCmptType.getQualifiedName());
        IPersistentTypeInfo perTypeInfoSub = subPcType.getPersistenceTypeInfo();
        perTypeInfoSub.setPersistentType(PersistentType.ENTITY);
        persTypeInfo.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);
        perTypeInfoSub.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);
        perTypeInfoSub.setTableName("subtypetablename");
        msgList = subPcType.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));

        perTypeInfoSub.setTableName("");
        msgList = subPcType.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));

        // test max length
        int maxTableNameLength = ipsProject.getProperties().getPersistenceOptions().getMaxTableNameLength();
        String tableName = StringUtils.repeat("a", maxTableNameLength);
        persTypeInfo.setTableName(tableName);
        msgList = persTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));

        persTypeInfo.setTableName("invalid_" + tableName);
        msgList = persTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));
    }

    @Test
    public void testSetPersistentType() {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persTypeInfo.setPersistentType(PersistentType.ENTITY);
        persTypeInfo.getIpsSrcFile().markAsClean();
        persTypeInfo.setPersistentType(PersistentType.NONE);
        assertTrue(persTypeInfo.getIpsSrcFile().isDirty());
    }

    @Test
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

    @Test
    public void testInitFromXml() {
        NodeList nodeList = getTestDocument().getElementsByTagName(IPersistentTypeInfo.XML_TAG);
        assertEquals(1, nodeList.getLength());

        Element element = (Element)nodeList.item(0);
        IPersistentTypeInfo persistenceTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persistenceTypeInfo.initFromXml(element);

        assertTrue(persistenceTypeInfo.isEnabled());
        assertTrue(persistenceTypeInfo.isDefinesDiscriminatorColumn());
        assertTrue(persistenceTypeInfo.isUseTableDefinedInSupertype());
        assertEquals("persistence descr", persistenceTypeInfo.getDescriptionText(Locale.US));
        assertEquals("D_COLUMN", persistenceTypeInfo.getDiscriminatorColumnName());
        assertEquals(DiscriminatorDatatype.INTEGER, persistenceTypeInfo.getDiscriminatorDatatype());
        assertEquals("422", persistenceTypeInfo.getDiscriminatorValue());
        assertEquals(InheritanceStrategy.SINGLE_TABLE, persistenceTypeInfo.getInheritanceStrategy());
        assertEquals("POLICY1", persistenceTypeInfo.getTableName());
    }

    @Test
    public void testToXml() throws CoreException {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persTypeInfo.setPersistentType(PersistentType.ENTITY);
        persTypeInfo.setDefinesDiscriminatorColumn(true);
        persTypeInfo.setUseTableDefinedInSupertype(true);
        persTypeInfo.setDiscriminatorColumnName("D_COLUMN");
        persTypeInfo.setDiscriminatorDatatype(DiscriminatorDatatype.CHAR);
        persTypeInfo.setDiscriminatorValue("A");
        persTypeInfo.setInheritanceStrategy(InheritanceStrategy.JOINED_SUBCLASS);
        persTypeInfo.setTableName("Table1");
        IDescription description = persTypeInfo.getDescription(Locale.US);
        description.setText("persistence descr");
        Element element = policyCmptType.toXml(newDocument());

        PolicyCmptType copyOfPcType = (PolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "Copy");
        copyOfPcType.initFromXml(element);
        IPersistentTypeInfo copy = copyOfPcType.getPersistenceTypeInfo();

        assertEquals("persistence descr", copy.getDescriptionText(Locale.US));
        assertEquals("D_COLUMN", copy.getDiscriminatorColumnName());
        assertEquals(DiscriminatorDatatype.CHAR, copy.getDiscriminatorDatatype());
        assertEquals("A", copy.getDiscriminatorValue());
        assertEquals(InheritanceStrategy.JOINED_SUBCLASS, copy.getInheritanceStrategy());
        assertEquals("Table1", copy.getTableName());
        assertTrue(copy.isEnabled());
        assertTrue(copy.isDefinesDiscriminatorColumn());
        assertTrue(copy.isUseTableDefinedInSupertype());
    }

    @Test
    public void testFindRootEntityWithMappedSuperclass() throws CoreException {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();

        PolicyCmptType superPcType = newPolicyCmptType(ipsProject, "SuperPolicy1");
        superPcType.getPersistenceTypeInfo().setPersistentType(PersistentType.MAPPED_SUPERCLASS);

        policyCmptType.setSupertype(superPcType.getQualifiedName());

        assertEquals(policyCmptType, persTypeInfo.findRootEntity());
    }

    @Test
    public void testFindRootEntity() throws CoreException {
        IPersistentTypeInfo persTypeInfo = policyCmptType.getPersistenceTypeInfo();
        assertEquals(policyCmptType, persTypeInfo.findRootEntity());

        policyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.NONE);
        assertNull(persTypeInfo.findRootEntity());

        policyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
        policyCmptType.setAbstract(true);
        assertEquals(policyCmptType, persTypeInfo.findRootEntity());

        PolicyCmptType superPcType = newPolicyCmptType(ipsProject, "SuperPolicy1");
        policyCmptType.setSupertype(superPcType.getQualifiedName());

        // with supertype but supertype should not persist
        assertEquals(policyCmptType, persTypeInfo.findRootEntity());

        superPcType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
        assertEquals(superPcType, persTypeInfo.findRootEntity());
        assertEquals(superPcType, superPcType.getPersistenceTypeInfo().findRootEntity());

        // doesn't matter if the super type is abstract
        superPcType.setAbstract(true);
        assertEquals(superPcType, persTypeInfo.findRootEntity());
    }

    @Test
    public void testValidateDiscriminatorValue() throws CoreException {
        MessageList msgList = null;
        PolicyCmptType superPcType = newPolicyCmptType(ipsProject, "SuperPolicy1");
        PolicyCmptType policyCmptType2 = newPolicyCmptType(ipsProject, "Policy2");
        policyCmptType.setSupertype(superPcType.getQualifiedName());
        policyCmptType2.setSupertype(superPcType.getQualifiedName());

        IPersistentTypeInfo persistenceTypeInfoSuper = superPcType.getPersistenceTypeInfo();
        IPersistentTypeInfo persistenceTypeInfo1 = policyCmptType.getPersistenceTypeInfo();
        IPersistentTypeInfo persistenceTypeInfo2 = policyCmptType2.getPersistenceTypeInfo();
        persistenceTypeInfoSuper.setPersistentType(PersistentType.ENTITY);
        persistenceTypeInfo1.setPersistentType(PersistentType.ENTITY);
        persistenceTypeInfo2.setPersistentType(PersistentType.ENTITY);

        persistenceTypeInfoSuper.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);
        persistenceTypeInfo1.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);
        persistenceTypeInfo2.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);

        msgList = persistenceTypeInfoSuper.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING));

        // if no attribute exists then the discriminator is not necessary
        msgList = persistenceTypeInfo1.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING));
        msgList = persistenceTypeInfo2.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING));

        IAttribute attribute1 = policyCmptType.newAttribute();
        IAttribute attribute2 = policyCmptType2.newAttribute();
        attribute1.setName("att1");
        attribute1.setDatatype("String");
        attribute2.setName("att1");
        attribute1.setDatatype("String");

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

        // with abstract subclass
        policyCmptType2.setAbstract(true);
        persistenceTypeInfo2.setDiscriminatorValue("");
        persistenceTypeInfoSuper.setDiscriminatorValue("1");
        msgList = persistenceTypeInfo1.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
        msgList = persistenceTypeInfo2.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));
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

    @Test
    public void testDiscriminatorDefinitionForMappedSuperclass() throws CoreException {
        MessageList msgList = null;
        IPersistentTypeInfo persistenceTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persistenceTypeInfo.setPersistentType(PersistentType.MAPPED_SUPERCLASS);
        persistenceTypeInfo.setDefinesDiscriminatorColumn(true);

        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_NOT_ALLOWED));

        persistenceTypeInfo.setDefinesDiscriminatorColumn(false);
        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_NOT_ALLOWED));

        persistenceTypeInfo.setDiscriminatorValue("X");
        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID));

        // test if discriminator definition is not necessary if superclass is marked as mapped
        // superclass
        persistenceTypeInfo.setDefinesDiscriminatorColumn(false);
        persistenceTypeInfo.setDiscriminatorValue("");
        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING));

        // if a subclass exists then the discriminator is necessary,
        // but the error is marked on the subclass
        PolicyCmptType subType = newPolicyCmptType(ipsProject, "subtype");
        subType.setSupertype(policyCmptType.getQualifiedName());
        subType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
        subType.newAttribute(); // TODO JPA Joerg sind attribte fuer diese Validierung wirklich
        // notwendig -> Test mit Eclipselink und Hibernate
        msgList = subType.getPersistenceTypeInfo().validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING));

        PolicyCmptType subSubType = newPolicyCmptType(ipsProject, "subsubtype");
        subSubType.setSupertype(subType.getQualifiedName());
        subSubType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
        subSubType.newAttribute();
        msgList = subType.getPersistenceTypeInfo().validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING));
        msgList = subSubType.getPersistenceTypeInfo().validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING));
    }

    @Test
    public void testTableNameForMappedSuperclass() throws CoreException {
        MessageList msgList = null;
        IPersistentTypeInfo persistenceTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persistenceTypeInfo.setPersistentType(PersistentType.MAPPED_SUPERCLASS);
        persistenceTypeInfo.setTableName("notValid");

        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));

        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_TABLE_NAME_INVALID));
        persistenceTypeInfo.setTableName("");
    }

    @Test
    public void testValidateUseTableDefinedInSupertype() throws CoreException {
        MessageList msgList = null;
        IPersistentTypeInfo persistenceTypeInfo = policyCmptType.getPersistenceTypeInfo();

        policyCmptType.setSupertype("");
        persistenceTypeInfo.setUseTableDefinedInSupertype(false);
        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_USE_TABLE_DEFINED_IN_SUPERTYPE_NOT_ALLOWED));

        // no superclass therefore it is not possible to use this definition
        persistenceTypeInfo.setUseTableDefinedInSupertype(true);
        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_USE_TABLE_DEFINED_IN_SUPERTYPE_NOT_ALLOWED));

        PolicyCmptType superPcType = newPolicyCmptType(ipsProject, "supertype");
        policyCmptType.setSupertype(superPcType.getQualifiedName());

        // no superclass therefore it is not possible to use this definition
        persistenceTypeInfo.setUseTableDefinedInSupertype(true);
        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_USE_TABLE_DEFINED_IN_SUPERTYPE_NOT_ALLOWED));
    }

    @Test
    public void testDuplicateColumnName() throws CoreException {
        MessageList ml = null;

        PolicyCmptType superPolicyCmptType = newPolicyCmptType(ipsProject, "super");
        superPolicyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());

        IPersistentTypeInfo superPersTypeInfo = superPolicyCmptType.getPersistenceTypeInfo();

        IPersistentTypeInfo pTypeInfo = policyCmptType.getPersistenceTypeInfo();
        IPolicyCmptTypeAttribute pcAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        IPersistentAttributeInfo pAttInfo = pcAttribute.getPersistenceAttributeInfo();
        pAttInfo.setTableColumnName("a");

        IPersistentAttributeInfo pAttInfo2 = policyCmptType.newPolicyCmptTypeAttribute().getPersistenceAttributeInfo();
        pAttInfo2.setTableColumnName("b");

        ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME));

        pAttInfo2.setTableColumnName("a");
        ml = pTypeInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME));

        // test duplicate column name in persistence type association
        // a) join column name
        // b) joint table source and target column name
        pAttInfo2.setTableColumnName("b");
        IPolicyCmptTypeAssociation association = policyCmptType.newPolicyCmptTypeAssociation();
        IPersistentAssociationInfo pAssInfo = association.getPersistenceAssociatonInfo();

        pAssInfo.setJoinColumnName("joinColumn");
        ml = pTypeInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME));

        pAssInfo.setJoinColumnName("a");
        ml = pTypeInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME));

        pAssInfo.setJoinColumnName("");
        pAssInfo.setJoinTableName("JoinTable");
        pAssInfo.setSourceColumnName("source");
        pAssInfo.setTargetColumnName("target");
        ml = pTypeInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME));

        pAssInfo.setSourceColumnName("a");
        pAssInfo.setTargetColumnName("target");
        ml = pTypeInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME));

        pAssInfo.setSourceColumnName("souce");
        pAssInfo.setTargetColumnName("a");
        ml = pTypeInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME));

        pAssInfo.setTargetColumnName("target");

        // test same column name in super type, single table strategy
        pTypeInfo.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);
        superPersTypeInfo.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = superPolicyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.getPersistenceAttributeInfo().setTableColumnName("superA");
        ml = pTypeInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME));

        policyCmptTypeAttribute.getPersistenceAttributeInfo().setTableColumnName("a");
        ml = pTypeInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME));

        // test same column name in super type, joined table strategy
        pTypeInfo.setInheritanceStrategy(InheritanceStrategy.JOINED_SUBCLASS);
        superPersTypeInfo.setInheritanceStrategy(InheritanceStrategy.JOINED_SUBCLASS);
        ml = pTypeInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME));
    }

    @Test
    public void testValidateInheritanceStrategy() throws CoreException {
        MessageList msgList = null;
        IPersistentTypeInfo persistenceTypeInfo = policyCmptType.getPersistenceTypeInfo();
        persistenceTypeInfo.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);

        policyCmptType.setSupertype("");
        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID));

        PolicyCmptType superPcType = newPolicyCmptType(ipsProject, "supertype");
        policyCmptType.setSupertype(superPcType.getQualifiedName());

        superPcType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
        superPcType.getPersistenceTypeInfo().setInheritanceStrategy(InheritanceStrategy.JOINED_SUBCLASS);
        persistenceTypeInfo.setInheritanceStrategy(InheritanceStrategy.SINGLE_TABLE);
        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID));

        // if supertype is mapped superclass then ignore the inheritance strategy of the supertype
        superPcType.getPersistenceTypeInfo().setPersistentType(PersistentType.MAPPED_SUPERCLASS);
        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID));

        // if supertypes persistence is not enabled then ignore the inheritance strategy of the
        // supertype
        superPcType.getPersistenceTypeInfo().setPersistentType(PersistentType.NONE);
        msgList = persistenceTypeInfo.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID));
    }
}
