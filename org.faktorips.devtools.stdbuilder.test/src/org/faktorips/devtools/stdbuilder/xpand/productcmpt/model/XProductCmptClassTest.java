/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.runtime.internal.ProductComponent;
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
        when(productCmptType.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.getJavaNamingConvention()).thenReturn(new JavaNamingConvention());
        when(productCmptType.findSupertype(ipsProject)).thenReturn(superType);
        when(superType.findSupertype(ipsProject)).thenReturn(superSuperType);
    }

    @Before
    public void createXProductCmptClass() {
        xProductCmptClass = new XProductCmptClass(productCmptType, modelContext, modelService);
    }

    @Test
    public void testGetBaseSuperclassName() throws Exception {
        String baseSuperclassName = xProductCmptClass.getBaseSuperclassName();
        assertEquals(ProductComponent.class.getSimpleName(), baseSuperclassName);
        verify(modelContext).addImport(ProductComponent.class.getName());
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
        when(modelService.getModelNode(superSuperType, XProductCmptClass.class, modelContext)).thenReturn(
                xSuperSuperType);

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

}
