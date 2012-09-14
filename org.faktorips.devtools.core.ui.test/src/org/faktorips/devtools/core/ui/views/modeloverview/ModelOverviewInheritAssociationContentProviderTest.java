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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Test;

public class ModelOverviewInheritAssociationContentProviderTest extends AbstractIpsPluginTest {

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
    public void testCollectElements_FindCorrectRootElementFromTwoProjects() throws CoreException {
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

        ModelOverviewInheritAssociationsContentProvider provider = new ModelOverviewInheritAssociationsContentProvider();
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
     * computation of the derived root nodes. <br/>
     * <strong>Example:</strong><br/>
     * Consider two projects {@code p} and {@code q}, and we want to compute the root nodes for
     * project {@code q}<br/>
     * If Type {@code p.A} has subtypes {@code q.AA} and {@code q.AB}, both subtypes will be in the
     * set of derived root nodes.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * TODO noschinski2 13.09.2012: Explain expected test outcome
     */
    @Test
    public void testCollectElements_FindMultipleRootElementFromTwoProjectsOnSameSupertype() throws CoreException {
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

        ModelOverviewInheritAssociationsContentProvider provider = new ModelOverviewInheritAssociationsContentProvider();
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
     * computation of the derived root nodes. <br/>
     * <strong>Example:</strong><br/>
     * Consider two projects {@code p} and {@code q}, and we want to compute the root nodes for
     * project {@code q}<br/>
     * If Type {@code p.A} has subtypes {@code p.AA} and {@code p.AB}, and {@code p.AA} has subtype
     * {@code q.AAA} and {@code p.AB} has subtype {@code q.ABA}, the subtypes {@code q.AAA} and
     * {@code q.ABA} will be in the set of derived root nodes.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * TODO noschinski2 13.09.2012: Explain expected test outcome
     */
    @Test
    public void testCollectElements_FindMultipleRootElementFromTwoProjectsOnSameLevelButFromDifferentSupertypes()
            throws CoreException {
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
        PolicyCmptType inheritedRootType1 = newPolicyCmptTypeWithoutProductCmptType(customProject, "InheritedRootType1");
        PolicyCmptType subInheritedRootType1 = newPolicyCmptTypeWithoutProductCmptType(customProject,
                "SubInheritedRootType1");
        PolicyCmptType inheritedRootType2 = newPolicyCmptTypeWithoutProductCmptType(customProject, "InheritedRootType2");

        inheritedRootType1.setSupertype(subType1.getQualifiedName());
        // check that no depth-first search is performed
        subInheritedRootType1.setSupertype(inheritedRootType1.getQualifiedName());
        inheritedRootType2.setSupertype(subType2.getQualifiedName());

        // set project dependencies
        IIpsObjectPath path = customProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        customProject.setIpsObjectPath(path);

        ModelOverviewInheritAssociationsContentProvider provider = new ModelOverviewInheritAssociationsContentProvider();
        Object[] elements = provider.collectElements(customProject, new NullProgressMonitor());

        // tests
        assertEquals(2, elements.length);
        List<IType> foundRootITypes = new ArrayList<IType>();
        for (Object element : elements) {
            foundRootITypes.add(((ComponentNode)element).getValue());
        }
        assertTrue(foundRootITypes.contains(inheritedRootType1));
        assertTrue(foundRootITypes.contains(inheritedRootType2));
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * When different subtypes of the desired project are located in different branches, only the
     * element from the highest level should be taken as root element. <strong>Example:</strong><br>
     * Projects: {@code p} and {@code q} <br>
     * The node hierarchy is indicated by the node names<br>
     * Nodes: {@code p.A}, {@code p.AA}, {@code p.AB}, {@code p.AAA}, {@code q.ABA}, {@code q.AAAA}<br>
     * 
     * The inherited root node is {@code q.ABA}
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * TODO noschinski2 13.09.2012: Explain expected test outcome
     */
    @Test
    public void testCollectElements_FindHighestRootElementFromTwoProjects() throws CoreException {
        // setup
        IIpsProject baseProject = newIpsProject();
        PolicyCmptType superType = newPolicyCmptTypeWithoutProductCmptType(baseProject, "Supertype");
        PolicyCmptType subTypeLevelOne1 = newPolicyCmptTypeWithoutProductCmptType(baseProject, "SubtypeLevelOne1");
        PolicyCmptType subTypeLevelOne2 = newPolicyCmptTypeWithoutProductCmptType(baseProject, "SubtypeLevelOne2");
        PolicyCmptType subTypeLevelTwo = newPolicyCmptTypeWithoutProductCmptType(baseProject, "SubtypeLevelTwo");

        subTypeLevelOne1.setSupertype(superType.getQualifiedName());
        subTypeLevelOne2.setSupertype(superType.getQualifiedName());
        subTypeLevelTwo.setSupertype(subTypeLevelOne1.getQualifiedName());

        IIpsProject customProject = newIpsProject();
        PolicyCmptType inheritedRootType = newPolicyCmptTypeWithoutProductCmptType(customProject, "InheritedRootType");
        PolicyCmptType notInheritedRootType = newPolicyCmptTypeWithoutProductCmptType(customProject,
                "NotInheritedRootType");

        inheritedRootType.setSupertype(subTypeLevelOne2.getQualifiedName());
        notInheritedRootType.setSupertype(subTypeLevelTwo.getQualifiedName());

        // set project dependencies
        IIpsObjectPath path = customProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        customProject.setIpsObjectPath(path);

        ModelOverviewInheritAssociationsContentProvider provider = new ModelOverviewInheritAssociationsContentProvider();
        Object[] elements = provider.collectElements(customProject, new NullProgressMonitor());

        // tests
        assertEquals(1, elements.length);
        assertEquals(inheritedRootType, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testGetChildren_() throws CoreException {
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

        ModelOverviewInheritAssociationsContentProvider provider = new ModelOverviewInheritAssociationsContentProvider();

        // tests
        Object[] elements = provider.collectElements(customProject, new NullProgressMonitor());
        assertEquals(1, elements.length);
        assertTrue(elements[0] instanceof ComponentNode);
        // this should be the derived root element from the customProject
        assertEquals(subCoverageType, ((ComponentNode)elements[0]).getValue());

        Object[] structureChildren = provider.getChildren(elements[0]);
        assertEquals(1, structureChildren.length);
        assertTrue(structureChildren[0] instanceof CompositeNode);

        Object[] associationChildren = provider.getChildren(structureChildren[0]);
        assertEquals(2, associationChildren.length);
        assertTrue(associationChildren[0] instanceof InheritedAssociationComponentNode);
        assertTrue(associationChildren[1] instanceof InheritedAssociationComponentNode);

        List<IType> associationChildrenList = new ArrayList<IType>();
        associationChildrenList.add(((ComponentNode)associationChildren[0]).getValue());
        associationChildrenList.add(((ComponentNode)associationChildren[1]).getValue());
        assertTrue(associationChildrenList.contains(clauseType));
        assertTrue(associationChildrenList.contains(deductibleType));
    }

}
