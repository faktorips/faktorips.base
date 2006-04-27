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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.IpsObjectType;


/**
 *
 */
public class IpsSrcFileImmutableTest extends IpsPluginTest {
    
    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot ipsRootFolder;
    private IIpsPackageFragment ipsFolder;
    private IIpsSrcFile ipsSrcFile; 
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        ipsRootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        ipsFolder = ipsRootFolder.createPackageFragment("folder", true, null);
        String content = ipsFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "ParsableFile", true, null).getContents();
        
        ipsSrcFile = new IpsSrcFileImmutable(ipsFolder, "ParsableFile.ipspct", "1.0", new ByteArrayInputStream(content.getBytes()));
    }

    public void testGetCorrespondingResource() {
        IResource resource = ipsSrcFile.getCorrespondingResource();
        assertNull(resource);
    }

    public void testGetCorrespondingFile() {
        IFile file = ipsSrcFile.getCorrespondingFile();
        assertNull(file);
    }
    
    public void testSetContents() throws CoreException {
        String oldContent = ipsSrcFile.getContents();

        ipsSrcFile.setContents("new contents");
        
        assertEquals(oldContent, ipsSrcFile.getContents());
        assertFalse(ipsSrcFile.isDirty());
    }

    public void testSetMemento() throws CoreException {
        String contents = ipsSrcFile.getContents();
        IIpsSrcFileMemento memento = ipsSrcFile.newMemento();
        ipsSrcFile.setContents("blabla");
        ipsSrcFile.setMemento(memento);
        assertEquals(contents, ipsSrcFile.getContents());
        assertFalse(ipsSrcFile.isDirty());
    }
    
}
