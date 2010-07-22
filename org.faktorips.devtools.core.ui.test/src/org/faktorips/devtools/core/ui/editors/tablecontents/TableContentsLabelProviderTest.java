/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

public class TableContentsLabelProviderTest extends AbstractIpsPluginTest {
    private IRow rowValid;
    private IRow rowInvalid;
    private IRow rowNull;
    private TableContentsLabelProvider labelProvider;
    private String nullPresentation = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();

    @Override
    protected void setUp() throws Exception {
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

        labelProvider = new TableContentsLabelProvider();
        labelProvider
                .setValueDatatypes(new ValueDatatype[] { column0.findValueDatatype(column0.getIpsProject()),
                        column1.findValueDatatype(column1.getIpsProject()),
                        column2.findValueDatatype(column2.getIpsProject()) });
    }

    public void testGetColumnImage() {
        assertNull(labelProvider.getColumnImage(rowValid, 0));
        assertNull(labelProvider.getColumnImage(rowValid, 1));
        assertNull(labelProvider.getColumnImage(rowValid, 2));
        assertNotNull(labelProvider.getColumnImage(rowInvalid, 0));
        assertNotNull(labelProvider.getColumnImage(rowInvalid, 1));
        assertNotNull(labelProvider.getColumnImage(rowInvalid, 2));
        assertNull(labelProvider.getColumnImage(rowNull, 0));
        assertNull(labelProvider.getColumnImage(rowNull, 1));
        assertNull(labelProvider.getColumnImage(rowNull, 2));
    }

    public void testGetColumnText() {
        assertEquals("1", labelProvider.getColumnText(rowValid, 0));
        assertEquals("2", labelProvider.getColumnText(rowValid, 1));
        assertEquals("3", labelProvider.getColumnText(rowValid, 2));
        assertEquals("A", labelProvider.getColumnText(rowInvalid, 0));
        assertEquals("B", labelProvider.getColumnText(rowInvalid, 1));
        assertEquals("C", labelProvider.getColumnText(rowInvalid, 2));
        assertEquals(nullPresentation, labelProvider.getColumnText(rowNull, 0));
        assertEquals(nullPresentation, labelProvider.getColumnText(rowNull, 1));
        assertEquals(nullPresentation, labelProvider.getColumnText(rowNull, 2));
    }
}
