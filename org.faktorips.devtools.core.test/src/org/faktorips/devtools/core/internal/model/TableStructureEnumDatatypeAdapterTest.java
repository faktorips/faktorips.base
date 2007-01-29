/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.GregorianCalendar;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;

/**
 * 
 * @author Jan Ortmann
 */
public class TableStructureEnumDatatypeAdapterTest extends AbstractIpsPluginTest {

	private IIpsProject ipsProject;
    
    ITableStructure structure;
    TableStructureEnumDatatypeAdapter adapter;
	
	protected void setUp() throws Exception {
		super.setUp();
		ipsProject = newIpsProject("TestProject");
        structure = (ITableStructure)this.newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE, "tables.Structure");
        structure.setTableStructureType(TableStructureType.ENUMTYPE_MODEL);
        IColumn col1 = structure.newColumn();
        col1.setName("Col1");
        col1.setDatatype(Datatype.MONEY.getQualifiedName());
        
        IColumn col2 = structure.newColumn();
        col2.setName("Col2");
        col2.setDatatype(Datatype.STRING.getQualifiedName());
        
        IUniqueKey key = structure.newUniqueKey();
        key.addKeyItem("Col1");
        
        IUniqueKey anotherKey = structure.newUniqueKey();
        anotherKey.addKeyItem("Col1");
        anotherKey.addKeyItem("Col2");
        structure.getIpsSrcFile().save(true, null);
        
        ITableContents content = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "tables.Content");
        content.setTableStructure(structure.getQualifiedName());
        GregorianCalendar validFrom = (GregorianCalendar)GregorianCalendar.getInstance();
        validFrom.set(2000, 1, 1);
        ITableContentsGeneration generation = (ITableContentsGeneration)content.newGeneration(validFrom);
        content.newColumn("");
        content.newColumn("");
        IRow row1 = generation.newRow();
        row1.setValue(0, "10EUR");
        row1.setValue(1, "Eins");
        
        IRow row2 = generation.newRow();
        row2.setValue(0, "20 EUR");
        row2.setValue(1, "Zwei");
        
        adapter = new TableStructureEnumDatatypeAdapter(structure, ipsProject);
	}

    public void testGetAllValueIds() {
        String[] values = adapter.getAllValueIds(true);
        
        assertEquals(3, values.length);
        assertEquals("10EUR", values[0]);
        assertEquals("20 EUR", values[1]);
        assertEquals(null, values[2]);
        
        values = adapter.getAllValueIds(false);
        assertEquals(2, values.length);
        assertEquals("10EUR", values[0]);
        assertEquals("20 EUR", values[1]);

        structure.setTableStructureType(TableStructureType.ENUMTYPE_PRODUCTDEFINTION);
        values = adapter.getAllValueIds(false);
        assertEquals(0, values.length);
    }
    
    public void testIsParsable() {
        assertTrue(adapter.isParsable("10EUR"));
        assertTrue(adapter.isParsable("10 EUR"));
        assertFalse(adapter.isParsable("30EUR"));
        assertTrue(adapter.isParsable(null));
    }
    
}
