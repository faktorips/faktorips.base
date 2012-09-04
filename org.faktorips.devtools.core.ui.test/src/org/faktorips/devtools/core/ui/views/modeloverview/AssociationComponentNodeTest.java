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
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.junit.Test;

public class AssociationComponentNodeTest extends AbstractIpsPluginTest {

    @Test
    public void testNewAssociationComponentNode_NullParentAllowed() throws CoreException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        IAssociation association = vertrag.newAssociation();
        association.setTarget(deckung.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        AssociationComponentNode.newAssociationComponentNode(association, null, project);
    }

    @Test(expected = NullPointerException.class)
    public void testNewAssociationComponentNode_NullProjectNotAllowed() throws CoreException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        IAssociation association = vertrag.newAssociation();
        association.setTarget(deckung.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        AssociationComponentNode.newAssociationComponentNode(association, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNewAssociationComponentNode_NullAssociationNotAllowed() throws CoreException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        IAssociation association = vertrag.newAssociation();
        association.setTarget(deckung.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        AssociationComponentNode.newAssociationComponentNode(null, null, project);
    }

    @Test
    public void testGetValue_IsAssociationTargetValue() throws CoreException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        IAssociation association = vertrag.newAssociation();
        association.setTarget(deckung.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        AssociationComponentNode node = AssociationComponentNode
                .newAssociationComponentNode(association, null, project);

        assertEquals(deckung, node.getValue());
    }

    @Test
    public void testGetAssociation_IsEqual() throws CoreException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        IAssociation association = vertrag.newAssociation();
        association.setTarget(deckung.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        AssociationComponentNode node = AssociationComponentNode
                .newAssociationComponentNode(association, null, project);

        assertEquals(association, node.getAssociation());
    }

    @Test
    public void testEncapsulateAssociationComponentTypes_EmptyListInput() throws CoreException {
        IIpsProject project = newIpsProject();
        List<AssociationComponentNode> encapsulateAssociationComponentTypes = AssociationComponentNode
                .encapsulateAssociationComponentTypes(new ArrayList<IAssociation>(), project);

        assertTrue(encapsulateAssociationComponentTypes.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testEncapsulateAssociationComponentTypes_NullListInput() throws CoreException {
        IIpsProject project = newIpsProject();

        AssociationComponentNode.encapsulateAssociationComponentTypes(null, project);
    }

    @Test
    public void testEncapsulateAssociationComponentTypes_NonEmptyListInput() throws CoreException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        IAssociation association = vertrag.newAssociation();
        association.setTarget(deckung.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        ArrayList<IAssociation> associations = new ArrayList<IAssociation>();
        associations.add(association);

        List<AssociationComponentNode> encapsulatedComponentTypes = AssociationComponentNode
                .encapsulateAssociationComponentTypes(associations, project);

        assertEquals(1, encapsulatedComponentTypes.size());
        assertEquals(association, encapsulatedComponentTypes.get(0).getAssociation());
    }

    @Test(expected = NullPointerException.class)
    public void testEncapsulateAssociationComponentTypes_NullProjectAndNonEmptyListInput() throws CoreException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        IAssociation association = vertrag.newAssociation();
        association.setTarget(deckung.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        ArrayList<IAssociation> associations = new ArrayList<IAssociation>();
        associations.add(association);
        AssociationComponentNode.encapsulateAssociationComponentTypes(associations, null);
    }

    @Test
    public void testEncapsulateAssociationComponentTypes_NullProjectAndEmptyListInput() {
        List<AssociationComponentNode> encapsulateAssociationComponentTypes = AssociationComponentNode
                .encapsulateAssociationComponentTypes(new ArrayList<IAssociation>(), null);
        assertTrue(encapsulateAssociationComponentTypes.isEmpty());
    }
}
