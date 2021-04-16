/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PolicyCmptTypeAssociationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType pcType;
    private IPolicyCmptType targetType;
    private PolicyCmptTypeAssociation association;
    private IPolicyCmptTypeAssociation implementationAssociation;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        pcType = newPolicyCmptType(ipsProject, "PolicyType");
        association = (PolicyCmptTypeAssociation)pcType.newPolicyCmptTypeAssociation();
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
    public void testIsContrainTrueWhenInverseAssociationIsConstrained() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        IPolicyCmptTypeAssociation inverseAssociation = association.newInverseAssociation();
        inverseAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        inverseAssociation.setConstrain(true);

        assertTrue(inverseAssociation.isConstrain());
        assertTrue(association.isConstrain());
    }

    @Test
    public void testIsContrainFalseWhenInverseAssociationIsNotConstrained() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        IPolicyCmptTypeAssociation inverseAssociation = association.newInverseAssociation();
        inverseAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        inverseAssociation.setConstrain(false);
        association.setConfigurable(true);

        assertFalse(inverseAssociation.isConstrain());
        assertFalse(association.isConstrain());
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
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION));

        // implementing revsere relation does not specify a container relation
        homeCoverageToPolicy.setSubsettedDerivedUnion("");
        ml = homePolicyToCoverage.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION));
        homeCoverageToPolicy.setSubsettedDerivedUnion(coverageToPolicy.getName());
        ml = homePolicyToCoverage.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION));

        // implementing revsere relation does specify a different container reverse relation (but
        // container does)
        homeCoverageToPolicy.setSubsettedDerivedUnion("someContainerRel");
        ml = homePolicyToCoverage.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION));
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
        assertNotNull(
                ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));

        IPolicyCmptTypeAssociation relationBtoA = typeB.newPolicyCmptTypeAssociation();
        relationBtoA.setAssociationType(AssociationType.ASSOCIATION);
        relationBtoA.setTarget("B");
        relationBtoA.setTargetRoleSingular("somethingThatIsNotA");
        relationBtoA.setTargetRoleSingular("somethingThatIsNotAs");
        ml = relationAtoB.validate(ipsProject);
        assertNotNull(
                ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));

        relationBtoA.setTargetRoleSingular("roleB");
        ml = relationAtoB.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET));
    }

    /**
     * FIPS-85 Checking MSGCODE_INVERSE_RELATION_MISMATCH with shared associations
     */
    @Test
    public void testValidateSharedAssociationCheckInverse() throws Exception {
        setOptionalConstraintSharedAssociation(true);
        PolicyCmptType basePolicy = newPolicyCmptType(ipsProject, "basePolicy");
        PolicyCmptType basePart = newPolicyCmptType(ipsProject, "basePart");
        PolicyCmptType policy = newPolicyCmptType(ipsProject, "policy");
        PolicyCmptType part = newPolicyCmptType(ipsProject, "part");

        policy.setSupertype(basePolicy.getQualifiedName());
        part.setSupertype(basePart.getQualifiedName());

        IPolicyCmptTypeAssociation compositBasePart = (IPolicyCmptTypeAssociation)basePolicy.newAssociation();
        compositBasePart.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        compositBasePart.setTargetRoleSingular("basePart");
        compositBasePart.setTargetRolePlural("baseParts");
        compositBasePart.setTarget(basePart.getQualifiedName());
        compositBasePart.setInverseAssociation("policy");
        compositBasePart.setDerivedUnion(true);

        IPolicyCmptTypeAssociation inverseBasePart = (IPolicyCmptTypeAssociation)basePart.newAssociation();
        inverseBasePart.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        inverseBasePart.setTargetRoleSingular("policy");
        inverseBasePart.setTarget(basePolicy.getQualifiedName());
        inverseBasePart.setInverseAssociation("basePart");

        IPolicyCmptTypeAssociation compositPart = (IPolicyCmptTypeAssociation)policy.newAssociation();
        compositPart.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        compositPart.setTargetRoleSingular("part");
        compositPart.setTargetRolePlural("parts");
        compositPart.setTarget(part.getQualifiedName());
        compositPart.setInverseAssociation("policy");
        compositPart.setSubsettedDerivedUnion(compositBasePart.getName());

        IPolicyCmptTypeAssociation inversePart = (IPolicyCmptTypeAssociation)part.newAssociation();
        inversePart.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        inversePart.setTargetRoleSingular("policy");
        inversePart.setTarget(basePolicy.getQualifiedName());
        inversePart.setInverseAssociation("basePart");

        MessageList messageList = compositPart.validate(ipsProject);
        assertNotNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));

        messageList = inversePart.validate(ipsProject);
        assertNotNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));

        inversePart.setSharedAssociation(true);

        messageList = compositPart.validate(ipsProject);
        assertNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));

        messageList = inversePart.validate(ipsProject);
        assertNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));

        inverseBasePart.setTargetRoleSingular("123");
        messageList = compositPart.validate(ipsProject);
        assertNotNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));

        inverseBasePart.setTargetRoleSingular("policy");
        compositPart.setSubsettedDerivedUnion("123");
        messageList = compositPart.validate(ipsProject);
        assertNotNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH));
    }

    /**
     * FIPS-85: check for MSGCODE_SHARED_ASSOCIATION_INVALID
     */
    @Test
    public void testValidateSharedAssociationInvalid() throws Exception {
        setOptionalConstraintSharedAssociation(true);
        PolicyCmptType basePolicy = newPolicyCmptType(ipsProject, "basePolicy");
        PolicyCmptType basePart = newPolicyCmptType(ipsProject, "basePart");
        PolicyCmptType policy = newPolicyCmptType(ipsProject, "policy");
        PolicyCmptType part = newPolicyCmptType(ipsProject, "part");

        policy.setSupertype(basePolicy.getQualifiedName());
        part.setSupertype(basePart.getQualifiedName());

        IPolicyCmptTypeAssociation compositBasePart = (IPolicyCmptTypeAssociation)basePolicy.newAssociation();
        compositBasePart.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        compositBasePart.setTargetRoleSingular("basePart");
        compositBasePart.setTargetRolePlural("baseParts");
        compositBasePart.setTarget(basePart.getQualifiedName());
        compositBasePart.setInverseAssociation("policy");
        compositBasePart.setDerivedUnion(true);

        IPolicyCmptTypeAssociation inverseBasePart = (IPolicyCmptTypeAssociation)basePart.newAssociation();
        inverseBasePart.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        inverseBasePart.setTargetRoleSingular("policy");
        inverseBasePart.setTarget(basePolicy.getQualifiedName());
        inverseBasePart.setInverseAssociation("basePart");

        IPolicyCmptTypeAssociation compositPart = (IPolicyCmptTypeAssociation)policy.newAssociation();
        compositPart.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        compositPart.setTargetRoleSingular("part");
        compositPart.setTargetRolePlural("parts");
        compositPart.setTarget(part.getQualifiedName());
        compositPart.setInverseAssociation("policy");
        compositPart.setSubsettedDerivedUnion(compositBasePart.getName());

        IPolicyCmptTypeAssociation inversePart = (IPolicyCmptTypeAssociation)part.newAssociation();
        inversePart.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        inversePart.setTargetRoleSingular("policyXXXXX");
        inversePart.setTarget(basePolicy.getQualifiedName());
        inversePart.setInverseAssociation("basePart");
        inversePart.setSharedAssociation(true);

        MessageList messageList = inversePart.validate(ipsProject);
        assertNotNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_SHARED_ASSOCIATION_INVALID));

        inversePart.setTargetRoleSingular("policy");
        messageList = inversePart.validate(ipsProject);
        assertNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_SHARED_ASSOCIATION_INVALID));

        compositBasePart.setDerivedUnion(false);
        messageList = inversePart.validate(ipsProject);
        assertNotNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_SHARED_ASSOCIATION_INVALID));

        inverseBasePart.setSharedAssociation(true);
        messageList = inversePart.validate(ipsProject);
        assertNotNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_SHARED_ASSOCIATION_INVALID));

        PolicyCmptType baseSuperPart = newPolicyCmptType(ipsProject, "baseSuperPart");
        basePart.setSupertype(baseSuperPart.getQualifiedName());
        IPolicyCmptTypeAssociation inverseBaseSuperPart = (IPolicyCmptTypeAssociation)baseSuperPart.newAssociation();
        inverseBaseSuperPart.setTargetRoleSingular("policy");
        inverseBaseSuperPart.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        inverseBaseSuperPart.setTarget(basePolicy.getQualifiedName());
        inverseBaseSuperPart.setInverseAssociation("basePart");
        compositBasePart.setDerivedUnion(true);

        messageList = inversePart.validate(ipsProject);
        assertNull(messageList.toString(),
                messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_SHARED_ASSOCIATION_INVALID));

    }

    @Test
    public void testRemove() {
        assertEquals(1, pcType.getPolicyCmptTypeAssociations().size());
        association.delete();
        assertEquals(0, pcType.getPolicyCmptTypeAssociations().size());
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
    public void testInitFromXml() throws CoreException {
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

        // FIPS-85 shared association is only true when configured in ips project and association is
        // a
        // detail-to-master composition
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        setOptionalConstraintSharedAssociation(true);
        assertEquals(true, association.isSharedAssociation());
    }

    @Test
    public void testToXml() {
        association = (PolicyCmptTypeAssociation)pcType.newPolicyCmptTypeAssociation();
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
        association.setSharedAssociation(true);
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
        // FIPS-85 getting directly from xml element because the getter also check other conditions
        assertEquals("true", element.getAttribute(IPolicyCmptTypeAssociation.PROPERTY_SHARED_ASSOCIATION));
    }

    @Test
    public void testValidateMaxCardinalityForReverseComposition() throws Exception {
        MessageList ml = new MessageList();

        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        association.setMaxCardinality(2);

        ml = association.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION));

        association.setMaxCardinality(1);
        ml = association.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION));
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
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER));
        rel2.setDerivedUnion(true);
        ml = association.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER));

        // in case of a derived union master to detail composition
        // the inverse must not be set as derived union
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setInverseAssociation("test");
        rel2.setDerivedUnion(false);
        ml = association.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER));
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
    public void testHasInverseAssociation() throws Exception {
        assertFalse(association.hasInverseAssociation());

        association.setInverseAssociation("abc");
        assertTrue(association.hasInverseAssociation());
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

    private void setOptionalConstraintSharedAssociation(boolean enabled) throws CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setSharedDetailToMasterAssociations(enabled);
        ipsProject.setProperties(properties);
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
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_MUST_BE_SET_IF_TYPE_IS_DETAIL_TO_MASTER));

        association.setInverseAssociation("association2");
        ml = association.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_INVERSE_ASSOCIATION_MUST_BE_SET_IF_TYPE_IS_DETAIL_TO_MASTER));

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
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_SUBSETTED_DERIVED_UNION_INVERSE_MUST_BE_EXISTS_IF_INVERSE_DERIVED_UNION_EXISTS));

        // b) derived union with inverse
        policyToCoverage.setInverseAssociation("Policy");
        messageList = homePolicyToHomeCoverage.validate(ipsProject);
        assertNotNull(messageList
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_SUBSETTED_DERIVED_UNION_INVERSE_MUST_BE_EXISTS_IF_INVERSE_DERIVED_UNION_EXISTS));

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
                .getMessageByCode(
                        IPolicyCmptTypeAssociation.MSGCODE_SUBSETTED_DERIVED_UNION_INVERSE_MUST_BE_EXISTS_IF_INVERSE_DERIVED_UNION_EXISTS));
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
    public void testIsSharedAssociatiopn() throws Exception {
        // FIPS-85
        association.setSharedAssociation(false);
        assertFalse(association.isSharedAssociation());
        association.setSharedAssociation(true);
        assertFalse(association.isSharedAssociation());

        setOptionalConstraintSharedAssociation(true);
        assertFalse(association.isSharedAssociation());

        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertTrue(association.isSharedAssociation());

        setOptionalConstraintSharedAssociation(false);
        assertFalse(association.isSharedAssociation());

        setOptionalConstraintSharedAssociation(true);
        association.setSharedAssociation(false);
        assertFalse(association.isSharedAssociation());
    }

    @Test
    public void testFindSharedAssociationHost() throws Exception {
        // FIPS-85
        association.setSharedAssociation(true);
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        setOptionalConstraintSharedAssociation(true);

        IPolicyCmptTypeAssociation host = association.findSharedAssociationHost(ipsProject);
        assertNull("should find no shared association host", host);

        PolicyCmptType superType = newPolicyCmptType(ipsProject, "superType");
        pcType.setSupertype(superType.getQualifiedName());
        IPolicyCmptTypeAssociation associationHost = (IPolicyCmptTypeAssociation)superType.newAssociation();
        associationHost.setTargetRoleSingular(association.getTargetRoleSingular());

        host = association.findSharedAssociationHost(ipsProject);
        assertNull("should find no shared association host", host);

        associationHost.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        host = association.findSharedAssociationHost(ipsProject);
        assertNull("should find no shared association host", host);

        associationHost.setTarget(association.getTarget());
        host = association.findSharedAssociationHost(ipsProject);
        assertEquals(associationHost, host);

    }

    @Test
    public void testFindMatchingProductCmptTypeAssociation() throws Exception {
        PolicyCmptType police = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = police.findProductCmptType(ipsProject);

        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IPolicyCmptTypeAssociation policeToTarifvereinbarung = police.newPolicyCmptTypeAssociation();
        policeToTarifvereinbarung.setTargetRoleSingular("VersPers");
        policeToTarifvereinbarung.setTarget(tarifvereinbarung.getQualifiedName());

        IProductCmptTypeAssociation produktToTarif = produkt.newProductCmptTypeAssociation();
        produktToTarif.setTargetRoleSingular("produktToTarif");
        produktToTarif.setTarget(tarif.getQualifiedName());

        IProductCmptTypeAssociation produktToTarif2 = produkt.newProductCmptTypeAssociation();
        produktToTarif2.setTargetRoleSingular("produktToTarif2");
        produktToTarif2.setTarget(tarif.getQualifiedName());

        assertEquals(produktToTarif, policeToTarifvereinbarung.findMatchingProductCmptTypeAssociation(ipsProject));

        policeToTarifvereinbarung.setMatchingAssociationSource(produkt.getQualifiedName());
        policeToTarifvereinbarung.setMatchingAssociationName(produktToTarif2.getName());

        assertEquals(produktToTarif2, policeToTarifvereinbarung.findMatchingProductCmptTypeAssociation(ipsProject));
    }

    /**
     * This is testing the special combination of product and policy type associations discussed in
     * FIPS-563
     * 
     */
    @Test
    public void testFindMatchingProductCmptTypeAssociation2() throws Exception {
        PolicyCmptType police = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = police.findProductCmptType(ipsProject);

        PolicyCmptType versPerson = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "VersPerson");

        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IPolicyCmptTypeAssociation policeToVersPerson = police.newPolicyCmptTypeAssociation();
        policeToVersPerson.setTargetRoleSingular("VersPers");
        policeToVersPerson.setTarget(versPerson.getQualifiedName());

        IPolicyCmptTypeAssociation versPersonToTarifvereinbarung = versPerson.newPolicyCmptTypeAssociation();
        versPersonToTarifvereinbarung.setTargetRoleSingular("versPersonToTarifvereinbarung");
        versPersonToTarifvereinbarung.setTarget(tarifvereinbarung.getQualifiedName());

        IProductCmptTypeAssociation produktToTarif = produkt.newProductCmptTypeAssociation();
        produktToTarif.setTargetRoleSingular("produktToTarif");
        produktToTarif.setTarget(tarif.getQualifiedName());

        assertNull(produktToTarif.findMatchingPolicyCmptTypeAssociation(ipsProject));

        versPersonToTarifvereinbarung.setMatchingAssociationSource(produkt.getQualifiedName());
        versPersonToTarifvereinbarung.setMatchingAssociationName(produktToTarif.getName());

        assertNull(produktToTarif.findMatchingPolicyCmptTypeAssociation(ipsProject));

        policeToVersPerson.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        assertEquals(produktToTarif, versPersonToTarifvereinbarung.findMatchingProductCmptTypeAssociation(ipsProject));
    }

    /**
     * FIPS-710
     * 
     */
    @Test
    public void shouldFindNoMatchingProductCmptTypeAssociationForDetailToMaster() throws Exception {
        PolicyCmptType police = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = police.findProductCmptType(ipsProject);

        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IPolicyCmptTypeAssociation policeToTarifver = police.newPolicyCmptTypeAssociation();
        policeToTarifver.setTarget(tarifvereinbarung.getQualifiedName());
        policeToTarifver.setTargetRolePlural("tarifVer");
        policeToTarifver.setTargetRolePlural("tarifVers");
        policeToTarifver.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IPolicyCmptTypeAssociation tarifverToPolice = policeToTarifver.newInverseAssociation();
        tarifverToPolice.setTargetRoleSingular("police");
        tarifverToPolice.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        IProductCmptTypeAssociation produktToTarif = produkt.newProductCmptTypeAssociation();
        produktToTarif.setTarget(tarif.getQualifiedName());
        produktToTarif.setTargetRoleSingular("tarif");
        produktToTarif.setTargetRolePlural("tarifs");

        IProductCmptTypeAssociation tarifToProdukt = tarif.newProductCmptTypeAssociation();
        tarifToProdukt.setTarget(produkt.getQualifiedName());
        tarifToProdukt.setTargetRoleSingular("produkt");
        tarifToProdukt.setTargetRolePlural("produkts");

        assertNull(tarifverToPolice.findMatchingProductCmptTypeAssociation(ipsProject));
    }

    @Test
    public void testFindCorrectMatchingPolicyCmptTypeRecoursive_null() throws Exception {
        boolean result = association.findCorrectMatchingPolicyCmptTypeRecoursive(null, ipsProject,
                new HashSet<IPolicyCmptType>());

        assertFalse(result);
    }

    @Test
    public void testValidate_constrainedNotQualified() throws Exception {
        PolicyCmptType subType = newPolicyCmptType(ipsProject, "SubType");
        subType.setSupertype(pcType.getQualifiedName());
        IPolicyCmptTypeAssociation subAssociation = subType.newPolicyCmptTypeAssociation();
        subAssociation.setTargetRoleSingular(association.getTargetRoleSingular());
        subAssociation.setTargetRolePlural(association.getTargetRolePlural());
        subAssociation.setConstrain(true);
        subAssociation.setQualified(true);
        association.setQualified(false);

        MessageList messageList = subAssociation.validate(ipsProject);

        assertNotNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_CONSTRAINED_QUALIFIER_MISMATCH));
    }

    @Test
    public void testValidate_constrainedQualified() throws Exception {
        PolicyCmptType subType = newPolicyCmptType(ipsProject, "SubType");
        subType.setSupertype(pcType.getQualifiedName());
        IPolicyCmptTypeAssociation subAssociation = subType.newPolicyCmptTypeAssociation();
        subAssociation.setTargetRoleSingular(association.getTargetRoleSingular());
        subAssociation.setTargetRolePlural(association.getTargetRolePlural());
        subAssociation.setConstrain(true);
        subAssociation.setQualified(false);
        association.setQualified(true);

        MessageList messageList = subAssociation.validate(ipsProject);

        assertNotNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_CONSTRAINED_QUALIFIER_MISMATCH));
    }

    @Test
    public void testValidate_constrainedMatchQualified() throws Exception {
        PolicyCmptType subType = newPolicyCmptType(ipsProject, "SubType");
        subType.setSupertype(pcType.getQualifiedName());
        IPolicyCmptTypeAssociation subAssociation = subType.newPolicyCmptTypeAssociation();
        subAssociation.setTargetRoleSingular(association.getTargetRoleSingular());
        subAssociation.setTargetRolePlural(association.getTargetRolePlural());
        subAssociation.setConstrain(true);
        subAssociation.setQualified(true);
        association.setQualified(true);

        MessageList messageList = subAssociation.validate(ipsProject);

        assertNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_CONSTRAINED_QUALIFIER_MISMATCH));
    }

    @Test
    public void testValidate_constrainedMatchNotQualified() throws Exception {
        PolicyCmptType subType = newPolicyCmptType(ipsProject, "SubType");
        subType.setSupertype(pcType.getQualifiedName());
        IPolicyCmptTypeAssociation subAssociation = subType.newPolicyCmptTypeAssociation();
        subAssociation.setTargetRoleSingular(association.getTargetRoleSingular());
        subAssociation.setTargetRolePlural(association.getTargetRolePlural());
        subAssociation.setConstrain(true);
        subAssociation.setQualified(false);
        association.setQualified(false);

        MessageList messageList = subAssociation.validate(ipsProject);

        assertNull(messageList.getMessageByCode(IPolicyCmptTypeAssociation.MSGCODE_CONSTRAINED_QUALIFIER_MISMATCH));
    }

    @Test
    public void test_propertiesToXMLConfigurable() {
        Element element = mock(Element.class);
        association.setConfigurable(true);

        association.propertiesToXml(element);

        verify(element).setAttribute(IPolicyCmptTypeAssociation.PROPERTY_CONFIGURABLE, Boolean.toString(true));
    }

    @Test
    public void test_initFromXMLConfigurable_true() {
        Element element = mock(Element.class);
        when(element.getAttribute(IPolicyCmptTypeAssociation.PROPERTY_ASSOCIATION_TYPE)).thenReturn("aggr");
        when(element.hasAttribute(IPolicyCmptTypeAssociation.PROPERTY_CONFIGURABLE)).thenReturn(true);
        when(element.getAttribute(IPolicyCmptTypeAssociation.PROPERTY_CONFIGURABLE)).thenReturn("true");

        association.initPropertiesFromXml(element, "aggr");

        assertTrue(association.isConfigurable());
    }

    @Test
    public void test_initFromXMLConfigurable_false() {
        Element element = mock(Element.class);
        when(element.getAttribute(IPolicyCmptTypeAssociation.PROPERTY_ASSOCIATION_TYPE)).thenReturn("aggr");
        when(element.hasAttribute(IPolicyCmptTypeAssociation.PROPERTY_CONFIGURABLE)).thenReturn(true);
        when(element.getAttribute(IPolicyCmptTypeAssociation.PROPERTY_CONFIGURABLE)).thenReturn("false");

        association.initPropertiesFromXml(element, "aggr");

        assertFalse(association.isConfigurable());
    }

    @Test
    public void test_initFromXMLConfigurable_default() {
        Element element = mock(Element.class);
        when(element.getAttribute(IPolicyCmptTypeAssociation.PROPERTY_ASSOCIATION_TYPE)).thenReturn("aggr");

        association.initPropertiesFromXml(element, "aggr");

        assertTrue(association.isConfigurable());
    }

}
