/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.MethodDefinition;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAssociation;
import org.faktorips.devtools.model.internal.builder.JavaNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class XProductAssociationTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private IProductCmptType targetCmptType;

    @Mock
    private IProductCmptTypeAssociation association;

    @Mock
    private XProductCmptClass xTargetCmptClass;

    @Mock
    private XProductCmptGenerationClass xTargetGenerationClass;

    private XProductAssociation xProductAssociation;

    @Before
    public void setUp() throws Exception {
        when(ipsProject.getJavaNamingConvention()).thenReturn(new JavaNamingConvention());
        when(association.getIpsProject()).thenReturn(ipsProject);
        when(association.findTarget(ipsProject)).thenReturn(targetCmptType);
        when(modelService.getModelNode(targetCmptType, XProductCmptGenerationClass.class, modelContext)).thenReturn(
                xTargetGenerationClass);
        when(xTargetGenerationClass.getSimpleName(BuilderAspect.INTERFACE)).thenReturn("ITargetTypeGen");
    }

    @Before
    public void createXProductAssociation() {
        xProductAssociation = new XProductAssociation(association, modelContext, modelService);
    }

    @Test
    public void testGetTargetClassGenerationName() {
        String targetClassGenerationName = xProductAssociation.getTargetClassGenerationName();
        assertThat(targetClassGenerationName, is("ITargetTypeGen"));
    }

    @Test
    public void testGetGetterNameForTargetGeneration() throws Exception {
        association.setTarget("test.TargetType");
        when(xTargetGenerationClass.getMethodNameGetProductComponentGeneration()).thenReturn("getTargetTypeGen");
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String getterNameForTargetGeneration = xProductAssociation.getMethodNameGetTargetGeneration();
        assertThat(getterNameForTargetGeneration, is("getTargetTypeGen"));
    }

    @Test
    public void testGetTargetClassProductComponentName() {
        String targetClassProductName = xProductAssociation.getTargetClassGenerationName();
        assertThat(targetClassProductName, is("ITargetTypeGen"));
    }

    @Test
    public void testGetMethodNameGetLinksFor_plural() throws Exception {
        when(association.getTargetRolePlural()).thenReturn("testTargets");
        when(association.is1ToMany()).thenReturn(true);
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String methodName = xProductAssociation.getMethodNameGetLinksFor();
        assertThat(methodName, is("getLinksForTestTargets"));
    }

    @Test
    public void testGetMethodNameGetLinksFor_singular() throws Exception {
        when(association.getTargetRoleSingular()).thenReturn("testTarget");
        when(association.is1ToMany()).thenReturn(false);
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String methodName = xProductAssociation.getMethodNameGetLinksFor();
        assertThat(methodName, is("getLinkForTestTarget"));
    }

    @Test
    public void testGetMethodNameGetLinkFor() throws Exception {
        when(association.getTargetRoleSingular()).thenReturn("testTarget");
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String methodName = xProductAssociation.getMethodNameGetLinkFor();

        assertThat(methodName, is("getLinkForTestTarget"));

        methodName = xProductAssociation.getMethodNameGetLinkFor();
        assertThat(methodName, is("getLinkForTestTarget"));
    }

    @Test
    public void testGetMethodGetCardinalityFor() throws Exception {
        IPolicyCmptTypeAssociation mockPolicyAsso = mock(IPolicyCmptTypeAssociation.class);
        when(mockPolicyAsso.getTargetRoleSingular()).thenReturn("polTarget");
        when(association.findMatchingPolicyCmptTypeAssociation(ipsProject)).thenReturn(mockPolicyAsso);
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String methodName = xProductAssociation.getMethodNameGetCardinalityFor();
        assertThat(methodName, is("getCardinalityForPolTarget"));
    }

    @Test
    public void testHasMatchingAssociation() throws Exception {
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);

        when(association.constrainsPolicyCmptTypeAssociation(any(IIpsProject.class))).thenReturn(true);
        assertThat(xProductAssociation.hasMatchingAssociation(), is(true));

        when(association.constrainsPolicyCmptTypeAssociation(any(IIpsProject.class))).thenReturn(false);
        assertThat(xProductAssociation.hasMatchingAssociation(), is(false));
    }

    @Test
    public void testIsCardinalityConfigurable_true() {
        IPolicyCmptTypeAssociation mockPolicyAsso = mock(IPolicyCmptTypeAssociation.class);
        when(mockPolicyAsso.isCardinalityConfigurable()).thenReturn(true);
        when(association.findMatchingPolicyCmptTypeAssociation(ipsProject)).thenReturn(mockPolicyAsso);

        assertThat(xProductAssociation.isCardinalityConfigurable(), is(true));
    }

    @Test
    public void testIsCardinalityConfigurable_falseWhenDerivedUnion() {
        when(association.isDerivedUnion()).thenReturn(true);

        assertThat(xProductAssociation.isCardinalityConfigurable(), is(false));
    }

    @Test
    public void testIsCardinalityConfigurable_falseWhenConstrain() {
        when(association.isConstrain()).thenReturn(true);

        assertThat(xProductAssociation.isCardinalityConfigurable(), is(false));
    }

    @Test
    public void testIsCardinalityConfigurable_falseWhenBothDerivedUnionAndConstrain() {
        when(association.isDerivedUnion()).thenReturn(true);

        assertThat(xProductAssociation.isCardinalityConfigurable(), is(false));
    }

    @Test
    public void testIsCardinalityConfigurable_falseWhenNoMatchingAssociation() {
        when(association.findMatchingPolicyCmptTypeAssociation(ipsProject)).thenReturn(null);

        assertThat(xProductAssociation.isCardinalityConfigurable(), is(false));
    }

    @Test
    public void testIsCardinalityConfigurable_falseWhenNotConfigurable() {
        IPolicyCmptTypeAssociation mockPolicyAsso = mock(IPolicyCmptTypeAssociation.class);
        when(mockPolicyAsso.isCardinalityConfigurable()).thenReturn(false);
        when(association.findMatchingPolicyCmptTypeAssociation(ipsProject)).thenReturn(mockPolicyAsso);

        assertThat(xProductAssociation.isCardinalityConfigurable(), is(false));
    }

    @Test
    public void testGetFieldNameCardinality() {
        IPolicyCmptTypeAssociation mockPolicyAsso = mock(IPolicyCmptTypeAssociation.class);
        when(mockPolicyAsso.getTargetRoleSingular()).thenReturn("coverage");
        when(association.findMatchingPolicyCmptTypeAssociation(ipsProject)).thenReturn(mockPolicyAsso);

        assertThat(xProductAssociation.getFieldNameCardinality(), is("cardinalityOfCoverage"));
    }

    @Test
    public void testGetMethodNameSetCardinalityFor() {
        IPolicyCmptTypeAssociation mockPolicyAsso = mock(IPolicyCmptTypeAssociation.class);
        when(mockPolicyAsso.getTargetRoleSingular()).thenReturn("coverage");
        when(association.findMatchingPolicyCmptTypeAssociation(ipsProject)).thenReturn(mockPolicyAsso);

        assertThat(xProductAssociation.getMethodNameSetCardinalityFor(), is("setCardinalityForCoverage"));
    }

    @Test
    public void testGetMethodNameDoInitCardinalityFromXml() {
        IPolicyCmptTypeAssociation mockPolicyAsso = mock(IPolicyCmptTypeAssociation.class);
        when(mockPolicyAsso.getTargetRoleSingular()).thenReturn("coverage");
        when(association.findMatchingPolicyCmptTypeAssociation(ipsProject)).thenReturn(mockPolicyAsso);

        assertThat(xProductAssociation.getMethodNameDoInitCardinalityFromXml(),
                is("doInitCardinalityOfCoverageFromXml"));
    }

    @Test
    public void testGetMethodNameWriteCardinalityToXml() {
        IPolicyCmptTypeAssociation mockPolicyAsso = mock(IPolicyCmptTypeAssociation.class);
        when(mockPolicyAsso.getTargetRoleSingular()).thenReturn("coverage");
        when(association.findMatchingPolicyCmptTypeAssociation(ipsProject)).thenReturn(mockPolicyAsso);

        assertThat(xProductAssociation.getMethodNameWriteCardinalityToXml(),
                is("writeCardinalityOfCoverageToXml"));
    }

    @Test
    public void testMatchingMethod_RegistersGeneratedJavaElementForPolicyAndProductAssociation() {
        IPolicyCmptTypeAssociation policyAssociation = mock(IPolicyCmptTypeAssociation.class);
        when(association.findMatchingAssociation()).thenReturn(policyAssociation);
        XPolicyAssociation xPolicyAssociation = new XPolicyAssociation(policyAssociation, modelContext, modelService);
        when(modelService.getModelNode(policyAssociation, XPolicyAssociation.class, modelContext)).thenReturn(
                xPolicyAssociation);
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);

        String method = xProductAssociation.matchingMethod("foo", "String", "bar", "int", "baz");

        assertThat(method, is("foo(String bar, int baz)"));
        ArgumentCaptor<MethodDefinition> methodCaptor = ArgumentCaptor.forClass(MethodDefinition.class);
        verify(modelContext).addGeneratedJavaElement(eq(xPolicyAssociation), methodCaptor.capture());
        assertThat(methodCaptor.getValue().getName(), is("foo"));
        verify(modelContext).addGeneratedJavaElement(eq(xProductAssociation), methodCaptor.capture());
        assertThat(methodCaptor.getValue().getName(), is("foo"));
    }

}
