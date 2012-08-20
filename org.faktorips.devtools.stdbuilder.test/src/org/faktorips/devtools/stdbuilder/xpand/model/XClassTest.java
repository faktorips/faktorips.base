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
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.xpand.model.filter.MasterToDetailWithoutSubsetsFilter;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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

        when(type.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.getJavaNamingConvention()).thenReturn(new JavaNamingConvention());
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
    public void testHasNonAbstractSupertype_NoSupertype() throws CoreException {
        XPolicyCmptClass xPolicyClass = setUpTypeHierarchy(false, false);
        when(xPolicyClass.getType().findSupertype(any(IIpsProject.class))).thenReturn(null);

        assertFalse(xPolicyClass.hasNonAbstractSupertype());
    }

    @Test
    public void testHasNonAbstractSupertype_AllSupertypesAbstract() throws CoreException {
        XPolicyCmptClass xPolicyClass = setUpTypeHierarchy(true, true);
        assertFalse(xPolicyClass.hasNonAbstractSupertype());
    }

    @Test
    public void testHasNonAbstractSupertype_OnlySupertypeNonAbstract() throws CoreException {
        XPolicyCmptClass xPolicyClass = setUpTypeHierarchy(false, true);
        assertTrue(xPolicyClass.hasNonAbstractSupertype());
    }

    @Test
    public void testHasNonAbstractSupertype_OnlySuperSupertypeNonAbstract() throws CoreException {
        XPolicyCmptClass xPolicyClass = setUpTypeHierarchy(true, false);
        assertTrue(xPolicyClass.hasNonAbstractSupertype());
    }

    private XPolicyCmptClass setUpTypeHierarchy(boolean superIsAbstract, boolean superSuperIsAbstract)
            throws CoreException {
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
        when(startType.isAbstract()).thenReturn(false);
        when(superType.isAbstract()).thenReturn(superIsAbstract);
        when(superSuperType.isAbstract()).thenReturn(superSuperIsAbstract);

        return xPolicyClass;
    }

    @Test
    public void testIsMasterToDetailAssociationIncludingDerivedUnions_detailToMaster() {
        AbstractAssociationFilter filter = new MasterToDetailWithoutSubsetsFilter();
        IAssociation association = mock(IAssociation.class);
        when(association.getAssociationType()).thenReturn(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        assertFalse(filter.isValidAssociation(association));
    }

    @Test
    public void testIsMasterToDetailAssociationIncludingDerivedUnions_subsetOfDU() {
        AbstractAssociationFilter filter = new MasterToDetailWithoutSubsetsFilter();
        IAssociation association = mock(IAssociation.class);
        when(association.getAssociationType()).thenReturn(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        when(association.isSubsetOfADerivedUnion()).thenReturn(true);
        when(association.isDerivedUnion()).thenReturn(false);

        assertFalse(filter.isValidAssociation(association));
    }

    @Test
    public void testIsMasterToDetailAssociationIncludingDerivedUnions_DUAndSubsetOfDU() {
        AbstractAssociationFilter filter = new MasterToDetailWithoutSubsetsFilter();
        IAssociation association = mock(IAssociation.class);
        when(association.getAssociationType()).thenReturn(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        when(association.isSubsetOfADerivedUnion()).thenReturn(true);
        when(association.isDerivedUnion()).thenReturn(true);

        assertTrue(filter.isValidAssociation(association));
    }

    @Test
    public void testIsMasterToDetailAssociationIncludingDerivedUnions_DU() {
        AbstractAssociationFilter filter = new MasterToDetailWithoutSubsetsFilter();
        IAssociation association = mock(IAssociation.class);
        when(association.getAssociationType()).thenReturn(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        when(association.isSubsetOfADerivedUnion()).thenReturn(false);
        when(association.isDerivedUnion()).thenReturn(true);

        assertTrue(filter.isValidAssociation(association));
    }

    @Test
    public void testIsMasterToDetailAssociationIncludingDerivedUnions_normalAssoc() {
        AbstractAssociationFilter filter = new MasterToDetailWithoutSubsetsFilter();
        IAssociation association = mock(IAssociation.class);
        when(association.getAssociationType()).thenReturn(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        when(association.isSubsetOfADerivedUnion()).thenReturn(false);
        when(association.isDerivedUnion()).thenReturn(false);

        assertTrue(filter.isValidAssociation(association));
    }
}
