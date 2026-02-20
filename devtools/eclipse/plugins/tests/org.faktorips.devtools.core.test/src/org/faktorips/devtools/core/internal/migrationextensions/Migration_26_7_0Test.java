/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.model.internal.ipsproject.IpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class Migration_26_7_0Test extends AbstractIpsPluginTest {

    private IIpsProject project;

    @Before
    public void setup() {
        project = newIpsProject("Migration_26_7_0Test");

        // Add German and English as supported languages
        IIpsProjectProperties properties = project.getProperties();
        properties.addSupportedLanguage(Locale.GERMAN);
        properties.addSupportedLanguage(Locale.ENGLISH);
        project.setProperties(properties);

        AFolder resources = project.getProject().getFolder(Path.of("resources"));
        resources.create(null);

        AFolder src = project.getProject().getFolder(Path.of("src"));

        AFolder model = project.getProject().getFolder(Path.of("model"));
        model.create(null);

        AFolder otherResources = project.getProject().getFolder(Path.of("resources2"));
        otherResources.create(null);

        createTestPropertiesFiles(resources);
        createTestPropertiesFiles(otherResources);

        IpsObjectPath ipsObjectPath = new IpsObjectPath(project);
        ipsObjectPath.setOutputFolderForMergableSources(src);
        ipsObjectPath.setOutputFolderForDerivedSources(resources);
        ipsObjectPath.setOutputDefinedPerSrcFolder(true);

        IpsSrcFolderEntry srcFolderEntry = new IpsSrcFolderEntry(ipsObjectPath, model);
        srcFolderEntry.setSpecificOutputFolderForDerivedJavaFiles(otherResources);

        ipsObjectPath.setEntries(List.of(srcFolderEntry).toArray(new IpsObjectPathEntry[0]));
        project.setIpsObjectPath(ipsObjectPath);
    }

    @Test
    public void testMigrate_DeletesDerivedPropertiesFiles() throws Exception {
        AFolder model = project.getProject().getFolder(Path.of("model"));
        createIpsFile(model, "TestEnum.ipsenumtype", IpsObjectType.ENUM_TYPE);
        Migration_26_7_0 migration = new Migration_26_7_0(project, "");

        migration.migrate(new NullProgressMonitor());

        // Verify that .properties files for enums are deleted
        assertThat(project.getProject().findMember("resources/package/TestEnum_de.properties"), is(nullValue()));
        assertThat(project.getProject().findMember("resources/package/TestEnum_en.properties"), is(nullValue()));

        // Verify that validation messages are deleted
        assertThat(project.getProject().findMember("resources/validation-messages_de.properties"), is(nullValue()));
        assertThat(project.getProject().findMember("resources/validation-messages_en.properties"), is(nullValue()));

        // Verify that label and description files are deleted
        assertThat(project.getProject().findMember("resources/model-label-and-descriptions_de.properties"),
                is(nullValue()));
        assertThat(project.getProject().findMember("resources/model-label-and-descriptions_en.properties"),
                is(nullValue()));

        // Verify that non-matching .properties files are NOT deleted
        assertThat(project.getProject().findMember("resources/package/SomeOther.properties"), isA(AFile.class));
        assertThat(project.getProject().findMember("resources2/package/SomeOther.properties"), isA(AFile.class));

        // Verify that non-.properties files are NOT deleted
        assertThat(project.getProject().findMember("resources/package/SomeFile.java"), isA(AFile.class));
        assertThat(project.getProject().findMember("resources2/package/SomeFile.java"), isA(AFile.class));
        assertThat(project.getProject().findMember("resources/.gitignore"), isA(AFile.class));
        assertThat(project.getProject().findMember("resources2/.gitignore"), isA(AFile.class));
    }

    @Test
    public void testMigrate_OnlyInSrcEntries() throws Exception {
        // Set output folder for derived sources to null, so only src folder entries should be
        // processed
        IpsObjectPath ipsObjectPath = (IpsObjectPath)project.getIpsObjectPath();
        ipsObjectPath.setOutputFolderForDerivedSources(null);
        project.setIpsObjectPath(ipsObjectPath);
        AFolder modelFolder = project.getProject().getFolder(Path.of("model"));
        createIpsFile(modelFolder, "TestEnum.ipsenumtype", IpsObjectType.ENUM_TYPE);
        Migration_26_7_0 migration = new Migration_26_7_0(project, "");

        migration.migrate(new NullProgressMonitor());

        // Verify that files in the global derived folder are NOT deleted
        assertThat(project.getProject().findMember("resources/package/TestEnum_de.properties"), isA(AFile.class));
        assertThat(project.getProject().findMember("resources/package/TestEnum_en.properties"), isA(AFile.class));

        // Verify that files in the src folder entry's output folder ARE deleted
        assertThat(project.getProject().findMember("resources2/package/TestEnum_de.properties"), is(nullValue()));
        assertThat(project.getProject().findMember("resources2/package/TestEnum_en.properties"), is(nullValue()));
    }

    @Test
    public void testMigrate_WithEnumContentAndType() throws Exception {
        // Create test IPS files with enum content and type
        AFolder modelFolder = project.getProject().getFolder(Path.of("model"));
        createIpsFile(modelFolder, "MyEnumContent.ipsenumcontent", IpsObjectType.ENUM_CONTENT);
        createIpsFile(modelFolder, "MyEnumType.ipsenumtype", IpsObjectType.ENUM_TYPE);
        AFolder resources = project.getProject().getFolder(Path.of("resources"));
        // Create properties files for these enums
        createPropertiesFile(resources, "MyEnumContent_de.properties");
        createPropertiesFile(resources, "MyEnumContent_en.properties");
        createPropertiesFile(resources, "MyEnumType_de.properties");
        createPropertiesFile(resources, "MyEnumType_en.properties");
        Migration_26_7_0 migration = new Migration_26_7_0(project, "");

        migration.migrate(new NullProgressMonitor());

        // Verify that enum properties files are deleted
        assertThat(project.getProject().findMember("resources/MyEnumContent_de.properties"), is(nullValue()));
        assertThat(project.getProject().findMember("resources/MyEnumContent_en.properties"), is(nullValue()));
        assertThat(project.getProject().findMember("resources/MyEnumType_de.properties"), is(nullValue()));
        assertThat(project.getProject().findMember("resources/MyEnumType_en.properties"), is(nullValue()));
    }

    @Test
    public void testCanMigrate() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_26_7_0Test_CanMigrate");
        IProject eclipseProject = ipsProject.getProject().unwrap();
        eclipseProject.getFile(".ipsproject").delete(true, null);
        eclipseProject.getFolder("model").create(true, true, null);
        copy(eclipseProject, ".project");
        copy(eclipseProject, ".classpath");
        copy(eclipseProject, ".ipsproject");
        IFolder metaInf = eclipseProject.getFolder("META-INF");
        metaInf.create(true, true, null);
        copy(metaInf, "MANIFEST.MF");
        ipsProject = IIpsModel.get().getIpsProject(wrap(eclipseProject).as(AProject.class));
        Migration_26_7_0 migration = new Migration_26_7_0(ipsProject, "irrelevant");

        MessageList messageList = migration.canMigrate();

        assertThat(messageList.containsErrorMsg(), is(false));
        assertThat(migration.getTargetVersion().contains("26.7"), is(true));
    }

    @Test
    public void testGetTargetVersion() {
        Migration_26_7_0 migration = new Migration_26_7_0(project, "");

        assertThat(migration.getTargetVersion().contains("26.7"), is(true));
    }

    @Test
    public void testGetDescription() {
        Migration_26_7_0 migration = new Migration_26_7_0(project, "");

        assertThat(migration.getDescription(), is(Messages.Migration_26_7_0_description));
    }

    private void createTestPropertiesFiles(AFolder parent) {
        AFolder packageFolder = createFolder(parent, "package");

        // Create enum-related properties files (will be deleted by migration)
        createPropertiesFile(packageFolder, "TestEnum_de.properties");
        createPropertiesFile(packageFolder, "TestEnum_en.properties");

        // Create validation message properties files (will be deleted by migration)
        createPropertiesFile(parent, "validation-messages_de.properties");
        createPropertiesFile(parent, "validation-messages_en.properties");

        // Create label and description properties files (will be deleted by migration)
        createPropertiesFile(parent, "model-label-and-descriptions_de.properties");
        createPropertiesFile(parent, "model-label-and-descriptions_en.properties");

        // Create non-matching properties files (should NOT be deleted)
        createPropertiesFile(packageFolder, "SomeOther.properties");

        // Create non-properties files (should NOT be deleted)
        createFile(packageFolder, "SomeFile.java");
        createFile(parent, ".gitignore");
    }

    private AFile createFile(AFolder parent, String name) {
        AFile file = parent.getFile(Path.of(name));
        file.create(new ByteArrayInputStream("test content".getBytes()), null);
        return file;
    }

    private AFile createPropertiesFile(AFolder parent, String name) {
        AFile file = parent.getFile(Path.of(name));
        String content = "# Test properties file" + System.lineSeparator() + "key=value" + System.lineSeparator();
        file.create(new ByteArrayInputStream(content.getBytes()), null);
        return file;
    }

    private AFolder createFolder(AFolder parentFolder, String name) {
        AFolder folder = parentFolder.getFolder(name);
        folder.create(null);
        return folder;
    }

    private AFile createIpsFile(AFolder parent, String name, IpsObjectType ipsObjectType) {
        AFile file = parent.getFile(Path.of(name));
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() + "<"
                + ipsObjectType.getXmlElementName() + "/>";
        file.create(new ByteArrayInputStream(content.getBytes()), null);
        return file;
    }

    private IFile copy(IContainer container, String fileName) throws CoreException {
        return copy(container, fileName, fileName);
    }

    private IFile copy(IContainer container, String inputFileName, String outputFileName) throws CoreException {
        IFile file = container.getFile(new org.eclipse.core.runtime.Path(outputFileName));
        InputStream inputStream = getClass().getResourceAsStream(getClass().getSimpleName() + inputFileName);
        if (file.exists()) {
            file.setContents(inputStream, true, true, null);
        } else {
            file.create(inputStream, true, null);
        }
        return file;
    }
}
