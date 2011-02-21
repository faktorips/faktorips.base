/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PolicyCmptTypeAssociationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType pcType;
    private IPolicyCmptType targetType;
    private IPolicyCmptTypeAssociation association;
    private IPolicyCmptTypeAssociation implementationAssociation;

    @Override
    @Before
    public void setUp() throws Exception {
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testSetQualified() {
        testPropertyAccessReadWrite(PolicyCmptTypeAssociation.class, IAssociation.PROPERTY_QUALIFIED, association,
                Boolean.TRUE);
    }

    @Test
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
        assertNull(ml
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION));

        // implementing revsere relation does not specify a container relation
        homeCoverageToPolicy.setSubsettedDerivedUnion("");
        ml = homePolicyToCoverage.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION));
        homeCoverageToPolicy.setSubsettedDerivedUnion(coverageToPolicy.getName());
        ml = homePolicyToCoverage.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION));

        // implementing revsere relation does specify a different container reverse relation (but
        // container does)
        homeCoverageToPolicy.setSubsettedDerivedUnion("someContainerRel");
        ml = homePolicyToCoverage.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION));
    }

    @Test
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
        relationBtoA.setTarget("A");
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

        // mismatch: master to detail composition
        relationAtoB.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        relationAtoB.setInverseAssociation("roleA");
        relationBtoA.setInverseAssociation("somethingElse"); //
        ml = relationAtoB.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));

        // mismatch: detail to master composition
        relationAtoB.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        relationAtoB.setInverseAssociation("roleA");
        ml = relationAtoB.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));
    }

    @Test
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

    @Test
    public void testRemove() {
        assertEquals(1, pcType.getPolicyCmptTypeAssociations().length);
        association.delete();
        assertEquals(0, pcType.getPolicyCmptTypeAssociations().length);
        assertTrue(association.getIpsObject().getIpsSrcFile().isDirty());
    }

    @Test
    public void testSetType() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setAssociationType(AssociationType.ASSOCIATION);
        assertEquals(AssociationType.ASSOCIATION, association.getAssociationType());
        assertTrue(association.getIpsObject().getIpsSrcFile().isDirty());
    }

    @Test
    public void testInitFromXml() {
        Document doc = getTestDocument();
        association.initFromXml(doc.getDocumentElement());
        assertEquals("42", association.getId());
        assertEquals(AssociationType.ASSOCIATION, association.getAssociationType());
        assertTrue(association.isDerivedUnion());
        assertTrue(association.isQualified());
        assertEquals("MotorPart", association.getTarget());
        assertEquals("blabla", association.getDescriptionText(Locale.GERMAN));
        assertEquals("PolicyPart", association.getTargetRoleSingular());
        assertEquals("PolicyParts", association.getTargetRolePlural());
        assertEquals(1, association.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, association.getMaxCardinality());
        assertEquals("Parts", association.getSubsettedDerivedUnion());
        assertEquals("Policy", association.getInverseAssociation());
    }

    @Test
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
        IDescription description = association.getDescription(Locale.US);
        description.setText("blabla");

        Element element = association.toXml(newDocument());

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
        assertEquals("blabla", copy.getDescriptionText(Locale.US));
    }

    // TODO
    // /**
    // * Test of searching the correct container relation candidates.
    // */
    // public void testSearchContainerRelationCandidates() throws Exception {
    // IPolicyCmptType contract = content.getContract();
    // IPolicyCmptType coverage = content.getCoverage();
    // IPolicyCmptType motorContract = content.getMotorContract();
    // IPolicyCmptType collisionCoverage = content.getCollisionCoverage();
    // IPolicyCmptType vehicle = content.getVehicle();
    //
    // // Setup test objects (clear existing relation)
    // clearAllRelations(contract);
    // clearAllRelations(coverage);
    // clearAllRelations(motorContract);
    // clearAllRelations(collisionCoverage);
    // clearAllRelations(vehicle);
    //
    // // New relation which will be used to get the container relation candidates
    // IRelation motorContract2CollisionCoverage = motorContract.newRelation();
    // motorContract2CollisionCoverage.setTarget(collisionCoverage.getQualifiedName());
    // motorContract2CollisionCoverage.setTargetRoleSingular("CollisionCoverage");
    //
    //
    // // Non container relation on supertype
    // IRelation contract2Coverage = contract.newRelation();
    // contract2Coverage.setTarget(coverage.getQualifiedName());
    // contract2Coverage.setReadOnlyContainer(false);
    // contract2Coverage.setTargetRoleSingular("Coverage");
    //
    // // Container relation on supertype with target in supertype hierarchy of rel. target
    // IRelation contRelContract2Coverage = contract.newRelation();
    // contRelContract2Coverage.setTarget(coverage.getQualifiedName());
    // contRelContract2Coverage.setTargetRoleSingular("CoverageContainer");
    // contRelContract2Coverage.setReadOnlyContainer(true);
    //
    // // Container relation on supertype with other target as rel. target
    // IRelation contRelContract2vehicle = contract.newRelation();
    // contRelContract2vehicle.setTarget(vehicle.getQualifiedName());
    // contRelContract2vehicle.setTargetRoleSingular("VehicleContainer");
    // contRelContract2vehicle.setReadOnlyContainer(true);
    //
    // // ==> check if the container relation of the super type and the container rel to the target
    // // will be returned as container candidate for the new relation
    // IRelation[] containerRelationCandidates =
    // motorContract2CollisionCoverage.findContainerRelationCandidates();
    // assertEquals(1, containerRelationCandidates.length);
    // assertEquals(contRelContract2Coverage, containerRelationCandidates[0]);
    //
    // // Container relation on supertype with target equal rel. target
    // IRelation contRelContract2CollisionCoverage = contract.newRelation();
    // contRelContract2CollisionCoverage.setTarget(collisionCoverage.getQualifiedName());
    // contRelContract2CollisionCoverage.setTargetRoleSingular("CollisionCoverageContainer");
    // contRelContract2CollisionCoverage.setReadOnlyContainer(true);
    //
    // // Container relation to target on policy cmpt the new relation belongs to
    // IRelation contRelMotorContract2CollisionCoverage = motorContract.newRelation();
    // contRelMotorContract2CollisionCoverage.setTarget(collisionCoverage.getQualifiedName());
    // contRelMotorContract2CollisionCoverage.setTargetRoleSingular("CollisionCoverageContainer");
    // contRelMotorContract2CollisionCoverage.setReadOnlyContainer(true);
    //
    // // Container relation not to target on policy cmpt the new relation belongs to
    // IRelation contRelMotorContract2Vehicle = motorContract.newRelation();
    // contRelMotorContract2Vehicle.setTarget(vehicle.getQualifiedName());
    // contRelMotorContract2Vehicle.setTargetRoleSingular("VehicleContainer");
    // contRelMotorContract2Vehicle.setReadOnlyContainer(true);
    //
    // // ==> check if the container relation of the super type and the container rel to the target
    // // will be returned as container candidate for the new relation
    // containerRelationCandidates =
    // motorContract2CollisionCoverage.findContainerRelationCandidates();
    // assertEquals(3, containerRelationCandidates.length);
    // List result = Arrays.asList(containerRelationCandidates);
    // assertTrue(result.contains(contRelContract2CollisionCoverage));
    // assertTrue(result.contains(contRelMotorContract2CollisionCoverage));
    // assertTrue(result.contains(contRelContract2Coverage));
    // assertFalse(result.contains(contRelMotorContract2Vehicle));
    // }
    //
    // private void clearAllRelations(IPolicyCmptType pcType){
    // IPolicyCmptTypeAssociation[] relations = pcType.getPolicyCmptTypeAssociations();
    // for (int i = 0; i < relations.length; i++) {
    // relations[i].delete();
    // }
    // }

    @Test
    public void testValidateMaxCardinalityForReverseComposition() throws Exception {
        MessageList ml = new MessageList();

        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        association.setMaxCardinality(2);

        ml = association.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION));

        association.setMaxCardinality(1);
        ml = association.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION));
    }

    @Test
    public void testValidateReverseRelationOfContainerRelationHasToBeContainerRelationToo() throws Exception {
        association.setAssociationType(AssociationType.ASSOCIATION);
        IPolicyCmptTypeAssociation rel2 = targetType.newPolicyCmptTypeAssociation();
        rel2.setAssociationType(AssociationType.ASSOCIATION);
        rel2.setTargetRoleSingular("test");
        rel2.setInverseAssociation(association.getTargetRoleSingular());
        association.setInverseAssociation("test");
        association.setDerivedUnion(true);
        rel2.setDerivedUnion(false);
        MessageList ml = association.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER));
        rel2.setDerivedUnion(true);
        ml = association.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER));

        // in case of a derived union master to detail composition
        // the inverse must not be set as derived union
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setInverseAssociation("test");
        rel2.setDerivedUnion(false);
        ml = association.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER));
    }

    @Test
    public void testValidateInverseAssociationTypeMissmatch() throws Exception {
        IPolicyCmptTypeAssociation ass2 = targetType.newPolicyCmptTypeAssociation();
        ass2.setTargetRoleSingular("test");
        ass2.setInverseAssociation(association.getTargetRoleSingular());
        ass2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setInverseAssociation("test");
        association.setAssociationType(AssociationType.ASSOCIATION);
        MessageList ml = association.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH));
        ass2.setAssociationType(AssociationType.ASSOCIATION);
        ml = association.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH));

        ass2.setAssociationType(AssociationType.ASSOCIATION);
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        association.setInverseAssociation("test");
        ml = association.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH));
    }

    @Test
    public void testValidateMasterDetailCompositionTypeMissmatch() throws Exception {
        IPolicyCmptTypeAssociation ass2 = targetType.newPolicyCmptTypeAssociation();
        ass2.setTargetRoleSingular("test");
        ass2.setInverseAssociation(association.getTargetRoleSingular());
        ass2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        ass2.setTarget(association.getPolicyCmptType().getQualifiedName());
        association.setInverseAssociation("test");
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        MessageList ml = association.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_DETAIL_TO_MASTER_TYPE_MISSMATCH));
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_MASTER_TO_DETAIL_TYPE_MISSMATCH));
        ml = ass2.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_DETAIL_TO_MASTER_TYPE_MISSMATCH));
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_MASTER_TO_DETAIL_TYPE_MISSMATCH));

        // both detail to master
        ass2.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        ass2.setInverseAssociation(association.getTargetRoleSingular());
        association.setInverseAssociation("test");
        ml = association.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_DETAIL_TO_MASTER_TYPE_MISSMATCH));
        ml = ass2.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_DETAIL_TO_MASTER_TYPE_MISSMATCH));

        // both master to detail
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        ass2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        ass2.setInverseAssociation(association.getTargetRoleSingular());
        association.setInverseAssociation("test");
        ml = association.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_MASTER_TO_DETAIL_TYPE_MISSMATCH));
        ml = ass2.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_MASTER_TO_DETAIL_TYPE_MISSMATCH));

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        ass2.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        ass2.setInverseAssociation(association.getTargetRoleSingular());
        association.setInverseAssociation("test");
        ml = association.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_DETAIL_TO_MASTER_TYPE_MISSMATCH));
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_MASTER_TO_DETAIL_TYPE_MISSMATCH));
        ml = ass2.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_DETAIL_TO_MASTER_TYPE_MISSMATCH));
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_MASTER_TO_DETAIL_TYPE_MISSMATCH));

        association.setAssociationType(AssociationType.ASSOCIATION);
        ass2.setAssociationType(AssociationType.ASSOCIATION);
        ml = association.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_DETAIL_TO_MASTER_TYPE_MISSMATCH));
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_MASTER_TO_DETAIL_TYPE_MISSMATCH));
        ml = ass2.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_DETAIL_TO_MASTER_TYPE_MISSMATCH));
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_MASTER_TO_DETAIL_TYPE_MISSMATCH));
    }

    @Test
    public void testFindInverseAssociation() throws CoreException {
        association.setInverseAssociation("");
        assertNull(association.findInverseAssociation(ipsProject));

        association.setInverseAssociation("reverseRelation");
        assertNull(association.findInverseAssociation(ipsProject));

        IPolicyCmptType targetType = (IPolicyCmptType)newIpsObject(pcType.getIpsProject(),
                IpsObjectType.POLICY_CMPT_TYPE, "pack2.MotorPolicy");
        association.setTarget(targetType.getQualifiedName());
        assertNull(association.findInverseAssociation(ipsProject));

        IPolicyCmptTypeAssociation relation2 = targetType.newPolicyCmptTypeAssociation();
        relation2.setTargetRoleSingular("reverseRelation");
        assertEquals(relation2, association.findInverseAssociation(ipsProject));
    }

    @Test
    public void testNewInverseAssociation() throws CoreException {
        IPolicyCmptType targetType = newPolicyCmptType(ipsProject, "TargetType");
        association.setTarget(targetType.getQualifiedName());

        checkNewInverseAssociation();
        checkNewInverseAssociation();
        checkNewInverseAssociation();

        association.setTarget("NONE");
        boolean exceptionThrown = false;
        try {
            association.newInverseAssociation();
        } catch (CoreException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testDetailToMasterMustDefineInverseAssociation() throws CoreException {
        // in case of a detail to master association the inverse must be set
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        association.setInverseAssociation("");
        MessageList ml = association.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_MUST_BE_SET_IF_TYPE_IS_DETAIL_TO_MASTER));

        association.setInverseAssociation("association2");
        ml = association.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_MUST_BE_SET_IF_TYPE_IS_DETAIL_TO_MASTER));

        // OK, inverse is set, we don't care about not existing association, this is be done be a
        // different validation message
    }

    @Test
    public void testInverseAssociationMismatch() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        association.setTargetRoleSingular("associationDtoM");
        association.setInverseAssociation("associationMtoD");

        IAssociation association2 = association.getPolicyCmptType().newAssociation();
        association2.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        association2.setTargetRoleSingular("dummy");

        IPolicyCmptType targetType = (IPolicyCmptType)newIpsObject(pcType.getIpsProject(),
                IpsObjectType.POLICY_CMPT_TYPE, "pack2.MotorPolicy");
        association.setTarget(targetType.getQualifiedName());
        IPolicyCmptTypeAssociation masterDetailAssociation = targetType.newPolicyCmptTypeAssociation();
        masterDetailAssociation.setTarget(association.getPolicyCmptType().getQualifiedName());
        masterDetailAssociation.setTargetRoleSingular("associationMtoD");
        masterDetailAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        masterDetailAssociation.setInverseAssociation("associationDtoM");
        MessageList ml = masterDetailAssociation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));

        masterDetailAssociation.setInverseAssociation("dummy");
        ml = masterDetailAssociation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));

        masterDetailAssociation.setInverseAssociation("associationDtoM");
        association.setTargetRoleSingular("associationDtoM");
        association.setInverseAssociation("associationMtoD");
        ml = masterDetailAssociation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));

        IPolicyCmptType targetType2 = (IPolicyCmptType)newIpsObject(pcType.getIpsProject(),
                IpsObjectType.POLICY_CMPT_TYPE, "pack2.Dummy");
        association.setTarget(targetType2.getQualifiedName());

        // wrong target policy component type of inverse association
        ml = masterDetailAssociation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));
    }

    @Test
    public void testFindTargetAssociationWithInverseSetToThis() throws CoreException {
        // test if using the master to detail compositions find method returns the correct detail to
        // master composition, target has two detail to master with same inverse name but different
        // target
        IIpsProject ipsProject = newIpsProject("TestFindCorrectInverse");
        IPolicyCmptType typeA = newPolicyCmptType(ipsProject, "A");
        IPolicyCmptType typeB = newPolicyCmptType(ipsProject, "B");
        IPolicyCmptType typeC = newPolicyCmptType(ipsProject, "C");

        // A - master to detail
        IPolicyCmptTypeAssociation relationAtoB = typeA.newPolicyCmptTypeAssociation();
        relationAtoB.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        relationAtoB.setTarget("B");
        relationAtoB.setTargetRoleSingular("roleB");
        relationAtoB.setTargetRolePlural("roleBs");
        relationAtoB.setInverseAssociation("roleA");

        // C - master to detail
        IPolicyCmptTypeAssociation relationCtoB = typeC.newPolicyCmptTypeAssociation();
        relationCtoB.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        relationCtoB.setTarget("B");
        relationCtoB.setTargetRoleSingular("roleB");
        relationCtoB.setTargetRolePlural("roleBs");
        relationCtoB.setInverseAssociation("roleC");

        // B- detail to master
        IPolicyCmptTypeAssociation relationBtoA = typeB.newPolicyCmptTypeAssociation();
        relationBtoA.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        relationBtoA.setTarget("A");
        relationBtoA.setTargetRoleSingular("roleA");
        relationBtoA.setTargetRolePlural("roleAs");
        relationBtoA.setInverseAssociation("roleB");

        // B- detail to master
        IPolicyCmptTypeAssociation relationBtoC = typeB.newPolicyCmptTypeAssociation();
        relationBtoC.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        relationBtoC.setTarget("C");
        relationBtoC.setTargetRoleSingular("roleC");
        relationBtoC.setTargetRolePlural("roleCs");
        relationBtoC.setInverseAssociation("roleB");

        // test internal find method
        assertEquals(relationBtoA,
                ((PolicyCmptTypeAssociation)relationAtoB).findTargetAssociationWithCorrespondingInverse(ipsProject));
        assertEquals(relationBtoC,
                ((PolicyCmptTypeAssociation)relationCtoB).findTargetAssociationWithCorrespondingInverse(ipsProject));
    }

    @Test
    public void testInverseOfSubsettedDerivedUnionMustExistsIfInverseOfDerivedUnionExists() throws CoreException {
        IPolicyCmptType policy = newPolicyCmptType(ipsProject, "my.Policy");
        IPolicyCmptType coverage = newPolicyCmptType(ipsProject, "my.Coverage");

        IPolicyCmptTypeAssociation policyToCoverage = policy.newPolicyCmptTypeAssociation();
        policyToCoverage.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        policyToCoverage.setMinCardinality(1);
        policyToCoverage.setMinCardinality(Integer.MAX_VALUE);
        policyToCoverage.setDerivedUnion(true);
        policyToCoverage.setTarget(coverage.getQualifiedName());
        policyToCoverage.setTargetRoleSingular("Coverage");
        policyToCoverage.setTargetRolePlural("Coverages");
        policyToCoverage.setInverseAssociation("Policy");

        IPolicyCmptTypeAssociation coverageToPolicy = coverage.newPolicyCmptTypeAssociation();
        coverageToPolicy.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        coverageToPolicy.setMinCardinality(1);
        coverageToPolicy.setMinCardinality(1);
        coverageToPolicy.setTarget(policy.getQualifiedName());
        coverageToPolicy.setTargetRoleSingular("Policy");
        coverageToPolicy.setInverseAssociation("Coverage");

        MessageList messageList = policyToCoverage.validate(ipsProject);
        assertTrue(messageList.isEmpty());

        IPolicyCmptType homePolicy = newPolicyCmptType(ipsProject, "HomePolicy");
        homePolicy.setSupertype(policy.getQualifiedName());
        IPolicyCmptType homeCoverage = newPolicyCmptType(ipsProject, "HomeCoverage");
        homeCoverage.setSupertype(coverage.getQualifiedName());

        IPolicyCmptTypeAssociation homePolicyToHomeCoverage = homePolicy.newPolicyCmptTypeAssociation();
        homePolicyToHomeCoverage.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        homePolicyToHomeCoverage.setMinCardinality(1);
        homePolicyToHomeCoverage.setMinCardinality(Integer.MAX_VALUE);
        homePolicyToHomeCoverage.setSubsettedDerivedUnion("Coverage");
        homePolicyToHomeCoverage.setTarget(homeCoverage.getQualifiedName());
        homePolicyToHomeCoverage.setTargetRoleSingular("HomeCoverage");
        homePolicyToHomeCoverage.setTargetRolePlural("HomeCoverages");

        // test that the inverse of a subsetted derived union is mandatory if the inverse of the
        // derived union exists
        // a) derived union without inverse
        policyToCoverage.setInverseAssociation("");
        messageList = homePolicyToHomeCoverage.validate(ipsProject);
        assertNull(messageList
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_SUBSETTED_DERIVED_UNION_INVERSE_MUST_BE_EXISTS_IF_INVERSE_DERIVED_UNION_EXISTS));

        // b) derived union with inverse
        policyToCoverage.setInverseAssociation("Policy");
        messageList = homePolicyToHomeCoverage.validate(ipsProject);
        assertNotNull(messageList
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_SUBSETTED_DERIVED_UNION_INVERSE_MUST_BE_EXISTS_IF_INVERSE_DERIVED_UNION_EXISTS));

        homePolicyToHomeCoverage.setInverseAssociation("HomePolicy");
        // the inverse must exists
        messageList = homePolicyToHomeCoverage.validate(ipsProject);
        assertNotNull(messageList
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));

        IPolicyCmptTypeAssociation homeCoverageToHomePolicy = homeCoverage.newPolicyCmptTypeAssociation();
        homeCoverageToHomePolicy.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        homeCoverageToHomePolicy.setMinCardinality(1);
        homeCoverageToHomePolicy.setMinCardinality(1);
        homeCoverageToHomePolicy.setTarget(homePolicy.getQualifiedName());
        homeCoverageToHomePolicy.setTargetRoleSingular("HomePolicy");
        homeCoverageToHomePolicy.setInverseAssociation("HomeCoverage");

        messageList = homePolicyToHomeCoverage.validate(ipsProject);
        assertNull(messageList
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));
        assertNull(messageList
                .getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_SUBSETTED_DERIVED_UNION_INVERSE_MUST_BE_EXISTS_IF_INVERSE_DERIVED_UNION_EXISTS));
    }

    private void checkNewInverseAssociation() throws CoreException {
        IPolicyCmptTypeAssociation targetAssociation = association.newInverseAssociation();
        assertEquals(association.getAssociationType().getCorrespondingAssociationType(),
                targetAssociation.getAssociationType());
        assertEquals(association.getTarget(), targetAssociation.getPolicyCmptType().getQualifiedName());
    }

    @Test
    public void testDuplicateAssociationNameDifferentCardinality() throws CoreException {
        IPolicyCmptTypeAssociation association2 = pcType.newPolicyCmptTypeAssociation();
        association2.setTarget(targetType.getQualifiedName());

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTargetRoleSingular("a");
        association.setTargetRolePlural("as");
        association.setMinCardinality(0);
        association.setMaxCardinality(2);

        association2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association2.setTargetRoleSingular("a");
        association2.setTargetRolePlural("");
        association2.setMinCardinality(0);
        association2.setMaxCardinality(1);

        MessageList ml = pcType.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));
        // assume that the duplicate property is the only error
        assertNotNull(IType.MSGCODE_DUPLICATE_PROPERTY_NAME,
                ml.getMessagesFor(association2).getFirstMessage(Message.ERROR));
        assertNotNull(IType.MSGCODE_DUPLICATE_PROPERTY_NAME,
                ml.getMessagesFor(association2).getFirstMessage(Message.ERROR));

        association2.setTargetRoleSingular("b");
        association2.setTargetRolePlural("");
        association2.setMinCardinality(0);
        association2.setMaxCardinality(1);

        ml = pcType.validate(ipsProject);
        assertNull(ml.getMessageByCode(IType.MSGCODE_DUPLICATE_PROPERTY_NAME));
    }

    @Test
    public void testIsInverseOfDerivedUnion() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setDerivedUnion(true);

        IPolicyCmptTypeAssociation inverse = targetType.newPolicyCmptTypeAssociation();
        inverse.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        inverse.setTarget(association.getPolicyCmptType().getQualifiedName());

        inverse.setInverseAssociation(association.getName());
        association.setInverseAssociation(inverse.getName());

        assertTrue(inverse.isInverseOfDerivedUnion());
        assertFalse(association.isInverseOfDerivedUnion());

        association.setDerivedUnion(false);

        assertFalse(inverse.isInverseOfDerivedUnion());
        assertFalse(association.isInverseOfDerivedUnion());
    }
}
