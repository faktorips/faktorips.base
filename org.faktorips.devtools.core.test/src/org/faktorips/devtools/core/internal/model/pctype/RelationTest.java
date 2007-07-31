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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
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
    
    public void testValidateContainerRelation_ReverseRelation_Mismtach() throws CoreException {
        IIpsProject project = newIpsProject();        
        IPolicyCmptType policyType = newPolicyCmptType(project, "Policy");
        IPolicyCmptType coverageType = newPolicyCmptType(project, "Coverage");

        IRelation policyToCoverage = policyType.newRelation();
        policyToCoverage.setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
        policyToCoverage.setMinCardinality(1);
        policyToCoverage.setMinCardinality(Integer.MAX_VALUE);
        policyToCoverage.setProductRelevant(false);
        policyToCoverage.setReadOnlyContainer(true);
        policyToCoverage.setTarget(coverageType.getQualifiedName());
        policyToCoverage.setTargetRoleSingular("Coverage");
        policyToCoverage.setTargetRolePlural("Coverages");
        policyToCoverage.setInverseRelation("Policy");
        
        IRelation coverageToPolicy = coverageType.newRelation();
        coverageToPolicy.setRelationType(RelationType.COMPOSITION_DETAIL_TO_MASTER);
        coverageToPolicy.setMinCardinality(1);
        coverageToPolicy.setMinCardinality(1);
        coverageToPolicy.setProductRelevant(false);
        coverageToPolicy.setReadOnlyContainer(true);
        coverageToPolicy.setTarget(policyType.getQualifiedName());
        coverageToPolicy.setTargetRoleSingular("Policy");
        coverageToPolicy.setTargetRolePlural("Policies");
        coverageToPolicy.setInverseRelation("Coverage");
        
        IPolicyCmptType homePolicyType = newPolicyCmptType(project, "HomePolicy");
        homePolicyType.setSupertype(policyType.getQualifiedName());
        IPolicyCmptType homeCoverageType = newPolicyCmptType(project, "HomeCoverage");
        homeCoverageType.setSupertype(coverageType.getQualifiedName());
        
        IRelation homePolicyToCoverage = homePolicyType.newRelation();
        homePolicyToCoverage.setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
        homePolicyToCoverage.setMinCardinality(1);
        homePolicyToCoverage.setMinCardinality(Integer.MAX_VALUE);
        homePolicyToCoverage.setProductRelevant(false);
        homePolicyToCoverage.setReadOnlyContainer(false);
        homePolicyToCoverage.setTarget(homeCoverageType.getQualifiedName());
        homePolicyToCoverage.setTargetRoleSingular("HomeCoverage");
        homePolicyToCoverage.setTargetRolePlural("HomeCoverages");
        homePolicyToCoverage.setContainerRelation(policyToCoverage.getName());
        homePolicyToCoverage.setInverseRelation("HomePolicy");
        
        IRelation homeCoverageToPolicy = homeCoverageType.newRelation();
        homeCoverageToPolicy.setRelationType(RelationType.COMPOSITION_DETAIL_TO_MASTER);
        homeCoverageToPolicy.setMinCardinality(1);
        homeCoverageToPolicy.setMinCardinality(1);
        homeCoverageToPolicy.setProductRelevant(false);
        homeCoverageToPolicy.setReadOnlyContainer(false);
        homeCoverageToPolicy.setContainerRelation(coverageToPolicy.getName());
        homeCoverageToPolicy.setTarget(homePolicyType.getQualifiedName());
        homeCoverageToPolicy.setTargetRoleSingular("HomePolicy");
        homeCoverageToPolicy.setTargetRolePlural("HomePolicies");
        homeCoverageToPolicy.setInverseRelation("HomeCoverage");
        
        MessageList ml = homePolicyToCoverage.validate();
        assertNull(ml.getMessageByCode(IRelation.MSGCODE_CONTAINERRELATION_REVERSERELATION_MISMATCH));
        
        // implementing revsere relation does not specify a container relation 
        homeCoverageToPolicy.setContainerRelation("");
        ml = homePolicyToCoverage.validate();
        assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_CONTAINERRELATION_REVERSERELATION_MISMATCH));
        homeCoverageToPolicy.setContainerRelation(coverageToPolicy.getName());
        ml = homePolicyToCoverage.validate();
        assertNull(ml.getMessageByCode(IRelation.MSGCODE_CONTAINERRELATION_REVERSERELATION_MISMATCH));
                
        // implementing revsere relation does specify a different container reverse relation (but container does)
        homeCoverageToPolicy.setContainerRelation("someContainerRel");
        ml = homePolicyToCoverage.validate();
        assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_CONTAINERRELATION_REVERSERELATION_MISMATCH));
    }
    
    public void testValidateReverseRelationMismatch() throws Exception {
        IRelation rel2 = content.getCoverage().newRelation();
        rel2.setTargetRoleSingular("test");
        relation.setInverseRelation("test");
        MessageList ml = relation.validate();
        assertNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_RELATION_MISMATCH)); // applies only to associations

        relation.setRelationType(RelationType.ASSOCIATION);
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_RELATION_MISMATCH));
        
        rel2.setInverseRelation(relation.getTargetRoleSingular());
        ml = relation.validate();
        assertNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_RELATION_MISMATCH));
    }

    public void testRemove() {
        assertEquals(1, pcType.getRelations().length);
        relation.delete();
        assertEquals(0, pcType.getRelations().length);
        assertTrue(relation.getIpsObject().getIpsSrcFile().isDirty());
    }
    
    public void testSetType() {
        relation.setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
        relation.setRelationType(RelationType.ASSOCIATION);
        assertEquals(RelationType.ASSOCIATION, relation.getRelationType());
        assertTrue(relation.getIpsObject().getIpsSrcFile().isDirty());
    }
    
    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        relation.initFromXml((Element)doc.getDocumentElement());
        assertEquals(42, relation.getId());
        assertEquals(RelationType.ASSOCIATION, relation.getRelationType());
        assertTrue(relation.isReadOnlyContainer());
        assertEquals("MotorPart", relation.getTarget());
        assertEquals("blabla", relation.getDescription());
        assertEquals("PolicyPart", relation.getTargetRoleSingular());
        assertEquals("PolicyParts", relation.getTargetRolePlural());
        assertEquals(1, relation.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, relation.getMaxCardinality());
        assertFalse(relation.isProductRelevant());
        assertEquals("Policy.Parts", relation.getContainerRelation());
        assertEquals("Part.Policy", relation.getInverseRelation());
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
        relation.setRelationType(RelationType.ASSOCIATION);
        relation.setReadOnlyContainer(true);
        relation.setTarget("target");
        relation.setTargetRoleSingular("targetRoleSingular");
        relation.setTargetRolePlural("targetRolePlural");
        relation.setProductRelevant(false);
        relation.setContainerRelation("super");
        relation.setInverseRelation("reverse");
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
        assertEquals(RelationType.ASSOCIATION, copy.getRelationType());
        assertTrue(copy.isReadOnlyContainer());
        assertEquals("target", copy.getTarget());
        assertEquals("targetRoleSingular", copy.getTargetRoleSingular());
        assertEquals("targetRolePlural", copy.getTargetRolePlural());
        assertEquals("super", copy.getContainerRelation());
        assertEquals("reverse", copy.getInverseRelation());
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
    
    /**
     * Test of searching the correct container relation candidates.
     */
    public void testSearchContainerRelationCandidates() throws Exception {
        IPolicyCmptType contract = content.getContract();
        IPolicyCmptType coverage = content.getCoverage();
        IPolicyCmptType motorContract = content.getMotorContract();
        IPolicyCmptType collisionCoverage = content.getCollisionCoverage();
        IPolicyCmptType vehicle = content.getVehicle();
        
        // Setup test objects (clear existing relation)
        clearAllRelations(contract);
        clearAllRelations(coverage);
        clearAllRelations(motorContract);
        clearAllRelations(collisionCoverage);
        clearAllRelations(vehicle);
        
        // New relation which will be used to get the container relation candidates
        IRelation motorContract2CollisionCoverage = motorContract.newRelation();
        motorContract2CollisionCoverage.setTarget(collisionCoverage.getQualifiedName());
        motorContract2CollisionCoverage.setTargetRoleSingular("CollisionCoverage");
        
        
        // Non container relation on supertype
        IRelation contract2Coverage = contract.newRelation();
        contract2Coverage.setTarget(coverage.getQualifiedName());
        contract2Coverage.setReadOnlyContainer(false);
        contract2Coverage.setTargetRoleSingular("Coverage");
        
        // Container relation on supertype with target in supertype hierarchy of rel. target
        IRelation contRelContract2Coverage = contract.newRelation();
        contRelContract2Coverage.setTarget(coverage.getQualifiedName());
        contRelContract2Coverage.setTargetRoleSingular("CoverageContainer");
        contRelContract2Coverage.setReadOnlyContainer(true);
        
        // Container relation on supertype with other target as rel. target
        IRelation contRelContract2vehicle = contract.newRelation();
        contRelContract2vehicle.setTarget(vehicle.getQualifiedName());
        contRelContract2vehicle.setTargetRoleSingular("VehicleContainer");
        contRelContract2vehicle.setReadOnlyContainer(true);
        
        // ==> check if the container relation of the super type and the container rel to the target
        //     will be returned as container candidate for the new relation
        IRelation[] containerRelationCandidates = motorContract2CollisionCoverage.findContainerRelationCandidates();
        assertEquals(1, containerRelationCandidates.length);
        assertEquals(contRelContract2Coverage, containerRelationCandidates[0]);
        
        // Container relation on supertype with target equal rel. target
        IRelation contRelContract2CollisionCoverage = contract.newRelation();
        contRelContract2CollisionCoverage.setTarget(collisionCoverage.getQualifiedName());
        contRelContract2CollisionCoverage.setTargetRoleSingular("CollisionCoverageContainer");
        contRelContract2CollisionCoverage.setReadOnlyContainer(true);
        
        // Container relation to target on policy cmpt the new relation belongs to
        IRelation contRelMotorContract2CollisionCoverage = motorContract.newRelation();
        contRelMotorContract2CollisionCoverage.setTarget(collisionCoverage.getQualifiedName());
        contRelMotorContract2CollisionCoverage.setTargetRoleSingular("CollisionCoverageContainer");
        contRelMotorContract2CollisionCoverage.setReadOnlyContainer(true);
        
        // Container relation not to target on policy cmpt the new relation belongs to
        IRelation contRelMotorContract2Vehicle = motorContract.newRelation();
        contRelMotorContract2Vehicle.setTarget(vehicle.getQualifiedName());
        contRelMotorContract2Vehicle.setTargetRoleSingular("VehicleContainer");
        contRelMotorContract2Vehicle.setReadOnlyContainer(true);
        
        // ==> check if the container relation of the super type and the container rel to the target
        //     will be returned as container candidate for the new relation
        containerRelationCandidates = motorContract2CollisionCoverage.findContainerRelationCandidates();
        assertEquals(3, containerRelationCandidates.length);
        List result = Arrays.asList(containerRelationCandidates);
        assertTrue(result.contains(contRelContract2CollisionCoverage));
        assertTrue(result.contains(contRelMotorContract2CollisionCoverage));
        assertTrue(result.contains(contRelContract2Coverage));
        assertFalse(result.contains(contRelMotorContract2Vehicle));
    }

    private void clearAllRelations(IPolicyCmptType pcType){
        IRelation[] relations = pcType.getRelations();
        for (int i = 0; i < relations.length; i++) {
            relations[i].delete();
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
    	relation.setRelationType(RelationType.COMPOSITION_DETAIL_TO_MASTER);
    	
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION));
    	
    	relation.setMaxCardinality(1);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION));
    }
    
    public void testValidateReverseCompositionProductRelevant() throws Exception {
    	MessageList ml = new MessageList();
    	
    	relation.setRelationType(RelationType.COMPOSITION_DETAIL_TO_MASTER);
    	ml = relation.validate();
    	
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_COMPOSITION_CANT_BE_MARKED_AS_PRODUCT_RELEVANT));
    	
    	relation.setProductRelevant(false);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_COMPOSITION_CANT_BE_MARKED_AS_PRODUCT_RELEVANT));
    }

    public void testValidateProductRelevant() throws Exception {
    	MessageList ml = new MessageList();
    	
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_RELATION_CAN_ONLY_BE_PRODUCT_RELEVANT_IF_THE_TYPE_IS));
    	
    	relation.getPolicyCmptType().setConfigurableByProductCmptType(false);
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_RELATION_CAN_ONLY_BE_PRODUCT_RELEVANT_IF_THE_TYPE_IS));
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
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_SAME_PLURAL_ROLENAME));
		
		relation.setTargetRolePlural(cRel.getTargetRolePlural());
		ml = cRel.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_SAME_PLURAL_ROLENAME));
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
		relation.setInverseRelation("abc");
		MessageList ml = relation.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSERELATION_NOT_IN_TARGET));
		
		relation.setInverseRelation("test");
		ml = relation.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSERELATION_NOT_IN_TARGET));
	}

	public void testValidateReverseRelationOfContainerRelationHasToBeContainerRelationToo() throws Exception {
		IRelation rel2 = content.getCoverage().newRelation();
		rel2.setTargetRoleSingular("test");
		rel2.setInverseRelation(relation.getTargetRoleSingular());
		relation.setInverseRelation("test");
		relation.setReadOnlyContainer(true);
		MessageList ml = relation.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_FORWARD_AND_REVERSE_RELATION_MUST_BOTH_BE_MARKED_AS_CONTAINER));
		rel2.setReadOnlyContainer(true);
		ml = relation.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_FORWARD_AND_REVERSE_RELATION_MUST_BOTH_BE_MARKED_AS_CONTAINER));
	}

	public void testValidateReverseCompositionMissmatch() throws Exception {
		IRelation rel2 = content.getCoverage().newRelation();
		rel2.setTargetRoleSingular("test");
		rel2.setInverseRelation(relation.getTargetRoleSingular());
		rel2.setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
		relation.setInverseRelation("test");
		relation.setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
		MessageList ml = relation.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_COMPOSITION_MISSMATCH));
		rel2.setRelationType(RelationType.COMPOSITION_DETAIL_TO_MASTER);
		ml = relation.validate();
		assertNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_COMPOSITION_MISSMATCH));
	}

	public void testValidateReverseAssociationMissmatch() throws Exception {
		IRelation rel2 = content.getCoverage().newRelation();
		rel2.setTargetRoleSingular("test");
		rel2.setInverseRelation(relation.getTargetRoleSingular());
		rel2.setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
		relation.setInverseRelation("test");
		relation.setRelationType(RelationType.ASSOCIATION);
		MessageList ml = relation.validate();
		assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_REVERSE_ASSOCIATION_MISSMATCH));
		rel2.setRelationType(RelationType.ASSOCIATION);
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
        assertNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_ROLE_PLURAL_PRODUCTSIDE_EQUALS_TARGET_ROLE_SINGULAR_PRODUCTSIDE));
        
        relation.setTargetRoleSingularProductSide("a");
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_ROLE_PLURAL_PRODUCTSIDE_EQUALS_TARGET_ROLE_SINGULAR_PRODUCTSIDE));
    }

    public void testValidationRelationCanOnlyBeProductRelevantIfTheTargetTypeIs() throws Exception {
    	// per default both the relation and the target type are product relevant, therefore the msg is not expected
        MessageList ml = relation.validate();
        assertNull(ml.getMessageByCode(IRelation.MSGCODE_RELATION_CAN_ONLY_BE_PRODUCT_RELEVANT_IF_THE_TARGET_TYPE_IS));
       
        relation.findTarget().setConfigurableByProductCmptType(false);
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_RELATION_CAN_ONLY_BE_PRODUCT_RELEVANT_IF_THE_TARGET_TYPE_IS));
       
    }
    
    public void testValidationTargetRolePlural_EqualsTargetRoleSingular() throws Exception {
    	relation.setTargetRoleSingular("role1");
    	relation.setTargetRolePlural("role2");
    	MessageList ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR));
    	
    	relation.setTargetRolePlural("role1");
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR));
    }

    public void testValidationTargetRolePluralMustBeSet() throws Exception {
    	relation.setMinCardinality(1);
    	relation.setMaxCardinality(1);
    	relation.setTargetRolePlural("");
    	MessageList ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));
    	
    	relation.setMaxCardinality(2);
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));
    	
    	relation.setTargetRolePlural("role1");
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IRelation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));
    }
    
    public void testIsContainerRelationImplementation() throws CoreException {
        assertFalse(relation.isContainerRelationImplementation(null));

        IRelation containerRelation = this.pcType.newRelation();
        containerRelation.setTargetRoleSingular("Target");
        try {
            relation.isContainerRelationImplementation(containerRelation);
            fail();
        } catch (CoreException e) {
        }
        
        containerRelation.setReadOnlyContainer(true);
        assertFalse(relation.isContainerRelationImplementation(containerRelation));
        
        relation.setContainerRelation(containerRelation.getName());
        assertTrue(relation.isContainerRelationImplementation(containerRelation));
        
        // check if the method returns false if the container relation has the same name but belongs to a 
        // different type
        IPolicyCmptType otherType = newPolicyCmptType(this.pcType.getIpsProject(), "OtherType");
        IRelation otherRelation = otherType.newRelation();
        otherRelation.setReadOnlyContainer(true);
        otherRelation.setTargetRoleSingular("Target");
        assertFalse(relation.isContainerRelationImplementation(otherRelation));
    }
    
    public void testFindReverseRelation() throws CoreException {
        relation.setInverseRelation("");
        assertNull(relation.findInverseRelation());

        relation.setInverseRelation("reverseRelation");
        assertNull(relation.findInverseRelation());
        
        IPolicyCmptType targetType = (IPolicyCmptType)newIpsObject(this.pcType.getIpsProject(), IpsObjectType.POLICY_CMPT_TYPE, "pack2.MotorPolicy");
        relation.setTarget(targetType.getQualifiedName());
        assertNull(relation.findInverseRelation());
        
        IRelation relation2 = targetType.newRelation();
        relation2.setTargetRoleSingular("reverseRelation");
        assertEquals(relation2, relation.findInverseRelation());
        
        relation.setRelationType(RelationType.COMPOSITION_DETAIL_TO_MASTER);
        assertNull(relation.findInverseRelation());
        
    }
    

}
