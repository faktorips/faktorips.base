/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpttype;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TableStructureUsageTest extends AbstractIpsPluginTest {

    private IIpsProject project;

    private IProductCmptType productCmptType;

    private ITableStructureUsage tableStructureUsage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        project = this.newIpsProject();

        productCmptType = newProductCmptType(project, "test.Product");
        tableStructureUsage = productCmptType.newTableStructureUsage();
        tableStructureUsage.setRoleName("roleName");
        productCmptType.getIpsSrcFile().save(true, null);

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
        assertEquals(0, productCmptType.getTableStructureUsages().size());
        assertTrue(productCmptType.getIpsSrcFile().isDirty());
    }

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "TableStructureUsage", 0);
        tableStructureUsage.initFromXml(paramEl);
        assertEquals("role1", tableStructureUsage.getRoleName());
        assertTrue(tableStructureUsage.isMandatoryTableContent());
        assertFalse(tableStructureUsage.isChangingOverTime());
        assertEquals(3, tableStructureUsage.getTableStructures().length);
        for (int i = 0; i < 3; i++) {
            assertEquals("tableStructure" + (i + 1), tableStructureUsage.getTableStructures()[i]);
        }
        paramEl = XmlUtil.getElement(docEl, "TableStructureUsage", 1);
        tableStructureUsage.initFromXml(paramEl);
        assertFalse(tableStructureUsage.isMandatoryTableContent());
        assertTrue(tableStructureUsage.isChangingOverTime());
    }

    @Test
    public void testToXml() {
        tableStructureUsage.setRoleName("roleA");
        tableStructureUsage.setMandatoryTableContent(true);
        tableStructureUsage.addTableStructure("tableStructureA");
        tableStructureUsage.addTableStructure("tableStructureB");
        tableStructureUsage.setCategory("foo");
        tableStructureUsage.setChangingOverTime(false);

        Element element = tableStructureUsage.toXml(this.newDocument());

        ITableStructureUsage copy = productCmptType.newTableStructureUsage();
        copy.initFromXml(element);

        assertEquals("roleA", copy.getRoleName());
        assertTrue(copy.isMandatoryTableContent());
        assertEquals(2, copy.getTableStructures().length);
        assertEquals("tableStructureA", copy.getTableStructures()[0]);
        assertEquals("tableStructureB", copy.getTableStructures()[1]);
        assertEquals("foo", copy.getCategory());
        assertFalse(copy.isChangingOverTime());
    }

    @Test
    public void testSetRoleName() {
        tableStructureUsage.setRoleName("role100");
        assertEquals("role100", tableStructureUsage.getRoleName());
        assertTrue(productCmptType.getIpsSrcFile().isDirty());
    }

    @Test
    public void testSetIsMandatoryTableContent() {
        tableStructureUsage.setMandatoryTableContent(false);
        assertFalse(tableStructureUsage.isMandatoryTableContent());
        assertFalse(productCmptType.getIpsSrcFile().isDirty());
        tableStructureUsage.setMandatoryTableContent(true);
        assertTrue(tableStructureUsage.isMandatoryTableContent());
        assertTrue(productCmptType.getIpsSrcFile().isDirty());
    }

    @Test
    public void testAddRemoveTableStructure() {
        assertEquals(0, tableStructureUsage.getTableStructures().length);
        tableStructureUsage.removeTableStructure("tableStructureA");
        assertFalse(productCmptType.getIpsSrcFile().isDirty());

        tableStructureUsage.addTableStructure("tableStructureA");
        tableStructureUsage.addTableStructure("tableStructureB");
        assertTrue(productCmptType.getIpsSrcFile().isDirty());
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
    public void testValidate_TableStructureNotFound() {
        MessageList ml = tableStructureUsage.validate(project);
        assertThat(ml, lacksMessageCode(ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND));

        tableStructureUsage.addTableStructure("test.TableStructureX");
        ml = tableStructureUsage.validate(project);
        assertThat(ml, hasMessageCode(ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND));

        tableStructureUsage.removeTableStructure("test.TableStructureX");
        tableStructureUsage.addTableStructure("test.TableStructure1");
        ml = tableStructureUsage.validate(project);
        assertThat(ml, lacksMessageCode(ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND));
    }

    @Test
    public void testValidate_InvalidRoleName() {
        tableStructureUsage.setRoleName("role1");
        MessageList ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertThat(ml, lacksMessageCode(ITableStructureUsage.MSGCODE_INVALID_ROLE_NAME));

        tableStructureUsage.setRoleName("1role");
        ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertThat(ml, hasMessageCode(ITableStructureUsage.MSGCODE_INVALID_ROLE_NAME));

        tableStructureUsage.setRoleName("role 1");
        ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertThat(ml, hasMessageCode(ITableStructureUsage.MSGCODE_INVALID_ROLE_NAME));
    }

    @Test
    public void testValidate_MustReferenceAtLeast1TableStructure() {
        MessageList ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertThat(ml, hasMessageCode(ITableStructureUsage.MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE));

        tableStructureUsage.addTableStructure("tableStructure1");
        ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertThat(ml, lacksMessageCode(ITableStructureUsage.MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE));

    }

    @Test
    public void testValidateRoleNameInSupertypeHierarchy() {
        IProductCmptType a = newProductCmptType(project, "a");
        ITableStructureUsage aStructureUsage = a.newTableStructureUsage();
        aStructureUsage.setRoleName("usage");

        MessageList ml = aStructureUsage.validate(aStructureUsage.getIpsProject());
        assertThat(ml, lacksMessageCode(ITableStructureUsage.MSGCODE_ROLE_NAME_ALREADY_IN_SUPERTYPE));

        IProductCmptType b = newProductCmptType(project, "b");
        a.setSupertype(b.getQualifiedName());
        ml = aStructureUsage.validate(aStructureUsage.getIpsProject());
        assertThat(ml, lacksMessageCode(ITableStructureUsage.MSGCODE_ROLE_NAME_ALREADY_IN_SUPERTYPE));

        ITableStructureUsage bStructureUsage = b.newTableStructureUsage();
        bStructureUsage.setRoleName("usage");
        ml = aStructureUsage.validate(aStructureUsage.getIpsProject());
        assertThat(ml, hasMessageCode(ITableStructureUsage.MSGCODE_ROLE_NAME_ALREADY_IN_SUPERTYPE));

        bStructureUsage.setRoleName("otherName");
        ml = aStructureUsage.validate(aStructureUsage.getIpsProject());
        assertThat(ml, lacksMessageCode(ITableStructureUsage.MSGCODE_ROLE_NAME_ALREADY_IN_SUPERTYPE));
    }

    @Test
    public void testValidate_typeDoesNotAcceptChangingOverTime() {
        productCmptType.setChangingOverTime(true);
        tableStructureUsage.setChangingOverTime(false);

        MessageList ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(true);
        tableStructureUsage.setChangingOverTime(true);

        ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(false);
        tableStructureUsage.setChangingOverTime(false);

        ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(false);
        tableStructureUsage.setChangingOverTime(true);

        ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
        assertNotNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testIsPolicyCmptTypeProperty() {
        assertFalse(tableStructureUsage.isPolicyCmptTypeProperty());
    }

    @Test
    public void testIsPropertyFor() {
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        IPropertyValue propertyValue = generation.newTableContentUsage(tableStructureUsage);

        assertTrue(tableStructureUsage.isPropertyFor(propertyValue));
    }

    @Test
    public void testChangingOverTime_default() {
        productCmptType.setChangingOverTime(false);
        tableStructureUsage = productCmptType.newTableStructureUsage();

        assertFalse(tableStructureUsage.isChangingOverTime());

        productCmptType.setChangingOverTime(true);
        tableStructureUsage = productCmptType.newTableStructureUsage();

        assertTrue(tableStructureUsage.isChangingOverTime());
    }
}
