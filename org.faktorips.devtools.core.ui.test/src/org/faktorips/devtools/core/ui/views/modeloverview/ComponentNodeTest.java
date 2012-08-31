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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.junit.Test;

public class ComponentNodeTest extends AbstractIpsPluginTest {

    /*
     * TODO CODE-REVIEW FIPS-1194: ComponentNode enthält noch einiges an Logik, für welches man
     * Unit-Tests (in einer eigenen Klasse ComponentNodeTest) schreiben sollte
     */

    @SuppressWarnings("null")
    @Test
    public void testGetChildren_HasCompositeAndSubtypeNodeWithChildren() throws CoreException {
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

        List<AbstractStructureNode> children = root.getChildren();
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
    public void testGetChildren_AbstractStructureNodeOrder() throws CoreException {
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

        List<AbstractStructureNode> children = root.getChildren();

        // the order of the elements in the list of children is fixed
        AbstractStructureNode subtypeNode = children.get(0);
        AbstractStructureNode compositeNode = children.get(1);

        assertTrue(subtypeNode instanceof SubtypeNode);
        assertTrue(compositeNode instanceof CompositeNode);
    }

    @Test
    public void testGetParent_RootParentIsNull() throws CoreException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        ComponentNode root = new ComponentNode(vertrag, null, project);

        assertNull(root.getParent());
    }

    @SuppressWarnings("null")
    @Test
    public void testGetParent_LeafComponentNodeParentIsSubtypeNode() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");

        // supertypes
        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        ComponentNode root = new ComponentNode(vertrag, null, project);
        List<AbstractStructureNode> children = root.getChildren();
        AbstractStructureNode subtypeNode = null;

        // more complicated logic to initialize subtypeNode to make the test more independent from
        // the testGetChildren_AbstractStructureNodeOrder() test-case
        for (AbstractStructureNode node : children) {
            if (node instanceof SubtypeNode) {
                subtypeNode = node;
            }
        }

        // tests
        assertEquals(subtypeNode, subtypeNode.getChildren().get(0).getParent());
    }

    @SuppressWarnings("null")
    @Test
    public void testGetParent_LeafComponentNodeParentIsCompositeNode() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        // associations
        IAssociation vertrag2deckung = vertrag.newAssociation();
        vertrag2deckung.setTarget(deckung.getQualifiedName());
        vertrag2deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        ComponentNode root = new ComponentNode(vertrag, null, project);
        List<AbstractStructureNode> children = root.getChildren();
        AbstractStructureNode compositeNode = null;

        // more complicated logic to initialize subtypeNode to make the test more independent from
        // the testGetChildren_AbstractStructureNodeOrder() test-case
        for (AbstractStructureNode node : children) {
            if (node instanceof CompositeNode) {
                compositeNode = node;
            }
        }

        // tests
        assertEquals(compositeNode, compositeNode.getChildren().get(0).getParent());
    }

    @Test
    public void testGetCompositeChild_IsNull() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        ComponentNode root = new ComponentNode(vertrag, null, project);

        // tests
        assertNull(root.getCompositeChild());
    }

    @Test
    public void testGetCompositeChild_NotNull() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        // associations
        IAssociation vertrag2deckung = vertrag.newAssociation();
        vertrag2deckung.setTarget(deckung.getQualifiedName());
        vertrag2deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        ComponentNode root = new ComponentNode(vertrag, null, project);
        CompositeNode child = root.getCompositeChild();

        // tests
        assertNotNull(child);
        assertEquals(1, child.getChildren().size());
        assertEquals(deckung, child.getChildren().get(0).getValue());
    }

    @Test
    public void testGetSubtypeChild_IsNull() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        ComponentNode root = new ComponentNode(vertrag, null, project);

        // tests
        assertNull(root.getSubtypeChild());
    }

    @Test
    public void testGetSubtypeChild_NotNull() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");

        // supertypes
        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        ComponentNode root = new ComponentNode(vertrag, null, project);
        SubtypeNode child = root.getSubtypeChild();

        // tests
        assertNotNull(child);
        assertEquals(1, child.getChildren().size());
        assertEquals(hausratVertrag, child.getChildren().get(0).getValue());
    }

    @Test
    public void testEquals_RootsWithoutChildrenAreEqual() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        ComponentNode root1 = new ComponentNode(vertrag, null, project);
        ComponentNode root2 = new ComponentNode(vertrag, null, project);

        // test
        assertTrue(root1.equals(root2));
    }

    @Test
    public void testEquals_RootsWithSameTypesAndDifferentProjectAreNotEqual() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        IIpsProject project2 = newIpsProject();

        ComponentNode root1 = new ComponentNode(vertrag, null, project);
        ComponentNode root2 = new ComponentNode(vertrag, null, project2);

        // test
        assertFalse(root1.equals(root2));
    }

    @Test
    public void testEquals_RootsWithDifferenValuesAreNotEqual() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType vertrag2 = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag2");

        ComponentNode root1 = new ComponentNode(vertrag, null, project);
        ComponentNode root2 = new ComponentNode(vertrag2, null, project);

        // test
        assertFalse(root1.equals(root2));
    }
}
