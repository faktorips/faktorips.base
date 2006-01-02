package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.util.StringUtil;


/**
 *
 */
public class IpsModelTest extends PluginTest implements ContentsChangeListener {
    
    private IpsModel model;
    private ContentChangeEvent lastEvent;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        model = (IpsModel)IpsPlugin.getDefault().getIpsModel();
    }

    /*
     * @see PluginTest#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetIpsProjects() throws CoreException {
        super.newPlatformProject("TestProject");
        IIpsProject[] pdProjects = model.getIpsProjects();
        assertEquals(0, pdProjects.length);
        
        super.newIpsProject("TestPdProject");
        pdProjects = model.getIpsProjects();
        assertEquals(1, pdProjects.length);
        assertEquals("TestPdProject", pdProjects[0].getName());
    }

    public void testGetIpsProject() {
    }
    
    public void testGetIpsElement() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        assertEquals(model, model.getIpsElement(root));
        
        IProject project = root.getProject("TestProject");
        IIpsProject pdProject = model.getIpsProject("TestProject");
        assertEquals(pdProject, model.getIpsElement(project));
        
        IFolder rootFolder = project.getFolder("productdef");
        IIpsPackageFragmentRoot pdRootFolder = pdProject.getIpsPackageFragmentRoot("productdef");
        assertEquals(pdRootFolder, model.getIpsElement(rootFolder));
        
        IFolder folderA = rootFolder.getFolder("a");
        IIpsPackageFragment pdFolderA = pdRootFolder.getIpsPackageFragment("a");
        assertEquals(pdFolderA, model.getIpsElement(folderA));

        IFolder folderB = folderA.getFolder("b");
        IIpsPackageFragment pdFolderB = pdRootFolder.getIpsPackageFragment("a.b");
        assertEquals(pdFolderB, model.getIpsElement(folderB));
        
        String filename = IpsObjectType.POLICY_CMPT_TYPE.getFileName("test");
        IFile file = folderB.getFile(filename);
        IIpsSrcFile srcFile = pdFolderB.getIpsSrcFile(filename);
        assertEquals(srcFile, model.getIpsElement(file));
    }
    
    public void testAddChangeListener() {
        model.addChangeListener(this);
        ContentChangeEvent event = new ContentChangeEvent(null);
        assertNull(lastEvent);
        model.notifyChangeListeners(event);
        assertEquals(event, lastEvent);
    }

    public void testRemoveChangeListener() {
        model.addChangeListener(this);
        model.removeChangeListener(this);
        ContentChangeEvent event = new ContentChangeEvent(null);
        model.notifyChangeListeners(event);
        assertNull(lastEvent);
    }
    
    public void testResourceChanged() throws IOException, CoreException {
        IIpsProject ipsProject = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment pack = root.createPackageFragment("pack", true, null);
        IIpsSrcFile sourceFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        IPolicyCmptType object = (PolicyCmptType)sourceFile.getIpsObject();
        IMethod method = object.newMethod();
        method.setBody("blabla");
        sourceFile.save(true, null);
        
        IFile file = sourceFile.getCorrespondingFile();
        assertTrue(file.exists());
        String encoding = ipsProject.getProject().getDefaultCharset(true);
        String contents = StringUtil.readFromInputStream(file.getContents(), encoding);
        contents = StringUtils.replace(contents, "blabla", "something serious");
        ByteArrayInputStream is = new ByteArrayInputStream(contents.getBytes(encoding)); 
        file.setContents(is, true, false, null);
        
        object = (IPolicyCmptType)sourceFile.getIpsObject();
        method = object.getMethods()[0];
        assertEquals("something serious", method.getBody());
    }
    
    public void testGetIpsObjectExtensionProperties() {
        ExtensionPropertyDefinition property = new StringExtensionPropertyDefinition();
        property.setPropertyId("prop1");
        property.setExtendedType(this.getClass());
        model.addIpsObjectExtensionProperty(property);
        IExtensionPropertyDefinition[] props = model.getExtensionPropertyDefinitions(getClass(), false);
        assertEquals(1, props.length);
        assertSame(property, props[0]);
        props = model.getExtensionPropertyDefinitions(this.getClass(), true);
        assertEquals(1, props.length);
        assertSame(property, props[0]);
        assertEquals(0, model.getExtensionPropertyDefinitions(String.class, true).length);
        
        // test properties defined on one of the supertypes
        ExtensionPropertyDefinition property2 = new StringExtensionPropertyDefinition();
        property2.setPropertyId("prop2");
        property2.setExtendedType(this.getClass().getSuperclass());
        model.addIpsObjectExtensionProperty(property2);
        ExtensionPropertyDefinition property3 = new StringExtensionPropertyDefinition();
        property3.setPropertyId("prop3");
        property3.setExtendedType(this.getClass().getSuperclass().getSuperclass());
        model.addIpsObjectExtensionProperty(property3);
        
        props = model.getExtensionPropertyDefinitions(getClass(), true);
        assertEquals(3, props.length);
        assertSame(property, props[0]);
        assertSame(property2, props[1]);
        assertSame(property3, props[2]);
        props = model.getExtensionPropertyDefinitions(getClass(), false);
        assertEquals(1, props.length);

        // test properties defined in one of the interfaces
        ExtensionPropertyDefinition property4 = new StringExtensionPropertyDefinition();
        property4.setPropertyId("prop4");
        property4.setExtendedType(ContentsChangeListener.class);
        model.addIpsObjectExtensionProperty(property4);
        props = model.getExtensionPropertyDefinitions(this.getClass(), true);
        assertEquals(4, props.length);
        assertSame(property, props[0]);  // first the type's properties
        assertSame(property2, props[1]); // then the supertype's properties
        assertSame(property3, props[2]); // then the supertype's supertype's properties
        assertSame(property4, props[3]); // the the type's interface's properties
        props = model.getExtensionPropertyDefinitions(getClass(), false);
        assertEquals(1, props.length);
    }
    
    public void testGetIpsObjectExtensionProperty() {
        ExtensionPropertyDefinition property = new StringExtensionPropertyDefinition();
        property.setPropertyId("prop1");
        property.setExtendedType(this.getClass());
        model.addIpsObjectExtensionProperty(property);
        
        assertEquals(property, model.getExtensionPropertyDefinition(getClass(), "prop1", false));
        
        // test properties defined on one of the supertypes
        ExtensionPropertyDefinition property2 = new StringExtensionPropertyDefinition();
        property2.setPropertyId("prop2");
        property2.setExtendedType(this.getClass().getSuperclass());
        model.addIpsObjectExtensionProperty(property2);
        ExtensionPropertyDefinition property3 = new StringExtensionPropertyDefinition();
        property3.setPropertyId("prop3");
        property3.setExtendedType(this.getClass().getSuperclass().getSuperclass());
        model.addIpsObjectExtensionProperty(property3);

        assertEquals(property2, model.getExtensionPropertyDefinition(getClass(), "prop2", true));
        assertEquals(property3, model.getExtensionPropertyDefinition(getClass(), "prop3", true));
        assertNull(model.getExtensionPropertyDefinition(getClass(), "prop2", false));
        assertNull(model.getExtensionPropertyDefinition(getClass(), "prop3", false));
        
        assertNull(model.getExtensionPropertyDefinition(getClass(), "unknownProp", true));
        assertNull(model.getExtensionPropertyDefinition(String.class, "prop2", false));
        
    }    

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.ContentsChangeListener#contentsChanged(org.faktorips.devtools.core.model.ContentChangeEvent)
     */
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }
}