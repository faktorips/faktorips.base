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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XAssociationTest {

    @Mock
    private IAssociation association;

    @Test
    public void testGetAddMethodName() throws Exception {
        when(association.getTargetRoleSingular()).thenReturn("testTarget");
        XAssociation xAssociation = mock(XAssociation.class, Mockito.CALLS_REAL_METHODS);
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        String addMethodName = xAssociation.getAddMethodName();
        assertEquals("addTestTarget", addMethodName);

        association.setTargetRoleSingular("TestTarget");
        addMethodName = xAssociation.getAddMethodName();
        assertEquals("addTestTarget", addMethodName);
    }

    @Test
    public void testGetSetterMethodName() throws Exception {
        when(association.getTargetRoleSingular()).thenReturn("testTarget");
        XAssociation xAssociation = mock(XAssociation.class, Mockito.CALLS_REAL_METHODS);
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        doReturn(new JavaNamingConvention()).when(xAssociation).getJavaNamingConvention();
        String methodName = xAssociation.getSetterMethodName();
        assertEquals("setTestTarget", methodName);
    }

    @Test
    public void testGetGetterMethodNameNumOf() throws Exception {
        when(association.getTargetRolePlural()).thenReturn("testTargets");
        XAssociation xAssociation = mock(XAssociation.class, Mockito.CALLS_REAL_METHODS);
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        doReturn(new JavaNamingConvention()).when(xAssociation).getJavaNamingConvention();
        String methodName = xAssociation.getGetterMethodNameNumOf();
        assertEquals("getNumOfTestTargets", methodName);
    }

    @Test
    public void testIsSubsetOf() throws Exception {
        XAssociation xAssociation = mock(XAssociation.class, Mockito.CALLS_REAL_METHODS);
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);

        XDerivedUnionAssociation xAssociation2 = mock(XDerivedUnionAssociation.class);
        when(xAssociation2.getName()).thenReturn("abc123");

        when(association.getSubsettedDerivedUnion()).thenReturn("other");
        assertFalse(xAssociation.isSubsetOf(xAssociation2));

        when(association.getSubsettedDerivedUnion()).thenReturn("abc123");
        assertTrue(xAssociation.isSubsetOf(xAssociation2));
    }

    @Test
    public void testIsDerivedUnion() throws Exception {
        XAssociation xAssociation = mock(XAssociation.class, Mockito.CALLS_REAL_METHODS);
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);

        when(association.isDerivedUnion()).thenReturn(true);
        assertTrue(xAssociation.isDerivedUnion());

        when(association.isDerivedUnion()).thenReturn(false);
        assertFalse(xAssociation.isDerivedUnion());
    }

    @Test
    public void testGetTargetType() throws Exception {
        IType type = mock(IType.class);
        XAssociation xAssociation = mock(XAssociation.class, Mockito.CALLS_REAL_METHODS);
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        when(association.getType()).thenReturn(type);

        assertEquals(type, xAssociation.getAssociationType());
    }

}
