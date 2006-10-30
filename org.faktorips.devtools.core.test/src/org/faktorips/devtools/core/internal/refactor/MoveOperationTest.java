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

package org.faktorips.devtools.core.internal.refactor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.util.StringUtil;

/**
 * Tests for move- and rename-operation
 * 
 * @author Thorsten Guenther
 */
public class MoveOperationTest extends AbstractIpsPluginTest {
	
	DefaultTestContent content;
	
	/**
	 * {@inheritDoc}
	 */
    protected void setUp() throws Exception {
        super.setUp();
        content = new DefaultTestContent();
    }
   
    /**
     * For this test, one object of the comfort-product of the default test content is moved to a product with
     * the same package, but another name. After that, the new 
     * file is expected to be existant and the references to this object have to be the same as to the source.
     */
    public void testRenameProduct() throws CoreException, InvocationTargetException, InterruptedException {

    	IProductCmpt source = content.getStandardVehicle();
    	IIpsSrcFile target = source.getIpsPackageFragment().getIpsSrcFile("Moved" + source.getName() + ".ipsproduct");
        
    	assertFalse(target.exists());
        assertTrue(source.getIpsSrcFile().exists());
    	
        IProductCmptGeneration[] sourceRefs = source.getIpsProject().findReferencingProductCmptGenerations(source.getQualifiedName());
        
        MoveOperation move = new MoveOperation(source, source.getIpsPackageFragment().getName() + "." + "Moved" + source.getName());
        move.run(null);
        
        assertTrue(target.exists());
        assertFalse(source.getIpsSrcFile().exists());        
        IIpsObject targetObject = target.getIpsObject();
        IProductCmptGeneration[] targetRefs = targetObject.getIpsProject().findReferencingProductCmptGenerations(targetObject.getQualifiedName());
        
        assertEquals(sourceRefs.length, targetRefs.length);
        
    }
    
    /**
     * For this test, one package of the comfort-product of the default test content is renamed. After that, the new 
     * package is expected to be existant and the references to the contained objects have to be the same as to the source.
     */
    public void testRenamePackage() throws CoreException, InvocationTargetException, InterruptedException {

    	IIpsPackageFragment sourcePackage = content.getStandardVehicle().getIpsPackageFragment();
    	IIpsPackageFragment target = sourcePackage.getRoot().getIpsPackageFragment("renamed");
    	
    	IProductCmpt source = content.getStandardVehicle();
        
    	assertFalse(target.exists());
        assertTrue(source.getIpsSrcFile().exists());
    	
        IProductCmptGeneration[] sourceRefs = source.getIpsProject().findReferencingProductCmptGenerations(source.getQualifiedName());
        
        MoveOperation move = new MoveOperation(new IIpsElement[] {sourcePackage}, new String[] {"renamed"});
        move.run(null);
        
        assertTrue(target.exists());
        assertFalse(sourcePackage.exists());     
        
        IIpsSrcFile vehicleFile = target.getIpsSrcFile("StandardVehicle.ipsproduct"); 
        
        assertTrue(vehicleFile.exists());
        
        IIpsObject targetObject = vehicleFile.getIpsObject();
        IProductCmptGeneration[] targetRefs = targetObject.getIpsProject().findReferencingProductCmptGenerations(targetObject.getQualifiedName());
        
        assertEquals(sourceRefs.length, targetRefs.length);
        
    }
    
    
    
    /**
     * For this test, one object of the comfort-product of the default test content is moved to a 
     * new, non-existing package. After that, the new file is expected to be existant and the references 
     * to this object have to be the same as to the source.
     */
    public void testMoveProduct() throws CoreException, InvocationTargetException, InterruptedException {

    	IProductCmpt source = content.getStandardVehicle();
    	
    	IIpsSrcFile target = source.getIpsPackageFragment().getRoot().getIpsPackageFragment("test.my.pack").getIpsSrcFile(source.getName() + ".ipsproduct");
    	String targetName = target.getIpsPackageFragment().getName() + "." + source.getName();
        
    	assertFalse(target.exists());
        assertTrue(source.getIpsSrcFile().exists());
    	
        IProductCmptGeneration[] sourceRefs = source.getIpsProject().findReferencingProductCmptGenerations(source.getQualifiedName());
        
        MoveOperation move = new MoveOperation(source, targetName);
        move.run(null);
        
        assertTrue(target.exists());
        assertFalse(source.getIpsSrcFile().exists());        
        IIpsObject targetObject = target.getIpsObject();
        IProductCmptGeneration[] targetRefs = targetObject.getIpsProject().findReferencingProductCmptGenerations(targetObject.getQualifiedName());
        
        assertEquals(sourceRefs.length, targetRefs.length);
        
    }

    /**
     * For this test, one package of the comfort-product of the default test content is renamed. After that, the new 
     * package is expected to be existant and the references to the contained objects have to be the same as to the source.
     */
    public void testMovePackage() throws CoreException, InvocationTargetException, InterruptedException {

    	IIpsPackageFragment sourcePackage = content.getStandardVehicle().getIpsPackageFragment();
    	IIpsPackageFragment target = sourcePackage.getRoot().getIpsPackageFragment("moved");
    	
    	IProductCmpt source = content.getStandardVehicle();
        
    	assertFalse(target.exists());
        assertTrue(source.getIpsSrcFile().exists());
    	
        IProductCmptGeneration[] sourceRefs = source.getIpsProject().findReferencingProductCmptGenerations(source.getQualifiedName());
        
        MoveOperation move = new MoveOperation(new IIpsElement[] {sourcePackage}, target);
        move.run(null);
        
        assertTrue(target.exists());
        assertFalse(sourcePackage.exists());     
        
        target = target.getRoot().getIpsPackageFragment("moved.products");
        
        IIpsSrcFile vehicleFile = target.getIpsSrcFile("StandardVehicle.ipsproduct"); 
        
        assertTrue(vehicleFile.exists());
        
        IIpsObject targetObject = vehicleFile.getIpsObject();
        IProductCmptGeneration[] targetRefs = targetObject.getIpsProject().findReferencingProductCmptGenerations(targetObject.getQualifiedName());
        
        assertEquals(sourceRefs.length, targetRefs.length);
        
    }
    
    public void testMoveTableContent() throws CoreException, InvocationTargetException, InterruptedException {
    	IIpsSrcFile file = content.getStandardVehicle().getIpsPackageFragment().createIpsFile(IpsObjectType.TABLE_CONTENTS, "table", true, null);
    	
    	assertTrue(file.exists());
    	
   		new MoveOperation(new IIpsElement[] {file.getIpsObject()}, new String[] {"table"}).run(null);
   		
   		IIpsSrcFile target = content.getStandardVehicle().getIpsPackageFragment().getRoot().getDefaultIpsPackageFragment().getIpsSrcFile(IpsObjectType.TABLE_CONTENTS.getFileName("table"));
   		assertTrue(target.exists());
   		assertFalse(file.exists());
    }
    
    public void testRenameTableContent() throws Exception {
    	IIpsSrcFile file = content.getStandardVehicle().getIpsPackageFragment().createIpsFile(IpsObjectType.TABLE_CONTENTS, "table", true, null);
    	
    	assertTrue(file.exists());
    	
   		new MoveOperation(new IIpsElement[] {file.getIpsObject()}, new String[] {"products.newTable"}).run(null);
   		
   		IIpsSrcFile target = content.getStandardVehicle().getIpsPackageFragment().getIpsSrcFile(IpsObjectType.TABLE_CONTENTS.getFileName("newTable"));
   		assertTrue(target.exists());
   		assertFalse(file.exists());
    }
    
    /**
     * Test to rename a package framgent which contains at least one file that is NOT a product component or a table content 
     */
    public void testRenamePackageWithFiles() throws Exception {
    	IIpsPackageFragmentRoot root = content.getStandardVehicle().getIpsPackageFragment().getRoot();
    	IIpsPackageFragment pack = root.createPackageFragment("test.subpackage", true, null);
    	IFile file = ((IFolder)pack.getCorrespondingResource()).getFile("test.unknown");
    	file.create(StringUtil.getInputStreamForString("Test content for file.", "UTF-8"), true, null);
    	assertTrue(pack.exists());
    	assertTrue(file.exists());
    	
        int count = pack.getParentIpsPackageFragment().getChildIpsPackageFragments().length;
        
    	new MoveOperation(new IIpsElement[] {pack}, new String[] {"test.renamedPackage"}).run(null);
    	
    	assertFalse(pack.exists());
    	assertFalse(file.exists());
    	
    	pack = root.createPackageFragment("test.renamedPackage", true, null);
    	file = ((IFolder)pack.getCorrespondingResource()).getFile("test.unknown");
    	assertTrue(pack.exists());
    	assertTrue(file.exists());
        assertEquals(count, pack.getParentIpsPackageFragment().getChildIpsPackageFragments().length);
        
    }
    
    public void testRenameEmptyFolder() throws Exception {
        IIpsPackageFragmentRoot root = content.getStandardVehicle().getIpsPackageFragment().getRoot();
        IIpsPackageFragment pack = root.createPackageFragment("empty", true, null);
        
        assertTrue(pack.exists());
        
        new MoveOperation(new IIpsElement[] {pack}, new String[] {"empty2"}).run(null);
        
        assertFalse(pack.exists());
        
        pack = root.getIpsPackageFragment("empty2");
        
        assertTrue(pack.exists());
    }
    
    public void testRenamePackageContainingOnlyPackages() throws Exception {
        IIpsPackageFragmentRoot root = content.getProject().getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment level1 = root.createPackageFragment("level1", true, null);
        
        root.createPackageFragment("level1.level2_1", true, null);
        root.createPackageFragment("level1.level2_2", true, null);
        root.createPackageFragment("level1.level2_3", true, null);
        root.createPackageFragment("level1.level2_4", true, null);
        
        super.newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "level1.level2_1.TableContent");
        
        new MoveOperation(new IIpsElement[] {level1}, new String[] {"levela"}).run(null);
        
        assertFalse(level1.exists());
        assertTrue(root.getIpsPackageFragment("levela").exists());
        
        super.newIpsObject(root, IpsObjectType.TABLE_STRUCTURE, "levela.level2_2.TableStructure");

        try {
            new MoveOperation(new IIpsElement[] {root.getIpsPackageFragment("levela")}, new String[] {"levelb"}).run(null);
            fail();
        } catch (CoreException ce) {
            // success
        }
    }
    
    public void testMovePackageInRootContainingEmptyPackage() throws Exception {
        IIpsPackageFragmentRoot root = content.getProject().getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment target = root.createPackageFragment("target", true, null);
        IIpsPackageFragment source = root.createPackageFragment("source", true, null);
        newProductCmpt(root, "source.TestProduct");
        root.createPackageFragment("source.empty", true, null);
        
        new MoveOperation(new IIpsElement[] {source}, target).run(null);
        
        assertFalse(source.exists());
        assertTrue(root.getIpsPackageFragment("target.source").exists());
        assertTrue(root.getIpsPackageFragment("target.source.empty").exists());
        assertTrue(content.getProject().findIpsObject(IpsObjectType.PRODUCT_CMPT, "target.source.TestProduct").exists());
        
    }
    
    public void testMovePackageContainingEmptyPackage() throws Exception {
        IIpsPackageFragmentRoot root = content.getProject().getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment target = root.createPackageFragment("target", true, null);
        IIpsPackageFragment source = root.createPackageFragment("parent.source", true, null);
        newProductCmpt(root, "parent.source.TestProduct");
        root.createPackageFragment("parent.source.empty", true, null);
        
        new MoveOperation(new IIpsElement[] {source}, target).run(null);
        
        assertFalse(source.exists());
        assertTrue(root.getIpsPackageFragment("parent").exists());
        assertTrue(root.getIpsPackageFragment("target.source").exists());
        assertTrue(root.getIpsPackageFragment("target.source.empty").exists());
        assertTrue(content.getProject().findIpsObject(IpsObjectType.PRODUCT_CMPT, "target.source.TestProduct").exists());
        
    }
    
}
