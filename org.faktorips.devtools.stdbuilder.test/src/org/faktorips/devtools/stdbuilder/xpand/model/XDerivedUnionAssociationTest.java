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

package org.faktorips.devtools.stdbuilder.xpand.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XDerivedUnionAssociationTest {

    private static final String DERIVED_UNION_NAME = "derivedUnion";

    @Mock
    private IAssociation association;

    @Mock
    private XClass xClass;

    @Mock
    private GeneratorModelContext context;

    @Mock
    private ModelService modelService;

    private XDerivedUnionAssociation xDerivedUnionAssociation;

    @Before
    public void setUp() throws Exception {
        when(association.isDerivedUnion()).thenReturn(true);
        when(association.getName()).thenReturn(DERIVED_UNION_NAME);
        xDerivedUnionAssociation = new XDerivedUnionAssociation(association, context, modelService);
    }

    @Test
    public void testGetSubsetAssociations() throws Exception {
        XAssociation association1 = mock(XAssociation.class);
        when(association1.isSubsetOf(xDerivedUnionAssociation)).thenReturn(true);

        XAssociation association2 = mock(XAssociation.class);
        when(association2.isSubsetOf(xDerivedUnionAssociation)).thenReturn(false);

        Set<XAssociation> associations = new LinkedHashSet<XAssociation>();
        associations.add(association1);
        associations.add(association2);
        doReturn(associations).when(xClass).getAssociations();

        Set<XAssociation> subsetAssociations = xDerivedUnionAssociation.getSubsetAssociations(xClass);
        assertEquals(1, subsetAssociations.size());
        assertTrue(subsetAssociations.contains(association1));
    }

    @Test
    public void testIsImplementedInSuperclass_sameClass() throws Exception {
        IType type = mock(IType.class);
        when(xDerivedUnionAssociation.getTypeOfAssociation()).thenReturn(type);
        when(xClass.getType()).thenReturn(type);

        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));
    }

    @Test
    public void testIsImplementedInSuperclass_superClassNotImplemented() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(xClass.getIpsProject()).thenReturn(ipsProject);

        IType superType = mock(IType.class);
        when(xDerivedUnionAssociation.getTypeOfAssociation()).thenReturn(superType);

        IType type = mock(IType.class);
        when(type.findSupertype(any(IIpsProject.class))).thenReturn(superType);
        when(xClass.getType()).thenReturn(type);

        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));
    }

    @Test
    public void testIsImplementedInSuperclass_superClassImplemented() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(xClass.getIpsProject()).thenReturn(ipsProject);

        IType superType = mock(IType.class);
        when(xDerivedUnionAssociation.getTypeOfAssociation()).thenReturn(superType);

        IType type = mock(IType.class);
        when(type.findSupertype(any(IIpsProject.class))).thenReturn(superType);
        when(xClass.getType()).thenReturn(type);

        IAssociation association2 = mock(IAssociation.class);
        when(association2.getSubsettedDerivedUnion()).thenReturn(DERIVED_UNION_NAME);
        ArrayList<IAssociation> associations = new ArrayList<IAssociation>();
        associations.add(association2);

        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));

        // derived union subset implemented in subtype should not matter at all
        when(type.getAssociations()).thenReturn(associations);
        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));

        when(superType.getAssociations()).thenReturn(associations);

        assertTrue(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));
    }

    @Test
    public void testIsImplementedInSuperclass_transitiv() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(xClass.getIpsProject()).thenReturn(ipsProject);

        IType type = mock(IType.class);
        IType superType = mock(IType.class);
        IType superSuperType = mock(IType.class);
        IType superSuperSuperType = mock(IType.class);

        when(xDerivedUnionAssociation.getTypeOfAssociation()).thenReturn(superSuperSuperType);

        when(xClass.getType()).thenReturn(type);
        when(type.findSupertype(any(IIpsProject.class))).thenReturn(superType);
        when(superType.findSupertype(any(IIpsProject.class))).thenReturn(superSuperType);
        when(superSuperType.findSupertype(any(IIpsProject.class))).thenReturn(superSuperSuperType);

        IAssociation association2 = mock(IAssociation.class);
        when(association2.getSubsettedDerivedUnion()).thenReturn(DERIVED_UNION_NAME);
        ArrayList<IAssociation> associations = new ArrayList<IAssociation>();
        associations.add(association2);

        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));

        when(superSuperType.getAssociations()).thenReturn(associations);

        assertTrue(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));

        when(superSuperType.getAssociations()).thenReturn(new ArrayList<IAssociation>());
        when(superSuperSuperType.getAssociations()).thenReturn(associations);

        assertTrue(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));
    }

    @Test
    public void testIsImplementedInSuperclass_checkProjects() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(xClass.getIpsProject()).thenReturn(ipsProject);

        IType type = mock(IType.class);
        IType superType = mock(IType.class);
        IType superSuperType = mock(IType.class);
        IType superSuperSuperType = mock(IType.class);

        when(xDerivedUnionAssociation.getTypeOfAssociation()).thenReturn(superSuperSuperType);

        when(xClass.getType()).thenReturn(type);
        when(type.findSupertype(ipsProject)).thenReturn(superType);
        when(superType.findSupertype(ipsProject)).thenReturn(superSuperType);
        when(superSuperType.findSupertype(ipsProject)).thenReturn(superSuperSuperType);

        IAssociation association2 = mock(IAssociation.class);
        when(association2.getSubsettedDerivedUnion()).thenReturn(DERIVED_UNION_NAME);
        ArrayList<IAssociation> associations = new ArrayList<IAssociation>();
        associations.add(association2);

        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));

        when(superSuperType.getAssociations()).thenReturn(associations);

        assertTrue(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));

        when(superSuperType.getAssociations()).thenReturn(new ArrayList<IAssociation>());
        when(superSuperSuperType.getAssociations()).thenReturn(associations);

        assertTrue(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));
    }

}
