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

import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
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
    private ModelService modelService;

    @Mock
    private IAssociation association;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private XAssociation xAssociation;

    @Before
    public void initMocks() {
        when(association.getTargetRoleSingular()).thenReturn("singular");
        when(association.getTargetRolePlural()).thenReturn("plural");
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        doReturn(new JavaNamingConvention()).when(xAssociation).getJavaNamingConvention();
        doReturn(modelContext).when(xAssociation).getContext();
        doReturn(modelService).when(xAssociation).getModelService();
    }

    @Test
    public void testGetMethodNameAdd() throws Exception {
        initMocks();
        when(xAssociation.isOneToMany()).thenReturn(true);
        String addMethodName = xAssociation.getMethodNameSetOrAdd();
        assertEquals("addSingular", addMethodName);

        when(association.getTargetRoleSingular()).thenReturn("Singular");
        addMethodName = xAssociation.getMethodNameSetOrAdd();
        assertEquals("addSingular", addMethodName);
    }

    @Test
    public void testGetMethodNameSetter() throws Exception {
        initMocks();
        String methodName = xAssociation.getMethodNameSetOrAdd();
        assertEquals("setSingular", methodName);
    }

    @Test
    public void testGetMethodNameGetterterNumOf() throws Exception {
        doReturn(XPolicyCmptClass.class).when(xAssociation).getModelNodeType();
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

        assertEquals(type, xAssociation.getTypeOfAssociation());
    }

    @Test
    public void testGetTargetInterfaceName() throws Exception {
        XProductCmptClass xProductCmptClass = initMocksForGetNameTests();
        doReturn(true).when(xAssociation).isGeneratePublishedInterfaces();
        doReturn("ITargetType").when(xProductCmptClass).getSimpleName(BuilderAspect.INTERFACE);

        doReturn(XProductCmptClass.class).when(xAssociation).getModelNodeType();
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

    @Test
    public void testIsMasterToDetail() throws Exception {
        when(association.getAssociationType()).thenReturn(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertTrue(xAssociation.isMasterToDetail());
        when(association.getAssociationType()).thenReturn(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertFalse(xAssociation.isMasterToDetail());
        when(association.getAssociationType()).thenReturn(AssociationType.ASSOCIATION);
        assertFalse(xAssociation.isMasterToDetail());
        when(association.getAssociationType()).thenReturn(AssociationType.AGGREGATION);
        assertTrue(xAssociation.isMasterToDetail());
    }

    @Test(expected = RuntimeException.class)
    public void testGetSubsettedDerivedUnion_notASubset() throws Exception {
        xAssociation.getSubsettedDerivedUnion();
    }

    @Test(expected = RuntimeException.class)
    public void testGetSubsettedDerivedUnion_subsetNotFound() throws Exception {
        when(association.isSubsetOfADerivedUnion()).thenReturn(true);
        xAssociation.getSubsettedDerivedUnion();
    }

    @Test
    public void testGetSubsettedDerivedUnion_subsetFound() throws Exception {
        IAssociation derivedUnion = mock(IAssociation.class);
        XDerivedUnionAssociation xDerivedUnion = mock(XDerivedUnionAssociation.class);
        when(modelService.getModelNode(derivedUnion, XDerivedUnionAssociation.class, modelContext)).thenReturn(
                xDerivedUnion);

        when(association.isSubsetOfADerivedUnion()).thenReturn(true);
        when(association.findSubsettedDerivedUnion(any(IIpsProject.class))).thenReturn(derivedUnion);

        assertEquals(xDerivedUnion, xAssociation.getSubsettedDerivedUnion());
    }

}
