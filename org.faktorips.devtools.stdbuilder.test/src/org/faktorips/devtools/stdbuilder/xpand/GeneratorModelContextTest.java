/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GeneratorModelContextTest {

    private static final String TEST_STRING = "testString";

    @Mock
    private Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorMap;

    @Mock
    private IIpsArtefactBuilderSetConfig config;

    @Mock
    private IJavaPackageStructure javaPackageStructure;

    private GeneratorModelContext generatorModelContext;

    @Before
    public void createGeneratorModelContext() throws Exception {
        generatorModelContext = new GeneratorModelContext(config, javaPackageStructure, annotationGeneratorMap);
        generatorModelContext.resetContext("any");
    }

    @Test
    public void testGetAnnotationGenerator() throws Exception {
        List<IAnnotationGenerator> annotationGenerators = generatorModelContext
                .getAnnotationGenerator(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS);
        assertTrue(annotationGenerators.isEmpty());

        List<IAnnotationGenerator> policyCmptImplClassAnnotationGens = new ArrayList<IAnnotationGenerator>();
        policyCmptImplClassAnnotationGens.add(mock(IAnnotationGenerator.class));
        when(annotationGeneratorMap.get(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS)).thenReturn(
                policyCmptImplClassAnnotationGens);

        annotationGenerators = generatorModelContext
                .getAnnotationGenerator(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS);
        assertEquals(policyCmptImplClassAnnotationGens, annotationGenerators);
    }

    @Test
    public void testIsGenerateSerializablePolicyCmptSupport_Default() {
        assertFalse(generatorModelContext.isGenerateSerializablePolicyCmptSupport());
    }

    @Test
    public void testIsGenerateSerializablePolicyCmptSupport_True() {
        when(
                config.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_SERIALIZABLE_POLICY_CMPTS_SUPPORT))
                .thenReturn(true);
        assertTrue(generatorModelContext.isGenerateSerializablePolicyCmptSupport());
    }

    @Test
    public void testIsGenerateSerializablePolicyCmptSupport_False() {
        when(
                config.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_SERIALIZABLE_POLICY_CMPTS_SUPPORT))
                .thenReturn(false);
        assertFalse(generatorModelContext.isGenerateSerializablePolicyCmptSupport());
    }

    @Test
    public void testIsGenerateConvenienceGetters_False() {
        when(config.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CONVENIENCE_GETTERS))
                .thenReturn(false);
        assertFalse(generatorModelContext.isGenerateConvenienceGetters());
    }

    @Test
    public void testIsGenerateConvenienceGetters_True() {
        when(config.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CONVENIENCE_GETTERS))
                .thenReturn(true);
        assertTrue(generatorModelContext.isGenerateConvenienceGetters());
    }

}
