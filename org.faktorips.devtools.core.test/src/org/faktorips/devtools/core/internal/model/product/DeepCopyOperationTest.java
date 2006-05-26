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
import java.util.GregorianCalendar;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.model.product.IProductCmptStructure.IStructureNode;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
public class DeepCopyOperationTest extends AbstractIpsPluginTest {
	
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
     * @throws CycleException 
     */
    public void testCopyAll() throws CoreException, InvocationTargetException, InterruptedException, CycleException {
    	IStructureNode[] toCopy = new IStructureNode[5];
    	int count = 0;

    	IProductCmptStructure structure = content.getComfortMotorProduct().getStructure();
    	IStructureNode node = structure.getRootNode();
    	IStructureNode[] children = node.getChildren();
    	for (int i = 0; i < children.length; i++) {
			IStructureNode[] children2 = children[i].getChildren();
			for (int j = 0; j < children2.length; j++) {
				toCopy[count] = children2[j];
				count++;
			}
		}
    	toCopy[count] = node;
    	
        Hashtable handles = new Hashtable();

        for (int i = 0; i < toCopy.length; i++) {
        	IProductCmpt cmpt = (IProductCmpt)toCopy[i].getWrappedElement();
        	handles.put(toCopy[i], cmpt.getIpsPackageFragment().getIpsSrcFile("DeepCopyOf" + cmpt.getName() + ".ipsproduct"));
        	assertFalse(((IIpsSrcFile)handles.get(toCopy[i])).exists());
		}
        
        DeepCopyOperation dco = new DeepCopyOperation(toCopy, new IStructureNode[0], handles);
        dco.run(null);
        
        for (int i = 0; i < toCopy.length; i++) {
        	IIpsSrcFile src = (IIpsSrcFile)handles.get(toCopy[i]);
        	assertTrue(src.exists());

        	// we have a race condition, because files are written async. So loop for some times...
        	count = 0;
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
     * @throws CycleException 
     */
    public void testCopySome() throws CoreException, InvocationTargetException, InterruptedException, CycleException {
    	IStructureNode[] toCopy = new IStructureNode[3];
    	IStructureNode[] toRefer = new IStructureNode[2];
    	int copyCount = 0;
    	int refCount = 0;

    	IProductCmptStructure structure = content.getComfortMotorProduct().getStructure();
    	IStructureNode node = structure.getRootNode();
    	IStructureNode[] children = node.getChildren();
    	for (int i = 0; i < children.length; i++) {
			IStructureNode[] children2 = children[i].getChildren();
			for (int j = 0; j < children2.length; j++) {
				if (children2[j].getWrappedElement().equals(
						content.getComfortMotorProduct())
						|| children2[j].getWrappedElement().equals(
								content.getStandardVehicle())
						|| children2[j].getWrappedElement().equals(
								content.getComfortCollisionCoverageA())) {
					toCopy[copyCount] = children2[j];
					copyCount++;
				} else if (children2[j].getWrappedElement().equals(
						content.getComfortCollisionCoverageB()) || children2[j].getWrappedElement().equals(
								content.getStandardTplCoverage())) {
					toRefer[refCount] = children2[j];
					refCount ++;
				}
			}
		}
    	toCopy[copyCount] = node;

    	Hashtable handles = new Hashtable();

        for (int i = 0; i < toCopy.length; i++) {
        	IProductCmpt cmpt = (IProductCmpt)toCopy[i].getWrappedElement();
        	handles.put(toCopy[i], cmpt.getIpsPackageFragment().getIpsSrcFile("DeepCopyOf" + cmpt.getName() + ".ipsproduct"));
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
        
        IProductCmpt base = (IProductCmpt)((IIpsSrcFile)handles.get(toCopy[copyCount])).getIpsObject();
        IProductCmptGeneration gen = (IProductCmptGeneration)base.getGenerations()[0];
        IProductCmptRelation[] rels = gen.getRelations("TplCoverageType");
        assertEquals(1, rels.length);
        assertEquals("products.StandardTplCoverage", rels[0].getName());
        
        rels = gen.getRelations("VehicleType");
        assertEquals(1, rels.length);
        assertEquals("products.DeepCopyOfStandardVehicle", rels[0].getName());
    }
    
    public void testCopyWithNoGeneration() throws Exception {
        IStructureNode[] toCopy = new IStructureNode[5];
        int count = 0;

        IProductCmptStructure structure = content.getComfortMotorProduct().getStructure();
        IStructureNode node = structure.getRootNode();
        IStructureNode[] children = node.getChildren();
        for (int i = 0; i < children.length; i++) {
            IStructureNode[] children2 = children[i].getChildren();
            for (int j = 0; j < children2.length; j++) {
                toCopy[count] = children2[j];
                count++;
            }
        }
        toCopy[count] = node;
        
        Hashtable handles = new Hashtable();

        for (int i = 0; i < toCopy.length; i++) {
            IProductCmpt cmpt = (IProductCmpt)toCopy[i].getWrappedElement();
            handles.put(toCopy[i], cmpt.getIpsPackageFragment().getIpsSrcFile("DeepCopy2Of" + cmpt.getName() + ".ipsproduct"));
            assertFalse(((IIpsSrcFile)handles.get(toCopy[i])).exists());
        }
        
        IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(new GregorianCalendar(1990, 1, 1));
        
        DeepCopyOperation dco = new DeepCopyOperation(toCopy, new IStructureNode[0], handles);
        dco.run(null);
        
        for (int i = 0; i < toCopy.length; i++) {
            IIpsSrcFile src = (IIpsSrcFile)handles.get(toCopy[i]);
            assertTrue(src.exists());

            // we have a race condition, because files are written async. So loop for some times...
            count = 0;
            if (src.isDirty() && count < 100) {
                count ++;
            }
            
            assertFalse(src.isDirty());
        }
        
    }
}
