/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
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
    public void testFindRoots_oneRoot() {
        createCompositionBetween(a, b);
        createCompositionBetween(a, c);
        createCompositionBetween(c, d);
        saveAll();

        assertSingleRoot(a);
    }

    @Test
    public void testFindRoots_twoRoots() {
        createCompositionBetween(a, b);
        createCompositionBetween(a, d);
        createCompositionBetween(c, d);
        saveAll();

        assertMultipleRoots(a, c);
    }

    protected void assertMultipleRoots(IProductCmpt... prodCmpts) {
        List<IProductCmpt> roots = aggregateRootFinder.findAggregateRoots();

        List<IProductCmpt> prodCmptsList = Arrays.asList(prodCmpts);
        assertEquals(prodCmptsList.size(), roots.size());
        assertTrue(roots.containsAll(prodCmptsList));
    }

    @Test
    public void testFindRoots_chain() {
        createCompositionBetween(a, b);
        createCompositionBetween(b, c);
        createCompositionBetween(c, d);
        saveAll();

        assertSingleRoot(a);
    }

    @Test
    public void testFindRoots_selfReferenceTwoRoots() {
        createCompositionBetween(b, b);
        createCompositionBetween(b, a);
        createCompositionBetween(c, d);
        saveAll();

        assertMultipleRoots(b, c);
    }

    @Test
    public void testFindRoots_selfReferenceSingleRoot() {
        createCompositionBetween(b, b);
        createCompositionBetween(b, a);
        createCompositionBetween(b, c);
        createCompositionBetween(c, d);
        saveAll();

        assertSingleRoot(b);
    }

    @Test
    public void testFindRoots_CycleSingleRoot() {
        createCompositionBetween(a, b);
        createCompositionBetween(b, c);
        createCompositionBetween(c, d);
        createCompositionBetween(d, b);
        saveAll();

        assertSingleRoot(a);
    }

    @Test
    public void testFindRoots_CycleInNonRoot() {
        createCompositionBetween(a, b);
        createCompositionBetween(b, b);
        createCompositionBetween(b, c);
        createCompositionBetween(c, d);
        saveAll();

        assertSingleRoot(a);
    }

    @Test
    public void testFindRoots_associationIgnored() {
        createCompositionBetween(a, b);
        createCompositionBetween(c, d);
        createAssociationBetween(a, c);
        saveAll();

        assertMultipleRoots(a, c);
    }

    @Test
    public void testFindRoots_associationIgnored2() {
        createAssociationBetween(d, c);
        createAssociationBetween(c, b);
        createAssociationBetween(b, a);
        saveAll();

        assertMultipleRoots(a, b, c, d);
    }

    @Test
    public void testFindRoots_completeCycle() {
        createCompositionBetween(a, b);
        createCompositionBetween(b, c);
        createCompositionBetween(c, d);
        createCompositionBetween(d, a);
        saveAll();

        assertZeroRoots();
    }

    @Test
    public void testFindRoots_aggregationInGeneration() {
        createChangingCompositionBetween(a, b);
        createChangingCompositionBetween(a, d);
        createChangingCompositionBetween(c, d);
        saveAll();

        assertMultipleRoots(a, c);
    }

    protected void assertZeroRoots() {
        assertMultipleRoots();
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
        try {
            prodCmpt.getIpsSrcFile().save(false, null);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
