package org.faktorips.devtools.core.internal.model.tablestructure;

import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.w3c.dom.Element;


/**
 *
 */
public class ForeignKeyImplTest extends IpsObjectTestCase {

    private TableStructure table;
    private ForeignKey key;
    
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.TABLE_STRUCTURE);
    }
    
    protected void createObjectAndPart() {
        table = new TableStructure(pdSrcFile);
        key = (ForeignKey)table.newForeignKey();
    }
    
    public void testRemove() {
        key.delete();
        assertEquals(0, table.getNumOfForeignKeys());
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testGetName() {
        assertEquals("()", key.getName());
        key.setReferencedTableStructure("RefTable");
        assertEquals("RefTable()", key.getName());
        
        key.setReferencedUniqueKey("age");
        assertEquals("RefTable(age)", key.getName());
    }

    public void testGetKeyItems() {
        assertEquals(0, key.getKeyItemNames().length);
        String[] items = new String[] {"age", "gender"};
        key.setKeyItems(items);
        assertNotSame(items, key.getKeyItemNames()); // defensive copy should be made
        assertEquals(2, key.getKeyItemNames().length);
        assertEquals("age", key.getKeyItemNames()[0]);
        assertEquals("gender", key.getKeyItemNames()[1]);
    }

    public void testSetKeyItems() {
        String[] items = new String[] {"age", "gender"};
        key.setKeyItems(items);
        assertTrue(pdSrcFile.isDirty());
    }

    public void testToXml() {
        key = (ForeignKey)table.newForeignKey();
        key.setReferencedTableStructure("RefTable");
        key.setReferencedUniqueKey("key");
        String[] items = new String[] {"age", "gender"};
        key.setKeyItems(items);
        Element element = key.toXml(newDocument());
        
        ForeignKey copy = new ForeignKey();
        copy.initFromXml(element);
        assertEquals(1, copy.getId());
        assertEquals("RefTable", copy.getReferencedTableStructure());
        assertEquals("key", copy.getReferencedUniqueKey());
        assertEquals(2, copy.getNumOfKeyItems());
        assertEquals("age", copy.getKeyItemNames()[0]);
        assertEquals("gender", copy.getKeyItemNames()[1]);
    }

    public void testInitFromXml() {
        key.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(42, key.getId());
        assertEquals("RefTable", key.getReferencedTableStructure());
        assertEquals("key", key.getReferencedUniqueKey());
        assertEquals(2, key.getNumOfKeyItems());
        assertEquals("age", key.getKeyItemNames()[0]);
        assertEquals("gender", key.getKeyItemNames()[1]);
    }

    /**
     * Tests for the correct type of excetion to be thrown - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			key.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
}
