/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builderpattern.ModelBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class AssociationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IType productType;
    private IType targetType;
    private IAssociation association;

    private IAssociation implementationRelation;
    private IProductCmptTypeAssociation constrains_subProductAssociation;
    private IPolicyCmptTypeAssociation constrains_subAssociation;
    private IPolicyCmptType constrains_sourceClass;
    private IPolicyCmptType constrains_targetClass;
    private IProductCmptType constrains_sourceProductClass;
    private IProductCmptType constrains_targetProductClass;
    private IPolicyCmptType constrains_subSourceClass;
    private IPolicyCmptType constrains_subTargetClass;
    private IProductCmptType constrains_subSourceProductClass;
    private IProductCmptType constrains_subTargetProductClass;

    private ModelBuilder builder;
    private IPolicyCmptType A;
    private IPolicyCmptType SUB_A;
    private IPolicyCmptType SUB_SUB_A;
    private IPolicyCmptType B;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productType = newProductCmptType(ipsProject, "Product");
        targetType = newProductCmptType(ipsProject, "CoverageType");
        association = productType.newAssociation();
        association.setTarget(targetType.getQualifiedName());
        association.setTargetRoleSingular("CoverageType");

        builder = new ModelBuilder(ipsProject);
        A = builder.createPolicyCmptType("A").build();
        SUB_A = builder.createPolicyCmptType("SubA").withSupertype(A).build();
        SUB_SUB_A = builder.createPolicyCmptType("SubSubA").withSupertype(SUB_A).build();
        B = builder.createPolicyCmptType("B").build();

        IType motorProductType = newProductCmptType(ipsProject, "MotorProduct");
        IType motorCoverageType = newProductCmptType(ipsProject, "MotorCoverageType");
        motorProductType.setSupertype(productType.getQualifiedName());
        motorCoverageType.setSupertype(targetType.getQualifiedName());
        implementationRelation = motorProductType.newAssociation();
        implementationRelation.setTargetRoleSingular("MotorCoverage");
        implementationRelation.setTarget(motorCoverageType.getQualifiedName());
        implementationRelation.setSubsettedDerivedUnion(association.getName());
    }

    @Test
    public void testIs1To1_And_Is1ToMany() {
        IPolicyCmptType type = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Policy");
        IPolicyCmptTypeAssociation ass = type.newPolicyCmptTypeAssociation();

        ass.setQualified(false);
        ass.setMaxCardinality(1);
        assertTrue(ass.is1To1());
        assertFalse(ass.is1ToMany());
        assertFalse(ass.is1ToManyIgnoringQualifier());

        ass.setQualified(true);
        assertFalse(ass.is1To1());
        assertTrue(ass.is1ToMany());
        assertFalse(ass.is1ToManyIgnoringQualifier());

        ass.setMaxCardinality(10);
        assertFalse(ass.is1To1());
        assertTrue(ass.is1ToMany());
        assertTrue(ass.is1ToManyIgnoringQualifier());

        ass.setQualified(false);
        ass.setMaxCardinality(10);
        assertFalse(ass.is1To1());
        assertTrue(ass.is1ToMany());
        assertTrue(ass.is1ToManyIgnoringQualifier());
    }

    @Test
    public void testValidateTargetTypeNotASubtype() throws Exception {
        MessageList ml = new MessageList();
        association.setDerivedUnion(true);

        ml = implementationRelation.validate(association.getIpsProject());
        assertThat(ml, lacksMessageCode(IAssociation.MSGCODE_TARGET_TYPE_NOT_A_SUBTYPE));

        IType otherType = newProductCmptType(ipsProject, "SomeType");
        association.setTarget(otherType.getQualifiedName());
        ml = implementationRelation.validate(association.getIpsProject());
        assertThat(ml, hasMessageCode(IAssociation.MSGCODE_TARGET_TYPE_NOT_A_SUBTYPE));
    }

    @Test
    public void testValidateTargetOfDerivedUnionNotFound() throws Exception {
        MessageList ml = new MessageList();
        association.setDerivedUnion(true);

        ml = implementationRelation.validate(association.getIpsProject());
        assertThat(ml, lacksMessageCode(IAssociation.MSGCODE_TARGET_OF_DERIVED_UNION_DOES_NOT_EXIST));

        association.setTarget("xxx");
        ml = implementationRelation.validate(association.getIpsProject());
        assertThat(ml, hasMessageCode(IAssociation.MSGCODE_TARGET_OF_DERIVED_UNION_DOES_NOT_EXIST));
    }

    @Test
    public void testValidateDerivedUnionNotFound() throws Exception {
        MessageList ml = new MessageList();

        association.setDerivedUnion(true);
        ml = implementationRelation.validate(association.getIpsProject());
        assertThat(ml, lacksMessageCode(IAssociation.MSGCODE_DERIVED_UNION_NOT_FOUND));

        implementationRelation.setSubsettedDerivedUnion("xxx");
        ml = implementationRelation.validate(association.getIpsProject());
        assertThat(ml, hasMessageCode(IAssociation.MSGCODE_DERIVED_UNION_NOT_FOUND));
    }

    @Test
    public void testValidateNotMarkedAsContainerRelation() throws Exception {
        MessageList ml = new MessageList();

        association.setDerivedUnion(false);
        ml = implementationRelation.validate(association.getIpsProject());
        assertThat(ml, hasMessageCode(IAssociation.MSGCODE_NOT_MARKED_AS_DERIVED_UNION));

        association.setDerivedUnion(true);
        ml = implementationRelation.validate(association.getIpsProject());
        assertThat(ml, lacksMessageCode(IAssociation.MSGCODE_NOT_MARKED_AS_DERIVED_UNION));
    }

    @Test
    public void testFindDerivedUnionCandidates() {
        IAssociation[] candidates = association.findDerivedUnionCandidates(ipsProject);
        assertEquals(0, candidates.length);

        association.setDerivedUnion(true);
        candidates = association.findDerivedUnionCandidates(ipsProject);
        assertEquals(0, candidates.length);

        association.setDerivedUnion(true);
        candidates = implementationRelation.findDerivedUnionCandidates(ipsProject);
        assertEquals(1, candidates.length);
        assertSame(association, candidates[0]);

        // if the association is not a derived union, it is not a candidate
        association.setDerivedUnion(false);
        candidates = implementationRelation.findDerivedUnionCandidates(ipsProject);
        assertEquals(0, candidates.length);

        // if the target type of the derived union is not a supertype (or same type) as the target
        // of the association, it is not a candidate
        association.setTarget("MotorPolicy");
        candidates = implementationRelation.findDerivedUnionCandidates(ipsProject);
        assertEquals(0, candidates.length);
    }

    @Test
    public void testFindSubsettedDerivedUnion() {
        association.setTargetRoleSingular("myRole");
        assertNull(association.findSubsettedDerivedUnion(ipsProject));

        association.setSubsettedDerivedUnion("SuperSet");
        assertNull(association.findSubsettedDerivedUnion(ipsProject));

        IType supertype = newProductCmptType(ipsProject, "Supertype");
        productType.setSupertype(supertype.getQualifiedName());

        association.setSubsettedDerivedUnion("SuperSet");
        assertNull(association.findSubsettedDerivedUnion(ipsProject));

        IAssociation union = supertype.newAssociation();
        union.setTargetRoleSingular("SuperSet");
        union.setDerivedUnion(true);
        assertSame(union, association.findSubsettedDerivedUnion(ipsProject));

        // association should be found wether the union flag is set or not.
        union.setDerivedUnion(false);
        assertSame(union, association.findSubsettedDerivedUnion(ipsProject));

        // make sure union in same type is found
        IAssociation union2 = productType.newAssociation();
        union2.setTargetRoleSingular("SuperSet");
        union2.setDerivedUnion(true);
        assertSame(union2, association.findSubsettedDerivedUnion(ipsProject));
    }

    @Test
    public void testIsSubsetOfADerivedUnion() {
        association.setSubsettedDerivedUnion("");
        assertFalse(association.isSubsetOfADerivedUnion());
        association.setSubsettedDerivedUnion("someContainerRelation");
        assertTrue(association.isSubsetOfADerivedUnion());
    }

    @Test
    public void testIsSubsetOfDerivedUnion_Union() {
        assertFalse(association.isSubsetOfDerivedUnion(null, ipsProject));

        IAssociation union = productType.newAssociation();
        union.setTargetRoleSingular("Target");
        union.setDerivedUnion(true);
        assertFalse(association.isSubsetOfDerivedUnion(union, ipsProject));

        association.setSubsettedDerivedUnion(union.getName());
        assertTrue(association.isSubsetOfDerivedUnion(union, ipsProject));

        // check if the method returns false if the container relation has the same name but belongs
        // to a different type
        IType otherType = newProductCmptType(ipsProject, "OtherType");
        IAssociation otherRelation = otherType.newAssociation();
        otherRelation.setDerivedUnion(true);
        otherRelation.setTargetRoleSingular("Target");
        assertFalse(association.isSubsetOfDerivedUnion(otherRelation, ipsProject));
    }

    @Test
    public void testValidate_TargetDoesNotExist() {
        MessageList ml = new MessageList();

        association.setTarget("UnknownTarget");
        ml = association.validate(association.getIpsProject());
        assertThat(ml, hasMessageCode(IAssociation.MSGCODE_TARGET_DOES_NOT_EXIST));

        association.setTarget(targetType.getQualifiedName());
        ml = association.validate(association.getIpsProject());
        assertThat(ml, lacksMessageCode(IAssociation.MSGCODE_TARGET_DOES_NOT_EXIST));
    }

    @Test
    public void testValidate_TargetRoleSingularMustBeAValidJavaFieldName() {
        association.setTargetRoleSingular("invalid java field name");

        Message message = association.validate(ipsProject).getMessageByCode(
                IAssociation.MSGCODE_TARGET_ROLE_SINGULAR_NOT_A_VALID_JAVA_FIELD_NAME);

        assertNotNull(message);
        assertEquals(Message.ERROR, message.getSeverity());
        assertEquals(association, message.getInvalidObjectProperties().get(0).getObject());
        assertEquals(IAssociation.PROPERTY_TARGET_ROLE_SINGULAR,
                message.getInvalidObjectProperties().get(0).getProperty());
    }

    @Test
    public void testValidate_TargetRolePluralMustBeAValidJavaFieldName() {
        association.setTargetRolePlural("invalid java field name");

        Message message = association.validate(ipsProject).getMessageByCode(
                IAssociation.MSGCODE_TARGET_ROLE_PLURAL_NOT_A_VALID_JAVA_FIELD_NAME);

        assertNotNull(message);
        assertEquals(Message.ERROR, message.getSeverity());
        assertEquals(association, message.getInvalidObjectProperties().get(0).getObject());
        assertEquals(IAssociation.PROPERTY_TARGET_ROLE_PLURAL,
                message.getInvalidObjectProperties().get(0).getProperty());
    }

    @Test
    public void testValidate_TargetRoleSingularMustBeSet() {
        association.setTargetRoleSingular("");
        MessageList ml = association.validate(association.getIpsProject());
        assertThat(ml, hasMessageCode(IAssociation.MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET));

        association.setTargetRoleSingular("Coverage");
        ml = association.validate(association.getIpsProject());
        assertThat(ml, lacksMessageCode(IAssociation.MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET));
    }

    @Test
    public void testValidate_TargetRolePluralMayBeEmptyIfCardinalityAllowsIt() {
        association.setMinCardinality(0);
        association.setMaxCardinality(1);
        association.setTargetRoleSingular("singular");
        association.setTargetRolePlural("");

        assertTrue(association.isValid(ipsProject));
    }

    @Test
    public void testValidate_TargetRolePluralMustNotBeEmptyIfTheCardinalityDoesNotAllowIt() {
        association.setMinCardinality(0);
        association.setMaxCardinality(Integer.MAX_VALUE);
        association.setTargetRoleSingular("singular");
        association.setTargetRolePlural("");

        Message message = association.validate(association.getIpsProject()).getMessageByCode(
                IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET);

        assertEquals(Message.ERROR, message.getSeverity());
        assertEquals(association, message.getInvalidObjectProperties().get(0).getObject());
        assertEquals(IAssociation.PROPERTY_TARGET_ROLE_PLURAL,
                message.getInvalidObjectProperties().get(0).getProperty());
    }

    @Test
    public void testValidate_MaxCardinalityMustBeAtLeast1() {
        association.setMaxCardinality(0);
        MessageList ml = association.validate(association.getIpsProject());
        assertThat(ml, hasMessageCode(IAssociation.MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1));

        association.setMaxCardinality(1);
        ml = association.validate(association.getIpsProject());
        assertThat(ml, lacksMessageCode(IAssociation.MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1));
    }

    @Test
    public void testValidate_MaxCardinalityIsLessThanMin() {
        association.setMinCardinality(3);
        association.setMaxCardinality(2);
        MessageList ml = association.validate(association.getIpsProject());
        assertThat(ml, hasMessageCode(IAssociation.MSGCODE_MAX_IS_LESS_THAN_MIN));

        association.setMaxCardinality(4);
        ml = association.validate(association.getIpsProject());
        assertThat(ml, lacksMessageCode(IAssociation.MSGCODE_MAX_IS_LESS_THAN_MIN));
    }

    @Test
    public void testValidateMaxCardinalityForContainerRelationTooLow() throws Exception {
        MessageList ml = association.validate(association.getIpsProject());
        assertThat(ml, lacksMessageCode(IAssociation.MSGCODE_MAX_CARDINALITY_FOR_DERIVED_UNION_TOO_LOW));

        association.setMaxCardinality(1);
        association.setDerivedUnion(true);
        ml = association.validate(association.getIpsProject());
        assertThat(ml, hasMessageCode(IAssociation.MSGCODE_MAX_CARDINALITY_FOR_DERIVED_UNION_TOO_LOW));
    }

    @Test
    public void testValidationTargetRolePluralMustBeSet() throws Exception {
        TestIpsArtefactBuilderSet builderset = new TestIpsArtefactBuilderSet();
        builderset.setIpsProject(association.getIpsProject());
        setArtefactBuildset(association.getIpsProject(), builderset);

        association.setMinCardinality(1);
        association.setMaxCardinality(1);
        association.setTargetRolePlural("");
        MessageList ml = association.validate(association.getIpsProject());
        assertThat(ml, lacksMessageCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));

        association.setMaxCardinality(2);
        ml = association.validate(association.getIpsProject());
        assertThat(ml, hasMessageCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));

        association.setTargetRolePlural("role1");
        ml = association.validate(association.getIpsProject());
        assertThat(ml, lacksMessageCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));

        builderset.setRoleNamePluralRequiredForTo1Relations(true);
        IIpsModel.get().clearValidationCache();
        association.setMaxCardinality(1);
        association.setTargetRolePlural("");
        ml = association.validate(association.getIpsProject());
        assertThat(ml, hasMessageCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));
    }

    @Test
    public void testValidateDerivedUnionAndSubsetOfDerivedUnionNotTheSame() throws Exception {
        IPolicyCmptType sourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldA");
        IPolicyCmptType targetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldB");
        IAssociation association = sourceCmpt.newAssociation();
        association.setTarget(targetCmpt.getQualifiedName());
        association.setDerivedUnion(true);
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTargetRoleSingular(targetCmpt.getQualifiedName());
        association.setSubsettedDerivedUnion(association.getName());
        MessageList msgList = association.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_DERIVED_UNION_SUBSET_NOT_SAME_AS_DERIVED_UNION));

        association.setSubsettedDerivedUnion("");
        msgList = association.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_DERIVED_UNION_SUBSET_NOT_SAME_AS_DERIVED_UNION));
    }

    @Test
    public void testValidateSubsetOfDerivedUnionSameMaxCardinality() throws Exception {
        IPolicyCmptType sourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldA");
        IPolicyCmptType targetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldB");
        IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)sourceCmpt.newAssociation();
        association.setTarget(targetCmpt.getQualifiedName());
        association.setDerivedUnion(true);
        association.setQualified(true);
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setMaxCardinality(Integer.MAX_VALUE);
        association.setTargetRoleSingular(targetCmpt.getQualifiedName());

        IPolicyCmptType subSourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "ASubtype");
        subSourceCmpt.setSupertype(sourceCmpt.getQualifiedName());
        IPolicyCmptType subTargetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "BSubtype");
        subTargetCmpt.setSupertype(targetCmpt.getQualifiedName());
        IPolicyCmptTypeAssociation subAssociation = (IPolicyCmptTypeAssociation)subSourceCmpt.newAssociation();
        subAssociation.setTarget(subTargetCmpt.getQualifiedName());
        subAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        subAssociation.setTargetRoleSingular(subTargetCmpt.getQualifiedName());
        subAssociation.setSubsettedDerivedUnion(association.getName());
        subAssociation.setMaxCardinality(1);
        subAssociation.setQualified(true);

        MessageList msgList = subAssociation.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_SUBSET_OF_DERIVED_UNION_SAME_MAX_CARDINALITY));

        subAssociation.setMaxCardinality(Integer.MAX_VALUE);
        msgList = subAssociation.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_SUBSET_OF_DERIVED_UNION_SAME_MAX_CARDINALITY));

        association.setMaxCardinality(1);
        msgList = subAssociation.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_SUBSET_OF_DERIVED_UNION_SAME_MAX_CARDINALITY));
    }

    @Test
    public void testValidateConstrain_Names() throws Exception {
        IPolicyCmptType sourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldA");
        IPolicyCmptType targetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldB");
        IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)sourceCmpt.newAssociation();
        association.setTarget(targetCmpt.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setMaxCardinality(Integer.MAX_VALUE);
        association.setTargetRoleSingular(targetCmpt.getQualifiedName());
        association.setTargetRolePlural(targetCmpt.getQualifiedName() + "s");

        IPolicyCmptType subSourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "ASubtype");
        subSourceCmpt.setSupertype(sourceCmpt.getQualifiedName());

        IPolicyCmptTypeAssociation subAssociation = (IPolicyCmptTypeAssociation)subSourceCmpt.newAssociation();
        subAssociation.setTarget(targetCmpt.getQualifiedName());
        subAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        subAssociation.setTargetRoleSingular(targetCmpt.getQualifiedName());
        subAssociation.setTargetRolePlural(targetCmpt.getQualifiedName() + "s");
        subAssociation.setConstrain(true);

        MessageList msgList = subAssociation.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAINED_SINGULAR_NOT_FOUND));

        subAssociation.setTargetRoleSingular("BSub");

        msgList = subAssociation.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAINED_SINGULAR_NOT_FOUND));

        subAssociation.setTargetRoleSingular(targetCmpt.getQualifiedName());
        subAssociation.setTargetRolePlural("BSubs");

        msgList = subAssociation.validate(ipsProject);
        assertNotNull(msgList.toString(), msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAINED_PLURAL_NOT_FOUND));
    }

    @Test
    public void testValidateConstrain_DerivedUnionOrSubSetOfDerivedUnion() throws Exception {
        IPolicyCmptType sourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldA");
        IPolicyCmptType targetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldB");
        IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)sourceCmpt.newAssociation();
        association.setTarget(targetCmpt.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTargetRoleSingular(targetCmpt.getQualifiedName());

        IPolicyCmptType subSourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "ASubtype");
        subSourceCmpt.setSupertype(sourceCmpt.getQualifiedName());
        IPolicyCmptType subTargetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "BSubtype");
        subTargetCmpt.setSupertype(targetCmpt.getQualifiedName());
        IPolicyCmptTypeAssociation subAssociation = (IPolicyCmptTypeAssociation)subSourceCmpt.newAssociation();
        subAssociation.setTarget(subTargetCmpt.getQualifiedName());
        subAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        subAssociation.setTargetRoleSingular(targetCmpt.getQualifiedName());
        subAssociation.setConstrain(true);

        MessageList msgList = subAssociation.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_DERIVED_UNION));
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_SUBSET_DERIVED_UNION));

        subAssociation.setDerivedUnion(true);
        subAssociation.setSubsettedDerivedUnion(association.getName());

        msgList = subAssociation.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_DERIVED_UNION));
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_SUBSET_DERIVED_UNION));
    }

    @Test
    public void testValidateConstrain_SupertypeDerivedUnionOrSubSetOfDerivedUnion() throws Exception {
        IPolicyCmptType sourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldA");
        IPolicyCmptType targetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldB");
        IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)sourceCmpt.newAssociation();
        association.setTarget(targetCmpt.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTargetRoleSingular(targetCmpt.getQualifiedName());

        IPolicyCmptType subSourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "ASubtype");
        subSourceCmpt.setSupertype(sourceCmpt.getQualifiedName());
        IPolicyCmptType subTargetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "BSubtype");
        subTargetCmpt.setSupertype(targetCmpt.getQualifiedName());
        IPolicyCmptTypeAssociation subAssociation = (IPolicyCmptTypeAssociation)subSourceCmpt.newAssociation();
        subAssociation.setTarget(subTargetCmpt.getQualifiedName());
        subAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        subAssociation.setTargetRoleSingular(targetCmpt.getQualifiedName());
        subAssociation.setConstrain(true);

        MessageList msgList = subAssociation.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAINED_DERIVED_UNION));
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAINED_SUBSET_DERIVED_UNION));

        association.setDerivedUnion(true);
        association.setSubsettedDerivedUnion(association.getName());

        msgList = subAssociation.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAINED_DERIVED_UNION));
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAINED_SUBSET_DERIVED_UNION));
    }

    @Test
    public void testValidateConstrain_SupertypeOfTargetIsNotInAssociationWithSupertypeOfThis() {
        IPolicyCmptType sourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldA");
        IPolicyCmptType targetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldB");
        IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)sourceCmpt.newAssociation();
        association.setTarget(targetCmpt.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTargetRoleSingular(targetCmpt.getQualifiedName());

        IPolicyCmptType subSourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "ASubtype");
        subSourceCmpt.setSupertype(sourceCmpt.getQualifiedName());
        IPolicyCmptType subTargetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "BSubtype");
        subTargetCmpt.setSupertype(targetCmpt.getQualifiedName());
        IPolicyCmptTypeAssociation subAssociation = (IPolicyCmptTypeAssociation)subSourceCmpt.newAssociation();
        subAssociation.setTarget(subTargetCmpt.getQualifiedName());
        subAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        subAssociation.setTargetRoleSingular(targetCmpt.getQualifiedName());
        subAssociation.setConstrain(true);

        MessageList msgList = subAssociation.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAINED_TARGET_SUPERTYP_NOT_COVARIANT));

        subSourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "ASubtype2");
        subSourceCmpt.setSupertype(sourceCmpt.getQualifiedName());
        subTargetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "BSubtype2");
        subAssociation = (IPolicyCmptTypeAssociation)subSourceCmpt.newAssociation();
        subAssociation.setTarget(subTargetCmpt.getQualifiedName());
        subAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        subAssociation.setTargetRoleSingular(targetCmpt.getQualifiedName());
        subAssociation.setConstrain(true);

        msgList = subAssociation.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAINED_TARGET_SUPERTYP_NOT_COVARIANT));
    }

    @Test
    public void testValidateConstrain_MatchingAssociationProductNotContrained() {
        setUpConstrainedMatchingAssociations();
        constrains_subProductAssociation.setConstrain(false);

        MessageList msgList = constrains_subAssociation.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_INVALID_MATCHING_ASSOCIATION));
    }

    @Test
    public void testValidateConstrain_MatchingAssociationPolicyNotContrained() {
        setUpConstrainedMatchingAssociations();
        constrains_subAssociation.setConstrain(false);

        MessageList msgList = constrains_subProductAssociation.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_INVALID_MATCHING_ASSOCIATION));
    }

    @Test
    public void testValidateConstrain_SuperAssociationsProductDoesNotMatch() {
        setUpConstrainedMatchingAssociations();
        // no matching association in super class
        constrains_sourceClass.setConfigurableByProductCmptType(false);
        constrains_targetClass.setConfigurableByProductCmptType(false);

        MessageList msgList = constrains_subAssociation.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_INVALID_MATCHING_ASSOCIATION));
    }

    @Test
    public void testValidateConstrain_SuperAssociationsPolicyDoesNotMatch() {
        setUpConstrainedMatchingAssociations();
        // no matching association in super class
        constrains_sourceClass.setConfigurableByProductCmptType(false);
        constrains_targetClass.setConfigurableByProductCmptType(false);

        MessageList msgListProduct = constrains_subProductAssociation.validate(ipsProject);
        assertNotNull(msgListProduct.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_INVALID_MATCHING_ASSOCIATION));
    }

    @Test
    public void testValidateConstrain_noMatchingAssociationsAtAll() {
        setUpConstrainedMatchingAssociations();
        constrains_sourceClass.setConfigurableByProductCmptType(false);
        constrains_targetClass.setConfigurableByProductCmptType(false);
        constrains_sourceProductClass.setConfigurationForPolicyCmptType(false);
        constrains_targetProductClass.setConfigurationForPolicyCmptType(false);
        constrains_subSourceClass.setConfigurableByProductCmptType(false);
        constrains_subTargetClass.setConfigurableByProductCmptType(false);
        constrains_subSourceProductClass.setConfigurationForPolicyCmptType(false);
        constrains_subTargetProductClass.setConfigurationForPolicyCmptType(false);

        MessageList msgList = constrains_subProductAssociation.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_INVALID_MATCHING_ASSOCIATION));
        msgList = constrains_subAssociation.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_INVALID_MATCHING_ASSOCIATION));
    }

    @Test
    public void testValidateConstrain_noMatchingAssociationInSubclasses() {
        setUpConstrainedMatchingAssociations();
        constrains_subSourceClass.setConfigurableByProductCmptType(false);
        constrains_subTargetClass.setConfigurableByProductCmptType(false);
        constrains_subSourceProductClass.setConfigurationForPolicyCmptType(false);
        constrains_subTargetProductClass.setConfigurationForPolicyCmptType(false);

        MessageList msgList = constrains_subProductAssociation.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_INVALID_MATCHING_ASSOCIATION));
        msgList = constrains_subAssociation.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_INVALID_MATCHING_ASSOCIATION));
    }

    @Test
    public void testValidateConstrain_matchingAssociationsValid() {
        setUpConstrainedMatchingAssociations();

        MessageList msgList = constrains_subProductAssociation.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_INVALID_MATCHING_ASSOCIATION));
        msgList = constrains_subAssociation.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IAssociation.MSGCODE_CONSTRAIN_INVALID_MATCHING_ASSOCIATION));
    }

    private void setUpConstrainedMatchingAssociations() {
        constrains_sourceClass = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldA");
        constrains_targetClass = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "oldB");
        IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)constrains_sourceClass.newAssociation();
        association.setTarget(constrains_targetClass.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTargetRoleSingular(constrains_targetClass.getQualifiedName());

        constrains_subSourceClass = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "ASubtype");
        constrains_subSourceClass.setSupertype(constrains_sourceClass.getQualifiedName());
        constrains_subTargetClass = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "BSubtype");
        constrains_subTargetClass.setSupertype(constrains_targetClass.getQualifiedName());
        constrains_subAssociation = (IPolicyCmptTypeAssociation)constrains_subSourceClass.newAssociation();
        constrains_subAssociation.setTarget(constrains_subTargetClass.getQualifiedName());
        constrains_subAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        constrains_subAssociation.setTargetRoleSingular(constrains_targetClass.getQualifiedName());
        constrains_subAssociation.setConstrain(true);

        constrains_sourceProductClass = newProductCmptType(ipsProject, "ProductA");
        constrains_targetProductClass = newProductCmptType(ipsProject, "ProductB");
        IProductCmptTypeAssociation productAssociation = (IProductCmptTypeAssociation)constrains_sourceProductClass
                .newAssociation();
        productAssociation.setAssociationType(AssociationType.AGGREGATION);
        productAssociation.setTarget(constrains_targetProductClass.getQualifiedName());

        constrains_sourceClass.setProductCmptType(constrains_sourceProductClass.getQualifiedName());
        constrains_sourceProductClass.setPolicyCmptType(constrains_sourceClass.getQualifiedName());
        constrains_sourceProductClass.setConfigurationForPolicyCmptType(true);
        constrains_targetClass.setProductCmptType(constrains_targetProductClass.getQualifiedName());
        constrains_targetProductClass.setPolicyCmptType(constrains_targetClass.getQualifiedName());
        constrains_targetProductClass.setConfigurationForPolicyCmptType(true);

        constrains_subSourceProductClass = newProductCmptType(ipsProject, "SubProductA");
        constrains_subSourceProductClass.setSupertype(constrains_sourceProductClass.getQualifiedName());
        constrains_subTargetProductClass = newProductCmptType(ipsProject, "SubProductB");
        constrains_subTargetProductClass.setSupertype(constrains_targetProductClass.getQualifiedName());
        constrains_subProductAssociation = (IProductCmptTypeAssociation)constrains_subSourceProductClass
                .newAssociation();
        constrains_subProductAssociation.setAssociationType(AssociationType.AGGREGATION);
        constrains_subProductAssociation.setTarget(constrains_subTargetProductClass.getQualifiedName());
        constrains_subProductAssociation.setConstrain(true);

        constrains_subSourceClass.setProductCmptType(constrains_subSourceProductClass.getQualifiedName());
        constrains_subSourceProductClass.setPolicyCmptType(constrains_subSourceClass.getQualifiedName());
        constrains_subSourceProductClass.setConfigurationForPolicyCmptType(true);
        constrains_subTargetClass.setProductCmptType(constrains_subTargetProductClass.getQualifiedName());
        constrains_subTargetProductClass.setPolicyCmptType(constrains_subTargetClass.getQualifiedName());
        constrains_subTargetProductClass.setConfigurationForPolicyCmptType(true);
    }

    @Test
    public void testValidateConstrain_Cardinality_LargerMaximum() {
        IAssociation association = builder.createComposition().from(A).to(B).withCardinality(0, 5).build();
        IAssociation subAssociation = builder.constrain(association).from(SUB_A).withCardinality(0, 7).build();

        MessageList messages = subAssociation.validate(ipsProject);
        Message error = messages
                .getMessageByCode(IAssociation.MSGCODE_MAX_CARDINALITY_NOT_VALID_CONSTRAINT_FOR_SUPER_ASSOCIATION);

        assertThat(messages.size(), is(1));
        assertThat(error, is(not(nullValue())));
    }

    @Test
    public void testValidateConstrain_Cardinality_SmallerMinimum() {
        IAssociation association = builder.createComposition().from(A).to(B).withCardinality(5, 10).build();
        IAssociation subAssociation = builder.constrain(association).from(SUB_A).withCardinality(3, 10).build();

        MessageList messages = subAssociation.validate(ipsProject);
        Message error = messages
                .getMessageByCode(IAssociation.MSGCODE_MIN_CARDINALITY_NOT_VALID_CONSTRAINT_FOR_SUPER_ASSOCIATION);

        assertThat(messages.size(), is(1));
        assertThat(error, is(not(nullValue())));
    }

    @Test
    public void testValidateConstrain_Cardinality_MultipleToSingle() {
        IAssociation association = builder.createComposition().from(A).to(B).withCardinality(1, 2).build();
        IAssociation subAssociation = builder.constrain(association).from(SUB_A).withCardinality(1, 1).build();

        MessageList messages = subAssociation.validate(ipsProject);
        Message error = messages
                .getMessageByCode(IAssociation.MSGCODE_MAX_CARDINALITY_NOT_VALID_CONSTRAINT_FOR_SUPER_ASSOCIATION);

        assertThat(messages.size(), is(1));
        assertThat(error, is(not(nullValue())));
    }

    @Test
    public void testValidateConstrain_Cardinality_SubSubAssociation() {
        IAssociation association = builder.createComposition().from(A).to(B).withCardinality(1, 5).build();
        IAssociation subAssociation = builder.constrain(association).from(SUB_A).withCardinality(2, 4).build();
        IAssociation subSubAssociation = builder.constrain(subAssociation).from(SUB_SUB_A).withCardinality(1, 5)
                .build();

        MessageList messages = subAssociation.validate(ipsProject);
        MessageList subMessages = subSubAssociation.validate(ipsProject);
        Message minError = subMessages
                .getMessageByCode(IAssociation.MSGCODE_MIN_CARDINALITY_NOT_VALID_CONSTRAINT_FOR_SUPER_ASSOCIATION);
        Message maxError = subMessages
                .getMessageByCode(IAssociation.MSGCODE_MAX_CARDINALITY_NOT_VALID_CONSTRAINT_FOR_SUPER_ASSOCIATION);

        assertThat(messages.size(), is(0));
        assertThat(subMessages.size(), is(2));
        assertThat(minError, is(not(nullValue())));
        assertThat(maxError, is(not(nullValue())));
    }

    @Test
    public void testValidateConstrain_AssociationTypeNotEqualToSuperAssosiation() {
        IAssociation association = builder.createComposition().from(A).to(B).withCardinality(1, 5).build();
        IAssociation subAssociation = builder.constrain(association).from(SUB_A)
                .withCardinality(1, 5).build();
        subAssociation.setAssociationType(AssociationType.ASSOCIATION);

        MessageList superMessages = association.validate(ipsProject);
        MessageList messages = subAssociation.validate(ipsProject);
        Message error = messages.getMessageByCode(IAssociation.MSGCODE_ASSOCIATION_TYPE_NOT_EQUAL_TO_SUPER_ASSOCIATION);

        assertThat(superMessages.size(), is(0));
        assertThat(messages.size(), is(1));
        assertThat(error, is(not(nullValue())));
        assertThat(error.getText(), is(not(nullValue())));
    }

    @Test
    public void testGetDescriptionFromThisOrSuper() {
        IAssociation association = builder.createComposition().from(A).to(B).withCardinality(1, 5).build();
        association.setDescriptionText(Locale.ENGLISH, "english description");

        IAssociation subAssociation = builder.constrain(association).from(SUB_A)
                .withCardinality(1, 5).build();

        assertEquals("english description", subAssociation.getDescriptionTextFromThisOrSuper(Locale.ENGLISH));

        subAssociation.setDescriptionText(Locale.ENGLISH, "overwritten description");
        assertEquals("overwritten description",
                subAssociation.getDescriptionTextFromThisOrSuper(Locale.ENGLISH));
    }

    @Test
    public void testGetLabelValueFromThisOrSuper() {
        IAssociation association = builder.createComposition().from(A).to(B).withCardinality(1, 5).build();
        association.setLabelValue(Locale.ENGLISH, "english label");

        IAssociation subAssociation = builder.constrain(association).from(SUB_A)
                .withCardinality(1, 5).build();

        assertEquals("english label", subAssociation.getLabelValueFromThisOrSuper(Locale.ENGLISH));

        subAssociation.setLabelValue(Locale.ENGLISH, "overwritten label");
        assertEquals("overwritten label",
                subAssociation.getLabelValueFromThisOrSuper(Locale.ENGLISH));
    }

    @Test
    public void testGetPluralLabelValueFromThisOrSuper() {
        IAssociation association = builder.createComposition().from(A).to(B).withCardinality(1, 5).build();
        association.setPluralLabelValue(Locale.ENGLISH, "english plural label");

        IAssociation subAssociation = builder.constrain(association).from(SUB_A)
                .withCardinality(1, 5).build();

        assertEquals("english plural label", subAssociation.getPluralLabelValueFromThisOrSuper(Locale.ENGLISH));

        subAssociation.setPluralLabelValue(Locale.ENGLISH, "overwritten plural label");
        assertEquals("overwritten plural label",
                subAssociation.getPluralLabelValueFromThisOrSuper(Locale.ENGLISH));
    }

    @Test
    public void testToXml() {
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTarget("pack1.CoverageType");
        association.setTargetRoleSingular("CoverageType");
        association.setTargetRolePlural("CoverageTypes");
        association.setMinCardinality(2);
        association.setMaxCardinality(4);
        association.setDerivedUnion(true);
        association.setSubsettedDerivedUnion("BaseCoverageType");
        association.setConstrain(true);

        Element el = association.toXml(newDocument());
        association = productType.newAssociation();
        association.initFromXml(el);

        assertEquals(AssociationType.AGGREGATION, association.getAssociationType());
        assertEquals("pack1.CoverageType", association.getTarget());
        assertEquals("CoverageType", association.getTargetRoleSingular());
        assertEquals("CoverageTypes", association.getTargetRolePlural());
        assertEquals(2, association.getMinCardinality());
        assertEquals(4, association.getMaxCardinality());
        assertTrue(association.isDerivedUnion());
        assertEquals("BaseCoverageType", association.getSubsettedDerivedUnion());
        assertTrue(association.isConstrain());
    }

    @Test
    public void testInitFromXmlElement() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, 0);
        association.initFromXml(el);
        assertEquals(AssociationType.AGGREGATION, association.getAssociationType());
        assertEquals("pack1.CoverageType", association.getTarget());
        assertEquals("CoverageType", association.getTargetRoleSingular());
        assertEquals("CoverageTypes", association.getTargetRolePlural());
        assertEquals(1, association.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, association.getMaxCardinality());
        assertTrue(association.isDerivedUnion());
        assertTrue(association.isConstrain());
        assertEquals("BaseCoverageType", association.getSubsettedDerivedUnion());
        assertEquals("blabla", association.getDescriptionText(Locale.US));
    }

    @Test
    public void testFindTarget() {
        association.setTarget("");
        assertNull(association.findTarget(ipsProject));

        association.setTarget("unknown");
        assertNull(association.findTarget(ipsProject));

        association.setTarget(targetType.getQualifiedName());
        assertEquals(targetType, association.findTarget(ipsProject));
    }

    @Test
    public void testSetTarget() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_TARGET, association, "newTarget");
    }

    @Test
    public void testSetTargetRoleSingular() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_TARGET_ROLE_SINGULAR, association,
                "newRole");
    }

    @Test
    public void testSetTargetRolePlural() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_TARGET_ROLE_PLURAL, association,
                "newRoles");
    }

    @Test
    public void testSetMinCardinality() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_MIN_CARDINALITY, association,
                Integer.valueOf(42));
    }

    @Test
    public void testSetMaxCardinality() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_MAX_CARDINALITY, association,
                Integer.valueOf(42));
    }

    @Test
    public void testSetDerivedUnion() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_SUBSETTED_DERIVED_UNION,
                association, "SomeUnion");
    }

    @Test
    public void testSetConstrain() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_CONSTRAIN, association,
                Boolean.valueOf(!association.isConstrain()));
    }

    @Test
    public void testSetSubsettedDerivedUnion() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_DERIVED_UNION, association,
                Boolean.valueOf(!association.isDerivedUnion()));
    }

}
