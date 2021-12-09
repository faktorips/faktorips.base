/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.valueset.StringLengthValueSet;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo.DateTimeMapping;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypeInfo.PersistentType;
import org.faktorips.runtime.MessageList;
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
    public void testTransient() throws CoreRuntimeException {
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
        assertEquals("premiumIndex", persistenceAttributeInfo.getIndexName());
    }

    @Test
    public void testToXml() throws CoreRuntimeException {
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
        persistenceAttributeInfo.setIndexName("XYZ");
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
        assertEquals("XYZ", persistenceAttributeInfo.getIndexName());
    }

    @Test
    public void testValidateSizeScalePrecision() throws CoreRuntimeException {
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

    private void setPersistenceOptionSizeScalePrecision(int size, int scale, int precision) throws CoreRuntimeException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        IPersistenceOptions persistenceOptions = properties.getPersistenceOptions();
        persistenceOptions.setMaxTableColumnSize(size);
        persistenceOptions.setMaxTableColumnScale(scale);
        persistenceOptions.setMaxTableColumnPrecision(precision);
        ipsProject.setProperties(properties);
    }

    @Test
    public void testValidateMaxColumnNameLength() throws CoreRuntimeException {
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
    public void testValidateWhitespaceInTableColumnName() throws CoreRuntimeException {
        IPersistentAttributeInfo pAttInfo = pcAttribute.getPersistenceAttributeInfo();

        pAttInfo.setTableColumnName("Invalid ColumnName");
        assertMessagePresent(pAttInfo);

        pAttInfo.setTableColumnName("Invalid\tColumnName");
        assertMessagePresent(pAttInfo);

        pAttInfo.setTableColumnName("Invalid\nColumnName");
        assertMessagePresent(pAttInfo);

        pAttInfo.setTableColumnName("ValidColumnName");
        MessageList ml = pAttInfo.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(
                        IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_COLNAME_MUST_NOT_CONTAIN_WHITESPACE_CHARACTERS));
    }

    private void assertMessagePresent(IPersistentAttributeInfo pAttInfo) throws CoreRuntimeException {
        MessageList ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(
                        IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_COLNAME_MUST_NOT_CONTAIN_WHITESPACE_CHARACTERS));
    }

    @Test
    public void testvalidateTableColumnNullableMatchesValueSet() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute("nullableAtt");
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.getValueSet().setContainsNull(true);
        IPersistentAttributeInfo persAttrInfo = attribute.getPersistenceAttributeInfo();
        persAttrInfo.setTableColumnName("a");
        persAttrInfo.setTableColumnNullable(false);

        MessageList list = persAttrInfo.validate(ipsProject);

        assertNotNull(list.getMessageByCode(
                IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_COLUMN_NULLABLE_DOES_NOT_MATCH_MODEL));
    }

    @Test
    public void testValidateStringLengthRestrictionsInModel_Unrestricted() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute("stringAtt");
        attribute.setDatatype(Datatype.STRING.getQualifiedName());
        IPersistentAttributeInfo persAttrInfo = attribute.getPersistenceAttributeInfo();
        persAttrInfo.setTableColumnName("a");

        MessageList list = persAttrInfo.validate(ipsProject);

        assertNotNull(list.getMessageByCode(
                IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_MODEL_CONTAINS_NO_LENGTH_RESTRICTION));
    }

    @Test
    public void testValidateStringLengthRestrictionsInModel_LimitExceeded() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute("stringAtt");
        attribute.setDatatype(Datatype.STRING.getQualifiedName());
        StringLengthValueSet valueSet = new StringLengthValueSet(attribute, "partId", "300", false);
        attribute.setValueSetCopy(valueSet);
        IPersistentAttributeInfo persAttrInfo = attribute.getPersistenceAttributeInfo();
        persAttrInfo.setTableColumnName("a");
        persAttrInfo.setTableColumnNullable(true);
        persAttrInfo.setTableColumnSize(255);

        MessageList list = persAttrInfo.validate(ipsProject);

        assertNotNull(list.getMessageByCode(
                IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_MODEL_EXCEEDS_COLUMN_SIZE));
    }

    @Test
    public void testValidateStringLengthRestrictionsInModel_LimitExceededUnlimited() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute("stringAtt");
        attribute.setDatatype(Datatype.STRING.getQualifiedName());
        StringLengthValueSet valueSet = new StringLengthValueSet(attribute, "partId", null, false);
        attribute.setValueSetCopy(valueSet);
        IPersistentAttributeInfo persAttrInfo = attribute.getPersistenceAttributeInfo();
        persAttrInfo.setTableColumnName("a");
        persAttrInfo.setTableColumnNullable(true);
        persAttrInfo.setTableColumnSize(255);

        MessageList list = persAttrInfo.validate(ipsProject);

        assertNotNull(list.getMessageByCode(
                IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_MODEL_EXCEEDS_COLUMN_SIZE));
    }

    @Test
    public void testEmptyTableName() throws CoreRuntimeException {
        IPersistentAttributeInfo pAttInfo = pcAttribute.getPersistenceAttributeInfo();
        pAttInfo.setTableColumnName("a");
        MessageList ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_EMPTY_COLNAME));

        pAttInfo.setTableColumnName("");
        ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_PERSISTENCEATTR_EMPTY_COLNAME));
    }

    @Test
    public void testColumnNameMustBeEmpty() throws CoreRuntimeException {
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
    public void testColumnNameIfOverwrittenAttribute() throws CoreRuntimeException {
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

    @Test
    public void testValidateIndexName() throws CoreRuntimeException {
        IPersistentAttributeInfo pAttInfo = pcAttribute.getPersistenceAttributeInfo();
        pAttInfo.setIndexName("");

        MessageList ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_INDEX_NAME_INVALID));

        pAttInfo.setIndexName(" ");

        ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_INDEX_NAME_INVALID));

        pAttInfo.setIndexName("INVALID INDEX_NAME");

        ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_INDEX_NAME_INVALID));

        pAttInfo.setIndexName(" INVALID_INDEX_NAME ");

        ml = pAttInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_INDEX_NAME_INVALID));

        pAttInfo.setIndexName("VALID_INDEX_NAME");

        ml = pAttInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAttributeInfo.MSGCODE_INDEX_NAME_INVALID));
    }
}
