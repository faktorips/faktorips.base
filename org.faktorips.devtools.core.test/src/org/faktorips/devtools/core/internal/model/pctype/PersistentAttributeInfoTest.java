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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo.DateTimeMapping;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.PersistentType;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PersistentAttributeInfoTest extends PersistenceIpsTest {

    private IPolicyCmptTypeAttribute pcAttribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        policyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
        pcAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        pcAttribute.getPersistenceAttributeInfo().setTransient(false);
        pcAttribute.setName("attr1");
    }

    @Test
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

    @Test
    public void testInitFromXml() {
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

    @Test
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

        assertEquals(1, copyOfPcType.getPolicyCmptTypeAttributes().size());
        IPersistentAttributeInfo persistenceAttributeInfoCopy = copyOfPcType.getPolicyCmptTypeAttributes().get(0)
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

    @Test
    public void testValidateSizeScalePrecision() throws CoreException {
        IPersistentAttributeInfo pAttInfo = pcAttribute.getPersistenceAttributeInfo();
        MessageList ml = null;

        setPersistenceOptionSizeScalePrecision(10, 20, 30);
        pAttInfo.setTableColumnSize(2);
        pAttInfo.setTableColumnScale(2);
        pAttInfo.setTableColumnPrecision(2);

        ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS));

        pAttInfo.setTableColumnSize(11);
        ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS));

        pAttInfo.setTableColumnSize(2);
        pAttInfo.setTableColumnScale(2);
        pAttInfo.setTableColumnPrecision(2);

        pAttInfo.setTableColumnScale(21);
        ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS));

        pAttInfo.setTableColumnSize(2);
        pAttInfo.setTableColumnScale(2);
        pAttInfo.setTableColumnPrecision(2);

        pAttInfo.setTableColumnPrecision(31);
        ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS));

        pAttInfo.setTableColumnSize(2);
        pAttInfo.setTableColumnScale(2);
        pAttInfo.setTableColumnPrecision(2);
        ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS));
    }

    private void setPersistenceOptionSizeScalePrecision(int size, int scale, int precision) throws CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        IPersistenceOptions persistenceOptions = properties.getPersistenceOptions();
        persistenceOptions.setMaxTableColumnSize(size);
        persistenceOptions.setMaxTableColumnScale(scale);
        persistenceOptions.setMaxTableColumnPrecision(precision);
        ipsProject.setProperties(properties);
    }

    @Test
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

    @Test
    public void testEmptyTableName() throws CoreException {
        IPersistentAttributeInfo pAttInfo = pcAttribute.getPersistenceAttributeInfo();
        pAttInfo.setTableColumnName("a");
        MessageList ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_EMPTY_COLNAME));

        pAttInfo.setTableColumnName("");
        ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_EMPTY_COLNAME));
    }

    @Test
    public void testColumnNameMustBeEmpty() throws CoreException {
        IPersistentAttributeInfo pAttInfo = pcAttribute.getPersistenceAttributeInfo();
        pAttInfo.setTransient(false);

        pAttInfo.setTableColumnName("a");
        pcAttribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        MessageList ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_COLNAME_MUST_BE_EMPTY));

        pAttInfo.setTableColumnName("");
        ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_COLNAME_MUST_BE_EMPTY));

        pAttInfo.setTableColumnName("a");
        pcAttribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_COLNAME_MUST_BE_EMPTY));

        pAttInfo.setTableColumnName("a");
        pcAttribute.setAttributeType(AttributeType.CHANGEABLE);
        ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_COLNAME_MUST_BE_EMPTY));
    }

    @Test
    public void testColumnNameIfOverwrittenAttribute() throws CoreException {
        MessageList ml;
        PolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "SubPolicy");
        policyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
        policyCmptType.setSupertype(policyCmptType.getQualifiedName());
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        IPersistentAttributeInfo persAttrInfo = attribute.getPersistenceAttributeInfo();
        persAttrInfo.setTransient(false);
        attribute.setOverwrite(false);
        attribute.setName(pcAttribute.getName() + "_2");
        ml = persAttrInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_EMPTY_COLNAME));

        attribute.setName(pcAttribute.getName());
        attribute.setOverwrite(true);
        ml = persAttrInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_EMPTY_COLNAME));

    }
}
