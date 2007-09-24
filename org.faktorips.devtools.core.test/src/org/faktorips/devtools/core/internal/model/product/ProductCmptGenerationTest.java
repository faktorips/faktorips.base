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

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IFormula;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptLink;
import org.faktorips.devtools.core.model.product.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype2.AggregationKind;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeAssociation;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptGenerationTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IIpsProject ipsProject;
    
    private IPolicyCmptType targetPolicyType;
    private IProductCmptType targetProductType;
    private IProductCmptTypeAssociation association;
    private IProductCmpt target;
    
    public void setUp() throws Exception {
        super.setUp();
        ipsProject =  newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);
        
        targetPolicyType = newPolicyAndProductCmptType(ipsProject, "TargetPolicyType", "TargetProductType");
        targetProductType = targetPolicyType.findProductCmptType(ipsProject);
        target = newProductCmpt(targetProductType, "TargetProduct");
        
        association = productCmptType.newAssociation();
        association.setAggregationKind(AggregationKind.SHARED);
        association.setTarget(targetProductType.getQualifiedName());
        association.setTargetRoleSingular("testRelationProductSide");
        association.setTargetRolePlural("testRelationsProductSide");
    }
    
    public void testNewLink() {
        IProductCmptLink link = generation.newLink("coverage");
        assertEquals(generation, link.getParent());
        assertEquals(1, generation.getNumOfLinks());
        assertEquals(link, generation.getLinks()[0]);
        
        IProductCmptLink link2 = generation.newLink("covergae");
        assertEquals(generation, link2.getParent());
        assertEquals(2, generation.getNumOfLinks());
        assertEquals(link, generation.getLinks()[0]);
        assertEquals(link2, generation.getLinks()[1]);
    }

    /*
     * Class under test for void toXml(Element)
     */
    public void testToXmlElement() {
        generation.setValidFrom(new GregorianCalendar(2005, 0, 1));
        generation.newConfigElement();
        generation.newConfigElement();
        generation.newLink("coverage");
        generation.newLink("coverage");
        generation.newLink("coverage");
        generation.newFormula();
        Element element = generation.toXml(newDocument());
        
        IProductCmptGeneration copy = new ProductCmptGeneration();
        copy.initFromXml(element);
        assertEquals(2, copy.getNumOfConfigElements());
        assertEquals(3, copy.getNumOfLinks());
        assertEquals(1, copy.getNumOfFormulas());
    }

    public void testInitFromXml() {
        generation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(new GregorianCalendar(2005, 0, 1), generation.getValidFrom());
        
        IConfigElement[] configElements = generation.getConfigElements();
        assertEquals(1, configElements.length);
        
        IProductCmptLink[] relations = generation.getLinks();
        assertEquals(1, relations.length);
        
        IFormula[] formulas = generation.getFormulas();
        assertEquals(1, formulas.length);
    }


    
    public void testValidateDuplicateRelationTarget() throws Exception {
        MessageList ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_DUPLICATE_RELATION_TARGET));
        
        generation.newLink(association.getName()).setTarget(target.getQualifiedName());
        ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_DUPLICATE_RELATION_TARGET));
        
        generation.newLink(association).setTarget(target.getQualifiedName());
        
        ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_DUPLICATE_RELATION_TARGET));
    }
    
    public void testValidateNotEnougthRelations() throws Exception {
        association.setMinCardinality(1);
        association.setMaxCardinality(2);

        MessageList ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NOT_ENOUGH_RELATIONS));
        
        generation.newLink(association.getTargetRoleSingular());
        ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NOT_ENOUGH_RELATIONS));
        
        generation.newLink(association.getTargetRoleSingular());
        ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NOT_ENOUGH_RELATIONS));
    }
    
    public void testValidateTooManyRelations() throws Exception {
        association.setMinCardinality(0);
        association.setMaxCardinality(1);

        MessageList ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_TOO_MANY_RELATIONS));
        
        generation.newLink(association.getTargetRoleSingular());
        ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_TOO_MANY_RELATIONS));
        
        generation.newLink(association.getTargetRoleSingular());
        ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_TOO_MANY_RELATIONS));
    }
    
    public void testValidateAttributeWithMissingConfigElement() throws Exception{        
        IProductCmpt product = newProductCmpt(productCmptType, "EmptyTestProduct");
        IProductCmptGeneration gen = product.getProductCmptGeneration(0);
        MessageList msgList = gen.validate();
        assertTrue(msgList.isEmpty());
        
        IAttribute attribute = policyCmptType.newAttribute();
        attribute.setProductRelevant(true);
        attribute.setName("test");        
        msgList = gen.validate();
        assertFalse(msgList.isEmpty());
        assertNotNull(msgList.getMessageByCode(IProductCmptGeneration.MSGCODE_ATTRIBUTE_WITH_MISSING_CONFIG_ELEMENT));
    }

    public void testCanCreateValidRelation() throws Exception {
        assertFalse(generation.canCreateValidLink(null, null, ipsProject));
        assertFalse(generation.canCreateValidLink(productCmpt, null, ipsProject));
        
        assertTrue(generation.canCreateValidLink(target, "testRelationProductSide", ipsProject));
    }
    
    /**
     * test for bug #829
     * @throws Exception
     */
    public void testCanCreateValidRelation_RelationDefinedInSupertypeHierarchyOfSourceType() throws Exception {
        // create a subtype of the existing policy component type
        IPolicyCmptType subpolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SubPolicyType", "SubProductType");
        IProductCmptType subProductCmptType = subpolicyCmptType.findProductCmptType(ipsProject);
        subpolicyCmptType.setSupertype(policyCmptType.getQualifiedName());
        subProductCmptType.setSupertype(productCmptType.getQualifiedName());
        
        IProductCmpt productCmpt2 = newProductCmpt(subProductCmptType, "TestProduct2");
        IProductCmptGeneration generation2 = productCmpt2.getProductCmptGeneration(0);

        assertTrue(generation2.canCreateValidLink(target, "testRelationProductSide", ipsProject));
    }
    
    
    public void testGetChildren() throws CoreException  {
        IConfigElement element = generation.newConfigElement();
        IProductCmptLink link = generation.newLink("targetRole");
        ITableContentUsage usage = generation.newTableContentUsage();
        IFormula formula = generation.newFormula();
        
        IIpsElement[] children = generation.getChildren();
        assertEquals(4, children.length);
        assertSame(element, children[0]);
        assertSame(usage, children[1]);
        assertSame(formula, children[2]);
        assertSame(link, children[3]);
    }
    
    public void testGetConfigElements() {
        assertEquals(0, generation.getNumOfConfigElements());
        
        IConfigElement ce1 = generation.newConfigElement();
        assertEquals(ce1, generation.getConfigElements()[0]);

        IConfigElement ce2 = generation.newConfigElement();
        assertEquals(ce1, generation.getConfigElements()[0]);
        assertEquals(ce2, generation.getConfigElements()[1]);
    }

    public void testGetConfigElements_Type() {
        IConfigElement ce1 = generation.newConfigElement();
        IConfigElement ce2 = generation.newConfigElement();
        IConfigElement ce3 = generation.newConfigElement();
        ce1.setType(ConfigElementType.PRODUCT_ATTRIBUTE);
        ce2.setType(ConfigElementType.POLICY_ATTRIBUTE);
        ce3.setType(ConfigElementType.PRODUCT_ATTRIBUTE);
        
        IConfigElement[] elements = generation.getConfigElements(ConfigElementType.PRODUCT_ATTRIBUTE);
        assertEquals(2, elements.length);
        assertEquals(ce1, elements[0]);
        assertEquals(ce3, elements[1]);
        
        elements = generation.getConfigElements(ConfigElementType.POLICY_ATTRIBUTE);
        assertEquals(1, elements.length);
        assertEquals(ce2, elements[0]);
    }
    
    public void testGetConfigElement_AttributeName() {
        generation.newConfigElement();
        IConfigElement ce2 = generation.newConfigElement();
        ce2.setPcTypeAttribute("a2");
        
        assertEquals(ce2, generation.getConfigElement("a2"));
        assertNull(generation.getConfigElement("unkown"));
        
    }

    public void testGetNumOfConfigElements() {
        assertEquals(0, generation.getNumOfConfigElements());
        
        generation.newConfigElement();
        assertEquals(1, generation.getNumOfConfigElements());

        generation.newConfigElement();
        assertEquals(2, generation.getNumOfConfigElements());
    }

    public void testNewConfigElement() {
        IConfigElement ce = generation.newConfigElement();
        assertEquals(generation, ce.getParent());
        assertEquals(1, generation.getNumOfConfigElements());
    }

    /*
     * Class under test for ProductCmptRelation[] getRelations()
     */
    public void testGetRelations() {
        IProductCmptLink r1 = generation.newLink("coverage");
        assertEquals(r1, generation.getLinks()[0]);

        IProductCmptLink r2 = generation.newLink("risk");
        assertEquals(r1, generation.getLinks()[0]);
        assertEquals(r2, generation.getLinks()[1]);
    }

    /*
     * Class under test for ProductCmptRelation[] getRelations(String)
     */
    public void testGetRelations_String() {
        IProductCmptLink r1 = generation.newLink("coverage");
        generation.newLink("risk");
        IProductCmptLink r3 = generation.newLink("coverage");
        
        IProductCmptLink[] relations = generation.getLinks("coverage");
        assertEquals(2, relations.length);
        assertEquals(r1, relations[0]);
        assertEquals(r3, relations[1]);

        relations = generation.getLinks("unknown");
        assertEquals(0, relations.length);
    }

    public void testGetNumOfLinks() {
        assertEquals(0, generation.getNumOfLinks());
        
        generation.newLink("coverage");
        assertEquals(1, generation.getNumOfLinks());

        generation.newLink("risk");
        assertEquals(2, generation.getNumOfLinks());
    }

    public void testValidateNoTemplate() throws Exception {
        generation.getProductCmpt().setProductCmptType("");
        MessageList ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NO_TEMPLATE));
    }
    

    public void testNewPart() {
    	try {
    		assertTrue(productCmpt.newPart(IConfigElement.class) instanceof IConfigElement);
    		assertTrue(productCmpt.newPart(IRelation.class) instanceof IRelation);
    		
    		productCmpt.newPart(Object.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    public void testValidateValidFrom() throws Exception {
        generation.getProductCmpt().setValidTo(new GregorianCalendar(2000, 10, 1));
        generation.setValidFrom(new GregorianCalendar(2000, 10, 2));
        
        MessageList ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_INVALID_VALID_FROM));
        
        generation.setValidFrom(new GregorianCalendar(2000, 9, 1));
        ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_INVALID_VALID_FROM));
    }

    
}