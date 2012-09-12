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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Test;

public class ComponentNodeTest extends AbstractIpsPluginTest {

    @Test
    public void testGetParent_RootParentIsNull() throws CoreException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        ComponentNode root = new ComponentNode(vertrag, null, project);

        assertNull(root.getParent());
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

    @Test
    public void testEncapsulateComponentTypes_EmptyListInput() throws CoreException {
        IIpsProject project = newIpsProject();
        List<ComponentNode> encapsulateComponentTypes = ComponentNode.encapsulateComponentTypes(new ArrayList<IType>(),
                project);

        assertTrue(encapsulateComponentTypes.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testEncapsulateComponentTypes_NullListInput() throws CoreException {
        IIpsProject project = newIpsProject();

        ComponentNode.encapsulateComponentTypes(null, project);
    }

    @Test
    public void testEncapsulateComponentTypes_NonEmptyListInput() throws CoreException {
        IIpsProject project = newIpsProject();
        ArrayList<IType> components = new ArrayList<IType>();
        PolicyCmptType type = newPolicyCmptTypeWithoutProductCmptType(project, "Component");
        components.add(type);
        List<ComponentNode> encapsulatedComponentTypes = ComponentNode.encapsulateComponentTypes(components, project);

        assertEquals(1, encapsulatedComponentTypes.size());
        assertEquals(type, encapsulatedComponentTypes.get(0).getValue());
    }

    @Test(expected = NullPointerException.class)
    public void testEncapsulateComponentTypes_NullProjectAndNonEmptyListInput() throws CoreException {
        IIpsProject project = newIpsProject();
        ArrayList<IType> components = new ArrayList<IType>();
        components.add(newPolicyCmptTypeWithoutProductCmptType(project, "Component"));

        ComponentNode.encapsulateComponentTypes(components, null);
    }

    @Test
    public void testEncapsulateComponentTypes_NullProjectAndEmptyListInput() {
        List<ComponentNode> encapsulateComponentTypes = ComponentNode.encapsulateComponentTypes(new ArrayList<IType>(),
                null);
        assertTrue(encapsulateComponentTypes.isEmpty());
    }
}
