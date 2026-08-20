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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.model.internal.builder.JavaNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class XAssociationTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private GeneratorConfig generatorConfig;

    @Mock
    private ModelService modelService;

    @Mock
    private IAssociation association;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private XAssociation xAssociation;

    @Mock
    private IIpsProject ipsProject;

    private MockitoSession mockito;

    @BeforeEach
    public void initMocks() {
        mockito = createMocks(this);
        lenient().when(association.getTargetRoleSingular()).thenReturn("singular");
        lenient().when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        lenient().when(xAssociation.getIpsProject()).thenReturn(ipsProject);
        lenient().doReturn(new JavaNamingConvention()).when(xAssociation).getJavaNamingConvention();
        lenient().doReturn(modelContext).when(xAssociation).getContext();
        lenient().doReturn(modelService).when(xAssociation).getModelService();
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testGetMethodNameAdd() throws Exception {
        when(xAssociation.isOneToMany()).thenReturn(true);
        String addMethodName = xAssociation.getMethodNameSetOrAdd();
        assertEquals("addSingular", addMethodName);

        when(association.getTargetRoleSingular()).thenReturn("Singular");
        addMethodName = xAssociation.getMethodNameSetOrAdd();
        assertEquals("addSingular", addMethodName);
    }

    @Test
    public void testGetMethodNameSetter() throws Exception {
        String methodName = xAssociation.getMethodNameSetOrAdd();
        assertEquals("setSingular", methodName);
    }

    @Test
    public void testGetMethodNameGetterterNumOf() throws Exception {
        doReturn(XPolicyCmptClass.class).when(xAssociation).getModelNodeType(false);
        when(association.getTargetRolePlural()).thenReturn("testTargets");
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        String methodName = xAssociation.getMethodNameGetNumOf();
        assertEquals("getNumOfTestTargets", methodName);
    }

    @Test
    public void testIsSubsetOf_noMatch() throws Exception {
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        XDerivedUnionAssociation xAssociation2 = mock(XDerivedUnionAssociation.class);

        assertFalse(xAssociation.isSubsetOf(xAssociation2));
    }

    @Test
    public void testIsSubsetOf_sameNameButNoSubset() throws Exception {
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        XDerivedUnionAssociation xAssociation2 = mock(XDerivedUnionAssociation.class);

        assertFalse(xAssociation.isSubsetOf(xAssociation2));
    }

    @Test
    public void testIsSubsetOf_foundSubset() throws Exception {
        XDerivedUnionAssociation xDerivedUnionAsso = mock(XDerivedUnionAssociation.class);
        IAssociation derivedUnion = mock(IAssociation.class);
        when(xDerivedUnionAsso.getAssociation()).thenReturn(derivedUnion);
        when(xAssociation.getIpsObjectPartContainer()).thenReturn(association);
        when(association.isSubsetOfDerivedUnion(derivedUnion, ipsProject)).thenReturn(true);

        assertTrue(xAssociation.isSubsetOf(xDerivedUnionAsso));
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

        assertEquals(type, xAssociation.getSourceType());
    }

    @Test
    public void testGetTargetInterfaceName() throws Exception {
        initMocksForGetNameTests();

        String targetClassName = xAssociation.getTargetInterfaceName();
        assertEquals("ITargetType", targetClassName);
    }

    @Test
    public void testGetTargetInterfaceName_differingPublishedInterfaceSettings() throws Exception {
        initMocksForGetNameTests();

        String targetClassName = xAssociation.getTargetInterfaceName();
        assertEquals("ITargetType", targetClassName);
    }

    @Test
    public void testGetTargetInterfaceNameBase_constrainedAssociation_differingPublishedInterfaceSettings()
            throws Exception {
        initMocksForGetNameTests();

        XAssociation xConstrainedAssociation = mock(XAssociation.class, CALLS_REAL_METHODS);
        XProductCmptClass xSuperProductCmptClass = mock(XProductCmptClass.class, CALLS_REAL_METHODS);
        doReturn(xSuperProductCmptClass).when(xConstrainedAssociation).getTargetModelNode();
        doReturn("ITargetSuperType").when(xSuperProductCmptClass).getPublishedInterfaceName();

        when(xAssociation.isConstrain()).thenReturn(true);
        when(xAssociation.getConstrainedAssociation()).thenReturn(xConstrainedAssociation);

        String targetClassName = xAssociation.getTargetInterfaceNameBase();
        assertEquals("ITargetSuperType", targetClassName);
    }

    @Test
    public void testGetTargetInterfaceNameBase_differingPublishedInterfaceSettings() throws Exception {
        initMocksForGetNameTests();
        when(xAssociation.isConstrain()).thenReturn(false);

        String targetClassName = xAssociation.getTargetInterfaceNameBase();
        assertEquals("ITargetType", targetClassName);
    }

    private XProductCmptClass initMocksForGetNameTests() {
        XProductCmptClass xProductCmptClass = mock(XProductCmptClass.class, CALLS_REAL_METHODS);
        lenient().doReturn(xProductCmptClass).when(xAssociation).getTargetModelNode();
        lenient().doReturn("ITargetType").when(xProductCmptClass).getPublishedInterfaceName();
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

    @Test
    public void testGetSubsettedDerivedUnion_notASubset() throws Exception {
        assertThrows(RuntimeException.class, () -> {
            xAssociation.getSubsettedDerivedUnion();
        });
    }

    @Test
    public void testGetSubsettedDerivedUnion_subsetNotFound() throws Exception {
        assertThrows(RuntimeException.class, () -> {
            when(association.isSubsetOfADerivedUnion()).thenReturn(true);
            xAssociation.getSubsettedDerivedUnion();
        });
    }

    @Test
    public void testGetSubsettedDerivedUnion_subsetFound() throws Exception {
        IAssociation derivedUnion = mock(IAssociation.class);
        XDerivedUnionAssociation xDerivedUnion = mock(XDerivedUnionAssociation.class);
        when(modelService.getModelNode(derivedUnion, XDerivedUnionAssociation.class, modelContext))
                .thenReturn(xDerivedUnion);

        when(association.isSubsetOfADerivedUnion()).thenReturn(true);
        when(association.findSubsettedDerivedUnion(any(IIpsProject.class))).thenReturn(derivedUnion);

        assertEquals(xDerivedUnion, xAssociation.getSubsettedDerivedUnion());
    }
}
