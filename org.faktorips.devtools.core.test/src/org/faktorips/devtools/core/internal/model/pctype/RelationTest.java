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

package org.faktorips.devtools.core.internal.model.pctype;

import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class RelationTest extends AbstractIpsPluginTest {
    
    private IPolicyCmptType pcType;
    private IRelation relation;
    private IRelation cRel;
    private DefaultTestContent content;
    
    protected void setUp() throws Exception {
        super.setUp();
        content = new DefaultTestContent();
        pcType = content.getContract();
        relation = content.getContract().getRelation("Coverage");
        cRel = content.getMotorContract().getRelation("CollisionCoverage");
    }
    
    public void testRemove() {
        assertEquals(1, pcType.getRelations().length);
        relation.delete();
        assertEquals(0, pcType.getRelations().length);
        assertTrue(relation.getIpsObject().getIpsSrcFile().isDirty());
    }
    
    public void testSetType() {
        relation.setRelationType(RelationType.COMPOSITION);
        relation.setRelationType(RelationType.ASSOZIATION);
        assertEquals(RelationType.ASSOZIATION, relation.getRelationType());
        assertTrue(relation.getIpsObject().getIpsSrcFile().isDirty());
    }
    
    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        relation.initFromXml((Element)doc.getDocumentElement());
        assertEquals(42, relation.getId());
        assertEquals(RelationType.ASSOZIATION, relation.getRelationType());
        assertTrue(relation.isReadOnlyContainer());
        assertEquals("MotorPart", relation.getTarget());
        assertEquals("blabla", relation.getDescription());
        assertEquals("PolicyPart", relation.getTargetRoleSingular());
        assertEquals("PolicyParts", relation.getTargetRolePlural());
        assertEquals(1, relation.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, relation.getMaxCardinality());
        assertFalse(relation.isProductRelevant());
        assertEquals("Policy.Parts", relation.getContainerRelation());
        assertEquals("Part.Policy", relation.getReverseRelation());
        assertEquals("PolicyPartType", relation.getTargetRoleSingularProductSide());
        assertEquals("PolicyPartTypes", relation.getTargetRolePluralProductSide());
        assertEquals(2, relation.getMinCardinalityProductSide());
        assertEquals(3, relation.getMaxCardinalityProductSide());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXml() {
        relation = pcType.newRelation(); 
        relation.setRelationType(RelationType.ASSOZIATION);
        relation.setReadOnlyContainer(true);
        relation.setTarget("target");
        relation.setTargetRoleSingular("targetRoleSingular");
        relation.setTargetRolePlural("targetRolePlural");
        relation.setProductRelevant(false);
        relation.setContainerRelation("super");
        relation.setReverseRelation("reverse");
        relation.setMinCardinality(2);
        relation.setMaxCardinality(3);
        relation.setDescription("blabla");
        relation.setTargetRoleSingularProductSide("targetRoleSingularProductSide");
        relation.setTargetRolePluralProductSide("targetRolePluralProductSide");
        relation.setMinCardinalityProductSide(4);
        relation.setMaxCardinalityProductSide(5);
        
        Element element = relation.toXml(this.newDocument());
        
        Relation copy = new Relation();
        copy.initFromXml(element);
        assertEquals(4, copy.getId());
        assertEquals(RelationType.ASSOZIATION, copy.getRelationType());
        assertTrue(copy.isReadOnlyContainer());
        assertEquals("target", copy.getTarget());
        assertEquals("targetRoleSingular", copy.getTargetRoleSingular());
        assertEquals("targetRolePlural", copy.getTargetRolePlural());
        assertEquals("super", copy.getContainerRelation());
        assertEquals("reverse", copy.getReverseRelation());
        assertEquals(2, copy.getMinCardinality());
        assertEquals(3, copy.getMaxCardinality());
        assertFalse(copy.isProductRelevant());
        assertEquals("blabla", copy.getDescription());
        
        assertEquals("targetRoleSingularProductSide", copy.getTargetRoleSingularProductSide());
        assertEquals("targetRolePluralProductSide", copy.getTargetRolePluralProductSide());
        assertEquals(4, copy.getMinCardinalityProductSide());
        assertEquals(5, copy.getMaxCardinalityProductSide());        
    }
    
    /**
     * Tests for the correct type of excetion to be thrwon - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			relation.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    public void testValidate() throws Exception {
    	MessageList ml = new MessageList();

    	IRelation relation = content.getContract().getRelation("Coverage");
    	
    	relation.setTarget("abx");
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_DOES_NOT_EXIST));
    	
    	relation.setTarget(content.getCoverage().getQualifiedName());
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_DOES_NOT_EXIST));
    	
    	relation.setTargetRoleSingular("");
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET));
    	
    	relation.setTargetRoleSingular("Coverage");
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET));
    	
    	relation.setMaxCardinality(0);
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1));
    	
    	relation.setMaxCardinality(1);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1));
    	
    	relation.setMinCardinality(3);
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_MAX_IS_LESS_THAN_MIN));
    	
    	relation.setMinCardinality(0);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_MAX_IS_LESS_THAN_MIN));
    	
    }
    
    public void testValidateProductRelevanceOfContainerRel() throws Exception {
    	MessageList ml = new MessageList();
    	
    	IRelation relation = content.getContract().getRelation("Coverage");
    	relation.setReadOnlyContainer(true);
    	IRelation containerRel = content.getMotorContract().getRelation("CollisionCoverage");
    	containerRel.setContainerRelation("Coverage");
    	
    	ml = containerRel.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_IMPLEMENTATION_MUST_HAVE_SAME_PRODUCT_RELEVANT_VALUE));
    	
    	containerRel.setProductRelevant(false);
    	ml = containerRel.validate();
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_IMPLEMENTATION_MUST_HAVE_SAME_PRODUCT_RELEVANT_VALUE));
    }
    
    public void testValidateMaxCardinalityForReverseComposition() throws Exception {
    	MessageList ml = new MessageList();
    	
    	relation.setMaxCardinality(2);
    	relation.setRelationType(RelationType.REVERSE_COMPOSITION);
    	
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION));
    	
    	relation.setMaxCardinality(1);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION));
    }
    
    public void testValidateReverseCompositionProductRelevant() throws Exception {
    	MessageList ml = new MessageList();
    	
    	relation.setRelationType(RelationType.REVERSE_COMPOSITION);
    	ml = relation.validate();
    	
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_COMPOSITION_CANT_BE_MARKED_AS_PRODUCT_RELEVANT));
    	
    	relation.setProductRelevant(false);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_COMPOSITION_CANT_BE_MARKED_AS_PRODUCT_RELEVANT));
    }

    public void testValidateProductRelevant() throws Exception {
    	MessageList ml = new MessageList();
    	
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_RELATION_CAN_BE_PRODUCT_RELEVANT_ONLY_IF_THE_TYPE_IS));
    	
    	relation.getPolicyCmptType().setConfigurableByProductCmptType(false);
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_RELATION_CAN_BE_PRODUCT_RELEVANT_ONLY_IF_THE_TYPE_IS));
    }
    
    public void testValidateContainerRelationNotInSupertype() throws Exception {
		MessageList ml = new MessageList();
		
		relation.setReadOnlyContainer(true);
		cRel.setContainerRelation("Coverage");
		
		ml = cRel.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_CONTAINERRELATION_NOT_IN_SUPERTYPE));
		
		cRel.setContainerRelation("xxx");
		ml = cRel.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_CONTAINERRELATION_NOT_IN_SUPERTYPE));
	}

	public void testValidateNotMarkedAsContainerRelation() throws Exception {
		MessageList ml = new MessageList();
		cRel.setContainerRelation("Coverage");

		ml = cRel.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_NOT_MARKED_AS_CONTAINERRELATION));
		
		relation.setReadOnlyContainer(true);
		ml = cRel.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_NOT_MARKED_AS_CONTAINERRELATION));
	}

	public void testValidateContainerRelTargetNotExisting() throws Exception {
		MessageList ml = new MessageList();
		relation.setReadOnlyContainer(true);
		cRel.setContainerRelation("Coverage");

		ml = cRel.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_CONTAINERRELATION_TARGET_DOES_NOT_EXIST));
		
		relation.setTarget("xxx");
		ml = cRel.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_CONTAINERRELATION_TARGET_DOES_NOT_EXIST));
	}

	public void testValidateTargetNotSubclass() throws Exception {
		MessageList ml = new MessageList();
		relation.setReadOnlyContainer(true);
		cRel.setContainerRelation("Coverage");

		ml = cRel.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_NOT_SUBCLASS));
		
		relation.setTarget("motor.Vehicle");
		ml = cRel.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_NOT_SUBCLASS));
	}

	public void testValidateSamePluralRolename() throws Exception {
		MessageList ml = new MessageList();
		relation.setReadOnlyContainer(true);
		cRel.setContainerRelation("Coverage");

		ml = cRel.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_CONTAINERRELATION_SAME_PLURAL_ROLENAME));
		
		relation.setTargetRolePlural(cRel.getTargetRolePlural());
		ml = cRel.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_CONTAINERRELATION_SAME_PLURAL_ROLENAME));
	}

	public void testValidateSameSingularRolename() throws Exception {
		MessageList ml = new MessageList();

		ml = cRel.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_SAME_SINGULAR_ROLENAME));
		
		cRel.setTargetRoleSingular(relation.getTargetRoleSingular());
		ml = cRel.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_SAME_SINGULAR_ROLENAME));
	}
	
	public void testValidateSamePluralRolenameProductSide() throws Exception {
		MessageList ml = cRel.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_SAME_PLURAL_ROLENAME_PRODUCTSIDE));

		cRel.setTargetRolePluralProductSide(relation.getTargetRolePluralProductSide());
		ml = cRel.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_SAME_PLURAL_ROLENAME_PRODUCTSIDE));
	}

	public void testValidateSameSingularRolenameProductSide() throws Exception {
		MessageList ml = cRel.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_SAME_SINGULAR_ROLENAME_PRODUCTSIDE));

		cRel.setTargetRoleSingularProductSide(relation.getTargetRoleSingularProductSide());
		ml = cRel.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_SAME_SINGULAR_ROLENAME_PRODUCTSIDE));
	}

	public void testValidateReverseRelationNotInTarget() throws Exception {
		IRelation rel2 = content.getCoverage().newRelation();
		rel2.setTargetRoleSingular("test");
		relation.setReverseRelation("abc");
		MessageList ml = relation.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSERELATION_NOT_IN_TARGET));
		
		relation.setReverseRelation("test");
		ml = relation.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSERELATION_NOT_IN_TARGET));
	}

	public void testValidateReverseRelationNotSpecified() throws Exception {
		IRelation rel2 = content.getCoverage().newRelation();
		rel2.setTargetRoleSingular("test");
		relation.setReverseRelation("test");
		MessageList ml = relation.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSERELATION_NOT_SPECIFIED));
		rel2.setReverseRelation(relation.getTargetRoleSingular());
		ml = relation.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSERELATION_NOT_SPECIFIED));
	}

	public void testValidateReverseRelationOfContainerRelationHasToBeContainerRelationToo() throws Exception {
		IRelation rel2 = content.getCoverage().newRelation();
		rel2.setTargetRoleSingular("test");
		rel2.setReverseRelation(relation.getTargetRoleSingular());
		relation.setReverseRelation("test");
		relation.setReadOnlyContainer(true);
		MessageList ml = relation.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSERELATION_OF_CONTAINERRELATION_MUST_BE_CONTAINERRELATION_TOO));
		rel2.setReadOnlyContainer(true);
		ml = relation.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSERELATION_OF_CONTAINERRELATION_MUST_BE_CONTAINERRELATION_TOO));
	}

	public void testValidateReverseCompositionMissmatch() throws Exception {
		IRelation rel2 = content.getCoverage().newRelation();
		rel2.setTargetRoleSingular("test");
		rel2.setReverseRelation(relation.getTargetRoleSingular());
		rel2.setRelationType(RelationType.COMPOSITION);
		relation.setReverseRelation("test");
		relation.setRelationType(RelationType.COMPOSITION);
		MessageList ml = relation.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_COMPOSITION_MISSMATCH));
		rel2.setRelationType(RelationType.REVERSE_COMPOSITION);
		ml = relation.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_COMPOSITION_MISSMATCH));
	}

	public void testValidateReverseAssociationMissmatch() throws Exception {
		IRelation rel2 = content.getCoverage().newRelation();
		rel2.setTargetRoleSingular("test");
		rel2.setReverseRelation(relation.getTargetRoleSingular());
		rel2.setRelationType(RelationType.COMPOSITION);
		relation.setReverseRelation("test");
		relation.setRelationType(RelationType.ASSOZIATION);
		MessageList ml = relation.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_ASSOCIATION_MISSMATCH));
		rel2.setRelationType(RelationType.ASSOZIATION);
		ml = relation.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_ASSOCIATION_MISSMATCH));
    }
	
    public void testValidateMaxCardinalityForContainerRelationTooLow() throws Exception {
        MessageList ml = relation.validate();
        assertNull(ml.getMessageByCode(IRelation.MSGCODE_MAX_CARDINALITY_FOR_CONTAINERRELATION_TOO_LOW));
        
        relation.setMaxCardinality(1);
        relation.setReadOnlyContainer(true);
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_MAX_CARDINALITY_FOR_CONTAINERRELATION_TOO_LOW));
    }

    public void testValidateEmptyTargetRoleSingularProductSide() throws Exception {
        relation.setProductRelevant(false);
        relation.setTargetRoleSingularProductSide("");
        MessageList ml = relation.validate();
        assertNull(ml.getMessageByCode(IRelation.MSGCODE_NO_TARGET_ROLE_SINGULAR_PRODUCTSIDE));
        
        relation.setProductRelevant(true);
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_NO_TARGET_ROLE_SINGULAR_PRODUCTSIDE));
        
        relation.setTargetRoleSingularProductSide("notEmpty");
        ml = relation.validate();
        assertNull(ml.getMessageByCode(IRelation.MSGCODE_NO_TARGET_ROLE_SINGULAR_PRODUCTSIDE));
    }

    public void testValidateEmptyTargetRolePluralProductSide() throws Exception {
        relation.setProductRelevant(false);
        relation.setTargetRolePluralProductSide("");
        MessageList ml = relation.validate();
        assertNull(ml.getMessageByCode(IRelation.MSGCODE_NO_TARGET_ROLE_PLURAL_PRODUCTSIDE));
        
        relation.setProductRelevant(true);
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_NO_TARGET_ROLE_PLURAL_PRODUCTSIDE));
        
        relation.setTargetRolePluralProductSide("notEmpty");
        ml = relation.validate();
        assertNull(ml.getMessageByCode(IRelation.MSGCODE_NO_TARGET_ROLE_PLURAL_PRODUCTSIDE));
    }

    public void testValidateSameSingularAndPluralTargetRoleProductSide() throws Exception {
        relation.setProductRelevant(true);
        relation.setTargetRolePluralProductSide("a");
        relation.setTargetRoleSingularProductSide("b");
        MessageList ml = relation.validate();
        assertNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_ROLE_PLURAL_PRODUCTSIDE_EQULAS_TARGET_ROLE_SINGULAR_PRODUCTSIDE));
        
        relation.setTargetRoleSingularProductSide("a");
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_ROLE_PLURAL_PRODUCTSIDE_EQULAS_TARGET_ROLE_SINGULAR_PRODUCTSIDE));
    }

    
}
