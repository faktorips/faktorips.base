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
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XAssociationTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private IAssociation association;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private XAssociation xAssociation;

    @Before
    public void initMocks() {
        when(association.getTargetRoleSingular()).thenReturn("testTarget");
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        doReturn(new JavaNamingConvention()).when(xAssociation).getJavaNamingConvention();
        doReturn(modelContext).when(xAssociation).getContext();
    }

    @Test
    public void testGetMethodNameAdd() throws Exception {
        initMocks();
        String addMethodName = xAssociation.getMethodNameAdd();
        assertEquals("addTestTarget", addMethodName);

        association.setTargetRoleSingular("TestTarget");
        addMethodName = xAssociation.getMethodNameAdd();
        assertEquals("addTestTarget", addMethodName);
    }

    @Test
    public void testGetMethodNameSetter() throws Exception {
        initMocks();
        String methodName = xAssociation.getMethodNameSetter();
        assertEquals("setTestTarget", methodName);
    }

    @Test
    public void testGetMethodNameGetterterNumOf() throws Exception {
        when(association.getTargetRolePlural()).thenReturn("testTargets");
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        String methodName = xAssociation.getMethodNameGetNumOf();
        assertEquals("getNumOfTestTargets", methodName);
    }

    @Test
    public void testIsSubsetOf() throws Exception {
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
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);

        when(association.isDerivedUnion()).thenReturn(true);
        assertTrue(xAssociation.isDerivedUnion());

        when(association.isDerivedUnion()).thenReturn(false);
        assertFalse(xAssociation.isDerivedUnion());
    }

    @Test
    public void testGetTargetType() throws Exception {
        IType type = mock(IType.class);
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        when(association.getType()).thenReturn(type);

        assertEquals(type, xAssociation.getAssociationType());
    }

    @Test
    public void testGetClassName() throws Exception {
        XProductCmptClass xProductCmptClass = initMocksForGetNameTests();
        doReturn("TargetType").when(xProductCmptClass).getSimpleName(BuilderAspect.IMPLEMENTATION);

        // doReturn(XProductCmptClass.class).when(xAssociation).getTargetModelNodeType();
        String targetClassName = xAssociation.getTargetClassName();
        assertEquals("TargetType", targetClassName);
    }

    @Test
    public void testGetTargetInterfaceName() throws Exception {
        XProductCmptClass xProductCmptClass = initMocksForGetNameTests();
        doReturn(true).when(xAssociation).isGeneratingPublishedInterfaces();
        doReturn("ITargetType").when(xProductCmptClass).getSimpleName(BuilderAspect.INTERFACE);

        // doReturn(XProductCmptClass.class).when(xAssociation).getTargetModelNodeType();
        String targetClassName = xAssociation.getTargetInterfaceName();
        assertEquals("ITargetType", targetClassName);
    }

    private XProductCmptClass initMocksForGetNameTests() {
        IProductCmptType productCmptType = mock(IProductCmptType.class);
        XProductCmptClass xProductCmptClass = mock(XProductCmptClass.class);
        doReturn(xProductCmptClass).when(xAssociation).getModelNode(productCmptType, XProductCmptClass.class);
        doReturn(productCmptType).when(xAssociation).getTargetType();
        return xProductCmptClass;
    }

    @Test
    public void testIsRecursiveSubsetOf() {
        XDerivedUnionAssociation du = mock(XDerivedUnionAssociation.class);
        XDerivedUnionAssociation otherDu = mock(XDerivedUnionAssociation.class);
        doReturn(otherDu).when(xAssociation).getSubsettedDerivedUnion();

        doReturn(false).when(xAssociation).isSubsetOfADerivedUnion();
        assertFalse(xAssociation.isRecursiveSubsetOf(du));

        doReturn(true).when(xAssociation).isSubsetOfADerivedUnion();
        doReturn(false).when(xAssociation).isSubsetOf(du);
        when(otherDu.isRecursiveSubsetOf(du)).thenReturn(false);
        assertFalse(xAssociation.isRecursiveSubsetOf(du));

        doReturn(true).when(xAssociation).isSubsetOf(du);
        when(otherDu.isRecursiveSubsetOf(du)).thenReturn(false);
        assertTrue(xAssociation.isRecursiveSubsetOf(du));

        doReturn(false).when(xAssociation).isSubsetOf(du);
        when(otherDu.isRecursiveSubsetOf(du)).thenReturn(true);
        assertTrue(xAssociation.isRecursiveSubsetOf(du));

        doReturn(true).when(xAssociation).isSubsetOf(du);
        when(otherDu.isRecursiveSubsetOf(du)).thenReturn(true);
        assertTrue(xAssociation.isRecursiveSubsetOf(du));

    }
}
