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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class PolicyCmptTypeAssociationTest extends AbstractIpsPluginTest {
    
    private IIpsProject ipsProject;
    private IPolicyCmptType pcType;
    private IPolicyCmptType targetType;
    private IPolicyCmptTypeAssociation relation;
    private IPolicyCmptTypeAssociation implementationRelation;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        pcType = newPolicyCmptType(ipsProject, "Policy");
        relation = pcType.newPolicyCmptTypeAssociation();
        relation.setTargetRoleSingular("Coverage");
        targetType = newPolicyCmptType(ipsProject, "Coverage");
        relation.setTarget(targetType.getQualifiedName());
        
        IPolicyCmptType motorPolicyType = newPolicyCmptType(ipsProject, "MotorPolicy");
        motorPolicyType.setSupertype(pcType.getQualifiedName());
        IPolicyCmptType collisionCoverageType = newPolicyCmptType(ipsProject, "CollisionCoverage");
        collisionCoverageType.setSupertype(targetType.getQualifiedName());
        implementationRelation = motorPolicyType.newPolicyCmptTypeAssociation();
        implementationRelation.setTargetRoleSingular("CollisionCoverage");
        implementationRelation.setTarget(collisionCoverageType.getQualifiedName());
    }
    
    public void testIsQualificationPossible() throws CoreException {
        relation.setTarget("UnknownTarget");
        assertFalse(relation.isQualificationPossible(ipsProject));
        
        relation.setTarget(targetType.getQualifiedName());
        targetType.setConfigurableByProductCmptType(false);
        assertFalse(relation.isQualificationPossible(ipsProject));

        IProductCmptType productCmptType = newProductCmptType(ipsProject, "producttypes.CoverageType");
        targetType.setProductCmptType(productCmptType.getQualifiedName());
        targetType.setConfigurableByProductCmptType(true);
        relation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertFalse(relation.isQualificationPossible(ipsProject));

        relation.setAssociationType(AssociationType.ASSOCIATION);
        assertFalse(relation.isQualificationPossible(ipsProject));
        
        relation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertTrue(relation.isQualificationPossible(ipsProject));
    }
    
    public void testFindQualifierCandidate() throws CoreException {
        relation.setQualified(false);
        relation.setTarget("UnknownTarget");
        assertEquals("", relation.findQualifierCandidate(ipsProject));
        
        relation.setTarget(targetType.getQualifiedName());
        targetType.setConfigurableByProductCmptType(false);
        assertEquals("", relation.findQualifierCandidate(ipsProject));
        
        targetType.setConfigurableByProductCmptType(true);
        targetType.setProductCmptType("UnkownType");
        assertEquals("UnkownType", relation.findQualifierCandidate(ipsProject));
        
        relation.setQualified(true);
        assertEquals("UnkownType", relation.findQualifierCandidate(ipsProject));
    }
    
    public void testFindQualifier() throws CoreException {
        relation.setQualified(false);
        assertNull(relation.findQualifier(ipsProject));

        relation.setQualified(true);
        relation.setTarget("UnknownTarget");
        assertNull(relation.findQualifier(ipsProject));
        
        relation.setTarget(targetType.getQualifiedName());
        targetType.setConfigurableByProductCmptType(false);
        assertNull(relation.findQualifier(ipsProject));
        
        targetType.setConfigurableByProductCmptType(true);
        targetType.setProductCmptType("UnkownType");
        assertNull(relation.findQualifier(ipsProject));
        
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "producttypes.CoverageType");
        targetType.setProductCmptType(productCmptType.getQualifiedName());
        
        assertEquals(productCmptType, relation.findQualifier(ipsProject));
    }
    
    public void testSetQualified() {
        testPropertyAccessReadWrite(PolicyCmptTypeAssociation.class, IPolicyCmptTypeAssociation.PROPERTY_QUALIFIED, relation, Boolean.TRUE);
    }
    
    public void testValidateContainerRelation_ReverseRelation_Mismtach() throws CoreException {
        IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "my.Policy");
        IPolicyCmptType coverageType = newPolicyCmptType(ipsProject, "my.Coverage");

        IPolicyCmptTypeAssociation policyToCoverage = policyType.newPolicyCmptTypeAssociation();
        policyToCoverage.setAssociationType(AssociationType.ASSOCIATION);
        policyToCoverage.setMinCardinality(1);
        policyToCoverage.setMinCardinality(Integer.MAX_VALUE);
        policyToCoverage.setProductRelevant(false);
        policyToCoverage.setDerivedUnion(true);
        policyToCoverage.setTarget(coverageType.getQualifiedName());
        policyToCoverage.setTargetRoleSingular("Coverage");
        policyToCoverage.setTargetRolePlural("Coverages");
        policyToCoverage.setInverseRelation("Policy");
        
        IPolicyCmptTypeAssociation coverageToPolicy = coverageType.newPolicyCmptTypeAssociation();
        coverageToPolicy.setAssociationType(AssociationType.ASSOCIATION);
        coverageToPolicy.setMinCardinality(1);
        coverageToPolicy.setMinCardinality(1);
        coverageToPolicy.setProductRelevant(false);
        coverageToPolicy.setDerivedUnion(true);
        coverageToPolicy.setTarget(policyType.getQualifiedName());
        coverageToPolicy.setTargetRoleSingular("Policy");
        coverageToPolicy.setTargetRolePlural("Policies");
        coverageToPolicy.setInverseRelation("Coverage");
        
        IPolicyCmptType homePolicyType = newPolicyCmptType(ipsProject, "HomePolicy");
        homePolicyType.setSupertype(policyType.getQualifiedName());
        IPolicyCmptType homeCoverageType = newPolicyCmptType(ipsProject, "HomeCoverage");
        homeCoverageType.setSupertype(coverageType.getQualifiedName());
        
        IPolicyCmptTypeAssociation homePolicyToCoverage = homePolicyType.newPolicyCmptTypeAssociation();
        homePolicyToCoverage.setAssociationType(AssociationType.ASSOCIATION);
        homePolicyToCoverage.setMinCardinality(1);
        homePolicyToCoverage.setMinCardinality(Integer.MAX_VALUE);
        homePolicyToCoverage.setProductRelevant(false);
        homePolicyToCoverage.setDerivedUnion(false);
        homePolicyToCoverage.setTarget(homeCoverageType.getQualifiedName());
        homePolicyToCoverage.setTargetRoleSingular("HomeCoverage");
        homePolicyToCoverage.setTargetRolePlural("HomeCoverages");
        homePolicyToCoverage.setSubsettedDerivedUnion(policyToCoverage.getName());
        homePolicyToCoverage.setInverseRelation("HomePolicy");
        
        IPolicyCmptTypeAssociation homeCoverageToPolicy = homeCoverageType.newPolicyCmptTypeAssociation();
        homeCoverageToPolicy.setAssociationType(AssociationType.ASSOCIATION);
        homeCoverageToPolicy.setMinCardinality(1);
        homeCoverageToPolicy.setMinCardinality(1);
        homeCoverageToPolicy.setProductRelevant(false);
        homeCoverageToPolicy.setDerivedUnion(false);
        homeCoverageToPolicy.setSubsettedDerivedUnion(coverageToPolicy.getName());
        homeCoverageToPolicy.setTarget(homePolicyType.getQualifiedName());
        homeCoverageToPolicy.setTargetRoleSingular("HomePolicy");
        homeCoverageToPolicy.setTargetRolePlural("HomePolicies");
        homeCoverageToPolicy.setInverseRelation("HomeCoverage");
        
        MessageList ml = homePolicyToCoverage.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INCONSTENT_WITH_DEFINITION_CONTAINER_RELATION));
        
        // implementing revsere relation does not specify a container relation 
        homeCoverageToPolicy.setSubsettedDerivedUnion("");
        ml = homePolicyToCoverage.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INCONSTENT_WITH_DEFINITION_CONTAINER_RELATION));
        homeCoverageToPolicy.setSubsettedDerivedUnion(coverageToPolicy.getName());
        ml = homePolicyToCoverage.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INCONSTENT_WITH_DEFINITION_CONTAINER_RELATION));
                
        // implementing revsere relation does specify a different container reverse relation (but container does)
        homeCoverageToPolicy.setSubsettedDerivedUnion("someContainerRel");
        ml = homePolicyToCoverage.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INCONSTENT_WITH_DEFINITION_CONTAINER_RELATION));
    }
    
    public void testValidateInverseRelationMismatch() throws Exception {
        IIpsProject ipsProject = newIpsProject("TestValidateInverseRelationMismatch");
        IPolicyCmptType typeA = newPolicyCmptType(ipsProject, "A");
        IPolicyCmptType typeB = newPolicyCmptType(ipsProject, "B");
        IPolicyCmptTypeAssociation relationAtoB = typeA.newPolicyCmptTypeAssociation();
        relationAtoB.setAssociationType(AssociationType.ASSOCIATION);
        relationAtoB.setProductRelevant(false);
        relationAtoB.setTarget("B");
        relationAtoB.setTargetRoleSingular("roleB");
        relationAtoB.setTargetRolePlural("roleBs");
        
        IPolicyCmptTypeAssociation relationBtoA = typeB.newPolicyCmptTypeAssociation();
        relationBtoA.setAssociationType(AssociationType.ASSOCIATION);
        relationBtoA.setProductRelevant(false);
        relationBtoA.setTarget("B");
        relationBtoA.setTargetRoleSingular("roleA");
        relationBtoA.setTargetRolePlural("roleAs");

        // mismatch: B does does refer to A
        relationAtoB.setInverseRelation("roleA");
        relationBtoA.setInverseRelation("somethingElse");
        MessageList ml = relationAtoB.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH)); 

        // now it fits
        relationBtoA.setInverseRelation("roleB");
        ml = relationAtoB.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH)); 

        // rule applies only to assoziations.
        relationAtoB.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        relationBtoA.setInverseRelation("somethingElse"); // 
        ml = relationAtoB.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));
        
        relationAtoB.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        ml = relationAtoB.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));
    }

    public void testValidateInverseRelationNotFoundInTarget() throws Exception {
        IIpsProject ipsProject = newIpsProject("testValidateInverseRelationNotFoundInTarget");
        IPolicyCmptType typeA = newPolicyCmptType(ipsProject, "A");
        IPolicyCmptType typeB = newPolicyCmptType(ipsProject, "B");
        IPolicyCmptTypeAssociation relationAtoB = typeA.newPolicyCmptTypeAssociation();
        relationAtoB.setAssociationType(AssociationType.ASSOCIATION);
        relationAtoB.setProductRelevant(false);
        relationAtoB.setTarget("B");
        relationAtoB.setTargetRoleSingular("roleB");
        relationAtoB.setTargetRoleSingular("roleBs");
        
        MessageList ml = relationAtoB.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));
        
        relationAtoB.setInverseRelation("roleB");
        ml = relationAtoB.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));

        IPolicyCmptTypeAssociation relationBtoA = typeB.newPolicyCmptTypeAssociation();
        relationBtoA.setAssociationType(AssociationType.ASSOCIATION);
        relationBtoA.setProductRelevant(false);
        relationBtoA.setTarget("B");
        relationBtoA.setTargetRoleSingular("somethingThatIsNotA");
        relationBtoA.setTargetRoleSingular("somethingThatIsNotAs");
        ml = relationAtoB.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));
        
        relationBtoA.setTargetRoleSingular("roleB");
        ml = relationAtoB.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));
    }    

    public void testIsInverseRelationApplicable() throws CoreException {
        relation = pcType.newPolicyCmptTypeAssociation();

        relation.setAssociationType(AssociationType.ASSOCIATION);
        assertTrue(relation.isInverseRelationApplicable());
        
        relation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertFalse(relation.isInverseRelationApplicable());

        TestIpsArtefactBuilderSet builderset = new TestIpsArtefactBuilderSet();
        setArtefactBuildset(pcType.getIpsProject(), builderset);
        
        relation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        builderset.setInverseRelationLinkRequiredFor2WayCompositions(false);
        assertFalse(relation.isInverseRelationApplicable()); 

        builderset.setInverseRelationLinkRequiredFor2WayCompositions(true);
        assertTrue(relation.isInverseRelationApplicable()); 
    }
    
    public void testValidateInverseRelationNotNeeded() throws Exception {
        relation.setAssociationType(AssociationType.ASSOCIATION);
        relation.setInverseRelation("something");
        
        MessageList ml = relation.validate();
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INFO_NOT_NEEDED));
        
        relation.setInverseRelation("");
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INFO_NOT_NEEDED));

        relation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        relation.setInverseRelation("something");
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INFO_NOT_NEEDED));
        
        relation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INFO_NOT_NEEDED));
    }
    
    public void testRemove() {
        assertEquals(1, pcType.getPolicyCmptTypeAssociations().length);
        relation.delete();
        assertEquals(0, pcType.getPolicyCmptTypeAssociations().length);
        assertTrue(relation.getIpsObject().getIpsSrcFile().isDirty());
    }
    
    public void testSetType() {
        relation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        relation.setAssociationType(AssociationType.ASSOCIATION);
        assertEquals(AssociationType.ASSOCIATION, relation.getAssociationType());
        assertTrue(relation.getIpsObject().getIpsSrcFile().isDirty());
    }
    
    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        relation.initFromXml((Element)doc.getDocumentElement());
        assertEquals(42, relation.getId());
        assertEquals(AssociationType.ASSOCIATION, relation.getAssociationType());
        assertTrue(relation.isDerivedUnion());
        assertTrue(relation.isQualified());
        assertEquals("MotorPart", relation.getTarget());
        assertEquals("blabla", relation.getDescription());
        assertEquals("PolicyPart", relation.getTargetRoleSingular());
        assertEquals("PolicyParts", relation.getTargetRolePlural());
        assertEquals(1, relation.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, relation.getMaxCardinality());
        assertEquals("Policy.Parts", relation.getSubsettedDerivedUnion());
        assertEquals("Part.Policy", relation.getInverseRelation());
    }

    public void testToXml() {
        relation = pcType.newPolicyCmptTypeAssociation(); 
        relation.setAssociationType(AssociationType.ASSOCIATION);
        relation.setDerivedUnion(true);
        relation.setQualified(true);
        relation.setTarget("target");
        relation.setTargetRoleSingular("targetRoleSingular");
        relation.setTargetRolePlural("targetRolePlural");
        relation.setSubsettedDerivedUnion("super");
        relation.setInverseRelation("reverse");
        relation.setMinCardinality(2);
        relation.setMaxCardinality(3);
        relation.setDescription("blabla");
        
        Element element = relation.toXml(this.newDocument());
        
        IPolicyCmptTypeAssociation copy = pcType.newPolicyCmptTypeAssociation();
        copy.initFromXml(element);
        assertEquals(AssociationType.ASSOCIATION, copy.getAssociationType());
        assertTrue(copy.isDerivedUnion());
        assertTrue(copy.isQualified());
        assertEquals("target", copy.getTarget());
        assertEquals("targetRoleSingular", copy.getTargetRoleSingular());
        assertEquals("targetRolePlural", copy.getTargetRolePlural());
        assertEquals("super", copy.getSubsettedDerivedUnion());
        assertEquals("reverse", copy.getInverseRelation());
        assertEquals(2, copy.getMinCardinality());
        assertEquals(3, copy.getMaxCardinality());
        assertEquals("blabla", copy.getDescription());
            }
    
    /**
     * Tests for the correct type of excetion to be thrwon - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			relation.newPart(IPolicyCmptTypeAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    // TODO
//    /**
//     * Test of searching the correct container relation candidates.
//     */
//    public void testSearchContainerRelationCandidates() throws Exception {
//        IPolicyCmptType contract = content.getContract();
//        IPolicyCmptType coverage = content.getCoverage();
//        IPolicyCmptType motorContract = content.getMotorContract();
//        IPolicyCmptType collisionCoverage = content.getCollisionCoverage();
//        IPolicyCmptType vehicle = content.getVehicle();
//        
//        // Setup test objects (clear existing relation)
//        clearAllRelations(contract);
//        clearAllRelations(coverage);
//        clearAllRelations(motorContract);
//        clearAllRelations(collisionCoverage);
//        clearAllRelations(vehicle);
//        
//        // New relation which will be used to get the container relation candidates
//        IRelation motorContract2CollisionCoverage = motorContract.newRelation();
//        motorContract2CollisionCoverage.setTarget(collisionCoverage.getQualifiedName());
//        motorContract2CollisionCoverage.setTargetRoleSingular("CollisionCoverage");
//        
//        
//        // Non container relation on supertype
//        IRelation contract2Coverage = contract.newRelation();
//        contract2Coverage.setTarget(coverage.getQualifiedName());
//        contract2Coverage.setReadOnlyContainer(false);
//        contract2Coverage.setTargetRoleSingular("Coverage");
//        
//        // Container relation on supertype with target in supertype hierarchy of rel. target
//        IRelation contRelContract2Coverage = contract.newRelation();
//        contRelContract2Coverage.setTarget(coverage.getQualifiedName());
//        contRelContract2Coverage.setTargetRoleSingular("CoverageContainer");
//        contRelContract2Coverage.setReadOnlyContainer(true);
//        
//        // Container relation on supertype with other target as rel. target
//        IRelation contRelContract2vehicle = contract.newRelation();
//        contRelContract2vehicle.setTarget(vehicle.getQualifiedName());
//        contRelContract2vehicle.setTargetRoleSingular("VehicleContainer");
//        contRelContract2vehicle.setReadOnlyContainer(true);
//        
//        // ==> check if the container relation of the super type and the container rel to the target
//        //     will be returned as container candidate for the new relation
//        IRelation[] containerRelationCandidates = motorContract2CollisionCoverage.findContainerRelationCandidates();
//        assertEquals(1, containerRelationCandidates.length);
//        assertEquals(contRelContract2Coverage, containerRelationCandidates[0]);
//        
//        // Container relation on supertype with target equal rel. target
//        IRelation contRelContract2CollisionCoverage = contract.newRelation();
//        contRelContract2CollisionCoverage.setTarget(collisionCoverage.getQualifiedName());
//        contRelContract2CollisionCoverage.setTargetRoleSingular("CollisionCoverageContainer");
//        contRelContract2CollisionCoverage.setReadOnlyContainer(true);
//        
//        // Container relation to target on policy cmpt the new relation belongs to
//        IRelation contRelMotorContract2CollisionCoverage = motorContract.newRelation();
//        contRelMotorContract2CollisionCoverage.setTarget(collisionCoverage.getQualifiedName());
//        contRelMotorContract2CollisionCoverage.setTargetRoleSingular("CollisionCoverageContainer");
//        contRelMotorContract2CollisionCoverage.setReadOnlyContainer(true);
//        
//        // Container relation not to target on policy cmpt the new relation belongs to
//        IRelation contRelMotorContract2Vehicle = motorContract.newRelation();
//        contRelMotorContract2Vehicle.setTarget(vehicle.getQualifiedName());
//        contRelMotorContract2Vehicle.setTargetRoleSingular("VehicleContainer");
//        contRelMotorContract2Vehicle.setReadOnlyContainer(true);
//        
//        // ==> check if the container relation of the super type and the container rel to the target
//        //     will be returned as container candidate for the new relation
//        containerRelationCandidates = motorContract2CollisionCoverage.findContainerRelationCandidates();
//        assertEquals(3, containerRelationCandidates.length);
//        List result = Arrays.asList(containerRelationCandidates);
//        assertTrue(result.contains(contRelContract2CollisionCoverage));
//        assertTrue(result.contains(contRelMotorContract2CollisionCoverage));
//        assertTrue(result.contains(contRelContract2Coverage));
//        assertFalse(result.contains(contRelMotorContract2Vehicle));
//    }
//
//    private void clearAllRelations(IPolicyCmptType pcType){
//        IPolicyCmptTypeAssociation[] relations = pcType.getPolicyCmptTypeAssociations();
//        for (int i = 0; i < relations.length; i++) {
//            relations[i].delete();
//        }
//    }
    
    public void testValidateMaxCardinalityForReverseComposition() throws Exception {
    	MessageList ml = new MessageList();
    	
    	relation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        relation.setMaxCardinality(2);
    	
    	ml = relation.validate();
    	assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION));
    	
    	relation.setMaxCardinality(1);
    	ml = relation.validate();
    	assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION));
    }
    
	public void testValidateSamePluralRolename() throws Exception {
		MessageList ml = new MessageList();
		relation.setDerivedUnion(true);
		implementationRelation.setTargetRolePlural("MotorCoverages");

		ml = implementationRelation.validate();
		assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_SAME_PLURAL_ROLENAME));
		
		relation.setTargetRolePlural(implementationRelation.getTargetRolePlural());
		ml = implementationRelation.validate();
		assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_SAME_PLURAL_ROLENAME));
	}

	public void testValidateSameSingularRolename() throws Exception {
		MessageList ml = new MessageList();

		ml = implementationRelation.validate();
		assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_SAME_SINGULAR_ROLENAME));
		
		implementationRelation.setTargetRoleSingular(relation.getTargetRoleSingular());
		ml = implementationRelation.validate();
		assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_SAME_SINGULAR_ROLENAME));
	}
	
	public void testValidateReverseRelationOfContainerRelationHasToBeContainerRelationToo() throws Exception {
		relation.setAssociationType(AssociationType.ASSOCIATION);
        IPolicyCmptTypeAssociation rel2 = targetType.newPolicyCmptTypeAssociation();
        rel2.setAssociationType(AssociationType.ASSOCIATION);
		rel2.setTargetRoleSingular("test");
		rel2.setInverseRelation(relation.getTargetRoleSingular());
		relation.setInverseRelation("test");
		relation.setDerivedUnion(true);
		MessageList ml = relation.validate();
		assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER));
		rel2.setDerivedUnion(true);
		ml = relation.validate();
		assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER));
	}

	public void testValidateInvesreAssociationTypeMissmatch() throws Exception {
		IPolicyCmptTypeAssociation rel2 = targetType.newPolicyCmptTypeAssociation();
		rel2.setTargetRoleSingular("test");
		rel2.setInverseRelation(relation.getTargetRoleSingular());
		rel2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
		relation.setInverseRelation("test");
		relation.setAssociationType(AssociationType.ASSOCIATION);
		MessageList ml = relation.validate();
		assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH));
		rel2.setAssociationType(AssociationType.ASSOCIATION);
		ml = relation.validate();
		assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH));
    }
	
    public void testFindInverseRelation() throws CoreException {
        relation.setInverseRelation("");
        assertNull(relation.findInverseRelation());

        relation.setInverseRelation("reverseRelation");
        assertNull(relation.findInverseRelation());
        
        IPolicyCmptType targetType = (IPolicyCmptType)newIpsObject(this.pcType.getIpsProject(), IpsObjectType.POLICY_CMPT_TYPE, "pack2.MotorPolicy");
        relation.setTarget(targetType.getQualifiedName());
        assertNull(relation.findInverseRelation());
        
        IPolicyCmptTypeAssociation relation2 = targetType.newPolicyCmptTypeAssociation();
        relation2.setTargetRoleSingular("reverseRelation");
        assertEquals(relation2, relation.findInverseRelation());
        
        relation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertNull(relation.findInverseRelation());
    }
    

}
