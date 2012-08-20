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

package org.faktorips.devtools.core.ui.views.modeloverview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Test;

public class ModelOverviewContentProviderTest extends AbstractIpsPluginTest {

    @Test
    public void testHasNoChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        newProductCmptType(project, "TestProductComponentType");

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // test
        for (Object object : elements) {
            assertFalse(contentProvider.hasChildren(object));
        }
    }

    @Test
    public void testHasSubtypeChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");
        subCmptType.setSupertype(cmptType.getQualifiedName());

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType subProdCmptType = newProductCmptType(project, "TestSubProductComponentType");
        subProdCmptType.setSupertype(prodCmptType.getQualifiedName());

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // test
        for (Object element : elements) {
            assertTrue(contentProvider.hasChildren(element));
        }
    }

    @Test
    public void testGetRootElements() throws CoreException {
        // setup
        // project1: Status of root elements depends only on associations
        IIpsProject project1 = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project1, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project1, "TestPolicyComponentType2");
        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IType prodCmptType = newProductCmptType(project1, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project1, "TestProductComponentType2");
        IAssociation association2 = prodCmptType.newAssociation();
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        // project2: Status of root elements depends only on supertypes
        IIpsProject project2 = newIpsProject();
        IType cmptType2 = newPolicyCmptTypeWithoutProductCmptType(project2, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project2, "TestSubPolicyComponentType");
        subCmptType.setSupertype(cmptType2.getQualifiedName());

        IType prodCmptType2 = newProductCmptType(project2, "TestProductComponentType");
        IType subProdCmptType = newProductCmptType(project2, "TestSubProductComponentType");
        subProdCmptType.setSupertype(prodCmptType2.getQualifiedName());

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements1 = contentProvider.getElements(project1);
        Object[] elements2 = contentProvider.getElements(project2);

        // test the number of existing root elements
        assertEquals(2, elements1.length);
        assertEquals(2, elements2.length);

    }

    @Test
    public void testHasAssociationChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");
        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");
        IAssociation association2 = prodCmptType.newAssociation();
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // test
        for (Object element : elements) {
            assertTrue(contentProvider.hasChildren(element));
        }
    }

    @Test
    public void testHasAssociationAndSubtypeChildren() throws CoreException {
        // setup
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");
        IType subProdCmptType = newProductCmptType(project, "TestSubProductComponentType");

        subCmptType.setSupertype(cmptType.getQualifiedName());
        subProdCmptType.setSupertype(prodCmptType.getQualifiedName());

        IAssociation association = cmptType.newAssociation();
        IAssociation association2 = prodCmptType.newAssociation();

        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        Object[] elements = contentProvider.getElements(project);

        // test
        assertEquals(2, elements.length);
        assertTrue(contentProvider.hasChildren(elements[0]));
        assertTrue(contentProvider.hasChildren(elements[1]));
        assertEquals(2, contentProvider.getChildren(elements[0]).length);
        assertEquals(2, contentProvider.getChildren(elements[1]).length);
    }

    @Test
    public void testGetChildrenEmpty() throws CoreException {
        // setup
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        newProductCmptType(project, "TestProductComponentType");

        Object[] elements = contentProvider.getElements(project);

        // test
        assertNotNull(contentProvider.getChildren(elements[0]));
        assertNotNull(contentProvider.getChildren(elements[1]));
        assertEquals(0, contentProvider.getChildren(elements[0]).length);
        assertEquals(0, contentProvider.getChildren(elements[1]).length);
    }

    @Test
    public void testGetChildrenNotEmpty() throws CoreException {
        // setup
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        subCmptType.setSupertype(cmptType.getQualifiedName());

        IIpsProject project2 = newIpsProject();
        IType prodCmptType = newProductCmptType(project2, "TestProductComponentType");
        IType subProdCmptType = newProductCmptType(project2, "TestSubProductComponentType");

        subProdCmptType.setSupertype(prodCmptType.getQualifiedName());

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        Object[] elements = contentProvider.getElements(project);
        Object[] elements2 = contentProvider.getElements(project2);

        // test
        System.out.println(((ComponentNode)elements[0]).getValue().getQualifiedName());
        System.out.println(((ComponentNode)elements2[0]).getValue().getQualifiedName());
        assertEquals(2, contentProvider.getChildren(elements[0]).length);
        assertEquals(1, contentProvider.getChildren(elements2[0]).length);

    }
}