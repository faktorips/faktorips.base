package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPathTest extends PluginTest {

    private IIpsProject ipsProject;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
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
        assertEquals(3, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);
        assertEquals(entry1, path.getEntries()[2]);
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
        assertEquals("", path.getBasePackageNameForGeneratedJavaClasses());
        assertNull(path.getOutputFolderForGeneratedJavaFiles());
        assertEquals("", path.getBasePackageNameForExtensionJavaClasses());
        assertNull(path.getOutputFolderForExtensionJavaFiles());
        
        IIpsObjectPathEntry[] entries = path.getEntries();
        assertEquals(2, entries.length);
        assertEquals("ipssrc/modelclasses", ((IIpsSrcFolderEntry)entries[0]).getSourceFolder().getProjectRelativePath().toString());
        assertEquals("ipssrc/products", ((IIpsSrcFolderEntry)entries[1]).getSourceFolder().getProjectRelativePath().toString());
        
        // test case 2
        path = IpsObjectPath.createFromXml(ipsProject, XmlUtil.getElement(docElement, IpsObjectPath.XML_TAG_NAME, 1));
        
        assertFalse(path.isOutputDefinedPerSrcFolder());
        assertEquals("org.sample.generated", path.getBasePackageNameForGeneratedJavaClasses());
        assertEquals("generated", path.getOutputFolderForGeneratedJavaFiles().getName());
        assertEquals("org.sample.extension", path.getBasePackageNameForExtensionJavaClasses());
        assertEquals("extensions", path.getOutputFolderForExtensionJavaFiles().getName());
        
        entries = path.getEntries();
        assertEquals(2, entries.length);
        assertEquals("ipssrc/modelclasses", ((IIpsSrcFolderEntry)entries[0]).getSourceFolder().getProjectRelativePath().toString());
        assertEquals("ipssrc/products", ((IIpsSrcFolderEntry)entries[1]).getSourceFolder().getProjectRelativePath().toString());
        
    }
    
    public void testToXml() {
        IProject project = ipsProject.getProject();
        IpsObjectPath path = new IpsObjectPath();
        
        // test case 1: output folder and base package defined per entry
        path.setOutputDefinedPerSrcFolder(true);
        
        IIpsSrcFolderEntry entry0 = new IpsSrcFolderEntry(path, project.getFolder("ipssrc").getFolder("modelclasses"));
        entry0.setSpecificOutputFolderForGeneratedJavaFiles(project.getFolder("javasrc").getFolder("modelclasses"));
        entry0.setSpecificBasePackageNameForGeneratedJavaClasses("org.faktorips.sample.model");
        entry0.setSpecificOutputFolderForExtensionJavaFiles(project.getFolder("javasrc").getFolder("modelclasses.extensions"));
        entry0.setSpecificBasePackageNameForExtensionJavaClasses("org.faktorips.sample.model.extensions");
        IIpsSrcFolderEntry entry1 = new IpsSrcFolderEntry(path, project.getFolder("ipssrc").getFolder("products"));
        entry1.setSpecificOutputFolderForGeneratedJavaFiles(project.getFolder("javasrc").getFolder("products"));
        entry1.setSpecificBasePackageNameForGeneratedJavaClasses("org.faktorips.sample.products");
        entry1.setSpecificOutputFolderForExtensionJavaFiles(project.getFolder("javasrc").getFolder("products").getFolder("extensions"));
        entry1.setSpecificBasePackageNameForExtensionJavaClasses("org.faktorips.sample.products.extensions");
        path.setEntries(new IIpsObjectPathEntry[]{entry0, entry1});
        
        Element element = path.toXml(newDocument());
        path = new IpsObjectPath();
        path = (IpsObjectPath)IpsObjectPath.createFromXml(ipsProject, element);
        assertTrue(path.isOutputDefinedPerSrcFolder());
        assertEquals("", path.getBasePackageNameForGeneratedJavaClasses());
        assertNull(path.getOutputFolderForGeneratedJavaFiles());
        assertEquals("", path.getBasePackageNameForExtensionJavaClasses());
        assertNull(path.getOutputFolderForExtensionJavaFiles());
        assertEquals(2, path.getEntries().length);
        
        // test case 1: output folder and package defined via the path for all entries 
        path.setOutputDefinedPerSrcFolder(false);
        path.setOutputFolderForGeneratedJavaFiles(project.getFolder("generated"));
        path.setBasePackageNameForGeneratedJavaClasses("org.sample.generated");
        path.setOutputFolderForExtensionJavaFiles(project.getFolder("extensions"));
        path.setBasePackageNameForExtensionJavaClasses("org.sample.extensions");
        
        element = path.toXml(newDocument());
        path = new IpsObjectPath();
        path = (IpsObjectPath)IpsObjectPath.createFromXml(ipsProject, element);
        assertFalse(path.isOutputDefinedPerSrcFolder());
        assertEquals("org.sample.generated", path.getBasePackageNameForGeneratedJavaClasses());
        assertEquals(project.getFolder("generated"), path.getOutputFolderForGeneratedJavaFiles());
        assertEquals("org.sample.extensions", path.getBasePackageNameForExtensionJavaClasses());
        assertEquals(project.getFolder("extensions"), path.getOutputFolderForExtensionJavaFiles());
        assertEquals(2, path.getEntries().length);
    }
    
    public void testFindIpsObjectsStartingWith() throws CoreException {
        IIpsProject ipsProject2 = newIpsProject("TestProject2");
        
        IpsObjectPath path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(path);
        
        IIpsObject obj1 = newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "MotorPolicy");
        IIpsObject obj2 = newIpsObject(ipsProject2, IpsObjectType.POLICY_CMPT_TYPE, "MotorPolicy2");
        
        ArrayList result = new ArrayList();
        path.findIpsObjectsStartingWith(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "Motor", false, result);
        assertEquals(2, result.size());
        assertTrue(result.contains(obj1));
        assertTrue(result.contains(obj2));
    }
    
    public void testGetOutputFolders() {
        IProject project = ipsProject.getProject();
        IpsObjectPath path = new IpsObjectPath();
        IFolder out0 = project.getFolder("out0");
        IFolder ext0 = project.getFolder("ext0");
        path.setOutputFolderForGeneratedJavaFiles(out0);
        path.setOutputFolderForExtensionJavaFiles(ext0);
        
        IIpsSrcFolderEntry entry0 = path.newSourceFolderEntry(project.getFolder("src0"));
        IFolder out1 = project.getFolder("out1");
        entry0.setSpecificOutputFolderForGeneratedJavaFiles(out1);
        IIpsSrcFolderEntry entry1 = path.newSourceFolderEntry(project.getFolder("src1"));
        IFolder out2 = project.getFolder("out2");
        entry1.setSpecificOutputFolderForGeneratedJavaFiles(out2);
        IIpsSrcFolderEntry entry2 = path.newSourceFolderEntry(project.getFolder("src1"));
        entry2.setSpecificOutputFolderForGeneratedJavaFiles(null);
        path.newIpsProjectRefEntry(ipsProject);
        
        // one output folder for all src folders
        path.setOutputDefinedPerSrcFolder(false);
        IFolder[] outFolders = path.getOutputFolders();
        assertEquals(1, outFolders.length);
        assertEquals(out0, outFolders[0]);
        
        // one output folder, but it is null
        path.setOutputFolderForGeneratedJavaFiles(null);
        outFolders = path.getOutputFolders();
        assertEquals(0, outFolders.length);
        
        
        // output defined per src folder
        path.setOutputDefinedPerSrcFolder(true);
        outFolders = path.getOutputFolders();
        assertEquals(2, outFolders.length);
        assertEquals(out1, outFolders[0]);
        assertEquals(out2, outFolders[1]);
    }
}
