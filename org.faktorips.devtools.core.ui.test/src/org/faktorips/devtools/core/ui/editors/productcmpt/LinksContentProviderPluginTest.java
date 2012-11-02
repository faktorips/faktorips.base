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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.editors.productcmpt.link.LinkSectionViewItem;
import org.faktorips.devtools.core.ui.editors.productcmpt.link.LinksContentProvider;
import org.junit.Before;
import org.junit.Test;

public class LinksContentProviderPluginTest extends AbstractIpsPluginTest {

    private static final String ASSOCIATION = "association";
    private static final String OTHER_ASSOCIATION = "otherAssociation";
    private static final String EMPTY_ASSOCIATION = "emptyAssociation";
    private static final String UNDEFINED_ASSOCIATION = "undefinedAssociation";

    private IIpsProject ipsProject;

    private ProductCmpt associated1;
    private ProductCmpt associated2;
    private ProductCmpt associated3;

    private IProductCmptType type;
    private IProductCmptType associatedType;

    private IProductCmptTypeAssociation association;
    private IProductCmptTypeAssociation otherAssociation;
    private IProductCmptTypeAssociation emptyAssociation;

    private LinksContentProvider linksContentProvider;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("TestProject");

        type = newProductCmptType(ipsProject, "model.ProductType");

        associatedType = newProductCmptType(ipsProject, "model.AssociatedType");

        associated1 = newProductCmpt(associatedType, "product.Associated1");
        associated2 = newProductCmpt(associatedType, "product.Associated2");
        associated3 = newProductCmpt(associatedType, "product.Associated3");

        association = type.newProductCmptTypeAssociation();
        association.setTarget(associatedType.getQualifiedName());
        association.setTargetRoleSingular(ASSOCIATION);

        otherAssociation = type.newProductCmptTypeAssociation();
        otherAssociation.setTarget(associatedType.getQualifiedName());
        otherAssociation.setTargetRoleSingular(OTHER_ASSOCIATION);

        emptyAssociation = type.newProductCmptTypeAssociation();
        emptyAssociation.setTarget(associatedType.getQualifiedName());
        emptyAssociation.setTargetRoleSingular(EMPTY_ASSOCIATION);

        linksContentProvider = new LinksContentProvider();
    }

    @Test
    public void testWithTypedProductCmpt() throws CoreException {

        IProductCmpt productCmpt = newProductCmpt(type, "product.Product");

        IProductCmptGeneration generation = createGenerationWithAssociations(productCmpt);

        Object[] elements = linksContentProvider.getElements(generation);

        assertEquals(3, elements.length);
        assertEquals(ASSOCIATION, ((LinkSectionViewItem)elements[0]).getAssociationName());
        assertEquals(OTHER_ASSOCIATION, ((LinkSectionViewItem)elements[1]).getAssociationName());
        assertEquals(EMPTY_ASSOCIATION, ((LinkSectionViewItem)elements[2]).getAssociationName());
    }

    @Test
    public void testWithNotTypedProductCmpt() throws CoreException {

        IProductCmpt productCmpt = newProductCmpt(ipsProject, "product.Product");

        IProductCmptGeneration generation = createGenerationWithAssociations(productCmpt);

        Object[] elements = linksContentProvider.getElements(generation);

        assertEquals(3, elements.length);
        assertEquals(OTHER_ASSOCIATION, ((LinkSectionViewItem)elements[0]).getAssociationName());
        assertEquals(ASSOCIATION, ((LinkSectionViewItem)elements[1]).getAssociationName());
        assertEquals(UNDEFINED_ASSOCIATION, ((LinkSectionViewItem)elements[2]).getAssociationName());
    }

    private IProductCmptGeneration createGenerationWithAssociations(IProductCmpt productCmpt) {
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();

        // sequence important for testing
        IProductCmptLink newLink = generation.newLink(otherAssociation.getName());
        newLink.setTarget(associated3.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated1.getQualifiedName());

        newLink = generation.newLink(association.getName());
        newLink.setTarget(associated2.getQualifiedName());

        newLink = generation.newLink(UNDEFINED_ASSOCIATION);
        newLink.setTarget(associated2.getQualifiedName());

        linksContentProvider.inputChanged(null, null, generation);
        return generation;
    }
}
