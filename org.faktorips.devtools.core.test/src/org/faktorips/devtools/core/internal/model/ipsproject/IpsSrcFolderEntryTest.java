/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFolderEntryTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsObjectPath path;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = new IpsObjectPath(ipsProject);
    }

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

        entry.initFromXml((Element)nl.item(1), ipsProject.getProject());
        assertNull(entry.getSpecificOutputFolderForMergableJavaFiles());
        assertEquals("", entry.getSpecificBasePackageNameForMergableJavaClasses());
        assertEquals("", entry.getSpecificBasePackageNameForDerivedJavaClasses());
    }

    public void testToXml() {
        IProject project = ipsProject.getProject();
        IpsSrcFolderEntry entry = new IpsSrcFolderEntry(path, project.getFolder("ipssrc").getFolder("modelclasses"));
        entry.setSpecificOutputFolderForMergableJavaFiles(project.getFolder("javasrc").getFolder("modelclasses"));
        entry.setSpecificBasePackageNameForMergableJavaClasses("org.faktorips.sample.model");
        entry.setBasePackageRelativeTocPath("toc.xml");
        Element element = entry.toXml(newDocument());
        entry = new IpsSrcFolderEntry(path);
        entry.initFromXml(element, project);
        assertEquals("toc.xml", entry.getBasePackageRelativeTocPath());
        assertEquals(project.getFolder("ipssrc").getFolder("modelclasses"), entry.getSourceFolder());
        assertEquals(project.getFolder("javasrc").getFolder("modelclasses"), entry
                .getSpecificOutputFolderForMergableJavaFiles());
        assertEquals("org.faktorips.sample.model", entry.getSpecificBasePackageNameForMergableJavaClasses());

        // null, default values for new entries
        entry = new IpsSrcFolderEntry(path, project.getFolder("ipssrc").getFolder("modelclasses"));
        element = entry.toXml(newDocument());
        entry = new IpsSrcFolderEntry(path);
        entry.initFromXml(element, project);
        assertNull(entry.getSpecificOutputFolderForMergableJavaFiles());
        assertNull(entry.getSpecificOutputFolderForDerivedJavaFiles());
        assertEquals("", entry.getSpecificBasePackageNameForMergableJavaClasses());
        assertEquals("", entry.getSpecificBasePackageNameForDerivedJavaClasses());
    }

    public void testValidate() throws CoreException {
        MessageList ml = ipsProject.validate();
        assertEquals(0, ml.getNoOfMessages());

        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();
        IIpsSrcFolderEntry[] srcEntries = path.getSourceFolderEntries();
        assertEquals(1, srcEntries.length);

        srcEntries[0].setSpecificOutputFolderForMergableJavaFiles(null);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING));

        // validate missing outputFolderGenerated
        IFolder folder1 = ipsProject.getProject().getFolder("none");
        srcEntries[0].setSpecificOutputFolderForMergableJavaFiles(folder1);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_DOESNT_EXIST));

        // validate missing outputFolderDerived
        srcEntries[0].setSpecificOutputFolderForDerivedJavaFiles(folder1);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(2, ml.getNoOfMessages());

        // validate missing source folder
        path.newSourceFolderEntry(folder1);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(5, ml.getNoOfMessages());
    }

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

    private String getFileContent(String rootName, String fileName) throws CoreException, IOException {
        InputStream aStream = ipsProject.getIpsObjectPath().getEntry(rootName).getRessourceAsStream(fileName);
        return getFileContent(aStream);
    }
}
