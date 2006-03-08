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

import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

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
        generation.setValidFrom(IpsPreferences.getWorkingDate());

        productCmptTarget = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "products.TestProductTarget");
        productCmptTarget.setPolicyCmptType(policyCmptTypeTarget.getQualifiedName());
        IProductCmptGeneration targetGen = (IProductCmptGeneration)productCmptTarget.newGeneration();
        targetGen.setValidFrom(IpsPreferences.getWorkingDate());
        
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
    
    public void testGetChildren() {
    	Object[] children = structure.getChildren(productCmpt);
    	
    	assertEquals(1, children.length);
    	assertTrue(children[0] instanceof IProductCmptTypeRelation);
    	
    	children = structure.getChildren((IProductCmptTypeRelation)children[0]);
    	
    	assertEquals(2, children.length);
    	assertTrue(children[0] instanceof IProductCmpt);
    }
    
    public void testGetParent() {
    	Object[] children = structure.getChildren(productCmpt);
    	
    	Object parent = structure.getParent((IProductCmptTypeRelation)children[0]);
    	assertSame(parent, productCmpt);
    }
    
    public void testGetTargets() {
    	Object[] relations = structure.getChildren(productCmpt);
    	IProductCmpt[] targets = structure.getTargets((IProductCmptTypeRelation)relations[0], productCmpt);
    	
    	assertEquals(2, targets.length);
    }
    
    public void testGetRelationTypes() {
    	Object[] relationtypes = structure.getRelationTypes(productCmpt);
    	
    	assertEquals(1, relationtypes.length);
    }
    
    public void testNoGeneration() {
    	productCmpt.getGenerations()[0].delete();
    	structure.refresh();
    }
}
