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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;


/**
 *
 */
public class IpsObjectTest extends AbstractIpsPluginTest implements ContentsChangeListener {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot rootFolder;
    private IIpsSrcFile srcFile;
    private IIpsObject ipsObject;
    private ContentChangeEvent lastEvent;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        rootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        ipsObject = newPolicyCmptType(ipsProject, "pack.TestProduct");
        srcFile = ipsObject.getIpsSrcFile();
    }
    
    public void testGetQualifiedName() throws CoreException {
        assertEquals("pack.TestProduct", ipsObject.getQualifiedName());
        IIpsPackageFragment defaultFolder = rootFolder.getIpsPackageFragment("");
        IIpsSrcFile file = defaultFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestProduct", true, null);
        assertEquals("TestProduct", file.getIpsObject().getQualifiedName());
    }
    
    public void testSetDescription() {
        ipsObject.getIpsModel().addChangeListener(this);
        ipsObject.setDescription("new description");
        assertEquals("new description", ipsObject.getDescription());
        assertTrue(srcFile.isDirty());
        assertEquals(srcFile, lastEvent.getIpsSrcFile());
    }
    
    /** 
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

}
