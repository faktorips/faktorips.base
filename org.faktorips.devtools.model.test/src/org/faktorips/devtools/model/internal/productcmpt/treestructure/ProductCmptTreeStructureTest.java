/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.treestructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.AssociationType;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptTreeStructureTest extends AbstractIpsPluginTest {

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

        structure = productCmpt.getStructure(new GregorianCalendar(), ipsProject);
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
        productCmpt.getStructure(new GregorianCalendar(), ipsProject);
        productCmptTarget.getStructure(new GregorianCalendar(), ipsProject);

        // create a circle
        association.setTarget(productCmptType.getQualifiedName());
        productCmptTarget.setProductCmptType(productCmptType.getQualifiedName());
        IProductCmptGeneration targetGen = (IProductCmptGeneration)productCmptTarget.getGeneration(0);
        IProductCmptLink link = targetGen.newLink(association.getName());
        link.setTarget(productCmpt.getQualifiedName());

        try {
            productCmpt.getStructure(new GregorianCalendar(), ipsProject);
            fail();
        } catch (CycleInProductStructureException e) {
            // success
        }
    }

    @Test
    public void testAssociationNotRelevant() throws Exception {
        assertTrue(structure.getRoot().hasAssociationChildren());
        association.setRelevant(false);
        structure.refresh();
        assertFalse(structure.getRoot().hasAssociationChildren());
    }

    @Test
    public void testTblContentUsageReferences() throws Exception {
        IProductCmptStructureTblUsageReference[] ptsus = structure
                .getChildProductCmptStructureTblUsageReference(structure.getRoot());
        assertEquals(1, ptsus.length);
        ITableContentUsage tcu = ptsus[0].getTableContentUsage();
        assertEquals("tableContent1", tcu.getTableContentName());

        IProductCmptTreeStructure structureTarget = productCmptTarget.getStructure(new GregorianCalendar(), ipsProject);
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

    @Test
    public void testReferencesProductCmpt() throws CoreException, CycleInProductStructureException {
        IProductCmpt unReferencedProductCmpt = newProductCmpt(productCmptType, "products.TestProductUnReferenced");

        assertTrue(structure.referencesProductCmptQualifiedName(productCmpt.getQualifiedName()));
        assertTrue(structure.referencesProductCmptQualifiedName(productCmptTarget.getQualifiedName()));
        assertTrue(structure.referencesProductCmptQualifiedName(productCmptTarget2.getQualifiedName()));
        assertFalse(structure.referencesProductCmptQualifiedName(unReferencedProductCmpt.getQualifiedName()));

        structure = productCmptTarget.getStructure(new GregorianCalendar(), ipsProject);
        assertFalse(structure.referencesProductCmptQualifiedName(productCmpt.getQualifiedName()));
        assertTrue(structure.referencesProductCmptQualifiedName(productCmptTarget.getQualifiedName()));
        assertFalse(structure.referencesProductCmptQualifiedName(productCmptTarget2.getQualifiedName()));
        assertFalse(structure.referencesProductCmptQualifiedName(unReferencedProductCmpt.getQualifiedName()));
    }

    @Test
    public void testFindReferencesFor() throws CoreException {
        List<IProductCmptReference> result;
        List<IProductCmpt> cmpts = new ArrayList<>();

        result = structure.findReferencesFor(cmpts);
        assertTrue(result.isEmpty());

        cmpts.add(productCmpt);
        result = structure.findReferencesFor(cmpts);
        assertEquals(1, result.size());

        cmpts.add(productCmptTarget);
        cmpts.add(productCmptTarget2);
        result = structure.findReferencesFor(cmpts);
        assertEquals(3, result.size());

        cmpts.add(newProductCmpt(ipsProject, "dummy"));
        result = structure.findReferencesFor(cmpts);
        assertEquals(3, result.size());
    }
}
