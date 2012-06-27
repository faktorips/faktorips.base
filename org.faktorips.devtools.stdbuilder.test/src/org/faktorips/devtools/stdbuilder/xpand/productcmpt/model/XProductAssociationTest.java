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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ImportHandler;
import org.faktorips.devtools.stdbuilder.xpand.model.ImportStatement;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class XProductAssociationTest extends AbstractIpsPluginTest {

    @Mock
    private IProductCmptTypeAssociation mockAssociation;

    @Mock
    private IIpsProject mockProject;

    @Mock
    private IIpsArtefactBuilderSetConfig config;

    private GeneratorModelContext modelContext;

    private ModelService modelService;

    private IIpsProject ipsProject;

    private ProductCmptType productCmptType;

    private IProductCmptTypeAssociation association;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        modelContext = new GeneratorModelContext(config);
        modelContext.setImportHandler(new ImportHandler());
        modelService = new ModelService();
        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "test.TestType");
        association = productCmptType.newProductCmptTypeAssociation();
        newProductCmptType(ipsProject, "test.TargetType");

        when(mockAssociation.getIpsProject()).thenReturn(mockProject);
        when(mockProject.getJavaNamingConvention()).thenReturn(new JavaNamingConvention());
    }

    @Test
    public void testGetTargetClassGenerationName() {
        association.setTarget("test.TargetType");
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String targetClassGenerationName = xProductAssociation.getTargetClassGenerationName();
        assertEquals("ITargetTypeGen", targetClassGenerationName);
        assertTrue(modelContext.getImports().contains(
                new ImportStatement("org.faktorips.sample.model.test.ITargetTypeGen")));
    }

    @Test
    public void testGetGetterNameForTargetGeneration() throws Exception {
        association.setTarget("test.TargetType");
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String getterNameForTargetGeneration = xProductAssociation.getMethodNameGetTargetGeneration();
        assertEquals("getTargetTypeGen", getterNameForTargetGeneration);
    }

    @Test
    public void testGetMethodNameGetLinksFor_plural() throws Exception {
        association.setTargetRolePlural("testTargets");
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String methodName = xProductAssociation.getMethodNameGetLinksFor();
        assertEquals("getLinksForTestTargets", methodName);
    }

    @Test
    public void testGetMethodNameGetLinksFor_singular() throws Exception {
        association.setTargetRoleSingular("testTarget");
        association.setMaxCardinality(1);
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String methodName = xProductAssociation.getMethodNameGetLinksFor();
        assertEquals("getLinkForTestTarget", methodName);
    }

    @Test
    public void testGetMethodNameGetLinkFor() throws Exception {
        association.setTargetRoleSingular("testTarget");
        association.setTargetRolePlural("testTargets");
        XProductAssociation xProductAssociation = new XProductAssociation(association, modelContext, modelService);
        String methodName = xProductAssociation.getMethodNameGetLinkFor();
        assertEquals("getLinkForTestTarget", methodName);
        association.setMaxCardinality(1);
        methodName = xProductAssociation.getMethodNameGetLinkFor();
        assertEquals("getLinkForTestTarget", methodName);
    }

    @Test
    public void testGetMethodGetCardinalityFor() throws Exception {
        IPolicyCmptTypeAssociation mockPolicyAsso = mock(IPolicyCmptTypeAssociation.class);
        when(mockPolicyAsso.getTargetRoleSingular()).thenReturn("polTarget");
        when(mockAssociation.findMatchingPolicyCmptTypeAssociation(mockProject)).thenReturn(mockPolicyAsso);
        XProductAssociation xProductAssociation = new XProductAssociation(mockAssociation, modelContext, modelService);
        String methodName = xProductAssociation.getMethodNameGetCardinalityFor();
        assertEquals("getCardinalityForPolTarget", methodName);
    }

    @Test
    public void testHasMatchingAssociation() throws Exception {
        XProductAssociation xProductAssociation = new XProductAssociation(mockAssociation, modelContext, modelService);

        when(mockAssociation.constrainsPolicyCmptTypeAssociation(any(IIpsProject.class))).thenReturn(true);
        assertTrue(xProductAssociation.hasMatchingAssociation());

        when(mockAssociation.constrainsPolicyCmptTypeAssociation(any(IIpsProject.class))).thenReturn(false);
        assertFalse(xProductAssociation.hasMatchingAssociation());
    }

}
