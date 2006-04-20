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

import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptStructureTest extends IpsPluginTest {
    
    private IProductCmpt productCmpt;
    private IProductCmpt productCmptTarget;
    private IIpsPackageFragmentRoot root;
    private IIpsProject pdProject;
    private IProductCmptStructure structure;
    
    /*
     * @see PluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        pdProject = this.newIpsProject("TestProject");
        root = pdProject.getIpsPackageFragmentRoots()[0];

        // Build policy component types
        IPolicyCmptType policyCmptType = (IPolicyCmptType)newIpsObject(root, IpsObjectType.POLICY_CMPT_TYPE, "policy.TestPolicy");
        policyCmptType.setUnqualifiedProductCmptType("dummy1");

        IPolicyCmptType policyCmptTypeTarget = (IPolicyCmptType)newIpsObject(root, IpsObjectType.POLICY_CMPT_TYPE, "policy.TestTargetPolicy");
        policyCmptType.setUnqualifiedProductCmptType("dummy2");
        
        IRelation relation = policyCmptType.newRelation();
        relation.setTargetRoleSingularProductSide("TestRelation");
        relation.setTargetRoleSingular("TestRelation");
        relation.setTarget(policyCmptTypeTarget.getQualifiedName());
        relation.setProductRelevant(true);
        
        // Build product component types
        productCmpt = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "products.TestProduct");
        productCmpt.setPolicyCmptType(policyCmptType.getQualifiedName());
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        generation.setValidFrom(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());

        productCmptTarget = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "products.TestProductTarget");
        productCmptTarget.setPolicyCmptType(policyCmptTypeTarget.getQualifiedName());
        IProductCmptGeneration targetGen = (IProductCmptGeneration)productCmptTarget.newGeneration();
        targetGen.setValidFrom(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
        
        IProductCmptRelation cmptRelation = generation.newRelation(relation.getName());
        cmptRelation.setTarget(productCmptTarget.getQualifiedName());
        
        cmptRelation = generation.newRelation(relation.getName());
        cmptRelation.setTarget(productCmptTarget.getQualifiedName());
        
        policyCmptType.getIpsSrcFile().save(true, null);
        policyCmptTypeTarget.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);
        productCmptTarget.getIpsSrcFile().save(true, null);
        
        structure = productCmpt.getStructure();
    }

    public void testGetRoot() {
    	IProductCmpt root = structure.getRoot();
    	assertSame(productCmpt, root);
    }
    
    public void testNoGeneration() throws CycleException {
    	productCmpt.getGenerations()[0].delete();
    	structure.refresh();
    }
    
    public void testCircleDetection() throws Exception {
    	DefaultTestContent content = new DefaultTestContent();
    	
    	// this has to work without any exception
    	content.getComfortMotorProduct().getStructure();
    	
    	// create a circle
    	IProductCmpt basic = content.getBasicMotorProduct();
    	IProductCmpt comfort = content.getComfortMotorProduct(); 
    	IProductCmptRelation rel = ((IProductCmptGeneration)basic.getGenerations()[0]).newRelation("VehicleType");
    	rel.setTarget(comfort.getQualifiedName());
    	rel = ((IProductCmptGeneration)comfort.getGenerations()[0]).newRelation("VehicleType");
    	rel.setTarget(basic.getQualifiedName());
    	
    	try {
			content.getComfortMotorProduct().getStructure();
			fail();
		} catch (CycleException e) {
			// success
		} 
    }
}
