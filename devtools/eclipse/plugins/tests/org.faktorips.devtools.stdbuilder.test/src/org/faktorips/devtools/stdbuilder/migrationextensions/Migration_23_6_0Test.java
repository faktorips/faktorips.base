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
import static org.faktorips.testsupport.IpsMatchers.containsErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.containsText;
import static org.faktorips.testsupport.IpsMatchers.hasMessageThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
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
                    containsString("<Property name=\"generateJaxbSupport\" value=\"None\"/>"));

            String manifestContent = Files.readString(manifest.getLocation().toFile().toPath(), StandardCharsets.UTF_8);
            manifestContent = manifestContent.replace("\r\n", "\n");
            manifestContent = manifestContent.replace("\n ", "");
            assertThat(manifestContent, containsString("Manifest-Version: 1.0"));
            assertThat(manifestContent,
                    containsString("Fips-GeneratorConfig: org.faktorips.devtools.stdbuilder.ipsstdbuilderset"));
            assertThat(manifestContent, containsString("generateJaxbSupport=\"None\""));

            String classpath = Files.readString(project.getFile(".classpath").getLocation().toFile().toPath(),
                    StandardCharsets.UTF_8);
            assertThat(classpath, containsString(
                    "<classpathentry kind=\"con\" path=\"org.faktorips.devtools.model.eclipse.ipsClasspathContainer/org.faktorips.valuetypes.joda\"/>"));
        } catch (NullPointerException npe) {
            npe.printStackTrace(System.err);
            assertEquals("", Arrays.stream(npe.getStackTrace()).map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
        }
    }

    @Test
    public void testMigrate_PreselectDefault() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_23_6_0Test");
        IProject project = ipsProject.getProject().unwrap();
        project.getFile(".ipsproject").delete(true, null);
        project.getFolder("model").create(true, true, null);
        copy(project, ".project");
        copy(project, ".classpath");
        copy(project, ".ipsproject");
        java.nio.file.Path ipsProjectFilePath = project.getFile(".ipsproject").getLocation().toFile().toPath();
        replace(ipsProjectFilePath,
                "<Property name=\"generateJaxbSupport\" value=\"false\"/>",
                "<Property name=\"generateJaxbSupport\" value=\"true\"/>");
        IFolder metaInf = project.getFolder("META-INF");
        metaInf.create(true, true, null);
        IFile manifest = copy(metaInf, "MANIFEST.MF");
        ipsProject = IIpsModel.get().getIpsProject(wrap(project).as(AProject.class));

        Migration_23_6_0 migration_23_6_0 = new Migration_23_6_0(ipsProject, "irrelevant");

        try {
            MessageList messageList = migration_23_6_0.migrate(new NullProgressMonitor());

            assertTrue(messageList.isEmpty());

            assertThat(Files.readString(ipsProjectFilePath, StandardCharsets.UTF_8),
                    containsString("<Property name=\"generateJaxbSupport\" value=\"ClassicJAXB\"/>"));

            String manifestContent = Files.readString(manifest.getLocation().toFile().toPath(), StandardCharsets.UTF_8);
            manifestContent = manifestContent.replace("\r\n", "\n");
            manifestContent = manifestContent.replace("\n ", "");
            assertThat(manifestContent, containsString("Manifest-Version: 1.0"));
            assertThat(manifestContent,
                    containsString("Fips-GeneratorConfig: org.faktorips.devtools.stdbuilder.ipsstdbuilderset"));
            assertThat(manifestContent, containsString("generateJaxbSupport=\"ClassicJAXB\""));

            String classpath = Files.readString(project.getFile(".classpath").getLocation().toFile().toPath(),
                    StandardCharsets.UTF_8);
            assertThat(classpath, containsString(
                    "<classpathentry kind=\"con\" path=\"org.faktorips.devtools.model.eclipse.ipsClasspathContainer/org.faktorips.valuetypes.joda,org.faktorips.runtime.xml.javax\"/>"));
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
                .setSelectedValue(JaxbSupportVariant.JakartaXmlBinding);

        try {
            MessageList messageList = migration_23_6_0.migrate(new NullProgressMonitor());

            assertTrue(messageList.isEmpty());

            String ipsProjectFile = Files.readString(project.getFile(".ipsproject").getLocation().toFile().toPath(),
                    StandardCharsets.UTF_8);
            assertThat(ipsProjectFile,
                    containsString("<Property name=\"generateJaxbSupport\" value=\"JakartaXmlBinding\"/>"));

            String manifestContent = Files.readString(manifest.getLocation().toFile().toPath(), StandardCharsets.UTF_8);
            manifestContent = manifestContent.replace("\r\n", "\n");
            manifestContent = manifestContent.replace("\n ", "");
            assertThat(manifestContent, containsString("Manifest-Version: 1.0"));
            assertThat(manifestContent,
                    containsString("Fips-GeneratorConfig: org.faktorips.devtools.stdbuilder.ipsstdbuilderset"));
            assertThat(manifestContent, containsString("generateJaxbSupport=\"JakartaXmlBinding\""));

            String classpath = Files.readString(project.getFile(".classpath").getLocation().toFile().toPath(),
                    StandardCharsets.UTF_8);
            assertThat(classpath, containsString(
                    "<classpathentry kind=\"con\" path=\"org.faktorips.devtools.model.eclipse.ipsClasspathContainer/org.faktorips.valuetypes.joda,org.faktorips.runtime.xml.jakarta\"/>"));
        } catch (NullPointerException npe) {
            npe.printStackTrace(System.err);
            assertEquals("", Arrays.stream(npe.getStackTrace()).map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMigrate_AgainWithChangedSelection() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_23_6_0Test");
        IProject project = ipsProject.getProject().unwrap();
        project.getFile(".ipsproject").delete(true, null);
        project.getFolder("model").create(true, true, null);
        copy(project, ".project");
        copy(project, ".classpath");
        java.nio.file.Path classpathFile = project.getFile(".classpath").getLocation().toFile().toPath();
        replace(classpathFile,
                "<classpathentry kind=\"con\" path=\"org.faktorips.devtools.model.eclipse.ipsClasspathContainer/org.faktorips.valuetypes.joda\"/>",
                "<classpathentry kind=\"con\" path=\"org.faktorips.devtools.model.eclipse.ipsClasspathContainer/org.faktorips.valuetypes.joda,org.faktorips.runtime.xml.javax\"/>");
        copy(project, ".ipsproject");
        java.nio.file.Path ipsProjectFilePath = project.getFile(".ipsproject").getLocation().toFile().toPath();
        replace(ipsProjectFilePath,
                "<Property name=\"generateJaxbSupport\" value=\"false\"/>",
                "<Property name=\"generateJaxbSupport\" value=\"ClassicJAXB\"/>");
        IFolder metaInf = project.getFolder("META-INF");
        metaInf.create(true, true, null);
        IFile manifest = copy(metaInf, "MANIFEST.MF");
        ipsProject = IIpsModel.get().getIpsProject(wrap(project).as(AProject.class));

        Migration_23_6_0 migration_23_6_0 = new Migration_23_6_0(ipsProject, "irrelevant");
        ((IpsMigrationOption<JaxbSupportVariant>)migration_23_6_0.getOptions().iterator().next())
                .setSelectedValue(JaxbSupportVariant.JakartaXmlBinding);

        try {
            MessageList messageList = migration_23_6_0.migrate(new NullProgressMonitor());

            assertTrue(messageList.isEmpty());

            assertThat(Files.readString(ipsProjectFilePath, StandardCharsets.UTF_8),
                    containsString("<Property name=\"generateJaxbSupport\" value=\"JakartaXmlBinding\"/>"));

            String manifestContent = Files.readString(manifest.getLocation().toFile().toPath(), StandardCharsets.UTF_8);
            manifestContent = manifestContent.replace("\r\n", "\n");
            manifestContent = manifestContent.replace("\n ", "");
            assertThat(manifestContent, containsString("Manifest-Version: 1.0"));
            assertThat(manifestContent,
                    containsString("Fips-GeneratorConfig: org.faktorips.devtools.stdbuilder.ipsstdbuilderset"));
            assertThat(manifestContent, containsString("generateJaxbSupport=\"JakartaXmlBinding\""));

            assertThat(Files.readString(classpathFile, StandardCharsets.UTF_8), containsString(
                    "<classpathentry kind=\"con\" path=\"org.faktorips.devtools.model.eclipse.ipsClasspathContainer/org.faktorips.valuetypes.joda,org.faktorips.runtime.xml.jakarta\"/>"));
        } catch (NullPointerException npe) {
            npe.printStackTrace(System.err);
            assertEquals("", Arrays.stream(npe.getStackTrace()).map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
        }
    }

    private String replace(java.nio.file.Path file, String original, String replacement) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        content = content.replace(
                original,
                replacement);
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return content;
    }

    @Test
    public void testMigrate_TooOld() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_23_6_0Test");
        IProject project = ipsProject.getProject().unwrap();
        project.getFile(".ipsproject").delete(true, null);
        project.getFolder("model").create(true, true, null);
        copy(project, ".project");
        copy(project, ".classpath");
        copy(project, ".ipsproject");
        java.nio.file.Path ipsProjectFilePath = project.getFile(".ipsproject").getLocation().toFile().toPath();
        replace(ipsProjectFilePath,
                "<RequiredIpsFeature id=\"org.faktorips.feature\" minVersion=\"22.12.0\"/>",
                "<RequiredIpsFeature id=\"org.faktorips.feature\" minVersion=\"20.12.0\"/>");
        IFolder metaInf = project.getFolder("META-INF");
        metaInf.create(true, true, null);
        copy(metaInf, "MANIFEST.MF");
        ipsProject = IIpsModel.get().getIpsProject(wrap(project).as(AProject.class));

        Migration_23_6_0 migration_23_6_0 = new Migration_23_6_0(ipsProject, "irrelevant");

        try {
            MessageList messageList = migration_23_6_0.canMigrate();

            assertThat(messageList, containsErrorMessage());
            assertThat(messageList, hasMessageThat(containsText("20.12.0")));
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
