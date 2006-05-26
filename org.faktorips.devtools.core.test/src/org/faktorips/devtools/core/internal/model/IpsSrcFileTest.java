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

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.StringUtil;


/**
 *
 */
public class IpsSrcFileTest extends AbstractIpsPluginTest implements ContentsChangeListener {
    
    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot ipsRootFolder;
    private IIpsPackageFragment ipsFolder;
    private IIpsSrcFile parsableFile; // file with parsable contents
    private IIpsSrcFile unparsableFile; // file with unparsable contents
    
    private ContentChangeEvent lastEvent;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        ipsRootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        ipsFolder = ipsRootFolder.createPackageFragment("folder", true, null);
        parsableFile = ipsFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "ParsableFile", true, null);
        unparsableFile = ipsFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE.getFileName("UnparsableFile"), "blabla", true, null);
    }

    /*
     * @see PluginTest#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testConstructor(){
    	
    	try{
    		new IpsSrcFile(ipsFolder, "readme.txt");
    		fail();
    	}
    	catch(Exception e){
    		//expected to fail
    	}
    }
    
    public void testGetCorrespondingResource() {
        IResource resource = parsableFile.getCorrespondingResource();
        assertTrue(resource.exists());
        assertEquals(parsableFile.getName(), resource.getName());
    }

    public void testGetCorrespondingFile() {
        IFile file = parsableFile.getCorrespondingFile();
        assertTrue(file.exists());
        assertEquals(parsableFile.getName(), file.getName());
    }
    
    public void testGetContents() throws CoreException {
        assertEquals("blabla", unparsableFile.getContents());
    }

    public void testContentIsParsable() throws CoreException {
        assertFalse(unparsableFile.isContentParsable());
        assertTrue(parsableFile.isContentParsable());
    }

    public void testGetPdObject() throws CoreException {
        IIpsObject pdObject = parsableFile.getIpsObject();
        assertNotNull(pdObject);
        
        pdObject.setDescription("blabla");
        assertSame(pdObject, parsableFile.getIpsObject());
        
        try {
            unparsableFile.getIpsObject();
            fail();
        } catch (CoreException e) {
        }
    }

    public void testGetElementName() {
        String expectedName = IpsObjectType.POLICY_CMPT_TYPE.getFileName("ParsableFile");
        assertEquals(expectedName, parsableFile.getName());
    }

    public void testGetParent() {
    }

    public void testGetChildren() throws CoreException {
        assertEquals(0, unparsableFile.getChildren().length);
        assertEquals(1, parsableFile.getChildren().length);
        assertEquals(parsableFile.getIpsObject(), parsableFile.getChildren()[0]);
    }

    public void testHasChildren() throws CoreException {
        assertFalse(unparsableFile.hasChildren());
        assertTrue(parsableFile.hasChildren());
    }
    
    public void testSetContents() throws CoreException {
        parsableFile.getIpsModel().addChangeListener(this);
        parsableFile.setContents("new contents");
        assertEquals("new contents", parsableFile.getContents());
        assertFalse(parsableFile.isContentParsable());
        assertTrue(parsableFile.isDirty());
        assertEquals(parsableFile, lastEvent.getIpsSrcFile());
    }

    public void testSave() throws IOException, CoreException {
        parsableFile.setContents("new contents with german umlaut ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½");
        parsableFile.getIpsModel().addChangeListener(this);
        parsableFile.save(true, null);
        IFile file = parsableFile.getCorrespondingFile();
        String contents = StringUtil.readFromInputStream(file.getContents(), ipsProject.getXmlFileCharset());
        assertEquals("new contents with german umlaut ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½", contents);
        assertFalse(parsableFile.isDirty());
        assertEquals(parsableFile, lastEvent.getIpsSrcFile());
    }
    
    public void testNewMemento() throws CoreException {
        parsableFile.setContents("blabla");
        IIpsSrcFileMemento memento = parsableFile.newMemento();
        assertEquals("blabla", memento.getContents());
        assertEquals(true, memento.isDirty());
        assertEquals(parsableFile, memento.getIpsSrcFile());
    }
    
    public void testSetMemento() throws CoreException {
        String contents = parsableFile.getContents();
        IIpsSrcFileMemento memento = parsableFile.newMemento();
        parsableFile.setContents("blabla");
        parsableFile.setMemento(memento);
        assertEquals(contents, parsableFile.getContents());
        assertFalse(parsableFile.isDirty());
    }
    
    public void testDiscardChanges() throws CoreException {
        String contents = parsableFile.getContents();
        parsableFile.setContents("newContents");
        assertTrue(parsableFile.isDirty());
        parsableFile.discardChanges();
        assertEquals(contents, parsableFile.getContents());
        assertFalse(parsableFile.isDirty());
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.ContentsChangeListener#contentsChanged(org.faktorips.devtools.core.model.ContentChangeEvent)
     */
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }
    

}
