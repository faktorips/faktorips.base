/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XAttributeTest {

    private static final String TEST_ANNOTATION = "@TestAnnotation";

    @Mock
    private IAttribute attribute;

    @Mock
    private GeneratorModelContext context;

    @Mock
    private GeneratorConfig generatorConfig;

    @Mock
    private ModelService modelService;

    @Mock
    private IAnnotationGenerator annotationGenerator;

    private XAttribute xAttribute;

    public void setUpMocks() {
        when(context.getBaseGeneratorConfig()).thenReturn(generatorConfig);
        xAttribute = new TestXAttribute(attribute, context, modelService);
        when(annotationGenerator.createAnnotation(xAttribute)).thenReturn(new JavaCodeFragment(TEST_ANNOTATION));
        when(annotationGenerator.isGenerateAnnotationFor(xAttribute)).thenReturn(true);
        when(context.getAnnotationGenerator(AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER))
                .thenReturn(Arrays.asList(annotationGenerator));
    }

    @Test
    public void testGetAnnotationsForPublishedInterfaceModifierRelevant_Published_GenInterface_HasInterface()
            throws Exception {
        setUpMocks();
        when(generatorConfig.isGeneratePublishedInterfaces(any(IIpsProject.class))).thenReturn(true);
        when(attribute.getModifier()).thenReturn(Modifier.PUBLISHED);

        String annotations = xAttribute.getAnnotationsForPublishedInterfaceModifierRelevant(
                AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, true);

        assertThat(annotations.trim(), is(TEST_ANNOTATION));
    }

    @Test
    public void testGetAnnotationsForPublishedInterfaceModifierRelevant_Public_GenInterface_HasInterface()
            throws Exception {
        setUpMocks();
        when(generatorConfig.isGeneratePublishedInterfaces(any(IIpsProject.class))).thenReturn(true);
        when(attribute.getModifier()).thenReturn(Modifier.PUBLIC);

        String annotations = xAttribute.getAnnotationsForPublishedInterfaceModifierRelevant(
                AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, true);

        assertThat(annotations.trim(), is(StringUtils.EMPTY));
    }

    @Test
    public void testGetAnnotationsForPublishedInterfaceModifierRelevant_Published_NotGenInterface_HasInterface()
            throws Exception {
        setUpMocks();
        when(generatorConfig.isGeneratePublishedInterfaces(any(IIpsProject.class))).thenReturn(true);
        when(attribute.getModifier()).thenReturn(Modifier.PUBLISHED);

        String annotations = xAttribute.getAnnotationsForPublishedInterfaceModifierRelevant(
                AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, false);

        assertThat(annotations.trim(), is(StringUtils.EMPTY));
    }

    @Test
    public void testGetAnnotationsForPublishedInterfaceModifierRelevant_Public_NotGenInterface_HasInterface()
            throws Exception {
        setUpMocks();
        when(generatorConfig.isGeneratePublishedInterfaces(any(IIpsProject.class))).thenReturn(true);
        when(attribute.getModifier()).thenReturn(Modifier.PUBLIC);

        String annotations = xAttribute.getAnnotationsForPublishedInterfaceModifierRelevant(
                AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, false);

        assertThat(annotations.trim(), is(TEST_ANNOTATION));
    }

    @Test
    public void testGetAnnotationsForPublishedInterfaceModifierRelevant_Published_NotGenInterface_HasNoInterface()
            throws Exception {
        setUpMocks();
        when(generatorConfig.isGeneratePublishedInterfaces(any(IIpsProject.class))).thenReturn(false);
        when(attribute.getModifier()).thenReturn(Modifier.PUBLISHED);

        String annotations = xAttribute.getAnnotationsForPublishedInterfaceModifierRelevant(
                AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, false);

        assertThat(annotations.trim(), is(TEST_ANNOTATION));
    }

    @Test
    public void testGetAnnotationsForPublishedInterfaceModifierRelevant_Public_NotGenInterface_HasNoInterface()
            throws Exception {
        setUpMocks();
        when(generatorConfig.isGeneratePublishedInterfaces(any(IIpsProject.class))).thenReturn(false);
        when(attribute.getModifier()).thenReturn(Modifier.PUBLIC);

        String annotations = xAttribute.getAnnotationsForPublishedInterfaceModifierRelevant(
                AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, false);

        assertThat(annotations.trim(), is(TEST_ANNOTATION));
    }

    private static class TestXAttribute extends XAttribute {

        private TestXAttribute(IAttribute attribute, GeneratorModelContext context, ModelService modelService) {
            super(attribute, context, modelService);
        }

    }

}
