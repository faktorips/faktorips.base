/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablestructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ColumnRangeTest extends AbstractIpsPluginTest {

    private IIpsSrcFile ipsSrcFile;
    private TableStructure table;
    private ColumnRange range;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        table = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
        ipsSrcFile = table.getIpsSrcFile();
        range = (ColumnRange)table.newRange();
        ipsSrcFile.save(true, null);
    }

    @Test
    public void testRemove() {
        IColumnRange r1 = table.newRange();
        IColumnRange r2 = table.newRange();

        r1.delete();
        assertEquals(2, table.getNumOfRanges());
        assertEquals(range, table.getRanges()[0]);
        assertEquals(r2, table.getRanges()[1]);
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testToXml() {
        range = (ColumnRange)table.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("ageFrom");
        range.setToColumn("ageTo");
        Element element = range.toXml(newDocument());
        assertEquals(range.getId(), element.getAttribute(IIpsObjectPart.PROPERTY_ID));
        assertEquals(ColumnRange.TAG_NAME, element.getNodeName());
        assertEquals("twoColumn", element.getAttribute(IColumnRange.PROPERTY_RANGE_TYPE));
        assertEquals("ageFrom", element.getAttribute(IColumnRange.PROPERTY_FROM_COLUMN));
        assertEquals("ageTo", element.getAttribute(IColumnRange.PROPERTY_TO_COLUMN));
    }

    @Test
    public void testToXmlNoFrom() {
        range = (ColumnRange)table.newRange();
        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_TO);
        range.setToColumn("ageTo");
        Element element = range.toXml(newDocument());
        assertEquals(range.getId(), element.getAttribute(IIpsObjectPart.PROPERTY_ID));
        assertEquals(ColumnRange.TAG_NAME, element.getNodeName());
        assertEquals("oneColumnTo", element.getAttribute(IColumnRange.PROPERTY_RANGE_TYPE));
        assertEquals("", element.getAttribute(IColumnRange.PROPERTY_FROM_COLUMN));
        assertEquals("ageTo", element.getAttribute(IColumnRange.PROPERTY_TO_COLUMN));
    }

    @Test
    public void testToXmlNoTo() {
        range = (ColumnRange)table.newRange();
        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_FROM);
        range.setFromColumn("ageFrom");
        Element element = range.toXml(newDocument());
        assertEquals(range.getId(), element.getAttribute(IIpsObjectPart.PROPERTY_ID));
        assertEquals(ColumnRange.TAG_NAME, element.getNodeName());
        assertEquals("oneColumnFrom", element.getAttribute(IColumnRange.PROPERTY_RANGE_TYPE));
        assertEquals("ageFrom", element.getAttribute(IColumnRange.PROPERTY_FROM_COLUMN));
        assertEquals("", element.getAttribute(IColumnRange.PROPERTY_TO_COLUMN));
    }

    @Test
    public void testInitFromXml() {
        range.initFromXml(XmlUtil.getFirstElement(getTestDocument().getDocumentElement()));
        assertEquals("42", range.getId());
        assertEquals(ColumnRangeType.TWO_COLUMN_RANGE, range.getColumnRangeType());
        assertEquals("ageFrom", range.getFromColumn());
        assertEquals("ageTo", range.getToColumn());
    }

    @Test
    public void testInitFromXmlEmptyFrom() {
        range.initFromXml(XmlUtil.getElement(getTestDocument().getDocumentElement(), 1));
        assertEquals("f", range.getId());
        assertEquals(ColumnRangeType.ONE_COLUMN_RANGE_FROM, range.getColumnRangeType());
        assertEquals("ageFrom", range.getFromColumn());
        assertEquals("", range.getToColumn());
    }

    @Test
    public void testInitFromXmlNoFrom() {
        range.initFromXml(XmlUtil.getElement(getTestDocument().getDocumentElement(), 2));
        assertEquals("fnew", range.getId());
        assertEquals(ColumnRangeType.ONE_COLUMN_RANGE_FROM, range.getColumnRangeType());
        assertEquals("ageFrom", range.getFromColumn());
        assertEquals("", range.getToColumn());
    }

    @Test
    public void testInitFromXmlEmptyTo() {
        range.initFromXml(XmlUtil.getElement(getTestDocument().getDocumentElement(), 3));
        assertEquals("t", range.getId());
        assertEquals(ColumnRangeType.ONE_COLUMN_RANGE_TO, range.getColumnRangeType());
        assertEquals("", range.getFromColumn());
        assertEquals("ageTo", range.getToColumn());
    }

    @Test
    public void testInitFromXmlNoTo() {
        range.initFromXml(XmlUtil.getElement(getTestDocument().getDocumentElement(), 4));
        assertEquals("tnew", range.getId());
        assertEquals(ColumnRangeType.ONE_COLUMN_RANGE_TO, range.getColumnRangeType());
        assertEquals("", range.getFromColumn());
        assertEquals("ageTo", range.getToColumn());
    }

    @Test
    public void testGetDatatype() {
        range.initFromXml(XmlUtil.getFirstElement(getTestDocument().getDocumentElement()));
        IColumn column = table.newColumn();
        column.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        column.setName("ageFrom");

        column = table.newColumn();
        column.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        column.setName("ageTo");

        String dataType = range.getDatatype();
        assertEquals("int", dataType);
    }

    @Test
    public void testGetColumns() {
        IColumn c0 = table.newColumn();
        c0.setName("c0");
        IColumn c1 = table.newColumn();
        c1.setName("c1");

        // two column range
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("c0");
        range.setToColumn("c1");
        IColumn[] columns = range.getColumns();
        assertEquals(2, columns.length);
        assertEquals(c0, columns[0]);
        assertEquals(c1, columns[1]);

        range.setFromColumn("unknownColumn");
        range.setToColumn("unknownColumn");
        columns = range.getColumns();
        assertEquals(0, columns.length);

        // from column range
        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_FROM);
        range.setFromColumn("c0");
        range.setToColumn("");
        columns = range.getColumns();
        assertEquals(1, columns.length);
        assertEquals(c0, columns[0]);

        range.setFromColumn("unknownColumn");
        columns = range.getColumns();
        assertEquals(0, columns.length);

        // to column range
        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_TO);
        range.setFromColumn("");
        range.setToColumn("c1");
        columns = range.getColumns();
        assertEquals(1, columns.length);
        assertEquals(c1, columns[0]);

        range.setToColumn("unknownColumn");
        columns = range.getColumns();
        assertEquals(0, columns.length);
    }

    /**
     * Tests if validate correctly signals an error for the missing parameter name of a newly
     * created range. Also tests if a correctly assigned parameter name does not raise an error on
     * validate().
     * 
     */
    @Test
    public void testValidate() {
        try {
            // the empty range will signal an error for the missing parameter name
            MessageList list = range.validate(ipsSrcFile.getIpsProject()).getMessagesFor(range,
                    IColumnRange.PROPERTY_PARAMETER_NAME);
            if (list.isEmpty() || !list.containsErrorMsg()) {
                fail();
            }

            // the parameter name must be a valid Java identifier, see FS #1415
            range.setParameterName("#invalidJavaIdentifier");
            list = range.validate(ipsSrcFile.getIpsProject()).getMessagesFor(range,
                    IColumnRange.PROPERTY_PARAMETER_NAME);
            if (list.isEmpty() || !list.containsErrorMsg()) {
                fail();
            }

            // an assigned parameter name must not signal an error message for the parameter name
            // property
            range.setParameterName("test");
            list = range.validate(ipsSrcFile.getIpsProject()).getMessagesFor(range,
                    IColumnRange.PROPERTY_PARAMETER_NAME);
            if (list.containsErrorMsg()) {
                fail();
            }
        } catch (CoreRuntimeException e) {
            fail();
        }
    }

    @Test
    public void testValidateRangeDatatype() throws CoreRuntimeException {
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setParameterName("egon");
        IColumn from = table.newColumn();
        from.setDatatype(Datatype.INTEGER.getName());
        from.setName("from");
        IColumn to = table.newColumn();
        to.setDatatype(Datatype.INTEGER.getName());
        to.setName("to");

        range.setFromColumn("from");
        range.setToColumn("to");

        table.getIpsSrcFile().save(true, null);

        MessageList ml = range.validate(ipsSrcFile.getIpsProject());
        assertTrue(ml.isEmpty());
    }

    @Test
    public void testValidate_FromColumnRange() throws CoreRuntimeException {
        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_FROM);
        range.setParameterName("egon");
        IColumn from = table.newColumn();
        from.setDatatype(Datatype.INTEGER.getName());
        from.setName("from");
        range.setFromColumn("from");

        MessageList ml = range.validate(ipsSrcFile.getIpsProject());

        assertTrue(ml.isEmpty());
    }

    @Test
    public void testValidate_ToColumnRange() throws CoreRuntimeException {
        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_TO);
        range.setParameterName("egon");
        IColumn to = table.newColumn();
        to.setDatatype(Datatype.INTEGER.getName());
        to.setName("to");
        range.setToColumn("to");

        MessageList ml = range.validate(ipsSrcFile.getIpsProject());

        assertTrue(ml.isEmpty());
    }

    @Test
    public void testValidateTwoColumnRangeWithSameDatatype() throws CoreRuntimeException {
        // test two column rage with same datatypes
        IColumn c0 = table.newColumn();
        c0.setName("c0");
        IColumn c1 = table.newColumn();
        c1.setName("c1");
        c0.setDatatype(Datatype.STRING.getName());
        c1.setDatatype(Datatype.STRING.getName());
        IColumnRange range = table.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        ((ColumnRange)range).setParameterName("c0Toc1");
        range.setFromColumn(c0.getName());
        range.setToColumn(c1.getName());

        MessageList ml = range.validate(ipsSrcFile.getIpsProject());
        assertTrue(ml.isEmpty());

        c1.setDatatype(Datatype.DECIMAL.getName());
        ml = range.validate(ipsSrcFile.getIpsProject());
        assertFalse(ml.isEmpty());
        assertNotNull(
                ml.getMessageByCode(IColumnRange.MSGCODE_TWO_COLUMN_RANGE_FROM_TO_COLUMN_WITH_DIFFERENT_DATATYPE));
        assertNotNull(ml.getMessagesFor(range));

        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_FROM);
        ((ColumnRange)range).setParameterName("c0");
        ml = range.validate(ipsSrcFile.getIpsProject());
        assertTrue(ml.isEmpty());

        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_TO);
        ((ColumnRange)range).setParameterName("c1");
        ml = range.validate(ipsSrcFile.getIpsProject());
        assertTrue(ml.isEmpty());

        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        ((ColumnRange)range).setParameterName("c0Toc1");
        ml = range.validate(ipsSrcFile.getIpsProject());
        assertFalse(ml.isEmpty());
        assertNotNull(
                ml.getMessageByCode(IColumnRange.MSGCODE_TWO_COLUMN_RANGE_FROM_TO_COLUMN_WITH_DIFFERENT_DATATYPE));

        c0.setDatatype(Datatype.DECIMAL.getName());
        ml = range.validate(ipsSrcFile.getIpsProject());
        assertTrue(ml.isEmpty());
    }
}
