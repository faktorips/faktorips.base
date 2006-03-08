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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;


/**
 *
 */
public class IpsPackageFragmentRootTest extends IpsPluginTest {
    
    private IIpsProject ipsProject;
    private IpsPackageFragmentRoot ipsRoot;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        ipsRoot = (IpsPackageFragmentRoot)ipsProject.getIpsPackageFragmentRoots()[0];
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetIpsObjectPathEntry() throws CoreException {
        IIpsObjectPathEntry entry = ipsRoot.getIpsObjectPathEntry();
        assertNotNull(entry);
    }
    
    public void testGetArtefactDestination() throws CoreException{
        IFolder destination = ipsRoot.getArtefactDestination();
        assertNotNull(destination);
        IIpsSrcFolderEntry srcEntry = ipsProject.getIpsObjectPath().getSourceFolderEntries()[0];
        IPath outputPath = srcEntry.getOutputFolderForGeneratedJavaFiles().getProjectRelativePath();
        assertEquals(outputPath, destination.getProjectRelativePath());
    }
    
    public void testGetPdProject() {
        assertEquals(ipsProject, ipsRoot.getIpsProject());
    }

    public void testGetPdFolders() throws CoreException {
        IIpsPackageFragment defaultFolder = ipsRoot.getIpsPackageFragment("");
        assertEquals(1, ipsRoot.getIpsPackageFragments().length);
        assertEquals(defaultFolder, ipsRoot.getIpsPackageFragments()[0]);
        
        IIpsPackageFragment folderA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment folderB = ipsRoot.createPackageFragment("a.b", true, null);
        IIpsPackageFragment folderC = ipsRoot.createPackageFragment("c", true, null);
        IIpsPackageFragment folderD = ipsRoot.createPackageFragment("c.d", true, null);
        
        IIpsElement[] children = ipsRoot.getIpsPackageFragments();

        assertEquals(5, ipsRoot.getIpsPackageFragments().length);
        assertEquals(defaultFolder, children[0]);
        assertEquals(folderA, children[1]);
        assertEquals(folderB, children[2]);
        assertEquals(folderC, children[3]);
        assertEquals(folderD, children[4]);
    }

    public void testGetPdFolder() {
        IIpsPackageFragment f = ipsRoot.getIpsPackageFragment("folder");
        assertFalse(f.exists());
    }

    public void testCreatePdFolder() throws CoreException {
        IIpsPackageFragment f = ipsRoot.createPackageFragment("a.b", true, null);
        assertTrue(f.exists());
        assertEquals(ipsRoot, f.getParent());
        IFolder folderB = (IFolder)f.getCorrespondingResource();
        assertTrue(folderB.exists());
        assertEquals("b", folderB.getName());
        IFolder folderA = (IFolder)folderB.getParent();
        assertTrue(folderA.exists());
        assertEquals("a", folderA.getName());
    }

    public void testGetCorrespondingResource() {
        IFolder folder = (IFolder)ipsRoot.getCorrespondingResource();
        assertTrue(folder.exists());
    }

    public void testGetChildren() {
    }
    
    public void testHasChildren() {
    }
    
    public void testExists() throws CoreException {
        assertTrue(ipsRoot.exists());
        IIpsPackageFragmentRoot root2 = ipsProject.getIpsPackageFragmentRoot("unknown");
        assertFalse(root2.exists());
        ((IFolder)root2.getCorrespondingResource()).create(true, true, null);
        assertFalse(root2.exists());
    }
    
    public void testGetPdObject() throws CoreException {
        IIpsPackageFragment pack = ipsRoot.createPackageFragment("a.b", true, null);
        IIpsSrcFile file = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Test", true, null);
        IIpsObject pdObject = ipsRoot.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "a.b.Test");
        assertNotNull(pdObject);
        assertEquals(file.getIpsObject(), pdObject);
        
        assertNull(ipsRoot.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "c.Unknown"));
    }
    
    public void testFindProductCmpts() throws CoreException {
        IIpsPackageFragment pack = ipsRoot.createPackageFragment("pack", true, null);
        pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Policy", true, null);
        IIpsSrcFile motorPolicyFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "MotorPolicy", true, null);
        IPolicyCmptType motorPolicy = (IPolicyCmptType)motorPolicyFile.getIpsObject();
        motorPolicy.setSupertype("pack.Policy");
        
        IIpsSrcFile product1File = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "Product1", true, null);
        IProductCmpt product1 = (IProductCmpt)product1File.getIpsObject();
        product1.setPolicyCmptType("pack.Policy");
        
        IIpsSrcFile product2File = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "Product2", true, null);
        IProductCmpt product2 = (IProductCmpt)product2File.getIpsObject();
        product2.setPolicyCmptType("pack.Policy");
        
        IIpsSrcFile motorProductFile = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "MotorProduct", true, null);
        IProductCmpt motorProduct = (IProductCmpt)motorProductFile.getIpsObject();
        motorProduct.setPolicyCmptType("pack.MotorPolicy");
        
        List result = new ArrayList();
        ipsRoot.findProductCmpts("pack.Policy", false, result);
        assertEquals(2, result.size());
        assertTrue(result.contains(product1));
        assertTrue(result.contains(product2));
        
        result.clear();
        ipsRoot.findProductCmpts("pack.Policy", true, result);
        assertEquals(3, result.size());
        assertTrue(result.contains(product1));
        assertTrue(result.contains(product2));
        assertTrue(result.contains(motorProduct));
        
        result.clear();
        ipsRoot.findProductCmpts("pack.Unknown", true, result);
        assertEquals(0, result.size());
    }

    public void testFindIpsObjectsStartingWith() throws CoreException {
        IIpsObject obj1 = newIpsObject(ipsRoot, IpsObjectType.POLICY_CMPT_TYPE, "pack1.MotorPolicy");
        IIpsObject obj2 = newIpsObject(ipsRoot, IpsObjectType.POLICY_CMPT_TYPE, "pack2.MotorCoverage");
        
        ArrayList result = new ArrayList();
        ipsRoot.findIpsObjectsStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "MotorP", false, result);
        assertEquals(1, result.size());
        assertTrue(result.contains(obj1));
        
        result.clear();
        ipsRoot.findIpsObjectsStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "Motor", false, result);
        assertEquals(2, result.size());
        assertTrue(result.contains(obj1));
        assertTrue(result.contains(obj2));

        // root does not exist
        IpsPackageFragmentRoot root2 = (IpsPackageFragmentRoot)ipsProject.getIpsPackageFragmentRoot("notExistingRoot");
        result.clear();
        root2.findIpsObjectsStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "Motor", false, result);
        assertEquals(0, result.size());
        
        // ipsobjecttype null
        try {
            ipsRoot.findIpsObjectsStartingWith(null, "M", true, result);
            fail();
        } catch (NullPointerException e) {
        }
        
        // prefix null
        try {
            ipsRoot.findIpsObjectsStartingWith(IpsObjectType.POLICY_CMPT_TYPE, null, true, result);
            fail();
        } catch (NullPointerException e) {
        }
        
        // result null
        try {
            ipsRoot.findIpsObjectsStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "M", true, null);
            fail();
        } catch (NullPointerException e) {
        }
    }
    
    public void testGetIpsDefaultPackageFragment() {
    	IIpsPackageFragment def = this.ipsRoot.getIpsDefaultPackageFragment();
    	assertEquals(def.getName(), "");
    }
    

}
