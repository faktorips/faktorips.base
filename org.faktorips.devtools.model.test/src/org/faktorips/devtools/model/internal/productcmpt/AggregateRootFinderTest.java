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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.junit.Test;

public class AggregateRootFinderTest extends AbstractIpsPluginTest {
    private AggregateRootFinder aggregateRootFinder;
    private IIpsProject ipsProject;
    private ProductCmpt a;
    private ProductCmpt b;
    private ProductCmpt c;
    private ProductCmpt d;
    private IProductCmptTypeAssociation selfReference;
    private IProductCmptTypeAssociation association;
    private IProductCmptTypeAssociation genSelfReference;
    private IProductCmptTypeAssociation genAssociation;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("AggregateRootTestProject");
        aggregateRootFinder = new AggregateRootFinder(ipsProject);

        ProductCmptType productCmptType = newProductCmptType(ipsProject, "SelfReferencingType");

        selfReference = productCmptType.newProductCmptTypeAssociation();
        selfReference.setTargetRoleSingular("SelfReference");
        selfReference.setAssociationType(AssociationType.AGGREGATION);
        selfReference.setTarget(productCmptType.getQualifiedName());
        selfReference.setMinCardinality(0);
        selfReference.setMinCardinality(5);

        association = productCmptType.newProductCmptTypeAssociation();
        association.setTargetRoleSingular("Association");
        association.setAssociationType(AssociationType.ASSOCIATION);
        association.setTarget(productCmptType.getQualifiedName());
        association.setMinCardinality(0);
        association.setMinCardinality(5);

        genSelfReference = productCmptType.newProductCmptTypeAssociation();
        genSelfReference.setChangingOverTime(true);
        genSelfReference.setTargetRoleSingular("SelfReferenceChangingOverTime");
        genSelfReference.setAssociationType(AssociationType.AGGREGATION);
        genSelfReference.setTarget(productCmptType.getQualifiedName());
        genSelfReference.setMinCardinality(0);
        genSelfReference.setMinCardinality(5);

        genAssociation = productCmptType.newProductCmptTypeAssociation();
        genAssociation.setTargetRoleSingular("AssociationChangingOverTime");
        genAssociation.setChangingOverTime(true);
        genAssociation.setAssociationType(AssociationType.ASSOCIATION);
        genAssociation.setTarget(productCmptType.getQualifiedName());
        genAssociation.setMinCardinality(0);
        genAssociation.setMinCardinality(5);

        a = newProductCmpt(productCmptType, "A");
        b = newProductCmpt(productCmptType, "B");
        c = newProductCmpt(productCmptType, "C");
        d = newProductCmpt(productCmptType, "D");
    }

    @Test
    public void testFindRoots() {
        List<IProductCmpt> roots = aggregateRootFinder.findAggregateRoots();

        assertEquals(4, roots.size());
    }

    @Test
    public void testFindRootsSingleRoot() {
        createCompositionBetween(a, b);
        createCompositionBetween(a, c);
        createCompositionBetween(c, d);
        saveAll();

        assertSingleRoot(a);
    }

    @Test
    public void testFindRootsTwoRoots() {
        createCompositionBetween(a, b);
        createCompositionBetween(a, d);
        createCompositionBetween(c, d);
        saveAll();

        assertMultipleRoots(a, c);
    }

    @Test
    public void testFindRootsChain() {
        createCompositionBetween(a, b);
        createCompositionBetween(b, c);
        createCompositionBetween(c, d);
        saveAll();

        assertSingleRoot(a);
    }

    @Test
    public void testFindRootsSelfReferenceTwoRoots() {
        createCompositionBetween(b, b);
        createCompositionBetween(b, a);
        createCompositionBetween(c, d);
        saveAll();

        assertMultipleRoots(b, c);
    }

    @Test
    public void testFindRootsSelfReferenceSingleRoot() {
        createCompositionBetween(b, b);
        createCompositionBetween(b, a);
        createCompositionBetween(b, c);
        createCompositionBetween(c, d);
        saveAll();

        assertSingleRoot(b);
    }

    @Test
    public void testFindRootsCycleSingleRoot() {
        createCompositionBetween(a, b);
        createCompositionBetween(b, c);
        createCompositionBetween(c, d);
        createCompositionBetween(d, b);
        saveAll();

        assertSingleRoot(a);
    }

    @Test
    public void testFindRootsCycleInNonRoot() {
        createCompositionBetween(a, b);
        createCompositionBetween(b, b);
        createCompositionBetween(b, c);
        createCompositionBetween(c, d);
        saveAll();

        assertSingleRoot(a);
    }

    @Test
    public void testFindRootsAssociationIgnored() {
        createCompositionBetween(a, b);
        createCompositionBetween(c, d);
        createAssociationBetween(a, c);
        saveAll();

        assertMultipleRoots(a, c);
    }

    @Test
    public void testFindRootsAssociationIgnored2() {
        createAssociationBetween(d, c);
        createAssociationBetween(c, b);
        createAssociationBetween(b, a);
        saveAll();

        assertMultipleRoots(a, b, c, d);
    }

    @Test
    public void testFindRootsCompleteCycle() {
        createCompositionBetween(a, b);
        createCompositionBetween(b, c);
        createCompositionBetween(c, d);
        createCompositionBetween(d, a);
        saveAll();

        assertZeroRoots();
    }

    @Test
    public void testFindRootsCompositionInGeneration() {
        createChangingCompositionBetween(a, b);
        createChangingCompositionBetween(a, d);
        createChangingCompositionBetween(c, d);
        saveAll();

        assertMultipleRoots(a, c);
    }

    @Test
    public void testFindRootsAssociationInGeneration() {
        createChangingAssociationBetween(a, b);
        createChangingCompositionBetween(a, d);
        createChangingCompositionBetween(c, d);
        saveAll();

        assertMultipleRoots(a, b, c);
    }

    protected void assertZeroRoots() {
        assertMultipleRoots();
    }

    protected void assertMultipleRoots(IProductCmpt... prodCmpts) {
        List<IProductCmpt> roots = aggregateRootFinder.findAggregateRoots();

        List<IProductCmpt> prodCmptsList = Arrays.asList(prodCmpts);
        assertEquals(prodCmptsList.size(), roots.size());
        assertTrue(roots.containsAll(prodCmptsList));
    }

    private void createAssociationBetween(ProductCmpt source, ProductCmpt target) {
        IProductCmptLink link = source.newLink(association);
        link.setTarget(target.getQualifiedName());
    }

    private void createChangingAssociationBetween(ProductCmpt source, ProductCmpt target) {
        IProductCmptLink link = source.getProductCmptGeneration(0).newLink(genAssociation);
        link.setTarget(target.getQualifiedName());
    }

    protected void assertSingleRoot(ProductCmpt root) {
        assertMultipleRoots(root);
    }

    protected void createCompositionBetween(ProductCmpt source, ProductCmpt target) {
        IProductCmptLink link = source.newLink(selfReference);
        link.setTarget(target.getQualifiedName());
    }

    private void createChangingCompositionBetween(ProductCmpt source, ProductCmpt target) {
        IProductCmptLink link = source.getProductCmptGeneration(0).newLink(genSelfReference);
        link.setTarget(target.getQualifiedName());
    }

    private void saveAll() {
        save(a);
        save(b);
        save(c);
        save(d);
    }

    private void save(ProductCmpt prodCmpt) {
        prodCmpt.getIpsSrcFile().save(null);
    }
}
