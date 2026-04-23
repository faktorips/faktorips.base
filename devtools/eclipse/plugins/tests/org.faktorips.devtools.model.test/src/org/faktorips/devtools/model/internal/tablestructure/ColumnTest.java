/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablestructure;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class ColumnTest extends AbstractIpsPluginTest {

    private IIpsSrcFile ipsSrcFile;
    private TableStructure table;
    private IColumn column;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        table = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
        ipsSrcFile = table.getIpsSrcFile();
        column = table.newColumn();
        ipsSrcFile.save(null);
    }

    @Test
    public void testSetName() {
        column.setName("newName");
        assertThat(column.getName(), is("newName"));
        assertThat(ipsSrcFile.isDirty(), is(true));
    }

    @Test
    public void testSetDatatype() {
        column.setDatatype("newType");
        assertThat(column.getDatatype(), is("newType"));
        assertThat(ipsSrcFile.isDirty(), is(true));
    }

    @Test
    public void testRemove() {
        IColumn c0 = column;
        IColumn c1 = table.newColumn();
        IColumn c2 = table.newColumn();

        assertThat(table.getColumns()[0], is(sameInstance(c0)));
        assertThat(table.getColumns()[1], is(sameInstance(c1)));
        assertThat(table.getColumns()[2], is(sameInstance(c2)));

        c1.delete();
        assertThat(table.getNumOfColumns(), is(2));
        assertThat(table.getColumns()[0], is(sameInstance(c0)));
        assertThat(table.getColumns()[1], is(sameInstance(c2)));
        assertThat(ipsSrcFile.isDirty(), is(true));
    }

    @Test
    public void testToXml() {
        column = table.newColumn();
        column.setName("premium");
        column.setDatatype("Money");
        Element element = column.toXml(newDocument());

        assertThat(element.getAttribute(IIpsObjectPart.PROPERTY_ID), is(column.getId()));
        assertThat(element.getAttribute(IIpsElement.PROPERTY_NAME), is("premium"));
        assertThat(element.getAttribute(IColumn.PROPERTY_DATATYPE), is("Money"));
    }

    @Test
    public void testInitFromXml() {
        column.initFromXml(getTestDocument().getDocumentElement());
        assertThat(column.getId(), is("42"));
        assertThat(column.getName(), is("premium"));
        assertThat(column.getDatatype(), is("Money"));
    }

    @Test
    public void testValidateName() throws Exception {
        column.setName("Boolean");
        column.setDatatype(Datatype.STRING.getQualifiedName());
        MessageList ml = column.validate(ipsSrcFile.getIpsProject());
        assertThat(ml, hasMessageCode(IColumn.MSGCODE_INVALID_NAME));

        column.setName("integer");
        ml = column.validate(ipsSrcFile.getIpsProject());
        assertThat(ml, lacksMessageCode(IColumn.MSGCODE_INVALID_NAME));
    }

    @Test
    public void testValidateName_EmptyName() throws Exception {
        column.setName("");
        column.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList ml = column.validate(ipsSrcFile.getIpsProject());
        assertThat(ml, lacksMessageCode(IColumn.MSGCODE_INVALID_NAME));
    }

    @Test
    public void testValidateDuplicateName() throws Exception {
        column.setName("premium");
        column.setDatatype(Datatype.STRING.getQualifiedName());

        IColumn column2 = table.newColumn();
        column2.setName("premium");
        column2.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList ml = column.validate(ipsSrcFile.getIpsProject());
        assertThat(ml, hasMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));

        MessageList ml2 = column2.validate(ipsSrcFile.getIpsProject());
        assertThat(ml2, hasMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));
    }

    @Test
    public void testValidateDuplicateName_InvalidDatatype() throws Exception {
        column.setName("premium");
        column.setDatatype("InvalidType");

        IColumn column2 = table.newColumn();
        column2.setName("premium");
        column2.setDatatype("InvalidType");

        MessageList ml = column.validate(ipsSrcFile.getIpsProject());
        assertThat(ml, hasMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));

        MessageList ml2 = column2.validate(ipsSrcFile.getIpsProject());
        assertThat(ml2, hasMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));
    }

    @Test
    public void testValidateDuplicateName_EmptyName() throws Exception {
        column.setName("");
        column.setDatatype(Datatype.STRING.getQualifiedName());

        IColumn column2 = table.newColumn();
        column2.setName("");
        column2.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList ml = column.validate(ipsSrcFile.getIpsProject());
        assertThat(ml, lacksMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));
    }

    @Test
    public void testValidateDuplicateName_UniqueNames() throws Exception {
        column.setName("premium");
        column.setDatatype(Datatype.STRING.getQualifiedName());

        IColumn column2 = table.newColumn();
        column2.setName("deductible");
        column2.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList ml = column.validate(ipsSrcFile.getIpsProject());
        assertThat(ml, lacksMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));

        MessageList ml2 = column2.validate(ipsSrcFile.getIpsProject());
        assertThat(ml2, lacksMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));
    }

    @Test
    public void testInitFromXml_DuplicateColumnNames() throws Exception {
        column.setName("premium");
        column.setDatatype(Datatype.STRING.getQualifiedName());

        IColumn column2 = table.newColumn();
        column2.setName("premium");
        column2.setDatatype(Datatype.STRING.getQualifiedName());

        Document doc = XmlUtil.getDefaultDocumentBuilder().newDocument();
        Element xml = table.toXml(doc);

        table.initFromXml(xml);

        assertThat(table.getNumOfColumns(), is(2));
        assertThat(table.getColumns()[0].getName(), is("premium"));
        assertThat(table.getColumns()[1].getName(), is("premium"));
    }

    @Test
    public void testValidateDuplicateName_ThreeColumnsWithSameName() throws Exception {
        column.setName("premium");
        column.setDatatype(Datatype.STRING.getQualifiedName());

        IColumn column2 = table.newColumn();
        column2.setName("premium");
        column2.setDatatype(Datatype.STRING.getQualifiedName());

        IColumn column3 = table.newColumn();
        column3.setName("premium");
        column3.setDatatype(Datatype.STRING.getQualifiedName());

        assertThat(column.validate(ipsSrcFile.getIpsProject()), hasMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));
        assertThat(column2.validate(ipsSrcFile.getIpsProject()), hasMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));
        assertThat(column3.validate(ipsSrcFile.getIpsProject()), hasMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));
    }

    @Test
    public void testValidateDuplicateName_CaseSensitive() throws Exception {
        column.setName("Premium");
        column.setDatatype(Datatype.STRING.getQualifiedName());

        IColumn column2 = table.newColumn();
        column2.setName("premium");
        column2.setDatatype(Datatype.STRING.getQualifiedName());

        assertThat(column.validate(ipsSrcFile.getIpsProject()), lacksMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));
        assertThat(column2.validate(ipsSrcFile.getIpsProject()), lacksMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));
    }

    @Test
    public void testValidateDuplicateName_MessageText() throws Exception {
        column.setName("premium");
        column.setDatatype(Datatype.STRING.getQualifiedName());

        IColumn column2 = table.newColumn();
        column2.setName("premium");
        column2.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList ml = column.validate(ipsSrcFile.getIpsProject());
        String messageText = ml.getMessageByCode(IColumn.MSGCODE_DUPLICATE_NAME).getText();
        assertThat(messageText, containsString("premium"));
    }

    @Test
    public void testValidateDuplicateName_NullName() throws Exception {
        column.setName(null);
        column.setDatatype(Datatype.STRING.getQualifiedName());

        IColumn column2 = table.newColumn();
        column2.setName(null);
        column2.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList ml = column.validate(ipsSrcFile.getIpsProject());
        assertThat(ml, lacksMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));
    }

    @Test
    public void testValidateDuplicateName_BlankName() throws Exception {
        column.setName("   ");
        column.setDatatype(Datatype.STRING.getQualifiedName());

        IColumn column2 = table.newColumn();
        column2.setName("   ");
        column2.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList ml = column.validate(ipsSrcFile.getIpsProject());
        assertThat(ml, hasMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));

        MessageList ml2 = column2.validate(ipsSrcFile.getIpsProject());
        assertThat(ml2, hasMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));
    }

    @Test
    public void testValidateDuplicateName_SingleColumn() throws Exception {
        column.setName("premium");
        column.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList ml = column.validate(ipsSrcFile.getIpsProject());
        assertThat(ml, lacksMessageCode(IColumn.MSGCODE_DUPLICATE_NAME));
    }

    @Test
    public void testFindValueDatatype() {
        column.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        assertThat(column.findValueDatatype(column.getIpsProject()), is(Datatype.BOOLEAN));

        column.setDatatype("NotADatatype");
        assertThat(column.findValueDatatype(column.getIpsProject()), is(nullValue()));
    }
}
