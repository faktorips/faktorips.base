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

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.MessageList;
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
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Structure");
        content = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "Contents");
        structUsage = productCmptType.newTableStructureUsage();
        structUsage.addTableStructure(structure.getQualifiedName());
        structUsage.setRoleName(STRUCTURE_ROLENAME);

        contentUsage = cmpt.getProductCmptGeneration(0).newTableContentUsage();
    }

    @Test
    public void testValidateUnknownStructure() throws Exception {
        MessageList ml = contentUsage.validate(project);
        assertNotNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_UNKNOWN_STRUCTURE_USAGE));

        contentUsage.setStructureUsage("StructUsageRole");
        ml = contentUsage.validate(project);
        assertNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_UNKNOWN_STRUCTURE_USAGE));
    }

    @Test
    public void testValidateUnknownContent() throws Exception {
        structUsage.setMandatoryTableContent(true);
        contentUsage.setStructureUsage("StructUsageRole");
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
        contentUsage.setStructureUsage("StructUsageRole");
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
        contentUsage.setStructureUsage("rateTable");
        contentUsage.setTableContentName("RateTable2007");
        Element el = contentUsage.toXml(newDocument());

        contentUsage.setStructureUsage("");
        contentUsage.setTableContentName("");
        contentUsage.initFromXml(el);

        assertEquals("rateTable", contentUsage.getStructureUsage());
        assertEquals("RateTable2007", contentUsage.getTableContentName());

        contentUsage.setTableContentName(null);
        el = contentUsage.toXml(newDocument());
        contentUsage.setTableContentName("");
        contentUsage.initFromXml(el);
        assertNull(contentUsage.getTableContentName());
    }

    @Test
    public void testGetCaption() throws CoreException {
        contentUsage.setStructureUsage(STRUCTURE_ROLENAME);
        structUsage.getLabel(Locale.GERMAN).setValue("foo");

        assertEquals("foo", contentUsage.getCaption(Locale.GERMAN));
    }

    @Test
    public void testGetCaptionNotExistent() throws CoreException {
        contentUsage.setStructureUsage(STRUCTURE_ROLENAME);
        assertNull(contentUsage.getCaption(Locale.TAIWAN));
    }

    @Test
    public void testGetCaptionNullPointer() throws CoreException {
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

}
