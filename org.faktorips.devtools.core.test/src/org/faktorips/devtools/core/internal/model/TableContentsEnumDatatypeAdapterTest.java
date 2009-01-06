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

package org.faktorips.devtools.core.internal.model;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;

public class TableContentsEnumDatatypeAdapterTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITableStructure structure;
    private ITableContents contents;
    private ITableContentsGeneration contentsGeneration;
    private TableContentsEnumDatatypeAdapter adapter;
    
    public void setUp() throws Exception{
        super.setUp();
        project = newIpsProject();
        
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "EnumTable");
        structure.setTableStructureType(TableStructureType.ENUMTYPE_MODEL);
        IColumn idColumn = structure.newColumn();
        idColumn.setDatatype(Datatype.INTEGER.getQualifiedName());
        idColumn.setName("id");
        IColumn nameColumn = structure.newColumn();
        nameColumn.setDatatype(Datatype.STRING.getQualifiedName());
        nameColumn.setName("name");
        IUniqueKey idKey = structure.newUniqueKey();
        idKey.setKeyItems(new String[]{idColumn.getName()});
        IUniqueKey nameKey = structure.newUniqueKey();
        nameKey.setKeyItems(new String[]{nameColumn.getName()});
        
        contents = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "PaymentMode");
        contents.setTableStructure(structure.getQualifiedName());
        contents.newColumn("0");
        contents.newColumn("");
        contentsGeneration = (ITableContentsGeneration)contents.newGeneration();
        contentsGeneration.setValidFrom(new GregorianCalendar(2007, Calendar.JANUARY, 1));
        IRow row = contentsGeneration.newRow();
        row.setValue(0, "1");
        row.setValue(1, "annually");
        row = contentsGeneration.newRow();
        row.setValue(0, "2");
        row.setValue(1, "semiannually");
        row = contentsGeneration.newRow();
        row.setValue(0, "3");
        row.setValue(1, "quarterly");
        row = contentsGeneration.newRow();
        row.setValue(0, "4");
        row.setValue(1, "monthly");
        
        adapter = new TableContentsEnumDatatypeAdapter(contents, project);
    }

    public final void testGetAllValueIds() {
        String[] ids = adapter.getAllValueIds(false);
        assertEquals(4, ids.length);
        List idList = Arrays.asList(ids);
        assertTrue(idList.contains("1"));
        assertTrue(idList.contains("2"));
        assertTrue(idList.contains("3"));
        assertTrue(idList.contains("4"));
    }

    public final void testGetValueName() {
        assertEquals("annually", adapter.getValueName("1"));
        assertEquals("semiannually", adapter.getValueName("2"));
        assertEquals("quarterly", adapter.getValueName("3"));
        assertEquals("monthly", adapter.getValueName("4"));
    }

    public final void testAreValuesEqual() {
        assertTrue(adapter.areValuesEqual("1", "1"));
        assertFalse(adapter.areValuesEqual("1", "2"));
    }

    public final void testIsParsable() {
        assertTrue(adapter.isParsable("1"));
        assertFalse(adapter.isParsable("hello"));
    }

    public final void testGetJavaClassName() {
        assertEquals("org.faktorips.sample.model.PaymentMode", adapter.getJavaClassName());
    }

    public final void testGetQualifiedName() {
        assertEquals("PaymentMode", adapter.getQualifiedName());
    }

}
