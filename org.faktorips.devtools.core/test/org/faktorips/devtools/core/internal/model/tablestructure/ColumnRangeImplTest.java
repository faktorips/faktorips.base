package org.faktorips.devtools.core.internal.model.tablestructure;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.w3c.dom.Element;


/**
 *
 */
public class ColumnRangeImplTest extends IpsObjectTestCase {

    private TableStructure table;
    private ColumnRange range;
    
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.TABLE_STRUCTURE);
    }
    
    protected void createObjectAndPart() {
        table = new TableStructure(pdSrcFile);
        range = (ColumnRange)table.newRange();
    }

    public void testRemove() {
        IColumnRange r1 = table.newRange();
        IColumnRange r2 = table.newRange();
        
        r1.delete();
        assertEquals(2, table.getNumOfRanges());
        assertEquals(range, table.getRanges()[0]);
        assertEquals(r2, table.getRanges()[1]);
        assertTrue(pdSrcFile.isDirty());
    }

    public void testToXml() {
        range = (ColumnRange)table.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("ageFrom");
        range.setToColumn("ageTo");
        Element element = range.toXml(newDocument());
        assertEquals("1", element.getAttribute(IColumnRange.PROPERTY_ID));
        assertEquals(ColumnRange.TAG_NAME, element.getNodeName());
        assertEquals("twoColumn", element.getAttribute(IColumnRange.PROPERTY_RANGE_TYPE));
        assertEquals("ageFrom", element.getAttribute(IColumnRange.PROPERTY_FROM_COLUMN));
        assertEquals("ageTo", element.getAttribute(IColumnRange.PROPERTY_TO_COLUMN));
    }

    public void testInitFromXml() {
        range.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(42, range.getId());
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
}
