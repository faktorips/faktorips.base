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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

public class TableContentsContentProviderTest extends AbstractIpsPluginTest {

    private IRow rowValid;
    private IRow rowInvalid;
    private IRow rowNull;

    private TableContentsContentProvider contentProvider = new TableContentsContentProvider();
    private ITableContents tableContents;

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

        tableContents = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "TestTableContents");
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
    }

    public void testGetElements() {
        Object[] elements = contentProvider.getElements(tableContents);
        assertEquals(3, elements.length);
        elements = contentProvider.getElements(rowValid);
        assertEquals(0, elements.length);
        elements = contentProvider.getElements(null);
        assertEquals(0, elements.length);
    }

}
