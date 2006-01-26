package org.faktorips.devtools.core.internal.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptRelationTest extends IpsPluginTest {

	private IIpsSrcFile ipsSrcFile;
    private ProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IProductCmptRelation relation;
    private IPolicyCmptType policyCmptType;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
    	super.setUp();
    	IIpsProject ipsProject = newIpsProject("TestProject");
    	policyCmptType = (IPolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy");
    	productCmpt = (ProductCmpt)newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT, "TestProduct");
    	productCmpt.setPolicyCmptType(policyCmptType.getQualifiedName());
    	generation = (IProductCmptGeneration)productCmpt.newGeneration();
    	relation = generation.newRelation("coverage");
    	ipsSrcFile = productCmpt.getIpsSrcFile();
    }
    
    public void testGetProductCmptTypeRelation() throws CoreException {
    	assertNull(relation.getProductCmptTypeRelation());
    	IRelation policyCmptTypeRelation = policyCmptType.newRelation();
    	policyCmptTypeRelation.setTargetRoleSingular("coverage");
    	
    	IProductCmptTypeRelation productCmptTypeRelation = policyCmptType.findProductCmptType().getRelations()[0];
    	assertEquals(productCmptTypeRelation.getName(), relation.getProductCmptTypeRelation());
    }

    public void testRemove() {
        relation.delete();
        assertEquals(0, generation.getNumOfRelations());
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testSetTarget() {
        relation.setTarget("newTarget");
        assertEquals("newTarget", relation.getTarget());
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testToXml() {
        relation = generation.newRelation("coverage");
        relation.setTarget("newTarget");
        relation.setMinCardinality(2);
        relation.setMaxCardinality("3");
        Element element = relation.toXml(newDocument());
        
        IProductCmptRelation copy = new ProductCmptRelation();
        copy.initFromXml(element);
        assertEquals(1, copy.getId());
        assertEquals("newTarget", copy.getTarget());
        assertEquals("coverage", copy.getPcTypeRelation());
        assertEquals(2, copy.getMinCardinality());
        assertEquals("3", copy.getMaxCardinality());
    }

    public void testInitFromXml() {
        relation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(42, relation.getId());
        assertEquals("FullCoverage", relation.getPcTypeRelation());
        assertEquals("FullCoveragePlus", relation.getTarget());
        assertEquals(2, relation.getMinCardinality());
        assertEquals("3", relation.getMaxCardinality());
    }

}
