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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;


/**
 *
 */
public class IpsPackageFragmentRootTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsPackageFragmentRoot ipsRoot;

    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        ipsRoot = (IpsPackageFragmentRoot)ipsProject.getIpsPackageFragmentRoots()[0];
    }

    public void testFindAllProductCmpts() throws CoreException {

        IProductCmptType productCmptType = newProductCmptType(ipsProject, "pack1.Product");
        IProductCmptType motorProductCmptType = newProductCmptType(ipsProject, "pack2.MotorProduct");
        motorProductCmptType.setSupertype(productCmptType.getQualifiedName());

        IProductCmpt product1 = newProductCmpt(productCmptType, "pack3.Product1");
        IProductCmpt product2 = newProductCmpt(productCmptType, "pack4.Product2");
        IProductCmpt motorProduct = newProductCmpt(motorProductCmptType, "pack5.MotorProduct");

        List result = new ArrayList();
        ipsRoot.findAllProductCmpts(productCmptType, false, result);
        assertEquals(2, result.size());
        assertTrue(result.contains(product1));
        assertTrue(result.contains(product2));

        result.clear();
        ipsRoot.findAllProductCmpts(productCmptType, true, result);
        assertEquals(3, result.size());
        assertTrue(result.contains(product1));
        assertTrue(result.contains(product2));
        assertTrue(result.contains(motorProduct));
    }

    public void testGetIpsObjectPathEntry() throws CoreException {
        IIpsObjectPathEntry entry = ipsRoot.getIpsObjectPathEntry();
        assertNotNull(entry);
    }

    public void testGetArtefactDestination() throws CoreException{
        IFolder destination = ipsRoot.getArtefactDestination(false);
        assertNotNull(destination);
        IIpsSrcFolderEntry srcEntry = ipsProject.getIpsObjectPath().getSourceFolderEntries()[0];
        IPath outputPath = srcEntry.getOutputFolderForMergableJavaFiles().getProjectRelativePath();
        assertEquals(outputPath, destination.getProjectRelativePath());
        destination = ipsRoot.getArtefactDestination(true);
        IPath outputPathDerived = srcEntry.getOutputFolderForDerivedJavaFiles().getProjectRelativePath();
        assertEquals(outputPathDerived, destination.getProjectRelativePath());
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

    public void testGetIpsObject() throws CoreException {
        IIpsPackageFragment pack = ipsRoot.createPackageFragment("a.b", true, null);
        IIpsSrcFile file = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Test", true, null);
        IIpsObject ipsObject = ipsRoot.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "a.b.Test");
        assertNotNull(ipsObject);
        assertEquals(file.getIpsObject(), ipsObject);

        assertNull(ipsRoot.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "c.Unknown"));
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
    	IIpsPackageFragment def = this.ipsRoot.getDefaultIpsPackageFragment();
    	assertEquals(def.getName(), "");
    }

    public void testGetNonIpsResources() throws CoreException{
        IIpsPackageFragment fragment= ipsRoot.createPackageFragment("fragment", true, null);
        IIpsPackageFragment subFragment= ipsRoot.createPackageFragment("fragment.sub", true, null);

        IFolder rootHandle= (IFolder) ipsRoot.getCorrespondingResource();
        IFile nonIpsFile= rootHandle.getFile("nonIpsFile");
        nonIpsFile.create(null, true, null);
        IFile nonIpsFile2= rootHandle.getFile("nonIpsFile2");
        nonIpsFile2.create(null, true, null);

        Object[] nonIpsResources= ipsRoot.getNonIpsResources();
        assertEquals(2, nonIpsResources.length);
        List list= Arrays.asList(nonIpsResources);
        assertTrue(list.contains(nonIpsFile));
        assertTrue(list.contains(nonIpsFile2));
        assertFalse(list.contains(fragment));
        assertFalse(list.contains(subFragment));
    }

    public void testFindProductCmptsByPolicyCmptWithExistingProductCmptMissingPolicyCmpt() throws CoreException{
        // find product cmpt by given product component type,
        // the package fragment we search in, contains a product cmpt without assigned product component type.
        IProductCmptType type = newProductCmptType(ipsProject, "MotorProduct");
        newProductCmpt(type, "ProductCmpt1");
        List result = new ArrayList(1);
        ipsRoot.findAllProductCmpts(type, true, result);
        assertEquals(1, result.size());

        result.clear();
        newProductCmpt(ipsRoot, "ProductCmpt2");
        ipsRoot.findAllProductCmpts(type, true, result);
        assertEquals(1, result.size());
    }

    public void testGetSortedIpsPackageFragments() throws CoreException, IOException {

        IIpsPackageFragment defaultFolder = ipsRoot.getIpsPackageFragment("");

        IIpsPackageFragment[] children = ipsRoot.getSortedIpsPackageFragments();
        assertEquals(children.length, 1);
        assertEquals(defaultFolder, children[0]);

        ipsRoot.createPackageFragment("hausrat", true, null);
        IIpsPackageFragment kranken = ipsRoot.createPackageFragment("kranken", true, null);
        ipsRoot.createPackageFragment("kranken.leistungsarten", true, null);
        ipsRoot.createPackageFragment("kranken.vertragsarten", true, null);
        ipsRoot.createPackageFragment("kranken.gruppenarten", true, null);
        ipsRoot.createPackageFragment("unfall", true, null);
        ipsRoot.createPackageFragment("haftpflicht", true, null);

        ArrayList strings = new ArrayList();
        strings.add("kranken");
        strings.add("unfall");
        strings.add("hausrat");
        strings.add("haftpflicht");

        createPackageOrderFile((IFolder) ipsRoot.getCorrespondingResource(), strings);

        strings.clear();
        strings.add("vertragsarten");
        strings.add("gruppenarten");
        strings.add("leistungsarten");

        createPackageOrderFile((IFolder) kranken.getCorrespondingResource(), strings);

        // sorted: valid files and entries
        children = ipsRoot.getSortedIpsPackageFragments();
        assertEquals(children.length, 8);
        assertEquals(children[0].getName(), "");
        assertEquals(children[1].getName(), "kranken");
        assertEquals(children[2].getName(), "kranken.vertragsarten");
        assertEquals(children[3].getName(), "kranken.gruppenarten");
        assertEquals(children[4].getName(), "kranken.leistungsarten");
        assertEquals(children[5].getName(), "unfall");
        assertEquals(children[6].getName(), "hausrat");
        assertEquals(children[7].getName(), "haftpflicht");
    }
}
