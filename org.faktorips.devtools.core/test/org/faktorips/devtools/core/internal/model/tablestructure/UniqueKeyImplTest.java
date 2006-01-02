package org.faktorips.devtools.core.internal.model.tablestructure;

import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.w3c.dom.Element;


/**
 *
 */
public class UniqueKeyImplTest extends IpsObjectTestCase {

    private TableStructure table;
    private UniqueKey key;
    
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.TABLE_STRUCTURE);
    }
    
    protected void createObjectAndPart() {
        table = new TableStructure(pdSrcFile);
        key = (UniqueKey)table.newUniqueKey();
    }
    
    public void testRemove() {
        key.delete();
        assertEquals(0, table.getNumOfUniqueKeys());
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testGetName() {
        assertEquals("", key.getName());
        key.setKeyItems(new String[] {"age"});
        assertEquals("age", key.getName());
        key.setKeyItems(new String[] {"age", "gender"});
        assertEquals("age, gender", key.getName());
    }

    public void testGetKeyItemNames() {
        assertEquals(0, key.getKeyItemNames().length);
        String[] items = new String[] {"age", "gender"};
        key.setKeyItems(items);
        assertNotSame(items, key.getKeyItemNames()); // defensive copy should be made
        assertEquals(2, key.getKeyItemNames().length);
        assertEquals("age", key.getKeyItemNames()[0]);
        assertEquals("gender", key.getKeyItemNames()[1]);
    }

    public void testGetKeyItems() {
        assertEquals(0, key.getKeyItems().length);
        IColumn c0 = table.newColumn();
        c0.setName("c0");
        IColumn c1 = table.newColumn();
        c1.setName("c1");
        IColumnRange range = table.newRange();
        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_FROM);
        
        key.setKeyItems(new String[] {"c0", range.getName(), "unknown"});
        IKeyItem[] keyItems = key.getKeyItems();
        assertEquals(2, keyItems.length);
        assertEquals(c0, keyItems[0]);
        assertEquals(range, keyItems[1]);
    }

    public void testSetKeyItems() {
        String[] items = new String[] {"age", "gender"};
        key.setKeyItems(items);
        assertTrue(pdSrcFile.isDirty());
    }

    public void testGetItemCandidates() {
        assertEquals(0, key.getItemCandidates().length);
        IColumn columnGender = table.newColumn();
        columnGender.setName("gender");
        IColumn columnSmoker = table.newColumn();
        columnSmoker.setName("smoker");
        IColumnRange range = table.newRange();
        range.setFromColumn("ageFrom");
        range.setToColumn("ageTo");
        
        assertEquals(3, key.getItemCandidates().length);
        assertEquals(columnGender, key.getItemCandidates()[0]);
        assertEquals(columnSmoker, key.getItemCandidates()[1]);
        assertEquals(range, key.getItemCandidates()[2]);
        
        String[] items = new String[] {"gender", range.getName()};
        key.setKeyItems(items);
        assertEquals(1, key.getItemCandidates().length);
        assertEquals(columnSmoker, key.getItemCandidates()[0]);
    }

    public void testToXml() {
        key = (UniqueKey)table.newUniqueKey();
        String[] items = new String[] {"age", "gender"};
        key.setKeyItems(items);
        Element element = key.toXml(newDocument());
        UniqueKey copy = new UniqueKey();
        copy.initFromXml(element);
        assertEquals(1, copy.getId());
        assertEquals(2, copy.getNumOfKeyItems());
        assertEquals("age", copy.getKeyItemNames()[0]);
        assertEquals("gender", copy.getKeyItemNames()[1]);
    }

    public void testInitFromXml() {
        key.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(42, key.getId());
        assertEquals(2, key.getNumOfKeyItems());
        assertEquals("age", key.getKeyItemNames()[0]);
        assertEquals("gender", key.getKeyItemNames()[1]);
    }

}
