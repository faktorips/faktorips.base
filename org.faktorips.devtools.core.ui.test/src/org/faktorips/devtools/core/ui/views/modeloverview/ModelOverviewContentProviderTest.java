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
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Test;

public class ModelOverviewContentProviderTest extends AbstractIpsPluginTest {

    @Test
    public void testHasNoChildren() {
        // setup
        IIpsProject project;
        try {
            project = newIpsProject();
            newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
            newProductCmptType(project, "TestProductComponentType");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // test
        for (Object object : elements) {
            assertFalse(contentProvider.hasChildren(object));
        }
    }

    @Test
    public void testGetChildrenEmpty() {
        // setup
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();

        IIpsProject project;
        try {
            project = newIpsProject();
            newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
            newProductCmptType(project, "TestProductComponentType");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        Object[] elements = contentProvider.getElements(project);

        // test
        assertNotNull(contentProvider.getChildren(elements[0]));
        assertNotNull(contentProvider.getChildren(elements[1]));
        assertEquals(0, contentProvider.getChildren(elements[0]).length);
        assertEquals(0, contentProvider.getChildren(elements[1]).length);
    }

    @Test
    public void testHasSubtypeChildren() {
        // setup
        IIpsProject project;
        IType cmptType;
        IType subCmptType;
        try {
            project = newIpsProject();
            cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
            subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        subCmptType.setSupertype(cmptType.getQualifiedName());

        IIpsProject project2;
        IType prodCmptType;
        IType subProdCmptType;
        try {
            project2 = newIpsProject();
            prodCmptType = newProductCmptType(project2, "TestProductComponentType");
            subProdCmptType = newProductCmptType(project2, "TestSubProductComponentType");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        subProdCmptType.setSupertype(prodCmptType.getQualifiedName());

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);
        Object[] elements2 = contentProvider.getElements(project2);

        // test
        assertTrue(contentProvider.hasChildren(elements[0]));
        assertTrue(contentProvider.hasChildren(elements2[0]));
    }

    @Test
    public void testGetRootElements() {
        // setup
        // project1: Status of root elements depends only on associations
        IIpsProject project1;
        IType cmptType;
        IType associatedCmptType;
        try {
            project1 = newIpsProject();
            cmptType = newPolicyCmptTypeWithoutProductCmptType(project1, "TestPolicyComponentType");
            associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project1, "TestPolicyComponentType2");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IType prodCmptType;
        IType associatedProdCmptType;
        try {
            prodCmptType = newProductCmptType(project1, "TestProductComponentType");
            associatedProdCmptType = newProductCmptType(project1, "TestProductComponentType2");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        IAssociation association2 = prodCmptType.newAssociation();
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        // project2: Status of root elements depends only on supertypes
        IIpsProject project2;
        IType cmptType2;
        IType subCmptType;
        try {
            project2 = newIpsProject();
            cmptType2 = newPolicyCmptTypeWithoutProductCmptType(project2, "TestPolicyComponentType");
            subCmptType = newPolicyCmptTypeWithoutProductCmptType(project2, "TestSubPolicyComponentType");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        subCmptType.setSupertype(cmptType2.getQualifiedName());

        IType prodCmptType2;
        IType subProdCmptType;
        try {
            prodCmptType2 = newProductCmptType(project2, "TestProductComponentType");
            subProdCmptType = newProductCmptType(project2, "TestSubProductComponentType");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
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
    public void testHasAssociationChildren() {
        // setup
        IIpsProject project;
        IType cmptType;
        IType associatedCmptType;
        try {
            project = newIpsProject();
            cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
            associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IType prodCmptType;
        IType associatedProdCmptType;
        try {
            prodCmptType = newProductCmptType(project, "TestProductComponentType");
            associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
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
    public void testHasAssociationAndSubtypeChildren() {
        // setup
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();

        IIpsProject project;
        IType cmptType;
        IType associatedCmptType;
        IType subCmptType;
        IType prodCmptType;
        IType associatedProdCmptType;
        IType subProdCmptType;
        try {
            project = newIpsProject();
            cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
            associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");
            subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

            prodCmptType = newProductCmptType(project, "TestProductComponentType");
            associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");
            subProdCmptType = newProductCmptType(project, "TestSubProductComponentType");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

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
     * is checked that the correct {@link AbstractStructureNode AbstractStructureNodes} are
     * returned. At last the nodes under these structure nodes will be checked on identity.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * In the first {@link IIpsProject} a {@link CompositeNode} and a {@link SubtypeNode} are
     * expected as children of the root element. In the second project only a SubType node is
     * expected.
     */
    @Test
    public void testGetChildren() {
        // setup
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();

        IIpsProject project;
        IType cmptType;
        IType associatedCmptType;
        IType subCmptType;
        try {
            project = newIpsProject();
            cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
            associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");
            subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        subCmptType.setSupertype(cmptType.getQualifiedName());

        IIpsProject project2;
        IType prodCmptType;
        IType subProdCmptType;
        try {
            project2 = newIpsProject();
            prodCmptType = newProductCmptType(project2, "TestProductComponentType");
            subProdCmptType = newProductCmptType(project2, "TestSubProductComponentType");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        subProdCmptType.setSupertype(prodCmptType.getQualifiedName());

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        Object[] elements = contentProvider.getElements(project);

        IModelOverviewNode subtypeNode = (IModelOverviewNode)contentProvider.getChildren(elements[0])[0];
        IModelOverviewNode compositeNode = (IModelOverviewNode)contentProvider.getChildren(elements[0])[1];

        // test
        // project1
        assertEquals(2, contentProvider.getChildren(elements[0]).length);

        assertTrue(compositeNode instanceof CompositeNode);
        assertTrue(subtypeNode instanceof SubtypeNode);

        List<ComponentNode> compositeChildren = ((CompositeNode)compositeNode).getChildren();
        assertEquals(1, compositeChildren.size());
        assertEquals(associatedCmptType, compositeChildren.get(0).getValue());

        List<ComponentNode> subtypeChildren = ((SubtypeNode)subtypeNode).getChildren();
        assertEquals(1, subtypeChildren.size());
        assertEquals(subCmptType, subtypeChildren.get(0).getValue());

        // project2
        Object[] elements2 = contentProvider.getElements(project2);

        IModelOverviewNode subtypeNode2 = (IModelOverviewNode)contentProvider.getChildren(elements2[0])[0];

        assertEquals(1, contentProvider.getChildren(elements2[0]).length);
        assertTrue(subtypeNode2 instanceof SubtypeNode);
        List<ComponentNode> subtypeChildren2 = ((SubtypeNode)subtypeNode2).getChildren();
        assertEquals(1, subtypeChildren2.size());
        assertEquals(subProdCmptType, subtypeChildren2.get(0).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * The associations which are derived via the supertype hierarchy should not be shown in the
     * ModelOverviewExplorer.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The elements in the supertype hierarchy should not be included in the construction of the
     * composite nodes.
     * 
     */
    @Test
    public void testNoSupertypeAssociations() {
        // setup
        IIpsProject project;
        PolicyCmptType vertrag;
        PolicyCmptType deckung;
        PolicyCmptType hausratVertrag;
        PolicyCmptType hausratGrunddeckung;
        try {
            project = newIpsProject();
            vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
            deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");
            hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");
            hausratGrunddeckung = newPolicyCmptTypeWithoutProductCmptType(project, "HausratGrunddeckung");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        IAssociation associationVertrag2Deckung = vertrag.newAssociation();
        associationVertrag2Deckung.setTarget(deckung.getQualifiedName());
        associationVertrag2Deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IAssociation associationHausratVertrag2HausratGrunddeckung = hausratVertrag.newAssociation();
        associationHausratVertrag2HausratGrunddeckung.setTarget(hausratGrunddeckung.getQualifiedName());
        associationHausratVertrag2HausratGrunddeckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // tests
        ComponentNode vertragNode = (ComponentNode)elements[0];
        assertEquals(vertrag, vertragNode.getValue());
        assertEquals(1, elements.length);

        Object[] vertragStructureChildren = contentProvider.getChildren(elements[0]);

        // test that hausratvertrag is a subtype of vertrag
        SubtypeNode subtypeNode = (SubtypeNode)vertragStructureChildren[0];
        Object[] vertragSubtypeChildren = contentProvider.getChildren(subtypeNode);
        assertEquals(1, vertragSubtypeChildren.length);
        ComponentNode hausratVertragNode = (ComponentNode)vertragSubtypeChildren[0];
        assertEquals(hausratVertrag, hausratVertragNode.getValue());

        // test that only hausratgrunddeckung is a composite of hausratvertrag
        Object[] hausratVertragStructureChildren = contentProvider.getChildren(hausratVertragNode);
        CompositeNode hausratVertragCompositeNode = (CompositeNode)hausratVertragStructureChildren[0];
        Object[] hausratVertragCompositeChildren = contentProvider.getChildren(hausratVertragCompositeNode);
        assertEquals(1, hausratVertragCompositeChildren.length);
        assertEquals(hausratGrunddeckung, ((ComponentNode)hausratVertragCompositeChildren[0]).getValue());

        // test that deckung is a composite of vertrag
        CompositeNode compositeNode = (CompositeNode)vertragStructureChildren[1];
        Object[] vertragCompositeChildren = contentProvider.getChildren(compositeNode);
        assertEquals(1, vertragCompositeChildren.length);
        assertEquals(deckung, ((ComponentNode)vertragCompositeChildren[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * 
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link SubtypeNode} should be before the {@link CompositeNode} in the list of
     * {@link ComponentNode} children
     */
    @Test
    public void testComponentNodeChildrenOrder() {
        // setup
        IIpsProject project;
        PolicyCmptType vertrag;
        PolicyCmptType deckung;
        PolicyCmptType hausratVertrag;
        try {
            project = newIpsProject();
            vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
            deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");
            hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        IAssociation associationVertrag2Deckung = vertrag.newAssociation();
        associationVertrag2Deckung.setTarget(deckung.getQualifiedName());
        associationVertrag2Deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // test
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // tests
        ComponentNode vertragNode = (ComponentNode)elements[0];
        assertEquals(vertrag, vertragNode.getValue());
        assertEquals(1, elements.length);

        Object[] vertragChildren = contentProvider.getChildren(elements[0]);
        assertTrue(vertragChildren[0] instanceof SubtypeNode);
        assertTrue(vertragChildren[1] instanceof CompositeNode);
    }
}