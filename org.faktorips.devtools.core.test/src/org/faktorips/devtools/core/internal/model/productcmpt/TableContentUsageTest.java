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

package org.faktorips.devtools.core.internal.model.productcmpt;

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
    protected void setUp() throws Exception {
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

    public void testValidateUnknownStructure() throws Exception {
        MessageList ml = contentUsage.validate(project);
        assertNotNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_UNKNOWN_STRUCTURE_USAGE));

        contentUsage.setStructureUsage("StructUsageRole");
        ml = contentUsage.validate(project);
        assertNull(ml.getMessageByCode(ITableContentUsage.MSGCODE_UNKNOWN_STRUCTURE_USAGE));
    }

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

    public void testFindTableContents() throws Exception {
        contentUsage.setTableContentName("none");
        assertNull(contentUsage.findTableContents(project));

        contentUsage.setTableContentName(content.getQualifiedName());
        assertSame(content, contentUsage.findTableContents(project));
    }

    public void testFindTableStructureUsage() throws Exception {
        contentUsage.setStructureUsage("none");
        assertNull(contentUsage.findTableStructureUsage(project));
        contentUsage.setStructureUsage(STRUCTURE_ROLENAME);
        assertEquals(structUsage, contentUsage.findTableStructureUsage(project));
    }

    public void testInitFromXml() {
        Element el = getTestDocument().getDocumentElement();
        contentUsage.initFromXml(el);

        assertEquals("rateTable", contentUsage.getStructureUsage());
        assertEquals("RateTable2007", contentUsage.getTableContentName());
    }

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
}
