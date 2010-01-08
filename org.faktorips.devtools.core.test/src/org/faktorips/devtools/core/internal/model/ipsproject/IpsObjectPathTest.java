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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPathTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
    }
    
    public void testGetEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        assertNull(path.getEntry(null));
        assertNull(path.getEntry("unknown"));
        
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        IIpsObjectPathEntry entry0 = path.newSourceFolderEntry(srcFolder);
        
        path.newIpsProjectRefEntry(newIpsProject("Project2"));
        
        IFile archiveFile = ipsProject.getProject().getFile("archive.jar");
        IIpsObjectPathEntry entry2 = path.newArchiveEntry(archiveFile.getLocation());
        
        assertEquals(entry0, path.getEntry("src"));
        assertNull(path.getEntry("Project2"));
        assertEquals(entry2, path.getEntry("archive.jar"));
        
        assertNull(path.getEntry("unknwon"));
        assertNull(path.getEntry(null));
    }
    
    public void testNewSrcFolderEntry() throws CoreException {
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsSrcFolderEntry entry0 = path.newSourceFolderEntry(srcFolder);
        assertEquals(path, entry0.getIpsObjectPath());
        assertEquals(2, path.getEntries().length); // default test project contains already 1 entry
        assertEquals(entry0, path.getEntries()[1]);

        IIpsSrcFolderEntry entry1 = path.newSourceFolderEntry(srcFolder);
        assertEquals(path, entry1.getIpsObjectPath());
        assertEquals(3, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);
        assertEquals(entry1, path.getEntries()[2]);
    }

    public void testNewProjectRefEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsProjectRefEntry entry0 = path.newIpsProjectRefEntry(ipsProject);
        assertEquals(path, entry0.getIpsObjectPath());
        assertEquals(2, path.getEntries().length); // default test project contains already 1 entry
        assertEquals(entry0, path.getEntries()[1]);

        IIpsProjectRefEntry entry1 = path.newIpsProjectRefEntry(ipsProject);
        assertEquals(path, entry1.getIpsObjectPath());
        assertEquals(2, path.getEntries().length); // the same project should not be added twice
        assertEquals(entry0, path.getEntries()[1]);
        assertEquals(entry1, path.getEntries()[1]);

        IIpsProjectRefEntry entry2 = path.newIpsProjectRefEntry(this.newIpsProject("TestProject2"));
        assertEquals(path, entry2.getIpsObjectPath());
        assertEquals(3, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);
        assertEquals(entry2, path.getEntries()[2]);
    }

    public void testContainsProjectRefEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        assertTrue(path.containsProjectRefEntry(ipsProject));

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(path.containsProjectRefEntry(ipsProject2));
        
        path.removeProjectRefEntry(ipsProject);
        assertFalse(path.containsProjectRefEntry(ipsProject));
    }

    public void testRemoveProjectRefEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsProjectRefEntry entry0 = path.newIpsProjectRefEntry(ipsProject);
        assertEquals(path, entry0.getIpsObjectPath());
        assertEquals(2, path.getEntries().length); // default test project contains already 1 entry
        assertEquals(entry0, path.getEntries()[1]);
        assertTrue(path.containsProjectRefEntry(ipsProject));
        path.removeProjectRefEntry(ipsProject);
        assertFalse(path.containsProjectRefEntry(ipsProject));
        assertEquals(1, path.getEntries().length);

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(path.containsProjectRefEntry(ipsProject2));
        path.removeProjectRefEntry(ipsProject2);
        assertFalse(path.containsProjectRefEntry(ipsProject2));
        assertEquals(1, path.getEntries().length);
    }
    
    public void testContainsArchiveEntry() throws Exception {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        createArchive(ipsProject, archiveFile);
        IIpsArchive ipsArchive = path.newArchiveEntry(archiveFile.getLocation()).getIpsArchive();
        assertTrue(path.containsArchiveEntry(ipsArchive));

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(ipsProject2.getIpsObjectPath().containsArchiveEntry(ipsArchive));
        
        path.removeArchiveEntry(ipsArchive);
        assertFalse(path.containsArchiveEntry(ipsArchive));
    }

    public void testRemoveArchiveEntry() throws Exception {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        createArchive(ipsProject, archiveFile);
        IIpsArchiveEntry entry0 = path.newArchiveEntry(archiveFile.getLocation());
        IIpsArchive archive0 = entry0.getIpsArchive();
        assertEquals(path, entry0.getIpsObjectPath());
        assertEquals(2, path.getEntries().length); // default test project contains already 1 entry
        assertEquals(entry0, path.getEntries()[1]);
        assertTrue(path.containsArchiveEntry(entry0.getIpsArchive()));
        path.removeArchiveEntry(archive0);
        assertEquals(1, path.getEntries().length);
        path.removeArchiveEntry(archive0);
        assertEquals(1, path.getEntries().length);
    }

    public void testContainsSrcFolderEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IFolder folder = ipsProject.getProject().getFolder("testfolder");
        path.newSourceFolderEntry(folder);
        assertTrue(path.containsSrcFolderEntry(folder));

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(ipsProject2.getIpsObjectPath().containsSrcFolderEntry(folder));
        
        path.removeSrcFolderEntry(folder);
        assertFalse(path.containsSrcFolderEntry(folder));
    }

    public void testRemoveSrcFolderEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IFolder folder = ipsProject.getProject().getFolder("testfolder");
        IIpsSrcFolderEntry entry0 = path.newSourceFolderEntry(folder);
        assertEquals(path, entry0.getIpsObjectPath());
        assertEquals(2, path.getEntries().length); // default test project contains already 1 entry
        assertEquals(entry0, path.getEntries()[1]);
        assertTrue(path.containsSrcFolderEntry(folder));
        path.removeSrcFolderEntry(folder);
        assertFalse(path.containsSrcFolderEntry(folder));
        assertEquals(1, path.getEntries().length);

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(ipsProject2.getIpsObjectPath().containsSrcFolderEntry(folder));
        path.removeSrcFolderEntry(folder);
        assertFalse(path.containsSrcFolderEntry(folder));
        assertEquals(1, path.getEntries().length);
    }    
    
    public void testGetReferencedIpsProjects() throws CoreException {
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        IIpsProject refProject1 = ipsProject.getIpsModel().getIpsProject("RefProject1");
        IIpsProject refProject2 = ipsProject.getIpsModel().getIpsProject("RefProject2");
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(refProject1);
        path.newSourceFolderEntry(srcFolder);
        path.newIpsProjectRefEntry(refProject2);
        
        IIpsProject[] projects = path.getReferencedIpsProjects();
        assertEquals(2, projects.length);
        assertEquals(refProject1, projects[0]);
        assertEquals(refProject2, projects[1]);
    }

    public void testCreateFromXml() {
        Element docElement = getTestDocument().getDocumentElement();
        
        // test case 1
        IIpsObjectPath path = IpsObjectPath.createFromXml(ipsProject, XmlUtil.getElement(docElement, IpsObjectPath.XML_TAG_NAME, 0));
        
        assertTrue(path.isOutputDefinedPerSrcFolder());
        assertEquals("", path.getBasePackageNameForMergableJavaClasses());
        assertNull(path.getOutputFolderForMergableSources());
        assertEquals("", path.getBasePackageNameForDerivedJavaClasses());
        assertEquals(ipsProject.getProject().getFolder("derived"), path.getOutputFolderForDerivedSources());
        
        IIpsObjectPathEntry[] entries = path.getEntries();
        assertEquals(2, entries.length);
        assertEquals("ipssrc/modelclasses", ((IIpsSrcFolderEntry)entries[0]).getSourceFolder().getProjectRelativePath().toString());
        assertEquals("ipssrc/products", ((IIpsSrcFolderEntry)entries[1]).getSourceFolder().getProjectRelativePath().toString());
        
        // test case 2
        path = IpsObjectPath.createFromXml(ipsProject, XmlUtil.getElement(docElement, IpsObjectPath.XML_TAG_NAME, 1));
        
        assertFalse(path.isOutputDefinedPerSrcFolder());
        assertEquals("org.sample.generated", path.getBasePackageNameForMergableJavaClasses());
        assertEquals("generated", path.getOutputFolderForMergableSources().getName());
        assertEquals("org.sample.extension", path.getBasePackageNameForDerivedJavaClasses());
        assertEquals("extensions", path.getOutputFolderForDerivedSources().getName());
        
        entries = path.getEntries();
        assertEquals(2, entries.length);
        assertEquals("ipssrc/modelclasses", ((IIpsSrcFolderEntry)entries[0]).getSourceFolder().getProjectRelativePath().toString());
        assertEquals("ipssrc/products", ((IIpsSrcFolderEntry)entries[1]).getSourceFolder().getProjectRelativePath().toString());
        
    }
    
    public void testToXml() {
        IProject project = ipsProject.getProject();
        IpsObjectPath path = new IpsObjectPath(ipsProject);
        
        // test case 1: output folder and base package defined per entry
        path.setOutputDefinedPerSrcFolder(true);
        
        IIpsSrcFolderEntry entry0 = new IpsSrcFolderEntry(path, project.getFolder("ipssrc").getFolder("modelclasses"));
        entry0.setSpecificOutputFolderForMergableJavaFiles(project.getFolder("javasrc").getFolder("modelclasses"));
        entry0.setSpecificBasePackageNameForMergableJavaClasses("org.faktorips.sample.model");
        entry0.setSpecificOutputFolderForDerivedJavaFiles(project.getFolder("javasrc").getFolder("modelclasses.extensions"));
        entry0.setSpecificBasePackageNameForDerivedJavaClasses("org.faktorips.sample.model.extensions");
        IIpsSrcFolderEntry entry1 = new IpsSrcFolderEntry(path, project.getFolder("ipssrc").getFolder("products"));
        entry1.setSpecificOutputFolderForMergableJavaFiles(project.getFolder("javasrc").getFolder("products"));
        entry1.setSpecificBasePackageNameForMergableJavaClasses("org.faktorips.sample.products");
        entry1.setSpecificOutputFolderForDerivedJavaFiles(project.getFolder("javasrc").getFolder("products").getFolder("extensions"));
        entry1.setSpecificBasePackageNameForDerivedJavaClasses("org.faktorips.sample.products.extensions");
        path.setEntries(new IIpsObjectPathEntry[]{entry0, entry1});
        
        Element element = path.toXml(newDocument());
        path = new IpsObjectPath(ipsProject);
        path = (IpsObjectPath)IpsObjectPath.createFromXml(ipsProject, element);
        assertTrue(path.isOutputDefinedPerSrcFolder());
        assertEquals("", path.getBasePackageNameForMergableJavaClasses());
        assertNull(path.getOutputFolderForMergableSources());
        assertEquals("", path.getBasePackageNameForDerivedJavaClasses());
        assertNull(path.getOutputFolderForDerivedSources());
        assertEquals(2, path.getEntries().length);
        
        // test case 2: output folder and package defined via the path for all entries 
        path.setOutputDefinedPerSrcFolder(false);
        path.setOutputFolderForMergableSources(project.getFolder("generated"));
        path.setBasePackageNameForMergableJavaClasses("org.sample.generated");
        path.setOutputFolderForDerivedSources(project.getFolder("extensions"));
        path.setBasePackageNameForDerivedJavaClasses("org.sample.extensions");
        path.setOutputFolderForDerivedSources(project.getFolder("derived"));
        element = path.toXml(newDocument());
        path = new IpsObjectPath(ipsProject);
        path = (IpsObjectPath)IpsObjectPath.createFromXml(ipsProject, element);
        assertFalse(path.isOutputDefinedPerSrcFolder());
        assertEquals("org.sample.generated", path.getBasePackageNameForMergableJavaClasses());
        assertEquals(project.getFolder("generated"), path.getOutputFolderForMergableSources());
        assertEquals("org.sample.extensions", path.getBasePackageNameForDerivedJavaClasses());
        assertEquals(2, path.getEntries().length);
        assertEquals(project.getFolder("derived"), path.getOutputFolderForDerivedSources());
    }
    
    public void testFindIpsSrcFileStartingWith() throws CoreException {
        IIpsProject ipsProject2 = newIpsProject("TestProject2");
        
        IpsObjectPath path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(path);
        
        IIpsObject obj1 = newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "MotorPolicy");
        IIpsObject obj2 = newIpsObject(ipsProject2, IpsObjectType.POLICY_CMPT_TYPE, "MotorPolicy2");
        
        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        path.findIpsSrcFilesStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "Motor", false, result, visitedEntries);
        assertEquals(2, result.size());
        assertTrue(result.contains(obj1.getIpsSrcFile()));
        assertTrue(result.contains(obj2.getIpsSrcFile()));
    }
    
    public void testFindIpsSrcFiles() throws Exception{
    
        IIpsObject obj1 = newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT_TYPE, "a.b.A");
        IIpsObject obj2 = newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT_TYPE, "a.b.B");
        IIpsObject obj3 = newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT_TYPE, "a.b.C");
        
        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        ((IpsObjectPath)ipsProject.getIpsObjectPath()).findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE, result, visitedEntries);
        
        assertTrue(result.contains(obj1.getIpsSrcFile()));
        assertTrue(result.contains(obj2.getIpsSrcFile()));
        assertTrue(result.contains(obj3.getIpsSrcFile()));
    }
    
    public void testGetOutputFolders() {
        IProject project = ipsProject.getProject();
        IpsObjectPath path = new IpsObjectPath(ipsProject);
        IFolder out0 = project.getFolder("out0");
        IFolder ext0 = project.getFolder("ext0");
        path.setOutputFolderForMergableSources(out0);
        path.setOutputFolderForDerivedSources(ext0);
        
        IIpsSrcFolderEntry entry0 = path.newSourceFolderEntry(project.getFolder("src0"));
        IFolder out1 = project.getFolder("out1");
        entry0.setSpecificOutputFolderForMergableJavaFiles(out1);
        IIpsSrcFolderEntry entry1 = path.newSourceFolderEntry(project.getFolder("src1"));
        IFolder out2 = project.getFolder("out2");
        entry1.setSpecificOutputFolderForMergableJavaFiles(out2);
        IIpsSrcFolderEntry entry2 = path.newSourceFolderEntry(project.getFolder("src1"));
        entry2.setSpecificOutputFolderForMergableJavaFiles(null);
        path.newIpsProjectRefEntry(ipsProject);
        
        // one output folder for all src folders
        path.setOutputDefinedPerSrcFolder(false);
        IFolder[] outFolders = path.getOutputFolders();
        assertEquals(1, outFolders.length);
        assertEquals(out0, outFolders[0]);
        
        // one output folder, but it is null
        path.setOutputFolderForMergableSources(null);
        outFolders = path.getOutputFolders();
        assertEquals(0, outFolders.length);
        
        
        // output defined per src folder
        path.setOutputDefinedPerSrcFolder(true);
        outFolders = path.getOutputFolders();
        assertEquals(2, outFolders.length);
        assertEquals(out1, outFolders[0]);
        assertEquals(out2, outFolders[1]);
    }
    
    public void testValidate() throws CoreException{
        MessageList ml = ipsProject.validate();
        assertEquals(0, ml.getNoOfMessages());
        
        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();
        
        // validate missing outputFolderGenerated
        IFolder folder1 = ipsProject.getProject().getFolder("none");
        path.setOutputFolderForMergableSources(folder1);
        path.setOutputDefinedPerSrcFolder(false);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsSrcFolderEntry.MSGCODE_MISSING_FOLDER));

        // validate missing outputFolderExtension
        path.setOutputFolderForDerivedSources(folder1);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(2, ml.getNoOfMessages());

        //validate missing folders only when general output folder needs to be defined
        path.setOutputDefinedPerSrcFolder(true);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(0, ml.getNoOfMessages());
        
    }
    
    public void testValidateOutputFolderMergableAndDerivedEmpty() throws Exception{
        MessageList ml = ipsProject.validate();
        assertEquals(0, ml.getNoOfMessages());
        
        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();
        
        ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsObjectPath.MSGCODE_MERGABLE_OUTPUT_FOLDER_NOT_SPECIFIED));
        assertNull(ml.getMessageByCode(IIpsObjectPath.MSGCODE_DERIVED_OUTPUT_FOLDER_NOT_SPECIFIED));

        path.setOutputDefinedPerSrcFolder(false);
        path.setOutputFolderForMergableSources(null);
        path.setOutputFolderForDerivedSources(null);
        ipsProject.setProperties(props);
        
        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsObjectPath.MSGCODE_MERGABLE_OUTPUT_FOLDER_NOT_SPECIFIED));
        assertNotNull(ml.getMessageByCode(IIpsObjectPath.MSGCODE_DERIVED_OUTPUT_FOLDER_NOT_SPECIFIED));
    }
    
    public void testConstructor(){
        
        try{
            new IpsObjectPath(null);
            fail();
        }
        catch(Exception e){}
        
        IpsProject ipsProject = new IpsProject();
        IpsObjectPath path = new IpsObjectPath(ipsProject);
        assertSame(ipsProject, path.getIpsProject());
    }
    
    public void testMoveEntries() throws Exception {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        
        IIpsObjectPathEntry entry0 = path.getEntries()[0];      // default test project contains already 1 entry
        IIpsSrcFolderEntry entry1 = path.newSourceFolderEntry(ipsProject.getProject().getFolder("src"));
        IIpsSrcFolderEntry entry2 = path.newSourceFolderEntry(ipsProject.getProject().getFolder("src2"));
        IIpsSrcFolderEntry entry3 = path.newSourceFolderEntry(ipsProject.getProject().getFolder("src3"));

        assertEquals(4, path.getEntries().length);

        // move top two entries one position down
        int[] newIndices = path.moveEntries(new int[] {0,1}, false);
        assertEquals(4, path.getEntries().length);
        assertEquals(2, newIndices.length);
        assertTrue((newIndices[0] == 1) || (newIndices[1] == 1));   // check if the expected indices are contained in the 
        assertTrue((newIndices[0] == 2) || (newIndices[1] == 2));   // returned array (no order guaranteed)
        
        assertEquals(entry2, path.getEntries()[0]);                 // check if the IPS object path was really modified in the
        assertEquals(entry0, path.getEntries()[1]);                 // expected manner
        assertEquals(entry1, path.getEntries()[2]);
        assertEquals(entry3, path.getEntries()[3]);
        
        // now move last three entries one position up
        newIndices = path.moveEntries(new int[] {3,1,2}, true);
        assertEquals(4, path.getEntries().length);
        assertEquals(3, newIndices.length);
        assertTrue((newIndices[0] == 0) || (newIndices[1] == 0) || (newIndices[2] == 0)); 
        assertTrue((newIndices[0] == 1) || (newIndices[1] == 1) || (newIndices[2] == 1)); 
        assertTrue((newIndices[0] == 2) || (newIndices[1] == 2) || (newIndices[2] == 2)); 
        
        assertEquals(entry0, path.getEntries()[0]);
        assertEquals(entry1, path.getEntries()[1]);
        assertEquals(entry3, path.getEntries()[2]);
        assertEquals(entry2, path.getEntries()[3]);
        
        // invalid values should not change the elements order
        newIndices = path.moveEntries(new int[] {-2, 42}, true);
        assertEquals(entry0, path.getEntries()[0]);
        assertEquals(entry1, path.getEntries()[1]);
        assertEquals(entry3, path.getEntries()[2]);
        assertEquals(entry2, path.getEntries()[3]);

        // invalid values should not change the elements order
        newIndices = path.moveEntries(new int[] {-3, 21}, false);
        assertEquals(entry0, path.getEntries()[0]);
        assertEquals(entry1, path.getEntries()[1]);
        assertEquals(entry3, path.getEntries()[2]);
        assertEquals(entry2, path.getEntries()[3]);
    }

}
