/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.ManifestElement;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsBundleManifestTest {

    private static final String MANIFEST = """
            Manifest-Version: 1.0
            Fips-BasePackage: myBasePackage
            Fips-SourcecodeOutput: mySrcOut
            Fips-ResourceOutput: myResourceOut
            Fips-UniqueQualifier: myQualifier
            Fips-ObjectDir: myObjectdir;toc="myToc";validation-messages="myMessages"
            Fips-GeneratorConfig: org.faktorips.devtools.stdbuilder.StandardBuilderS
             et;changesInTimeNamingConvention="FIPS";generatePublishedInterfaces="tr
             ue"
            Fips-RuntimeIdPrefix: myPrefix.

            Name: myObjectdir
            Fips-BasePackage: myBasePackage2
            Fips-UniqueQualifier: myQualifier2
            Fips-SourcecodeOutput: mySrcOut2
            Fips-ResourceOutput: myResourceOut2
            """;

    private static final String STANDARD_BUILDER_SET_ID = "org.faktorips.devtools.stdbuilder.StandardBuilderSet";

    private static final String TRUE = Boolean.TRUE.toString();

    private static final String ATTRIBUTE_GENERATE_PUBLISHED_INTERFACES = "generatePublishedInterfaces";

    private static final String MY_BASE_PACKAGE = "myBasePackage";

    private static final String MY_BASE_PACKAGE2 = "myBasePackage2";

    private static final String MY_UNIQUE_QUALIFIER = "myQualifier";

    private static final String MY_UNIQUE_QUALIFIER2 = "myQualifier2";

    private static final String MY_OBJECT_DIR = "myObjectdir";

    private static final String INVALID_OBJECT_DIR = "invalidObjectdir";

    private static final String MY_SRC_OUT = "mySrcOut";

    private static final String MY_SRC_OUT2 = "mySrcOut2";

    private static final String MY_RESOURCE_OUT = "myResourceOut";

    private static final String MY_RESOURCE_OUT2 = "myResourceOut2";

    private static final String MY_TOC = "myToc";

    private static final String MY_MESSAGES = "myMessages";

    private static final String MY_PREFIX = "myPrefix.";

    private Manifest manifest;

    private IpsBundleManifest ipsBundleManifest;

    @Mock
    private IpsProject ipsProject;

    @Before
    public void createIpsBundleManifest() throws IOException {
        mockManifest();
        ipsBundleManifest = new IpsBundleManifest(manifest);
        AProject project = mock(AProject.class);
        when(project.getDefaultLineSeparator()).thenReturn("\n");
        when(ipsProject.getProject()).thenReturn(project);
    }

    public void mockManifest() throws IOException {
        manifest = new Manifest(new ByteArrayInputStream(MANIFEST.getBytes()));
    }

    @Test
    public void testGetBasePackage() {
        String basePackage = ipsBundleManifest.getBasePackage();

        assertEquals(MY_BASE_PACKAGE, basePackage);
    }

    @Test
    public void testGetBasePackage_trim() {
        String basePackage = ipsBundleManifest.getBasePackage();

        assertEquals(MY_BASE_PACKAGE, basePackage);
    }

    @Test
    public void testGetBasePackage_forObjectDir() {
        String basePackage = ipsBundleManifest.getBasePackage(MY_OBJECT_DIR);

        assertEquals(MY_BASE_PACKAGE2, basePackage);
    }

    @Test
    public void testGetBasePackage_forObjectDirTrim() throws IOException {
        manifest = new Manifest(
                new ByteArrayInputStream(MANIFEST.replace(MY_BASE_PACKAGE2, " " + MY_BASE_PACKAGE2 + " ").getBytes()));
        ipsBundleManifest = new IpsBundleManifest(manifest);

        String basePackage = ipsBundleManifest.getBasePackage(MY_OBJECT_DIR);

        assertEquals(MY_BASE_PACKAGE2, basePackage);
    }

    @Test
    public void testGetBasePackage_forInvalidObjectDir() {
        String basePackage = ipsBundleManifest.getBasePackage(INVALID_OBJECT_DIR);

        assertEquals(MY_BASE_PACKAGE, basePackage);
    }

    @Test
    public void testGetUniqueQualifier() {
        String uniqueQualifier = ipsBundleManifest.getUniqueQualifier();

        assertEquals(MY_UNIQUE_QUALIFIER, uniqueQualifier);
    }

    @Test
    public void testGetUniqueQualifier_trim() {

        String uniqueQualifier = ipsBundleManifest.getUniqueQualifier();

        assertEquals(MY_UNIQUE_QUALIFIER, uniqueQualifier);
    }

    @Test
    public void testGetUniqueQualifier_forObjectDir() {
        String uniqueQualifier = ipsBundleManifest.getUniqueQualifier(MY_OBJECT_DIR);

        assertEquals(MY_UNIQUE_QUALIFIER2, uniqueQualifier);
    }

    @Test
    public void testGetUniqueQualifier_forObjectDirTrim() {

        String uniqueQualifier = ipsBundleManifest.getUniqueQualifier(MY_OBJECT_DIR);

        assertEquals(MY_UNIQUE_QUALIFIER2, uniqueQualifier);
    }

    @Test
    public void testGetUniqueQualifier_forInvalidObjectDir() {
        String uniqueQualifier = ipsBundleManifest.getUniqueQualifier(INVALID_OBJECT_DIR);

        assertEquals(MY_UNIQUE_QUALIFIER, uniqueQualifier);
    }

    @Test
    public void testGetSourcecodeOutput() {
        String srcOutput = ipsBundleManifest.getSourcecodeOutput();

        assertEquals(MY_SRC_OUT, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_forObjectDir() {
        String srcOutput = ipsBundleManifest.getSourcecodeOutput(MY_OBJECT_DIR);

        assertEquals(MY_SRC_OUT2, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_trim() throws IOException {
        manifest = new Manifest(
                new ByteArrayInputStream(MANIFEST.replace(MY_SRC_OUT, MY_SRC_OUT + " ").getBytes()));
        ipsBundleManifest = new IpsBundleManifest(manifest);

        String srcOutput = ipsBundleManifest.getSourcecodeOutput();

        assertEquals(MY_SRC_OUT, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_objectDirAndTrim() throws IOException {
        manifest = new Manifest(
                new ByteArrayInputStream(MANIFEST.replace(MY_SRC_OUT2, MY_SRC_OUT2 + " ").getBytes()));
        ipsBundleManifest = new IpsBundleManifest(manifest);

        String srcOutput = ipsBundleManifest.getSourcecodeOutput(MY_OBJECT_DIR);

        assertEquals(MY_SRC_OUT2, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_forInvalidObjectDir() {
        String srcOutput = ipsBundleManifest.getSourcecodeOutput(INVALID_OBJECT_DIR);

        assertEquals(MY_SRC_OUT, srcOutput);
    }

    @Test
    public void testGetResourceOutput() {
        String srcOutput = ipsBundleManifest.getResourceOutput();

        assertEquals(MY_RESOURCE_OUT, srcOutput);
    }

    @Test
    public void testGetResourceOutput_trim() throws IOException {
        manifest = new Manifest(
                new ByteArrayInputStream(MANIFEST.replace(MY_RESOURCE_OUT, " " + MY_RESOURCE_OUT + " ").getBytes()));
        ipsBundleManifest = new IpsBundleManifest(manifest);

        String srcOutput = ipsBundleManifest.getResourceOutput();

        assertEquals(MY_RESOURCE_OUT, srcOutput);
    }

    @Test
    public void testGetResourceOutput_forObjectDir() {
        String srcOutput = ipsBundleManifest.getResourceOutput(MY_OBJECT_DIR);

        assertEquals(MY_RESOURCE_OUT2, srcOutput);
    }

    @Test
    public void testGetResourceOutput_forObjectDirTrim() throws IOException {
        manifest = new Manifest(
                new ByteArrayInputStream(MANIFEST.replace(MY_RESOURCE_OUT2, " " + MY_RESOURCE_OUT2 + " ").getBytes()));
        ipsBundleManifest = new IpsBundleManifest(manifest);

        String srcOutput = ipsBundleManifest.getResourceOutput(MY_OBJECT_DIR);

        assertEquals(MY_RESOURCE_OUT2, srcOutput);
    }

    @Test
    public void testGetResourceOutput_forInvalidObjectDir() {
        String srcOutput = ipsBundleManifest.getResourceOutput(INVALID_OBJECT_DIR);

        assertEquals(MY_RESOURCE_OUT, srcOutput);
    }

    @Test
    public void testGetObjectDirs() {
        List<Path> objectDir = ipsBundleManifest.getObjectDirs();

        assertEquals(1, objectDir.size());
        assertEquals(Path.of(MY_OBJECT_DIR), objectDir.get(0));
    }

    @Test
    public void testGetObjectDirElements_empty() {
        ipsBundleManifest = new IpsBundleManifest(mock(Manifest.class));
        ManifestElement[] objectDir = ipsBundleManifest.getObjectDirElements();

        assertEquals(0, objectDir.length);
    }

    @Test
    public void testGetObjectDirElements_noValue() throws IOException {
        manifest = new Manifest(new ByteArrayInputStream("""
                Manifest-Version: 1.0
                """.getBytes()));
        ipsBundleManifest = new IpsBundleManifest(manifest);

        ManifestElement[] objectDir = ipsBundleManifest.getObjectDirElements();

        assertEquals(0, objectDir.length);
    }

    @Test
    public void testGetObjectDirElements() {
        ManifestElement[] objectDir = ipsBundleManifest.getObjectDirElements();

        assertEquals(1, objectDir.length);
        assertEquals(1, objectDir[0].getValueComponents().length);
        assertEquals(MY_OBJECT_DIR, objectDir[0].getValue());
    }

    @Test
    public void testHasObjectDirs() {
        List<Path> objectDir = ipsBundleManifest.getObjectDirs();

        assertEquals(1, objectDir.size());
        assertEquals(Path.of(MY_OBJECT_DIR), objectDir.get(0));
        assertEquals(true, ipsBundleManifest.hasObjectDirs());
    }

    @Test
    public void testHasObjectDirs_NoObjectDirs() {
        ipsBundleManifest = new IpsBundleManifest(mock(Manifest.class));
        List<Path> objectDir = ipsBundleManifest.getObjectDirs();

        assertEquals(0, objectDir.size());
        assertEquals(false, ipsBundleManifest.hasObjectDirs());
    }

    @Test
    public void testGetTocPath() {
        ManifestElement objectDirElement = ipsBundleManifest.getObjectDirElements()[0];

        String toc = ipsBundleManifest.getTocPath(objectDirElement);

        assertEquals(MY_TOC, toc);
    }

    @Test
    public void testGetValidationMessagesBundle() {
        ManifestElement objectDirElement = ipsBundleManifest.getObjectDirElements()[0];
        String messages = ipsBundleManifest.getValidationMessagesBundle(objectDirElement);
        assertEquals(MY_MESSAGES, messages);
    }

    @Test
    public void testGetGeneratorConfig_ConfiguredBuilderSet() {
        Map<String, String> generatorConfig = ipsBundleManifest.getGeneratorConfig(STANDARD_BUILDER_SET_ID);

        assertThat(generatorConfig, is(notNullValue()));
        assertThat(generatorConfig.get(IpsProjectProperties.ATTRIBUTE_CHANGES_IN_TIME_NAMING_CONVENTION),
                is(IChangesOverTimeNamingConvention.FAKTOR_IPS));
        assertThat(generatorConfig.get(ATTRIBUTE_GENERATE_PUBLISHED_INTERFACES), is(TRUE));
    }

    @Test
    public void testGetGeneratorConfig_ConfiguredBuilderSet_caseInsensitive() {
        Map<String, String> generatorConfig = ipsBundleManifest
                .getGeneratorConfig(STANDARD_BUILDER_SET_ID.toLowerCase());

        assertThat(generatorConfig, is(notNullValue()));
        assertThat(generatorConfig.get(IpsProjectProperties.ATTRIBUTE_CHANGES_IN_TIME_NAMING_CONVENTION),
                is(IChangesOverTimeNamingConvention.FAKTOR_IPS));
        assertThat(generatorConfig.get(ATTRIBUTE_GENERATE_PUBLISHED_INTERFACES), is(TRUE));
    }

    @Test
    public void testGetGeneratorConfig_UnknownBuilderSet() {
        Map<String, String> generatorConfig = ipsBundleManifest.getGeneratorConfig("FooBar");

        assertThat(generatorConfig, is(notNullValue()));
        assertThat(generatorConfig.isEmpty(), is(true));
    }

    @Test
    public void testGetRuntimeIdPrefix() {
        String runtimeIdPrefix = ipsBundleManifest.getRuntimeIdPrefix();

        assertEquals(MY_PREFIX, runtimeIdPrefix);
    }

    @Test
    public void testWriteBuilderSettings() throws IOException {
        IIpsArtefactBuilderSet builderSet = mock(IIpsArtefactBuilderSet.class);
        when(builderSet.getId()).thenReturn(STANDARD_BUILDER_SET_ID);
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        when(ipsProject.getRuntimeIdPrefix()).thenReturn("pre.");
        IIpsArtefactBuilderSetConfig builderSetConfig = mock(IIpsArtefactBuilderSetConfig.class);
        when(builderSet.getConfig()).thenReturn(builderSetConfig);
        when(builderSetConfig.getPropertyNames())
                .thenReturn(new String[] { "stringAttr", "nullAttr", "booleanAttr", "intAttr" });
        when(builderSetConfig.getPropertyValue("stringAttr")).thenReturn("Foo \"quoted\" Bar");
        when(builderSetConfig.getPropertyValue("nullAttr")).thenReturn(null);
        when(builderSetConfig.getPropertyValue("booleanAttr")).thenReturn(Boolean.TRUE);
        when(builderSetConfig.getPropertyValue("intAttr")).thenReturn(42);
        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        AFile eclipseManifestFile = mockEclipseManifestFile(manifestFile);

        ipsBundleManifest.writeBuilderSettings(ipsProject, eclipseManifestFile);

        manifest = new Manifest(new FileInputStream(manifestFile));
        Attributes mainAttributes = manifest.getMainAttributes();

        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_GENERATOR_CONFIG), is(
                "org.faktorips.devtools.stdbuilder.StandardBuilderSet;booleanAttr=\"true\";intAttr=\"42\";stringAttr=\"Foo \\\"quoted\\\" Bar\""));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_RUNTIME_ID_PREFIX), is("pre."));

        String manifestString = Files.readString(manifestFile.toPath());
        assertThat(manifestString, containsString(
                "et;booleanAttr=\"true\";intAttr=\"42\";stringAttr=\"Foo \\\"quoted\\\" Bar\""));
    }

    private AFile mockEclipseManifestFile(File manifestFile) throws FileNotFoundException {
        AFile eclipseManifestFile = mock(AFile.class);
        Path eclipseManifestPath = mock(Path.class);
        when(eclipseManifestFile.getLocation()).thenReturn(eclipseManifestPath);
        when(eclipseManifestPath.toFile()).thenReturn(manifestFile);
        when(eclipseManifestFile.getContents()).thenReturn(new FileInputStream(manifestFile));

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            try {
                Files.copy((InputStream)args[0], manifestFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                fail();
            }
            return null;
        })
                .when(eclipseManifestFile).setContents(any(InputStream.class), ArgumentMatchers.eq(true),
                        ArgumentMatchers.nullable(IProgressMonitor.class));
        return eclipseManifestFile;
    }

    @Test
    public void testWriteBuilderSettingsWithObjectPath() throws IOException {
        IIpsArtefactBuilderSet builderSet = mock(IIpsArtefactBuilderSet.class);
        when(builderSet.getId()).thenReturn(STANDARD_BUILDER_SET_ID);
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        when(ipsProject.getRuntimeIdPrefix()).thenReturn("pre.");
        IIpsArtefactBuilderSetConfig builderSetConfig = mock(IIpsArtefactBuilderSetConfig.class);
        when(builderSet.getConfig()).thenReturn(builderSetConfig);
        when(builderSetConfig.getPropertyNames()).thenReturn(new String[] { "testAttr" });
        when(builderSetConfig.getPropertyValue("testAttr")).thenReturn("testValue");

        IIpsObjectPath ipsObjectPath = mock(IIpsObjectPath.class);
        when(ipsProject.getIpsObjectPath()).thenReturn(ipsObjectPath);
        when(ipsObjectPath.getBasePackageNameForMergableJavaClasses()).thenReturn("org.test.package");

        AFolder mergableSourcesFolder = mock(AFolder.class);
        AFolder derivedSourcesFolder = mock(AFolder.class);
        when(ipsObjectPath.getOutputFolderForMergableSources()).thenReturn(mergableSourcesFolder);
        when(ipsObjectPath.getOutputFolderForDerivedSources()).thenReturn(derivedSourcesFolder);

        Path mergablePath = mock(Path.class);
        Path derivedPath = mock(Path.class);
        when(mergableSourcesFolder.getProjectRelativePath()).thenReturn(mergablePath);
        when(derivedSourcesFolder.getProjectRelativePath()).thenReturn(derivedPath);
        when(mergablePath.toString()).thenReturn("src");
        when(derivedPath.toString()).thenReturn("resources");

        IIpsSrcFolderEntry sourceFolderEntry = mock(IIpsSrcFolderEntry.class);
        when(ipsObjectPath.getSourceFolderEntries()).thenReturn(new IIpsSrcFolderEntry[] { sourceFolderEntry });

        AFolder sourceFolder = mock(AFolder.class);
        when(sourceFolderEntry.getSourceFolder()).thenReturn(sourceFolder);
        when(sourceFolder.getProjectRelativePath()).thenReturn(Path.of("model"));
        when(sourceFolderEntry.getValidationMessagesBundle()).thenReturn("validation-messages");

        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        AFile eclipseManifestFile = mockEclipseManifestFile(manifestFile);

        ipsBundleManifest.writeBuilderSettings(ipsProject, eclipseManifestFile);

        manifest = new Manifest(new FileInputStream(manifestFile));
        Attributes mainAttributes = manifest.getMainAttributes();

        assertThat(mainAttributes.get(Attributes.Name.MANIFEST_VERSION), is("1.0"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_GENERATOR_CONFIG), is(
                "org.faktorips.devtools.stdbuilder.StandardBuilderSet;testAttr=\"testValue\""));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_RUNTIME_ID_PREFIX), is("pre."));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_BASE_PACKAGE), is("org.test.package"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_SRC_OUT), is("src"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_RESOURCE_OUT), is("resources"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_OBJECT_DIR), is(
                "model;toc=\"faktorips-repository-toc.xml\";validation-messages=\"validation-messages\""));
    }

    @Test
    public void testWriteBuilderSettingsWithOutputDefinedPerSrcFolder() throws IOException {
        IIpsArtefactBuilderSet builderSet = mock(IIpsArtefactBuilderSet.class);
        when(builderSet.getId()).thenReturn(STANDARD_BUILDER_SET_ID);
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        when(ipsProject.getRuntimeIdPrefix()).thenReturn("pre.");
        IIpsArtefactBuilderSetConfig builderSetConfig = mock(IIpsArtefactBuilderSetConfig.class);
        when(builderSet.getConfig()).thenReturn(builderSetConfig);
        when(builderSetConfig.getPropertyNames()).thenReturn(new String[] { "testAttr" });
        when(builderSetConfig.getPropertyValue("testAttr")).thenReturn("testValue");

        IIpsObjectPath ipsObjectPath = mock(IIpsObjectPath.class);
        when(ipsProject.getIpsObjectPath()).thenReturn(ipsObjectPath);
        when(ipsObjectPath.getBasePackageNameForMergableJavaClasses()).thenReturn("org.test.package");

        when(ipsObjectPath.isOutputDefinedPerSrcFolder()).thenReturn(true);
        IIpsSrcFolderEntry sourceFolderEntry1 = mock(IIpsSrcFolderEntry.class);
        IIpsSrcFolderEntry sourceFolderEntry2 = mock(IIpsSrcFolderEntry.class);
        when(ipsObjectPath.getSourceFolderEntries())
                .thenReturn(new IIpsSrcFolderEntry[] { sourceFolderEntry1, sourceFolderEntry2 });

        AFolder sourceFolder1 = mock(AFolder.class);
        when(sourceFolderEntry1.getSourceFolder()).thenReturn(sourceFolder1);
        when(sourceFolder1.getProjectRelativePath()).thenReturn(Path.of("model"));
        when(sourceFolderEntry1.getValidationMessagesBundle()).thenReturn("validation-messages");

        AFolder sourceFolder2 = mock(AFolder.class);
        when(sourceFolderEntry2.getSourceFolder()).thenReturn(sourceFolder2);
        when(sourceFolder2.getProjectRelativePath()).thenReturn(Path.of("test"));
        when(sourceFolderEntry2.getValidationMessagesBundle()).thenReturn("test-validation-messages");
        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        AFile eclipseManifestFile = mockEclipseManifestFile(manifestFile);

        ipsBundleManifest.writeBuilderSettings(ipsProject, eclipseManifestFile);

        manifest = new Manifest(new FileInputStream(manifestFile));
        Attributes mainAttributes = manifest.getMainAttributes();

        assertThat(mainAttributes.get(Attributes.Name.MANIFEST_VERSION), is("1.0"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_GENERATOR_CONFIG), is(
                "org.faktorips.devtools.stdbuilder.StandardBuilderSet;testAttr=\"testValue\""));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_RUNTIME_ID_PREFIX), is("pre."));

        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_OBJECT_DIR), is(
                "model;toc=\"faktorips-repository-toc.xml\";validation-messages=\"validation-messages\",test;toc=\"faktorips-repository-toc.xml\";validation-messages=\"test-validation-messages\""));
    }

    @Test
    public void testWriteBuilderSettingsWithSourceFolderSpecificConfigurations() throws IOException {
        IIpsArtefactBuilderSet builderSet = mock(IIpsArtefactBuilderSet.class);
        when(builderSet.getId()).thenReturn(STANDARD_BUILDER_SET_ID);
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        when(ipsProject.getRuntimeIdPrefix()).thenReturn("pre.");
        IIpsArtefactBuilderSetConfig builderSetConfig = mock(IIpsArtefactBuilderSetConfig.class);
        when(builderSet.getConfig()).thenReturn(builderSetConfig);
        when(builderSetConfig.getPropertyNames()).thenReturn(new String[] { "testAttr" });
        when(builderSetConfig.getPropertyValue("testAttr")).thenReturn("testValue");
        when(builderSetConfig.getPropertyValue("testAttr")).thenReturn("testValue");

        IIpsObjectPath ipsObjectPath = mock(IIpsObjectPath.class);
        when(ipsProject.getIpsObjectPath()).thenReturn(ipsObjectPath);
        when(ipsObjectPath.getBasePackageNameForMergableJavaClasses()).thenReturn("org.test.package");
        when(ipsObjectPath.isOutputDefinedPerSrcFolder()).thenReturn(true);

        IIpsSrcFolderEntry sourceFolderEntry = mock(IIpsSrcFolderEntry.class);
        when(ipsObjectPath.getSourceFolderEntries()).thenReturn(new IIpsSrcFolderEntry[] { sourceFolderEntry });

        AFolder sourceFolder = mock(AFolder.class);
        when(sourceFolderEntry.getSourceFolder()).thenReturn(sourceFolder);
        when(sourceFolder.getProjectRelativePath()).thenReturn(Path.of("src", "main", "ips", "test"));
        when(sourceFolderEntry.getValidationMessagesBundle()).thenReturn("test-validation-messages");

        AFolder specificMergableFolder = mock(AFolder.class);
        AFolder specificDerivedFolder = mock(AFolder.class);
        when(sourceFolderEntry.getOutputFolderForMergableJavaFiles()).thenReturn(specificMergableFolder);
        when(sourceFolderEntry.getOutputFolderForDerivedJavaFiles()).thenReturn(specificDerivedFolder);

        Path specificMergablePath = mock(Path.class);
        Path specificDerivedPath = mock(Path.class);
        when(specificMergableFolder.getProjectRelativePath()).thenReturn(specificMergablePath);
        when(specificDerivedFolder.getProjectRelativePath()).thenReturn(specificDerivedPath);
        when(specificMergablePath.toString()).thenReturn("test-src");
        when(specificDerivedPath.toString()).thenReturn("test-resources");

        when(sourceFolderEntry.getBasePackageNameForMergableJavaClasses()).thenReturn("org.test.abc");

        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        AFile manifestAFile = mockEclipseManifestFile(manifestFile);

        ipsBundleManifest.writeBuilderSettings(ipsProject, manifestAFile);

        manifest = new Manifest(new FileInputStream(manifestFile));
        Attributes mainAttributes = manifest.getMainAttributes();

        assertThat(mainAttributes.get(Attributes.Name.MANIFEST_VERSION), is("1.0"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_GENERATOR_CONFIG), is(
                "org.faktorips.devtools.stdbuilder.StandardBuilderSet;testAttr=\"testValue\""));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_RUNTIME_ID_PREFIX), is("pre."));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_BASE_PACKAGE), is("org.test.package"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_SRC_OUT), is("test-src"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_RESOURCE_OUT), is("test-resources"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_OBJECT_DIR), is(
                "src/main/ips/test;toc=\"faktorips-repository-toc.xml\";validation-messages=\"test-validation-messages\""));

        Map<String, Attributes> entries = manifest.getEntries();
        assertThat(entries, hasKey("src/main/ips/test"));
        Attributes entry = entries.get("src/main/ips/test");
        assertThat(entry, hasAttribute(IpsBundleManifest.HEADER_BASE_PACKAGE, "org.test.abc"));
        assertThat(entry, hasAttribute(IpsBundleManifest.HEADER_SRC_OUT, "test-src"));
        assertThat(entry, hasAttribute(IpsBundleManifest.HEADER_RESOURCE_OUT, "test-resources"));
    }

    @Test
    public void testWriteBuilderSettingsWithSourceFolderSpecificConfigurations_CleansUpEntries() throws IOException {
        Map<String, Attributes> entries = manifest.getEntries();
        Attributes oldIpsAttributes = new Attributes();
        entries.put("oldIpsEntry", oldIpsAttributes);
        oldIpsAttributes.putValue(IpsBundleManifest.HEADER_BASE_PACKAGE, "base");
        oldIpsAttributes.putValue(IpsBundleManifest.HEADER_SRC_OUT, "src");
        oldIpsAttributes.putValue(IpsBundleManifest.HEADER_RESOURCE_OUT, "derived");
        Attributes oldNonIpsAttributes = new Attributes();
        entries.put("oldNonIpsEntry", oldNonIpsAttributes);
        oldNonIpsAttributes.putValue("foo", "bar");
        IIpsArtefactBuilderSet builderSet = mock(IIpsArtefactBuilderSet.class);
        when(builderSet.getId()).thenReturn(STANDARD_BUILDER_SET_ID);
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        when(ipsProject.getRuntimeIdPrefix()).thenReturn("pre.");
        IIpsArtefactBuilderSetConfig builderSetConfig = mock(IIpsArtefactBuilderSetConfig.class);
        when(builderSet.getConfig()).thenReturn(builderSetConfig);
        when(builderSetConfig.getPropertyNames()).thenReturn(new String[] { "testAttr" });
        when(builderSetConfig.getPropertyValue("testAttr")).thenReturn("testValue");
        when(builderSetConfig.getPropertyValue("testAttr")).thenReturn("testValue");

        IIpsObjectPath ipsObjectPath = mock(IIpsObjectPath.class);
        when(ipsProject.getIpsObjectPath()).thenReturn(ipsObjectPath);
        when(ipsObjectPath.getBasePackageNameForMergableJavaClasses()).thenReturn("org.test.package");
        when(ipsObjectPath.isOutputDefinedPerSrcFolder()).thenReturn(true);

        IIpsSrcFolderEntry sourceFolderEntry = mock(IIpsSrcFolderEntry.class);
        when(ipsObjectPath.getSourceFolderEntries()).thenReturn(new IIpsSrcFolderEntry[] { sourceFolderEntry });

        AFolder sourceFolder = mock(AFolder.class);
        when(sourceFolderEntry.getSourceFolder()).thenReturn(sourceFolder);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("src\\main\\ips\\test");
        when(sourceFolder.getProjectRelativePath()).thenReturn(path);
        when(sourceFolderEntry.getValidationMessagesBundle()).thenReturn("test-validation-messages");

        AFolder specificMergableFolder = mock(AFolder.class);
        AFolder specificDerivedFolder = mock(AFolder.class);
        when(sourceFolderEntry.getOutputFolderForMergableJavaFiles()).thenReturn(specificMergableFolder);
        when(sourceFolderEntry.getOutputFolderForDerivedJavaFiles()).thenReturn(specificDerivedFolder);

        Path specificMergablePath = mock(Path.class);
        Path specificDerivedPath = mock(Path.class);
        when(specificMergableFolder.getProjectRelativePath()).thenReturn(specificMergablePath);
        when(specificDerivedFolder.getProjectRelativePath()).thenReturn(specificDerivedPath);
        when(specificMergablePath.toString()).thenReturn("test-src");
        when(specificDerivedPath.toString()).thenReturn("test-resources");

        when(sourceFolderEntry.getBasePackageNameForMergableJavaClasses()).thenReturn("org.test.abc");

        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        AFile manifestAFile = mockEclipseManifestFile(manifestFile);
        ipsBundleManifest.writeBuilderSettings(ipsProject, manifestAFile);

        manifest = new Manifest(new FileInputStream(manifestFile));
        Attributes mainAttributes = manifest.getMainAttributes();

        assertThat(mainAttributes.get(Attributes.Name.MANIFEST_VERSION), is("1.0"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_GENERATOR_CONFIG), is(
                "org.faktorips.devtools.stdbuilder.StandardBuilderSet;testAttr=\"testValue\""));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_RUNTIME_ID_PREFIX), is("pre."));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_BASE_PACKAGE), is("org.test.package"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_SRC_OUT), is("test-src"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_RESOURCE_OUT), is("test-resources"));
        assertThat(mainAttributes.getValue(IpsBundleManifest.HEADER_OBJECT_DIR), is(
                "src/main/ips/test;toc=\"faktorips-repository-toc.xml\";validation-messages=\"test-validation-messages\""));

        manifest = new Manifest(new FileInputStream(manifestFile));
        entries = manifest.getEntries();
        assertThat(entries, not(hasKey("oldIpsEntry")));
        assertThat(entries, hasKey("oldNonIpsEntry"));
        Attributes entry = entries.get("oldNonIpsEntry");
        assertThat(entry, hasAttribute("foo", "bar"));

        assertThat(entries, hasKey("src/main/ips/test"));
        entry = entries.get("src/main/ips/test");
        assertThat(entry, hasAttribute(IpsBundleManifest.HEADER_BASE_PACKAGE, "org.test.abc"));
        assertThat(entry, hasAttribute(IpsBundleManifest.HEADER_SRC_OUT, "test-src"));
        assertThat(entry, hasAttribute(IpsBundleManifest.HEADER_RESOURCE_OUT, "test-resources"));
    }

    private Matcher<Attributes> hasAttribute(String key, String value) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("has an attribute '");
                description.appendValue(key);
                description.appendText("' with the value '");
                description.appendValue(value);
                description.appendText("'");
            }

            protected void describeMismatchSafely(Attributes attributes, Description mismatchDescription) {
                mismatchDescription
                        .appendText(attributes.entrySet().stream().map(e -> "<" + e.getKey() + ":" + e.getValue() + ">")
                                .collect(Collectors.joining(", ", "[", "]")));
            }

            @Override
            protected boolean matchesSafely(Attributes attributes) {
                return Objects.equals(attributes.getValue(key), value);
            }
        };
    }

    @Test
    public void testSortBySourceFolder_maintainsSourceFolderOrder() throws IOException {
        IIpsArtefactBuilderSet builderSet = mock(IIpsArtefactBuilderSet.class);
        when(builderSet.getId()).thenReturn(STANDARD_BUILDER_SET_ID);
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        when(ipsProject.getRuntimeIdPrefix()).thenReturn("pre.");
        IIpsArtefactBuilderSetConfig builderSetConfig = mock(IIpsArtefactBuilderSetConfig.class);
        when(builderSet.getConfig()).thenReturn(builderSetConfig);
        when(builderSetConfig.getPropertyNames()).thenReturn(new String[] {});

        IIpsObjectPath ipsObjectPath = mock(IIpsObjectPath.class);
        when(ipsProject.getIpsObjectPath()).thenReturn(ipsObjectPath);
        when(ipsObjectPath.getBasePackageNameForMergableJavaClasses()).thenReturn("org.test.package");
        when(ipsObjectPath.isOutputDefinedPerSrcFolder()).thenReturn(true);

        // Create three source folders in specific order: model, test, integration
        IIpsSrcFolderEntry sourceFolderEntry1 = mock(IIpsSrcFolderEntry.class);
        IIpsSrcFolderEntry sourceFolderEntry2 = mock(IIpsSrcFolderEntry.class);
        IIpsSrcFolderEntry sourceFolderEntry3 = mock(IIpsSrcFolderEntry.class);
        when(ipsObjectPath.getSourceFolderEntries())
                .thenReturn(new IIpsSrcFolderEntry[] { sourceFolderEntry1, sourceFolderEntry2, sourceFolderEntry3 });

        AFolder sourceFolder1 = mock(AFolder.class);
        when(sourceFolderEntry1.getSourceFolder()).thenReturn(sourceFolder1);
        when(sourceFolder1.getProjectRelativePath()).thenReturn(Path.of("model"));
        when(sourceFolderEntry1.getValidationMessagesBundle()).thenReturn("validation-messages");

        AFolder sourceFolder2 = mock(AFolder.class);
        when(sourceFolderEntry2.getSourceFolder()).thenReturn(sourceFolder2);
        when(sourceFolder2.getProjectRelativePath()).thenReturn(Path.of("test"));
        when(sourceFolderEntry2.getValidationMessagesBundle()).thenReturn("test-validation-messages");

        AFolder sourceFolder3 = mock(AFolder.class);
        when(sourceFolderEntry3.getSourceFolder()).thenReturn(sourceFolder3);
        when(sourceFolder3.getProjectRelativePath()).thenReturn(Path.of("integration"));
        when(sourceFolderEntry3.getValidationMessagesBundle()).thenReturn("integration-validation-messages");

        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        AFile eclipseManifestFile = mockEclipseManifestFile(manifestFile);

        ipsBundleManifest.writeBuilderSettings(ipsProject, eclipseManifestFile);

        String manifestContent = Files.readString(manifestFile.toPath());

        // Verify that Name: sections appear in the order they were added: model, test, integration
        int modelIndex = manifestContent.indexOf("Name: model");
        int testIndex = manifestContent.indexOf("Name: test");
        int integrationIndex = manifestContent.indexOf("Name: integration");

        assertThat(modelIndex, is(not(-1)));
        assertThat(testIndex, is(not(-1)));
        assertThat(integrationIndex, is(not(-1)));

        // Verify the order: model < test < integration
        assertThat("model should come before test", modelIndex < testIndex, is(true));
        assertThat("test should come before integration", testIndex < integrationIndex, is(true));
    }

    @Test
    public void testSortBySourceFolder_withAlphabeticallyReversedOrder() throws IOException {
        IIpsArtefactBuilderSet builderSet = mock(IIpsArtefactBuilderSet.class);
        when(builderSet.getId()).thenReturn(STANDARD_BUILDER_SET_ID);
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        when(ipsProject.getRuntimeIdPrefix()).thenReturn("pre.");
        IIpsArtefactBuilderSetConfig builderSetConfig = mock(IIpsArtefactBuilderSetConfig.class);
        when(builderSet.getConfig()).thenReturn(builderSetConfig);
        when(builderSetConfig.getPropertyNames()).thenReturn(new String[] {});

        IIpsObjectPath ipsObjectPath = mock(IIpsObjectPath.class);
        when(ipsProject.getIpsObjectPath()).thenReturn(ipsObjectPath);
        when(ipsObjectPath.getBasePackageNameForMergableJavaClasses()).thenReturn("org.test.package");
        when(ipsObjectPath.isOutputDefinedPerSrcFolder()).thenReturn(true);

        // Create folders in reverse alphabetical order: zebra, yankee, xray
        IIpsSrcFolderEntry sourceFolderEntry1 = mock(IIpsSrcFolderEntry.class);
        IIpsSrcFolderEntry sourceFolderEntry2 = mock(IIpsSrcFolderEntry.class);
        IIpsSrcFolderEntry sourceFolderEntry3 = mock(IIpsSrcFolderEntry.class);
        when(ipsObjectPath.getSourceFolderEntries())
                .thenReturn(new IIpsSrcFolderEntry[] { sourceFolderEntry1, sourceFolderEntry2, sourceFolderEntry3 });

        AFolder sourceFolder1 = mock(AFolder.class);
        when(sourceFolderEntry1.getSourceFolder()).thenReturn(sourceFolder1);
        when(sourceFolder1.getProjectRelativePath()).thenReturn(Path.of("zebra"));
        when(sourceFolderEntry1.getValidationMessagesBundle()).thenReturn("validation-messages");

        AFolder sourceFolder2 = mock(AFolder.class);
        when(sourceFolderEntry2.getSourceFolder()).thenReturn(sourceFolder2);
        when(sourceFolder2.getProjectRelativePath()).thenReturn(Path.of("yankee"));
        when(sourceFolderEntry2.getValidationMessagesBundle()).thenReturn("validation-messages");

        AFolder sourceFolder3 = mock(AFolder.class);
        when(sourceFolderEntry3.getSourceFolder()).thenReturn(sourceFolder3);
        when(sourceFolder3.getProjectRelativePath()).thenReturn(Path.of("xray"));
        when(sourceFolderEntry3.getValidationMessagesBundle()).thenReturn("validation-messages");

        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        AFile eclipseManifestFile = mockEclipseManifestFile(manifestFile);

        ipsBundleManifest.writeBuilderSettings(ipsProject, eclipseManifestFile);

        String manifestContent = Files.readString(manifestFile.toPath());

        // Verify that Name: sections appear in array order (zebra, yankee, xray), NOT alphabetical order
        int zebraIndex = manifestContent.indexOf("Name: zebra");
        int yankeeIndex = manifestContent.indexOf("Name: yankee");
        int xrayIndex = manifestContent.indexOf("Name: xray");

        assertThat(zebraIndex, is(not(-1)));
        assertThat(yankeeIndex, is(not(-1)));
        assertThat(xrayIndex, is(not(-1)));

        // Verify the order matches array order, not alphabetical
        assertThat("zebra should come before yankee", zebraIndex < yankeeIndex, is(true));
        assertThat("yankee should come before xray", yankeeIndex < xrayIndex, is(true));
    }

    @Test
    public void testSortBySourceFolder_singleSourceFolder() throws IOException {
        IIpsArtefactBuilderSet builderSet = mock(IIpsArtefactBuilderSet.class);
        when(builderSet.getId()).thenReturn(STANDARD_BUILDER_SET_ID);
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        when(ipsProject.getRuntimeIdPrefix()).thenReturn("pre.");
        IIpsArtefactBuilderSetConfig builderSetConfig = mock(IIpsArtefactBuilderSetConfig.class);
        when(builderSet.getConfig()).thenReturn(builderSetConfig);
        when(builderSetConfig.getPropertyNames()).thenReturn(new String[] {});

        IIpsObjectPath ipsObjectPath = mock(IIpsObjectPath.class);
        when(ipsProject.getIpsObjectPath()).thenReturn(ipsObjectPath);
        when(ipsObjectPath.getBasePackageNameForMergableJavaClasses()).thenReturn("org.test.package");
        when(ipsObjectPath.isOutputDefinedPerSrcFolder()).thenReturn(true);

        IIpsSrcFolderEntry sourceFolderEntry = mock(IIpsSrcFolderEntry.class);
        when(ipsObjectPath.getSourceFolderEntries()).thenReturn(new IIpsSrcFolderEntry[] { sourceFolderEntry });

        AFolder sourceFolder = mock(AFolder.class);
        when(sourceFolderEntry.getSourceFolder()).thenReturn(sourceFolder);
        when(sourceFolder.getProjectRelativePath()).thenReturn(Path.of("model"));
        when(sourceFolderEntry.getValidationMessagesBundle()).thenReturn("validation-messages");

        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        AFile eclipseManifestFile = mockEclipseManifestFile(manifestFile);

        ipsBundleManifest.writeBuilderSettings(ipsProject, eclipseManifestFile);

        String manifestContent = Files.readString(manifestFile.toPath());

        // Should contain the single Name: section
        assertThat(manifestContent, containsString("Name: model"));

        // Should end with a newline
        assertThat(manifestContent.endsWith("\n"), is(true));
    }
}
