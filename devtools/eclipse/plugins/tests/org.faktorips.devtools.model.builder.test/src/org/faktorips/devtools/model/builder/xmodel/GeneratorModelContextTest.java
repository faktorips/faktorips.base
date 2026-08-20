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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.faktorips.devtools.model.builder.IJavaPackageStructure;
import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGenerator;
import org.faktorips.devtools.model.internal.ipsproject.IpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class GeneratorModelContextTest {


    @Mock
    private Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorMap;

    @Mock
    private IIpsArtefactBuilderSetConfig config;

    @Mock
    private IJavaPackageStructure javaPackageStructure;

    @Mock
    private IIpsProject ipsProject;

    private MockitoSession mockito;

    private GeneratorModelContext generatorModelContext;

    @BeforeEach
    public void createGeneratorModelContext() throws Exception {
        mockito = createMocks(this);
        lenient().when(ipsProject.getIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[0]);
        generatorModelContext = new GeneratorModelContext(config, javaPackageStructure, annotationGeneratorMap,
                ipsProject);
        generatorModelContext.resetContext("any", Collections.emptySet());
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testGetAnnotationGenerator() throws Exception {
        List<IAnnotationGenerator> annotationGenerators = generatorModelContext
                .getAnnotationGenerator(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS);
        assertTrue(annotationGenerators.isEmpty());

        List<IAnnotationGenerator> policyCmptImplClassAnnotationGens = new ArrayList<>();
        policyCmptImplClassAnnotationGens.add(mock(IAnnotationGenerator.class));
        when(annotationGeneratorMap.get(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS))
                .thenReturn(policyCmptImplClassAnnotationGens);

        annotationGenerators = generatorModelContext
                .getAnnotationGenerator(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS);
        assertEquals(policyCmptImplClassAnnotationGens, annotationGenerators);
    }

    @Test
    public void testGetGeneratorConfig_IpsObject_Null() {
        assertThrows(NullPointerException.class, () -> {
            generatorModelContext.getGeneratorConfig((IIpsObject)null);
        });
    }

    @Test
    public void testGetGeneratorConfig_IpsObject_NoPackageFragment() {
        assertThrows(NullPointerException.class, () -> {
            generatorModelContext.getGeneratorConfig(mock(IIpsObject.class));
        });
    }

    @Test
    public void testGetGeneratorConfig_IpsObject_NoPackageFragmentRoot() {
        GeneratorConfig baseGeneratorConfig = generatorModelContext.getBaseGeneratorConfig();
        IIpsObject ipsObject = mock(IIpsObject.class);
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        when(ipsObject.getIpsPackageFragment()).thenReturn(packageFragment);
        assertThat(generatorModelContext.getGeneratorConfig(ipsObject), is(baseGeneratorConfig));
    }

    @Test
    public void testGetGeneratorConfig_IpsObject_PackageFragmentRootNotInProject() {
        GeneratorConfig baseGeneratorConfig = generatorModelContext.getBaseGeneratorConfig();
        IIpsObject ipsObject = mock(IIpsObject.class);
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        IIpsPackageFragmentRoot packageFragmentRoot = mock(IIpsPackageFragmentRoot.class);
        when(packageFragment.getRoot()).thenReturn(packageFragmentRoot);
        when(ipsObject.getIpsPackageFragment()).thenReturn(packageFragment);
        assertThat(generatorModelContext.getGeneratorConfig(ipsObject), is(baseGeneratorConfig));
    }

    @Test
    public void testGetGeneratorConfig_IpsObject() {
        IIpsPackageFragmentRoot packageFragmentRoot = mock(IIpsPackageFragmentRoot.class);
        when(ipsProject.getIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[] { packageFragmentRoot });
        generatorModelContext = new GeneratorModelContext(config, javaPackageStructure, annotationGeneratorMap,
                ipsProject);
        GeneratorConfig baseGeneratorConfig = generatorModelContext.getBaseGeneratorConfig();
        IIpsObject ipsObject = mock(IIpsObject.class);
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        when(packageFragment.getRoot()).thenReturn(packageFragmentRoot);
        when(ipsObject.getIpsPackageFragment()).thenReturn(packageFragment);
        assertThat(generatorModelContext.getGeneratorConfig(ipsObject), is(baseGeneratorConfig));
    }

    @Test
    public void testGetGeneratorConfig_IpsSrcFile_Null() {
        assertThrows(NullPointerException.class, () -> {
            generatorModelContext.getGeneratorConfig((IIpsSrcFile)null);
        });
    }

    @Test
    public void testGetGeneratorConfig_IpsSrcFile_NoPackageFragment() {
        assertThrows(NullPointerException.class, () -> {
            generatorModelContext.getGeneratorConfig(mock(IIpsSrcFile.class));
        });
    }

    @Test
    public void testGetGeneratorConfig_IpsSrcFile_NoPackageFragmentRoot() {
        GeneratorConfig baseGeneratorConfig = generatorModelContext.getBaseGeneratorConfig();
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(packageFragment);
        assertThat(generatorModelContext.getGeneratorConfig(ipsSrcFile), is(baseGeneratorConfig));
    }

    @Test
    public void testGetGeneratorConfig_IpsSrcFile_PackageFragmentRootNotInProject() {
        GeneratorConfig baseGeneratorConfig = generatorModelContext.getBaseGeneratorConfig();
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        IIpsPackageFragmentRoot packageFragmentRoot = mock(IIpsPackageFragmentRoot.class);
        when(packageFragment.getRoot()).thenReturn(packageFragmentRoot);
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(packageFragment);
        assertThat(generatorModelContext.getGeneratorConfig(ipsSrcFile), is(baseGeneratorConfig));
    }

    @Test
    public void testGetGeneratorConfig_IpsSrcFile() {
        IIpsPackageFragmentRoot packageFragmentRoot = mock(IIpsPackageFragmentRoot.class);
        when(ipsProject.getIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[] { packageFragmentRoot });
        generatorModelContext = new GeneratorModelContext(config, javaPackageStructure, annotationGeneratorMap,
                ipsProject);
        GeneratorConfig baseGeneratorConfig = generatorModelContext.getBaseGeneratorConfig();
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        when(packageFragment.getRoot()).thenReturn(packageFragmentRoot);
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(packageFragment);
        assertThat(generatorModelContext.getGeneratorConfig(ipsSrcFile), is(baseGeneratorConfig));
    }

    @Test
    public void testGetValidationMessageBundleBaseName() {
        IIpsSrcFolderEntry entry = mock(IpsSrcFolderEntry.class);
        when(entry.getValidationMessagesBundle()).thenReturn("name-for-message-bundle");
        when(entry.getUniqueQualifier()).thenReturn("UniqueQualifier");
        when(entry.getBasePackageNameForDerivedJavaClasses()).thenReturn("org.faktorips.test");
        when(entry.getUniqueBasePackageNameForDerivedArtifacts()).thenCallRealMethod();

        assertThat(generatorModelContext.getValidationMessageBundleBaseName(entry),
                is("org.faktorips.test.UniqueQualifier.name-for-message-bundle"));
    }

    @Test
    public void testGetValidationMessageBundleBaseName_emptyQualifier() {
        IIpsSrcFolderEntry entry = mock(IpsSrcFolderEntry.class);
        when(entry.getValidationMessagesBundle()).thenReturn("name-for-message-bundle");
        when(entry.getBasePackageNameForDerivedJavaClasses()).thenReturn("org.faktorips.test");
        when(entry.getUniqueBasePackageNameForDerivedArtifacts()).thenCallRealMethod();

        assertThat(generatorModelContext.getValidationMessageBundleBaseName(entry),
                is("org.faktorips.test.name-for-message-bundle"));
    }
}
