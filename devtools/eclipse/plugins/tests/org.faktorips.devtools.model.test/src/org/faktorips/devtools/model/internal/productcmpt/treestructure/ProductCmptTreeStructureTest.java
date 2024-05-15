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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptVRuleReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.AssociationType;
import org.junit.Before;
import org.junit.Ignore;
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
    private IValidationRule validationRule;

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

        validationRule = policyCmptType.newRule();
        validationRule.setName("rule1");

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

        assertThat(productCmpt, is(sameInstance(root)));
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
        assertThat(structure.getRoot().hasAssociationChildren(), is(true));
        association.setRelevant(false);
        structure.refresh();
        assertThat(structure.getRoot().hasAssociationChildren(), is(false));
    }

    @Test
    public void testTblContentUsageReferences() throws Exception {
        IProductCmptStructureTblUsageReference[] ptsus = structure
                .getChildProductCmptStructureTblUsageReference(structure.getRoot());
        assertThat(ptsus.length, is(1));
        ITableContentUsage tcu = ptsus[0].getTableContentUsage();
        assertThat(tcu.getTableContentName(), is("tableContent1"));

        IProductCmptTreeStructure structureTarget = productCmptTarget.getStructure(new GregorianCalendar(), ipsProject);
        ptsus = structure.getChildProductCmptStructureTblUsageReference(structureTarget.getRoot());
        assertThat(ptsus.length, is(1));
        tcu = ptsus[0].getTableContentUsage();
        assertThat(tcu.getTableContentName(), is("tableContent2"));
    }

    @Test
    public void testToSet() throws Exception {
        Set<IProductCmptStructureReference> array = structure.toSet(true);
        assertThat(array.size(), is(6));
        // -> 3 table references: two different tables, with one in two different links
    }

    @Ignore
    @Test
    /*
     * This test creates a large, strongly interconnected product to test the performance of the
     * toSet method. We don't need to run it regularly, but the setup remains here for future tests
     */
    public void testToSet_Large() throws Exception {
        long start = System.currentTimeMillis();
        PolicyCmptType a = newPolicyAndProductCmptType(ipsProject, "A", "PA");
        ProductCmptType pa = (ProductCmptType)a.findProductCmptType(ipsProject);
        addLayers(a, pa, 5);
        ProductCmpt prod = newProductCmpt(pa, "Prod");
        addChildren(prod);
        long createdProducts = System.currentTimeMillis();
        System.out.println("Creating " + ipsProject.findAllIpsSrcFiles().size() + " products took "
                + ((createdProducts - start) / 1000) + "s");

        structure = prod.getStructure(new GregorianCalendar(), ipsProject);
        long createdStructure = System.currentTimeMillis();
        System.out.println("Creating structure took " + ((createdStructure - createdProducts) / 1000) + "s");

        Set<IProductCmptStructureReference> set = structure.toSet(false);
        long createdSet = System.currentTimeMillis();
        System.out.println("Creating set took " + ((createdSet - createdStructure) / 1000) + "s");

        assertEquals(661937, set.size());
    }

    private void addLayers(PolicyCmptType pol, ProductCmptType prod, int layers) {
        if (layers > 0) {
            for (int i = 0; i < 2; i++) {
                char c = 'A';
                c += i;
                String name = pol.getName() + c;
                PolicyCmptType a = newPolicyAndProductCmptType(ipsProject, name, "P" + name);
                ProductCmptType pa = (ProductCmptType)a.findProductCmptType(ipsProject);
                IPolicyCmptTypeAssociation ass = pol.newPolicyCmptTypeAssociation();
                ass.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
                ass.setTargetRoleSingular(a.getName());
                ass.setTarget(a.getQualifiedName());
                IProductCmptTypeAssociation pass = prod.newProductCmptTypeAssociation();
                pass.setAssociationType(AssociationType.AGGREGATION);
                pass.setTargetRoleSingular(pa.getName());
                pass.setTarget(pa.getQualifiedName());
                addLayers(a, pa, layers - 1);
            }
        }
        pol.getIpsSrcFile().save(null);
        prod.getIpsSrcFile().save(null);
    }

    private void addChildren(IProductCmpt prod) {
        IProductCmptType prodCmptType = prod.findProductCmptType(ipsProject);
        for (IProductCmptTypeAssociation pass : prodCmptType.getProductCmptTypeAssociations()) {
            IProductCmptType targetProductCmptType = pass.findTargetProductCmptType(ipsProject);
            for (int i = 0; i < 5; i++) {
                String name = "prod." + prodCmptType.getName() + targetProductCmptType.getName() + i;
                IProductCmpt targetProd = ipsProject.findProductCmpt(name);
                if (targetProd == null) {
                    targetProd = newProductCmpt(targetProductCmptType, name);
                    addChildren(targetProd);
                }
                IProductCmptLink link = prod.newLink(pass);
                link.setTarget(name);
            }
            for (int i = 5; i < 7; i++) {
                String name = "prod." + prod.getName() + targetProductCmptType.getName() + i;
                IProductCmpt targetProd = newProductCmpt(targetProductCmptType, name);
                addChildren(targetProd);
                IProductCmptLink link = prod.newLink(pass);
                link.setTarget(name);
            }
        }
        prodCmptType.getIpsSrcFile().save(null);
    }

    @Test
    public void testReferencesProductCmpt() throws IpsException, CycleInProductStructureException {
        IProductCmpt unReferencedProductCmpt = newProductCmpt(productCmptType, "products.TestProductUnReferenced");

        assertThat(structure.referencesProductCmptQualifiedName(productCmpt.getQualifiedName()), is(true));
        assertThat(structure.referencesProductCmptQualifiedName(productCmptTarget.getQualifiedName()), is(true));
        assertThat(structure.referencesProductCmptQualifiedName(productCmptTarget2.getQualifiedName()), is(true));
        assertThat(structure.referencesProductCmptQualifiedName(unReferencedProductCmpt.getQualifiedName()), is(false));

        structure = productCmptTarget.getStructure(new GregorianCalendar(), ipsProject);
        assertThat(structure.referencesProductCmptQualifiedName(productCmpt.getQualifiedName()), is(false));
        assertThat(structure.referencesProductCmptQualifiedName(productCmptTarget.getQualifiedName()), is(true));
        assertThat(structure.referencesProductCmptQualifiedName(productCmptTarget2.getQualifiedName()), is(false));
        assertThat(structure.referencesProductCmptQualifiedName(unReferencedProductCmpt.getQualifiedName()), is(false));
    }

    @Test
    public void testFindReferencesFor() {
        List<IProductCmptReference> result;
        List<IProductCmpt> cmpts = new ArrayList<>();

        result = structure.findReferencesFor(cmpts);
        assertThat(result.isEmpty(), is(true));

        cmpts.add(productCmpt);
        result = structure.findReferencesFor(cmpts);
        assertThat(result.size(), is(1));

        cmpts.add(productCmptTarget);
        cmpts.add(productCmptTarget2);
        result = structure.findReferencesFor(cmpts);
        assertThat(result.size(), is(3));

        cmpts.add(newProductCmpt(ipsProject, "dummy"));
        result = structure.findReferencesFor(cmpts);
        assertThat(result.size(), is(3));
    }

    @Test
    public void testRulesFromGeneration() throws CycleInProductStructureException {
        IValidationRuleConfig ruleConfig = productCmptGen.newValidationRuleConfig(validationRule);
        structure.refresh();

        IProductCmptVRuleReference[] rule = structure.getChildProductCmptVRuleReferences(structure.getRoot());

        assertThat(rule.length, is(1));
        assertThat(rule[0].getValidationRuleConfig(), is(ruleConfig));
        assertThat(rule[0].getValidationRuleConfig().getParent(), is(productCmptGen));
    }

    @Test
    public void testRulesFromProductCmpt() throws CycleInProductStructureException {
        IValidationRuleConfig ruleConfig = productCmpt.newValidationRuleConfig(validationRule);
        structure.refresh();

        IProductCmptVRuleReference[] rule = structure.getChildProductCmptVRuleReferences(structure.getRoot());

        assertThat(rule.length, is(1));
        assertThat(rule[0].getValidationRuleConfig(), is(ruleConfig));
        assertThat(rule[0].getValidationRuleConfig().getParent(), is(productCmpt));
    }
}
