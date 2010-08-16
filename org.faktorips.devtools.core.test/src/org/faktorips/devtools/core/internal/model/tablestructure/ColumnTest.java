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

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 *
 */
public class ColumnTest extends AbstractIpsPluginTest {

    private IIpsSrcFile ipsSrcFile;
    private TableStructure table;
    private IColumn column;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        table = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
        ipsSrcFile = table.getIpsSrcFile();
        column = table.newColumn();
        ipsSrcFile.save(true, null);
    }

    public void testSetName() {
        column.setName("newName");
        assertEquals("newName", column.getName());
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testSetDatatype() {
        column.setDatatype("newType");
        assertEquals("newType", column.getDatatype());
        assertTrue(ipsSrcFile.isDirty());
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
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testToXml() {
        column = table.newColumn();
        column.setName("premium");
        column.setDatatype("Money");
        Element element = column.toXml(newDocument());

        assertEquals(column.getId(), element.getAttribute(IIpsObjectPart.PROPERTY_ID));
        assertEquals("premium", element.getAttribute(IIpsElement.PROPERTY_NAME));
        assertEquals("Money", element.getAttribute(IColumn.PROPERTY_DATATYPE));
    }

    public void testInitFromXml() {
        column.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("42", column.getId());
        assertEquals("premium", column.getName());
        assertEquals("Money", column.getDatatype());
    }

    public void testValidateName() throws Exception {
        column.setName("Boolean");
        column.setDatatype(Datatype.STRING.getQualifiedName());
        MessageList ml = column.validate(ipsSrcFile.getIpsProject());
        assertNotNull(ml.getMessageByCode(IColumn.MSGCODE_INVALID_NAME));

        column.setName("integer");
        ml = column.validate(ipsSrcFile.getIpsProject());
        assertNull(ml.getMessageByCode(IColumn.MSGCODE_INVALID_NAME));
    }

    public void testFindValueDatatype() throws CoreException {
        column.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        assertEquals(Datatype.BOOLEAN, column.findValueDatatype(column.getIpsProject()));

        column.setDatatype("NotADatatype");
        assertNull(column.findValueDatatype(column.getIpsProject()));
    }
}
