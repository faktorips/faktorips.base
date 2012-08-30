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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.views.modeloverview.ModelOverviewContentProvider.ToChildAssociationType;
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

    @Test
    public void testGetElementsOnIType() {
        // setup
        IIpsProject project;
        PolicyCmptType leafPolicy;
        PolicyCmptType rootPolicy;
        PolicyCmptType superPolicy;
        try {
            project = newIpsProject();
            leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");
            rootPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Root Node");
            superPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Super Node");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        // the Master-to-Detail association makes a root node out of rootPolicy
        IAssociation association = rootPolicy.newAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTarget(leafPolicy.getQualifiedName());

        // the Supertype superPolicy is no root node of leafPolicy
        leafPolicy.setSupertype(superPolicy.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.getElements(leafPolicy);
        assertEquals(1, elements.length);
        assertEquals(rootPolicy, ((ComponentNode)elements[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * An {@link IType} is indirectly targeted by an {@link IAssociation} over the supertype
     * hierarchy: <br />
     * Example: if A supertype B and B -> C, then !A->C
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * B is only root for C
     */
    @Test
    public void testGetElementsInITypeSupertypeHierarchy() {
        // setup
        IIpsProject project;
        ProductCmptType leafPolicy;
        ProductCmptType rootPolicy;
        ProductCmptType superPolicy;
        try {
            project = newIpsProject();
            leafPolicy = newProductCmptType(project, "Leaf Node");
            rootPolicy = newProductCmptType(project, "Root Node");
            superPolicy = newProductCmptType(project, "Super Node");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        // the Master-to-Detail association makes an indirect root node out of rootPolicy
        IAssociation association = rootPolicy.newAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTarget(leafPolicy.getQualifiedName());

        // the Supertype superPolicy is no root node of leafProduct
        rootPolicy.setSupertype(superPolicy.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.getElements(leafPolicy);
        assertEquals(1, elements.length);
        assertEquals(rootPolicy, ((ComponentNode)elements[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * An {@link IType} is indirectly targeted by an {@link IAssociation} over the supertype
     * hierarchy: <br />
     * Example: if A -> B and B supertype of C, then A->C
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * A is the root for object C
     */
    @Test
    public void testGetElementsOnITypeSupertypeAssociation() {
        // setup
        IIpsProject project;
        PolicyCmptType leafPolicy;
        PolicyCmptType rootPolicy;
        PolicyCmptType superPolicy;
        try {
            project = newIpsProject();
            leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");
            rootPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Root Node");
            superPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Super Node");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        // the Master-to-Detail association makes an indirect root node out of rootPolicy
        IAssociation association = rootPolicy.newAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTarget(superPolicy.getQualifiedName());

        // the Supertype superPolicy is no root node of leafPolicy
        leafPolicy.setSupertype(superPolicy.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.getElements(leafPolicy);
        assertEquals(1, elements.length);
        assertEquals(rootPolicy, ((ComponentNode)elements[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * If the selected object is the only object in the hierarchy, it is the root element.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The element is also the root element
     */
    @Test
    public void testGetElementsOnITypeSingleElement() {
        // setup
        IIpsProject project;
        PolicyCmptType leafPolicy;
        try {
            project = newIpsProject();
            leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.getElements(leafPolicy);
        assertEquals(1, elements.length);
        assertEquals(leafPolicy, ((ComponentNode)elements[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * If a child is already a root node, none of its parent nodes should be root. The problem is,
     * that a node can be reached via different paths.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Only the topmost element should be root.
     */
    @Test
    public void testGetElementsOnITypeOnlyOneRoot() {
        // setup
        IIpsProject project;
        ProductCmptType produkt;
        ProductCmptType hausratProdukt;
        ProductCmptType deckungstyp;
        ProductCmptType hausratGrunddeckungstyp;
        try {
            project = newIpsProject();
            produkt = newProductCmptType(project, "Produkt");
            hausratProdukt = newProductCmptType(project, "HausratProdukt");
            deckungstyp = newProductCmptType(project, "Deckungstyp");
            hausratGrunddeckungstyp = newProductCmptType(project, "HausratGrunddeckungstyp");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        // set supertypes
        hausratProdukt.setSupertype(produkt.getQualifiedName());
        hausratGrunddeckungstyp.setSupertype(deckungstyp.getQualifiedName());

        // set associations
        IAssociation deckungstyp2produkt = produkt.newAssociation();
        deckungstyp2produkt.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        deckungstyp2produkt.setTarget(deckungstyp.getQualifiedName());

        IAssociation hausratGrunddeckungstyp2hausratProdukt = hausratProdukt.newAssociation();
        hausratGrunddeckungstyp2hausratProdukt.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        hausratGrunddeckungstyp2hausratProdukt.setTarget(hausratGrunddeckungstyp.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.getElements(hausratGrunddeckungstyp);

        assertEquals(1, elements.length);
        assertEquals(produkt, ((ComponentNode)elements[0]).getValue());

    }

    @Test
    public void testGetElementsOnITypeOnlySupertypeHierarchy() {
        // setup
        IIpsProject project;
        PolicyCmptType leafPolicy;
        PolicyCmptType superPolicy;
        PolicyCmptType superSuperPolicy;
        try {
            project = newIpsProject();
            leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");
            superSuperPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Root Node");
            superPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Super Node");
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        // the Supertype superPolicy is no root node of leafPolicy
        leafPolicy.setSupertype(superPolicy.getQualifiedName());
        superPolicy.setSupertype(superSuperPolicy.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.getElements(leafPolicy);
        assertEquals(1, elements.length);
        assertEquals(leafPolicy, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testGetElementsComputedPaths() {
        // setup
        ModelOverviewContentProvider contenProvider = new ModelOverviewContentProvider();

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

        List<IType> componentList = new ArrayList<IType>();
        componentList.add(vertrag);
        componentList.add(deckung);
        componentList.add(hausratVertrag);
        componentList.add(hausratGrunddeckung);

        hausratVertrag.setSupertype(vertrag.getQualifiedName());
        hausratGrunddeckung.setSupertype(deckung.getQualifiedName());

        IAssociation associationVertrag2Deckung = vertrag.newAssociation();
        associationVertrag2Deckung.setTarget(deckung.getQualifiedName());
        associationVertrag2Deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IAssociation associationHausratVertrag2HausratGrunddeckung = hausratVertrag.newAssociation();
        associationHausratVertrag2HausratGrunddeckung.setTarget(hausratGrunddeckung.getQualifiedName());
        associationHausratVertrag2HausratGrunddeckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // get the rootCandidates
        Collection<IType> rootCandidatesForIType = contenProvider.getRootElementsForIType(hausratGrunddeckung,
                componentList, ModelOverviewContentProvider.ToChildAssociationType.SELF, new ArrayList<IType>(),
                new ArrayList<Deque<PathElement>>(), new ArrayDeque<PathElement>());

        // compute the actual list of root elements and most importantly the list of paths from the
        // root elements to the selected element
        List<Deque<PathElement>> paths = new ArrayList<Deque<PathElement>>();
        contenProvider.getRootElementsForIType(hausratGrunddeckung, componentList,
                ModelOverviewContentProvider.ToChildAssociationType.SELF, rootCandidatesForIType, paths,
                new ArrayDeque<PathElement>());

        // expected paths
        Deque<PathElement> paths1 = new ArrayDeque<PathElement>();
        paths1.push(new PathElement(hausratGrunddeckung, ToChildAssociationType.SELF));
        paths1.push(new PathElement(hausratVertrag, ToChildAssociationType.ASSOCIATION));
        paths1.push(new PathElement(vertrag, ToChildAssociationType.SUPERTYPE));

        Deque<PathElement> paths2 = new ArrayDeque<PathElement>();
        paths1.push(new PathElement(hausratGrunddeckung, ToChildAssociationType.SELF));
        paths1.push(new PathElement(deckung, ToChildAssociationType.SUPERTYPE));
        paths1.push(new PathElement(vertrag, ToChildAssociationType.ASSOCIATION));

        // tests
        assertEquals(2, paths.size());

        paths.contains(paths1);
        paths.contains(paths2);
    }
}