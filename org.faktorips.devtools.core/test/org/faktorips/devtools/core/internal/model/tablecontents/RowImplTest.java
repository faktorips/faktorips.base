package org.faktorips.devtools.core.internal.model.tablecontents;

import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.w3c.dom.Element;


/**
 *
 */
public class RowImplTest extends IpsObjectTestCase {

    private ITableContents table; 
    private ITableContentsGeneration generation;
    private Row row;
    
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.TABLE_STRUCTURE);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.internal.model.IpsObjectTestCase#createObjectAndPart()
     */
    protected void createObjectAndPart() {
        table = new TableContents(pdSrcFile);
        generation = (ITableContentsGeneration)table.newGeneration();
        table.newColumn(null);
        table.newColumn(null);
        table.newColumn(null);
        row = (Row)generation.newRow();
    }

    public void testSetValue() {
        row.setValue(0, "newValue0");
        assertEquals("newValue0", row.getValue(0));
        row.setValue(1, "newValue1");
        assertEquals("newValue1", row.getValue(1));
        assertTrue(pdSrcFile.isDirty());
        
        try {
            row.setValue(4, "newValue2");
            fail();
        } catch (RuntimeException e) {
        }
    }

    public void testRemove() {
        row.delete();
        assertEquals(0, generation.getNumOfRows());
        assertTrue(pdSrcFile.isDirty());
    }

    public void testToXml() {
        row.setValue(0, "value0");
        row.setValue(1, "");
        row.setValue(2, null);
        
        Element element = row.toXml(newDocument());
        row.setValue(0, null);
        row.setValue(1, null);
        row.setValue(2, "");
        row.initFromXml(element);
        assertEquals("value0", row.getValue(0));
        assertEquals("", row.getValue(1));
        assertNull(row.getValue(2));
    }

    public void testInitFromXml() {
        row.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(42, row.getId());
        assertEquals("0.15", row.getValue(0));
        assertEquals("", row.getValue(1));
        assertNull(row.getValue(2));
    }

}
