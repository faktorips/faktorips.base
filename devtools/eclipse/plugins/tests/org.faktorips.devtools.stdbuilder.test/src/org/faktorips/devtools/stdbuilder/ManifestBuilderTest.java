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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.junit.Before;
import org.junit.Test;

public class ManifestBuilderTest extends AbstractStdBuilderTest {

    private ManifestBuilder manifestBuilder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        manifestBuilder = builderSet.getBuilderById(BuilderKindIds.MANIFEST_FILE, ManifestBuilder.class);
        createManifestFileInTestProject();
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
        assertThat(initialContent, not(containsString("Fips-GeneratorConfig")));
        assertThat(initialContent, not(containsString("Fips-RuntimeIdPrefix")));
        assertThat(initialContent, not(containsString("org.faktorips.devtools.stdbuilder.ipsstdbuilderset")));
        assertThat(initialContent, not(containsString("generateJaxbSupport")));
        assertThat(initialContent, not(containsString("generatePublishedInterfaces")));

        ipsProject.getProject().build(ABuildKind.FULL, null);
        String afterBuild = readManifestContent(aManifestFile);

        assertThat(afterBuild, containsString("Fips-GeneratorConfig"));
        assertThat(afterBuild, containsString("Fips-RuntimeIdPrefix"));
        assertThat(afterBuild, containsString("org.faktorips.devtools.stdbuilder.ipsstdbuilderset"));
        assertThat(afterBuild, containsString("generateJaxbSupport"));
        assertThat(afterBuild, containsString("generatePublishedInterfaces"));
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

    private String readManifestContent(AFile manifestFile) throws IOException {
        if (!manifestFile.exists()) {
            return "";
        }
        Path path = Paths.get(manifestFile.getLocation().toFile().toURI());
        return Files.readString(path, StandardCharsets.UTF_8);
    }

}
