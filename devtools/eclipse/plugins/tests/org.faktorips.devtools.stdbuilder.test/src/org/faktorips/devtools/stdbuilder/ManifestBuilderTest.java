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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.exception.IpsException;
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
        createManifestFileInTestProject();
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

        String initialContent = readManifestContent(aManifestFile);
        assertThat(initialContent, not(containsString("Fips-BasePackage")));
        assertThat(initialContent, not(containsString("Fips-SourcecodeOutput")));
        assertThat(initialContent, not(containsString("Fips-ResourceOutput")));
        assertThat(initialContent, not(containsString("Fips-GeneratorConfig")));
        assertThat(initialContent, not(containsString("Fips-ObjectDir")));
        assertThat(initialContent, not(containsString("toc=")));
        assertThat(initialContent, not(containsString("generateJaxbSupport")));
        assertThat(initialContent, not(containsString("generatePublishedInterfaces")));
        assertThat(initialContent, not(containsString("Fips-RuntimeIdPrefix")));

        ipsProject.getProject().build(ABuildKind.FULL, null);
        String afterBuild = readManifestContent(aManifestFile);

        assertThat(afterBuild, containsString("Fips-BasePackage: org.fipsi"));
        assertThat(afterBuild, containsString("Fips-SourcecodeOutput: src"));
        assertThat(afterBuild, containsString("Fips-ResourceOutput: resources"));
        assertThat(afterBuild,
                containsString("Fips-GeneratorConfig: org.faktorips.devtools.stdbuilder.ipsstdbuilderset"));
        assertThat(afterBuild, containsString("Fips-ObjectDir: productdef"));
        assertThat(afterBuild, containsString("toc=\"faktorips-repository-toc.xml\""));
        assertThat(afterBuild, containsString("generateJaxbSupport"));
        assertThat(afterBuild, containsString("generatePublishedInterfaces"));
        assertThat(afterBuild, containsString("Fips-RuntimeIdPrefix"));
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

    private String getContentAsString(InputStream is, String charSet) {
        try {
            return StringUtil.readFromInputStream(is, charSet);
        } catch (IOException e) {
            throw new IpsException(new IpsStatus(e));
        }
    }

    private void createManifestFileInTestProject() throws Exception {
        IProject project = (IProject)ipsProject.getProject().unwrap();

        IFolder metaInfFolder = project.getFolder("META-INF");
        if (!metaInfFolder.exists()) {
            metaInfFolder.create(true, true, null);
        }

        IFile manifestFile = metaInfFolder.getFile("MANIFEST.MF");
        if (!manifestFile.exists()) {
            String manifestContent = "Manifest-Version: 1.0\n" +
                    "Bundle-ManifestVersion: 2\n" +
                    "Bundle-Name: Test Bundle\n" +
                    "Bundle-SymbolicName: " + project.getName() + "\n" +
                    "Bundle-Version: 26.1.0.qualifier\n";

            manifestFile.create(new ByteArrayInputStream(manifestContent.getBytes()), true, null);
        }
    }

    private void configureOutputFolders() throws Exception {
        AFolder srcFolder = ipsProject.getProject().getFolder("src");
        if (!srcFolder.exists()) {
            srcFolder.create(null);
        }

        AFolder resourcesFolder = ipsProject.getProject().getFolder("resources");
        if (!resourcesFolder.exists()) {
            resourcesFolder.create(null);
        }

        IIpsObjectPath objectPath = ipsProject.getIpsObjectPath();
        objectPath.setOutputFolderForMergableSources(srcFolder);
        objectPath.setOutputFolderForDerivedSources(resourcesFolder);
        objectPath.setBasePackageNameForMergableJavaClasses("org.fipsi");
        objectPath.setBasePackageNameForDerivedJavaClasses("org.fipsi");
        objectPath.setOutputDefinedPerSrcFolder(false);
    }

    private String readManifestContent(AFile manifestFile) throws IOException {
        if (!manifestFile.exists()) {
            return "";
        }
        Path path = Paths.get(manifestFile.getLocation().toFile().toURI());
        return Files.readString(path, StandardCharsets.UTF_8);
    }

}
