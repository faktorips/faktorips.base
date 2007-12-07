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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.GregorianCalendar;
import java.util.Hashtable;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
public class DeepCopyOperationTest extends AbstractIpsPluginTest {
	
    private IIpsProject ipsProject;
	private IProductCmpt product;
	
	/**
	 * {@inheritDoc}
	 */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IPolicyCmptType pctype = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        product = newProductCmpt(pctype.findProductCmptType(ipsProject), "Product");
    }
   
    // Auskommentiert bis zur Umstellung auf neues Metamodell. Siehe Flyspray-Eintrag 939
    
//    /**
//     * For this test, the comfort-product of the default test content is copied completely. After that, the new 
//     * files are expected to be existant and not dirty.
//     * @throws CycleException 
//     */
//    public void testCopyAll() throws CoreException, InvocationTargetException, InterruptedException, CycleException {
//    	IProductCmptStructure structure = content.getComfortMotorProduct().getStructure();
//        IProductCmptReference[] toCopy = (IProductCmptReference[])structure.toArray(true);
//
//        Hashtable handles = new Hashtable();
//
//        for (int i = 0; i < toCopy.length; i++) {
//        	IProductCmpt cmpt = toCopy[i].getProductCmpt();
//        	handles.put(toCopy[i], cmpt.getIpsPackageFragment().getIpsSrcFile("DeepCopyOf" + cmpt.getName() + ".ipsproduct"));
//        	assertFalse(((IIpsSrcFile)handles.get(toCopy[i])).exists());
//		}
//        
//        DeepCopyOperation dco = new DeepCopyOperation(toCopy, new IProductCmptReference[0], handles);
//        dco.run(null);
//        
//        for (int i = 0; i < toCopy.length; i++) {
//        	IIpsSrcFile src = (IIpsSrcFile)handles.get(toCopy[i]);
//        	assertTrue(src.exists());
//
//        	// we have a race condition, because files are written async. So loop for some times...
//        	int count = 0;
//        	if (src.isDirty() && count < 100) {
//        		count ++;
//        	}
//        	
//        	assertFalse(src.isDirty());
//		}
//    }
//    
//    /**
//     * For this test, the comfort-product of the default test content is copied only in part. After that, the new 
//     * files are expected to be existant and not dirty. Some relations from the new objects link now the the not 
//     * copied old objects.
//     * @throws CycleException 
//     */
//    public void testCopySome() throws CoreException, InvocationTargetException, InterruptedException, CycleException {
//    	IProductCmptReference[] toCopy = new IProductCmptReference[3];
//        IProductCmptReference[] toRefer = new IProductCmptReference[2];
//    	int copyCount = 0;
//    	int refCount = 0;
//
//    	IProductCmptStructure structure = content.getComfortMotorProduct().getStructure();
//        IProductCmptReference node = structure.getRoot();
//        IProductCmptReference[] children = structure.getChildProductCmptReferences(node);
//    	for (int i = 0; i < children.length; i++) {
//            if (children[i].getProductCmpt().equals(content.getComfortMotorProduct())
//                    || children[i].getProductCmpt().equals(content.getStandardVehicle())
//                    || children[i].getProductCmpt().equals(content.getComfortCollisionCoverageA())) {
//                toCopy[copyCount] = children[i];
//                copyCount++;
//            } else if (children[i].getProductCmpt().equals(content.getComfortCollisionCoverageB())
//                    || children[i].getProductCmpt().equals(content.getStandardTplCoverage())) {
//                toRefer[refCount] = children[i];
//                refCount++;
//            }
//        }
//    	toCopy[copyCount] = node;
//
//    	Hashtable handles = new Hashtable();
//
//        for (int i = 0; i < toCopy.length; i++) {
//        	IProductCmpt cmpt = toCopy[i].getProductCmpt();
//        	handles.put(toCopy[i], cmpt.getIpsPackageFragment().getIpsSrcFile("DeepCopyOf" + cmpt.getName() + ".ipsproduct"));
//        	assertFalse(((IIpsSrcFile)handles.get(toCopy[i])).exists());
//		}
//        
//        DeepCopyOperation dco = new DeepCopyOperation(toCopy, toRefer, handles);
//        dco.run(null);
//        for (int i = 0; i < toCopy.length; i++) {
//        	IIpsSrcFile src = (IIpsSrcFile)handles.get(toCopy[i]);
//        	assertTrue(src.exists());
//
//        	// we have a race condition, because files are written async. So loop for some times...
//        	int count = 0;
//        	if (src.isDirty() && count < 100) {
//        		count ++;
//        	}
//        	assertFalse(src.isDirty());
//		}
//        
//        IProductCmpt base = (IProductCmpt)((IIpsSrcFile)handles.get(toCopy[copyCount])).getIpsObject();
//        IProductCmptGeneration gen = (IProductCmptGeneration)base.getGenerations()[0];
//        IProductCmptRelation[] rels = gen.getRelations("TplCoverageType");
//        assertEquals(1, rels.length);
//        assertEquals("products.StandardTplCoverage", rels[0].getName());
//        
//        rels = gen.getRelations("VehicleType");
//        assertEquals(1, rels.length);
//        assertEquals("products.DeepCopyOfStandardVehicle", rels[0].getName());
//    }
//    
    public void testCopyWithNoGeneration() throws Exception {
        product = newProductCmpt(ipsProject, "EmptyProduct");
        IProductCmptTreeStructure structure = product.getStructure(ipsProject);
        IProductCmptReference[] toCopy = (IProductCmptReference[])structure.toArray(true);
        
        Hashtable handles = new Hashtable();

        for (int i = 0; i < toCopy.length; i++) {
            IProductCmpt cmpt = toCopy[i].getProductCmpt();
            handles.put(toCopy[i], cmpt.getIpsPackageFragment().getIpsSrcFile("DeepCopy2Of" + cmpt.getName(), IpsObjectType.PRODUCT_CMPT));
            assertFalse(((IIpsSrcFile)handles.get(toCopy[i])).exists());
        }
        
        IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(new GregorianCalendar(1990, 1, 1));
        
        DeepCopyOperation dco = new DeepCopyOperation(toCopy, new IProductCmptReference[0], handles);
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
}
