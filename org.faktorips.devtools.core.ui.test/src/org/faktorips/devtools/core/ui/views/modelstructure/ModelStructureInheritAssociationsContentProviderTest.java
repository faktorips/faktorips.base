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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.junit.Test;

public class ModelStructureInheritAssociationsContentProviderTest extends AbstractIpsPluginTest {

    @Test
    public void testCollectElements_FindCorrectRootElementInASingleProject() {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType superType = newPolicyCmptTypeWithoutProductCmptType(project, "Supertype");
        PolicyCmptType subType = newPolicyCmptTypeWithoutProductCmptType(project, "Subtype");

        subType.setSupertype(superType.getQualifiedName());

        ModelStructureInheritAssociationsContentProvider provider = new ModelStructureInheritAssociationsContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // tests
        assertEquals(1, elements.length);
        assertTrue(elements[0] instanceof ComponentNode);
        assertEquals(superType, ((ComponentNode)elements[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * Since only elements from a selected project should be shown, the local root elements have to
     * be derived from the hierarchy and associations of the base projects.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * TODO noschinski2 12.09.2012: Explain expected test outcome
     */
    @Test
    public void testCollectElements_FindCorrectRootElementFromTwoProjects() {
        // setup
        IIpsProject baseProject = newIpsProject();
        PolicyCmptType superType = newPolicyCmptTypeWithoutProductCmptType(baseProject, "Supertype");

        IIpsProject customProject = newIpsProject();
        PolicyCmptType subType = newPolicyCmptTypeWithoutProductCmptType(customProject, "Subtype");

        subType.setSupertype(superType.getQualifiedName());

        // set project dependencies
        IIpsObjectPath path = customProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        customProject.setIpsObjectPath(path);

        ModelStructureInheritAssociationsContentProvider provider = new ModelStructureInheritAssociationsContentProvider();
        Object[] elements = provider.collectElements(customProject, new NullProgressMonitor());

        // tests
        assertEquals(1, elements.length);
        assertTrue(elements[0] instanceof ComponentNode);
        assertEquals(subType, ((ComponentNode)elements[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * An {@link IType} can have multiple subtypes. Therefore we have to consider all of them in the
     * computation of the derived root nodes. <br>
     * <strong>Example:</strong><br>
     * Consider two projects {@code p} and {@code q}, and we want to compute the root nodes for
     * project {@code q}<br>
     * If Type {@code p.A} has subtypes {@code q.AA} and {@code q.AB}, both subtypes will be in the
     * set of derived root nodes.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * TODO noschinski2 13.09.2012: Explain expected test outcome
     */
    @Test
    public void testCollectElements_FindMultipleRootElementFromTwoProjectsOnSameSupertype() {
        // setup
        IIpsProject baseProject = newIpsProject();
        PolicyCmptType superType = newPolicyCmptTypeWithoutProductCmptType(baseProject, "Supertype");

        IIpsProject customProject = newIpsProject();
        PolicyCmptType subType1 = newPolicyCmptTypeWithoutProductCmptType(customProject, "Subtype1");
        PolicyCmptType subType2 = newPolicyCmptTypeWithoutProductCmptType(customProject, "Subtype2");

        subType1.setSupertype(superType.getQualifiedName());
        subType2.setSupertype(superType.getQualifiedName());

        // set project dependencies
        IIpsObjectPath path = customProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        customProject.setIpsObjectPath(path);

        ModelStructureInheritAssociationsContentProvider provider = new ModelStructureInheritAssociationsContentProvider();
        Object[] elements = provider.collectElements(customProject, new NullProgressMonitor());

        // tests
        assertEquals(2, elements.length);
        assertEquals(subType1, ((ComponentNode)elements[0]).getValue());
        assertEquals(subType2, ((ComponentNode)elements[1]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * An {@link IType} can have multiple subtypes. Therefore we have to consider all of them in the
     * computation of the derived root nodes. <br>
     * <strong>Example:</strong><br>
     * Consider two projects {@code p} and {@code q}, and we want to compute the root nodes for
     * project {@code q}<br>
     * If Type {@code p.A} has subtypes {@code p.AA} and {@code p.AB}, and {@code p.AA} has subtype
     * {@code q.AAA} and {@code p.AB} has subtype {@code q.ABA}, the subtypes {@code q.AAA} and
     * {@code q.ABA} will be in the set of derived root nodes.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * TODO noschinski2 13.09.2012: Explain expected test outcome
     */
    @Test
    public void testCollectElements_FindMultipleRootElementFromTwoProjectsOnSameLevelButFromDifferentSupertypes()
            {
        // setup
        IIpsProject baseProject = newIpsProject();
        PolicyCmptType superType = newPolicyCmptTypeWithoutProductCmptType(baseProject, "Supertype");
        PolicyCmptType subType1 = newPolicyCmptTypeWithoutProductCmptType(baseProject, "Subtype1");
        PolicyCmptType subType2 = newPolicyCmptTypeWithoutProductCmptType(baseProject, "Subtype2");
        PolicyCmptType nonRootType = newPolicyCmptTypeWithoutProductCmptType(baseProject, "NonRootType");

        subType1.setSupertype(superType.getQualifiedName());
        subType2.setSupertype(superType.getQualifiedName());
        nonRootType.setSupertype(subType1.getQualifiedName()); // for the test of the diversity

        IIpsProject customProject = newIpsProject();
        PolicyCmptType inheritedRootType1 = newPolicyCmptTypeWithoutProductCmptType(customProject,
                "InheritedRootType1");
        PolicyCmptType subInheritedRootType1 = newPolicyCmptTypeWithoutProductCmptType(customProject,
                "SubInheritedRootType1");
        PolicyCmptType inheritedRootType2 = newPolicyCmptTypeWithoutProductCmptType(customProject,
                "InheritedRootType2");

        inheritedRootType1.setSupertype(subType1.getQualifiedName());
        // check that no depth-first search is performed
        subInheritedRootType1.setSupertype(inheritedRootType1.getQualifiedName());
        inheritedRootType2.setSupertype(subType2.getQualifiedName());

        // set project dependencies
        IIpsObjectPath path = customProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        customProject.setIpsObjectPath(path);

        ModelStructureInheritAssociationsContentProvider provider = new ModelStructureInheritAssociationsContentProvider();
        Object[] elements = provider.collectElements(customProject, new NullProgressMonitor());

        // tests
        assertEquals(2, elements.length);
        List<IType> foundRootITypes = new ArrayList<>();
        for (Object element : elements) {
            foundRootITypes.add(((ComponentNode)element).getValue());
        }
        assertTrue(foundRootITypes.contains(inheritedRootType1));
        assertTrue(foundRootITypes.contains(inheritedRootType2));
    }

    @Test
    public void testCollectElements_DoNotIncludeAssociatedElements() {
        // setup
        IIpsProject project1 = newIpsProject();
        IType externalElement = newPolicyCmptTypeWithoutProductCmptType(project1, "ExternalElement");

        IIpsProject project2 = newIpsProject();
        IType root = newPolicyCmptTypeWithoutProductCmptType(project2, "Root");
        IType nonRoot = newPolicyCmptTypeWithoutProductCmptType(project2, "NonRoot");

        nonRoot.setSupertype(externalElement.getQualifiedName());

        IAssociation association = root.newAssociation();
        association.setTarget(nonRoot.getQualifiedName());
        association.setAssociationType(AssociationType.AGGREGATION);

        // set project dependencies
        IIpsObjectPath path = project2.getIpsObjectPath();
        path.newIpsProjectRefEntry(project1);
        project2.setIpsObjectPath(path);

        // tests
        ModelStructureInheritAssociationsContentProvider provider = new ModelStructureInheritAssociationsContentProvider();
        Object[] elements = provider.collectElements(project2, new NullProgressMonitor());
        assertEquals(1, elements.length);
        assertEquals(root, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testCollectElements_RemoveRootNodesWhichAreAlreadyContainedInOtherBranches() {
        // setup
        IIpsProject projectA = newIpsProject();
        IType generalRootA = newPolicyCmptTypeWithoutProductCmptType(projectA, "GeneralRootA");

        IIpsProject projectB = newIpsProject();
        IType generalRootB = newPolicyCmptTypeWithoutProductCmptType(projectB, "GeneralRootB");
        IType nonRootB = newPolicyCmptTypeWithoutProductCmptType(projectB, "NonRootB");

        IIpsProject projectC = newIpsProject();
        IType inheritedRootC = newPolicyCmptTypeWithoutProductCmptType(projectC, "InheritedRootC");
        IType nonRootC = newPolicyCmptTypeWithoutProductCmptType(projectC, "NonRootC");

        nonRootB.setSupertype(generalRootA.getQualifiedName());
        nonRootC.setSupertype(nonRootB.getQualifiedName());
        inheritedRootC.setSupertype(generalRootB.getQualifiedName());

        IAssociation association = generalRootB.newAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTarget(nonRootB.getQualifiedName());

        // set project dependencies
        IIpsObjectPath path = projectB.getIpsObjectPath();
        path.newIpsProjectRefEntry(projectA);
        projectB.setIpsObjectPath(path);

        IIpsObjectPath pathC = projectC.getIpsObjectPath();
        pathC.newIpsProjectRefEntry(projectB);
        projectC.setIpsObjectPath(pathC);

        // tests
        ModelStructureInheritAssociationsContentProvider provider = new ModelStructureInheritAssociationsContentProvider();
        Object[] elements = provider.collectElements(projectC, new NullProgressMonitor());

        assertEquals(1, elements.length);
        assertEquals(inheritedRootC, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testCollectElements_findSingleRootElementAfterAssociations() {
        IIpsProject projectA = newIpsProject();
        IType aA = newPolicyCmptTypeWithoutProductCmptType(projectA, "a.A");
        IType aB = newPolicyCmptTypeWithoutProductCmptType(projectA, "a.B");

        IIpsProject projectB = newIpsProject();
        IType bA = newPolicyCmptTypeWithoutProductCmptType(projectB, "b.A");

        bA.setSupertype(aB.getQualifiedName());

        IAssociation association = aA.newAssociation();
        association.setTarget(aB.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // set project dependencies
        IIpsObjectPath path = projectB.getIpsObjectPath();
        path.newIpsProjectRefEntry(projectA);
        projectB.setIpsObjectPath(path);

        ModelStructureInheritAssociationsContentProvider provider = new ModelStructureInheritAssociationsContentProvider();
        Object[] elements = provider.collectElements(projectB, new NullProgressMonitor());

        // tests
        assertEquals(1, elements.length);
        assertEquals(bA, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testCollectElements_findRootElementsAfterAssociations() {
        IIpsProject projectA = newIpsProject();
        IType aA = newPolicyCmptTypeWithoutProductCmptType(projectA, "aA");
        IType aB = newPolicyCmptTypeWithoutProductCmptType(projectA, "aB");
        IType aC = newPolicyCmptTypeWithoutProductCmptType(projectA, "aC");
        IType aD = newPolicyCmptTypeWithoutProductCmptType(projectA, "aD");

        IIpsProject projectB = newIpsProject();
        IType bA = newPolicyCmptTypeWithoutProductCmptType(projectB, "bA");
        IType bB = newPolicyCmptTypeWithoutProductCmptType(projectB, "bB");

        bA.setSupertype(aB.getQualifiedName());
        aD.setSupertype(aC.getQualifiedName());
        bB.setSupertype(aD.getQualifiedName());

        IAssociation association = aA.newAssociation();
        association.setTarget(aB.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IAssociation association2 = aA.newAssociation();
        association2.setTarget(aC.getQualifiedName());
        association2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // set project dependencies
        IIpsObjectPath path = projectB.getIpsObjectPath();
        path.newIpsProjectRefEntry(projectA);
        projectB.setIpsObjectPath(path);

        ModelStructureInheritAssociationsContentProvider provider = new ModelStructureInheritAssociationsContentProvider();
        Object[] elements = provider.collectElements(projectB, new NullProgressMonitor());

        // tests
        assertEquals(2, elements.length);
        List<IType> list = new ArrayList<>();
        list.add(((ComponentNode)elements[0]).getValue());
        list.add(((ComponentNode)elements[1]).getValue());
        assertTrue(list.contains(bA));
        assertTrue(list.contains(bB));
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

        ModelStructureInheritAssociationsContentProvider provider = new ModelStructureInheritAssociationsContentProvider();

        Object[] children = provider.getChildren(new ComponentNode(typeA, project));

        // test
        assertEquals(1, children.length);
        assertTrue(children[0] instanceof AssociationComponentNode);
        assertEquals(typeB, ((AssociationComponentNode)children[0]).getValue());
        assertEquals(typeA, ((ComponentNode)children[0]).getParent().getValue());
    }

    @Test
    public void testGetChildren_SubtypeChildrenHaveParent() {
        // setup
        IIpsProject project = newIpsProject();
        IType typeA = newPolicyCmptTypeWithoutProductCmptType(project, "typeA");
        IType typeB = newPolicyCmptTypeWithoutProductCmptType(project, "typeB");

        typeB.setSupertype(typeA.getQualifiedName());

        ModelStructureInheritAssociationsContentProvider provider = new ModelStructureInheritAssociationsContentProvider();

        Object[] children = provider.getChildren(new ComponentNode(typeA, project));

        // test
        assertEquals(1, children.length);
        assertTrue(children[0] instanceof SubtypeComponentNode);
        assertEquals(typeB, ((SubtypeComponentNode)children[0]).getValue());
        assertEquals(typeA, ((ComponentNode)children[0]).getParent().getValue());
    }

    @Test
    public void testGetChildren_InheritedAssociationChildrenHaveParent() {
        // setup
        IIpsProject projectA = newIpsProject();
        IType typeAA = newPolicyCmptTypeWithoutProductCmptType(projectA, "typeAA");
        IType typeAB = newPolicyCmptTypeWithoutProductCmptType(projectA, "typeAB");

        IAssociation association = typeAA.newAssociation();
        association.setTarget(typeAB.getQualifiedName());
        association.setAssociationType(AssociationType.AGGREGATION);

        IIpsProject projectB = newIpsProject();
        IType typeBA = newPolicyCmptTypeWithoutProductCmptType(projectB, "typeBA");
        IType typeBB = newPolicyCmptTypeWithoutProductCmptType(projectB, "typeBB");

        typeBA.setSupertype(typeAA.getQualifiedName());
        typeBB.setSupertype(typeAB.getQualifiedName());

        // set project dependencies
        IIpsObjectPath path = projectB.getIpsObjectPath();
        path.newIpsProjectRefEntry(projectA);
        projectB.setIpsObjectPath(path);

        ModelStructureInheritAssociationsContentProvider provider = new ModelStructureInheritAssociationsContentProvider();

        Object[] children = provider.getChildren(new ComponentNode(typeBA, projectB));

        assertEquals(1, children.length);
        assertTrue(children[0] instanceof AssociationComponentNode);
        AssociationComponentNode componentNodeBB = (AssociationComponentNode)children[0];
        assertEquals(typeAB, componentNodeBB.getValue());
        assertEquals(typeBA, componentNodeBB.getParent().getValue());
        assertTrue(componentNodeBB.isInherited());
    }

    @Test
    public void testGetChildren_FindsDerivedAssociations() {

        // setup
        IIpsProject baseProject = newIpsProject();
        PolicyCmptType stdSubCoverageType = newPolicyCmptTypeWithoutProductCmptType(baseProject, "StdSubCoverageType");
        PolicyCmptType stdClauseType = newPolicyCmptTypeWithoutProductCmptType(baseProject, "StdClauseType");
        PolicyCmptType stdDeductibleType = newPolicyCmptTypeWithoutProductCmptType(baseProject, "StdDeductibleType");

        IIpsProject customProject = newIpsProject();
        PolicyCmptType subCoverageType = newPolicyCmptTypeWithoutProductCmptType(customProject, "SubCoverageType");
        PolicyCmptType clauseType = newPolicyCmptTypeWithoutProductCmptType(customProject, "ClauseType");
        PolicyCmptType deductibleType = newPolicyCmptTypeWithoutProductCmptType(customProject, "DeductibleType");

        // set supertypes
        subCoverageType.setSupertype(stdSubCoverageType.getQualifiedName());
        clauseType.setSupertype(stdClauseType.getQualifiedName());
        deductibleType.setSupertype(stdDeductibleType.getQualifiedName());

        // create associations
        IAssociation association1 = stdSubCoverageType.newAssociation();
        association1.setTarget(stdClauseType.getQualifiedName());
        association1.setAssociationType(AssociationType.AGGREGATION);

        IAssociation association2 = stdSubCoverageType.newAssociation();
        association2.setTarget(stdDeductibleType.getQualifiedName());
        association2.setAssociationType(AssociationType.AGGREGATION);

        // set project dependencies
        IIpsObjectPath path = customProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        customProject.setIpsObjectPath(path);

        ModelStructureInheritAssociationsContentProvider provider = new ModelStructureInheritAssociationsContentProvider();

        // tests
        Object[] elements = provider.collectElements(customProject, new NullProgressMonitor());
        assertEquals(1, elements.length);
        assertTrue(elements[0] instanceof ComponentNode);
        // this should be the derived root element from the customProject
        assertEquals(subCoverageType, ((ComponentNode)elements[0]).getValue());

        Object[] associationChildren = provider.getChildren(elements[0]);
        assertEquals(2, associationChildren.length);
        assertTrue(associationChildren[0] instanceof AssociationComponentNode);
        assertTrue(((AssociationComponentNode)associationChildren[0]).isInherited());
        assertTrue(associationChildren[1] instanceof AssociationComponentNode);
        assertTrue(((AssociationComponentNode)associationChildren[1]).isInherited());

        List<IType> associationChildrenList = new ArrayList<>();
        associationChildrenList.add(((ComponentNode)associationChildren[0]).getValue());
        associationChildrenList.add(((ComponentNode)associationChildren[1]).getValue());
        assertTrue(associationChildrenList.contains(stdClauseType));
        assertTrue(associationChildrenList.contains(stdDeductibleType));
    }

    @Test
    public void testGetChildren_DoNoInheritDerivedUnionAssociations() {
        // setup
        IIpsProject baseProject = newIpsProject();
        IType vertrag = newPolicyCmptTypeWithoutProductCmptType(baseProject, "Vertrag");
        IType deckung = newPolicyCmptTypeWithoutProductCmptType(baseProject, "Deckung");

        IAssociation derivedUnionAssociation = vertrag.newAssociation();
        derivedUnionAssociation.setTarget(deckung.getQualifiedName());
        derivedUnionAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        derivedUnionAssociation.setDerivedUnion(true);

        IIpsProject customProject = newIpsProject();
        IType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(customProject, "HausratVertrag");
        IType hausratGrunddeckung = newPolicyCmptTypeWithoutProductCmptType(customProject, "HausratGrunddeckung");

        IAssociation standardAssociation = hausratVertrag.newAssociation();
        standardAssociation.setTarget(hausratGrunddeckung.getQualifiedName());
        standardAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        hausratVertrag.setSupertype(vertrag.getQualifiedName());
        hausratGrunddeckung.setSupertype(deckung.getQualifiedName());

        // set project dependencies
        IIpsObjectPath path = customProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        customProject.setIpsObjectPath(path);

        // test
        AbstractModelStructureContentProvider provider = new ModelStructureInheritAssociationsContentProvider();
        Object[] children = provider.getChildren(new SubtypeComponentNode(hausratVertrag, new ComponentNode(vertrag,
                customProject), customProject));

        assertEquals(1, children.length);
        assertEquals(hausratGrunddeckung, ((ComponentNode)children[0]).getValue());
    }

    @Test
    public void testRemoveImplementedDerivedUnions_nothingToRemove() throws Exception {
        ModelStructureInheritAssociationsContentProvider contentProvider = new ModelStructureInheritAssociationsContentProvider();
        List<AssociationComponentNode> associationNodes = new ArrayList<>();
        AssociationComponentNode node1 = mock(AssociationComponentNode.class);
        associationNodes.add(node1);
        AssociationComponentNode node2 = mock(AssociationComponentNode.class);
        when(node2.isDerivedUnion()).thenReturn(true);
        when(node2.getTargetRoleSingular()).thenReturn("abc123");
        associationNodes.add(node2);
        AssociationComponentNode node3 = mock(AssociationComponentNode.class);
        when(node3.isSubsetOfADerivedUnion()).thenReturn(true);
        when(node3.getSubsettedDerivedUnion()).thenReturn("nononever");
        associationNodes.add(node3);

        contentProvider.removeImplementedDerivedUnions(associationNodes);

        assertEquals(3, associationNodes.size());
        assertThat(associationNodes, hasItem(node1));
        assertThat(associationNodes, hasItem(node2));
        assertThat(associationNodes, hasItem(node3));
    }

    @Test
    public void testRemoveImplementedDerivedUnions() throws Exception {
        ModelStructureInheritAssociationsContentProvider contentProvider = new ModelStructureInheritAssociationsContentProvider();
        List<AssociationComponentNode> associationNodes = new ArrayList<>();
        AssociationComponentNode node1 = mock(AssociationComponentNode.class);
        associationNodes.add(node1);
        AssociationComponentNode node2 = mock(AssociationComponentNode.class);
        when(node2.isDerivedUnion()).thenReturn(true);
        when(node2.getTargetRoleSingular()).thenReturn("abc123");
        associationNodes.add(node2);
        AssociationComponentNode node3 = mock(AssociationComponentNode.class);
        when(node3.isSubsetOfADerivedUnion()).thenReturn(true);
        when(node3.getSubsettedDerivedUnion()).thenReturn("abc123");
        associationNodes.add(node3);

        contentProvider.removeImplementedDerivedUnions(associationNodes);

        assertEquals(2, associationNodes.size());
        assertThat(associationNodes, hasItem(node1));
        assertThat(associationNodes, hasItem(node3));
    }

}
