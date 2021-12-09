/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
public class TableContentUsageTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITableStructureUsage structUsage;
    private ITableContentUsage contentUsage;
    private ITableContents content;
    private ITableStructure structure;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt cmpt;

    final private String STRUCTURE_ROLENAME = "StructUsageRole";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(project, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(project);
        cmpt = newProductCmpt(productCmptType, "Cmpt");
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "SearchStructure");
        content = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "Contents");
        structUsage = productCmptType.newTableStructureUsage();
        structUsage.addTableStructure(structure.getQualifiedName());
        structUsage.setRoleName(STRUCTURE_ROLENAME);

        contentUsage = cmpt.getProductCmptGeneration(0).newTableContentUsage(structUsage);
    }

    @Test
    public void testValidateUnknownStructure() throws Exception {
        contentUsage.setStructureUsage("");
        MessageList ml = contentUsage.validate(project);
        assertNotNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_UNKNOWN_STRUCTURE_USAGE));

        contentUsage.setStructureUsage(STRUCTURE_ROLENAME);
        ml = contentUsage.validate(project);
        assertNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_UNKNOWN_STRUCTURE_USAGE));
    }

    @Test
    public void testValidateUnknownContent() throws Exception {
        structUsage.setMandatoryTableContent(true);
        contentUsage.setStructureUsage(STRUCTURE_ROLENAME);
        contentUsage.setTableContentName("unknown");
        MessageList ml = contentUsage.validate(project);
        assertNotNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_UNKNOWN_TABLE_CONTENT));

        contentUsage.setTableContentName(content.getQualifiedName());
        ml = contentUsage.validate(project);
        assertNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_UNKNOWN_TABLE_CONTENT));

        contentUsage.setTableContentName("");
        ml = contentUsage.validate(project);
        assertNotNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_UNKNOWN_TABLE_CONTENT));

        contentUsage.setTableContentName(null);
        ml = contentUsage.validate(project);
        assertNotNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_UNKNOWN_TABLE_CONTENT));

        structUsage.setMandatoryTableContent(false);
        contentUsage.setTableContentName("");
        ml = contentUsage.validate(project);
        assertNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_UNKNOWN_TABLE_CONTENT));

        contentUsage.setTableContentName(null);
        ml = contentUsage.validate(project);
        assertNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_UNKNOWN_TABLE_CONTENT));
    }

    @Test
    public void testValidateInvalidContent() throws Exception {
        content.setTableStructure("unknown");
        contentUsage.setStructureUsage(STRUCTURE_ROLENAME);
        contentUsage.setTableContentName(content.getQualifiedName());
        MessageList ml = contentUsage.validate(project);
        assertNotNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_INVALID_TABLE_CONTENT));

        content.setTableStructure(structUsage.getTableStructures()[0]);
        ml = contentUsage.validate(project);
        assertNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_INVALID_TABLE_CONTENT));
    }

    @Test
    public void testFindTableContents() throws Exception {
        contentUsage.setTableContentName("none");
        assertNull(contentUsage.findTableContents(project));

        contentUsage.setTableContentName(content.getQualifiedName());
        assertSame(content, contentUsage.findTableContents(project));
    }

    @Test
    public void testFindTableStructureUsage() throws Exception {
        contentUsage.setStructureUsage("none");
        assertNull(contentUsage.findTableStructureUsage(project));
        contentUsage.setStructureUsage(STRUCTURE_ROLENAME);
        assertEquals(structUsage, contentUsage.findTableStructureUsage(project));
    }

    @Test
    public void testInitFromXml() {
        Element el = getTestDocument().getDocumentElement();
        contentUsage.initFromXml(el);

        assertEquals("rateTable", contentUsage.getStructureUsage());
        assertEquals("RateTable2007", contentUsage.getTableContentName());
    }

    @Test
    public void testToXml() {
        contentUsage.setStructureUsage(STRUCTURE_ROLENAME);
        contentUsage.setTableContentName("RateTable2007");
        Element el = contentUsage.toXml(newDocument());

        contentUsage.setStructureUsage("");
        contentUsage.setTableContentName("");
        contentUsage.initFromXml(el);

        assertEquals(STRUCTURE_ROLENAME, contentUsage.getStructureUsage());
        assertEquals("RateTable2007", contentUsage.getTableContentName());

        contentUsage.setTableContentName(null);
        el = contentUsage.toXml(newDocument());
        contentUsage.setTableContentName("");
        contentUsage.initFromXml(el);
        assertNull(contentUsage.getTableContentName());
    }

    @Test
    public void testToXml_InheritedValue() throws CoreRuntimeException {
        ITableContentUsage templateContentUsage = addTemplateContentUsage();
        assertThat(contentUsage.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));

        contentUsage.setStructureUsage(STRUCTURE_ROLENAME);
        contentUsage.setTableContentName("RateTable2007");
        templateContentUsage.setTableContentName("TemplateTable");

        Element el = contentUsage.toXml(newDocument());
        ITableContentUsage copy = cmpt.getProductCmptGeneration(0).newTableContentUsage(structUsage);
        copy.initFromXml(el);

        assertThat(copy.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(copy.getStructureUsage(), is(STRUCTURE_ROLENAME));
        assertThat(copy.getTableContentName(), is("TemplateTable"));

    }

    @Test
    public void testGetCaption() throws CoreRuntimeException {
        contentUsage.setStructureUsage(STRUCTURE_ROLENAME);
        structUsage.getLabel(Locale.GERMAN).setValue("foo");

        assertEquals("foo", contentUsage.getCaption(Locale.GERMAN));
    }

    @Test
    public void testGetCaptionNotExistent() throws CoreRuntimeException {
        contentUsage.setStructureUsage(STRUCTURE_ROLENAME);
        assertNull(contentUsage.getCaption(Locale.TAIWAN));
    }

    @Test
    public void testGetCaptionNullPointer() throws CoreRuntimeException {
        try {
            structUsage.getCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetLastResortCaption() {
        contentUsage.setStructureUsage("foo");
        assertEquals("Foo", contentUsage.getLastResortCaption());
    }

    @Test
    public void testGetProductCmpt() {
        assertNotNull(contentUsage.getProductCmpt());

        ITableContentUsage contentUsagePC = cmpt.newPropertyValue(structUsage, ITableContentUsage.class);
        assertNotNull(contentUsagePC);
        assertNotNull(contentUsagePC.getProductCmpt());
        assertEquals(cmpt, contentUsagePC.getProductCmpt());
    }

    @Test
    public void testGetTableContentName() {
        assertThat(contentUsage.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
        contentUsage.setTableContentName(content.getQualifiedName());
        assertThat(contentUsage.getTableContentName(), is(content.getQualifiedName()));
    }

    @Test
    public void testGetTableContentName_InheritedValue() throws CoreRuntimeException {
        ITableContentUsage templateContentUsage = addTemplateContentUsage();
        assertThat(contentUsage.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));

        contentUsage.setTableContentName("my little table");
        templateContentUsage.setTableContentName(content.getQualifiedName());

        assertThat(contentUsage.getTableContentName(), is(content.getQualifiedName()));
    }

    private ITableContentUsage addTemplateContentUsage() throws CoreRuntimeException {
        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGen = template.getProductCmptGeneration(0);
        ITableContentUsage templateContentUsage = templateGen.newTableContentUsage(structUsage);

        cmpt.setTemplate(template.getQualifiedName());
        contentUsage.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        return templateContentUsage;
    }
}
