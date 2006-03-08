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

package org.faktorips.devtools.core.internal.model.product;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
public class DeepCopyOperationTest extends IpsPluginTest {
	
	DefaultTestContent content;
	
	/**
	 * {@inheritDoc}
	 */
    protected void setUp() throws Exception {
        super.setUp();
        content = new DefaultTestContent();
        
    }
   
    /**
     * For this test, the comfort-product of the default test content is copied completely. After that, the new 
     * files are expected to be existant and not dirty.
     */
    public void testCopyAll() throws CoreException, InvocationTargetException, InterruptedException {
        IProductCmpt[] toCopy = new IProductCmpt[5];
        
        toCopy[0] = content.getComfortMotorProduct();
        toCopy[1] = content.getStandardVehicle();
        toCopy[2] = content.getComfortCollisionCoverageA();
        toCopy[3] = content.getComfortCollisionCoverageB();
        toCopy[4] = content.getStandardTplCoverage();
        
        Hashtable handles = new Hashtable();

        for (int i = 0; i < toCopy.length; i++) {
        	handles.put(toCopy[i], toCopy[i].getIpsPackageFragment().getIpsSrcFile("DeepCopyOf" + toCopy[i].getName() + ".ipsproduct"));
        	assertFalse(((IIpsSrcFile)handles.get(toCopy[i])).exists());
		}
        
        DeepCopyOperation dco = new DeepCopyOperation(toCopy, new IProductCmpt[0], handles);
        dco.run(null);
        
        for (int i = 0; i < toCopy.length; i++) {
        	IIpsSrcFile src = (IIpsSrcFile)handles.get(toCopy[i]);
        	assertTrue(src.exists());

        	// we have a race condition, because files are written async. So loop for some times...
        	int count = 0;
        	if (src.isDirty() && count < 100) {
        		count ++;
        	}
        	
        	assertFalse(src.isDirty());
		}
    }
    
    /**
     * For this test, the comfort-product of the default test content is copied only in part. After that, the new 
     * files are expected to be existant and not dirty. Some relations from the new objects link now the the not 
     * copied old objects.
     */
    public void testCopySome() throws CoreException, InvocationTargetException, InterruptedException {
        IProductCmpt[] toCopy = new IProductCmpt[3];        
        toCopy[0] = content.getComfortMotorProduct();
        toCopy[1] = content.getStandardVehicle();
        toCopy[2] = content.getComfortCollisionCoverageA();
        
        IProductCmpt[] toRefer = new IProductCmpt[2];
        toRefer[0] = content.getComfortCollisionCoverageB();
        toRefer[1] = content.getStandardTplCoverage();
        
        Hashtable handles = new Hashtable();

        for (int i = 0; i < toCopy.length; i++) {
        	handles.put(toCopy[i], toCopy[i].getIpsPackageFragment().getIpsSrcFile("DeepCopyOf" + toCopy[i].getName() + ".ipsproduct"));
        	assertFalse(((IIpsSrcFile)handles.get(toCopy[i])).exists());
		}
        
        DeepCopyOperation dco = new DeepCopyOperation(toCopy, toRefer, handles);
        dco.run(null);
        for (int i = 0; i < toCopy.length; i++) {
        	IIpsSrcFile src = (IIpsSrcFile)handles.get(toCopy[i]);
        	assertTrue(src.exists());

        	// we have a race condition, because files are written async. So loop for some times...
        	int count = 0;
        	if (src.isDirty() && count < 100) {
        		count ++;
        	}
        	assertFalse(src.isDirty());
		}
        
        IProductCmpt base = (IProductCmpt)((IIpsSrcFile)handles.get(toCopy[0])).getIpsObject();
        IProductCmptGeneration gen = (IProductCmptGeneration)base.getGenerations()[0];
        IProductCmptRelation[] rels = gen.getRelations("TplCoverageType");
        assertEquals(1, rels.length);
        assertEquals("products.StandardTplCoverage", rels[0].getName());
        
        rels = gen.getRelations("VehicleType");
        assertEquals(1, rels.length);
        assertEquals("products.DeepCopyOfStandardVehicle", rels[0].getName());
    }
}
