package org.faktorips.devtools.core.internal.model.tablecontents;

import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.w3c.dom.Element;


/**
 *
 */
public class TableContentsGenerationImplTest extends IpsObjectTestCase {

    private ITableContents table; 
    private TableContentsGeneration generation;
    
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.TABLE_STRUCTURE);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.internal.model.IpsObjectTestCase#createObjectAndPart()
     */
    protected void createObjectAndPart() {
        table = new TableContents(pdSrcFile);
        generation = (TableContentsGeneration)table.newGeneration();
        table.newColumn(null);
        table.newColumn(null);
        table.newColumn(null);
    }
    
    public void testGetChildren() {
        table.newColumn(null);
        table.newColumn(null);
        IRow row0 = generation.newRow();
        IRow row1 = generation.newRow();
        IIpsElement[] children = generation.getChildren();
        assertEquals(2, children.length);
        assertSame(row0, children[0]);
        assertSame(row1, children[1]);
    }
    
    public void testNewRow() {
        table.newColumn(null);
        table.newColumn(null);
        IRow row0 = generation.newRow();
        assertEquals(0, row0.getId());
        assertEquals("", row0.getValue(0));
        assertEquals("", row0.getValue(1));
        
        IRow row1 = generation.newRow();
        assertEquals(1, row1.getId());
    }
    
    public void testNewColumn() {
        IRow row1 = generation.newRow();
        IRow row2 = generation.newRow();
        generation.newColumn("a");
        assertEquals("a", row1.getValue(3));
        assertEquals("a", row2.getValue(3));
    }
    
    public void testRemoveColumn() {
        IRow row1 = generation.newRow();
        IRow row2 = generation.newRow();
        row1.setValue(0, "row1,col1");
        row1.setValue(1, "row1,col2");
        row1.setValue(2, "row1,col3");
        row2.setValue(0, "row2,col1");
        row2.setValue(1, "row2,col2");
        row2.setValue(2, "row2,col3");
        generation.removeColumn(1);
        assertEquals("row1,col1", row1.getValue(0));
        assertEquals("row1,col3", row1.getValue(1));
        try {
            row1.getValue(2);
            fail();
        } catch (Exception e) {}
        assertEquals("row2,col1", row2.getValue(0));
        assertEquals("row2,col3", row2.getValue(1));
        try {
            row2.getValue(2);
            fail();
        } catch (Exception e) {}
        
    }

    public void testToXml() {
        IRow row1 = generation.newRow();
        IRow row2 = generation.newRow();
        Element element = generation.toXml(newDocument());
        row1.delete();
        row2.delete();
        generation.initFromXml(element);
        assertEquals(2, generation.getNumOfRows());
    }

    public void testInitFromXml() {
        generation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(2, generation.getNumOfRows());
    }

    public void testNewPart() {
    	try {
    		assertTrue(generation.newPart(IRow.class) instanceof IRow);
    		
    		generation.newPart(Object.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
}
