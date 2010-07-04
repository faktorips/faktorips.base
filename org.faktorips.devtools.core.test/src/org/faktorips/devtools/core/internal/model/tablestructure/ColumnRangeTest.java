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

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public class ColumnRangeTest extends AbstractIpsPluginTest {

    private IIpsSrcFile ipsSrcFile;
    private TableStructure table;
    private ColumnRange range;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        table = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
        ipsSrcFile = table.getIpsSrcFile();
        range = (ColumnRange)table.newRange();
        ipsSrcFile.save(true, null);
    }

    public void testRemove() {
        IColumnRange r1 = table.newRange();
        IColumnRange r2 = table.newRange();

        r1.delete();
        assertEquals(2, table.getNumOfRanges());
        assertEquals(range, table.getRanges()[0]);
        assertEquals(r2, table.getRanges()[1]);
        assertTrue(ipsSrcFile.isDirty());
    }

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

    public void testInitFromXml() {
        range.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("42", range.getId());
        assertEquals(ColumnRangeType.TWO_COLUMN_RANGE, range.getColumnRangeType());
        assertEquals("ageFrom", range.getFromColumn());
        assertEquals("ageTo", range.getToColumn());
    }

    public void testGetDatatype() {
        range.initFromXml(getTestDocument().getDocumentElement());
        IColumn column = table.newColumn();
        column.setDatatype(Datatype.PRIMITIVE_INT.getJavaClassName());
        column.setName("ageFrom");

        column = table.newColumn();
        column.setDatatype(Datatype.PRIMITIVE_INT.getJavaClassName());
        column.setName("ageTo");

        String dataType = range.getDatatype();
        assertEquals("int", dataType);
    }

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
     * Tests for the correct type of excetion to be thrown - no part of any type could ever be
     * created.
     */
    public void testNewPart() {
        try {
            range.newPart(IPolicyCmptTypeAttribute.class);
            fail();
        } catch (IllegalArgumentException e) {
            // nothing to do :-)
        }
    }

    /**
     * Tests if validate correctly signals an error for the missing parameter name of a newly
     * created range. Also tests if a correctly assigned parameter name does not raise an error on
     * validate().
     * 
     */
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
        } catch (CoreException e) {
            fail();
        }
    }

    public void testValidateRangeDatatype() throws CoreException {
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

        from.setDatatype(Datatype.BOOLEAN.getName());
        table.getIpsSrcFile().save(true, null);
        ml = range.validate(ipsSrcFile.getIpsProject());
        assertNotNull(ml.getMessageByCode(IColumnRange.MSGCODE_INVALID_DATATYPE_FOR_FROM));

        from.setDatatype(Datatype.PRIMITIVE_BOOLEAN.getName());
        table.getIpsSrcFile().save(true, null);
        ml = range.validate(ipsSrcFile.getIpsProject());
        assertNotNull(ml.getMessageByCode(IColumnRange.MSGCODE_INVALID_DATATYPE_FOR_FROM));

        from.setDatatype(Datatype.INTEGER.getName());
        to.setDatatype(Datatype.BOOLEAN.getName());
        table.getIpsSrcFile().save(true, null);
        ml = range.validate(ipsSrcFile.getIpsProject());
        assertNotNull(ml.getMessageByCode(IColumnRange.MSGCODE_INVALID_DATATYPE_FOR_TO));

        to.setDatatype(Datatype.PRIMITIVE_BOOLEAN.getName());
        table.getIpsSrcFile().save(true, null);
        ml = range.validate(ipsSrcFile.getIpsProject());
        assertNotNull(ml.getMessageByCode(IColumnRange.MSGCODE_INVALID_DATATYPE_FOR_TO));
    }

    public void testValidateTwoColumnRangeWithSameDatatype() throws CoreException {
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
        assertNotNull(ml.getMessageByCode(IColumnRange.MSGCODE_TWO_COLUMN_RANGE_FROM_TO_COLUMN_WITH_DIFFERENT_DATATYPE));
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
        assertNotNull(ml.getMessageByCode(IColumnRange.MSGCODE_TWO_COLUMN_RANGE_FROM_TO_COLUMN_WITH_DIFFERENT_DATATYPE));

        c0.setDatatype(Datatype.DECIMAL.getName());
        ml = range.validate(ipsSrcFile.getIpsProject());
        assertTrue(ml.isEmpty());
    }
}
