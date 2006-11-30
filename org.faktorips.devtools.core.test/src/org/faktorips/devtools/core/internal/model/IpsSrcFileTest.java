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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.IModificationStatusChangeListener;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;


/**
 *
 */
public class IpsSrcFileTest extends AbstractIpsPluginTest implements IModificationStatusChangeListener {
    
    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot ipsRootFolder;
    private IIpsPackageFragment ipsFolder;
    private IIpsSrcFile parsableFile; // file with parsable contents
    private IPolicyCmptType policyCmptType;
    private IIpsSrcFile unparsableFile; // file with unparsable contents
    
    private ModificationStatusChangedEvent lastModStatusEvent;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        ipsRootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        ipsFolder = ipsRootFolder.createPackageFragment("folder", true, null);
        
        parsableFile = ipsFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "ParsableFile", true, null);
        policyCmptType = (IPolicyCmptType)parsableFile.getIpsObject();
        unparsableFile = ipsFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE.getFileName("UnparsableFile"), "blabla", true, null);
        unparsableFile.getCorrespondingFile().setContents(new ByteArrayInputStream("Blabla".getBytes()), true, false, null);
    }

    public void testContentIsParsable() throws CoreException {
        assertFalse(unparsableFile.isContentParsable());
        assertTrue(parsableFile.isContentParsable());
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
    
    public void testDiscardChanges_ParsableContents() throws CoreException {
        IPolicyCmptType type = newPolicyCmptType(this.ipsProject, "Policy");
        IIpsSrcFile file = type.getIpsSrcFile();
        type.newAttribute();
        assertEquals(1, type.getNumOfAttributes());
        assertTrue(file.isDirty());
        file.discardChanges();
        type = (IPolicyCmptType)file.getIpsObject();
        assertEquals(0, type.getNumOfAttributes());
        assertFalse(file.isDirty());
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
    
    public void testSave() throws IOException, CoreException {
        policyCmptType.newAttribute();
        parsableFile.getIpsModel().addModifcationStatusChangeListener(this);
        parsableFile.save(true, null);
        assertFalse(parsableFile.isDirty());
        assertEquals(parsableFile, lastModStatusEvent.getIpsSrcFile());
    }
    
    
    public void testIsHistoric() {
        assertFalse(parsableFile.isHistoric());
    }

    public void testNewMemento() throws CoreException {
        policyCmptType.newAttribute();
        IIpsSrcFileMemento memento = parsableFile.newMemento();
        assertEquals(true, memento.isDirty());
        assertEquals(parsableFile, memento.getIpsSrcFile());
    }
    
    public void testSetMemento() throws CoreException {
        IIpsSrcFileMemento memento = parsableFile.newMemento();
        policyCmptType.newAttribute();
        parsableFile.setMemento(memento);
        assertEquals(0, policyCmptType.getNumOfAttributes());
        assertFalse(parsableFile.isDirty());
    }

    /**
     * {@inheritDoc}
     */
    public void modificationStatusHasChanged(ModificationStatusChangedEvent event) {
        lastModStatusEvent = event;
    }

}
