package org.faktorips.devtools.core.internal.model.tablecontents;

import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.w3c.dom.Element;


/**
 *
 */
public class TableContentsImplTest extends IpsObjectTestCase {
    
    private TableContents table;
    
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.TABLE_STRUCTURE);
    }
    
    protected void createObjectAndPart() {
        table = new TableContents(pdSrcFile);
    }
    
    public void testNewColumn() {
        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        IRow row11 = gen1.newRow();
        IRow row12 = gen1.newRow();
        ITableContentsGeneration gen2 = (ITableContentsGeneration)table.newGeneration();
        IRow row21 = gen1.newRow();
        IRow row22 = gen1.newRow();
        
        pdSrcFile.markAsClean();
        table.newColumn("a");
        assertTrue(pdSrcFile.isDirty());
        assertEquals(1, table.getNumOfColumns());
        assertEquals("a", row11.getValue(0));
        assertEquals("a", row12.getValue(0));
        assertEquals("a", row21.getValue(0));
        assertEquals("a", row22.getValue(0));
        
        table.newColumn("b");
        assertEquals(2, table.getNumOfColumns());
        assertEquals("a", row11.getValue(0));
        assertEquals("a", row12.getValue(0));
        assertEquals("a", row21.getValue(0));
        assertEquals("a", row22.getValue(0));
        assertEquals("b", row11.getValue(1));
        assertEquals("b", row12.getValue(1));
        assertEquals("b", row21.getValue(1));
        assertEquals("b", row22.getValue(1));
    }
    
    public void testDeleteColumn() {
        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        IRow row11 = gen1.newRow();
        IRow row12 = gen1.newRow();
        ITableContentsGeneration gen2 = (ITableContentsGeneration)table.newGeneration();
        IRow row21 = gen1.newRow();
        IRow row22 = gen1.newRow();
        
        table.newColumn("a");
        table.newColumn("b");
        table.newColumn("c");
        
        pdSrcFile.markAsClean();
        table.deleteColumn(1);
        assertTrue(pdSrcFile.isDirty());
        assertEquals(2, table.getNumOfColumns());
        assertEquals("a", row11.getValue(0));
        assertEquals("a", row12.getValue(0));
        assertEquals("a", row21.getValue(0));
        assertEquals("a", row22.getValue(0));
        assertEquals("c", row11.getValue(1));
        assertEquals("c", row12.getValue(1));
        assertEquals("c", row21.getValue(1));
        assertEquals("c", row22.getValue(1));
        
    }

    public void testInitFromXml() {
        table.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("blabla", table.getDescription());
        assertEquals("RateTableStructure", table.getTableStructure());
        assertEquals(2, table.getNumOfColumns());
        assertEquals(2, table.getNumOfGenerations());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXmlDocument() {
        table.setDescription("blabla");
        table.setTableStructure("RateTableStructure");
        table.newColumn("");
        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        IIpsObjectGeneration gen2 = table.newGeneration();
        IRow row = gen1.newRow();
        row.setValue(0, "value");
        
        Element element = table.toXml(this.newDocument());
        table.setDescription("");
        table.setTableStructure("");
        table.deleteColumn(0);
        gen1.delete();
        gen2.delete();
        table.initFromXml(element);
        assertEquals("blabla", table.getDescription());
        assertEquals("RateTableStructure", table.getTableStructure());
        assertEquals(1, table.getNumOfColumns());
        assertEquals(2, table.getNumOfGenerations());
        ITableContentsGeneration gen = (ITableContentsGeneration)table.getGenerations()[0];
        assertEquals(1, gen.getRows().length);
        row = gen.getRows()[0];
        assertEquals("value", row.getValue(0));
        
    }
}
