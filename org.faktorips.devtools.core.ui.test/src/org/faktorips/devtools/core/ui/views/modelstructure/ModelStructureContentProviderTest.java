/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelstructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.ShowTypeState;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.junit.Test;

public class ModelStructureContentProviderTest extends AbstractIpsPluginTest {

    @Test
    public void testHasChildren_NoChildren() {
        // setup
        IIpsProject project = newIpsProject();
        newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test
        assertFalse(provider.hasChildren(elements[0]));
    }

    @Test
    public void testHasChildren_CyclicNodeIsRepeatedOnlyOnce() {
        // setup
        ModelStructureContentProvider provider = new ModelStructureContentProvider();

        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        IAssociation vertrag2vertrag = vertrag.newAssociation();
        vertrag2vertrag.setTarget(vertrag.getQualifiedName());
        vertrag2vertrag.setAssociationType(AssociationType.AGGREGATION);

        Object[] elements = provider.collectElements(vertrag, new NullProgressMonitor());

        assertFalse(provider.hasChildren(provider.getChildren(elements[0])));
    }

    @Test
    public void testGetChildren_PolicyChildrenEmpty() {
        // setup
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        provider.setShowTypeState(ShowTypeState.SHOW_POLICIES);

        IIpsProject project = newIpsProject();
        newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test
        assertNotNull(provider.getChildren(elements[0]));
        assertEquals(0, provider.getChildren(elements[0]).length);
    }

    @Test
    public void testGetChildren_ProductChildrenEmpty() {
        // setup
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        provider.setShowTypeState(ShowTypeState.SHOW_PRODUCTS);

        IIpsProject project = newIpsProject();
        newProductCmptType(project, "TestProductComponentType");

        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test
        assertNotNull(provider.getChildren(elements[0]));
        assertEquals(0, provider.getChildren(elements[0]).length);
    }

    @Test
    public void testGetChildren_DetectOneElementCycle() {
        // setup
        IIpsProject project = newIpsProject();
        IPolicyCmptType selfReferencingVertrag = newPolicyCmptTypeWithoutProductCmptType(project,
                "SelfReferencingVertrag");
        IAssociation association = selfReferencingVertrag.newAssociation();
        association.setTarget(selfReferencingVertrag.getQualifiedName());
        association.setAssociationType(AssociationType.AGGREGATION);

        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // tests
        assertEquals(1, elements.length); // only one root element

        Object[] children = provider.getChildren(elements[0]);
        assertEquals(1, children.length); // only one structure child

        AssociationComponentNode componentNode = (AssociationComponentNode)children[0];
        assertEquals(selfReferencingVertrag, componentNode.getValue()); // the self referencing node
                                                                        // has itself as a child!
        assertTrue(componentNode.isRepetition());
        assertEquals(0, provider.getChildren(componentNode).length);
    }

    @Test
    public void testGetChildren_DetectMultiElementCycle() {
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

        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        assertEquals(1, elements.length);

        Object[] childrenLevel1 = provider.getChildren(elements[0]);
        Object[] childrenLevel2 = provider.getChildren(childrenLevel1[0]);
        Object[] childrenLevel3 = provider.getChildren(childrenLevel2[0]);

        assertTrue(((ComponentNode)childrenLevel3[0]).isRepetition());
        assertEquals(((ComponentNode)elements[0]).getValue(), ((ComponentNode)childrenLevel3[0]).getValue());
    }

    @Test
    public void testGetChildren_SubtypeChildrenHaveParent() {
        // setup
        IIpsProject project = newIpsProject();
        IType typeA = newPolicyCmptTypeWithoutProductCmptType(project, "typeA");
        IType typeB = newPolicyCmptTypeWithoutProductCmptType(project, "typeB");

        typeB.setSupertype(typeA.getQualifiedName());

        ModelStructureContentProvider provider = new ModelStructureContentProvider();

        Object[] children = provider.getChildren(new ComponentNode(typeA, project));

        // test
        assertEquals(1, children.length);
        assertTrue(children[0] instanceof SubtypeComponentNode);
        assertEquals(typeB, ((SubtypeComponentNode)children[0]).getValue());
        assertEquals(typeA, ((ComponentNode)children[0]).getParent().getValue());
    }

    @Test
    public void testGetChildren_HasInheritedAssociations() {
        // setup
        IIpsProject projectA = newIpsProject();
        IType typeAA = newPolicyCmptTypeWithoutProductCmptType(projectA, "aA");
        IType typeAB = newPolicyCmptTypeWithoutProductCmptType(projectA, "aB");

        IIpsProject projectB = newIpsProject();
        IType typeBA = newPolicyCmptTypeWithoutProductCmptType(projectB, "bA");
        IType typeBB = newPolicyCmptTypeWithoutProductCmptType(projectB, "bB");

        typeBA.setSupertype(typeAA.getQualifiedName());
        typeBB.setSupertype(typeAB.getQualifiedName());

        IAssociation association = typeAA.newAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTarget(typeAB.getQualifiedName());

        // set project dependencies
        IIpsObjectPath path = projectB.getIpsObjectPath();
        path.newIpsProjectRefEntry(projectA);
        projectB.setIpsObjectPath(path);

        // test
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        Object[] children = provider.getChildren(new ComponentNode(typeAB, projectB));
        assertTrue(((ComponentNode)children[0]).isTargetOfInheritedAssociation());
    }

    @Test
    public void testGetChildren_HasNoInheritedAssociations() {
        // setup
        IIpsProject projectA = newIpsProject();
        IType typeAA = newPolicyCmptTypeWithoutProductCmptType(projectA, "aA");
        IType typeAB = newPolicyCmptTypeWithoutProductCmptType(projectA, "aB");

        IIpsProject projectB = newIpsProject();
        IType typeBA = newPolicyCmptTypeWithoutProductCmptType(projectB, "bA");
        IType typeBB = newPolicyCmptTypeWithoutProductCmptType(projectB, "bB");

        typeBA.setSupertype(typeAA.getQualifiedName());
        typeBB.setSupertype(typeAB.getQualifiedName());

        // set project dependencies
        IIpsObjectPath path = projectB.getIpsObjectPath();
        path.newIpsProjectRefEntry(projectA);
        projectB.setIpsObjectPath(path);

        // test
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        Object[] children = provider.getChildren(new ComponentNode(typeAB, projectB));
        assertFalse(((ComponentNode)children[0]).isTargetOfInheritedAssociation());
    }

    @Test
    public void testGetChildren_AssociationChildrenHaveParent() {
        // setup
        IIpsProject project = newIpsProject();
        IType typeA = newPolicyCmptTypeWithoutProductCmptType(project, "typeA");
        IType typeB = newPolicyCmptTypeWithoutProductCmptType(project, "typeB");

        IAssociation association = typeA.newAssociation();
        association.setTarget(typeB.getQualifiedName());
        association.setAssociationType(AssociationType.AGGREGATION);

        ModelStructureContentProvider provider = new ModelStructureContentProvider();

        Object[] children = provider.getChildren(new ComponentNode(typeA, project));

        // test
        assertEquals(1, children.length);
        assertTrue(children[0] instanceof AssociationComponentNode);
        assertEquals(typeB, ((AssociationComponentNode)children[0]).getValue());
        assertEquals(typeA, ((ComponentNode)children[0]).getParent().getValue());
    }

    @Test
    public void testHasChildren_PolicyHasSubtypeChildren() {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");
        subCmptType.setSupertype(cmptType.getQualifiedName());

        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        provider.setShowTypeState(ShowTypeState.SHOW_POLICIES);
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test
        assertTrue(provider.hasChildren(elements[0]));
    }

    @Test
    public void testCollectElements_FindAssociationRootElements() {
        // setup
        // Status of root elements depends only on associations
        IIpsProject project = newIpsProject();

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");

        IAssociation association2 = prodCmptType.newAssociation();
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        provider.setShowTypeState(ShowTypeState.SHOW_PRODUCTS);
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test the number of existing root elements
        assertEquals(1, elements.length);

        // test the identity of the root elements
        // project1
        List<IType> elementList = new ArrayList<>();
        elementList.add(((ComponentNode)elements[0]).getValue());
        assertTrue(elementList.contains(prodCmptType));
    }

    @Test
    public void testCollectElements_FindSupertypeRootElements() {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        subCmptType.setSupertype(cmptType.getQualifiedName());

        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        provider.setShowTypeState(ShowTypeState.SHOW_POLICIES);
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test the number of existing root elements
        assertEquals(1, elements.length);

        // test the identity of the root elements
        List<IType> elementList = new ArrayList<>();
        elementList.add(((ComponentNode)elements[0]).getValue());
        assertTrue(elementList.contains(cmptType));
    }

    @Test
    public void testHasChildren_HasAssociationChildren() {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");

        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // test
        assertTrue(provider.hasChildren(elements[0]));
    }

    @Test
    public void testHasChildren_HasAssociationAndSubtypeChildren() {
        // setup
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
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
    public void testGetChildren_NoSupertypeAssociations() {
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

        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // tests
        ComponentNode vertragNode = (ComponentNode)elements[0];
        assertEquals(vertrag, vertragNode.getValue());
        assertEquals(1, elements.length);

        Object[] vertragChildren = provider.getChildren(elements[0]);

        assertEquals(2, vertragChildren.length);

        // test that hausratvertrag is a subtype of vertrag
        ComponentNode hausratVertragNode = (SubtypeComponentNode)vertragChildren[0];
        assertEquals(hausratVertrag, hausratVertragNode.getValue());

        // test that only hausratgrunddeckung is a composite of hausratvertrag
        Object[] hausratVertragChildren = provider.getChildren(hausratVertragNode);
        assertEquals(1, hausratVertragChildren.length);
        assertEquals(hausratGrunddeckung, ((ComponentNode)hausratVertragChildren[0]).getValue());

        // test that deckung is a composite of vertrag
        ComponentNode deckungNode = (AssociationComponentNode)vertragChildren[1];
        assertEquals(deckung, deckungNode.getValue());
    }

    @Test
    public void testGetChildren_ComponentNodeChildrenOrder() {
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
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // tests
        ComponentNode vertragNode = (ComponentNode)elements[0];
        assertEquals(vertrag, vertragNode.getValue());
        assertEquals(1, elements.length);

        Object[] vertragChildren = provider.getChildren(elements[0]);
        assertTrue(vertragChildren[0] instanceof SubtypeComponentNode);
        assertTrue(vertragChildren[1] instanceof AssociationComponentNode);
    }

    @Test
    public void testCollectElements_InputInstanceofIType() {
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
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        Object[] elements = provider.collectElements(leafPolicy, new NullProgressMonitor());
        assertEquals(1, elements.length);
        assertEquals(rootPolicy, ((ComponentNode)elements[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * An {@link IType} is indirectly targeted by an {@link IAssociation} over the supertype
     * hierarchy: <br>
     * Example: if A supertype B and B&rarr;C, then !A&rarr;C
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * B is the only root for C
     * 
     */
    @Test
    public void testCollectElements_OmitSupertypeNode() {
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
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        Object[] elements = provider.collectElements(leafPolicy, new NullProgressMonitor());
        assertEquals(1, elements.length);
        assertEquals(rootPolicy, ((ComponentNode)elements[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * An {@link IType} is indirectly targeted by an {@link IAssociation} over the supertype
     * hierarchy: <br>
     * Example: if A &rarr; B and B supertype of C, then A&rarr;C
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * A is the root for object C
     * 
     */
    @Test
    public void testCollectElements_ConsiderSupertypeAssociation() {
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
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
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
    public void testCollectElements_SingleElementIsItsOwnRoot() {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");

        // test
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
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
    public void testCollectElements_OnlyOneRootInASingleHierarchyPath() {
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
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        Object[] elements = provider.collectElements(hausratGrunddeckungstyp, new NullProgressMonitor());

        assertEquals(1, elements.length);
        assertEquals(produkt, ((ComponentNode)elements[0]).getValue());

    }

    @Test
    public void testCollectElements_ElementIsItsOwnRootInPureSupertypeHierarchy() {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");
        PolicyCmptType superSuperPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Root Node");
        PolicyCmptType superPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Super Node");

        // the Supertype superPolicy is no root node of leafPolicy
        leafPolicy.setSupertype(superPolicy.getQualifiedName());
        superPolicy.setSupertype(superSuperPolicy.getQualifiedName());

        // test
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        Object[] elements = provider.collectElements(leafPolicy, new NullProgressMonitor());
        assertEquals(1, elements.length);
        assertEquals(leafPolicy, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testCollectElements_OnIType_DetectCycleOnSelfreferencingELement() {
        // setup
        ModelStructureContentProvider provider = new ModelStructureContentProvider();

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
    public void testCollectElements_OnIType_DetectCycleOnIndirectSelfreferencingELement() {
        // setup
        ModelStructureContentProvider provider = new ModelStructureContentProvider();

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
    public void testCollectElements_OnIIpsProject_DetectCycleOnSelfreferencingELement() {
        // setup
        ModelStructureContentProvider provider = new ModelStructureContentProvider();

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
    public void testCollectElements_OnIIpsProject_DetectCycleOnIndirectSelfreferencingELement() {
        // setup
        ModelStructureContentProvider provider = new ModelStructureContentProvider();

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
    public void testGetComponentNodeCompositeChild_IsNull() {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        ComponentNode root = new ComponentNode(vertrag, project);

        // tests
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        assertNull(provider.getComponentNodeAssociationChildren(root));
    }

    @Test
    public void testGetComponentNodeCompositeChild_NotNull() {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        // associations
        IAssociation vertrag2deckung = vertrag.newAssociation();
        vertrag2deckung.setTarget(deckung.getQualifiedName());
        vertrag2deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        ComponentNode root = new ComponentNode(vertrag, project);
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        List<AssociationComponentNode> children = provider.getComponentNodeAssociationChildren(root);

        // tests
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals(deckung, children.get(0).getValue());
    }

    @Test
    public void testGetComponentNodeSubtypeChildren_NotNull() {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");

        // supertypes
        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        ComponentNode root = new ComponentNode(vertrag, project);
        ModelStructureContentProvider provider = new ModelStructureContentProvider();
        List<SubtypeComponentNode> child = provider.getComponentNodeSubtypeChildren(root);

        // tests
        assertNotNull(child);
        assertEquals(1, child.size());
        assertEquals(hausratVertrag, child.get(0).getValue());
    }

}
