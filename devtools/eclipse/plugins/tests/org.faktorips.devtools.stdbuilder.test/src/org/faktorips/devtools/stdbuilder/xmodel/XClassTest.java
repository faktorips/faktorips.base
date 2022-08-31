/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.naming.JavaClassNaming;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class XClassTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private IType type;

    @Mock
    private IIpsProject ipsProject;

    private XClass xClass;

    @Before
    public void createTestXClass() {
        xClass = mock(XClass.class, CALLS_REAL_METHODS);
        when(xClass.getIpsObjectPartContainer()).thenReturn(type);
        when(xClass.getContext()).thenReturn(modelContext);
    }

    @Test
    public void testGetSimpleName() throws Exception {
        doReturn("test.Type").when(xClass).getQualifiedName(BuilderAspect.IMPLEMENTATION);
        doReturn("test.IType").when(xClass).getQualifiedName(BuilderAspect.INTERFACE);
        doReturn("Type").when(xClass).addImport("test.Type");
        doReturn("IType").when(xClass).addImport("test.IType");

        assertEquals("Type", xClass.getSimpleName(BuilderAspect.IMPLEMENTATION));
        verify(xClass).addImport("test.Type");

        assertEquals("IType", xClass.getSimpleName(BuilderAspect.INTERFACE));
        verify(xClass).addImport("test.IType");
    }

    @Test
    public void testGetQualifiedName() throws Exception {
        JavaClassNaming javaClassNaming = mock(JavaClassNaming.class);
        when(xClass.getJavaClassNaming()).thenReturn(javaClassNaming);

        when(
                javaClassNaming.getQualifiedClassName(type, BuilderAspect.IMPLEMENTATION,
                        xClass.getJavaClassNameProvider())).thenReturn("test.Type");
        when(javaClassNaming.getQualifiedClassName(type, BuilderAspect.INTERFACE, xClass.getJavaClassNameProvider()))
                .thenReturn("test.IType");

        assertEquals("test.Type", xClass.getQualifiedName(BuilderAspect.IMPLEMENTATION));
        assertEquals("test.IType", xClass.getQualifiedName(BuilderAspect.INTERFACE));
    }

    @Test
    public void testHasNonAbstractSupertype_NoSupertype() {
        XPolicyCmptClass xPolicyClass = setUpTypeHierarchy(false, false);
        when(xPolicyClass.getType().findSupertype(any(IIpsProject.class))).thenReturn(null);

        assertFalse(xPolicyClass.hasNonAbstractSupertype());
    }

    @Test
    public void testHasNonAbstractSupertype_AllSupertypesAbstract() {
        XPolicyCmptClass xPolicyClass = setUpTypeHierarchy(true, true);
        assertFalse(xPolicyClass.hasNonAbstractSupertype());
    }

    @Test
    public void testHasNonAbstractSupertype_OnlySupertypeNonAbstract() {
        XPolicyCmptClass xPolicyClass = setUpTypeHierarchy(false, true);
        assertTrue(xPolicyClass.hasNonAbstractSupertype());
    }

    @Test
    public void testHasNonAbstractSupertype_OnlySuperSupertypeNonAbstract() {
        XPolicyCmptClass xPolicyClass = setUpTypeHierarchy(true, false);
        assertTrue(xPolicyClass.hasNonAbstractSupertype());
    }

    private XPolicyCmptClass setUpTypeHierarchy(boolean superIsAbstract, boolean superSuperIsAbstract) {
        XPolicyCmptClass xPolicyClass = mock(XPolicyCmptClass.class, CALLS_REAL_METHODS);
        IIpsProject ipsProjectMock = mock(IIpsProject.class, CALLS_REAL_METHODS);

        IPolicyCmptType startType = mock(IPolicyCmptType.class);
        IPolicyCmptType superType = mock(IPolicyCmptType.class);
        IPolicyCmptType superSuperType = mock(IPolicyCmptType.class);

        when(xPolicyClass.getType()).thenReturn(startType);
        when(xPolicyClass.getIpsObjectPartContainer()).thenReturn(startType);
        when(xPolicyClass.getIpsProject()).thenReturn(ipsProjectMock);

        when(startType.findSupertype(any(IIpsProject.class))).thenReturn(superType);
        when(superType.findSupertype(any(IIpsProject.class))).thenReturn(superSuperType);
        when(superSuperType.findSupertype(any(IIpsProject.class))).thenReturn(null);
        when(superType.isAbstract()).thenReturn(superIsAbstract);
        when(superSuperType.isAbstract()).thenReturn(superSuperIsAbstract);

        return xPolicyClass;
    }

    // @Test
    // public void testIsMasterToDetailAssociationIncludingDerivedUnions_detailToMaster() {
    // AbstractAssociationFilter filter = new MasterToDetailWithoutSubsetsFilter();
    // IAssociation association = mock(IAssociation.class);
    // when(association.getAssociationType()).thenReturn(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
    //
    // assertFalse(filter.isValidAssociation(association));
    // }
    //
    // @Test
    // public void testIsMasterToDetailAssociationIncludingDerivedUnions_subsetOfDU() {
    // AbstractAssociationFilter filter = new MasterToDetailWithoutSubsetsFilter();
    // IAssociation association = mock(IAssociation.class);
    // when(association.getAssociationType()).thenReturn(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
    // when(association.isSubsetOfADerivedUnion()).thenReturn(true);
    // when(association.isDerivedUnion()).thenReturn(false);
    //
    // assertFalse(filter.isValidAssociation(association));
    // }
    //
    // @Test
    // public void testIsMasterToDetailAssociationIncludingDerivedUnions_DUAndSubsetOfDU() {
    // AbstractAssociationFilter filter = new MasterToDetailWithoutSubsetsFilter();
    // IAssociation association = mock(IAssociation.class);
    // when(association.getAssociationType()).thenReturn(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
    // when(association.isSubsetOfADerivedUnion()).thenReturn(true);
    // when(association.isDerivedUnion()).thenReturn(true);
    //
    // assertTrue(filter.isValidAssociation(association));
    // }
    //
    // @Test
    // public void testIsMasterToDetailAssociationIncludingDerivedUnions_DU() {
    // AbstractAssociationFilter filter = new MasterToDetailWithoutSubsetsFilter();
    // IAssociation association = mock(IAssociation.class);
    // when(association.getAssociationType()).thenReturn(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
    // when(association.isSubsetOfADerivedUnion()).thenReturn(false);
    // when(association.isDerivedUnion()).thenReturn(true);
    //
    // assertTrue(filter.isValidAssociation(association));
    // }
    //
    // @Test
    // public void testIsMasterToDetailAssociationIncludingDerivedUnions_normalAssoc() {
    // AbstractAssociationFilter filter = new MasterToDetailWithoutSubsetsFilter();
    // IAssociation association = mock(IAssociation.class);
    // when(association.getAssociationType()).thenReturn(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
    // when(association.isSubsetOfADerivedUnion()).thenReturn(false);
    // when(association.isDerivedUnion()).thenReturn(false);
    //
    // assertTrue(filter.isValidAssociation(association));
    // }
}
