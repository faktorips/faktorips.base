/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.internal.AbstractJaxbModelObject;
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
        when(config.getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_BASE_CLASS_POLICY_CMPT_TYPE))
                .thenReturn("org.faktorips.FooBar");
        assertThat(generatorConfig.getBaseClassPolicyCmptType(), is("org.faktorips.FooBar"));
    }

    @Test
    public void testGetBaseClassPolicyCmptType_default() {
        when(config.getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_BASE_CLASS_POLICY_CMPT_TYPE))
                .thenReturn("");
        assertThat(generatorConfig.getBaseClassPolicyCmptType(), is(AbstractModelObject.class.getName()));
    }

    @Test
    public void testGetBaseClassPolicyCmptType_defaultJaxb() {
        when(config.getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_BASE_CLASS_POLICY_CMPT_TYPE))
                .thenReturn("");
        when(config.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT))
                .thenReturn(true);
        assertThat(generatorConfig.getBaseClassPolicyCmptType(), is(AbstractJaxbModelObject.class.getName()));
    }

    @Test
    public void testGetChangesOverTimeNamingConvention() {
        String id = "FIPS";
        when(config.getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION))
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
        when(config.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES))
                .thenReturn(true);
        assertThat(generatorConfig.isGeneratePublishedInterfaces(ipsProject), is(true));
    }

    @Test
    public void testIsGeneratePublishedInterfaces_OtherProject() throws Exception {
        IIpsArtefactBuilderSetConfig otherConfig = mock(IIpsArtefactBuilderSetConfig.class);
        when(otherConfig.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES))
                .thenReturn(false);
        IIpsArtefactBuilderSet builderSet = mock(IIpsArtefactBuilderSet.class);
        when(builderSet.getConfig()).thenReturn(otherConfig);
        IIpsProject otherProject = mock(IIpsProject.class);
        when(otherProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);

        assertThat(generatorConfig.isGeneratePublishedInterfaces(otherProject), is(false));
    }

    @Test
    public void testGenerateMinimalJavadoc_Default() throws Exception {
        Boolean propertyValueAsBoolean = config
                .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_MINIMAL_JAVADOC);
        assertThat(propertyValueAsBoolean, is(false));
    }

}
