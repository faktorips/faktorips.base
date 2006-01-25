package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.internal.model.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

public class ProductCmptTypeTest extends IpsPluginTest {

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
		relations = productCmptType.getRelations();
		assertEquals(1, relations.length);
		assertEquals("CoveragePk", relations[0].getTargetRoleSingular());
		
		IRelation r1 = policyCmptType.newRelation();
		r1.setProductRelevant(false);
		relations = productCmptType.getRelations();
		assertEquals(1, relations.length);
		
		r1.setProductRelevant(true);
		r1.setTargetRoleSingular("Benefit");
		relations = productCmptType.getRelations();
		assertEquals(2, relations.length);
		assertEquals("CoveragePk", relations[0].getTargetRoleSingular());
		assertEquals("BenefitPk", relations[1].getTargetRoleSingular());
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
	public void testGetQualifiedName() {
		assertEquals("motor.MotorProduct", productCmptType.getQualifiedName());
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

}
