/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand;

import static org.junit.Assert.assertEquals;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GeneratorModelContextTest {

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

}
