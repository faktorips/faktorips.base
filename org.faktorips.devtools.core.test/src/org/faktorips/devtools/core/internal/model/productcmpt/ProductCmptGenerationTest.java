/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.ConfigElementType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.DateUtil;
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
        
        association = productCmptType.newProductCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTarget(targetProductType.getQualifiedName());
        association.setTargetRoleSingular("testRelationProductSide");
        association.setTargetRolePlural("testRelationsProductSide");
    }
    
    public void testGetAttributeValue() {
        IAttributeValue value1 = generation.newAttributeValue();
        value1.setAttribute("a1");
        IAttributeValue value2 = generation.newAttributeValue();
        value2.setAttribute("a2");
        
        assertEquals(value1, generation.getAttributeValue("a1"));
        assertEquals(value2, generation.getAttributeValue("a2"));
        
        assertNull(generation.getAttributeValue("unknwon"));
        assertNull(generation.getAttributeValue(null));
    }
    
    public void testGetFormula() {
        IFormula formula1 = generation.newFormula();
        formula1.setFormulaSignature("f1");
        IFormula formula2 = generation.newFormula();
        formula2.setFormulaSignature("f2");
        
        assertEquals(formula1, generation.getFormula("f1"));
        assertEquals(formula2, generation.getFormula("f2"));
        
        assertNull(generation.getFormula("unknwon"));
        assertNull(generation.getFormula(null));
    }
    
    public void testGetPropertyValue() {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();
        attribute.setName("a1");
        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setRoleName("RateTable");
        IProductCmptTypeMethod signature = productCmptType.newFormulaSignature("calculation");
        IPolicyCmptTypeAttribute policyAttr = policyCmptType.newPolicyCmptTypeAttribute();
        policyAttr.setName("policyAttribute");
        policyAttr.setProductRelevant(true);
        
        IAttributeValue value = generation.newAttributeValue();
        value.setAttribute("a1");
        IFormula formula = generation.newFormula();
        formula.setFormulaSignature("calculation");
        ITableContentUsage contentUsage = generation.newTableContentUsage();
        contentUsage.setStructureUsage("RateTable");
        IConfigElement element = generation.newConfigElement();
        element.setPolicyCmptTypeAttribute("policyAttribute");
        
        assertEquals(value, generation.getPropertyValue(attribute));
        assertEquals(formula, generation.getPropertyValue(signature));
        assertEquals(contentUsage, generation.getPropertyValue(structureUsage));
        assertEquals(element, generation.getPropertyValue(policyAttr));
    }

    public void testNewFormula_FormulaSignature() {
        IProductCmptTypeMethod signature = productCmptType.newFormulaSignature("Calc");
        IFormula formula = generation.newFormula(signature);
        assertEquals("Calc", formula.getFormulaSignature());
        
        formula = generation.newFormula(null);
        assertEquals("", formula.getFormulaSignature());
    }
    
    public void testNewTableContentUsage_TableStructure() {
        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setRoleName("RateTable");
        ITableContentUsage contentUsage  = generation.newTableContentUsage(structureUsage);
        assertEquals("RateTable", contentUsage.getStructureUsage());
        
        contentUsage  = generation.newTableContentUsage(null);
        assertEquals("", contentUsage.getStructureUsage());
    }
    
    public void testNewAttributeValue_Attribute() {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();
        attribute.setName("premium");
        attribute.setDefaultValue("123");
        IAttributeValue value = generation.newAttributeValue(attribute);
        assertEquals("123", value.getValue());
        assertEquals("premium", value.getAttribute());
        
        value = generation.newAttributeValue(null);
        assertEquals("", value.getValue());
        assertEquals("", value.getAttribute());
    }
    
    public void testNewConfigElement_PolicyAttribute() {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("a1");
        attribute.setProductRelevant(true);
        attribute.setDefaultValue("10");
        attribute.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet range = (IRangeValueSet)attribute.getValueSet();
        range.setLowerBound("1");
        range.setUpperBound("42");
        
        IConfigElement el = generation.newConfigElement(attribute);
        assertEquals("a1", el.getPolicyCmptTypeAttribute());
        assertEquals("10", el.getValue());
        range = (IRangeValueSet)el.getValueSet();
        assertEquals("1", range.getLowerBound());
        assertEquals("42", range.getUpperBound());
    }
    
    public void testGetPropertyValues() {
        IAttributeValue value1 = generation.newAttributeValue();
        IFormula formula1 = generation.newFormula();
        IFormula formula2 = generation.newFormula();
        ITableContentUsage tcu1 = generation.newTableContentUsage();
        ITableContentUsage tcu2 = generation.newTableContentUsage();
        ITableContentUsage tcu3 = generation.newTableContentUsage();
        IConfigElement ce1 = generation.newConfigElement();
        IConfigElement ce2 = generation.newConfigElement();
        IConfigElement ce3 = generation.newConfigElement();
        IConfigElement ce4 = generation.newConfigElement();
        
        IPropertyValue[] values = generation.getPropertyValues(ProdDefPropertyType.VALUE);
        assertEquals(1, values.length);
        assertEquals(value1, values[0]);
        
        values = generation.getPropertyValues(ProdDefPropertyType.FORMULA);
        assertEquals(2, values.length);
        assertEquals(formula1, values[0]);
        assertEquals(formula2, values[1]);

        values = generation.getPropertyValues(ProdDefPropertyType.TABLE_CONTENT_USAGE);
        assertEquals(3, values.length);
        assertEquals(tcu1, values[0]);
        assertEquals(tcu2, values[1]);
        assertEquals(tcu3, values[2]);

        values = generation.getPropertyValues(ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET);
        assertEquals(4, values.length);
        assertEquals(ce1, values[0]);
        assertEquals(ce2, values[1]);
        assertEquals(ce3, values[2]);
        assertEquals(ce4, values[3]);
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
        generation.newFormula();
        generation.newAttributeValue();
        Element element = generation.toXml(newDocument());
        
        IProductCmptGeneration copy = new ProductCmptGeneration();
        copy.initFromXml(element);
        assertEquals(2, copy.getNumOfConfigElements());
        assertEquals(3, copy.getNumOfLinks());
        assertEquals(2, copy.getNumOfFormulas());
        assertEquals(1, copy.getNumOfAttributeValues());
    }

    public void testInitFromXml() {
        generation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(new GregorianCalendar(2005, 0, 1), generation.getValidFrom());
        
        IAttributeValue[] attrValues = generation.getAttributeValues();
        assertEquals(1, attrValues.length);

        IConfigElement[] configElements = generation.getConfigElements();
        assertEquals(1, configElements.length);
        
        IProductCmptLink[] relations = generation.getLinks();
        assertEquals(1, relations.length);
        
        IFormula[] formulas = generation.getFormulas();
        assertEquals(1, formulas.length);
    }


    
    public void testValidateDuplicateRelationTarget() throws Exception {
        MessageList ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_DUPLICATE_RELATION_TARGET));
        
        generation.newLink(association.getName()).setTarget(target.getQualifiedName());
        ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_DUPLICATE_RELATION_TARGET));
        
        generation.newLink(association).setTarget(target.getQualifiedName());
        
        ml = generation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_DUPLICATE_RELATION_TARGET));
    }
    
    public void testValidateNotEnoughRelations() throws Exception {
        association.setMinCardinality(1);
        association.setMaxCardinality(2);

        MessageList ml = generation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NOT_ENOUGH_RELATIONS));
        
        generation.newLink(association.getTargetRoleSingular());
        ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NOT_ENOUGH_RELATIONS));
        
        generation.newLink(association.getTargetRoleSingular());
        ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NOT_ENOUGH_RELATIONS));
    }
    
    public void testValidateTooManyRelations() throws Exception {
        association.setMinCardinality(0);
        association.setMaxCardinality(1);

        MessageList ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_TOO_MANY_RELATIONS));
        
        generation.newLink(association.getTargetRoleSingular());
        ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_TOO_MANY_RELATIONS));
        
        generation.newLink(association.getTargetRoleSingular());
        ml = generation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_TOO_MANY_RELATIONS));
    }
    
    public void testValidateAttributeWithMissingConfigElement() throws Exception{        
        IProductCmpt product = newProductCmpt(productCmptType, "EmptyTestProduct");
        IProductCmptGeneration gen = product.getProductCmptGeneration(0);
        MessageList msgList = gen.validate(ipsProject);
        assertTrue(msgList.isEmpty());
        
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setProductRelevant(true);
        attribute.setName("test");        
        msgList = gen.validate(ipsProject);
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
        ce1.setType(ConfigElementType.POLICY_ATTRIBUTE);
        ce2.setType(ConfigElementType.POLICY_ATTRIBUTE);
        
        IConfigElement[] elements = generation.getConfigElements(ConfigElementType.POLICY_ATTRIBUTE);
        assertEquals(2, elements.length);
        assertEquals(ce1, elements[0]);
        assertEquals(ce2, elements[1]);
    }
    
    public void testGetConfigElement_AttributeName() {
        generation.newConfigElement();
        IConfigElement ce2 = generation.newConfigElement();
        ce2.setPolicyCmptTypeAttribute("a2");
        
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
        MessageList ml = generation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NO_TEMPLATE));
    }
    

    public void testNewPart() {
    	try {
    		assertTrue(productCmpt.newPart(ConfigElement.class) instanceof IConfigElement);
    		assertTrue(productCmpt.newPart(PolicyCmptTypeAssociation.class) instanceof IPolicyCmptTypeAssociation);
    		
    		productCmpt.newPart(Object.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    public void testValidateValidFrom() throws Exception {
        generation.getProductCmpt().setValidTo(new GregorianCalendar(2000, 10, 1));
        generation.setValidFrom(new GregorianCalendar(2000, 10, 2));
        
        MessageList ml = generation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_INVALID_VALID_FROM));
        
        generation.setValidFrom(new GregorianCalendar(2000, 9, 1));
        ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_INVALID_VALID_FROM));
    }

    public void testValidateIfReferencedProductComponentsAreValidOnThisGenerationsValidFromDate() throws CoreException,
            Exception {
        generation.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2007-01-01"));
        IProductCmptLink link = generation.newLink(association);
        link.setTarget(target.getQualifiedName());
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
        IProductCmptGeneration targetGeneration = (IProductCmptGeneration)target.getGeneration(0);
        targetGeneration.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2008-01-01"));
        
        MessageList msgList = ((ProductCmptGeneration)generation).validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IProductCmptGeneration.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));

        // assert that there is no validation error if the optional constraint 
        // "referencedProductComponentsAreValidOnThisGenerationsValidFromDate" is turned off
        IIpsProjectProperties oldProps = ipsProject.getProperties();
        IIpsProjectProperties newProps = new IpsProjectProperties(ipsProject, (IpsProjectProperties)oldProps);
        newProps.setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(false);
        ipsProject.setProperties(newProps);
        msgList = ((ProductCmptGeneration)generation).validate(ipsProject);
        assertNull(msgList.getMessageByCode(IProductCmptGeneration.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));
        ipsProject.getProperties().setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(true);
        ipsProject.setProperties(oldProps);
        
        targetGeneration.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2007-01-01"));
        msgList = ((ProductCmptGeneration)generation).validate(ipsProject);
        assertNull(msgList.getMessageByCode(IProductCmptGeneration.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));

        targetGeneration.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2006-01-01"));
        msgList = ((ProductCmptGeneration)generation).validate(ipsProject);
        assertNull(msgList.getMessageByCode(IProductCmptGeneration.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));
    }
    
}
