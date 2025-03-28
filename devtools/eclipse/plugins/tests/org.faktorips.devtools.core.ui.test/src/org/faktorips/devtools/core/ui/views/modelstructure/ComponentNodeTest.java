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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IType;
import org.junit.Test;

public class ComponentNodeTest extends AbstractIpsPluginTest {

    @Test
    public void testGetParent_RootParentIsNull() {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        ComponentNode root = new ComponentNode(vertrag, project);

        assertNull(root.getParent());
    }

    @Test
    public void testEquals_RootsWithoutChildrenAreEqual() {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");

        ComponentNode root1 = new ComponentNode(vertrag, project);
        ComponentNode root2 = new ComponentNode(vertrag, project);

        // test
        assertTrue(root1.equals(root2));
    }

    @Test
    public void testEquals_RootsWithSameTypesAndDifferentProjectAreNotEqual() {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        IIpsProject project2 = newIpsProject();

        ComponentNode root1 = new ComponentNode(vertrag, project);
        ComponentNode root2 = new ComponentNode(vertrag, project2);

        // test
        assertFalse(root1.equals(root2));
    }

    @Test
    public void testEquals_RootsWithDifferenValuesAreNotEqual() {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType vertrag2 = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag2");

        ComponentNode root1 = new ComponentNode(vertrag, project);
        ComponentNode root2 = new ComponentNode(vertrag2, project);

        // test
        assertFalse(root1.equals(root2));
    }

    @Test
    public void testEncapsulateComponentTypes_EmptyListInput() {
        IIpsProject project = newIpsProject();
        List<ComponentNode> encapsulateComponentTypes = ComponentNode.encapsulateComponentTypes(new ArrayList<>(),
                null, project);

        assertTrue(encapsulateComponentTypes.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testEncapsulateComponentTypes_NullListInput() {
        IIpsProject project = newIpsProject();

        ComponentNode.encapsulateComponentTypes(null, null, project);
    }

    @Test
    public void testEncapsulateComponentTypes_NonEmptyListInput() {
        IIpsProject project = newIpsProject();
        ArrayList<IType> components = new ArrayList<>();
        PolicyCmptType type = newPolicyCmptTypeWithoutProductCmptType(project, "Component");
        components.add(type);
        List<ComponentNode> encapsulatedComponentTypes = ComponentNode.encapsulateComponentTypes(components, null,
                project);

        assertEquals(1, encapsulatedComponentTypes.size());
        assertEquals(type, encapsulatedComponentTypes.get(0).getValue());
    }

    @Test(expected = NullPointerException.class)
    public void testEncapsulateComponentTypes_NullProjectAndNonEmptyListInput() {
        IIpsProject project = newIpsProject();
        ArrayList<IType> components = new ArrayList<>();
        components.add(newPolicyCmptTypeWithoutProductCmptType(project, "Component"));

        ComponentNode.encapsulateComponentTypes(components, null, null);
    }

    @Test
    public void testEncapsulateComponentTypes_NullProjectAndEmptyListInput() {
        List<ComponentNode> encapsulateComponentTypes = ComponentNode.encapsulateComponentTypes(new ArrayList<>(),
                null, null);
        assertTrue(encapsulateComponentTypes.isEmpty());
    }
}
