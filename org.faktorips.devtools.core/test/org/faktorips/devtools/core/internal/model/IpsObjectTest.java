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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.util.memento.Memento;


/**
 *
 */
public class IpsObjectTest extends IpsPluginTest implements ContentsChangeListener {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot rootFolder;
    private IIpsSrcFile srcFile;
    private IIpsObject ipsObject;
    private ContentChangeEvent lastEvent;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        rootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment folder = rootFolder.getIpsPackageFragment("folder");
        srcFile = new IpsSrcFile(folder, IpsObjectType.POLICY_CMPT_TYPE.getFileName("TestProduct"));
        ipsObject = new PolicyCmptType(srcFile);
        IpsPlugin.getDefault().getManager().putSrcFileContents(srcFile, new IpsSourceFileContents(srcFile, "", ipsProject.getProject().getDefaultCharset()));
    }
    
    public void testGetQualifiedName() throws CoreException {
        assertEquals("folder.TestProduct", ipsObject.getQualifiedName());
        IIpsPackageFragment defaultFolder = rootFolder.getIpsPackageFragment("");
        IIpsSrcFile file = defaultFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestProduct", true, null);
        assertEquals("TestProduct", file.getIpsObject().getQualifiedName());
    }
    
    public void testSetDescription() {
        ipsObject.getIpsModel().addChangeListener(this);
        ipsObject.setDescription("new description");
        assertEquals("new description", ipsObject.getDescription());
        assertTrue(srcFile.isDirty());
        assertEquals(srcFile, lastEvent.getPdSrcFile());
    }
    
    public void testNewMemento() {
        Memento memento = ipsObject.newMemento();
        assertEquals(ipsObject, memento.getOriginator());        
    }
    
    public void testSetState() throws CoreException {
        ipsObject.setDescription("blabla");
        Memento memento = ipsObject.newMemento();
        ipsObject.setDescription("newDescription");
        ipsObject.setState(memento);
        assertEquals("blabla", ipsObject.getDescription());
        
        // test if new parts are removed when the state is restored from the memento
        ((IPolicyCmptType)ipsObject).newAttribute();
        assertEquals(1, ipsObject.getChildren().length);
        ipsObject.setState(memento);
        assertEquals(0, ipsObject.getChildren().length);
        
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
