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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptGenerationTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IIpsPackageFragmentRoot root;
    private IIpsProject ipsProject;
    
    private IPolicyCmptType targetType;
    private IRelation typeRelation;
    private IProductCmpt target;
    private IProductCmptGeneration targetGen;
    
    public void setUp() throws Exception {
        super.setUp();
        ipsProject =  newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        policyCmptType = newPolicyCmptType(ipsProject, "type");
        productCmpt = (IProductCmpt)newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT, "testProduct");
        productCmpt.setPolicyCmptType(policyCmptType.getQualifiedName());
        generation = (IProductCmptGeneration)productCmpt.newGeneration();
        
        targetType = newPolicyCmptType(ipsProject, "TargetType");
        target = newProductCmpt(ipsProject, "TargetProduct");
        target.setPolicyCmptType(targetType.getQualifiedName());
        targetGen = (IProductCmptGeneration)target.newGeneration();
        targetGen.setValidFrom(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
        
        typeRelation = policyCmptType.newRelation();
        typeRelation.setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
        typeRelation.setProductRelevant(true);
        typeRelation.setTarget(targetType.getQualifiedName());
    }
    
    public void testCanCreateValidRelation() throws Exception {
        assertFalse(generation.canCreateValidRelation(null, null));
        assertFalse(generation.canCreateValidRelation(productCmpt, null));
        
        IPolicyCmptType targetType = newPolicyCmptType(ipsProject, "target.TargetPolicy");
        IProductCmpt target = newProductCmpt(ipsProject, "target.Target");
        target.setPolicyCmptType(targetType.getQualifiedName());
        
        IRelation rel = policyCmptType.newRelation();
        rel.setTarget(targetType.getQualifiedName());
        rel.setTargetRoleSingular("testRelation");
        rel.setTargetRoleSingularProductSide("testRelation");
        
        assertTrue(generation.canCreateValidRelation(target, "testRelation"));
    }
    
    /**
     * test for bug #829
     * @throws Exception
     */
    public void testCanCreateValidRelation_RelationDefinedInSupertypeHierarchyOfSourceType() throws Exception {
        // create a subtype of the existing policy component type
        IPolicyCmptType subpolicyCmptType = newPolicyCmptType(ipsProject, "Subtype");
        subpolicyCmptType.setSupertype(policyCmptType.getQualifiedName());
        
        IProductCmpt productCmpt2 = (IProductCmpt)newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT, "TestProduct2");
        productCmpt2.setPolicyCmptType(subpolicyCmptType.getQualifiedName());
        IProductCmptGeneration generation2 = (IProductCmptGeneration)productCmpt2.newGeneration();

        IPolicyCmptType targetType = newPolicyCmptType(ipsProject, "target.TargetPolicy");
        IProductCmpt target = newProductCmpt(ipsProject, "target.Target");
        target.setPolicyCmptType(targetType.getQualifiedName());
        
        IRelation rel = policyCmptType.newRelation();
        rel.setTarget(targetType.getQualifiedName());
        rel.setTargetRoleSingular("testRelation");
        rel.setTargetRoleSingularProductSide("testRelation");
        
        assertTrue(generation2.canCreateValidRelation(target, "testRelation"));
    }
    
    
    public void testGetChildren() throws CoreException  {
        IConfigElement cf0 = generation.newConfigElement();
        IProductCmptRelation r0 = generation.newRelation("targetRole");
        IIpsElement[] children = generation.getChildren();
        assertEquals(2, children.length);
        assertSame(cf0, children[0]);
        assertSame(r0, children[1]);
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
        ce2.setType(ConfigElementType.FORMULA);
        IConfigElement ce3 = generation.newConfigElement();
        
        IConfigElement[] elements = generation.getConfigElements(ConfigElementType.PRODUCT_ATTRIBUTE);
        assertEquals(2, elements.length);
        assertEquals(ce1, elements[0]);
        assertEquals(ce3, elements[1]);
        
        elements = generation.getConfigElements(ConfigElementType.POLICY_ATTRIBUTE);
        assertEquals(0, elements.length);
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
        IProductCmptRelation r1 = generation.newRelation("coverage");
        assertEquals(r1, generation.getRelations()[0]);

        IProductCmptRelation r2 = generation.newRelation("risk");
        assertEquals(r1, generation.getRelations()[0]);
        assertEquals(r2, generation.getRelations()[1]);
    }

    /*
     * Class under test for ProductCmptRelation[] getRelations(String)
     */
    public void testGetRelations_String() {
        IProductCmptRelation r1 = generation.newRelation("coverage");
        generation.newRelation("risk");
        IProductCmptRelation r3 = generation.newRelation("coverage");
        
        IProductCmptRelation[] relations = generation.getRelations("coverage");
        assertEquals(2, relations.length);
        assertEquals(r1, relations[0]);
        assertEquals(r3, relations[1]);

        relations = generation.getRelations("unknown");
        assertEquals(0, relations.length);
    }

    public void testGetNumOfRelations() {
        assertEquals(0, generation.getNumOfRelations());
        
        generation.newRelation("coverage");
        assertEquals(1, generation.getNumOfRelations());

        generation.newRelation("risk");
        assertEquals(2, generation.getNumOfRelations());
    }

    public void testNewRelation() {
        IProductCmptRelation relation = generation.newRelation("coverage");
        assertEquals(generation, relation.getParent());
        assertEquals(1, generation.getNumOfRelations());
        assertEquals(relation, generation.getRelations()[0]);
    }

    /*
     * Class under test for void toXml(Element)
     */
    public void testToXmlElement() {
        generation.setValidFrom(new GregorianCalendar(2005, 0, 1));
        generation.newConfigElement();
        generation.newConfigElement();
        generation.newRelation("coverage");
        generation.newRelation("coverage");
        generation.newRelation("coverage");
        Element element = generation.toXml(newDocument());
        
        IProductCmptGeneration copy = new ProductCmptGeneration();
        copy.initFromXml(element);
        assertEquals(2, copy.getNumOfConfigElements());
        assertEquals(3, copy.getNumOfRelations());
    }

    public void testInitFromXml() {
        generation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(new GregorianCalendar(2005, 0, 1), generation.getValidFrom());
        
        IConfigElement[] configElements = generation.getConfigElements();
        assertEquals(1, configElements.length);
        
        IProductCmptRelation[] relations = generation.getRelations();
        assertEquals(1, relations.length);
    }

    
    public void testValidate() throws Exception{
        
        IPolicyCmptType a = newPolicyCmptType(root, "A");
        IPolicyCmptType b = newPolicyCmptType(root, "B");
        IAttribute bAttribute = b.newAttribute();
        bAttribute.setAttributeType(AttributeType.CHANGEABLE);
        bAttribute.setName("bAttribute");
        bAttribute.setDatatype("String");
        IAttribute anAttribute = a.newAttribute();
        anAttribute.setName("anAttribute");
        anAttribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        anAttribute.setDatatype("String");
        Parameter p = new Parameter(0, "b", b.getQualifiedName());
        anAttribute.setFormulaParameters(new Parameter[]{p});
        
        IProductCmpt aProduct = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "aProduct");
        aProduct.setPolicyCmptType(a.getQualifiedName());
        IProductCmptGeneration aProductGen = (IProductCmptGeneration)aProduct.newGeneration();
        IConfigElement configElement = aProductGen.newConfigElement();
        configElement.setPcTypeAttribute("anAttribute");
        configElement.setType(ConfigElementType.FORMULA);
        configElement.setValue("b.bAttribute");
        MessageList msgList = aProductGen.validate();
        assertTrue(msgList.isEmpty());
        
        //change the name of bAttribute. A validation message from the formula validation is expected
        bAttribute.setName("cAttribute");
        msgList = aProductGen.validate();
        assertNotNull(msgList.getMessageByCode(ExprCompiler.UNDEFINED_IDENTIFIER));
    }
    
    public void testValidateDuplicateRelationTarget() throws Exception {
        MessageList ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_DUPLICATE_RELATION_TARGET));
        
        generation.newRelation(typeRelation.getName()).setTarget(target.getQualifiedName());
        ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_DUPLICATE_RELATION_TARGET));
        
        IProductCmpt target2 = newProductCmpt(ipsProject, "Target2");
        target2.setPolicyCmptType(policyCmptType.getQualifiedName());
        generation.newRelation(typeRelation.getName()).setTarget(target2.getQualifiedName());
        
        ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_DUPLICATE_RELATION_TARGET));
    }
    
    public void testValidateNotEnougthRelations() throws Exception {
        typeRelation.setMinCardinalityProductSide(1);
        typeRelation.setMaxCardinalityProductSide(2);

        MessageList ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NOT_ENOUGH_RELATIONS));
        
        generation.newRelation(typeRelation.getName());
        ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NOT_ENOUGH_RELATIONS));
        
        IProductCmpt target2 = newProductCmpt(ipsProject, "Target2");
        target2.setPolicyCmptType(policyCmptType.getQualifiedName());
        generation.newRelation(typeRelation.getName());
        
        ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NOT_ENOUGH_RELATIONS));
    }
    
    public void testValidateTooManyRelations() throws Exception {
        typeRelation.setMinCardinalityProductSide(0);
        typeRelation.setMaxCardinalityProductSide(1);

        MessageList ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_TOO_MANY_RELATIONS));
        
        generation.newRelation(typeRelation.getName());
        ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_TOO_MANY_RELATIONS));
        
        IProductCmpt target2 = newProductCmpt(ipsProject, "Target2");
        target2.setPolicyCmptType(policyCmptType.getQualifiedName());
        generation.newRelation(typeRelation.getName());
        
        ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_TOO_MANY_RELATIONS));
    }
    
    public void testValidateNoTemplate() throws Exception {
        generation.getProductCmpt().setPolicyCmptType("");
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

    
    public void testValidateAttributeWithMissingConfigElement() throws Exception{        
        IPolicyCmptType pcType = newPolicyCmptType(root, "EmptyTestPcType");
        IProductCmpt product = newProductCmpt(ipsProject, "EmptyTestProduct");
        product.setPolicyCmptType(pcType.getQualifiedName());
        IProductCmptGeneration gen = (IProductCmptGeneration)product.newGeneration();
        MessageList msgList = gen.validate();
        assertTrue(msgList.isEmpty());
        
        IAttribute attribute = pcType.newAttribute();
        attribute.setProductRelevant(true);
        attribute.setName("test");        
        msgList = gen.validate();
        assertFalse(msgList.isEmpty());
        assertNotNull(msgList.getMessageByCode(IProductCmptGeneration.MSGCODE_ATTRIBUTE_WITH_MISSING_CONFIG_ELEMENT));
    }
}