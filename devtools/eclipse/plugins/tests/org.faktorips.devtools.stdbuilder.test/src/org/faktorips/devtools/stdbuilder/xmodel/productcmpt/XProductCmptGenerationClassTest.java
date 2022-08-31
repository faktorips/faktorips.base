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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.XMethod;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelCaches;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XProductCmptGenerationClassTest {

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private IProductCmptType superType;

    @Mock
    private IProductCmptType superSuperType;

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private GeneratorConfig generatorConfig;

    @Mock
    private ModelService modelService;

    @Mock
    private IIpsProject ipsProject;

    private XProductCmptGenerationClass xProductCmptGenerationClass;

    @Before
    public void initMocks() {
        when(modelContext.getBaseGeneratorConfig()).thenReturn(generatorConfig);
        when(modelContext.getGeneratorModelCache()).thenReturn(new GeneratorModelCaches());
        when(productCmptType.getQualifiedName()).thenReturn("ProductCmptType");
        when(productCmptType.getIpsProject()).thenReturn(ipsProject);
        when(productCmptType.findSupertype(ipsProject)).thenReturn(superType);
        when(superType.findSupertype(ipsProject)).thenReturn(superSuperType);
        xProductCmptGenerationClass = new XProductCmptGenerationClass(productCmptType, modelContext, modelService);
    }

    @Test
    public void testGetClassHierarchy_productCmptGenerationClass() throws Exception {
        XProductCmptGenerationClass xSuperType = mock(XProductCmptGenerationClass.class);
        XProductCmptGenerationClass xSuperSuperType = mock(XProductCmptGenerationClass.class);
        when(modelService.getModelNode(superType, XProductCmptGenerationClass.class, modelContext))
                .thenReturn(xSuperType);
        when(modelService.getModelNode(superSuperType, XProductCmptGenerationClass.class, modelContext))
                .thenReturn(xSuperSuperType);

        Set<XProductCmptGenerationClass> superclasses = xProductCmptGenerationClass.getClassHierarchy();
        assertEquals(3, superclasses.size());
        assertThat(superclasses, hasItems(xSuperType, xSuperSuperType));
    }

    @Test
    public void testOptionalFormulas() {
        final IProductCmptTypeMethod superMandatoryMethod = mock(IProductCmptTypeMethod.class);
        when(superMandatoryMethod.isFormulaMandatory()).thenReturn(true);

        final IProductCmptTypeMethod superOptionalMethod = mock(IProductCmptTypeMethod.class);
        when(superOptionalMethod.isFormulaMandatory()).thenReturn(false);
        when(superOptionalMethod.isOverloadsFormula()).thenReturn(false);

        final IProductCmptTypeMethod optionalOverloadedMethod = mock(IProductCmptTypeMethod.class);
        when(optionalOverloadedMethod.isFormulaMandatory()).thenReturn(false);
        when(optionalOverloadedMethod.isOverloadsFormula()).thenReturn(true);

        XProductCmptGenerationClass xSuperType = new XProductCmptGenerationClass(superType, modelContext,
                modelService) {
            @Override
            public Set<XMethod> getMethods() {
                return new HashSet<>(
                        Arrays.asList(new XMethod(superOptionalMethod, modelContext, modelService),
                                new XMethod(superMandatoryMethod, modelContext, modelService),
                                new XMethod(optionalOverloadedMethod, modelContext, modelService)));
            }
        };

        Set<XMethod> optionalFormulas = xSuperType.getOptionalFormulas();

        assertEquals(1, optionalFormulas.size());
        XMethod method = optionalFormulas.iterator().next();
        assertEquals(superOptionalMethod, method.getMethod());
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

        XProductCmptGenerationClass xGenerationClassSpy = spy(xProductCmptGenerationClass);
        doReturn(xPolicyCmptClass).when(xGenerationClassSpy).getPolicyCmptClass();
        when(xGenerationClassSpy.isConfigurationForPolicyCmptType()).thenReturn(true);

        // execute
        Set<XPolicyAttribute> configuredAttributes = xGenerationClassSpy.getConfiguredAttributes();

        // verify
        assertThat(configuredAttributes, not(hasItem(xNonChangingAttribute)));
        assertThat(configuredAttributes, hasItem(xChangingAttribute));
    }

}
