/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt.treestructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.type.AssociationType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptStructureTest extends AbstractIpsPluginTest {

    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration productCmptGen;
    private IProductCmpt productCmptTarget;
    private IProductCmptTypeAssociation association;
    private IIpsProject ipsProject;
    private IProductCmptTreeStructure structure;
    private ProductCmpt productCmptTarget2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");

        // Build product component types
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "dummy1");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        ITableStructureUsage tsu1 = productCmptType.newTableStructureUsage();
        tsu1.setRoleName("usage1");
        tsu1.addTableStructure("tableStructure1");

        IPolicyCmptType policyCmptTypeTarget = newPolicyAndProductCmptType(ipsProject, "TestTarget", "dummy2");
        IProductCmptType productCmptTypeTarget = policyCmptTypeTarget.findProductCmptType(ipsProject);
        ITableStructureUsage tsu2 = productCmptType.newTableStructureUsage();
        tsu2.setRoleName("usage2");
        tsu2.addTableStructure("tableStructure2");

        association = productCmptType.newProductCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTargetRoleSingular("TestRelation");
        association.setTarget(productCmptTypeTarget.getQualifiedName());

        // Build product component types
        productCmpt = newProductCmpt(productCmptType, "products.TestProduct");
        productCmptGen = productCmpt.getProductCmptGeneration(0);
        ITableContentUsage tcu = productCmptGen.newTableContentUsage();
        tcu.setStructureUsage(tsu1.getRoleName());
        tcu.setTableContentName("tableContent1");

        productCmptTarget = newProductCmpt(productCmptTypeTarget, "products.TestProductTarget");
        productCmptTarget2 = newProductCmpt(productCmptTypeTarget, "products.TestProductTarget2");

        IProductCmptGeneration targetGen = productCmptTarget.getProductCmptGeneration(0);
        tcu = targetGen.newTableContentUsage();
        tcu.setStructureUsage(tsu2.getRoleName());
        tcu.setTableContentName("tableContent2");

        IProductCmptGeneration target2Gen = productCmptTarget2.getProductCmptGeneration(0);
        tcu = target2Gen.newTableContentUsage();
        tcu.setStructureUsage(tsu2.getRoleName());
        tcu.setTableContentName("tableContent2");

        IProductCmptLink link = productCmptGen.newLink(association.getName());
        link.setTarget(productCmptTarget.getQualifiedName());

        link = productCmptGen.newLink(association.getName());
        link.setTarget(productCmptTarget2.getQualifiedName());

        structure = productCmpt.getStructure(ipsProject);
    }

    @Test
    public void testGetRoot() {
        IProductCmpt root = structure.getRoot().getProductCmpt();
        assertSame(productCmpt, root);
    }

    @Test
    public void testNoGeneration() throws CycleInProductStructureException {
        productCmpt.getGenerationsOrderedByValidDate()[0].delete();
        structure.refresh();
    }

    @Test
    public void testCircleDetection() throws Exception {
        // this has to work without any exception
        productCmpt.getStructure(ipsProject);
        productCmptTarget.getStructure(ipsProject);

        // create a circle
        association.setTarget(productCmptType.getQualifiedName());
        productCmptTarget.setProductCmptType(productCmptType.getQualifiedName());
        IProductCmptGeneration targetGen = (IProductCmptGeneration)productCmptTarget.getGeneration(0);
        IProductCmptLink link = targetGen.newLink(association.getName());
        link.setTarget(productCmpt.getQualifiedName());

        try {
            productCmpt.getStructure(ipsProject);
            fail();
        } catch (CycleInProductStructureException e) {
            // success
        }
    }

    @Test
    public void testTblContentUsageReferences() throws Exception {
        IProductCmptStructureTblUsageReference[] ptsus = structure
                .getChildProductCmptStructureTblUsageReference(structure.getRoot());
        assertEquals(1, ptsus.length);
        ITableContentUsage tcu = ptsus[0].getTableContentUsage();
        assertEquals("tableContent1", tcu.getTableContentName());

        IProductCmptTreeStructure structureTarget = productCmptTarget.getStructure(ipsProject);
        ptsus = structure.getChildProductCmptStructureTblUsageReference(structureTarget.getRoot());
        assertEquals(1, ptsus.length);
        tcu = ptsus[0].getTableContentUsage();
        assertEquals("tableContent2", tcu.getTableContentName());
    }

    @Test
    public void testToSet() throws Exception {
        Set<IProductCmptStructureReference> array = structure.toSet(true);
        assertEquals(6, array.size());
        // -> 3 table references: two different tables, with one in two different links
    }
}
