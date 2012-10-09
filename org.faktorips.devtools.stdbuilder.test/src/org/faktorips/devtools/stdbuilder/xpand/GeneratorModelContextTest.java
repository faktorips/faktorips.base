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
import org.faktorips.devtools.stdbuilder.xpand.model.ImportHandler;
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
    private ImportHandler importHandler;

    @Mock
    private IJavaPackageStructure javaPackageStructure;

    private GeneratorModelContext generatorModelContext;

    @Before
    public void createGeneratorModelContext() throws Exception {
        generatorModelContext = new GeneratorModelContext(config, javaPackageStructure, annotationGeneratorMap);
        generatorModelContext.setImportHandler(importHandler);
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
