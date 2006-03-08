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
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.message.MessageList;
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
    }

    public void testInitFromXml() {
        relation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(42, relation.getId());
        assertEquals("FullCoverage", relation.getProductCmptTypeRelation());
        assertEquals("FullCoveragePlus", relation.getTarget());
        assertEquals(2, relation.getMinCardinality());
        assertEquals(3, relation.getMaxCardinality());
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
    	
    	rel.setMinCardinality(3);
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_MIN_CARDINALITY_IS_LESS_THAN_MODEL_MIN));

    	rel.setMinCardinality(0);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_MIN_CARDINALITY_IS_LESS_THAN_MODEL_MIN));
    	
    	rel.setMaxCardinality(1);
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX));

    	rel.setMaxCardinality(3);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IProductCmptRelation.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX));
    }
    
}
