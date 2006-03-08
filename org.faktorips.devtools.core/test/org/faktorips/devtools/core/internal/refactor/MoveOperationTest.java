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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;

/**
 * Tests for move- and rename-operation
 * 
 * @author Thorsten Guenther
 */
public class MoveOperationTest extends IpsPluginTest {
	
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
        
        for (int i = 0; i < targetRefs.length; i++) {
//            assertFalse(targetRefs[i].getIpsObject().getIpsSrcFile().isDirty());
		}
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
        
        for (int i = 0; i < targetRefs.length; i++) {
//            assertFalse(targetRefs[i].getIpsObject().getIpsSrcFile().isDirty());
		}
    }
    
    
    
    /**
     * For this test, one object of the comfort-product of the default test content is moved to a 
     * new, non-existing package. After that, the new file is expected to be existant and the references 
     * to this object have to be the same as to the source.
     */
    public void testMoveProduct() throws CoreException, InvocationTargetException, InterruptedException {

    	IProductCmpt source = content.getStandardVehicle();
    	
    	IIpsSrcFile target = source.getIpsPackageFragment().getRoot().getIpsPackageFragment("test.new.package").getIpsSrcFile(source.getName() + ".ipsproduct");
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
        
        for (int i = 0; i < targetRefs.length; i++) {
//            assertFalse(targetRefs[i].getIpsObject().getIpsSrcFile().isDirty());
		}
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
        
        for (int i = 0; i < targetRefs.length; i++) {
//            assertFalse(targetRefs[i].getIpsObject().getIpsSrcFile().isDirty());
		}
    }
    
    /**
     * Try to rename a table content which should lead to an exception.
     */
    public void testRenameTableContent() throws CoreException, InvocationTargetException, InterruptedException {
    	IIpsSrcFile file = content.getStandardVehicle().getIpsPackageFragment().createIpsFile(IpsObjectType.TABLE_CONTENTS, "table", true, null);
    	
    	assertTrue(file.exists());
    	
    	try {
    		new MoveOperation(new IIpsElement[] {file.getIpsObject()}, new String[] {"newTable"});
    		fail();
    	}
    	catch (CoreException e) {
    		// nothing to do
    	}
    }
}
