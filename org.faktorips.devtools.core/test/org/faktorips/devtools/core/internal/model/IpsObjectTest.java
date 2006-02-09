package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.memento.Memento;


/**
 *
 */
public class IpsObjectTest extends IpsPluginTest implements ContentsChangeListener {

    private IIpsProject pdProject;
    private IIpsPackageFragmentRoot rootFolder;
    private IIpsSrcFile srcFile;
    private IIpsObject pdObject;
    private ContentChangeEvent lastEvent;
    
    protected void setUp() throws Exception {
        super.setUp();
        pdProject = this.newIpsProject("TestProject");
        rootFolder = pdProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment folder = rootFolder.getIpsPackageFragment("folder");
        srcFile = new IpsSrcFile(folder, IpsObjectType.POLICY_CMPT_TYPE.getFileName("TestProduct"));
        pdObject = new PolicyCmptType(srcFile);
        IpsPlugin.getDefault().getManager().putSrcFileContents(srcFile, new IpsSourceFileContents(srcFile, "", pdProject.getProject().getDefaultCharset()));
    }
    
    public void testGetQualifiedName() throws CoreException {
        assertEquals("folder.TestProduct", pdObject.getQualifiedName());
        IIpsPackageFragment defaultFolder = rootFolder.getIpsPackageFragment("");
        IIpsSrcFile file = defaultFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestProduct", true, null);
        assertEquals("TestProduct", file.getIpsObject().getQualifiedName());
    }
    
    public void testSetDescription() {
        pdObject.getIpsModel().addChangeListener(this);
        pdObject.setDescription("new description");
        assertEquals("new description", pdObject.getDescription());
        assertTrue(srcFile.isDirty());
        assertEquals(srcFile, lastEvent.getPdSrcFile());
    }
    
    public void testNewMemento() {
        Memento memento = pdObject.newMemento();
        assertEquals(pdObject, memento.getOriginator());        
    }
    
    public void testSetState() {
        pdObject.setDescription("blabla");
        Memento memento = pdObject.newMemento();
        pdObject.setDescription("newDescription");
        pdObject.setState(memento);
        assertEquals("blabla", pdObject.getDescription());
        
        IpsSrcFile file2 = new IpsSrcFile(null, IpsObjectType.POLICY_CMPT_TYPE.getFileName("file"));
        IIpsObject pdObject2 = new PolicyCmptType(file2);
        try {
            pdObject2.setState(memento);
            fail();
        } catch(IllegalArgumentException e) {
        }
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.ContentsChangeListener#contentsChanged(org.faktorips.devtools.core.model.ContentChangeEvent)
     */
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

}
