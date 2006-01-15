package org.faktorips.devtools.core.internal.model.tablestructure;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.w3c.dom.Element;


/**
 *
 */
public class TableStructureImplTest extends IpsPluginTest {
    
    private TableStructure table;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        table = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
    }
    
    public void testGetChildren() {
        IColumn c0 = table.newColumn();
        IColumnRange r0 = table.newRange();
        IUniqueKey uk0 = table.newUniqueKey();
        IForeignKey fk0 = table.newForeignKey();
        
        IIpsElement[] children = table.getChildren();
        assertEquals(4, children.length);
        assertEquals(c0, children[0]);
        assertEquals(r0, children[1]);
        assertEquals(uk0, children[2]);
        assertEquals(fk0, children[3]);
    }

    public void testGetColumns() {
        assertEquals(0, table.getColumns().length);
        IColumn c1 = table.newColumn();
        IColumn c2 = table.newColumn();
        assertEquals(2, table.getColumns().length);
        assertEquals(c1, table.getColumns()[0]);
        assertEquals(c2, table.getColumns()[1]);
    }

    public void testGetNumOfColumns() {
        assertEquals(0, table.getNumOfColumns());
        table.newColumn();
        assertEquals(1, table.getNumOfColumns());
        table.newColumn();
        assertEquals(2, table.getNumOfColumns());
    }

    public void testNewColumn() {
        IColumn c = table.newColumn();
        assertEquals(table, c.getParent());
        assertEquals(c, table.getColumns()[0]);
    }

    public void testGetRanges() {
        assertEquals(0, table.getRanges().length);
        IColumnRange r1 = table.newRange();
        IColumnRange r2 = table.newRange();
        assertEquals(2, table.getRanges().length);
        assertEquals(r1, table.getRanges()[0]);
        assertEquals(r2, table.getRanges()[1]);
    }

    public void testGetNumOfRanges() {
        assertEquals(0, table.getNumOfRanges());
        table.newRange();
        assertEquals(1, table.getNumOfRanges());
        table.newRange();
        assertEquals(2, table.getNumOfRanges());
    }

    public void testNewRange() {
        IColumnRange r = table.newRange();
        assertEquals(table, r.getParent());
        assertEquals(r, table.getRanges()[0]);
    }
    
    public void testGetColumnsNotInKey() {
        IColumn gender = table.newColumn();
        gender.setName("gender");
        IColumn ageFrom = table.newColumn();
        ageFrom.setName("ageFrom");
        IColumn ageTo = table.newColumn();
        ageTo.setName("ageTo");
        IColumn rate = table.newColumn();
        rate.setName("rate");
        IColumn premium = table.newColumn();
        premium.setName("premium");
        IColumnRange range = table.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("ageFrom");
        range.setToColumn("ageTo");
        IUniqueKey key = table.newUniqueKey();
        key.setKeyItems(new String[]{gender.getName(), range.getName()});
        IColumn[] columns = table.getColumnsNotInKey(key);
        assertEquals(2, columns.length);
        assertEquals(rate, columns[0]);
        assertEquals(premium, columns[1]);
    }
        
        public void testGetAccessFunctions() {
        ITableAccessFunction[] fcts = table.getAccessFunctions();
        assertEquals(0, fcts.length);

        IColumn gender = table.newColumn();
        gender.setName("gender");
        gender.setDatatype(Datatype.STRING.getQualifiedName());
        IColumn ageFrom = table.newColumn();
        ageFrom.setName("ageFrom");
        ageFrom.setDatatype(Datatype.INTEGER.getQualifiedName());
        IColumn ageTo = table.newColumn();
        ageTo.setName("ageTo");
        IColumn rate = table.newColumn();
        rate.setName("rate");
        rate.setDatatype(Datatype.DECIMAL.getQualifiedName());
        IColumn premium = table.newColumn();
        premium.setName("minPremium");
        premium.setDatatype(Datatype.MONEY.getQualifiedName());
        IColumnRange range = table.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("ageFrom");
        range.setToColumn("ageTo");
        IUniqueKey key = table.newUniqueKey();
        key.setKeyItems(new String[]{gender.getName(), range.getName()});
        
        fcts = table.getAccessFunctions();
        assertEquals(2, fcts.length);
        assertSame(table, fcts[0].getTableStructure());
        assertEquals("TestTable.rate", fcts[0].getName());
        assertEquals("rate", fcts[0].getAccessedColumn());
        assertSame(rate, fcts[0].findAccessedColumn());
        assertEquals(rate.getDatatype(), fcts[0].getType());
        String[] argTypes = fcts[0].getArgTypes();
        assertEquals(2, argTypes.length);
        assertEquals(gender.getDatatype(), argTypes[0]);
        assertEquals(range.getDatatype(), argTypes[1]);
        
        assertEquals("TestTable.minPremium", fcts[1].getName());
    }

    public void testInitFromXml() {
        table.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("blabla", table.getDescription());
        assertEquals(3, table.getNumOfColumns());
        assertEquals("ageFrom", table.getColumns()[0].getName());
        assertEquals("ageTo", table.getColumns()[1].getName());
        assertEquals("rate", table.getColumns()[2].getName());
        
        assertEquals(1, table.getNumOfRanges());
        assertEquals("ageFrom-ageTo", table.getRanges()[0].getName());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXmlDocument() {
        table.setDescription("blabla");
        IColumn column1 = table.newColumn();
        column1.setName("ageFrom");
        IColumn column2 = table.newColumn();
        column2.setName("ageTo");
        IColumnRange range = table.newRange();
        range.setFromColumn("ageFrom");
        range.setToColumn("ageTo");
        IUniqueKey uniqueKey = table.newUniqueKey();
        uniqueKey.setKeyItems(new String[]{"ageFrom", "ageTo"});
        
        Element element = table.toXml(this.newDocument());
        ITableStructure copy = new TableStructure();
        copy.initFromXml(element);
        
        assertEquals("blabla", copy.getDescription());
        assertEquals(2, copy.getNumOfColumns());
        assertEquals("ageFrom", copy.getColumns()[0].getName());
        assertEquals("ageTo", copy.getColumns()[1].getName());
        
        assertEquals(1, copy.getNumOfRanges());
        assertEquals("ageFrom-ageTo", copy.getRanges()[0].getName());
        
        assertEquals(1, copy.getNumOfUniqueKeys());
        IUniqueKey copyKey = copy.getUniqueKeys()[0];
        assertEquals(2, copyKey.getKeyItemNames().length);
        
    }


}
