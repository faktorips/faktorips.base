/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class XDerivedUnionAssociationTest {

    private static final String DERIVED_UNION_NAME = "derivedUnion";

    @Mock
    private IAssociation association;

    @Mock
    private XType xType;

    @Mock
    private GeneratorModelContext context;

    @Mock
    private ModelService modelService;

    private XDerivedUnionAssociation xDerivedUnionAssociation;

    @Before
    public void setUp() throws Exception {
        when(association.getName()).thenReturn(DERIVED_UNION_NAME);
        xDerivedUnionAssociation = new XDerivedUnionAssociation(association, context, modelService);
    }

    @Test
    public void testGetSubsetAssociations() throws Exception {
        XAssociation association1 = mock(XAssociation.class);
        when(association1.isSubsetOf(xDerivedUnionAssociation)).thenReturn(true);

        XAssociation association2 = mock(XAssociation.class);
        when(association2.isSubsetOf(xDerivedUnionAssociation)).thenReturn(false);

        Set<XAssociation> associations = new LinkedHashSet<>();
        associations.add(association1);
        associations.add(association2);
        doReturn(associations).when(xType).getAssociations();

        Set<XAssociation> subsetAssociations = xDerivedUnionAssociation.getSubsetAssociations(xType);
        assertEquals(1, subsetAssociations.size());
        assertTrue(subsetAssociations.contains(association1));
    }

    @Test
    public void testIsImplementedInSuperclass_sameClass() throws Exception {
        IType type = mock(IType.class);
        when(xDerivedUnionAssociation.getSourceType()).thenReturn(type);
        when(xType.getType()).thenReturn(type);

        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xType));
    }

    @Test
    public void testIsImplementedInSuperclass_superClassNotImplemented() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(xType.getIpsProject()).thenReturn(ipsProject);

        IType superType = mock(IType.class);
        when(xDerivedUnionAssociation.getSourceType()).thenReturn(superType);

        IType type = mock(IType.class);
        when(type.findSupertype(any(IIpsProject.class))).thenReturn(superType);
        when(xType.getType()).thenReturn(type);

        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xType));
    }

    @Test
    public void testIsImplementedInSuperclass_superClassImplemented() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(xType.getIpsProject()).thenReturn(ipsProject);

        IType superType = mock(IType.class);
        when(xDerivedUnionAssociation.getSourceType()).thenReturn(superType);

        IType type = mock(IType.class);
        when(type.findSupertype(any(IIpsProject.class))).thenReturn(superType);
        when(xType.getType()).thenReturn(type);

        IAssociation association2 = mock(IAssociation.class);
        when(association2.getSubsettedDerivedUnion()).thenReturn(DERIVED_UNION_NAME);
        ArrayList<IAssociation> associations = new ArrayList<>();
        associations.add(association2);

        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xType));

        when(superType.getAssociations()).thenReturn(associations);

        assertTrue(xDerivedUnionAssociation.isImplementedInSuperclass(xType));
    }

    @Test
    public void testIsImplementedInSuperclass_transitiv() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(xType.getIpsProject()).thenReturn(ipsProject);

        IType type = mock(IType.class);
        IType superType = mock(IType.class);
        IType superSuperType = mock(IType.class);
        IType superSuperSuperType = mock(IType.class);

        when(xDerivedUnionAssociation.getSourceType()).thenReturn(superSuperSuperType);

        when(xType.getType()).thenReturn(type);
        when(type.findSupertype(any(IIpsProject.class))).thenReturn(superType);
        when(superType.findSupertype(any(IIpsProject.class))).thenReturn(superSuperType);
        when(superSuperType.findSupertype(any(IIpsProject.class))).thenReturn(superSuperSuperType);

        IAssociation association2 = mock(IAssociation.class);
        when(association2.getSubsettedDerivedUnion()).thenReturn(DERIVED_UNION_NAME);
        ArrayList<IAssociation> associations = new ArrayList<>();
        associations.add(association2);

        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xType));

        when(superSuperType.getAssociations()).thenReturn(associations);

        assertTrue(xDerivedUnionAssociation.isImplementedInSuperclass(xType));

        when(superSuperType.getAssociations()).thenReturn(new ArrayList<>());
        when(superSuperSuperType.getAssociations()).thenReturn(associations);

        assertTrue(xDerivedUnionAssociation.isImplementedInSuperclass(xType));
    }

    @Test
    public void testIsImplementedInSuperclass_checkProjects() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(xType.getIpsProject()).thenReturn(ipsProject);

        IType type = mock(IType.class);
        IType superType = mock(IType.class);
        IType superSuperType = mock(IType.class);
        IType superSuperSuperType = mock(IType.class);

        when(xDerivedUnionAssociation.getSourceType()).thenReturn(superSuperSuperType);

        when(xType.getType()).thenReturn(type);
        when(type.findSupertype(ipsProject)).thenReturn(superType);
        when(superType.findSupertype(ipsProject)).thenReturn(superSuperType);
        when(superSuperType.findSupertype(ipsProject)).thenReturn(superSuperSuperType);

        IAssociation association2 = mock(IAssociation.class);
        when(association2.getSubsettedDerivedUnion()).thenReturn(DERIVED_UNION_NAME);
        ArrayList<IAssociation> associations = new ArrayList<>();
        associations.add(association2);

        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xType));

        when(superSuperType.getAssociations()).thenReturn(associations);

        assertTrue(xDerivedUnionAssociation.isImplementedInSuperclass(xType));

        when(superSuperType.getAssociations()).thenReturn(new ArrayList<>());
        when(superSuperSuperType.getAssociations()).thenReturn(associations);

        assertTrue(xDerivedUnionAssociation.isImplementedInSuperclass(xType));
    }

}
