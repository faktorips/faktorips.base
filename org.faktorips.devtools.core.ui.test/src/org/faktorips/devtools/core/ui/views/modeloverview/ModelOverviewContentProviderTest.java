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

import java.util.ArrayList;
import java.util.List;

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
    public void testHasSubtypeChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");
        subCmptType.setSupertype(cmptType.getQualifiedName());

        IIpsProject project2 = newIpsProject();
        IType prodCmptType = newProductCmptType(project2, "TestProductComponentType");
        IType subProdCmptType = newProductCmptType(project2, "TestSubProductComponentType");
        subProdCmptType.setSupertype(prodCmptType.getQualifiedName());

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);
        Object[] elements2 = contentProvider.getElements(project2);

        // test
        assertTrue(contentProvider.hasChildren(elements[0]));
        assertTrue(contentProvider.hasChildren(elements2[0]));
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

        // test the identity of the root elements
        // project1
        List<IType> elementList1 = new ArrayList<IType>();
        elementList1.add(((ComponentNode)elements1[0]).getValue());
        elementList1.add(((ComponentNode)elements1[1]).getValue());
        assertTrue(elementList1.contains(cmptType));
        assertTrue(elementList1.contains(prodCmptType));

        // project2
        List<IType> elementList2 = new ArrayList<IType>();
        elementList2.add(((ComponentNode)elements2[0]).getValue());
        elementList2.add(((ComponentNode)elements2[1]).getValue());
        assertTrue(elementList2.contains(cmptType2));
        assertTrue(elementList2.contains(prodCmptType2));
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

    /**
     * 
     * <strong>Scenario:</strong><br>
     * Tests if the root Elements have children and the returned lists are not null. Furthermore it
     * is checked that the correct {@link AbstractStrucureNode AbstractStructureNodes} are returned.
     * At last the nodes under these structure nodes will be checked on identity.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * In the first {@link IIpsProject} a {@link CompositeNode} and a {@link SubtypeNode} are
     * expected as children of the root element. In the second project only a SubType node is
     * expected.
     */
    @Test
    public void testGetChildren() throws CoreException {
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

        IModelOverviewNode compositeNode = (IModelOverviewNode)contentProvider.getChildren(elements[0])[0];
        IModelOverviewNode subtypeNode = (IModelOverviewNode)contentProvider.getChildren(elements[0])[1];

        // test
        // project1
        assertEquals(2, contentProvider.getChildren(elements[0]).length);

        assertTrue(compositeNode instanceof CompositeNode);
        assertTrue(subtypeNode instanceof SubtypeNode);

        List<IModelOverviewNode> compositeChildren = compositeNode.getChildren();
        assertEquals(1, compositeChildren.size());
        assertEquals(associatedCmptType, ((ComponentNode)compositeChildren.get(0)).getValue());

        List<IModelOverviewNode> subtypeChildren = subtypeNode.getChildren();
        assertEquals(1, subtypeChildren.size());
        assertEquals(subCmptType, ((ComponentNode)subtypeChildren.get(0)).getValue());

        // project2
        Object[] elements2 = contentProvider.getElements(project2);

        IModelOverviewNode subtypeNode2 = (IModelOverviewNode)contentProvider.getChildren(elements2[0])[0];

        assertEquals(1, contentProvider.getChildren(elements2[0]).length);
        assertTrue(subtypeNode2 instanceof SubtypeNode);
        List<IModelOverviewNode> subtypeChildren2 = subtypeNode2.getChildren();
        assertEquals(1, subtypeChildren2.size());
        assertEquals(subProdCmptType, ((ComponentNode)subtypeChildren2.get(0)).getValue());
    }
}