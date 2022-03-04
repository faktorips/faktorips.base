/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.model.internal.ipsproject.properties.SupportedLanguage;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.AbstractValidationMessagesBuilderTest;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractLocalizedPropertiesBuilder;
import org.junit.Test;

public class ValidationRuleMessagesPropertiesBuilderTest extends AbstractValidationMessagesBuilderTest {

    private static final String TEST_VALIDATION_MESSAGES = "test.validation-messages";

    @Test
    public void testIsBuilderFor() throws Exception {
        StandardBuilderSet builderSet = mockBuilderSet();
        ValidationRuleMessagesPropertiesBuilder builder = new ValidationRuleMessagesPropertiesBuilder(builderSet);
        IIpsSrcFile ipsSrcFile = mockIpsSrcFile();

        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);
        assertTrue(builder.isBuilderFor(ipsSrcFile));

        IpsObjectType mockIpsObjectType = mock(IpsObjectType.class);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(mockIpsObjectType);
        assertFalse(builder.isBuilderFor(ipsSrcFile));
    }

    @Test
    public void testBuild() throws Exception {
        StandardBuilderSet builderSet = mockBuilderSet();

        // we use a spy object to insert the generatorMock
        ValidationRuleMessagesGenerator generatorMock = mock(ValidationRuleMessagesGenerator.class);
        ValidationRuleMessagesPropertiesBuilder builderSpy = spy(
                new ValidationRuleMessagesPropertiesBuilder(builderSet));
        doReturn(generatorMock).when(builderSpy).getMessagesGenerator(any(IIpsPackageFragmentRoot.class),
                any(ISupportedLanguage.class));

        IPolicyCmptType pcType = mock(IPolicyCmptType.class);
        IIpsSrcFile ipsSrcFile = mockIpsSrcFile();
        IIpsPackageFragment pack = mockPackageFragment();

        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        when(ipsSrcFile.getIpsObject()).thenReturn(pcType);

        builderSpy.build(ipsSrcFile);

        verify(generatorMock).generate(pcType);
    }

    @Test
    public void shouldCreateDifferentMessageFilesForDifferentRoots() throws Exception {
        StandardBuilderSet builderSet = mockBuilderSet();
        ValidationRuleMessagesPropertiesBuilder builder = new ValidationRuleMessagesPropertiesBuilder(builderSet);

        IIpsPackageFragment pack = mockPackageFragment();

        IIpsSrcFile ipsSrcFile = mockIpsSrcFile();
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        ISupportedLanguage supportedLanguage = new SupportedLanguage(Locale.GERMAN);
        ValidationRuleMessagesGenerator messageGenerator = builder.getMessagesGenerator(ipsSrcFile, supportedLanguage);
        assertSame(messageGenerator, builder.getMessagesGenerator(ipsSrcFile, supportedLanguage));

        IIpsSrcFile otherIpsSrcFile = mockIpsSrcFile();
        when(otherIpsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        assertSame(messageGenerator, builder.getMessagesGenerator(otherIpsSrcFile, supportedLanguage));

        IIpsPackageFragment pack2 = mockPackageFragment();

        IIpsSrcFile ipsSrcFile2 = mockIpsSrcFile();
        when(ipsSrcFile2.getIpsPackageFragment()).thenReturn(pack2);

        assertNotSame(messageGenerator, builder.getMessagesGenerator(ipsSrcFile2, supportedLanguage));

        // overwrite the root of pack2
        doReturn(pack.getRoot()).when(pack2).getRoot();
        assertSame(messageGenerator, builder.getMessagesGenerator(ipsSrcFile2, supportedLanguage));
    }

    @Test
    public void testBuildIgnoresOtherTypes() throws Exception {
        StandardBuilderSet builderSet = mockBuilderSet();

        // we use a spy object to insert the generatorMock
        ValidationRuleMessagesGenerator generatorMock = mock(ValidationRuleMessagesGenerator.class);
        ValidationRuleMessagesPropertiesBuilder builderSpy = spy(
                new ValidationRuleMessagesPropertiesBuilder(builderSet));
        doReturn(generatorMock).when(builderSpy).getMessagesGenerator(any(IIpsPackageFragmentRoot.class),
                any(ISupportedLanguage.class));

        IIpsPackageFragment pack = mockPackageFragment();

        IIpsSrcFile ipsSrcFile = mockIpsSrcFile();
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        // should not throw any exception
        builderSpy.build(ipsSrcFile);
        verifyZeroInteractions(generatorMock);

        IIpsObject anyIpsObject = mock(IIpsObject.class);
        when(ipsSrcFile.getIpsObject()).thenReturn(anyIpsObject);
        // should not throw any exception
        builderSpy.build(ipsSrcFile);
        verifyZeroInteractions(generatorMock);
    }

    @Test
    public void testDelete() throws Exception {
        StandardBuilderSet builderSet = mockBuilderSet();
        IIpsPackageFragment pack = mockPackageFragment();

        // we use a spy object to insert the generatorMock
        ValidationRuleMessagesGenerator generatorMock = mock(ValidationRuleMessagesGenerator.class);
        ValidationRuleMessagesPropertiesBuilder builderSpy = spy(
                new ValidationRuleMessagesPropertiesBuilder(builderSet));
        doReturn(generatorMock).when(builderSpy).getMessagesGenerator(any(IIpsPackageFragmentRoot.class),
                any(ISupportedLanguage.class));

        IIpsSrcFile ipsSrcFile = mockIpsSrcFile();
        IPolicyCmptType pcType = mock(IPolicyCmptType.class);
        when(ipsSrcFile.getIpsObject()).thenReturn(pcType);
        String pcTypeName = "PcTypeTestName";
        QualifiedNameType qualifiedNameType = new QualifiedNameType(pcTypeName, IpsObjectType.POLICY_CMPT_TYPE);
        when(ipsSrcFile.getQualifiedNameType()).thenReturn(qualifiedNameType);
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        builderSpy.delete(ipsSrcFile);

        verify(generatorMock).deleteAllMessagesFor(qualifiedNameType);

    }

    @Test
    public void shouldBuildDerivedArtifacts() throws Exception {
        StandardBuilderSet builderSet = mockBuilderSet();
        ValidationRuleMessagesPropertiesBuilder builder = new ValidationRuleMessagesPropertiesBuilder(builderSet);
        assertTrue(builder.buildsDerivedArtefacts());
    }

    @Test
    public void testGetPropertyFile() throws Exception {
        StandardBuilderSet builderSet = mockBuilderSet();

        ValidationRuleMessagesPropertiesBuilder validationMessagesBuilder = new ValidationRuleMessagesPropertiesBuilder(
                builderSet);

        AFile file = mock(AFile.class);

        Path path = Path.of(ROOT_FOLDER, TEST_VALIDATION_MESSAGES.split("\\."))
                .resolveSibling(
                        TEST_VALIDATION_MESSAGES.substring(TEST_VALIDATION_MESSAGES.lastIndexOf('.') + 1) + "_en" + "."
                                + AbstractLocalizedPropertiesBuilder.MESSAGES_EXTENSION);
        when(builderSet.getValidationMessageBundleBaseName(any(IIpsSrcFolderEntry.class))).thenReturn(
                ROOT_FOLDER + "." + TEST_VALIDATION_MESSAGES);

        AFolder derivedFolder = mock(AFolder.class);
        when(derivedFolder.getFile(path)).thenReturn(file);

        IIpsSrcFolderEntry ipsSrcFolderEntry = mock(IIpsSrcFolderEntry.class);
        when(ipsSrcFolderEntry.getOutputFolderForDerivedJavaFiles()).thenReturn(derivedFolder);
        when(ipsSrcFolderEntry.getBasePackageNameForDerivedJavaClasses()).thenReturn(TEST_PACK);
        when(ipsSrcFolderEntry.getValidationMessagesBundle()).thenReturn(TEST_VALIDATION_MESSAGES);

        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        when(root.getIpsObjectPathEntry()).thenReturn(ipsSrcFolderEntry);

        ISupportedLanguage supportedLanguage = new SupportedLanguage(Locale.ENGLISH, true);
        AFile propertyFile = validationMessagesBuilder.getPropertyFile(root, supportedLanguage);
        assertEquals(file, propertyFile);
    }

    @Test
    public void testGetPropertyFileForLocale() throws Exception {
        StandardBuilderSet builderSet = mockBuilderSet();
        ValidationRuleMessagesPropertiesBuilder validationMessagesBuilder = new ValidationRuleMessagesPropertiesBuilder(
                builderSet);

        Locale locale = Locale.GERMAN;

        AFile file = mock(AFile.class);

        Path path = Path.of(ROOT_FOLDER, TEST_VALIDATION_MESSAGES.split("\\."))
                .resolveSibling(
                        TEST_VALIDATION_MESSAGES.substring(TEST_VALIDATION_MESSAGES.lastIndexOf('.') + 1) + "_"
                                + locale.getLanguage() + "."
                                + ValidationRuleMessagesPropertiesBuilder.MESSAGES_EXTENSION);
        when(builderSet.getValidationMessageBundleBaseName(any(IIpsSrcFolderEntry.class))).thenReturn(
                ROOT_FOLDER + "." + TEST_VALIDATION_MESSAGES);

        AFolder derivedFolder = mock(AFolder.class);
        when(derivedFolder.getFile(path)).thenReturn(file);

        IIpsSrcFolderEntry ipsSrcFolderEntry = mock(IIpsSrcFolderEntry.class);
        when(ipsSrcFolderEntry.getOutputFolderForDerivedJavaFiles()).thenReturn(derivedFolder);
        when(ipsSrcFolderEntry.getBasePackageNameForDerivedJavaClasses()).thenReturn(TEST_PACK);
        when(ipsSrcFolderEntry.getValidationMessagesBundle()).thenReturn(TEST_VALIDATION_MESSAGES);

        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        when(root.getIpsObjectPathEntry()).thenReturn(ipsSrcFolderEntry);

        ISupportedLanguage supportedLanguage = new SupportedLanguage(locale, false);
        AFile propertyFile = validationMessagesBuilder.getPropertyFile(root, supportedLanguage);
        assertEquals(file, propertyFile);
    }

    @Test
    public void testAfterBuildProcess() throws Exception {
        StandardBuilderSet builderSet = mockBuilderSet();
        IIpsPackageFragment pack = mockPackageFragment();
        IIpsPackageFragmentRoot root = pack.getRoot();

        // we use a spy object to insert the generatorMock
        ValidationRuleMessagesGenerator generatorMock = mock(ValidationRuleMessagesGenerator.class);
        ValidationRuleMessagesPropertiesBuilder builderSpy = spy(
                new ValidationRuleMessagesPropertiesBuilder(builderSet));
        doReturn(generatorMock).when(builderSpy).getMessagesGenerator(any(IIpsPackageFragmentRoot.class),
                any(ISupportedLanguage.class));

        IIpsProject ipsProject = mockIpsProject();
        when(ipsProject.getSourceIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[] {});

        builderSpy.afterBuildProcess(ipsProject, null);
        verifyZeroInteractions(generatorMock);

        when(ipsProject.getSourceIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[] { root });
        builderSpy.afterBuildProcess(ipsProject, null);

        verify(generatorMock).saveIfModified();
    }

    @Test
    public void shouldCreateFolderIfNotExists() throws Exception {
        StandardBuilderSet builderSet = mockBuilderSet();
        IIpsPackageFragment fragment = mockPackageFragment();
        IIpsPackageFragmentRoot root = fragment.getRoot();

        IIpsProject ipsProject = mockIpsProject();
        when(ipsProject.getSourceIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[] { root });

        ValidationRuleMessagesPropertiesBuilder validationMessagesBuilder = new ValidationRuleMessagesPropertiesBuilder(
                builderSet);

        AFolder folder = mock(AFolder.class);
        ISupportedLanguage supportedLanguage = new SupportedLanguage(Locale.GERMAN);

        AFile propertyFile = validationMessagesBuilder.getPropertyFile(root, supportedLanguage);
        when(propertyFile.getParent()).thenReturn(folder);

        validationMessagesBuilder.beforeBuildProcess(ipsProject, ABuildKind.INCREMENTAL);
        verify(folder).create(any(IProgressMonitor.class));
    }

    protected IIpsSrcFile mockIpsSrcFile() {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IIpsProject ipsProject = mockIpsProject();
        when(ipsSrcFile.getIpsProject()).thenReturn(ipsProject);
        return ipsSrcFile;
    }

    protected IIpsProject mockIpsProject() {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IIpsProjectProperties properties = mock(IIpsProjectProperties.class);
        when(ipsProject.getReadOnlyProperties()).thenReturn(properties);
        ISupportedLanguage supportedLanguage = new SupportedLanguage(Locale.GERMAN);
        HashSet<ISupportedLanguage> supportedLanguages = new HashSet<>();
        supportedLanguages.add(supportedLanguage);
        when(properties.getSupportedLanguages()).thenReturn(supportedLanguages);
        return ipsProject;
    }

}
