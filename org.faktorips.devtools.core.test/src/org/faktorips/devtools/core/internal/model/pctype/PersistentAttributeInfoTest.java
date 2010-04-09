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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo.DateTimeMapping;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.PersistentType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PersistentAttributeInfoTest extends PersistenceIpsTest {

    private IPolicyCmptTypeAttribute pcAttribute;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        policyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
        pcAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        pcAttribute.getPersistenceAttributeInfo().setTransient(false);
    }

    public void testValidate() {
        // TODO missing validate test
        // MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS / scale and precision
    }

    public void testTransient() throws CoreException {
        IPersistentAttributeInfo pAttInfo = pcAttribute.getPersistenceAttributeInfo();
        int maxColumnNameLenght = ipsProject.getProperties().getPersistenceOptions().getMaxColumnNameLenght();
        String invalidColumnName = StringUtils.repeat("a", maxColumnNameLenght + 1);
        pAttInfo.setTableColumnName(invalidColumnName);

        MessageList ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_COLUMN_NAME_EXCEEDS_MAX_LENGTH));

        pAttInfo.setTransient(true);
        ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_COLUMN_NAME_EXCEEDS_MAX_LENGTH));
    }

    public void testInitFromXml() throws CoreException {
        NodeList nodeList = getTestDocument().getElementsByTagName(IPersistentAttributeInfo.XML_TAG);
        assertEquals(1, nodeList.getLength());

        Element element = (Element)nodeList.item(0);
        IPersistentAttributeInfo persistenceAttributeInfo = pcAttribute.getPersistenceAttributeInfo();
        persistenceAttributeInfo.initFromXml(element);

        assertFalse(persistenceAttributeInfo.isTransient());
        assertEquals("PREMIUM", persistenceAttributeInfo.getTableColumnName());
        assertTrue(persistenceAttributeInfo.getTableColumnNullable());
        assertEquals(2, persistenceAttributeInfo.getTableColumnPrecision());
        assertEquals(16, persistenceAttributeInfo.getTableColumnScale());
        assertEquals(255, persistenceAttributeInfo.getTableColumnSize());
        assertFalse(persistenceAttributeInfo.getTableColumnUnique());
        assertEquals(DateTimeMapping.DATE_ONLY, persistenceAttributeInfo.getTemporalMapping());
        assertEquals("sqlColumnDefinition1", persistenceAttributeInfo.getSqlColumnDefinition());
        assertEquals("converterQualifiedClassName1", persistenceAttributeInfo.getConverterQualifiedClassName());
    }

    public void testToXml() throws CoreException {
        IPersistentAttributeInfo persistenceAttributeInfo = pcAttribute.getPersistenceAttributeInfo();
        persistenceAttributeInfo.setTableColumnName("TEST_COLUMN");
        persistenceAttributeInfo.setTableColumnNullable(true);
        persistenceAttributeInfo.setTableColumnPrecision(3);
        persistenceAttributeInfo.setTableColumnScale(17);
        persistenceAttributeInfo.setTableColumnSize(256);
        persistenceAttributeInfo.setTableColumnUnique(false);
        persistenceAttributeInfo.setTemporalMapping(DateTimeMapping.DATE_AND_TIME);
        persistenceAttributeInfo.setTransient(true);
        persistenceAttributeInfo.setSqlColumnDefinition("sqlColumnDefinition0");
        persistenceAttributeInfo.setConverterQualifiedClassName("converterQualifiedClassName0");
        Element element = policyCmptType.toXml(newDocument());

        PolicyCmptType copyOfPcType = (PolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "Copy");
        copyOfPcType.initFromXml(element);

        assertEquals(1, copyOfPcType.getPolicyCmptTypeAttributes().length);
        IPersistentAttributeInfo persistenceAttributeInfoCopy = copyOfPcType.getPolicyCmptTypeAttributes()[0]
                .getPersistenceAttributeInfo();

        assertTrue(persistenceAttributeInfoCopy.isTransient());
        assertEquals("TEST_COLUMN", persistenceAttributeInfoCopy.getTableColumnName());
        assertTrue(persistenceAttributeInfoCopy.getTableColumnNullable());
        assertEquals(3, persistenceAttributeInfoCopy.getTableColumnPrecision());
        assertEquals(17, persistenceAttributeInfoCopy.getTableColumnScale());
        assertEquals(256, persistenceAttributeInfoCopy.getTableColumnSize());
        assertFalse(persistenceAttributeInfoCopy.getTableColumnUnique());
        assertEquals(DateTimeMapping.DATE_AND_TIME, persistenceAttributeInfoCopy.getTemporalMapping());
        assertEquals("sqlColumnDefinition0", persistenceAttributeInfo.getSqlColumnDefinition());
        assertEquals("converterQualifiedClassName0", persistenceAttributeInfo.getConverterQualifiedClassName());
    }

    public void testValidateMaxColumnNameLength() throws CoreException {
        IPersistentAttributeInfo pAttInfo = pcAttribute.getPersistenceAttributeInfo();
        int maxColumnNameLenght = ipsProject.getProperties().getPersistenceOptions().getMaxColumnNameLenght();
        String columnName = StringUtils.repeat("a", maxColumnNameLenght);
        pAttInfo.setTableColumnName(columnName);

        MessageList ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_COLUMN_NAME_EXCEEDS_MAX_LENGTH));

        columnName = "invalid" + columnName;
        pAttInfo.setTableColumnName(columnName);
        ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_COLUMN_NAME_EXCEEDS_MAX_LENGTH));
    }

    public void testEmptyTableName() throws CoreException {
        IPersistentAttributeInfo pAttInfo = pcAttribute.getPersistenceAttributeInfo();
        pAttInfo.setTableColumnName("a");
        MessageList ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_EMPTY_COLNAME));

        pAttInfo.setTableColumnName("");
        ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_EMPTY_COLNAME));
    }
}
