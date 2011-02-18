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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TableStructureUsageTest extends AbstractIpsPluginTest {
    private IIpsProject project;
    private IProductCmptType pcType;
    private ITableStructureUsage tableStructureUsage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        project = this.newIpsProject();

        pcType = newProductCmptType(project, "test.Product");
        tableStructureUsage = pcType.newTableStructureUsage();
        pcType.getIpsSrcFile().save(true, null);

        newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "test.TableStructure1");
    }

    @Test
    public void testIsUsed() {
        assertFalse(tableStructureUsage.isUsed(null));
        assertFalse(tableStructureUsage.isUsed("MyTable"));

        tableStructureUsage.addTableStructure("RateTable");
        tableStructureUsage.addTableStructure("MyTable");

        assertTrue(tableStructureUsage.isUsed("MyTable"));
        assertFalse(tableStructureUsage.isUsed("SomeOtherTable"));
    }

    @Test
    public void testRemove() {
        tableStructureUsage.delete();
        assertEquals(0, pcType.getTableStructureUsages().length);
        assertTrue(pcType.getIpsSrcFile().isDirty());
    }

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "TableStructureUsage", 0);
        tableStructureUsage.initFromXml(paramEl);
        assertEquals("role1", tableStructureUsage.getRoleName());
        assertTrue(tableStructureUsage.isMandatoryTableContent());
        assertEquals(3, tableStructureUsage.getTableStructures().length);
        for (int i = 0; i < 3; i++) {
            assertEquals("tableStructure" + (i + 1), tableStructureUsage.getTableStructures()[i]);
        }
        paramEl = XmlUtil.getElement(docEl, "TableStructureUsage", 1);
        tableStructureUsage.initFromXml(paramEl);
        assertFalse(tableStructureUsage.isMandatoryTableContent());
    }

    @Test
    public void testToXml() {
        tableStructureUsage.setRoleName("roleA");
        tableStructureUsage.setMandatoryTableContent(true);
        tableStructureUsage.addTableStructure("tableStructureA");
        tableStructureUsage.addTableStructure("tableStructureB");
        Element element = tableStructureUsage.toXml(this.newDocument());

        ITableStructureUsage copy = new TableStructureUsage();
        copy.initFromXml(element);

        assertEquals("roleA", copy.getRoleName());
        assertTrue(copy.isMandatoryTableContent());
        assertEquals(2, copy.getTableStructures().length);
        assertEquals("tableStructureA", copy.getTableStructures()[0]);
        assertEquals("tableStructureB", copy.getTableStructures()[1]);
    }

    @Test
    public void testSetRoleName() {
        tableStructureUsage.setRoleName("role100");
        assertEquals("role100", tableStructureUsage.getRoleName());
        assertTrue(pcType.getIpsSrcFile().isDirty());
    }

    @Test
    public void testSetIsMandatoryTableContent() {
        tableStructureUsage.setMandatoryTableContent(false);
        assertFalse(tableStructureUsage.isMandatoryTableContent());
        assertFalse(pcType.getIpsSrcFile().isDirty());
        tableStructureUsage.setMandatoryTableContent(true);
        assertTrue(tableStructureUsage.isMandatoryTableContent());
        assertTrue(pcType.getIpsSrcFile().isDirty());
    }

    @Test
    public void testAddRemoveTableStructure() {
        assertEquals(0, tableStructureUsage.getTableStructures().length);
        tableStructureUsage.removeTableStructure("tableStructureA");
        assertFalse(pcType.getIpsSrcFile().isDirty());

        tableStructureUsage.addTableStructure("tableStructureA");
        tableStructureUsage.addTableStructure("tableStructureB");
        assertTrue(pcType.getIpsSrcFile().isDirty());
        assertEquals(2, tableStructureUsage.getTableStructures().length);
        tableStructureUsage.removeTableStructure("tableStructureC");
        assertEquals(2, tableStructureUsage.getTableStructures().length);
        tableStructureUsage.removeTableStructure("tableStructureA");
        assertEquals(1, tableStructureUsage.getTableStructures().length);
        assertEquals("tableStructureB", tableStructureUsage.getTableStructures()[0]);
        tableStructureUsage.removeTableStructure("tableStructureB");
        assertEquals(0, tableStructureUsage.getTableStructures().length);
    }

    @Test
    public void testValidate_TableStructureNotFound() throws CoreException {
        MessageList ml = tableStructureUsage.validate(project);
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND));

        tableStructureUsage.addTableStructure("test.TableStructureX");
        ml = tableStructureUsage.validate(project);
        assertNotNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND));

        tableStructureUsage.removeTableStructure("test.TableStructureX");
        tableStructureUsage.addTableStructure("test.TableStructure1");
        ml = tableStructureUsage.validate(project);
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND));
    }

    @Test
    public void testValidate_InvalidRoleName() throws CoreException {
        tableStructureUsage.setRoleName("role1");
        MessageList ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_INVALID_ROLE_NAME));

        tableStructureUsage.setRoleName("1role");
        ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertNotNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_INVALID_ROLE_NAME));

        tableStructureUsage.setRoleName("role 1");
        ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertNotNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_INVALID_ROLE_NAME));
    }

    @Test
    public void testValidate_MustReferenceAtLeast1TableStructure() throws CoreException {
        MessageList ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertNotNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE));

        tableStructureUsage.addTableStructure("tableStructure1");
        ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE));

    }

    @Test
    public void testValidateDuplicateRoleName() throws CoreException {
        tableStructureUsage.setRoleName("role1");
        MessageList ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_SAME_ROLENAME));

        tableStructureUsage = pcType.newTableStructureUsage();
        tableStructureUsage.setRoleName("role1");
        ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertNotNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_SAME_ROLENAME));
    }

    @Test
    public void testValidateRoleNameInSupertypeHierarchy() throws CoreException {
        IProductCmptType a = newProductCmptType(project, "a");
        ITableStructureUsage aStructureUsage = a.newTableStructureUsage();
        aStructureUsage.setRoleName("usage");

        MessageList ml = aStructureUsage.validate(aStructureUsage.getIpsProject());
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_ROLE_NAME_ALREADY_IN_SUPERTYPE));

        IProductCmptType b = newProductCmptType(project, "b");
        a.setSupertype(b.getQualifiedName());
        ml = aStructureUsage.validate(aStructureUsage.getIpsProject());
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_ROLE_NAME_ALREADY_IN_SUPERTYPE));

        ITableStructureUsage bStructureUsage = b.newTableStructureUsage();
        bStructureUsage.setRoleName("usage");
        ml = aStructureUsage.validate(aStructureUsage.getIpsProject());
        assertNotNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_ROLE_NAME_ALREADY_IN_SUPERTYPE));

        bStructureUsage.setRoleName("otherName");
        ml = aStructureUsage.validate(aStructureUsage.getIpsProject());
        assertNull(ml.getMessageByCode(ITableStructureUsage.MSGCODE_ROLE_NAME_ALREADY_IN_SUPERTYPE));

    }
}
