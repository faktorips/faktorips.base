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
import java.util.List;

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
        xDerivedUnionAssociation = new XDerivedUnionAssociation(association, context, modelService);
    }

    @Test
    public void testGetSubsetAssociations() throws Exception {
        XAssociation association1 = mock(XAssociation.class);
        when(association1.isSubsetOf(xDerivedUnionAssociation)).thenReturn(true);

        XAssociation association2 = mock(XAssociation.class);
        when(association2.isSubsetOf(xDerivedUnionAssociation)).thenReturn(false);

        List<XAssociation> list = new ArrayList<XAssociation>();
        list.add(association1);
        list.add(association2);
        doReturn(list).when(xClass).getAssociations();

        List<XAssociation> subsetAssociations = xDerivedUnionAssociation.getSubsetAssociations(xClass);
        assertEquals(1, subsetAssociations.size());
        assertTrue(subsetAssociations.contains(association1));
    }

    @Test
    public void testIsImplementedInSuperclass_sameClass() throws Exception {
        IType type = mock(IType.class);
        when(xDerivedUnionAssociation.getAssociationType()).thenReturn(type);
        when(xClass.getType()).thenReturn(type);

        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));
    }

    @Test
    public void testIsImplementedInSuperclass_superClass() throws Exception {
        IType superType = mock(IType.class);
        when(xDerivedUnionAssociation.getAssociationType()).thenReturn(superType);

        IType type = mock(IType.class);
        when(type.findSupertype(any(IIpsProject.class))).thenReturn(superType);
        when(xClass.getType()).thenReturn(type);

        assertFalse(xDerivedUnionAssociation.isImplementedInSuperclass(xClass));

        // TODO zweiter Test: Derived Union wird in SuperKlasse bereits implementiert --> assertTrue
    }

    @Test
    public void testIsImplementedInSuperclass_transitiv() throws Exception {
        IType type = mock(IType.class);
        IType superType = mock(IType.class);
        when(type.findSupertype(any(IIpsProject.class))).thenReturn(superType);

        // when(modelService.getModelNode(ipsObjectPartContainer, nodeClass, modelContext))
    }
}
