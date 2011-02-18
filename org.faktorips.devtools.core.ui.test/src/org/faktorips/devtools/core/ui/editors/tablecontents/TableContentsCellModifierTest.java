/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;

public class TableContentsCellModifierTest extends AbstractIpsPluginTest {

    private IRow rowValid;
    private IRow rowInvalid;
    private IRow rowNull;
    private TableContentsCellModifier cellModifier;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject proj = newIpsProject("TableContentsLabelProviderProject");
        IIpsPackageFragmentRoot root = proj.getIpsPackageFragmentRoots()[0];

        ITableStructure structure = (ITableStructure)newIpsObject(root, IpsObjectType.TABLE_STRUCTURE,
                "TestTableStructure");
        IColumn column0 = structure.newColumn();
        column0.setDatatype("Integer");
        IColumn column1 = structure.newColumn();
        column1.setDatatype("Integer");
        IColumn column2 = structure.newColumn();
        column2.setDatatype("Integer");

        ITableContents tableContents = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS,
                "TestTableContents");
        tableContents.setTableStructure(structure.getQualifiedName());
        ITableContentsGeneration gen = (ITableContentsGeneration)tableContents.newGeneration();
        rowValid = gen.newRow();
        rowInvalid = gen.newRow();
        rowNull = gen.newRow();

        tableContents.newColumn("1");
        tableContents.newColumn("2");
        tableContents.newColumn("3");

        rowValid.setValue(0, "1");
        rowValid.setValue(1, "2");
        rowValid.setValue(2, "3");
        rowInvalid.setValue(0, "A");
        rowInvalid.setValue(1, "B");
        rowInvalid.setValue(2, "C");
        rowNull.setValue(0, null);
        rowNull.setValue(1, null);
        rowNull.setValue(2, null);

        TableViewer tableViewer = new TableViewer(new Shell());
        tableViewer.setColumnProperties(new String[] { "ColumnA", "ColumnB", "ColumnC" });
        cellModifier = new TableContentsCellModifier(tableViewer, null);
    }

    @Test
    public void testGetValue() {
        assertEquals("1", cellModifier.getValue(rowValid, "ColumnA"));
        assertEquals("2", cellModifier.getValue(rowValid, "ColumnB"));
        assertEquals("3", cellModifier.getValue(rowValid, "ColumnC"));
        assertEquals("A", cellModifier.getValue(rowInvalid, "ColumnA"));
        assertEquals("B", cellModifier.getValue(rowInvalid, "ColumnB"));
        assertEquals("C", cellModifier.getValue(rowInvalid, "ColumnC"));
        assertNull(cellModifier.getValue(rowNull, "ColumnA"));
        assertNull(cellModifier.getValue(rowNull, "ColumnB"));
        assertNull(cellModifier.getValue(rowNull, "ColumnC"));
    }

    @Test
    public void testModify() {
        cellModifier.modify(rowValid, "ColumnA", "X");
        cellModifier.modify(rowValid, "ColumnB", "Y");
        cellModifier.modify(rowValid, "ColumnC", "Z");
        assertEquals("X", rowValid.getValue(0));
        assertEquals("Y", rowValid.getValue(1));
        assertEquals("Z", rowValid.getValue(2));
        cellModifier.modify(rowInvalid, "ColumnA", null);
        cellModifier.modify(rowInvalid, "ColumnB", null);
        cellModifier.modify(rowInvalid, "ColumnC", null);
        assertNull(rowInvalid.getValue(0));
        assertNull(rowInvalid.getValue(1));
        assertNull(rowInvalid.getValue(2));
    }
}
