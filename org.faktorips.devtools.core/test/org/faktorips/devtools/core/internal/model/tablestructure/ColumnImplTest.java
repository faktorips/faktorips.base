/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.w3c.dom.Element;


/**
 *
 */
public class ColumnImplTest extends IpsObjectTestCase {

    private TableStructure table;
    private IColumn column;
    
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.TABLE_STRUCTURE);
    }
    
    protected void createObjectAndPart() {
        table = new TableStructure(pdSrcFile);
        column = table.newColumn();
    }

    public void testSetName() {
        column.setName("newName");
        assertEquals("newName", column.getName());
        assertTrue(pdSrcFile.isDirty());
    }

    public void testSetDatatype() {
        column.setDatatype("newType");
        assertEquals("newType", column.getDatatype());
        assertTrue(pdSrcFile.isDirty());
    }

    public void testRemove() {
        IColumn c0 = column;
        IColumn c1 = table.newColumn();
        IColumn c2 = table.newColumn();
        
        assertSame(c0, table.getColumns()[0]);
        assertSame(c1, table.getColumns()[1]);
        assertSame(c2, table.getColumns()[2]);
        
        c1.delete();
        assertEquals(2, table.getNumOfColumns());
        assertEquals(c0, table.getColumns()[0]);
        assertEquals(c2, table.getColumns()[1]);
        assertTrue(pdSrcFile.isDirty());
    }

    public void testToXml() {
        column = table.newColumn();
        column.setName("premium");
        column.setDatatype("Money");
        Element element = column.toXml(newDocument());
        
        assertEquals("1", element.getAttribute(IColumn.PROPERTY_ID));
        assertEquals("premium", element.getAttribute(IColumn.PROPERTY_NAME));
        assertEquals("Money", element.getAttribute(IColumn.PROPERTY_DATATYPE));
    }

    public void testInitFromXml() {
        column.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(42, column.getId());
        assertEquals("premium", column.getName());
        assertEquals("Money", column.getDatatype());
    }

    /**
     * Tests for the correct type of excetion to be thrown - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			column.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
}
