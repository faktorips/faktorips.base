/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public class IpsModelTest extends AbstractIpsPluginTest implements ContentsChangeListener {
    
    private IpsModel model;
    private ContentChangeEvent lastEvent;
    
    // JavaProjects for testGetNonIpsResources()
    private IJavaProject javaProject= null;
    private IJavaProject javaProject2= null;

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
    
    public void testCreateIpsProject() throws CoreException {
    	IProject project = newPlatformProject("TestProject");
    	IJavaProject javaProject = addJavaCapabilities(project);
    	IIpsProject ipsProject = model.createIpsProject(javaProject);
    	assertNotNull(ipsProject);
    	assertEquals(0, ipsProject.getValueDatatypes(false).length);
    	IIpsObjectPath path = ipsProject.getIpsObjectPath();
    	assertNotNull(path);
    	assertEquals(0, path.getEntries().length);
    }

    public void testGetIpsProjects() throws CoreException {
        super.newPlatformProject("TestProject");
        IIpsProject[] ipsProjects = model.getIpsProjects();
        assertEquals(0, ipsProjects.length);
        
        IIpsProject project = super.newIpsProject("TestPdProject");
        ipsProjects = model.getIpsProjects();
        assertEquals(1, ipsProjects.length);
        assertEquals("TestPdProject", ipsProjects[0].getName());
        
        project.getProject().close(null);
        super.newIpsProject("TestProject2");
        ipsProjects = model.getIpsProjects();
        assertEquals(1, ipsProjects.length);
        assertEquals("TestProject2", ipsProjects[0].getName());
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
        IAttribute attribute = object.newAttribute();
        attribute.setDescription("blabla");
        sourceFile.save(true, null);
        
        IFile file = sourceFile.getCorrespondingFile();
        assertTrue(file.exists());
        String encoding = ipsProject.getXmlFileCharset();
        String contents = StringUtil.readFromInputStream(file.getContents(), encoding);
        contents = StringUtils.replace(contents, "blabla", "something serious");
        ByteArrayInputStream is = new ByteArrayInputStream(contents.getBytes(encoding)); 
        file.setContents(is, true, false, null);
        
        object = (IPolicyCmptType)sourceFile.getIpsObject();
        attribute = object.getAttributes()[0];
        assertEquals("something serious", attribute.getDescription());
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
    
    public void testGetPredefinedValueDatatypes() {
    	ValueDatatype[] datatypes = model.getPredefinedValueDatatypes();
    	assertTrue(datatypes.length > 0);
    }
    
    public void testIsPredefinedValueDatatype() {
    	ValueDatatype[] datatypes = model.getPredefinedValueDatatypes();
    	assertTrue(model.isPredefinedValueDatatype(datatypes[0].getQualifiedName()));
    	assertFalse(model.isPredefinedValueDatatype("unknownDatatype"));
    	assertFalse(model.isPredefinedValueDatatype(null));
    }
    
    public void testGetChangesInTimeNamingConvention() {
    	IChangesOverTimeNamingConvention convention = model.getChangesOverTimeNamingConvention(IChangesOverTimeNamingConvention.VAA);
    	assertNotNull(convention);
    	assertEquals(IChangesOverTimeNamingConvention.VAA, convention.getId());
    	
    	convention = model.getChangesOverTimeNamingConvention(IChangesOverTimeNamingConvention.PM);
    	assertNotNull(convention);
    	assertEquals(IChangesOverTimeNamingConvention.PM, convention.getId());
    	
    	convention = model.getChangesOverTimeNamingConvention("unknown");
    	assertNotNull(convention);
    	assertEquals(IChangesOverTimeNamingConvention.VAA, convention.getId());
    }
    
    public void testGetNonIpsResources() throws CoreException{
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                IProject project = newPlatformProject("TestJavaProject");
                javaProject= addJavaCapabilities(project);
                IProject project2 = newPlatformProject("TestJavaProject2");
                javaProject2= addJavaCapabilities(project2);
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

        newIpsProject("TestProject");

        assertNotNull(javaProject);
        assertNotNull(javaProject2);
        
        Object[] nonIpsResources= model.getNonIpsResources();
        assertEquals(2, nonIpsResources.length);
        // compare handles (IProject)
        assertEquals(javaProject.getProject(), nonIpsResources[0]);
        assertEquals(javaProject2.getProject(), nonIpsResources[1]);
    }
    
    public void testCheckForDuplicateRuntimeIds() throws CoreException {
        IIpsProject prj = newIpsProject("PRJ1");
        IProductCmpt cmpt1 = newProductCmpt(prj, "product1");
        IProductCmpt cmpt2 = newProductCmpt(prj, "product2");
        
        cmpt1.setRuntimeId("Egon");
        cmpt2.setRuntimeId("Egon");
        assertEquals(cmpt1.getRuntimeId(), cmpt2.getRuntimeId());
        
        MessageList ml = model.checkForDuplicateRuntimeIds();
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsModel.MSGCODE_RUNTIME_ID_COLLISION));
        
        cmpt2.setRuntimeId("Hugo");
        ml = model.checkForDuplicateRuntimeIds();
        assertEquals(0, ml.getNoOfMessages());
        assertNull(ml.getMessageByCode(IIpsModel.MSGCODE_RUNTIME_ID_COLLISION));
        
        IProductCmpt cmpt3 = newProductCmpt(prj, "product3");
        cmpt3.setRuntimeId("Egon");
        cmpt2.setRuntimeId("Egon");
        ml = model.checkForDuplicateRuntimeIds();
        assertEquals(3, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsModel.MSGCODE_RUNTIME_ID_COLLISION));
        
        ml = model.checkForDuplicateRuntimeIds(new IProductCmpt[] {cmpt3});
        assertEquals(2, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsModel.MSGCODE_RUNTIME_ID_COLLISION));
        
        ml = model.checkForDuplicateRuntimeIds(new IProductCmpt[] {cmpt1, cmpt3});
        assertEquals(4, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsModel.MSGCODE_RUNTIME_ID_COLLISION));
        
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.ContentsChangeListener#contentsChanged(org.faktorips.devtools.core.model.ContentChangeEvent)
     */
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }
}