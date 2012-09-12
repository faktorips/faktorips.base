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

import static org.faktorips.devtools.core.ui.views.modeloverview.AbstractModelOverviewContentProvider.getProjectITypes;
import static org.faktorips.devtools.core.ui.views.modeloverview.AbstractModelOverviewContentProvider.getProjectRootElementsFromComponentList;
import static org.faktorips.devtools.core.ui.views.modeloverview.ModelOverviewInheritAssociationsContentProvider.getProjectSpecificITypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Test;

public class ModelOverviewInheritAssociationContentProviderTest extends AbstractIpsPluginTest {

    private final IpsObjectType[] ipsObjectTypes = { IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE };
    private final AssociationType[] associationTypeFilter = { AssociationType.AGGREGATION,
            AssociationType.COMPOSITION_MASTER_TO_DETAIL };

    @Test
    public void testGetLocalProjectRootElements_FindLocalRootElementsInDistributedProjects() throws CoreException {
        // setup
        IIpsProject superProject = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(superProject, "Vertrag");

        IIpsProject localProject = newIpsProject();
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(localProject, "HausratVertrag");

        // set project dependencies
        IIpsObjectPath path = localProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(superProject);
        localProject.setIpsObjectPath(path);

        hausratVertrag.setSupertype(vertrag.getQualifiedName());
        List<IType> components = getProjectITypes(localProject, ipsObjectTypes);
        List<IType> projectComponents = getProjectSpecificITypes(components, localProject);

        List<IType> rootComponents = getProjectRootElementsFromComponentList(projectComponents, associationTypeFilter);

        // tests
        assertEquals(1, rootComponents.size());
        assertEquals(hausratVertrag, rootComponents.get(0));
    }

    @Test
    public void testCollectElements_FindCorrectRootElementInASingleProject() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType superType = newPolicyCmptTypeWithoutProductCmptType(project, "Supertype");
        PolicyCmptType subType = newPolicyCmptTypeWithoutProductCmptType(project, "Subtype");

        subType.setSupertype(superType.getQualifiedName());

        ModelOverviewInheritAssociationsContentProvider provider = new ModelOverviewInheritAssociationsContentProvider();
        Object[] elements = provider.collectElements(project, new NullProgressMonitor());

        // tests
        assertEquals(1, elements.length);
        assertTrue(elements[0] instanceof ComponentNode);
        assertEquals(superType, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testCollectElements_GetLocalRootElement() throws CoreException {
        // setup
        IIpsProject superProject = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(superProject, "Vertrag");

        IIpsProject localProject = newIpsProject();
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(localProject, "HausratVertrag");

        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        // set project dependencies
        IIpsObjectPath path = localProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(superProject);
        localProject.setIpsObjectPath(path);

        ModelOverviewInheritAssociationsContentProvider provider = new ModelOverviewInheritAssociationsContentProvider();
        Object[] elements = provider.collectElements(localProject, new NullProgressMonitor());

        // tests
        assertEquals(1, elements.length);
        assertTrue(elements[0] instanceof ComponentNode);
        assertEquals(hausratVertrag, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testGetChildren_() throws CoreException {
        // setup
        IIpsProject baseProject = newIpsProject();
        PolicyCmptType stdSubCoverageType = newPolicyCmptType(baseProject, "StdSubCoverageType");
        PolicyCmptType stdClauseType = newPolicyCmptType(baseProject, "StdClauseType");
        PolicyCmptType stdDeductibleType = newPolicyCmptType(baseProject, "StdDeductibleType");

        IIpsProject customProject = newIpsProject();
        PolicyCmptType subCoverageType = newPolicyCmptType(customProject, "StdSubCoverageType");
        PolicyCmptType clauseType = newPolicyCmptType(customProject, "StdClauseType");
        PolicyCmptType deductibleType = newPolicyCmptType(customProject, "StdDeductibleType");

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

        ModelOverviewInheritAssociationsContentProvider provider = new ModelOverviewInheritAssociationsContentProvider();

        // tests
        // Input is of type IpsProject!
        Object[] elements = provider.collectElements(customProject, new NullProgressMonitor());
        assertEquals(1, elements.length);
        assertTrue(elements[0] instanceof ComponentNode);
        assertEquals(subCoverageType, ((ComponentNode)elements[0]).getValue());

        Object[] structureChildren = provider.getChildren(elements[0]);
        assertEquals(1, structureChildren.length);
        assertTrue(structureChildren[0] instanceof CompositeNode);

        Object[] associationChildren = provider.getChildren(structureChildren[0]);
        assertEquals(2, associationChildren.length);
        assertTrue(associationChildren[0] instanceof AssociationComponentNode);
        assertTrue(associationChildren[1] instanceof AssociationComponentNode);

        List<IType> associationChildrenList = new ArrayList<IType>();
        associationChildrenList.add(((AssociationComponentNode)associationChildren[0]).getValue());
        associationChildrenList.add(((AssociationComponentNode)associationChildren[1]).getValue());
        assertTrue(associationChildrenList.contains(clauseType));
        assertTrue(associationChildrenList.contains(deductibleType));
    }
}
