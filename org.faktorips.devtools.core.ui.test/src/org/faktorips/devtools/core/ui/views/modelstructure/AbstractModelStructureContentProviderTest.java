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

import static org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.getAssociatingTypes;
import static org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.getAssociationsForAssociationTypes;
import static org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.getExistingSupertypeFromList;
import static org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.getProjectITypes;
import static org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.getProjectRootElementsFromComponentList;
import static org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.hasExistingSupertype;
import static org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.isAssociationTarget;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.junit.Test;

public class AbstractModelStructureContentProviderTest extends AbstractIpsPluginTest {

    private final AssociationType[] ASSOCIATION_TYPES = { AssociationType.AGGREGATION,
            AssociationType.COMPOSITION_MASTER_TO_DETAIL };
    private final IpsObjectType[] ipsObjectTypes = { IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE };

    @Test
    public void testGetProjectRootElements_DontFindLocalRootElementsInDistributedProjects() {
        // setup
        IIpsProject superProject = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(superProject, "Vertrag");

        IIpsProject localProject = newIpsProject();
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(localProject, "HausratVertrag");

        // set project references
        IIpsObjectPath path = localProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(superProject);
        localProject.setIpsObjectPath(path);

        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        List<IType> rootComponents = getProjectRootElementsFromComponentList(
                getProjectITypes(localProject, ipsObjectTypes), localProject, new NullProgressMonitor(),
                ASSOCIATION_TYPES);

        assertEquals(1, rootComponents.size());
        assertEquals(vertrag, rootComponents.get(0));
    }

    @Test
    public void testGetProjectRootElements_DetectCycleOnSelfreferencingELement() {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        IAssociation vertrag2vertrag = vertrag.newAssociation();
        vertrag2vertrag.setTarget(vertrag.getQualifiedName());
        vertrag2vertrag.setAssociationType(AssociationType.AGGREGATION);

        List<IType> rootComponents = getProjectRootElementsFromComponentList(getProjectITypes(project, ipsObjectTypes),
                project, new NullProgressMonitor(), ASSOCIATION_TYPES);

        assertEquals(1, rootComponents.size());
        assertEquals(vertrag, rootComponents.get(0));
    }

    @Test
    public void testGetProjectRootElements_DetectCycleOnIndirectSelfreferencingELement() {
        // setup
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

        List<IType> rootComponents = getProjectRootElementsFromComponentList(getProjectITypes(project, ipsObjectTypes),
                project, new NullProgressMonitor(), ASSOCIATION_TYPES);

        assertEquals(1, rootComponents.size());
        assertEquals(vertrag, rootComponents.get(0));
    }

    @Test
    public void testGetProjectRootElements_FindAssociationRootElements() {
        // setup
        // Status of root elements depends only on associations
        IIpsProject project = newIpsProject();

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");

        IAssociation association2 = prodCmptType.newAssociation();
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        List<IType> rootComponents = getProjectRootElementsFromComponentList(getProjectITypes(project, ipsObjectTypes),
                project, new NullProgressMonitor(), ASSOCIATION_TYPES);

        // test the number of existing root elements
        assertEquals(1, rootComponents.size());

        // test the identity of the root elements
        assertTrue(rootComponents.contains(prodCmptType));
    }

    @Test
    public void testGetProjectRootElements_FindSupertypeRootElements() {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        subCmptType.setSupertype(cmptType.getQualifiedName());

        List<IType> rootComponents = getProjectRootElementsFromComponentList(getProjectITypes(project, ipsObjectTypes),
                project, new NullProgressMonitor(), ASSOCIATION_TYPES);

        // test the number of existing root elements
        assertEquals(1, rootComponents.size());

        // test the identity of the root elements
        assertTrue(rootComponents.contains(cmptType));
    }

    /**
     * 
     * 
     * <strong>Scenario:</strong><br>
     * When the root-elements for a project are computed, all referenced projects are included in
     * this process. Therefore it is possible to obtain root-nodes whose descendants do not contain
     * any element of the source-project. Such root elements should be omitted.
     */
    @Test
    public void testGetProjectRootElements_OmitBranchesNotContainingSoureceProjectElementsWithoutAnyHierarchy() {
        // setup
        IIpsProject project = newIpsProject();
        newPolicyCmptTypeWithoutProductCmptType(project, "AnyElement");

        IIpsProject project2 = newIpsProject();
        IType type2 = newPolicyCmptTypeWithoutProductCmptType(project2, "AnyOtherElement");

        // set project references
        IIpsObjectPath path = project2.getIpsObjectPath();
        path.newIpsProjectRefEntry(project);
        project2.setIpsObjectPath(path);

        // test
        List<IType> rootElements = getProjectRootElementsFromComponentList(getProjectITypes(project2, ipsObjectTypes),
                project2, new NullProgressMonitor(), ASSOCIATION_TYPES);
        assertEquals(1, rootElements.size());
        assertEquals(type2, rootElements.get(0));
    }

    @Test
    public void testIsAssociationTarget_Aggregation_IsAssociationTarget() {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        IAssociation cmptType2SubCmptType = cmptType.newAssociation();
        cmptType2SubCmptType.setAssociationType(AssociationType.AGGREGATION);
        cmptType2SubCmptType.setTarget(subCmptType.getQualifiedName());

        // test
        assertTrue(isAssociationTarget(subCmptType, getProjectITypes(project, ipsObjectTypes), ASSOCIATION_TYPES));
    }

    @Test
    public void testIsAssociationTarget_MasterToDetail_IsAssociationTarget() {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        IAssociation cmptType2SubCmptType = cmptType.newAssociation();
        cmptType2SubCmptType.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        cmptType2SubCmptType.setTarget(subCmptType.getQualifiedName());

        // test
        assertTrue(isAssociationTarget(subCmptType, getProjectITypes(project, ipsObjectTypes), ASSOCIATION_TYPES));
    }

    @Test
    public void testIsAssociationTarget_DetailToMaster_IsNoAssociationTarget() {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        IAssociation subCmptType2cmptType = subCmptType.newAssociation();
        subCmptType2cmptType.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        subCmptType2cmptType.setTarget(cmptType.getQualifiedName());

        // test
        assertFalse(isAssociationTarget(subCmptType, getProjectITypes(project, ipsObjectTypes), ASSOCIATION_TYPES));
    }

    @Test
    public void testIsAssociationTarget_Association_IsNoAssociationTarget() {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        IAssociation cmptType2SubCmptType = cmptType.newAssociation();
        cmptType2SubCmptType.setAssociationType(AssociationType.ASSOCIATION);
        cmptType2SubCmptType.setTarget(subCmptType.getQualifiedName());

        // test
        assertFalse(isAssociationTarget(subCmptType, getProjectITypes(project, ipsObjectTypes), ASSOCIATION_TYPES));
    }

    @Test
    public void testIsAssociationTarget_IsNoAssociationTarget() {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        // test
        assertFalse(isAssociationTarget(cmptType, getProjectITypes(project, ipsObjectTypes), ASSOCIATION_TYPES));
    }

    @Test
    public void testGetAsscoiatingTypes_NoAssociatingType() {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        // test
        assertTrue(getAssociatingTypes(cmptType, getProjectITypes(project, ipsObjectTypes)).isEmpty());
    }

    @Test
    public void testGetAsscoiatingTypes_HasAssociatingTypes() {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType associatingType1 = newPolicyCmptTypeWithoutProductCmptType(project, "AssociatingType1");
        IType associatingType2 = newPolicyCmptTypeWithoutProductCmptType(project, "AssociatingType2");
        IType associatingType3 = newPolicyCmptTypeWithoutProductCmptType(project, "AssociatingType3");
        IType associatingType4 = newPolicyCmptTypeWithoutProductCmptType(project, "AssociatingType4");

        IAssociation association1 = associatingType1.newAssociation();
        association1.setTarget(cmptType.getQualifiedName());
        association1.setAssociationType(AssociationType.AGGREGATION);

        IAssociation association2 = associatingType2.newAssociation();
        association2.setTarget(cmptType.getQualifiedName());
        association2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IAssociation association3 = associatingType3.newAssociation();
        association3.setTarget(cmptType.getQualifiedName());
        association3.setAssociationType(AssociationType.ASSOCIATION);

        IAssociation association4 = associatingType4.newAssociation();
        association4.setTarget(cmptType.getQualifiedName());
        association4.setAssociationType(AssociationType.ASSOCIATION);

        List<IType> associatingTypes = getAssociatingTypes(cmptType, getProjectITypes(project, ipsObjectTypes),
                ASSOCIATION_TYPES);

        // test
        assertEquals(2, associatingTypes.size());
        assertTrue(associatingTypes.contains(associatingType1));
        assertTrue(associatingTypes.contains(associatingType2));
    }

    @Test
    public void testGetAsscoiatingTypes_NoAssociatingTypeBetweenDifferentProjects() {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        IIpsProject project2 = newIpsProject();
        IType associatingType1 = newPolicyCmptTypeWithoutProductCmptType(project2, "AssociatingType1");
        IType associatingType2 = newPolicyCmptTypeWithoutProductCmptType(project2, "AssociatingType2");

        // set project references
        IIpsObjectPath path = project2.getIpsObjectPath();
        path.newIpsProjectRefEntry(project);
        project2.setIpsObjectPath(path);

        IAssociation association1 = associatingType1.newAssociation();
        association1.setTarget(cmptType.getQualifiedName());
        association1.setAssociationType(AssociationType.AGGREGATION);

        IAssociation association2 = associatingType2.newAssociation();
        association2.setTarget(cmptType.getQualifiedName());
        association2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        List<IType> associatingTypes = getAssociatingTypes(cmptType, getProjectITypes(project, ipsObjectTypes),
                ASSOCIATION_TYPES);
        // test
        assertTrue(associatingTypes.isEmpty());
    }

    @Test
    public void testGetAsscoiatingTypes_AssociatingTypeBetweenDifferentProjects() {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        IIpsProject project2 = newIpsProject();
        IType associatingType1 = newPolicyCmptTypeWithoutProductCmptType(project2, "AssociatingType1");
        IType associatingType2 = newPolicyCmptTypeWithoutProductCmptType(project2, "AssociatingType2");

        // set project references
        IIpsObjectPath path = project2.getIpsObjectPath();
        path.newIpsProjectRefEntry(project);
        project2.setIpsObjectPath(path);

        IAssociation association1 = associatingType1.newAssociation();
        association1.setTarget(cmptType.getQualifiedName());
        association1.setAssociationType(AssociationType.AGGREGATION);

        IAssociation association2 = associatingType2.newAssociation();
        association2.setTarget(cmptType.getQualifiedName());
        association2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        List<IType> associatingTypes = getAssociatingTypes(cmptType, getProjectITypes(project2, ipsObjectTypes),
                ASSOCIATION_TYPES);
        // test
        assertEquals(2, associatingTypes.size());
        assertTrue(associatingTypes.contains(associatingType1));
        assertTrue(associatingTypes.contains(associatingType2));
    }

    @Test
    public void hasExistingSupertype_HasSupertypeInSingleProject() {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subType = newPolicyCmptTypeWithoutProductCmptType(project, "subTestPolicyComponentType2");

        subType.setSupertype(cmptType.getQualifiedName());

        // test
        assertTrue(hasExistingSupertype(subType, getProjectITypes(project, ipsObjectTypes)));
    }

    @Test
    public void hasExistingSupertype_HasSupertypeFromDifferentProject() {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        IIpsProject referencingProject = newIpsProject();
        IType subType = newPolicyCmptTypeWithoutProductCmptType(referencingProject, "subTestPolicyComponentType2");

        // set project references
        IIpsObjectPath path = referencingProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(project);
        referencingProject.setIpsObjectPath(path);

        subType.setSupertype(cmptType.getQualifiedName());

        // test
        assertTrue(hasExistingSupertype(subType, getProjectITypes(referencingProject, ipsObjectTypes)));
    }

    @Test
    public void hasExistingSupertype_HasNoSupertypeFromDifferentProject() {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        IIpsProject referencingProject = newIpsProject();
        IType subType = newPolicyCmptTypeWithoutProductCmptType(referencingProject, "subTestPolicyComponentType2");

        subType.setSupertype(cmptType.getQualifiedName());

        // the project references have been omitted intentionally to simulate a project with
        // external and not existing supertypes

        // test
        assertFalse(hasExistingSupertype(subType, getProjectITypes(referencingProject, ipsObjectTypes)));
    }

    @Test
    public void hasExistingSupertype_HasNoSupertype() {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        // test
        assertFalse(hasExistingSupertype(cmptType, getProjectITypes(project, ipsObjectTypes)));
    }

    @Test
    public void testGetAssociationsForAssociationTypes_HasAggregationAndMasterToDetailAssociation() {
        // setup
        IIpsProject project = newIpsProject();
        IType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");
        IType hausratGrunddeckung = newPolicyCmptTypeWithoutProductCmptType(project, "HausratGrunddeckung");
        IType hausratZusatzdeckung = newPolicyCmptTypeWithoutProductCmptType(project, "HausratZusatzdeckung");

        IAssociation association1 = hausratVertrag.newAssociation();
        association1.setTarget(hausratGrunddeckung.getQualifiedName());
        association1.setAssociationType(AssociationType.AGGREGATION);

        IAssociation association2 = hausratVertrag.newAssociation();
        association2.setTarget(hausratZusatzdeckung.getQualifiedName());
        association2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        List<IType> associations = getAssociationsForAssociationTypes(hausratVertrag, ASSOCIATION_TYPES);

        // tests
        assertEquals(2, associations.size());
        assertTrue(associations.contains(hausratGrunddeckung));
        assertTrue(associations.contains(hausratZusatzdeckung));
    }

    @Test
    public void testGetAssociationsForAssociationTypes_NoAssociationCausesEmptyList() {
        // setup
        IIpsProject project = newIpsProject();
        IType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");

        List<IType> associations = getAssociationsForAssociationTypes(hausratVertrag, ASSOCIATION_TYPES);

        // test
        assertTrue(associations.isEmpty());
    }

    @Test
    public void testGetExistingSupertypeFromList_FindSupertype() {
        // setup
        IIpsProject project = newIpsProject();
        IType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        IType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");

        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        IType supertype = getExistingSupertypeFromList(hausratVertrag, getProjectITypes(project, ipsObjectTypes));

        // test
        assertNotNull(supertype);
        assertEquals(vertrag, supertype);
    }

    @Test
    public void testGetExistingSupertypeFromList_HasNoSupertype() {
        // setup
        IIpsProject project = newIpsProject();
        IType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        IType supertype = getExistingSupertypeFromList(vertrag, getProjectITypes(project, ipsObjectTypes));

        // test
        assertNull(supertype);
    }

    @Test
    public void testGetExistingSupertypeFromList_HasNoExistingSupertypeFromDifferentProjects() {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        IIpsProject referencingProject = newIpsProject();
        IType subType = newPolicyCmptTypeWithoutProductCmptType(referencingProject, "subTestPolicyComponentType2");

        subType.setSupertype(cmptType.getQualifiedName());

        // the project references have been omitted intentionally to simulate a project with
        // external and not existing supertypes

        // test
        IType supertype = getExistingSupertypeFromList(subType, getProjectITypes(referencingProject, ipsObjectTypes));
        assertNull(supertype);
    }

    @Test
    public void testGetExistingSupertypeFromList_HasExistingSupertypeFromDifferentProjects() {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        IIpsProject referencingProject = newIpsProject();
        IType subType = newPolicyCmptTypeWithoutProductCmptType(referencingProject, "subTestPolicyComponentType2");

        subType.setSupertype(cmptType.getQualifiedName());

        // set project references
        IIpsObjectPath path = referencingProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(project);
        referencingProject.setIpsObjectPath(path);

        // test
        IType supertype = getExistingSupertypeFromList(subType, getProjectITypes(referencingProject, ipsObjectTypes));
        assertEquals(cmptType, supertype);
    }

    @Test
    public void testRemoveDescendants_removeElementsFromInheritanceHierarchyAndKeepSecondRootElement() {
        // setup
        IIpsProject project = newIpsProject();
        IType root = newPolicyCmptTypeWithoutProductCmptType(project, "RootType");
        IType root2 = newPolicyCmptTypeWithoutProductCmptType(project, "RootType2");
        IType descendant = newPolicyCmptTypeWithoutProductCmptType(project, "SubType");

        descendant.setSupertype(root.getQualifiedName());

        List<IType> rootComponents = new ArrayList<>();
        rootComponents.add(root);

        List<IType> rootCandidateComponents = new ArrayList<>();
        rootCandidateComponents.add(root);
        rootCandidateComponents.add(root2);
        rootCandidateComponents.add(descendant);

        List<IType> allComponents = new ArrayList<>();
        allComponents.add(root);
        allComponents.add(root2);
        allComponents.add(descendant);

        AbstractModelStructureContentProvider.removeDescendants(rootCandidateComponents, rootComponents, allComponents,
                ASSOCIATION_TYPES);

        // tests
        assertEquals(1, rootCandidateComponents.size());
        assertTrue(rootCandidateComponents.contains(root2));
    }

    @Test
    public void testRemoveDescendants_removeElementsFromInheritanceHierarchy() {
        // setup
        IIpsProject project = newIpsProject();
        IType root = newPolicyCmptTypeWithoutProductCmptType(project, "RootType");
        IType descendant = newPolicyCmptTypeWithoutProductCmptType(project, "SubType");

        descendant.setSupertype(root.getQualifiedName());

        List<IType> rootComponents = new ArrayList<>();
        rootComponents.add(root);

        List<IType> rootCandidatesComponents = new ArrayList<>();
        rootCandidatesComponents.add(root);
        rootCandidatesComponents.add(descendant);

        List<IType> allComponents = new ArrayList<>();
        allComponents.add(root);
        allComponents.add(descendant);

        AbstractModelStructureContentProvider.removeDescendants(rootCandidatesComponents, rootComponents, allComponents,
                ASSOCIATION_TYPES);

        // tests
        assertEquals(0, rootCandidatesComponents.size());
    }

    @Test
    public void testRemoveDescendants_removeAssociatedElementsAndKeepSecondRootElement() {
        // setup
        IIpsProject project = newIpsProject();
        IType root = newPolicyCmptTypeWithoutProductCmptType(project, "RootType");
        IType root2 = newPolicyCmptTypeWithoutProductCmptType(project, "RootType2");
        IType descendant = newPolicyCmptTypeWithoutProductCmptType(project, "SubType");

        IAssociation association = root.newAssociation();
        association.setTarget(descendant.getQualifiedName());
        association.setAssociationType(AssociationType.AGGREGATION);

        List<IType> rootComponents = new ArrayList<>();
        rootComponents.add(root);

        List<IType> rootCandidateComponents = new ArrayList<>();
        rootCandidateComponents.add(root);
        rootCandidateComponents.add(root2);
        rootCandidateComponents.add(descendant);

        List<IType> allComponents = new ArrayList<>();
        allComponents.add(root);
        allComponents.add(root2);
        allComponents.add(descendant);

        AbstractModelStructureContentProvider.removeDescendants(rootCandidateComponents, rootComponents, allComponents,
                ASSOCIATION_TYPES);

        // tests
        assertEquals(1, rootCandidateComponents.size());
        assertTrue(rootCandidateComponents.contains(root2));
    }

    @Test
    public void testRemoveDescendants_removeAssociatedElements() {
        // setup
        IIpsProject project = newIpsProject();
        IType root = newPolicyCmptTypeWithoutProductCmptType(project, "RootType");
        IType descendant = newPolicyCmptTypeWithoutProductCmptType(project, "SubType");

        IAssociation association = root.newAssociation();
        association.setTarget(descendant.getQualifiedName());
        association.setAssociationType(AssociationType.AGGREGATION);

        List<IType> rootComponents = new ArrayList<>();
        rootComponents.add(root);

        List<IType> rootCandidateComponents = new ArrayList<>();
        rootCandidateComponents.add(root);
        rootCandidateComponents.add(descendant);

        List<IType> allComponents = new ArrayList<>();
        allComponents.add(root);
        allComponents.add(descendant);

        AbstractModelStructureContentProvider.removeDescendants(rootCandidateComponents, rootComponents, allComponents,
                ASSOCIATION_TYPES);

        // tests
        assertEquals(0, rootCandidateComponents.size());
    }

    @Test
    public void testRemoveDescendants_removeAssociatedAndSubtypedElements() {
        // setup
        IIpsProject project = newIpsProject();
        IType root = newPolicyCmptTypeWithoutProductCmptType(project, "RootType");
        IType descendant1 = newPolicyCmptTypeWithoutProductCmptType(project, "SubType1");
        IType descendant2 = newPolicyCmptTypeWithoutProductCmptType(project, "SubType2");
        IType descendant3 = newPolicyCmptTypeWithoutProductCmptType(project, "SubType3");
        IType descendant4 = newPolicyCmptTypeWithoutProductCmptType(project, "SubType4");

        IAssociation association = root.newAssociation();
        association.setTarget(descendant1.getQualifiedName());
        association.setAssociationType(AssociationType.AGGREGATION);
        descendant2.setSupertype(descendant1.getQualifiedName());

        descendant3.setSupertype(root.getQualifiedName());

        IAssociation association2 = descendant3.newAssociation();
        association2.setTarget(descendant4.getQualifiedName());
        association2.setAssociationType(AssociationType.AGGREGATION);

        List<IType> rootComponents = new ArrayList<>();
        rootComponents.add(root);

        List<IType> rootCandidateComponents = new ArrayList<>();
        rootCandidateComponents.add(root);
        rootCandidateComponents.add(descendant1);
        rootCandidateComponents.add(descendant2);
        rootCandidateComponents.add(descendant3);
        rootCandidateComponents.add(descendant4);

        List<IType> allComponents = new ArrayList<>();
        allComponents.add(root);
        allComponents.add(descendant1);
        allComponents.add(descendant2);
        allComponents.add(descendant3);
        allComponents.add(descendant4);

        AbstractModelStructureContentProvider.removeDescendants(rootCandidateComponents, rootComponents, allComponents,
                ASSOCIATION_TYPES);

        // tests
        assertEquals(0, rootCandidateComponents.size());
    }

    @Test
    public void testIsAssociated_DirectAssociation() {
        // setup
        IIpsProject projectA = newIpsProject();
        IType aA = newPolicyCmptTypeWithoutProductCmptType(projectA, "a.A");
        IType aB = newPolicyCmptTypeWithoutProductCmptType(projectA, "a.B");

        IAssociation association = aA.newAssociation();
        association.setTarget(aB.getQualifiedName());
        association.setAssociationType(AssociationType.AGGREGATION);

        // test
        List<IType> types = AbstractModelStructureContentProvider.getProjectITypes(projectA,
                IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE);
        boolean isAssociated = AbstractModelStructureContentProvider.isAssociated(aB, types, types, projectA,
                AssociationType.AGGREGATION, AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertTrue(isAssociated);
    }

    @Test
    public void testIsAssociated_SupertypesAssociationHasAssociatingSubtype() {
        // setup
        IIpsProject projectA = newIpsProject();
        IType aA = newPolicyCmptTypeWithoutProductCmptType(projectA, "a.A");
        IType aB = newPolicyCmptTypeWithoutProductCmptType(projectA, "a.B");

        IIpsProject projectB = newIpsProject();
        IType bA = newPolicyCmptTypeWithoutProductCmptType(projectB, "b.A");
        IType bB = newPolicyCmptTypeWithoutProductCmptType(projectB, "b.B");

        bA.setSupertype(aA.getQualifiedName());
        bB.setSupertype(aB.getQualifiedName());
        IAssociation association = aA.newAssociation();
        association.setTarget(aB.getQualifiedName());
        association.setAssociationType(AssociationType.AGGREGATION);

        // set project dependencies
        IIpsObjectPath path = projectB.getIpsObjectPath();
        path.newIpsProjectRefEntry(projectA);
        projectB.setIpsObjectPath(path);

        // test
        List<IType> allTypes = AbstractModelStructureContentProvider.getProjectITypes(projectB,
                IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE);

        List<IType> projectBTypes = new ArrayList<>();
        projectBTypes.add(bA);
        projectBTypes.add(bB);

        boolean isAssociated = AbstractModelStructureContentProvider.isAssociated(bB, projectBTypes, allTypes, projectB,
                AssociationType.AGGREGATION, AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertTrue(isAssociated);
    }

    @Test
    public void testIsAssociated_AssociationOverTwoInheritanceLevels() {
        // setup
        IIpsProject projectA = newIpsProject();
        IType aA = newPolicyCmptTypeWithoutProductCmptType(projectA, "aA");
        IType aB = newPolicyCmptTypeWithoutProductCmptType(projectA, "aB");

        IIpsProject projectB = newIpsProject();
        IType bA = newPolicyCmptTypeWithoutProductCmptType(projectB, "bA");
        IType bB = newPolicyCmptTypeWithoutProductCmptType(projectB, "bB");

        IIpsProject projectC = newIpsProject();
        IType cA = newPolicyCmptTypeWithoutProductCmptType(projectC, "cA");
        IType cB = newPolicyCmptTypeWithoutProductCmptType(projectC, "cB");

        bA.setSupertype(aA.getQualifiedName());
        bB.setSupertype(aB.getQualifiedName());

        cA.setSupertype(bA.getQualifiedName());
        cB.setSupertype(bB.getQualifiedName());

        IAssociation association = aA.newAssociation();
        association.setTarget(aB.getQualifiedName());
        association.setAssociationType(AssociationType.AGGREGATION);

        // set project dependencies
        IIpsObjectPath path = projectB.getIpsObjectPath();
        path.newIpsProjectRefEntry(projectA);
        projectB.setIpsObjectPath(path);

        // set project dependencies
        IIpsObjectPath path2 = projectC.getIpsObjectPath();
        path2.newIpsProjectRefEntry(projectB);
        projectC.setIpsObjectPath(path2);

        // test
        List<IType> allTypes = AbstractModelStructureContentProvider.getProjectITypes(projectC,
                IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE);

        List<IType> projectCTypes = new ArrayList<>();
        projectCTypes.add(cA);
        projectCTypes.add(cB);

        boolean isAssociated = AbstractModelStructureContentProvider.isAssociated(cB, projectCTypes, allTypes, projectC,
                AssociationType.AGGREGATION, AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertTrue(isAssociated);
    }
}
