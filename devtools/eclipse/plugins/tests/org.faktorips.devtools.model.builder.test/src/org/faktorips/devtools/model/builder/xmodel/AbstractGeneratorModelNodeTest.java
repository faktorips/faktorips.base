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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.model.internal.DefaultVersion;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AbstractGeneratorModelNodeTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private GeneratorConfig generatorConfig;

    @Mock
    private ModelService modelService;

    @Mock
    private IPolicyCmptType type;

    private XClass xClass;

    @Before
    public void setUp() throws Exception {
        when(modelContext.getBaseGeneratorConfig()).thenReturn(generatorConfig);
        xClass = new XPolicyCmptClass(type, modelContext, modelService);
        when(modelContext.addImport(anyString())).thenReturn("dummyImportStatement");
    }

    @Test
    public void testGetGeneratorConfig_Base() {
        assertThat(xClass.getGeneratorConfig(), is(generatorConfig));
        verify(modelContext, atLeastOnce()).getBaseGeneratorConfig();
    }

    @Test
    public void testGetGeneratorConfig_ForIpsObject() {
        when(type.getIpsObject()).thenReturn(type);

        GeneratorConfig generatorConfig2 = mock(GeneratorConfig.class);

        GeneratorModelContext modelContext2 = mock(GeneratorModelContext.class);
        when(modelContext2.getGeneratorConfig(type)).thenReturn(generatorConfig2);

        JavaBuilderSet builderSet = mock(JavaBuilderSet.class);
        when(builderSet.getGeneratorModelContext()).thenReturn(modelContext2);

        IIpsProject ipsProject = mock(IIpsProject.class);
        when(type.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);

        assertThat(xClass.getGeneratorConfig(), is(generatorConfig2));
    }

    @Test
    public void testAddImport() {
        xClass.addImport("java.util.Map");
        xClass.addImport("package.subpackage.ClassName");
        verify(modelContext).addImport("java.util.Map");
        verify(modelContext).addImport("package.subpackage.ClassName");
    }

    @Test
    public void testAddImportByClass() {
        xClass.addImport(java.util.Map.class);
        verify(modelContext).addImport("java.util.Map");
    }

    @Test
    public void testAddImportByInnerClass() {
        xClass.addImport(org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNodeTest.Inner.class);
        verify(modelContext)
                .addImport("org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNodeTest.Inner");
    }

    @Test
    public void testGetLocalizedComent() {
        xClass = spy(xClass);
        doReturn("GenericComment").when(xClass).getLocalizedText("key");

        String localizedComment = xClass.localizedComment("key");
        assertEquals("// GenericComment", localizedComment);
    }

    @Test
    public void testHasSinceVersion_true() throws Exception {
        IVersionControlledElement versionControlledElement = mock(IVersionControlledElement.class);
        when(versionControlledElement.isValidSinceVersion()).thenReturn(true);
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(versionControlledElement, null, null);

        boolean hasSinceVersion = modelNode.hasSinceVersion();

        assertTrue(hasSinceVersion);
    }

    @Test
    public void testHasSinceVersion_nullVersion() throws Exception {
        IVersionControlledElement versionControlledElement = mock(IVersionControlledElement.class);
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(versionControlledElement, null, null);

        boolean hasSinceVersion = modelNode.hasSinceVersion();

        assertFalse(hasSinceVersion);
    }

    @Test
    public void testHasSinceVersion_blankVersion() throws Exception {
        IVersionControlledElement versionControlledElement = mock(IVersionControlledElement.class);
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(versionControlledElement, null, null);
        boolean hasSinceVersion = modelNode.hasSinceVersion();

        assertFalse(hasSinceVersion);
    }

    @Test
    public void testHasSinceVersion_noVersionControlledElement() throws Exception {
        IpsObjectPartContainer part = mock(IpsObjectPartContainer.class);
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(part, null, null);

        boolean hasSinceVersion = modelNode.hasSinceVersion();

        assertFalse(hasSinceVersion);
    }

    @Test
    public void testGetSinceVersion() throws Exception {
        IVersionControlledElement versionControlledElement = mock(IVersionControlledElement.class);
        doReturn(new DefaultVersion("1.2.3")).when(versionControlledElement).getSinceVersion();
        when(versionControlledElement.isValidSinceVersion()).thenReturn(true);
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(versionControlledElement, null, null);

        String sinceVersion = modelNode.getSinceVersion();

        assertEquals("1.2.3", sinceVersion);
    }

    @Test
    public void testGetSinceVersion_noVersion() throws Exception {
        IVersionControlledElement versionControlledElement = mock(IVersionControlledElement.class);
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(versionControlledElement, null, null);

        String sinceVersion = modelNode.getSinceVersion();

        assertNull(sinceVersion);
    }

    @Test
    public void testGetSinceVersion_noVersionControlledElement() throws Exception {
        IpsObjectPartContainer part = mock(IpsObjectPartContainer.class);
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(part, null, null);

        String sinceVersion = modelNode.getSinceVersion();

        assertNull(sinceVersion);
    }

    public static class Inner {
        // plain empty class without annotations
    }

}
