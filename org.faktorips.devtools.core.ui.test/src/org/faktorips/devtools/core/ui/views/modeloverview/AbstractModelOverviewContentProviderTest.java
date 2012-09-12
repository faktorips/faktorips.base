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

import static org.faktorips.devtools.core.ui.views.modeloverview.AbstractModelOverviewContentProvider.getAssociatingTypes;
import static org.faktorips.devtools.core.ui.views.modeloverview.AbstractModelOverviewContentProvider.getAssociationsForAssociationTypes;
import static org.faktorips.devtools.core.ui.views.modeloverview.AbstractModelOverviewContentProvider.getExistingSupertypeFromList;
import static org.faktorips.devtools.core.ui.views.modeloverview.AbstractModelOverviewContentProvider.getProjectITypes;
import static org.faktorips.devtools.core.ui.views.modeloverview.AbstractModelOverviewContentProvider.getProjectRootElementsFromComponentList;
import static org.faktorips.devtools.core.ui.views.modeloverview.AbstractModelOverviewContentProvider.hasExistingSupertype;
import static org.faktorips.devtools.core.ui.views.modeloverview.AbstractModelOverviewContentProvider.isAssociationTarget;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Test;

public class AbstractModelOverviewContentProviderTest extends AbstractIpsPluginTest {

    private final AssociationType[] associationTypes = { AssociationType.AGGREGATION,
            AssociationType.COMPOSITION_MASTER_TO_DETAIL };
    private final IpsObjectType[] ipsObjectTypes = { IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE };

    @Test
    public void testGetProjectRootElements_DontFindLocalRootElementsInDistributedProjects() throws CoreException {
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
                getProjectITypes(localProject, ipsObjectTypes), associationTypes);

        assertEquals(1, rootComponents.size());
        assertEquals(vertrag, rootComponents.get(0));
    }

    @Test
    public void testGetProjectRootElements_DetectCycleOnSelfreferencingELement() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        IAssociation vertrag2vertrag = vertrag.newAssociation();
        vertrag2vertrag.setTarget(vertrag.getQualifiedName());
        vertrag2vertrag.setAssociationType(AssociationType.AGGREGATION);

        List<IType> rootComponents = getProjectRootElementsFromComponentList(getProjectITypes(project, ipsObjectTypes),
                associationTypes);

        assertEquals(1, rootComponents.size());
        assertEquals(vertrag, rootComponents.get(0));
    }

    @Test
    public void testGetProjectRootElements_DetectCycleOnIndirectSelfreferencingELement() throws CoreException {
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
                associationTypes);

        assertEquals(1, rootComponents.size());
        assertEquals(vertrag, rootComponents.get(0));
    }

    @Test
    public void testGetProjectRootElements_FindAssociationRootElements() throws CoreException {
        // setup
        // Status of root elements depends only on associations
        IIpsProject project = newIpsProject();

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");

        IAssociation association2 = prodCmptType.newAssociation();
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        List<IType> rootComponents = getProjectRootElementsFromComponentList(getProjectITypes(project, ipsObjectTypes),
                associationTypes);

        // test the number of existing root elements
        assertEquals(1, rootComponents.size());

        // test the identity of the root elements
        assertTrue(rootComponents.contains(prodCmptType));
    }

    @Test
    public void testGetProjectRootElements_FindSupertypeRootElements() throws CoreException {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        subCmptType.setSupertype(cmptType.getQualifiedName());

        List<IType> rootComponents = getProjectRootElementsFromComponentList(getProjectITypes(project, ipsObjectTypes),
                associationTypes);

        // test the number of existing root elements
        assertEquals(1, rootComponents.size());

        // test the identity of the root elements
        assertTrue(rootComponents.contains(cmptType));
    }

    @Test
    public void testIsAssociationTarget_Aggregation_IsAssociationTarget() throws CoreException {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        IAssociation cmptType2SubCmptType = cmptType.newAssociation();
        cmptType2SubCmptType.setAssociationType(AssociationType.AGGREGATION);
        cmptType2SubCmptType.setTarget(subCmptType.getQualifiedName());

        // test
        assertTrue(isAssociationTarget(subCmptType, getProjectITypes(project, ipsObjectTypes), associationTypes));
    }

    @Test
    public void testIsAssociationTarget_MasterToDetail_IsAssociationTarget() throws CoreException {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        IAssociation cmptType2SubCmptType = cmptType.newAssociation();
        cmptType2SubCmptType.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        cmptType2SubCmptType.setTarget(subCmptType.getQualifiedName());

        // test
        assertTrue(isAssociationTarget(subCmptType, getProjectITypes(project, ipsObjectTypes), associationTypes));
    }

    @Test
    public void testIsAssociationTarget_DetailToMaster_IsNoAssociationTarget() throws CoreException {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        IAssociation subCmptType2cmptType = subCmptType.newAssociation();
        subCmptType2cmptType.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        subCmptType2cmptType.setTarget(cmptType.getQualifiedName());

        // test
        assertFalse(isAssociationTarget(subCmptType, getProjectITypes(project, ipsObjectTypes), associationTypes));
    }

    @Test
    public void testIsAssociationTarget_Association_IsNoAssociationTarget() throws CoreException {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        IAssociation cmptType2SubCmptType = cmptType.newAssociation();
        cmptType2SubCmptType.setAssociationType(AssociationType.ASSOCIATION);
        cmptType2SubCmptType.setTarget(subCmptType.getQualifiedName());

        // test
        assertFalse(isAssociationTarget(subCmptType, getProjectITypes(project, ipsObjectTypes), associationTypes));
    }

    @Test
    public void testIsAssociationTarget_IsNoAssociationTarget() throws CoreException {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        // test
        assertFalse(isAssociationTarget(cmptType, getProjectITypes(project, ipsObjectTypes), associationTypes));
    }

    @Test
    public void testGetAsscoiatingTypes_NoAssociatingType() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        // test
        assertTrue(getAssociatingTypes(cmptType, getProjectITypes(project, ipsObjectTypes)).isEmpty());
    }

    @Test
    public void testGetAsscoiatingTypes_HasAssociatingTypes() throws CoreException {
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
                associationTypes);

        // test
        assertEquals(2, associatingTypes.size());
        assertTrue(associatingTypes.contains(associatingType1));
        assertTrue(associatingTypes.contains(associatingType2));
    }

    @Test
    public void testGetAsscoiatingTypes_NoAssociatingTypeBetweenDifferentProjects() throws CoreException {
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
                associationTypes);
        // test
        assertTrue(associatingTypes.isEmpty());
    }

    @Test
    public void testGetAsscoiatingTypes_AssociatingTypeBetweenDifferentProjects() throws CoreException {
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
                associationTypes);
        // test
        assertEquals(2, associatingTypes.size());
        assertTrue(associatingTypes.contains(associatingType1));
        assertTrue(associatingTypes.contains(associatingType2));
    }

    @Test
    public void hasExistingSupertype_HasSupertypeInSingleProject() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subType = newPolicyCmptTypeWithoutProductCmptType(project, "subTestPolicyComponentType2");

        subType.setSupertype(cmptType.getQualifiedName());

        // test
        assertTrue(hasExistingSupertype(subType, getProjectITypes(project, ipsObjectTypes)));
    }

    @Test
    public void hasExistingSupertype_HasSupertypeFromDifferentProject() throws CoreException {
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
    public void hasExistingSupertype_HasNoSupertypeFromDifferentProject() throws CoreException {
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
    public void hasExistingSupertype_HasNoSupertype() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");

        // test
        assertFalse(hasExistingSupertype(cmptType, getProjectITypes(project, ipsObjectTypes)));
    }

    @Test
    public void testGetAssociationsForAssociationTypes_HasAggregationAndMasterToDetailAssociation()
            throws CoreException {
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

        List<IType> associations = getAssociationsForAssociationTypes(hausratVertrag, associationTypes);

        // tests
        assertEquals(2, associations.size());
        assertTrue(associations.contains(hausratGrunddeckung));
        assertTrue(associations.contains(hausratZusatzdeckung));
    }

    @Test
    public void testGetAssociationsForAssociationTypes_NoAssociationCausesEmptyList() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");

        List<IType> associations = getAssociationsForAssociationTypes(hausratVertrag, associationTypes);

        // test
        assertTrue(associations.isEmpty());
    }

    @Test
    public void getExistingSupertypeFromList_FindSupertype() throws CoreException {
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
    public void getExistingSupertypeFromList_HasNoSupertype() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        IType supertype = getExistingSupertypeFromList(vertrag, getProjectITypes(project, ipsObjectTypes));

        // test
        assertNull(supertype);
    }

    @Test
    public void getExistingSupertypeFromList_HasNoExistingSupertypeFromDifferentProjects() throws CoreException {
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
    public void getExistingSupertypeFromList_HasExistingSupertypeFromDifferentProjects() throws CoreException {
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
}