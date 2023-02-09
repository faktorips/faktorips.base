/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.migrationextensions;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.core.internal.migrationextensions.Migration_23_6_0;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.options.IpsMigrationOption;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

public class Migration_23_6_0Test extends AbstractIpsPluginTest {

    @Test
    public void testMigrate() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_23_6_0Test");
        IProject project = ipsProject.getProject().unwrap();
        project.getFile(".ipsproject").delete(true, null);
        project.getFolder("model").create(true, true, null);
        copy(project, ".project");
        copy(project, ".classpath");
        copy(project, ".ipsproject");
        IFolder metaInf = project.getFolder("META-INF");
        metaInf.create(true, true, null);
        IFile manifest = copy(metaInf, "MANIFEST.MF");
        ipsProject = IIpsModel.get().getIpsProject(wrap(project).as(AProject.class));

        Migration_23_6_0 migration_23_6_0 = new Migration_23_6_0(ipsProject, "irrelevant");

        try {
            MessageList messageList = migration_23_6_0.migrate(new NullProgressMonitor());

            assertTrue(messageList.isEmpty());

            String ipsProjectFile = Files.readString(project.getFile(".ipsproject").getLocation().toFile().toPath(),
                    StandardCharsets.UTF_8);
            assertThat(ipsProjectFile,
                    containsString("<Property name=\"generateJaxbSupport\" value=\"ClassicJAXB\"/>"));

            String manifestContent = Files.readString(manifest.getLocation().toFile().toPath(), StandardCharsets.UTF_8);
            manifestContent = manifestContent.replace("\r\n", "\n");
            manifestContent = manifestContent.replace("\n ", "");
            assertThat(manifestContent, containsString("Manifest-Version: 1.0"));
            assertThat(manifestContent,
                    containsString("Fips-GeneratorConfig: org.faktorips.devtools.stdbuilder.ipsstdbuilderset"));
            assertThat(manifestContent, containsString("generateJaxbSupport=\"ClassicJAXB\""));
        } catch (NullPointerException npe) {
            npe.printStackTrace(System.err);
            assertEquals("", Arrays.stream(npe.getStackTrace()).map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMigrate_ChangeToJakarta() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_23_6_0Test");
        IProject project = ipsProject.getProject().unwrap();
        project.getFile(".ipsproject").delete(true, null);
        project.getFolder("model").create(true, true, null);
        copy(project, ".project");
        copy(project, ".classpath");
        copy(project, ".ipsproject");
        IFolder metaInf = project.getFolder("META-INF");
        metaInf.create(true, true, null);
        IFile manifest = copy(metaInf, "MANIFEST.MF");
        ipsProject = IIpsModel.get().getIpsProject(wrap(project).as(AProject.class));

        Migration_23_6_0 migration_23_6_0 = new Migration_23_6_0(ipsProject, "irrelevant");
        ((IpsMigrationOption<JaxbSupportVariant>)migration_23_6_0.getOptions().iterator().next())
                .setSelectedValue(JaxbSupportVariant.JakartaXmlBinding3);

        try {
            MessageList messageList = migration_23_6_0.migrate(new NullProgressMonitor());

            assertTrue(messageList.isEmpty());

            String ipsProjectFile = Files.readString(project.getFile(".ipsproject").getLocation().toFile().toPath(),
                    StandardCharsets.UTF_8);
            assertThat(ipsProjectFile,
                    containsString("<Property name=\"generateJaxbSupport\" value=\"JakartaXmlBinding3\"/>"));

            String manifestContent = Files.readString(manifest.getLocation().toFile().toPath(), StandardCharsets.UTF_8);
            manifestContent = manifestContent.replace("\r\n", "\n");
            manifestContent = manifestContent.replace("\n ", "");
            assertThat(manifestContent, containsString("Manifest-Version: 1.0"));
            assertThat(manifestContent,
                    containsString("Fips-GeneratorConfig: org.faktorips.devtools.stdbuilder.ipsstdbuilderset"));
            assertThat(manifestContent, containsString("generateJaxbSupport=\"JakartaXmlBinding3\""));
        } catch (NullPointerException npe) {
            npe.printStackTrace(System.err);
            assertEquals("", Arrays.stream(npe.getStackTrace()).map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
        }
    }

    private IFile copy(IContainer container, String fileName) throws CoreException {
        return copy(container, fileName, fileName);
    }

    private IFile copy(IContainer container, String inputFileName, String outputFileName) throws CoreException {
        IFile file = container.getFile(new Path(outputFileName));
        InputStream inputStream = getClass().getResourceAsStream(getClass().getSimpleName() + inputFileName);
        if (file.exists()) {
            file.setContents(inputStream, true, true, null);
        } else {
            file.create(inputStream, true, null);
        }
        return file;
    }

}
