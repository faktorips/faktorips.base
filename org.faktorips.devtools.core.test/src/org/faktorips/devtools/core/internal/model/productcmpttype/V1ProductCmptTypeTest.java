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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;

public class V1ProductCmptTypeTest extends AbstractIpsPluginTest {

	private IIpsProject ipsProject;
	private IPolicyCmptType policyCmptType;
	private IProductCmptType productCmptType;
	private IIpsPackageFragment pack;
	private IIpsSrcFile srcFile;
	
	protected void setUp() throws Exception {
		super.setUp();
		ipsProject = newIpsProject("TestProject");
		policyCmptType = (IPolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "motor.MotorPolicy");
		productCmptType = new ProductCmptType((PolicyCmptType)policyCmptType);
		policyCmptType.setUnqualifiedProductCmptType("MotorProduct");
		policyCmptType.getIpsSrcFile().save(true, null);
		pack = policyCmptType.getIpsPackageFragment();
		srcFile = policyCmptType.getIpsSrcFile();
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.findPolicyCmptyType()'
	 */
	public void testFindPolicyCmptyType() throws CoreException {
		assertEquals(policyCmptType, productCmptType.findPolicyCmptyType());
	}

	public void testFindSupertype() throws CoreException {
		assertNull(productCmptType.findSupertype());
		
        policyCmptType.setSupertype("unknownSupertype");
		assertNull(productCmptType.findSupertype());
		
		IPolicyCmptType superPolicyCmptType = newPolicyCmptType(ipsProject, "Policy");
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());
		superPolicyCmptType.setUnqualifiedProductCmptType("");
        assertNull(productCmptType.findSupertype());
        
        superPolicyCmptType.setUnqualifiedProductCmptType("Product");
		assertEquals(superPolicyCmptType.findProductCmptType(), productCmptType.findSupertype());
        
        superPolicyCmptType.setConfigurableByProductCmptType(false);
        assertNull(productCmptType.findSupertype());
	}
	
	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getRelations()'
	 */
	public void testGetRelations() {
		IProductCmptTypeRelation[] relations = productCmptType.getRelations();
		assertEquals(0, relations.length);
		
		IRelation r0 = policyCmptType.newRelation();
		r0.setProductRelevant(false);
		relations = productCmptType.getRelations();
		assertEquals(0, relations.length);
		
		r0.setProductRelevant(true);
		r0.setTargetRoleSingular("Coverage");
		r0.setTargetRoleSingularProductSide("CoverageType");
		relations = productCmptType.getRelations();
		assertEquals(1, relations.length);
		assertEquals("CoverageType", relations[0].getTargetRoleSingular());
		
		IRelation r1 = policyCmptType.newRelation();
		r1.setProductRelevant(false);
		relations = productCmptType.getRelations();
		assertEquals(1, relations.length);
		
		r1.setProductRelevant(true);
		r1.setTargetRoleSingular("Benefit");
		r1.setTargetRoleSingularProductSide("BenefitType");
		relations = productCmptType.getRelations();
		assertEquals(2, relations.length);
		assertEquals("CoverageType", relations[0].getTargetRoleSingular());
		assertEquals("BenefitType", relations[1].getTargetRoleSingular());
	}
    
    public void testFindRelationInHierarchy() throws CoreException {
        assertNull(productCmptType.findRelationInHierarchy(null));
        
        IRelation r0 = policyCmptType.newRelation();
        r0.setProductRelevant(false);
        r0.setTargetRoleSingular("Coverage");
        r0.setTargetRoleSingularProductSide("CoverageType");
        assertNull(productCmptType.findRelationInHierarchy("CoverageType"));
        assertNull(productCmptType.findRelationInHierarchy("UnknownRelation"));
        
        r0.setProductRelevant(true);
        IProductCmptTypeRelation productRelation = productCmptType.getRelations()[0];
        assertEquals(productRelation, productCmptType.findRelationInHierarchy("CoverageType"));
        
        IPolicyCmptType subPolicyCmptType = (IPolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "motor.SubMotorPolicy");
        subPolicyCmptType.setSupertype(policyCmptType.getQualifiedName());
        IProductCmptType subProductCmptType = new ProductCmptType((PolicyCmptType)subPolicyCmptType);
        assertEquals(productRelation, subProductCmptType.findRelationInHierarchy("CoverageType"));
        assertNull(subProductCmptType.findRelationInHierarchy("UnknownRelation"));
        
        IPolicyCmptType superPolicyCmptType = (IPolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "motor.SuperMotorPolicy");
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());
        assertEquals(productRelation, subProductCmptType.findRelationInHierarchy("CoverageType"));
        assertNull(subProductCmptType.findRelationInHierarchy("UnknownRelation"));
    }
    
	public void testGetAttributes() {
		IAttribute[] attributes = productCmptType.getAttributes();
		assertEquals(0, attributes.length);
		
		IAttribute a0 = policyCmptType.newAttribute();
		a0.setProductRelevant(false);
		attributes = productCmptType.getAttributes();
		assertEquals(0, attributes.length);

		a0.setProductRelevant(true);
		a0.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
		attributes = productCmptType.getAttributes();
		assertEquals(0, attributes.length);
		
		a0.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
		attributes = productCmptType.getAttributes();
		assertEquals(0, attributes.length);
		
		a0.setAttributeType(AttributeType.CHANGEABLE);
		attributes = productCmptType.getAttributes();
		assertEquals(1, attributes.length);
		assertEquals(a0, attributes[0]);

		a0.setAttributeType(AttributeType.CONSTANT);
		attributes = productCmptType.getAttributes();
		assertEquals(1, attributes.length);
		assertEquals(a0, attributes[0]);

		IAttribute a1 = policyCmptType.newAttribute();
		a1.setProductRelevant(false);
		attributes = productCmptType.getAttributes();
		assertEquals(1, attributes.length);
		
		a1.setProductRelevant(true);
		attributes = productCmptType.getAttributes();
		assertEquals(2, attributes.length);
		assertEquals(a0, attributes[0]);
		assertEquals(a1, attributes[1]);
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getPolicyCmptyType()'
	 */
	public void testGetPolicyCmptyType() {
		assertEquals(policyCmptType.getQualifiedName(), productCmptType.getPolicyCmptyType());
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getIpsObjectType()'
	 */
	public void testGetIpsObjectType() {
		assertEquals(IpsObjectType.PRODUCT_CMPT_TYPE, productCmptType.getIpsObjectType());
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getIpsSrcFile()'
	 */
	public void testGetIpsSrcFile() {
		assertEquals(srcFile, productCmptType.getIpsSrcFile());
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getQualifiedName()'
	 */
	public void testGetQualifiedName() throws Exception {
		assertEquals("motor.MotorProduct", productCmptType.getQualifiedName());
		
		PolicyCmptType pct = super.newPolicyCmptType(ipsProject, "Test");
		pct.setUnqualifiedProductCmptType("TestProductType");
		pct.setConfigurableByProductCmptType(true);
		
		IProductCmptType type = pct.findProductCmptType();
		
		assertEquals("TestProductType", type.getQualifiedName());
		
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getQualifiedNameType()'
	 */
	public void testGetQualifiedNameType() {
		assertEquals(new QualifiedNameType("motor.MotorProduct", IpsObjectType.PRODUCT_CMPT_TYPE), productCmptType.getQualifiedNameType());

	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getIpsPackageFragment()'
	 */
	public void testGetIpsPackageFragment() {
		assertEquals(pack, productCmptType.getIpsPackageFragment());
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getName()'
	 */
	public void testGetName() {
		assertEquals("MotorProduct", productCmptType.getName());
	}	

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getIpsModel()'
	 */
	public void testGetIpsModel() {
		assertNotNull(productCmptType.getIpsModel());
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getIpsProject()'
	 */
	public void testGetIpsProject() {
		assertEquals(ipsProject, productCmptType.getIpsProject());
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getCorrespondingResource()'
	 */
	public void testGetCorrespondingResource() {
		assertNull(productCmptType.getCorrespondingResource());
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getEnclosingResource()'
	 */
	public void testGetEnclosingResource() {
		assertEquals(srcFile.getCorrespondingResource(), productCmptType.getEnclosingResource());
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getParent()'
	 */
	public void testGetParent() {
		assertEquals(srcFile, productCmptType.getParent());
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.getChildren()'
	 */
	public void testGetChildren() throws CoreException {
		IIpsElement[] children = productCmptType.getChildren();
		assertEquals(0, children.length);
		
		IRelation r0 = policyCmptType.newRelation();
		r0.setProductRelevant(true);
		children = productCmptType.getChildren();
		assertEquals(1, children.length);
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType.hasChildren()'
	 */
	public void testHasChildren() throws CoreException {
		assertFalse(productCmptType.hasChildren());
		
		IRelation r0 = policyCmptType.newRelation();
		r0.setProductRelevant(true);
		assertTrue(productCmptType.hasChildren());
	}

    /**
     * Tests for the correct type of excetion to be thrown - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			productCmptType.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    public void testNewGetTableStructureUsage(){
        assertEquals(0, productCmptType.getTableStructureUsages().length);
        ITableStructureUsage tableStructureUsage1 = productCmptType.newTableStructureUsage();
        tableStructureUsage1.setRoleName("Test1");
        ITableStructureUsage tableStructureUsage2 = productCmptType.newTableStructureUsage();
        tableStructureUsage2.setRoleName("Test2");
        assertEquals(2, productCmptType.getTableStructureUsages().length);
        assertEquals(tableStructureUsage1, productCmptType.getTableStructureUsage("Test1"));
        assertEquals(tableStructureUsage2, productCmptType.getTableStructureUsage("Test2"));
    }
}
