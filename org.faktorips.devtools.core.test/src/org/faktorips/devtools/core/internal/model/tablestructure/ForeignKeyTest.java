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

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.w3c.dom.Element;

public class ForeignKeyTest extends AbstractIpsPluginTest {

    private IIpsSrcFile ipsSrcFile;
    private TableStructure table;
    private ForeignKey key;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        table = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
        ipsSrcFile = table.getIpsSrcFile();
        key = (ForeignKey)table.newForeignKey();
        ipsSrcFile.save(true, null);
    }

    public void testRemove() {
        key.delete();
        assertEquals(0, table.getNumOfForeignKeys());
        assertTrue(ipsSrcFile.isDirty());
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
        String[] items = new String[] { "age", "gender" };
        key.setKeyItems(items);
        assertNotSame(items, key.getKeyItemNames()); // defensive copy should be made
        assertEquals(2, key.getKeyItemNames().length);
        assertEquals("age", key.getKeyItemNames()[0]);
        assertEquals("gender", key.getKeyItemNames()[1]);
    }

    public void testSetKeyItems() {
        String[] items = new String[] { "age", "gender" };
        key.setKeyItems(items);
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testToXml() {
        key = (ForeignKey)table.newForeignKey();
        key.setReferencedTableStructure("RefTable");
        key.setReferencedUniqueKey("key");
        String[] items = new String[] { "age", "gender" };
        key.setKeyItems(items);
        Element element = key.toXml(newDocument());

        ForeignKey copy = new ForeignKey();
        copy.initFromXml(element);
        assertEquals(key.getId(), copy.getId());
        assertEquals("RefTable", copy.getReferencedTableStructure());
        assertEquals("key", copy.getReferencedUniqueKey());
        assertEquals(2, copy.getNumOfKeyItems());
        assertEquals("age", copy.getKeyItemNames()[0]);
        assertEquals("gender", copy.getKeyItemNames()[1]);
    }

    public void testInitFromXml() {
        key.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("42", key.getId());
        assertEquals("RefTable", key.getReferencedTableStructure());
        assertEquals("key", key.getReferencedUniqueKey());
        assertEquals(2, key.getNumOfKeyItems());
        assertEquals("age", key.getKeyItemNames()[0]);
        assertEquals("gender", key.getKeyItemNames()[1]);
    }

    /**
     * Tests for the correct type of excetion to be thrown - no part of any type could ever be
     * created.
     */
    public void testNewPart() {
        try {
            key.newPart(IPolicyCmptTypeAttribute.class);
            fail();
        } catch (IllegalArgumentException e) {
            // nothing to do :-)
        }
    }
}
