/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGenerator;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

    @Mock
    private IIpsProject ipsProject;

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
        when(attribute.getModifier()).thenReturn(Modifier.PUBLISHED);

        String annotations = xAttribute.getAnnotationsForPublishedInterfaceModifierRelevant(
                AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, true);

        assertThat(annotations.trim(), is(TEST_ANNOTATION));
    }

    @Test
    public void testGetAnnotationsForPublishedInterfaceModifierRelevant_Public_GenInterface_HasInterface()
            throws Exception {
        setUpMocks();
        when(attribute.getModifier()).thenReturn(Modifier.PUBLIC);

        String annotations = xAttribute.getAnnotationsForPublishedInterfaceModifierRelevant(
                AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, true);

        assertThat(annotations.trim(), is(IpsStringUtils.EMPTY));
    }

    @Test
    public void testGetAnnotationsForPublishedInterfaceModifierRelevant_Published_NotGenInterface_HasInterface()
            throws Exception {
        setUpMocks();
        when(generatorConfig.isGeneratePublishedInterfaces(any(IIpsProject.class))).thenReturn(true);
        when(attribute.getModifier()).thenReturn(Modifier.PUBLISHED);
        when(attribute.getIpsProject()).thenReturn(ipsProject);

        String annotations = xAttribute.getAnnotationsForPublishedInterfaceModifierRelevant(
                AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, false);

        assertThat(annotations.trim(), is(IpsStringUtils.EMPTY));
    }

    @Test
    public void testGetAnnotationsForPublishedInterfaceModifierRelevant_Public_NotGenInterface_HasInterface()
            throws Exception {
        setUpMocks();
        when(attribute.getModifier()).thenReturn(Modifier.PUBLIC);

        String annotations = xAttribute.getAnnotationsForPublishedInterfaceModifierRelevant(
                AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, false);

        assertThat(annotations.trim(), is(TEST_ANNOTATION));
    }

    @Test
    public void testGetAnnotationsForPublishedInterfaceModifierRelevant_Published_NotGenInterface_HasNoInterface()
            throws Exception {
        setUpMocks();
        when(attribute.getModifier()).thenReturn(Modifier.PUBLISHED);

        String annotations = xAttribute.getAnnotationsForPublishedInterfaceModifierRelevant(
                AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, false);

        assertThat(annotations.trim(), is(TEST_ANNOTATION));
    }

    @Test
    public void testGetAnnotationsForPublishedInterfaceModifierRelevant_Public_NotGenInterface_HasNoInterface()
            throws Exception {
        setUpMocks();
        when(attribute.getModifier()).thenReturn(Modifier.PUBLIC);

        String annotations = xAttribute.getAnnotationsForPublishedInterfaceModifierRelevant(
                AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, false);

        assertThat(annotations.trim(), is(TEST_ANNOTATION));
    }

    @Test
    public void testGetValuesetCode_range_openBounds() {
        assertGetValuesetCode_rangePassesBoundOpenFlags(true, true);
    }

    @Test
    public void testGetValuesetCode_range_closedBounds() {
        assertGetValuesetCode_rangePassesBoundOpenFlags(false, false);
    }

    private void assertGetValuesetCode_rangePassesBoundOpenFlags(boolean lowerBoundOpen, boolean upperBoundOpen) {
        setUpMocks();
        TestXAttribute xAttributeSpy = spy(new TestXAttribute(attribute, context, modelService));

        IRangeValueSet rangeValueSet = mock(IRangeValueSet.class);
        when(rangeValueSet.getValueSetType()).thenReturn(ValueSetType.RANGE);
        when(rangeValueSet.isEmpty()).thenReturn(false);
        when(rangeValueSet.isLowerBoundOpen()).thenReturn(lowerBoundOpen);
        when(rangeValueSet.isUpperBoundOpen()).thenReturn(upperBoundOpen);
        when(rangeValueSet.isContainsNull()).thenReturn(false);
        when(rangeValueSet.getLowerBound()).thenReturn("1");
        when(rangeValueSet.getUpperBound()).thenReturn("100");
        when(rangeValueSet.getStep()).thenReturn(null);
        when(attribute.getValueSet()).thenReturn(rangeValueSet);

        DatatypeHelper valuesetHelper = mock(DatatypeHelper.class);
        when(valuesetHelper.newRangeInstance(any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(new JavaCodeFragment("IntegerRange.valueOf(1, 100, null, false, "
                        + lowerBoundOpen + ", " + upperBoundOpen + ")"));
        when(valuesetHelper.createCastExpression(any()))
                .thenAnswer(inv -> new JavaCodeFragment(inv.getArgument(0, String.class)));
        doReturn(valuesetHelper).when(xAttributeSpy).getValuesetDatatypeHelper();

        xAttributeSpy.getValuesetCode();

        ArgumentCaptor<JavaCodeFragment> lowerBoundOpenCaptor = ArgumentCaptor.forClass(JavaCodeFragment.class);
        ArgumentCaptor<JavaCodeFragment> upperBoundOpenCaptor = ArgumentCaptor.forClass(JavaCodeFragment.class);
        verify(valuesetHelper).newRangeInstance(any(), any(), any(), any(),
                lowerBoundOpenCaptor.capture(), upperBoundOpenCaptor.capture(), eq(true));
        assertThat(lowerBoundOpenCaptor.getValue().getSourcecode(), is(String.valueOf(lowerBoundOpen)));
        assertThat(upperBoundOpenCaptor.getValue().getSourcecode(), is(String.valueOf(upperBoundOpen)));
    }

    private static class TestXAttribute extends XAttribute {

        private TestXAttribute(IAttribute attribute, GeneratorModelContext context, ModelService modelService) {
            super(attribute, context, modelService);
        }

    }

}
