/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablecontents;

import static org.junit.Assert.assertEquals;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IPartReference;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class TableColumnReferenceTest extends AbstractIpsPluginTest {
    private IIpsProject project;
    private ITableStructure structure;
    private ITableContents table;
    private IPartReference genderIdReference;
    private IPartReference genderNameReference;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table = newTableContents(structure, "Tc");
        IColumn column1 = structure.newColumn();
        column1.setName("Id");
        IColumn column2 = structure.newColumn();
        column2.setName("Name");
        table.newColumn("a", "Id");
        table.newColumn("b", "Name");
        genderIdReference = table.getColumnReferences().get(0);
        genderNameReference = table.getColumnReferences().get(1);
    }

    @Test
    public void testGetSetName() {
        assertEquals("Id", genderIdReference.getName());
        genderIdReference.setName("foo");
        assertEquals("foo", genderIdReference.getName());
    }

    @Test
    public void testXml() throws ParserConfigurationException {
        Element xmlElement = genderIdReference.toXml(createXmlDocument(TableColumnReference.XML_TAG));
        NamedNodeMap attributes = xmlElement.getAttributes();
        assertEquals("Id", attributes.getNamedItem(IIpsElement.PROPERTY_NAME).getTextContent());

        genderNameReference.initFromXml(xmlElement);
        assertEquals(genderIdReference.getName(), genderNameReference.getName());
    }

}
