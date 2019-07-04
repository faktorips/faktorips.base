/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptGenerationClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XProductAssociationTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private ProductCmptType productCmptType;

    @Mock
    private ProductCmptType targetCmptType;

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
        when(productCmptType.getIpsProject()).thenReturn(ipsProject);
        when(association.getIpsProject()).thenReturn(ipsProject);
        when(association.findTarget(ipsProject)).thenReturn(targetCmptType);
        when(modelService.getModelNode(targetCmptType, XProductCmptGenerationClass.class, modelContext)).thenReturn(
                xTargetGenerationClass);
        when(xTargetGenerationClass.getSimpleName(BuilderAspect.INTERFACE)).thenReturn("ITargetTypeGen");
        when(modelService.getModelNode(targetCmptType, XProductCmptClass.class, modelContext)).thenReturn(
                xTargetCmptClass);
        when(xTargetCmptClass.getSimpleName(BuilderAspect.INTERFACE)).thenReturn("ITargetType");
    }

    @Before
    public void createXProductAssociation() {
        xProductAssociation = new XProductAssociation(association, modelContext, modelService);
    }

    @Test
    public void testGetTargetClassGenerationName() {
        when(targetCmptType.isChangingOverTime()).thenReturn(true);
        String targetClassGenerationName = xProductAssociation.getTargetClassGenerationName();
        assertEquals("ITargetTypeGen", targetClassGenerationName);
    }

    @Test
    public void testGetGetterNameForTargetGeneration() throws Exception {
        association.setTarget("test.TargetType");
        when(xTargetGenerationClass.getMethodNameGetProductComponentGeneration()).thenReturn("getTargetTypeGen");
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String getterNameForTargetGeneration = xProductAssociation.getMethodNameGetTargetGeneration();
        assertEquals("getTargetTypeGen", getterNameForTargetGeneration);
    }

    @Test
    public void testGetTargetClassProductComponentName() {
        String targetClassProductName = xProductAssociation.getTargetClassGenerationName();
        assertEquals("ITargetTypeGen", targetClassProductName);
    }

    @Test
    public void testGetMethodNameGetLinksFor_plural() throws Exception {
        when(association.getTargetRolePlural()).thenReturn("testTargets");
        when(association.is1ToMany()).thenReturn(true);
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String methodName = xProductAssociation.getMethodNameGetLinksFor();
        assertEquals("getLinksForTestTargets", methodName);
    }

    @Test
    public void testGetMethodNameGetLinksFor_singular() throws Exception {
        when(association.getTargetRoleSingular()).thenReturn("testTarget");
        when(association.is1ToMany()).thenReturn(false);
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String methodName = xProductAssociation.getMethodNameGetLinksFor();
        assertEquals("getLinkForTestTarget", methodName);
    }

    @Test
    public void testGetMethodNameGetLinkFor() throws Exception {
        when(association.getTargetRoleSingular()).thenReturn("testTarget");
        when(association.getTargetRolePlural()).thenReturn("testTargets");
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String methodName = xProductAssociation.getMethodNameGetLinkFor();

        assertEquals("getLinkForTestTarget", methodName);

        when(association.is1ToMany()).thenReturn(true);
        methodName = xProductAssociation.getMethodNameGetLinkFor();
        assertEquals("getLinkForTestTarget", methodName);
    }

    @Test
    public void testGetMethodGetCardinalityFor() throws Exception {
        IPolicyCmptTypeAssociation mockPolicyAsso = mock(IPolicyCmptTypeAssociation.class);
        when(mockPolicyAsso.getTargetRoleSingular()).thenReturn("polTarget");
        when(association.findMatchingPolicyCmptTypeAssociation(ipsProject)).thenReturn(mockPolicyAsso);
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String methodName = xProductAssociation.getMethodNameGetCardinalityFor();
        assertEquals("getCardinalityForPolTarget", methodName);
    }

    @Test
    public void testHasMatchingAssociation() throws Exception {
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);

        when(association.constrainsPolicyCmptTypeAssociation(any(IIpsProject.class))).thenReturn(true);
        assertTrue(xProductAssociation.hasMatchingAssociation());

        when(association.constrainsPolicyCmptTypeAssociation(any(IIpsProject.class))).thenReturn(false);
        assertFalse(xProductAssociation.hasMatchingAssociation());
    }

}
