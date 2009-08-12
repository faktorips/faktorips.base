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

package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
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
    private IPolicyCmptTypeAssociation association;
    private IPolicyCmptTypeAssociation implementationAssociation;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        pcType = newPolicyCmptType(ipsProject, "Policy");
        association = pcType.newPolicyCmptTypeAssociation();
        association.setTargetRoleSingular("Coverage");
        targetType = newPolicyCmptType(ipsProject, "Coverage");
        association.setTarget(targetType.getQualifiedName());
        
        IPolicyCmptType motorPolicyType = newPolicyCmptType(ipsProject, "MotorPolicy");
        motorPolicyType.setSupertype(pcType.getQualifiedName());
        IPolicyCmptType collisionCoverageType = newPolicyCmptType(ipsProject, "CollisionCoverage");
        collisionCoverageType.setSupertype(targetType.getQualifiedName());
        implementationAssociation = motorPolicyType.newPolicyCmptTypeAssociation();
        implementationAssociation.setTargetRoleSingular("CollisionCoverage");
        implementationAssociation.setTarget(collisionCoverageType.getQualifiedName());
    }
    
    public void testIsQualificationPossible() throws CoreException {
        association.setTarget("UnknownTarget");
        assertFalse(association.isQualificationPossible(ipsProject));
        
        association.setTarget(targetType.getQualifiedName());
        targetType.setConfigurableByProductCmptType(false);
        assertFalse(association.isQualificationPossible(ipsProject));

        IProductCmptType productCmptType = newProductCmptType(ipsProject, "producttypes.CoverageType");
        targetType.setProductCmptType(productCmptType.getQualifiedName());
        targetType.setConfigurableByProductCmptType(true);
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertFalse(association.isQualificationPossible(ipsProject));

        association.setAssociationType(AssociationType.ASSOCIATION);
        assertFalse(association.isQualificationPossible(ipsProject));
        
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertTrue(association.isQualificationPossible(ipsProject));
    }
    
    public void testFindQualifierCandidate() throws CoreException {
        association.setQualified(false);
        association.setTarget("UnknownTarget");
        assertEquals("", association.findQualifierCandidate(ipsProject));
        
        association.setTarget(targetType.getQualifiedName());
        targetType.setConfigurableByProductCmptType(false);
        assertEquals("", association.findQualifierCandidate(ipsProject));
        
        targetType.setConfigurableByProductCmptType(true);
        targetType.setProductCmptType("UnkownType");
        assertEquals("UnkownType", association.findQualifierCandidate(ipsProject));
        
        association.setQualified(true);
        assertEquals("UnkownType", association.findQualifierCandidate(ipsProject));
    }
    
    public void testFindQualifier() throws CoreException {
        association.setQualified(false);
        assertNull(association.findQualifier(ipsProject));

        association.setQualified(true);
        association.setTarget("UnknownTarget");
        assertNull(association.findQualifier(ipsProject));
        
        association.setTarget(targetType.getQualifiedName());
        targetType.setConfigurableByProductCmptType(false);
        assertNull(association.findQualifier(ipsProject));
        
        targetType.setConfigurableByProductCmptType(true);
        targetType.setProductCmptType("UnkownType");
        assertNull(association.findQualifier(ipsProject));
        
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "producttypes.CoverageType");
        targetType.setProductCmptType(productCmptType.getQualifiedName());
        
        assertEquals(productCmptType, association.findQualifier(ipsProject));
    }
    
    public void testSetQualified() {
        testPropertyAccessReadWrite(PolicyCmptTypeAssociation.class, IPolicyCmptTypeAssociation.PROPERTY_QUALIFIED, association, Boolean.TRUE);
    }
    
    public void testValidateContainerRelation_ReverseRelation_Mismtach() throws CoreException {
        IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "my.Policy");
        IPolicyCmptType coverageType = newPolicyCmptType(ipsProject, "my.Coverage");

        IPolicyCmptTypeAssociation policyToCoverage = policyType.newPolicyCmptTypeAssociation();
        policyToCoverage.setAssociationType(AssociationType.ASSOCIATION);
        policyToCoverage.setMinCardinality(1);
        policyToCoverage.setMinCardinality(Integer.MAX_VALUE);
        policyToCoverage.setDerivedUnion(true);
        policyToCoverage.setTarget(coverageType.getQualifiedName());
        policyToCoverage.setTargetRoleSingular("Coverage");
        policyToCoverage.setTargetRolePlural("Coverages");
        policyToCoverage.setInverseAssociation("Policy");
        
        IPolicyCmptTypeAssociation coverageToPolicy = coverageType.newPolicyCmptTypeAssociation();
        coverageToPolicy.setAssociationType(AssociationType.ASSOCIATION);
        coverageToPolicy.setMinCardinality(1);
        coverageToPolicy.setMinCardinality(1);
        coverageToPolicy.setDerivedUnion(true);
        coverageToPolicy.setTarget(policyType.getQualifiedName());
        coverageToPolicy.setTargetRoleSingular("Policy");
        coverageToPolicy.setTargetRolePlural("Policies");
        coverageToPolicy.setInverseAssociation("Coverage");
        
        IPolicyCmptType homePolicyType = newPolicyCmptType(ipsProject, "HomePolicy");
        homePolicyType.setSupertype(policyType.getQualifiedName());
        IPolicyCmptType homeCoverageType = newPolicyCmptType(ipsProject, "HomeCoverage");
        homeCoverageType.setSupertype(coverageType.getQualifiedName());
        
        IPolicyCmptTypeAssociation homePolicyToCoverage = homePolicyType.newPolicyCmptTypeAssociation();
        homePolicyToCoverage.setAssociationType(AssociationType.ASSOCIATION);
        homePolicyToCoverage.setMinCardinality(1);
        homePolicyToCoverage.setMinCardinality(Integer.MAX_VALUE);
        homePolicyToCoverage.setTarget(homeCoverageType.getQualifiedName());
        homePolicyToCoverage.setTargetRoleSingular("HomeCoverage");
        homePolicyToCoverage.setTargetRolePlural("HomeCoverages");
        homePolicyToCoverage.setSubsettedDerivedUnion(policyToCoverage.getName());
        homePolicyToCoverage.setInverseAssociation("HomePolicy");
        
        IPolicyCmptTypeAssociation homeCoverageToPolicy = homeCoverageType.newPolicyCmptTypeAssociation();
        homeCoverageToPolicy.setAssociationType(AssociationType.ASSOCIATION);
        homeCoverageToPolicy.setMinCardinality(1);
        homeCoverageToPolicy.setMinCardinality(1);
        homeCoverageToPolicy.setSubsettedDerivedUnion(coverageToPolicy.getName());
        homeCoverageToPolicy.setTarget(homePolicyType.getQualifiedName());
        homeCoverageToPolicy.setTargetRoleSingular("HomePolicy");
        homeCoverageToPolicy.setTargetRolePlural("HomePolicies");
        homeCoverageToPolicy.setInverseAssociation("HomeCoverage");
        
        MessageList ml = homePolicyToCoverage.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION));
        
        // implementing revsere relation does not specify a container relation 
        homeCoverageToPolicy.setSubsettedDerivedUnion("");
        ml = homePolicyToCoverage.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION));
        homeCoverageToPolicy.setSubsettedDerivedUnion(coverageToPolicy.getName());
        ml = homePolicyToCoverage.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION));
                
        // implementing revsere relation does specify a different container reverse relation (but container does)
        homeCoverageToPolicy.setSubsettedDerivedUnion("someContainerRel");
        ml = homePolicyToCoverage.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION));
    }
    
    public void testValidateInverseRelationMismatch() throws Exception {
        IIpsProject ipsProject = newIpsProject("TestValidateInverseRelationMismatch");
        IPolicyCmptType typeA = newPolicyCmptType(ipsProject, "A");
        IPolicyCmptType typeB = newPolicyCmptType(ipsProject, "B");
        IPolicyCmptTypeAssociation relationAtoB = typeA.newPolicyCmptTypeAssociation();
        relationAtoB.setAssociationType(AssociationType.ASSOCIATION);
        relationAtoB.setTarget("B");
        relationAtoB.setTargetRoleSingular("roleB");
        relationAtoB.setTargetRolePlural("roleBs");
        
        IPolicyCmptTypeAssociation relationBtoA = typeB.newPolicyCmptTypeAssociation();
        relationBtoA.setAssociationType(AssociationType.ASSOCIATION);
        relationBtoA.setTarget("B");
        relationBtoA.setTargetRoleSingular("roleA");
        relationBtoA.setTargetRolePlural("roleAs");

        // mismatch: B does does refer to A
        relationAtoB.setInverseAssociation("roleA");
        relationBtoA.setInverseAssociation("somethingElse");
        MessageList ml = relationAtoB.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH)); 

        // now it fits
        relationBtoA.setInverseAssociation("roleB");
        ml = relationAtoB.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH)); 

        // rule applies only to assoziations.
        relationAtoB.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        relationBtoA.setInverseAssociation("somethingElse"); // 
        ml = relationAtoB.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));
        
        relationAtoB.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        ml = relationAtoB.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));
    }

    public void testValidateInverseRelationNotFoundInTarget() throws Exception {
        IIpsProject ipsProject = newIpsProject("testValidateInverseRelationNotFoundInTarget");
        IPolicyCmptType typeA = newPolicyCmptType(ipsProject, "A");
        IPolicyCmptType typeB = newPolicyCmptType(ipsProject, "B");
        IPolicyCmptTypeAssociation relationAtoB = typeA.newPolicyCmptTypeAssociation();
        relationAtoB.setAssociationType(AssociationType.ASSOCIATION);
        relationAtoB.setTarget("B");
        relationAtoB.setTargetRoleSingular("roleB");
        relationAtoB.setTargetRoleSingular("roleBs");
        
        MessageList ml = relationAtoB.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));
        
        relationAtoB.setInverseAssociation("roleB");
        ml = relationAtoB.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));

        IPolicyCmptTypeAssociation relationBtoA = typeB.newPolicyCmptTypeAssociation();
        relationBtoA.setAssociationType(AssociationType.ASSOCIATION);
        relationBtoA.setTarget("B");
        relationBtoA.setTargetRoleSingular("somethingThatIsNotA");
        relationBtoA.setTargetRoleSingular("somethingThatIsNotAs");
        ml = relationAtoB.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));
        
        relationBtoA.setTargetRoleSingular("roleB");
        ml = relationAtoB.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));
    }    

    public void testIsInverseAssociationApplicable() throws CoreException {
        association = pcType.newPolicyCmptTypeAssociation();

        association.setAssociationType(AssociationType.ASSOCIATION);
        assertTrue(association.isInverseAssociationApplicable());
        
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertFalse(association.isInverseAssociationApplicable());

        TestIpsArtefactBuilderSet builderset = new TestIpsArtefactBuilderSet();
        builderset.setIpsProject(pcType.getIpsProject());
        setArtefactBuildset(pcType.getIpsProject(), builderset);
        
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        builderset.setInverseRelationLinkRequiredFor2WayCompositions(false);
        assertFalse(association.isInverseAssociationApplicable()); 

        builderset.setInverseRelationLinkRequiredFor2WayCompositions(true);
        assertTrue(association.isInverseAssociationApplicable()); 
        
        testPropertyAccessReadOnly(PolicyCmptTypeAssociation.class, IPolicyCmptTypeAssociation.PROPERTY_INVERSE_ASSOCIATION_APPLICABLE);
    }
    
    public void testValidateInverseRelationNotNeeded() throws Exception {
        association.setAssociationType(AssociationType.ASSOCIATION);
        association.setInverseAssociation("something");
        
        MessageList ml = association.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INFO_NOT_NEEDED));
        
        association.setInverseAssociation("");
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INFO_NOT_NEEDED));

        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        association.setInverseAssociation("something");
        ml = association.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INFO_NOT_NEEDED));
        
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        ml = association.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INFO_NOT_NEEDED));
    }
    
    public void testRemove() {
        assertEquals(1, pcType.getPolicyCmptTypeAssociations().length);
        association.delete();
        assertEquals(0, pcType.getPolicyCmptTypeAssociations().length);
        assertTrue(association.getIpsObject().getIpsSrcFile().isDirty());
    }
    
    public void testSetType() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setAssociationType(AssociationType.ASSOCIATION);
        assertEquals(AssociationType.ASSOCIATION, association.getAssociationType());
        assertTrue(association.getIpsObject().getIpsSrcFile().isDirty());
    }
    
    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        association.initFromXml((Element)doc.getDocumentElement());
        assertEquals(42, association.getId());
        assertEquals(AssociationType.ASSOCIATION, association.getAssociationType());
        assertTrue(association.isDerivedUnion());
        assertTrue(association.isQualified());
        assertEquals("MotorPart", association.getTarget());
        assertEquals("blabla", association.getDescription());
        assertEquals("PolicyPart", association.getTargetRoleSingular());
        assertEquals("PolicyParts", association.getTargetRolePlural());
        assertEquals(1, association.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, association.getMaxCardinality());
        assertEquals("Parts", association.getSubsettedDerivedUnion());
        assertEquals("Policy", association.getInverseAssociation());
    }

    public void testToXml() {
        association = pcType.newPolicyCmptTypeAssociation(); 
        association.setAssociationType(AssociationType.ASSOCIATION);
        association.setDerivedUnion(true);
        association.setQualified(true);
        association.setTarget("target");
        association.setTargetRoleSingular("targetRoleSingular");
        association.setTargetRolePlural("targetRolePlural");
        association.setSubsettedDerivedUnion("super");
        association.setInverseAssociation("reverse");
        association.setMinCardinality(2);
        association.setMaxCardinality(3);
        association.setDescription("blabla");
        
        Element element = association.toXml(this.newDocument());
        
        IPolicyCmptTypeAssociation copy = pcType.newPolicyCmptTypeAssociation();
        copy.initFromXml(element);
        assertEquals(AssociationType.ASSOCIATION, copy.getAssociationType());
        assertTrue(copy.isDerivedUnion());
        assertTrue(copy.isQualified());
        assertEquals("target", copy.getTarget());
        assertEquals("targetRoleSingular", copy.getTargetRoleSingular());
        assertEquals("targetRolePlural", copy.getTargetRolePlural());
        assertEquals("super", copy.getSubsettedDerivedUnion());
        assertEquals("reverse", copy.getInverseAssociation());
        assertEquals(2, copy.getMinCardinality());
        assertEquals(3, copy.getMaxCardinality());
        assertEquals("blabla", copy.getDescription());
            }
    
    /**
     * Tests for the correct type of excetion to be thrwon - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			association.newPart(PolicyCmptTypeAttribute.class);
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
    	
    	association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        association.setMaxCardinality(2);
    	
    	ml = association.validate(ipsProject);
    	assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION));
    	
    	association.setMaxCardinality(1);
    	ml = association.validate(ipsProject);
    	assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION));
    }
    
	public void testValidateReverseRelationOfContainerRelationHasToBeContainerRelationToo() throws Exception {
		association.setAssociationType(AssociationType.ASSOCIATION);
        IPolicyCmptTypeAssociation rel2 = targetType.newPolicyCmptTypeAssociation();
        rel2.setAssociationType(AssociationType.ASSOCIATION);
		rel2.setTargetRoleSingular("test");
		rel2.setInverseAssociation(association.getTargetRoleSingular());
		association.setInverseAssociation("test");
		association.setDerivedUnion(true);
		MessageList ml = association.validate(ipsProject);
		assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER));
		rel2.setDerivedUnion(true);
		ml = association.validate(ipsProject);
		assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER));
	}

	public void testValidateInvesreAssociationTypeMissmatch() throws Exception {
		IPolicyCmptTypeAssociation rel2 = targetType.newPolicyCmptTypeAssociation();
		rel2.setTargetRoleSingular("test");
		rel2.setInverseAssociation(association.getTargetRoleSingular());
		rel2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
		association.setInverseAssociation("test");
		association.setAssociationType(AssociationType.ASSOCIATION);
		MessageList ml = association.validate(ipsProject);
		assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH));
		rel2.setAssociationType(AssociationType.ASSOCIATION);
		ml = association.validate(ipsProject);
		assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH));
    }
	
    public void testFindInverseAssociation() throws CoreException {
        association.setInverseAssociation("");
        assertNull(association.findInverseAssociation(ipsProject));

        association.setInverseAssociation("reverseRelation");
        assertNull(association.findInverseAssociation(ipsProject));
        
        IPolicyCmptType targetType = (IPolicyCmptType)newIpsObject(this.pcType.getIpsProject(), IpsObjectType.POLICY_CMPT_TYPE, "pack2.MotorPolicy");
        association.setTarget(targetType.getQualifiedName());
        assertNull(association.findInverseAssociation(ipsProject));
        
        IPolicyCmptTypeAssociation relation2 = targetType.newPolicyCmptTypeAssociation();
        relation2.setTargetRoleSingular("reverseRelation");
        assertEquals(relation2, association.findInverseAssociation(ipsProject));
        
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertNull(association.findInverseAssociation(ipsProject));
    }
    
    public void testNewInverseAssociation() throws CoreException{
        IPolicyCmptType targetType = newPolicyCmptType(ipsProject, "TargetType");
        association.setTarget(targetType.getQualifiedName());
        
        checkNewInverseAssociation(AssociationType.ASSOCIATION);
        checkNewInverseAssociation(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        checkNewInverseAssociation(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        
        association.setTarget("NONE");
        boolean exceptionThrown = false;
        try {
            association.newInverseAssociation();
        } catch (CoreException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    private void checkNewInverseAssociation(AssociationType associationType) throws CoreException {
        IPolicyCmptTypeAssociation targetAssociation = association.newInverseAssociation();
        assertEquals(association.getAssociationType().getCorrespondingAssociationType(), targetAssociation.getAssociationType());
        assertEquals(association.getTarget(), targetAssociation.getPolicyCmptType().getQualifiedName());
    }
}
