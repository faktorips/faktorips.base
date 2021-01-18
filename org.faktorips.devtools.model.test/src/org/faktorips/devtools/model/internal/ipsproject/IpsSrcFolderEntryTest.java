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

import static org.faktorips.abstracttest.matcher.Matchers.hasMessageCode;
import static org.faktorips.abstracttest.matcher.Matchers.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFolderEntryTest extends AbstractIpsPluginTest {

    private static final String MY_RESOURCE_PATH = "myResourcePath";
    private IIpsProject ipsProject;
    private IpsObjectPath path;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        path.setOutputDefinedPerSrcFolder(true);
    }

    @Test
    public void testGetOutputFolderForGeneratedJavaFiles() {
        IFolder src = ipsProject.getProject().getFolder("src");
        IFolder out1 = ipsProject.getProject().getFolder("out1");
        IFolder out2 = ipsProject.getProject().getFolder("out2");
        path.setOutputFolderForMergableSources(out1);
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(src);
        entry.setSpecificOutputFolderForMergableJavaFiles(out2);

        path.setOutputDefinedPerSrcFolder(false);
        assertEquals(out1, entry.getOutputFolderForMergableJavaFiles());

        path.setOutputDefinedPerSrcFolder(true);
        assertEquals(out2, entry.getOutputFolderForMergableJavaFiles());
    }

    @Test
    public void testGetBasePackageNameForGeneratedJavaClasses() {
        IFolder src = ipsProject.getProject().getFolder("src");
        path.setBasePackageNameForMergableJavaClasses("pack1");
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(src);
        entry.setSpecificBasePackageNameForMergableJavaClasses("pack2");

        path.setOutputDefinedPerSrcFolder(false);
        assertEquals("pack1", entry.getBasePackageNameForMergableJavaClasses());

        path.setOutputDefinedPerSrcFolder(true);
        assertEquals("pack2", entry.getBasePackageNameForMergableJavaClasses());
    }

    @Test
    public void testGetOutputFolderForExtensionJavaFiles() {
        IFolder src = ipsProject.getProject().getFolder("src");
        IFolder out1 = ipsProject.getProject().getFolder("out1");
        IFolder out2 = ipsProject.getProject().getFolder("out2");
        path.setOutputFolderForDerivedSources(out1);
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(src);
        entry.setSpecificOutputFolderForDerivedJavaFiles(out2);

        path.setOutputDefinedPerSrcFolder(false);
        assertEquals(out1, entry.getOutputFolderForDerivedJavaFiles());

        path.setOutputDefinedPerSrcFolder(true);
        assertEquals(out2, entry.getOutputFolderForDerivedJavaFiles());
    }

    @Test
    public void testGetBasePackageNameForExtensionJavaClasses() {
        IFolder src = ipsProject.getProject().getFolder("src");
        path.setBasePackageNameForDerivedJavaClasses("pack1");
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(src);
        entry.setSpecificBasePackageNameForDerivedJavaClasses("pack2");

        path.setOutputDefinedPerSrcFolder(false);
        assertEquals("pack1", entry.getBasePackageNameForDerivedJavaClasses());

        path.setOutputDefinedPerSrcFolder(true);
        assertEquals("pack2", entry.getBasePackageNameForDerivedJavaClasses());
    }

    @Test
    public void testInitFromXml() {
        IProject project = ipsProject.getProject();
        IpsSrcFolderEntry entry = new IpsSrcFolderEntry(path);
        Document doc = getTestDocument();
        NodeList nl = doc.getDocumentElement().getElementsByTagName("Entry");

        entry.initFromXml((Element)nl.item(0), ipsProject.getProject());
        assertEquals(project.getFolder("ipssrc"), entry.getSourceFolder());
        assertEquals(project.getFolder("generated"), entry.getSpecificOutputFolderForMergableJavaFiles());
        assertEquals("org.sample.generated", entry.getSpecificBasePackageNameForMergableJavaClasses());
        assertEquals(project.getFolder("extensions"), entry.getSpecificOutputFolderForDerivedJavaFiles());
        assertEquals("org.sample.extensions", entry.getSpecificBasePackageNameForDerivedJavaClasses());
        assertEquals("motor.repository-toc.xml", entry.getBasePackageRelativeTocPath());
        assertEquals("org.sample.generated.abc", entry.getUniqueBasePackageNameForMergableArtifacts());
        assertEquals("org.sample.extensions.abc", entry.getUniqueBasePackageNameForDerivedArtifacts());

        entry.initFromXml((Element)nl.item(1), ipsProject.getProject());
        assertNull(entry.getSpecificOutputFolderForMergableJavaFiles());
        assertEquals("", entry.getSpecificBasePackageNameForMergableJavaClasses());
        assertEquals("", entry.getSpecificBasePackageNameForDerivedJavaClasses());
        assertEquals("", entry.getUniqueBasePackageNameForMergableArtifacts());
        assertEquals("", entry.getUniqueBasePackageNameForDerivedArtifacts());
    }

    @Test
    public void testToXml() {
        IProject project = ipsProject.getProject();
        IpsSrcFolderEntry entry = new IpsSrcFolderEntry(path, project.getFolder("ipssrc").getFolder("modelclasses"));
        entry.setSpecificOutputFolderForMergableJavaFiles(project.getFolder("javasrc").getFolder("modelclasses"));
        entry.setSpecificBasePackageNameForMergableJavaClasses("org.faktorips.sample.model");
        entry.setSpecificBasePackageNameForDerivedJavaClasses("org.faktorips.sample.model.derived");
        entry.setBasePackageRelativeTocPath("toc.xml");
        entry.setUniqueQualifier("unique");
        Element element = entry.toXml(newDocument());
        entry = new IpsSrcFolderEntry(path);
        entry.initFromXml(element, project);
        assertEquals("toc.xml", entry.getBasePackageRelativeTocPath());
        assertEquals(project.getFolder("ipssrc").getFolder("modelclasses"), entry.getSourceFolder());
        assertEquals(project.getFolder("javasrc").getFolder("modelclasses"),
                entry.getSpecificOutputFolderForMergableJavaFiles());
        assertEquals("org.faktorips.sample.model", entry.getSpecificBasePackageNameForMergableJavaClasses());
        assertEquals("org.faktorips.sample.model.unique", entry.getUniqueBasePackageNameForMergableArtifacts());
        assertEquals("org.faktorips.sample.model.derived", entry.getSpecificBasePackageNameForDerivedJavaClasses());
        assertEquals("org.faktorips.sample.model.derived.unique", entry.getUniqueBasePackageNameForDerivedArtifacts());

        // null, default values for new entries
        entry = new IpsSrcFolderEntry(path, project.getFolder("ipssrc").getFolder("modelclasses"));
        element = entry.toXml(newDocument());
        entry = new IpsSrcFolderEntry(path);
        entry.initFromXml(element, project);
        assertNull(entry.getSpecificOutputFolderForMergableJavaFiles());
        assertNull(entry.getSpecificOutputFolderForDerivedJavaFiles());
        assertEquals("", entry.getSpecificBasePackageNameForMergableJavaClasses());
        assertEquals("", entry.getUniqueBasePackageNameForMergableArtifacts());
        assertEquals("", entry.getSpecificBasePackageNameForDerivedJavaClasses());
        assertEquals("", entry.getUniqueBasePackageNameForDerivedArtifacts());
    }

    @Test
    public void testValidate() throws CoreException {
        MessageList ml = ipsProject.validate();
        assertEquals(0, ml.size());

        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();
        IIpsSrcFolderEntry[] srcEntries = path.getSourceFolderEntries();
        assertEquals(1, srcEntries.length);

        srcEntries[0].setSpecificOutputFolderForMergableJavaFiles(null);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(1, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING));

        // validate missing outputFolderGenerated
        IFolder folder1 = ipsProject.getProject().getFolder("none");
        srcEntries[0].setSpecificOutputFolderForMergableJavaFiles(folder1);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(1, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_DOESNT_EXIST));

        // validate missing outputFolderDerived
        srcEntries[0].setSpecificOutputFolderForDerivedJavaFiles(folder1);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(2, ml.size());

        // validate missing source folder
        path.newSourceFolderEntry(folder1);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(5, ml.size());
    }

    @Test
    public void testValidate_UniqueBasePackage_ValidInSameProject() throws CoreException {
        IIpsSrcFolderEntry entry = setUpPathForUniqueBasePackageTest();

        MessageList ml = path.validate();

        assertThat(ml, isEmpty());

        entry.setSpecificBasePackageNameForMergableJavaClasses(path.getSourceFolderEntries()[0]
                .getBasePackageNameForMergableJavaClasses());
    }

    @Test
    public void testValidate_UniqueBasePackage_SameMergableBasePackageInSameProject() throws CoreException {
        IIpsSrcFolderEntry entry = setUpPathForUniqueBasePackageTest();
        entry.setSpecificBasePackageNameForMergableJavaClasses(path.getSourceFolderEntries()[0]
                .getBasePackageNameForMergableJavaClasses());

        MessageList ml = path.validate();

        assertThat(ml, hasMessageCode(IpsSrcFolderEntry.MSGCODE_DUPLICATE_BASE_PACKAGE));
    }

    @Test
    public void testValidate_UniqueBasePackage_SameDerivedBasePackageInSameProject() throws CoreException {
        IIpsSrcFolderEntry entry = setUpPathForUniqueBasePackageTest();
        entry.setSpecificBasePackageNameForDerivedJavaClasses(path.getSourceFolderEntries()[0]
                .getBasePackageNameForDerivedJavaClasses());

        MessageList ml = path.validate();

        assertThat(ml, hasMessageCode(IpsSrcFolderEntry.MSGCODE_DUPLICATE_BASE_PACKAGE));
    }

    @Test
    public void testValidate_UniqueBasePackage_DefinedInObjectPathInSameProject() throws CoreException {
        path.setOutputDefinedPerSrcFolder(false);
        setUpPathForUniqueBasePackageTest();

        MessageList ml = path.validate();

        assertThat(ml, hasMessageCode(IpsSrcFolderEntry.MSGCODE_DUPLICATE_BASE_PACKAGE));
    }

    @Test
    public void testValidate_UniqueBasePackage_SameInDifferentProjects() throws CoreException {
        IIpsProject ipsProject2 = newIpsProject();
        path.newIpsProjectRefEntry(ipsProject2);

        MessageList ml = path.validate();

        assertThat(ml, hasMessageCode(IpsSrcFolderEntry.MSGCODE_DUPLICATE_BASE_PACKAGE));
    }

    @Test
    public void testValidate_UniqueBasePackage_DiffInDifferentProjects() throws CoreException {
        IIpsProject ipsProject2 = newIpsProject();
        path.newIpsProjectRefEntry(ipsProject2);
        path.getSourceFolderEntries()[0].setSpecificBasePackageNameForMergableJavaClasses("asd");
        path.getSourceFolderEntries()[0].setSpecificBasePackageNameForDerivedJavaClasses("asd");

        MessageList ml = path.validate();

        assertThat(ml, isEmpty());
    }

    protected IIpsSrcFolderEntry setUpPathForUniqueBasePackageTest() throws CoreException {
        IFolder testFolder = ipsProject.getProject().getFolder("test");
        ipsProject.getProject().getFolder("derived").create(true, true, null);
        testFolder.create(false, true, null);
        IIpsSrcFolderEntry folderEntry = path.newSourceFolderEntry(testFolder);
        folderEntry.setSpecificOutputFolderForMergableJavaFiles(ipsProject.getProject().getFolder("src"));
        folderEntry.setSpecificOutputFolderForDerivedJavaFiles(ipsProject.getProject().getFolder("derived"));
        return folderEntry;
    }

    @Test
    public void testGetResourceAsStream() throws CoreException, IOException {
        IFolder projectSubFolder = ipsProject.getProject().getFolder(new Path("subFolder"));
        if (!projectSubFolder.exists()) {
            projectSubFolder.create(true, true, null);
        }
        createFileWithContent(projectSubFolder, "file.txt", "CCC");

        // File not found, as Folder is not an IPS SourceFolder
        assertNull(ipsProject.getIpsObjectPath().getResourceAsStream("subFolder/file.txt"));
        assertNull(ipsProject.getIpsObjectPath().getResourceAsStream("file.txt"));

        // Roots in subfolders (e.g. project/folder/root) cannot be created via the FIPS GUI. Tests
        // with IconPaths will fail in that case.
        IIpsPackageFragmentRoot rootOne = newIpsPackageFragmentRoot(ipsProject, null, "rootOne");
        IIpsPackageFragmentRoot rootTwo = newIpsPackageFragmentRoot(ipsProject, null, "rootTwo");

        createFileWithContent((IFolder)rootOne.getCorrespondingResource(), "file.txt", "111");
        createFileWithContent((IFolder)rootTwo.getCorrespondingResource(), "file.txt", "222");
        assertEquals("111", getFileContent("rootOne", "file.txt"));
        assertEquals("222", getFileContent("rootTwo", "file.txt"));
    }

    private String getFileContent(String rootName, String fileName) throws IOException {
        InputStream aStream = ipsProject.getIpsObjectPath().getEntry(rootName).getResourceAsStream(fileName);
        return getFileContent(aStream);
    }

    @Test
    public void testContainsResource_true() throws Exception {
        IIpsPackageFragmentRoot root = newIpsPackageFragmentRoot(ipsProject, null, "rootOne");
        createFileWithContent((IFolder)root.getCorrespondingResource(), MY_RESOURCE_PATH, "asdfasf");

        assertTrue(root.getIpsObjectPathEntry().containsResource(MY_RESOURCE_PATH));
    }

    @Test
    public void testContainsResource_false() throws Exception {
        IIpsPackageFragmentRoot root = newIpsPackageFragmentRoot(ipsProject, null, "rootOne");

        assertFalse(root.getIpsObjectPathEntry().containsResource(MY_RESOURCE_PATH));
    }

    @Test
    public void testIsReexported() {
        IFolder src = ipsProject.getProject().getFolder("src");
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(src);
        entry.setReexported(false);
        assertTrue(entry.isReexported());
    }
}
