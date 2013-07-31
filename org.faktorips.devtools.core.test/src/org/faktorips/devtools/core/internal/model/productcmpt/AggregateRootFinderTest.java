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

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("AggregateRootTestProject");
        aggregateRootFinder = new AggregateRootFinder(ipsProject);

        ProductCmptType productCmptType = newProductCmptType(ipsProject, "SelfReferencingType");
        selfReference = productCmptType.newProductCmptTypeAssociation();
        selfReference.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        selfReference.setTarget(productCmptType.getQualifiedName());
        selfReference.setMinCardinality(0);
        selfReference.setMinCardinality(5);

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
        createLinkBetween(a, b);
        createLinkBetween(a, c);
        createLinkBetween(c, d);
        saveAll();

        assertSingleRoot(a);
    }

    @Test
    public void testFindRoots_twoRoots() {
        createLinkBetween(a, b);
        createLinkBetween(a, d);
        createLinkBetween(c, d);
        saveAll();

        List<IProductCmpt> roots = aggregateRootFinder.findAggregateRoots();

        assertEquals(2, roots.size());
        assertTrue(roots.contains(a));
        assertTrue(roots.contains(c));
    }

    @Test
    public void testFindRoots_chain() {
        createLinkBetween(a, b);
        createLinkBetween(b, c);
        createLinkBetween(c, d);
        saveAll();

        assertSingleRoot(a);
    }

    @Test
    public void testFindRoots_selfReferenceTwoRoots() {
        createLinkBetween(b, b);
        createLinkBetween(b, a);
        createLinkBetween(c, d);
        saveAll();

        List<IProductCmpt> roots = aggregateRootFinder.findAggregateRoots();

        assertEquals(2, roots.size());
        assertTrue(roots.contains(b));
        assertTrue(roots.contains(c));
    }

    @Test
    public void testFindRoots_selfReferenceSingleRoot() {
        createLinkBetween(b, b);
        createLinkBetween(b, a);
        createLinkBetween(b, c);
        createLinkBetween(c, d);
        saveAll();

        assertSingleRoot(b);
    }

    @Test
    public void testFindRoots_CycleSingleRoot() {
        createLinkBetween(a, b);
        createLinkBetween(b, c);
        createLinkBetween(c, d);
        createLinkBetween(d, b);
        saveAll();

        assertSingleRoot(a);
    }

    @Test
    public void testFindRoots_CycleInNonRoot() {
        createLinkBetween(a, b);
        createLinkBetween(b, b);
        createLinkBetween(b, c);
        createLinkBetween(c, d);
        saveAll();

        assertSingleRoot(a);
    }

    protected void assertSingleRoot(ProductCmpt root) {
        List<IProductCmpt> roots = aggregateRootFinder.findAggregateRoots();

        assertEquals(1, roots.size());
        assertTrue(roots.contains(root));
    }

    protected void createLinkBetween(ProductCmpt source, ProductCmpt target) {
        IProductCmptLink linkAB = source.newLink(selfReference);
        linkAB.setTarget(target.getQualifiedName());
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
