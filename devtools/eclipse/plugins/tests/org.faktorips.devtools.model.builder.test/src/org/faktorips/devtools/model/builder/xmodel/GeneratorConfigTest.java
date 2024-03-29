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
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class GeneratorConfigTest {

    @Mock
    private IIpsArtefactBuilderSetConfig config;

    @Mock
    private IIpsProject ipsProject;

    private GeneratorConfig generatorConfig;

    @Before
    public void createGeneratorConfig() throws Exception {
        generatorConfig = new GeneratorConfig(config, ipsProject);
    }

    @Test
    public void testIsGenerateSerializablePolicyCmptSupport_Default() {
        assertFalse(generatorConfig.isGenerateSerializablePolicyCmptSupport());
    }

    @Test
    public void testGetBaseClassPolicyCmptType() {
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_BASE_CLASS_POLICY_CMPT_TYPE))
                .thenReturn("org.faktorips.FooBar");
        assertThat(generatorConfig.getBaseClassPolicyCmptType(), is("org.faktorips.FooBar"));
    }

    @Test
    public void testGetBaseClassPolicyCmptType_default() {
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_BASE_CLASS_POLICY_CMPT_TYPE))
                .thenReturn("");
        assertThat(generatorConfig.getBaseClassPolicyCmptType(), is(AbstractModelObject.class.getName()));
    }

    @Test
    public void testGetBaseClassPolicyCmptType_defaultJaxb() {
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_BASE_CLASS_POLICY_CMPT_TYPE))
                .thenReturn("");
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT))
                .thenReturn("true");
        assertThat(generatorConfig.getBaseClassPolicyCmptType(),
                is("org.faktorips.runtime.xml.javax.AbstractJaxbModelObject"));
    }

    @Test
    public void testGetBaseClassPolicyCmptType_Jakarta() {
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_BASE_CLASS_POLICY_CMPT_TYPE))
                .thenReturn("");
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT))
                .thenReturn("jakartaxmlbinding");
        assertThat(generatorConfig.getBaseClassPolicyCmptType(),
                is("org.faktorips.runtime.xml.jakarta.AbstractJaxbModelObject"));
    }

    @Test
    public void testGetChangesOverTimeNamingConvention() {
        String id = "FIPS";
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION))
                .thenReturn(id);
        IIpsModel ipsModel = mock(IIpsModel.class);
        when(ipsProject.getIpsModel()).thenReturn(ipsModel);
        IChangesOverTimeNamingConvention convention = mock(IChangesOverTimeNamingConvention.class);
        when(ipsModel.getChangesOverTimeNamingConvention(id)).thenReturn(convention);

        assertThat(generatorConfig.getChangesOverTimeNamingConvention(), is(convention));
        verify(ipsModel).getChangesOverTimeNamingConvention(id);
    }

    @Test
    public void testIsGeneratePublishedInterfaces_OwnProject() throws Exception {
        when(config.getPropertyValueAsBoolean(JavaBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES))
                .thenReturn(true);
        assertThat(generatorConfig.isGeneratePublishedInterfaces(ipsProject), is(true));
    }

    @Test
    public void testGenerateMinimalJavadoc_Default() throws Exception {
        Boolean propertyValueAsBoolean = config
                .getPropertyValueAsBoolean(JavaBuilderSet.CONFIG_PROPERTY_GENERATE_MINIMAL_JAVADOC);
        assertThat(propertyValueAsBoolean, is(false));
    }

    @SuppressWarnings("removal")
    @Test
    public void testGetJaxbSupport_Default() throws Exception {
        assertThat(generatorConfig.getJaxbSupport(), is(JaxbSupportVariant.None));
        assertThat(generatorConfig.isGenerateJaxbSupport(), is(false));
    }

    @SuppressWarnings("removal")
    @Test
    public void testGetJaxbSupport_OldValue_True() throws Exception {
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT))
                .thenReturn("true");
        assertThat(generatorConfig.getJaxbSupport(), is(JaxbSupportVariant.ClassicJAXB));
        assertThat(generatorConfig.isGenerateJaxbSupport(), is(true));
    }

    @SuppressWarnings("removal")
    @Test
    public void testGetJaxbSupport_OldValue_False() throws Exception {
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT))
                .thenReturn("false");
        assertThat(generatorConfig.getJaxbSupport(), is(JaxbSupportVariant.None));
        assertThat(generatorConfig.isGenerateJaxbSupport(), is(false));
    }

    @SuppressWarnings("removal")
    @Test
    public void testGetJaxbSupport_None() throws Exception {
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT))
                .thenReturn("none");
        assertThat(generatorConfig.getJaxbSupport(), is(JaxbSupportVariant.None));
        assertThat(generatorConfig.isGenerateJaxbSupport(), is(false));
    }

    @SuppressWarnings("removal")
    @Test
    public void testGetJaxbSupport_ClassicJAXB() throws Exception {
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT))
                .thenReturn("ClassicJAXB");
        assertThat(generatorConfig.getJaxbSupport(), is(JaxbSupportVariant.ClassicJAXB));
        assertThat(generatorConfig.isGenerateJaxbSupport(), is(true));
    }

    @SuppressWarnings("removal")
    @Test
    public void testGetJaxbSupport_JakartaXmlBinding() throws Exception {
        when(config.getPropertyValueAsString(JavaBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT))
                .thenReturn("JakartaXmlBinding");
        assertThat(generatorConfig.getJaxbSupport(), is(JaxbSupportVariant.JakartaXmlBinding));
        assertThat(generatorConfig.isGenerateJaxbSupport(), is(true));
    }

}
