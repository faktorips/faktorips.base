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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptRelationTest extends AbstractIpsPluginTest {

	private IIpsSrcFile ipsSrcFile;
    private ProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IProductCmptRelation relation;
    private IPolicyCmptType policyCmptType;
    private IIpsProject ipsProject;
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
    	super.setUp();
    	ipsProject = newIpsProject("TestProject");
    	policyCmptType = (IPolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy");
    	productCmpt = (ProductCmpt)newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT, "TestProduct");
    	productCmpt.setPolicyCmptType(policyCmptType.getQualifiedName());
    	generation = (IProductCmptGeneration)productCmpt.newGeneration();
    	relation = generation.newRelation("CoverageType");
    	ipsSrcFile = productCmpt.getIpsSrcFile();
    }
    
    public void testGetProductCmptTypeRelation() throws CoreException {
    	assertEquals("CoverageType", relation.getProductCmptTypeRelation());
    }

    public void testFindProductCmptTypeRelation() throws CoreException {
    	IRelation policyCmptTypeRelation = policyCmptType.newRelation();
    	policyCmptTypeRelation.setTargetRoleSingular("Coverage");
    	policyCmptTypeRelation.setTargetRoleSingularProductSide("CoverageType");
    	
    	IProductCmptTypeRelation productCmptTypeRelation = policyCmptType.findProductCmptType().getRelations()[0];
    	assertEquals(productCmptTypeRelation, relation.findProductCmptTypeRelation());
    	
    	policyCmptTypeRelation.setTargetRoleSingularProductSide("blabla");
    	assertNull(relation.findProductCmptTypeRelation());
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
        relation.setMaxCardinality(3);
        Element element = relation.toXml(newDocument());
        
        IProductCmptRelation copy = new ProductCmptRelation();
        copy.initFromXml(element);
        assertEquals(1, copy.getId());
        assertEquals("newTarget", copy.getTarget());
        assertEquals("coverage", copy.getProductCmptTypeRelation());
        assertEquals(2, copy.getMinCardinality());
        assertEquals(3, copy.getMaxCardinality());
        
        relation.setMaxCardinality(Integer.MAX_VALUE);
        element = relation.toXml(newDocument());
        copy.initFromXml(element);
        assertEquals(Integer.MAX_VALUE, copy.getMaxCardinality());
    }

    public void testInitFromXml() {
        relation.initFromXml((Element)getTestDocument().getDocumentElement().getElementsByTagName("Relation").item(0));
        assertEquals(42, relation.getId());
        assertEquals("FullCoverage", relation.getProductCmptTypeRelation());
        assertEquals("FullCoveragePlus", relation.getTarget());
        assertEquals(2, relation.getMinCardinality());
        assertEquals(3, relation.getMaxCardinality());

        relation.initFromXml((Element)getTestDocument().getDocumentElement().getElementsByTagName("Relation").item(1));
        assertEquals(43, relation.getId());
        assertEquals(1, relation.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, relation.getMaxCardinality());
    }

    /**
     * Tests for the correct type of excetion to be thrown - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			relation.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
 
    public void testValidate() throws CoreException {
    	MessageList ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_UNKNWON_RELATIONTYPE));
    	
    	IRelation rel = policyCmptType.newRelation();
    	rel.setTargetRoleSingularProductSide("CoverageType");

    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_UNKNWON_RELATIONTYPE));
    	
    	relation.setTarget("unknown");
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_UNKNWON_TARGET));

    	relation.setTarget(productCmpt.getQualifiedName());
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_UNKNWON_TARGET));
    	
    	relation.setMaxCardinality(0);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_MISSING_MAX_CARDINALITY));
    	assertNotNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1));

    	relation.setMaxCardinality(1);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1));
    	
    	relation.setMinCardinality(2);
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN));
    	
    	relation.setMaxCardinality(3);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN));
    	
    	rel.setMaxCardinality(1);
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX));

    	rel.setMaxCardinality(3);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX));
    }
    
    public void testValidateInvalidTarget() throws Exception{
        IPolicyCmptType targetType = newPolicyCmptType(ipsProject, "target.TargetPolicy");
        IProductCmpt target = newProductCmpt(ipsProject, "target.Target");
        target.setPolicyCmptType(targetType.getQualifiedName());
        IRelation rel = policyCmptType.newRelation();
        rel.setTarget(targetType.getQualifiedName());
        rel.setTargetRoleSingular("testRelation");
        rel.setTargetRoleSingularProductSide("testRelation");
        
        IProductCmptRelation relation = generation.newRelation(rel.getName());
        relation.setTarget(productCmpt.getQualifiedName());
        
        MessageList ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_INVALID_TARGET));
        
        relation.setTarget(target.getQualifiedName());
        
        ml = relation.validate();
        assertNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_INVALID_TARGET));
    }

    public void testIsMandatory(){
        relation.setMinCardinality(0);
        relation.setMaxCardinality(1);
        assertFalse(relation.isMandatory());
        
        relation.setMinCardinality(1);
        relation.setMaxCardinality(1);
        assertTrue(relation.isMandatory());

        relation.setMinCardinality(2);
        relation.setMaxCardinality(3);
        assertFalse(relation.isMandatory());

        relation.setMinCardinality(3);
        relation.setMaxCardinality(2);
        assertFalse(relation.isMandatory());
    }
    public void testIsOptional(){
        relation.setMinCardinality(0);
        relation.setMaxCardinality(1);
        assertTrue(relation.isOptional());
        
        relation.setMinCardinality(1);
        relation.setMaxCardinality(1);
        assertFalse(relation.isOptional());

        relation.setMinCardinality(2);
        relation.setMaxCardinality(3);
        assertFalse(relation.isOptional());

        relation.setMinCardinality(3);
        relation.setMaxCardinality(2);
        assertFalse(relation.isOptional());
    }
}
