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
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.eclipse.osgi.util.ManifestElement;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsBundleManifestTest {

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

    @Mock
    private Manifest manifest;

    private IpsBundleManifest ipsBundleManifest;

    @Mock
    private IpsProject ipsProject;

    @Before
    public void createIpsBundleManifest() {
        mockManifest();
        ipsBundleManifest = new IpsBundleManifest(manifest);
    }

    public void mockManifest() {
        Attributes attributes = mock(Attributes.class);
        when(manifest.getMainAttributes()).thenReturn(attributes);
        when(attributes.getValue(IpsBundleManifest.HEADER_BASE_PACKAGE)).thenReturn(MY_BASE_PACKAGE);
        when(attributes.getValue(IpsBundleManifest.HEADER_UNIQUE_QUALIFIER)).thenReturn(MY_UNIQUE_QUALIFIER);
        when(attributes.getValue(IpsBundleManifest.HEADER_SRC_OUT)).thenReturn(MY_SRC_OUT);
        when(attributes.getValue(IpsBundleManifest.HEADER_RESOURCE_OUT)).thenReturn(MY_RESOURCE_OUT);
        when(attributes.getValue(IpsBundleManifest.HEADER_OBJECT_DIR))
                .thenReturn(MY_OBJECT_DIR + " ; toc=\"" + MY_TOC + "\";validation-messages=\"" + MY_MESSAGES + "\"");
        when(attributes.getValue(IpsBundleManifest.HEADER_GENERATOR_CONFIG)).thenReturn(
                STANDARD_BUILDER_SET_ID + ";" + IpsProjectProperties.ATTRIBUTE_CHANGES_IN_TIME_NAMING_CONVENTION + "=\""
                        + IChangesOverTimeNamingConvention.FAKTOR_IPS + "\";" + ATTRIBUTE_GENERATE_PUBLISHED_INTERFACES
                        + "=\"" + TRUE + "\"");
        when(attributes.getValue(IpsBundleManifest.HEADER_RUNTIME_ID_PREFIX)).thenReturn(MY_PREFIX);

        Attributes attributesForObjectDir = mock(Attributes.class);
        when(manifest.getAttributes(MY_OBJECT_DIR)).thenReturn(attributesForObjectDir);
        when(attributesForObjectDir.getValue(IpsBundleManifest.HEADER_BASE_PACKAGE)).thenReturn(MY_BASE_PACKAGE2);
        when(attributesForObjectDir.getValue(IpsBundleManifest.HEADER_UNIQUE_QUALIFIER))
                .thenReturn(MY_UNIQUE_QUALIFIER2);
        when(attributesForObjectDir.getValue(IpsBundleManifest.HEADER_SRC_OUT)).thenReturn(MY_SRC_OUT2);
        when(attributesForObjectDir.getValue(IpsBundleManifest.HEADER_RESOURCE_OUT)).thenReturn(MY_RESOURCE_OUT2);
    }

    @Test
    public void testGetBasePackage() {
        when(manifest.getMainAttributes().getValue(IpsBundleManifest.HEADER_BASE_PACKAGE))
                .thenReturn(" " + MY_BASE_PACKAGE + " ");

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
    public void testGetBasePackage_forObjectDirTrim() {
        when(manifest.getAttributes(MY_OBJECT_DIR).getValue(IpsBundleManifest.HEADER_BASE_PACKAGE))
                .thenReturn(" " + MY_BASE_PACKAGE2 + " ");

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
    public void testGetSourcecodeOutput_trim() {
        when(manifest.getMainAttributes().getValue(IpsBundleManifest.HEADER_SRC_OUT)).thenReturn(MY_SRC_OUT + " ");

        String srcOutput = ipsBundleManifest.getSourcecodeOutput();

        assertEquals(MY_SRC_OUT, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_objectDirAndTrim() {
        when(manifest.getAttributes(MY_OBJECT_DIR).getValue(IpsBundleManifest.HEADER_SRC_OUT))
                .thenReturn(MY_SRC_OUT2 + " ");

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
    public void testGetResourceOutput_trim() {
        when(manifest.getMainAttributes().getValue(IpsBundleManifest.HEADER_RESOURCE_OUT))
                .thenReturn(MY_RESOURCE_OUT + " ");

        String srcOutput = ipsBundleManifest.getResourceOutput();

        assertEquals(MY_RESOURCE_OUT, srcOutput);
    }

    @Test
    public void testGetResourceOutput_forObjectDir() {
        String srcOutput = ipsBundleManifest.getResourceOutput(MY_OBJECT_DIR);

        assertEquals(MY_RESOURCE_OUT2, srcOutput);
    }

    @Test
    public void testGetResourceOutput_forObjectDirTrim() {
        when(manifest.getAttributes(MY_OBJECT_DIR).getValue(IpsBundleManifest.HEADER_RESOURCE_OUT))
                .thenReturn(MY_RESOURCE_OUT2 + " ");

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
    public void testGetObjectDirElements_noValue() {
        Attributes attributes = mock(Attributes.class);
        when(manifest.getMainAttributes()).thenReturn(attributes);
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
        AFile eclipseManifestFile = mock(AFile.class);
        Path eclipseManifestPath = mock(Path.class);
        when(eclipseManifestFile.getLocation()).thenReturn(eclipseManifestPath);
        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        when(eclipseManifestPath.toFile()).thenReturn(manifestFile);
        Attributes mainAttributes = manifest.getMainAttributes();

        ipsBundleManifest.writeBuilderSettings(ipsProject, eclipseManifestFile);

        verify(mainAttributes).put(new Attributes.Name(IpsBundleManifest.HEADER_GENERATOR_CONFIG),
                "org.faktorips.devtools.stdbuilder.StandardBuilderSet;booleanAttr=\"true\";intAttr=\"42\";stringAttr=\"Foo \\\"quoted\\\" Bar\"");
        verify(mainAttributes).put(new Attributes.Name(IpsBundleManifest.HEADER_RUNTIME_ID_PREFIX), "pre.");
        verify(manifest).write(any(OutputStream.class));
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

        AFile eclipseManifestFile = mock(AFile.class);
        Path eclipseManifestPath = mock(Path.class);
        when(eclipseManifestFile.getLocation()).thenReturn(eclipseManifestPath);
        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        when(eclipseManifestPath.toFile()).thenReturn(manifestFile);
        Attributes mainAttributes = manifest.getMainAttributes();

        ipsBundleManifest.writeBuilderSettings(ipsProject, eclipseManifestFile);
        verify(mainAttributes).put(Attributes.Name.MANIFEST_VERSION, "1.0");
        verify(mainAttributes).put(new Attributes.Name(IpsBundleManifest.HEADER_GENERATOR_CONFIG),
                "org.faktorips.devtools.stdbuilder.StandardBuilderSet;testAttr=\"testValue\"");
        verify(mainAttributes).put(new Attributes.Name(IpsBundleManifest.HEADER_RUNTIME_ID_PREFIX), "pre.");
        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_BASE_PACKAGE, "org.test.package");
        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_SRC_OUT, "src");
        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_RESOURCE_OUT, "resources");
        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_OBJECT_DIR,
                "model;toc=\"faktorips-repository-toc.xml\";validation-messages=\"validation-messages\"");
        verify(manifest).write(any(OutputStream.class));
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
        AFile eclipseManifestFile = mock(AFile.class);
        Path eclipseManifestPath = mock(Path.class);
        when(eclipseManifestFile.getLocation()).thenReturn(eclipseManifestPath);
        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        when(eclipseManifestPath.toFile()).thenReturn(manifestFile);
        Attributes mainAttributes = manifest.getMainAttributes();
        ipsBundleManifest.writeBuilderSettings(ipsProject, eclipseManifestFile);
        verify(mainAttributes).put(Attributes.Name.MANIFEST_VERSION, "1.0");
        verify(mainAttributes).put(new Attributes.Name(IpsBundleManifest.HEADER_GENERATOR_CONFIG),
                "org.faktorips.devtools.stdbuilder.StandardBuilderSet;testAttr=\"testValue\"");
        verify(mainAttributes).put(new Attributes.Name(IpsBundleManifest.HEADER_RUNTIME_ID_PREFIX), "pre.");

        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_OBJECT_DIR,
                "model;toc=\"faktorips-repository-toc.xml\";validation-messages=\"validation-messages\",test;toc=\"faktorips-repository-toc.xml\";validation-messages=\"test-validation-messages\"");
    }

    @Test
    public void testWriteBuilderSettingsWithSourceFolderSpecificConfigurations() throws IOException {
        Map<String, Attributes> entries = new LinkedHashMap<>();
        when(manifest.getEntries()).thenReturn(entries);
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

        AFile manifestAFile = mock(AFile.class);
        Path manifestPath = mock(Path.class);
        when(manifestAFile.getLocation()).thenReturn(manifestPath);
        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        when(manifestPath.toFile()).thenReturn(manifestFile);
        Attributes mainAttributes = manifest.getMainAttributes();

        ipsBundleManifest.writeBuilderSettings(ipsProject, manifestAFile);

        verify(mainAttributes).put(new Attributes.Name(IpsBundleManifest.HEADER_GENERATOR_CONFIG),
                "org.faktorips.devtools.stdbuilder.StandardBuilderSet;testAttr=\"testValue\"");
        verify(mainAttributes).put(new Attributes.Name(IpsBundleManifest.HEADER_RUNTIME_ID_PREFIX), "pre.");
        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_BASE_PACKAGE, "org.test.package");
        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_SRC_OUT, "test-src");
        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_RESOURCE_OUT, "test-resources");
        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_OBJECT_DIR,
                "src/main/ips/test;toc=\"faktorips-repository-toc.xml\";validation-messages=\"test-validation-messages\"");

        assertThat(entries, hasKey("src/main/ips/test"));
        Attributes entry = entries.get("src/main/ips/test");
        assertThat(entry, hasAttribute(IpsBundleManifest.HEADER_BASE_PACKAGE, "org.test.abc"));
        assertThat(entry, hasAttribute(IpsBundleManifest.HEADER_SRC_OUT, "test-src"));
        assertThat(entry, hasAttribute(IpsBundleManifest.HEADER_RESOURCE_OUT, "test-resources"));
    }

    @Test
    public void testWriteBuilderSettingsWithSourceFolderSpecificConfigurations_CleansUpEntries() throws IOException {
        Map<String, Attributes> entries = new LinkedHashMap<>();
        Attributes oldIpsAttributes = new Attributes();
        entries.put("oldIpsEntry", oldIpsAttributes);
        oldIpsAttributes.putValue(IpsBundleManifest.HEADER_BASE_PACKAGE, "base");
        oldIpsAttributes.putValue(IpsBundleManifest.HEADER_SRC_OUT, "src");
        oldIpsAttributes.putValue(IpsBundleManifest.HEADER_RESOURCE_OUT, "derived");
        Attributes oldNonIpsAttributes = new Attributes();
        entries.put("oldNonIpsEntry", oldNonIpsAttributes);
        oldNonIpsAttributes.putValue("foo", "bar");
        when(manifest.getEntries()).thenReturn(entries);
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

        AFile manifestAFile = mock(AFile.class);
        Path manifestPath = mock(Path.class);
        when(manifestAFile.getLocation()).thenReturn(manifestPath);
        File manifestFile = Files.createTempFile("MANIFEST", "MF").toFile();
        when(manifestPath.toFile()).thenReturn(manifestFile);
        Attributes mainAttributes = manifest.getMainAttributes();

        ipsBundleManifest.writeBuilderSettings(ipsProject, manifestAFile);

        verify(mainAttributes).put(new Attributes.Name(IpsBundleManifest.HEADER_GENERATOR_CONFIG),
                "org.faktorips.devtools.stdbuilder.StandardBuilderSet;testAttr=\"testValue\"");
        verify(mainAttributes).put(new Attributes.Name(IpsBundleManifest.HEADER_RUNTIME_ID_PREFIX), "pre.");
        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_BASE_PACKAGE, "org.test.package");
        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_SRC_OUT, "test-src");
        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_RESOURCE_OUT, "test-resources");
        verify(mainAttributes).putValue(IpsBundleManifest.HEADER_OBJECT_DIR,
                "src/main/ips/test;toc=\"faktorips-repository-toc.xml\";validation-messages=\"test-validation-messages\"");

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
}
