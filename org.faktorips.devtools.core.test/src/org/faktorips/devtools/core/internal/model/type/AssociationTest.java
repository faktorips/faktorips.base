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

package org.faktorips.devtools.core.internal.model.type;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productType = newProductCmptType(ipsProject, "Product");
        targetType = newProductCmptType(ipsProject, "CoverageType");
        association = productType.newAssociation();
        association.setTarget(targetType.getQualifiedName());
        association.setTargetRoleSingular("CoverageType");

        IType motorProductType = newProductCmptType(ipsProject, "MotorProduct");
        IType motorCoverageType = newProductCmptType(ipsProject, "MotorCoverageType");
        motorProductType.setSupertype(productType.getQualifiedName());
        motorCoverageType.setSupertype(targetType.getQualifiedName());
        implementationRelation = motorProductType.newAssociation();
        implementationRelation.setTargetRoleSingular("MotorCoverage");
        implementationRelation.setTarget(motorCoverageType.getQualifiedName());
        implementationRelation.setSubsettedDerivedUnion(association.getName());
    }

    public void testIs1To1_And_Is1ToMany() throws CoreException {
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

    public void testValidateTargetTypeNotASubtype() throws Exception {
        MessageList ml = new MessageList();
        association.setDerivedUnion(true);

        ml = implementationRelation.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_TYPE_NOT_A_SUBTYPE));

        IType otherType = newProductCmptType(ipsProject, "SomeType");
        association.setTarget(otherType.getQualifiedName());
        ml = implementationRelation.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_TYPE_NOT_A_SUBTYPE));
    }

    public void testValidateTargetOfDerivedUnionNotFound() throws Exception {
        MessageList ml = new MessageList();
        association.setDerivedUnion(true);

        ml = implementationRelation.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_OF_DERIVED_UNION_DOES_NOT_EXIST));

        association.setTarget("xxx");
        ml = implementationRelation.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_OF_DERIVED_UNION_DOES_NOT_EXIST));
    }

    public void testValidateDerivedUnionNotFound() throws Exception {
        MessageList ml = new MessageList();

        association.setDerivedUnion(true);
        ml = implementationRelation.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_DERIVED_UNION_NOT_FOUND));

        implementationRelation.setSubsettedDerivedUnion("xxx");
        ml = implementationRelation.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_DERIVED_UNION_NOT_FOUND));
    }

    public void testValidateNotMarkedAsContainerRelation() throws Exception {
        MessageList ml = new MessageList();

        association.setDerivedUnion(false);
        ml = implementationRelation.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_NOT_MARKED_AS_DERIVED_UNION));

        association.setDerivedUnion(true);
        ml = implementationRelation.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_NOT_MARKED_AS_DERIVED_UNION));
    }

    public void testFindDerivedUnionCandidates() throws CoreException {
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

    public void testFindSubsettedDerivedUnion() throws CoreException {
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

    public void testIsSubsetOfADerivedUnion() {
        association.setSubsettedDerivedUnion("");
        assertFalse(association.isSubsetOfADerivedUnion());
        association.setSubsettedDerivedUnion("someContainerRelation");
        assertTrue(association.isSubsetOfADerivedUnion());
    }

    public void testIsSubsetOfDerivedUnion_Union() throws CoreException {
        assertFalse(association.isSubsetOfDerivedUnion(null, ipsProject));

        IAssociation union = this.productType.newAssociation();
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

    public void testValidate_TargetDoesNotExist() throws CoreException {
        MessageList ml = new MessageList();

        association.setTarget("UnknownTarget");
        ml = association.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_DOES_NOT_EXIST));

        association.setTarget(targetType.getQualifiedName());
        ml = association.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_DOES_NOT_EXIST));
    }

    public void testValidate_TargetRoleSingularMustBeSet() throws CoreException {
        association.setTargetRoleSingular("");
        MessageList ml = association.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET));

        association.setTargetRoleSingular("Coverage");
        ml = association.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET));
    }

    public void testValidate_MaxCardinalityMustBeAtLeast1() throws CoreException {
        association.setMaxCardinality(0);
        MessageList ml = association.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(Association.MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1));

        association.setMaxCardinality(1);
        ml = association.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1));
    }

    public void testValidate_MaxCardinalityIsLessThanMin() throws CoreException {
        association.setMinCardinality(3);
        association.setMaxCardinality(2);
        MessageList ml = association.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(Association.MSGCODE_MAX_IS_LESS_THAN_MIN));

        association.setMaxCardinality(4);
        ml = association.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_MAX_IS_LESS_THAN_MIN));
    }

    public void testValidateMaxCardinalityForContainerRelationTooLow() throws Exception {
        MessageList ml = association.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_MAX_CARDINALITY_FOR_DERIVED_UNION_TOO_LOW));

        association.setMaxCardinality(1);
        association.setDerivedUnion(true);
        ml = association.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_MAX_CARDINALITY_FOR_DERIVED_UNION_TOO_LOW));
    }

    public void testValidationTargetRolePlural_EqualsTargetRoleSingular() throws Exception {
        association.setMaxCardinality(10);
        association.setTargetRoleSingular("role1");
        association.setTargetRolePlural("role2");
        MessageList ml = association.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR));

        association.setTargetRolePlural("role1");
        ml = association.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR));

        // even if the plural form is not needed, the rolenames shouldn't be equal.
        association.setMaxCardinality(1);
        ml = association.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR));

        association.setTargetRolePlural("");
        association.setTargetRoleSingular("");
        ml = association.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR));
    }

    public void testValidationTargetRolePluralMustBeSet() throws Exception {
        TestIpsArtefactBuilderSet builderset = new TestIpsArtefactBuilderSet();
        builderset.setIpsProject(association.getIpsProject());
        builderset.setRoleNamePluralRequiredForTo1Relations(false);
        setArtefactBuildset(association.getIpsProject(), builderset);

        association.setMinCardinality(1);
        association.setMaxCardinality(1);
        association.setTargetRolePlural("");
        MessageList ml = association.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));

        association.setMaxCardinality(2);
        ml = association.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));

        association.setTargetRolePlural("role1");
        ml = association.validate(association.getIpsProject());
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));

        builderset.setRoleNamePluralRequiredForTo1Relations(true);
        IpsPlugin.getDefault().getIpsModel().clearValidationCache();
        association.setMaxCardinality(1);
        association.setTargetRolePlural("");
        ml = association.validate(association.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));
    }

    public void testValidateDerivedUnionAndSubsetOfDerivedUnionNotTheSame() throws Exception {
        IPolicyCmptType sourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "A");
        IPolicyCmptType targetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "B");
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

    public void testValidateSubsetOfDerivedUnionSameMaxCardinality() throws Exception {
        IPolicyCmptType sourceCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "A");
        IPolicyCmptType targetCmpt = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "B");
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

    public void testToXml() {
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTarget("pack1.CoverageType");
        association.setTargetRoleSingular("CoverageType");
        association.setTargetRolePlural("CoverageTypes");
        association.setMinCardinality(2);
        association.setMaxCardinality(4);
        association.setDerivedUnion(true);
        association.setSubsettedDerivedUnion("BaseCoverageType");

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
    }

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
        assertEquals("BaseCoverageType", association.getSubsettedDerivedUnion());
        assertEquals("blabla", association.getDescription(Locale.US).getText());
    }

    public void testFindTarget() throws CoreException {
        association.setTarget("");
        assertNull(association.findTarget(ipsProject));

        association.setTarget("unknown");
        assertNull(association.findTarget(ipsProject));

        association.setTarget(targetType.getQualifiedName());
        assertEquals(targetType, association.findTarget(ipsProject));
    }

    public void testSetTarget() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_TARGET, association, "newTarget");
    }

    public void testSetTargetRoleSingular() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_TARGET_ROLE_SINGULAR, association,
                "newRole");
    }

    public void testSetTargetRolePlural() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_TARGET_ROLE_PLURAL, association,
                "newRoles");
    }

    public void testSetMinCardinality() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_MIN_CARDINALITY, association,
                new Integer(42));
    }

    public void testSetMaxCardinality() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_MAX_CARDINALITY, association,
                new Integer(42));
    }

    public void testSetDerivedUnion() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_SUBSETTED_DERIVED_UNION,
                association, "SomeUnion");
    }

    public void testSetSubsettedDerivedUnion() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_DERIVED_UNION, association, Boolean
                .valueOf(!association.isDerivedUnion()));
    }

}
