/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.productcmpt;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.faktorips.devtools.model.internal.builder.JavaNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelCaches;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XProductCmptClassTest {

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private GeneratorConfig generatorConfig;

    @Mock
    private ModelService modelService;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IProductCmptType superType;

    @Mock
    private IProductCmptType superSuperType;

    private XProductCmptClass xProductCmptClass;

    @Before
    public void initMocks() throws Exception {
        when(modelContext.getBaseGeneratorConfig()).thenReturn(generatorConfig);
        when(modelContext.getGeneratorModelCache()).thenReturn(new GeneratorModelCaches());
        when(productCmptType.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.getJavaNamingConvention()).thenReturn(new JavaNamingConvention());
        when(productCmptType.findSupertype(ipsProject)).thenReturn(superType);
        when(superType.findSupertype(ipsProject)).thenReturn(superSuperType);
        xProductCmptClass = new XProductCmptClass(productCmptType, modelContext, modelService);
    }

    @Test
    public void testGetBaseSuperclassName() throws Exception {
        when(generatorConfig.getBaseClassProductCmptType()).thenReturn("pack.MyBaseClass");
        when(modelContext.addImport("pack.MyBaseClass")).thenReturn("MyBaseClass");

        String baseSuperclassName = xProductCmptClass.getBaseSuperclassName();

        assertEquals("MyBaseClass", baseSuperclassName);
        verify(modelContext).addImport("pack.MyBaseClass");
    }

    @Test
    public void testGetMethodNameGetProductCmpt() throws Exception {
        when(productCmptType.getName()).thenReturn("testType");
        assertEquals("getTestType", xProductCmptClass.getMethodNameGetProductCmpt());
    }

    @Test
    public void testGetClassHierarchy_productCmptClass() throws Exception {
        XProductCmptClass xSuperType = mock(XProductCmptClass.class);
        XProductCmptClass xSuperSuperType = mock(XProductCmptClass.class);
        when(modelService.getModelNode(superType, XProductCmptClass.class, modelContext)).thenReturn(xSuperType);
        when(modelService.getModelNode(superSuperType, XProductCmptClass.class, modelContext))
                .thenReturn(xSuperSuperType);

        Set<XProductCmptClass> superclasses = xProductCmptClass.getClassHierarchy();
        assertEquals(3, superclasses.size());
        assertThat(superclasses, hasItems(xSuperType, xSuperSuperType));
    }

    @Test
    public void testIsGenerateGenerationAccessMethods_isChangingOverTime_true() throws Exception {
        when(productCmptType.isChangingOverTime()).thenReturn(true);
        assertTrue(xProductCmptClass.isGenerateGenerationAccessMethods());
    }

    @Test
    public void testIsGenerateGenerationAccessMethods_isChangingOverTime_false() throws Exception {
        when(productCmptType.isChangingOverTime()).thenReturn(false);
        assertFalse(xProductCmptClass.isGenerateGenerationAccessMethods());
    }

    @Test
    public void testIsGenerateIsChangingOverTimeAccessMethod_trueIfNoSupertype() {
        when(productCmptType.hasSupertype()).thenReturn(false);
        assertTrue(xProductCmptClass.isGenerateIsChangingOverTimeAccessMethod());
    }

    @Test
    public void testIsGenerateIsChangingOverTimeAccessMethod_falseIfSupertype() {
        when(productCmptType.hasSupertype()).thenReturn(true);
        assertFalse(xProductCmptClass.isGenerateIsChangingOverTimeAccessMethod());
    }

    @Test
    public void testGetConfiguredAttributes() {
        // setup
        XPolicyAttribute xNonChangingAttribute = mock(XPolicyAttribute.class);
        when(xNonChangingAttribute.isProductRelevant()).thenReturn(true);
        when(xNonChangingAttribute.isChangingOverTime()).thenReturn(false);
        when(xNonChangingAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue()).thenReturn(true);
        XPolicyAttribute xChangingAttribute = mock(XPolicyAttribute.class);
        when(xChangingAttribute.isProductRelevant()).thenReturn(true);
        when(xChangingAttribute.isChangingOverTime()).thenReturn(true);
        when(xChangingAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue()).thenReturn(true);

        XPolicyCmptClass xPolicyCmptClass = mock(XPolicyCmptClass.class);
        when(xPolicyCmptClass.isConfiguredBy(productCmptType.getQualifiedName())).thenReturn(true);
        when(xPolicyCmptClass.getAttributes())
                .thenReturn(new HashSet<>(Arrays.asList(xNonChangingAttribute, xChangingAttribute)));

        XProductCmptClass xProductCmptClassSpy = spy(xProductCmptClass);
        doReturn(xPolicyCmptClass).when(xProductCmptClassSpy).getPolicyCmptClass();
        when(xProductCmptClassSpy.isConfigurationForPolicyCmptType()).thenReturn(true);

        // execute
        Set<XPolicyAttribute> configuredAttributes = xProductCmptClassSpy.getConfiguredAttributes();

        // verify
        assertThat(configuredAttributes, hasItem(xNonChangingAttribute));
        assertThat(configuredAttributes, not(hasItem(xChangingAttribute)));
    }

}
