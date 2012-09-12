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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.views.modeloverview.ModelOverviewContentProvider.ShowTypeState;
import org.faktorips.devtools.core.ui.views.modeloverview.ModelOverviewContentProvider.ToChildAssociationType;
import org.junit.Test;

public class ModelOverviewContentProviderTest extends AbstractIpsPluginTest {

    @Test
    public void testHasChildren_NoChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test
        assertFalse(provider.hasChildren(elements[0]));
    }

    @Test
    public void testHasChildren_CyclicNodeHasNoChildren() throws CoreException {
        // setup
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        IAssociation vertrag2vertrag = vertrag.newAssociation();
        vertrag2vertrag.setTarget(vertrag.getQualifiedName());
        vertrag2vertrag.setAssociationType(AssociationType.AGGREGATION);

        Object[] elements = provider.collectElements(vertrag, new NullProgressMonitor());

        Object[] structureChildren = provider.getChildren(elements[0]);
        Object[] componentChildren = provider.getChildren(structureChildren[0]);
        assertFalse(provider.hasChildren(componentChildren[0]));
    }

    @Test
    public void testGetChildren_PolicyChildrenEmpty() throws CoreException {
        // setup
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        provider.setShowTypeState(ShowTypeState.SHOW_POLICIES);

        IIpsProject project = newIpsProject();
        newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test
        assertNotNull(provider.getChildren(elements[0]));
        assertEquals(0, provider.getChildren(elements[0]).length);
    }

    @Test
    public void testGetChildren_ProductChildrenEmpty() throws CoreException {
        // setup
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        provider.setShowTypeState(ShowTypeState.SHOW_PRODUCTS);

        IIpsProject project = newIpsProject();
        newProductCmptType(project, "TestProductComponentType");

        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test
        assertNotNull(provider.getChildren(elements[0]));
        assertEquals(0, provider.getChildren(elements[0]).length);
    }

    @Test
    public void testGetChildren_DetectOneElementCycle() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IPolicyCmptType selfReferencingVertrag = newPolicyCmptTypeWithoutProductCmptType(project,
                "SelfReferencingVertrag");
        IAssociation association = selfReferencingVertrag.newAssociation();
        association.setTarget(selfReferencingVertrag.getQualifiedName());
        association.setAssociationType(AssociationType.AGGREGATION);

        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // tests
        assertEquals(1, elements.length); // only one root element

        Object[] structureChildren = provider.getChildren(elements[0]);
        assertEquals(1, structureChildren.length); // only one structure child
        assertTrue(structureChildren[0] instanceof CompositeNode);

        Object[] componentChildren = provider.getChildren(structureChildren[0]);
        assertEquals(1, componentChildren.length);
        AssociationComponentNode componentNode = (AssociationComponentNode)componentChildren[0];
        assertEquals(selfReferencingVertrag, componentNode.getValue()); // the self referencing node
                                                                        // has itself as a child!
        assertTrue(componentNode.isRepetition());
        assertEquals(0, provider.getChildren(componentNode).length);
    }

    @Test
    public void testGetChildren_DetectMultiElementCycle() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IPolicyCmptType typeA = newPolicyCmptTypeWithoutProductCmptType(project, "typeA");
        IPolicyCmptType typeB = newPolicyCmptTypeWithoutProductCmptType(project, "typeB");
        IPolicyCmptType typeC = newPolicyCmptTypeWithoutProductCmptType(project, "typeC");

        IAssociation ab = typeA.newAssociation();
        ab.setTarget(typeB.getQualifiedName());
        ab.setAssociationType(AssociationType.AGGREGATION);

        IAssociation bc = typeB.newAssociation();
        bc.setTarget(typeC.getQualifiedName());
        bc.setAssociationType(AssociationType.AGGREGATION);

        IAssociation ac = typeC.newAssociation();
        ac.setTarget(typeA.getQualifiedName());
        ac.setAssociationType(AssociationType.AGGREGATION);

        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        assertEquals(1, elements.length);

        // get the parent element
        Object[] structChildren1 = provider.getChildren(elements[0]);

        // get the first level children
        Object[] componentChildren1 = provider.getChildren(structChildren1[0]);
        Object[] structChildren2 = provider.getChildren(componentChildren1[0]);

        // get the second level children
        Object[] componentChildren2 = provider.getChildren(structChildren2[0]);
        Object[] structChildren3 = provider.getChildren(componentChildren2[0]);

        // get the third level children
        Object[] componentChildren3 = provider.getChildren(structChildren3[0]);

        assertTrue(((ComponentNode)componentChildren3[0]).isRepetition());
        assertEquals(((ComponentNode)elements[0]).getValue(), ((ComponentNode)componentChildren3[0]).getValue());
    }

    @Test
    public void testHasChildren_PolicyHasSubtypeChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");
        subCmptType.setSupertype(cmptType.getQualifiedName());

        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        provider.setShowTypeState(ShowTypeState.SHOW_POLICIES);
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test
        assertTrue(provider.hasChildren(elements[0]));
    }

    @Test
    public void testCollectElements_FindAssociationRootElements() throws CoreException {
        // setup
        // Status of root elements depends only on associations
        IIpsProject project = newIpsProject();

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");

        IAssociation association2 = prodCmptType.newAssociation();
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        provider.setShowTypeState(ShowTypeState.SHOW_PRODUCTS);
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test the number of existing root elements
        assertEquals(1, elements.length);

        // test the identity of the root elements
        // project1
        List<IType> elementList = new ArrayList<IType>();
        elementList.add(((ComponentNode)elements[0]).getValue());
        assertTrue(elementList.contains(prodCmptType));
    }

    @Test
    public void testCollectElements_FindSupertypeRootElements() throws CoreException {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        subCmptType.setSupertype(cmptType.getQualifiedName());

        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        provider.setShowTypeState(ShowTypeState.SHOW_POLICIES);
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test the number of existing root elements
        assertEquals(1, elements.length);

        // test the identity of the root elements
        List<IType> elementList = new ArrayList<IType>();
        elementList.add(((ComponentNode)elements[0]).getValue());
        assertTrue(elementList.contains(cmptType));
    }

    @Test
    public void testHasChildren_HasAssociationChildren() throws CoreException {
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

        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test
        for (Object element : elements) {
            assertTrue(provider.hasChildren(element));
        }
    }

    @Test
    public void testHasChildren_HasAssociationAndSubtypeChildren() throws CoreException {
        // setup
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        provider.setShowTypeState(ShowTypeState.SHOW_POLICIES);

        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        subCmptType.setSupertype(cmptType.getQualifiedName());

        IAssociation association = cmptType.newAssociation();

        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test
        assertEquals(1, elements.length);
        assertTrue(provider.hasChildren(elements[0]));
        assertEquals(2, provider.getChildren(elements[0]).length);
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
     * 
     */
    @Test
    public void testGetChildren_CorrectStructureAndComponentNodeHierarchy() throws CoreException {
        // setup
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();

        IIpsProject project1 = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project1, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project1, "TestPolicyComponentType2");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project1, "TestSubPolicyComponentType");

        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        subCmptType.setSupertype(cmptType.getQualifiedName());

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        Object[] elements = provider.collectElements(project1, new NullProgressMonitor());

        IModelOverviewNode subtypeNode = (IModelOverviewNode)provider.getChildren(elements[0])[0];
        IModelOverviewNode compositeNode = (IModelOverviewNode)provider.getChildren(elements[0])[1];

        // test
        // project1
        assertEquals(2, provider.getChildren(elements[0]).length);

        assertTrue(compositeNode instanceof CompositeNode);
        assertTrue(subtypeNode instanceof SubtypeNode);

        List<ComponentNode> compositeChildren = ((CompositeNode)compositeNode).getChildren();
        assertEquals(1, compositeChildren.size());
        assertEquals(associatedCmptType, compositeChildren.get(0).getValue());

        List<ComponentNode> subtypeChildren = ((SubtypeNode)subtypeNode).getChildren();
        assertEquals(1, subtypeChildren.size());
        assertEquals(subCmptType, subtypeChildren.get(0).getValue());
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
     * 
     */
    @Test
    public void testGetChildren_NoSupertypeAssociations() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");
        PolicyCmptType hausratGrunddeckung = newPolicyCmptTypeWithoutProductCmptType(project, "HausratGrunddeckung");

        hausratVertrag.setSupertype(vertrag.getQualifiedName());
        hausratGrunddeckung.setSupertype(deckung.getQualifiedName());

        IAssociation associationVertrag2Deckung = vertrag.newAssociation();
        associationVertrag2Deckung.setTarget(deckung.getQualifiedName());
        associationVertrag2Deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IAssociation associationHausratVertrag2HausratGrunddeckung = hausratVertrag.newAssociation();
        associationHausratVertrag2HausratGrunddeckung.setTarget(hausratGrunddeckung.getQualifiedName());
        associationHausratVertrag2HausratGrunddeckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // tests
        ComponentNode vertragNode = (ComponentNode)elements[0];
        assertEquals(vertrag, vertragNode.getValue());
        assertEquals(1, elements.length);

        Object[] vertragStructureChildren = provider.getChildren(elements[0]);

        // test that hausratvertrag is a subtype of vertrag
        SubtypeNode subtypeNode = (SubtypeNode)vertragStructureChildren[0];
        Object[] vertragSubtypeChildren = provider.getChildren(subtypeNode);
        assertEquals(1, vertragSubtypeChildren.length);
        ComponentNode hausratVertragNode = (ComponentNode)vertragSubtypeChildren[0];
        assertEquals(hausratVertrag, hausratVertragNode.getValue());

        // test that only hausratgrunddeckung is a composite of hausratvertrag
        Object[] hausratVertragStructureChildren = provider.getChildren(hausratVertragNode);
        CompositeNode hausratVertragCompositeNode = (CompositeNode)hausratVertragStructureChildren[0];
        Object[] hausratVertragCompositeChildren = provider.getChildren(hausratVertragCompositeNode);
        assertEquals(1, hausratVertragCompositeChildren.length);
        assertEquals(hausratGrunddeckung, ((ComponentNode)hausratVertragCompositeChildren[0]).getValue());

        // test that deckung is a composite of vertrag
        CompositeNode compositeNode = (CompositeNode)vertragStructureChildren[1];
        Object[] vertragCompositeChildren = provider.getChildren(compositeNode);
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
     * 
     */
    @Test
    public void testGetChildren_ComponentNodeChildrenOrder() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");

        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        IAssociation associationVertrag2Deckung = vertrag.newAssociation();
        associationVertrag2Deckung.setTarget(deckung.getQualifiedName());
        associationVertrag2Deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // tests
        ComponentNode vertragNode = (ComponentNode)elements[0];
        assertEquals(vertrag, vertragNode.getValue());
        assertEquals(1, elements.length);

        Object[] vertragChildren = provider.getChildren(elements[0]);
        assertTrue(vertragChildren[0] instanceof SubtypeNode);
        assertTrue(vertragChildren[1] instanceof CompositeNode);
    }

    @Test
    public void testCollectElements_InputInstanceofIType() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");
        PolicyCmptType rootPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Root Node");
        PolicyCmptType superPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Super Node");

        // the Master-to-Detail association makes a root node out of rootPolicy
        IAssociation association = rootPolicy.newAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTarget(leafPolicy.getQualifiedName());

        // the Supertype superPolicy is no root node of leafPolicy
        leafPolicy.setSupertype(superPolicy.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.collectElements(leafPolicy, new NullProgressMonitor());
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
     * B is the only root for C
     * 
     */
    @Test
    public void testCollectElements_OmitSupertypeNode() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        ProductCmptType leafPolicy = newProductCmptType(project, "Leaf Node");
        ProductCmptType rootPolicy = newProductCmptType(project, "Root Node");
        ProductCmptType superPolicy = newProductCmptType(project, "Super Node");

        // the Master-to-Detail association makes an indirect root node out of rootPolicy
        IAssociation association = rootPolicy.newAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTarget(leafPolicy.getQualifiedName());

        // the Supertype superPolicy is no root node of leafProduct
        rootPolicy.setSupertype(superPolicy.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.collectElements(leafPolicy, new NullProgressMonitor());
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
     * 
     */
    @Test
    public void testCollectElements_ConsiderSupertypeAssociation() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");
        PolicyCmptType rootPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Root Node");
        PolicyCmptType superPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Super Node");

        // the Master-to-Detail association makes an indirect root node out of rootPolicy
        IAssociation association = rootPolicy.newAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTarget(superPolicy.getQualifiedName());

        // the Supertype superPolicy is no root node of leafPolicy
        leafPolicy.setSupertype(superPolicy.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.collectElements(leafPolicy, new NullProgressMonitor());
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
    public void testCollectElements_SingleElementIsItsOwnRoot() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.collectElements(leafPolicy, new NullProgressMonitor());
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
    public void testCollectElements_OnlyOneRootInASingleHierarchyPath() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        ProductCmptType produkt = newProductCmptType(project, "Produkt");
        ProductCmptType hausratProdukt = newProductCmptType(project, "HausratProdukt");
        ProductCmptType deckungstyp = newProductCmptType(project, "Deckungstyp");
        ProductCmptType hausratGrunddeckungstyp = newProductCmptType(project, "HausratGrunddeckungstyp");

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
        Object[] elements = provider.collectElements(hausratGrunddeckungstyp, new NullProgressMonitor());

        assertEquals(1, elements.length);
        assertEquals(produkt, ((ComponentNode)elements[0]).getValue());

    }

    @Test
    public void testCollectElements_ElementIsItsOwnRootInPureSupertypeHierarchy() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");
        PolicyCmptType superSuperPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Root Node");
        PolicyCmptType superPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Super Node");

        // the Supertype superPolicy is no root node of leafPolicy
        leafPolicy.setSupertype(superPolicy.getQualifiedName());
        superPolicy.setSupertype(superSuperPolicy.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.collectElements(leafPolicy, new NullProgressMonitor());
        assertEquals(1, elements.length);
        assertEquals(leafPolicy, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testCollectElements_ComputeRootToLeafHierarchyPaths() throws CoreException {
        // setup
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");
        PolicyCmptType hausratGrunddeckung = newPolicyCmptTypeWithoutProductCmptType(project, "HausratGrunddeckung");

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
        List<AssociationType> associationTypeFilter = new ArrayList<AssociationType>();
        associationTypeFilter.add(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        associationTypeFilter.add(AssociationType.AGGREGATION);
        Collection<IType> rootCandidatesForIType = provider.getRootElementsForIType(hausratGrunddeckung, componentList,
                ModelOverviewContentProvider.ToChildAssociationType.SELF, new ArrayList<IType>(),
                new ArrayList<List<PathElement>>(), new ArrayList<PathElement>());

        // compute the actual list of root elements and most importantly the list of paths from the
        // root elements to the selected element
        List<List<PathElement>> paths = new ArrayList<List<PathElement>>();
        provider.getRootElementsForIType(hausratGrunddeckung, componentList,
                ModelOverviewContentProvider.ToChildAssociationType.SELF, rootCandidatesForIType, paths,
                new ArrayList<PathElement>());

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

    @Test
    public void testCollectElements_OnIType_DetectCycleOnSelfreferencingELement() throws CoreException {
        // setup
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        IAssociation vertrag2vertrag = vertrag.newAssociation();
        vertrag2vertrag.setTarget(vertrag.getQualifiedName());
        vertrag2vertrag.setAssociationType(AssociationType.AGGREGATION);

        Object[] elements = provider.collectElements(vertrag, new NullProgressMonitor());

        assertEquals(1, elements.length);
        assertEquals(vertrag, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testCollectElements_OnIType_DetectCycleOnIndirectSelfreferencingELement() throws CoreException {
        // setup
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType vertrag2 = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag2");
        PolicyCmptType vertrag3 = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag3");

        IAssociation vertragToVertrag2 = vertrag.newAssociation();
        vertragToVertrag2.setTarget(vertrag2.getQualifiedName());
        vertragToVertrag2.setAssociationType(AssociationType.AGGREGATION);

        IAssociation vertrag2ToVertrag3 = vertrag.newAssociation();
        vertrag2ToVertrag3.setTarget(vertrag3.getQualifiedName());
        vertrag2ToVertrag3.setAssociationType(AssociationType.AGGREGATION);

        IAssociation vertrag3ToVertrag = vertrag.newAssociation();
        vertrag3ToVertrag.setTarget(vertrag.getQualifiedName());
        vertrag3ToVertrag.setAssociationType(AssociationType.AGGREGATION);

        Object[] elements = provider.collectElements(vertrag, new NullProgressMonitor());

        assertEquals(1, elements.length);
        assertEquals(vertrag, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testCollectElements_OnIIpsProject_DetectCycleOnSelfreferencingELement() throws CoreException {
        // setup
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        IAssociation vertrag2vertrag = vertrag.newAssociation();
        vertrag2vertrag.setTarget(vertrag.getQualifiedName());
        vertrag2vertrag.setAssociationType(AssociationType.AGGREGATION);

        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        assertEquals(1, elements.length);
        assertEquals(vertrag, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testCollectElements_OnIIpsProject_DetectCycleOnIndirectSelfreferencingELement() throws CoreException {
        // setup
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType vertrag2 = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag2");
        PolicyCmptType vertrag3 = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag3");

        IAssociation vertragToVertrag2 = vertrag.newAssociation();
        vertragToVertrag2.setTarget(vertrag2.getQualifiedName());
        vertragToVertrag2.setAssociationType(AssociationType.AGGREGATION);

        IAssociation vertrag2ToVertrag3 = vertrag.newAssociation();
        vertrag2ToVertrag3.setTarget(vertrag3.getQualifiedName());
        vertrag2ToVertrag3.setAssociationType(AssociationType.AGGREGATION);

        IAssociation vertrag3ToVertrag = vertrag.newAssociation();
        vertrag3ToVertrag.setTarget(vertrag.getQualifiedName());
        vertrag3ToVertrag.setAssociationType(AssociationType.AGGREGATION);

        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        assertEquals(1, elements.length);
        assertEquals(vertrag, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testGetComponentNodeCompositeChild_IsNull() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        ComponentNode root = new ComponentNode(vertrag, null, project);

        // tests
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        assertNull(provider.getComponentNodeCompositeChild(root));
    }

    @Test
    public void testGetComponentNodeCompositeChild_NotNull() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        // associations
        IAssociation vertrag2deckung = vertrag.newAssociation();
        vertrag2deckung.setTarget(deckung.getQualifiedName());
        vertrag2deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        ComponentNode root = new ComponentNode(vertrag, null, project);
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        CompositeNode child = provider.getComponentNodeCompositeChild(root);

        // tests
        assertNotNull(child);
        assertEquals(1, child.getChildren().size());
        assertEquals(deckung, child.getChildren().get(0).getValue());
    }

    @Test
    public void testGetComponentNodeSubtypeChild_IsNull() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        ComponentNode root = new ComponentNode(vertrag, null, project);

        // tests
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        assertNull(provider.getComponentNodeSubtypeChild(root));
    }

    @Test
    public void testGetComponentNodeSubtypeChild_NotNull() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");

        // supertypes
        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        ComponentNode root = new ComponentNode(vertrag, null, project);
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        SubtypeNode child = provider.getComponentNodeSubtypeChild(root);

        // tests
        assertNotNull(child);
        assertEquals(1, child.getChildren().size());
        assertEquals(hausratVertrag, child.getChildren().get(0).getValue());
    }

    @SuppressWarnings("null")
    @Test
    public void testGetComponentNodeChildren_HasCompositeAndSubtypeNodeWithChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        // supertypes
        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        // associations
        IAssociation vertrag2deckung = vertrag.newAssociation();
        vertrag2deckung.setTarget(deckung.getQualifiedName());
        vertrag2deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // tests
        ComponentNode root = new ComponentNode(vertrag, null, project);

        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        List<AbstractStructureNode> children = provider.getComponentNodeChildren(root);
        assertEquals(2, children.size());

        AbstractStructureNode subtypeNode = null;
        AbstractStructureNode compositeNode = null;
        // more complicated logic to initialize these values to make the test more independent from
        // the testGetChildren_AbstractStructureNodeOrder() test-case

        for (AbstractStructureNode node : children) {
            if (node instanceof SubtypeNode) {
                subtypeNode = node;
            } else if (node instanceof CompositeNode) {
                compositeNode = node;
            }
        }

        assertTrue(subtypeNode instanceof SubtypeNode);
        assertTrue(compositeNode instanceof CompositeNode);

        assertEquals(1, subtypeNode.getChildren().size());
        assertEquals(1, compositeNode.getChildren().size());

        assertEquals(hausratVertrag, subtypeNode.getChildren().get(0).getValue());
        assertEquals(deckung, compositeNode.getChildren().get(0).getValue());
    }

    @Test
    public void testGetComponentNodeChildren_AbstractStructureNodeOrder() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        // supertypes
        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        // associations
        IAssociation vertrag2deckung = vertrag.newAssociation();
        vertrag2deckung.setTarget(deckung.getQualifiedName());
        vertrag2deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // tests
        ComponentNode root = new ComponentNode(vertrag, null, project);
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        List<AbstractStructureNode> children = provider.getComponentNodeChildren(root);

        // the order of the elements in the list of children is fixed
        AbstractStructureNode subtypeNode = children.get(0);
        AbstractStructureNode compositeNode = children.get(1);

        assertTrue(subtypeNode instanceof SubtypeNode);
        assertTrue(compositeNode instanceof CompositeNode);
    }
}
