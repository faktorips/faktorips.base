/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.builder.base.BuilderKindIds;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.prefs.Preferences;

public class ManifestBuilderTest extends AbstractStdBuilderTest {

    private ManifestBuilder manifestBuilder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        manifestBuilder = builderSet.getBuilderById(BuilderKindIds.MANIFEST_FILE, ManifestBuilder.class);
        String manifestContent = """
                Manifest-Version: 1.0
                Bundle-ManifestVersion: 2
                Bundle-Name: Test Bundle
                Bundle-SymbolicName: %s
                Bundle-Version: 26.1.0.qualifier
                """.formatted(ipsProject.getName());
        createManifestFileInTestProject(manifestContent);
        configureOutputFolders();
    }

    @Test
    public void testManifestBuilderExists() {
        assertThat(manifestBuilder, is(notNullValue()));
        assertThat(manifestBuilder.getName(), is(("ManifestBuilder")));
    }

    @Test
    public void testManifestBuilderNotForSpecificFiles() {
        assertThat(manifestBuilder.isBuilderFor(null), is(false));
    }

    @Test
    public void testManifestUpdatedOnFullBuild() throws Exception {
        AFile aManifestFile = ipsProject.getProject().getFile(IpsBundleManifest.MANIFEST_NAME);
        assertThat(aManifestFile.exists(), is(true));

        Attributes initialAttrs = readManifestAttributes(aManifestFile);
        assertThat(initialAttrs.getValue("Fips-BasePackage"), is(nullValue()));
        assertThat(initialAttrs.getValue("Fips-SourcecodeOutput"), is(nullValue()));
        assertThat(initialAttrs.getValue("Fips-ResourceOutput"), is(nullValue()));
        assertThat(initialAttrs.getValue("Fips-GeneratorConfig"), is(nullValue()));
        assertThat(initialAttrs.getValue("Fips-ObjectDir"), is(nullValue()));
        assertThat(initialAttrs.getValue("Fips-RuntimeIdPrefix"), is(nullValue()));

        ipsProject.getProject().build(ABuildKind.FULL, null);
        Attributes afterAttrs = readManifestAttributes(aManifestFile);

        assertThat(afterAttrs.getValue("Fips-BasePackage"), is("org.fipsi"));
        assertThat(afterAttrs.getValue("Fips-SourcecodeOutput"), is("src/main/java"));
        assertThat(afterAttrs.getValue("Fips-ResourceOutput"), is("src/main/resources"));
        assertThat(afterAttrs.getValue("Fips-GeneratorConfig"),
                startsWith("org.faktorips.devtools.stdbuilder.ipsstdbuilderset"));
        assertThat(afterAttrs.getValue("Fips-GeneratorConfig"), containsString("generateJaxbSupport"));
        assertThat(afterAttrs.getValue("Fips-GeneratorConfig"), containsString("generatePublishedInterfaces"));
        assertThat(afterAttrs.getValue("Fips-ObjectDir"), startsWith("productdef"));
        assertThat(afterAttrs.getValue("Fips-ObjectDir"), containsString("toc=\"faktorips-repository-toc.xml\""));
        assertThat(afterAttrs.getValue("Fips-RuntimeIdPrefix"), is(notNullValue()));
    }

    @Test
    public void testLineBreaks_Linux() {
        IProject project = ipsProject.getProject().unwrap();
        Preferences preferences = Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE)
                .node(project.getName());
        preferences.node(Platform.PI_RUNTIME).put(Platform.PREF_LINE_SEPARATOR,
                "\n");
        AFile aManifestFile = ipsProject.getProject().getFile(IpsBundleManifest.MANIFEST_NAME);

        ipsProject.getProject().build(ABuildKind.FULL, null);

        String content = getContentAsString(aManifestFile.getContents(), StandardCharsets.UTF_8.name());
        assertThat(content, containsString("\n"));
        assertThat(content, not(containsString("\r\n")));
    }

    @Test
    public void testLineBreaks_Windows() {
        IProject project = ipsProject.getProject().unwrap();
        Preferences preferences = Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE)
                .node(project.getName());
        preferences.node(Platform.PI_RUNTIME).put(Platform.PREF_LINE_SEPARATOR,
                "\r\n");
        AFile aManifestFile = ipsProject.getProject().getFile(IpsBundleManifest.MANIFEST_NAME);

        ipsProject.getProject().build(ABuildKind.FULL, null);

        String content = getContentAsString(aManifestFile.getContents(), StandardCharsets.UTF_8.name());
        assertThat(content, containsString("\r\n"));
    }

    @Test
    public void testNoBackslashes_Windows() throws Exception {
        IProject project = ipsProject.getProject().unwrap();
        Preferences preferences = Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE)
                .node(project.getName());
        preferences.node(Platform.PI_RUNTIME).put(Platform.PREF_LINE_SEPARATOR, "\n");
        AFile aManifestFile = ipsProject.getProject().getFile(IpsBundleManifest.MANIFEST_NAME);
        String content = getContentAsString(aManifestFile.getContents(), StandardCharsets.UTF_8.name());
        content = content + "Fips-SourcecodeOutput: src\\main\\java\n" + "Fips-ResourceOutput: src\\main\\resources";
        createManifestFileInTestProject(content);

        ipsProject.getProject().build(ABuildKind.FULL, null);

        content = getContentAsString(aManifestFile.getContents(), StandardCharsets.UTF_8.name());
        assertThat(content, containsString("Fips-SourcecodeOutput"));
        assertThat(content, containsString("Fips-ResourceOutput"));
        assertThat(content, not(containsString("\\")));
    }

    private String getContentAsString(InputStream is, String charSet) {
        try {
            return StringUtil.readFromInputStream(is, charSet);
        } catch (IOException e) {
            throw new IpsException(new IpsStatus(e));
        }
    }

    private void createManifestFileInTestProject(String manifestContent) throws Exception {
        IProject project = (IProject)ipsProject.getProject().unwrap();

        IFolder metaInfFolder = project.getFolder("META-INF");
        if (!metaInfFolder.exists()) {
            metaInfFolder.create(true, true, null);
        }

        IFile manifestFile = metaInfFolder.getFile("MANIFEST.MF");
        if (!manifestFile.exists()) {
            manifestFile.create(new ByteArrayInputStream(manifestContent.getBytes()), true, null);
        }
    }

    private void configureOutputFolders() throws Exception {
        AFolder srcFolder = ipsProject.getProject().getFolder("src");
        if (!srcFolder.exists()) {
            srcFolder.create(null);
        }
        AFolder mainFolder = srcFolder.getFolder("main");
        if (!mainFolder.exists()) {
            mainFolder.create(null);
        }
        AFolder javaFolder = mainFolder.getFolder("java");
        if (!javaFolder.exists()) {
            javaFolder.create(null);
        }
        AFolder resourcesFolder = mainFolder.getFolder("resources");
        if (!resourcesFolder.exists()) {
            resourcesFolder.create(null);
        }

        IIpsObjectPath objectPath = ipsProject.getIpsObjectPath();
        objectPath.setOutputFolderForMergableSources(javaFolder);
        objectPath.setOutputFolderForDerivedSources(resourcesFolder);
        objectPath.setBasePackageNameForMergableJavaClasses("org.fipsi");
        objectPath.setBasePackageNameForDerivedJavaClasses("org.fipsi");
        objectPath.setOutputDefinedPerSrcFolder(false);
    }

    private Attributes readManifestAttributes(AFile manifestFile) throws IOException {
        try (InputStream in = manifestFile.getContents()) {
            return new Manifest(in).getMainAttributes();
        }
    }

}
