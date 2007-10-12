/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.AggregationKind;
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
    private IAssociation relation; 

    private IAssociation implementationRelation;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productType = newProductCmptType(ipsProject, "Product");
        targetType = newProductCmptType(ipsProject, "CoverageType");
        relation = productType.newAssociation();
        relation.setTarget(targetType.getQualifiedName());
        relation.setTargetRoleSingular("CoverageType");
        
        IType motorProductType = newProductCmptType(ipsProject, "MotorProduct");
        IType motorCoverageType = newProductCmptType(ipsProject, "MotorCoverageType");
        motorProductType.setSupertype(productType.getQualifiedName());
        motorCoverageType.setSupertype(targetType.getQualifiedName());
        implementationRelation = motorProductType.newAssociation();
        implementationRelation.setTargetRoleSingular("MotorCoverage");
        implementationRelation.setTarget(motorCoverageType.getQualifiedName());
        implementationRelation.setSubsettedDerivedUnion(relation.getName());
    }
    
    public void testValidateTargetTypeNotASubtype() throws Exception {
        MessageList ml = new MessageList();
        relation.setDerivedUnion(true);

        ml = implementationRelation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_TYPE_NOT_A_SUBTYPE));
        
        IType otherType = newProductCmptType(ipsProject, "SomeType");
        relation.setTarget(otherType.getQualifiedName());
        ml = implementationRelation.validate();
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_TYPE_NOT_A_SUBTYPE));
    }

    public void testValidateTargetOfDerivedUnionNotFound() throws Exception {
        MessageList ml = new MessageList();
        relation.setDerivedUnion(true);

        ml = implementationRelation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_OF_DERIVED_UNION_DOES_NOT_EXIST));
        
        relation.setTarget("xxx");
        ml = implementationRelation.validate();
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_OF_DERIVED_UNION_DOES_NOT_EXIST));
    }

    public void testValidateDerivedUnionNotFound() throws Exception {
        MessageList ml = new MessageList();
        
        relation.setDerivedUnion(true);
        ml = implementationRelation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_DERIVED_UNION_NOT_FOUND));
        
        implementationRelation.setSubsettedDerivedUnion("xxx");
        ml = implementationRelation.validate();
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_DERIVED_UNION_NOT_FOUND));
    }

    public void testValidateNotMarkedAsContainerRelation() throws Exception {
        MessageList ml = new MessageList();
        
        relation.setDerivedUnion(false);
        ml = implementationRelation.validate();
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_NOT_MARKED_AS_DERIVED_UNION));
        
        relation.setDerivedUnion(true);
        ml = implementationRelation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_NOT_MARKED_AS_DERIVED_UNION));
    }

    public void testFindDerivedUnionCandidates() throws CoreException {
        IAssociation[] candidates = relation.findDerivedUnionCandidates(ipsProject);
        assertEquals(0, candidates.length);

        relation.setDerivedUnion(true);
        candidates = implementationRelation.findDerivedUnionCandidates(ipsProject);
        assertEquals(1, candidates.length);
        assertSame(relation, candidates[0]);

        // if the association is not a derived union, it is not a candidate
        relation.setDerivedUnion(false);
        candidates = implementationRelation.findDerivedUnionCandidates(ipsProject);
        assertEquals(0, candidates.length);
        
        // if the target type of the derived union is not a supertype (or same type) as the target of the association, it is not a candidate
        relation.setTarget("MotorPolicy");
        candidates = implementationRelation.findDerivedUnionCandidates(ipsProject);
        assertEquals(0, candidates.length);
    }
    
    public void testFindSubsettedDerivedUnion() throws CoreException {
        relation.setTargetRoleSingular("myRole");
        assertNull(relation.findSubsettedDerivedUnion(ipsProject));
        
        relation.setSubsettedDerivedUnion("SuperSet");
        assertNull(relation.findSubsettedDerivedUnion(ipsProject));
        
        IType supertype = newProductCmptType(ipsProject, "Supertype");
        productType.setSupertype(supertype.getQualifiedName());
        
        relation.setSubsettedDerivedUnion("SuperSet");
        assertNull(relation.findSubsettedDerivedUnion(ipsProject));

        IAssociation union = supertype.newAssociation();
        union.setTargetRoleSingular("SuperSet");
        union.setDerivedUnion(true);
        assertSame(union, relation.findSubsettedDerivedUnion(ipsProject));
        
        // association should be found wether the union flag is set or not.
        union.setDerivedUnion(false);
        assertSame(union, relation.findSubsettedDerivedUnion(ipsProject));
        
        // make sure union in same type is found
        IAssociation union2 = productType.newAssociation();
        union2.setTargetRoleSingular("SuperSet");
        union2.setDerivedUnion(true);
        assertSame(union2, relation.findSubsettedDerivedUnion(ipsProject));
    }
    
    public void testIsSubsetOfADerivedUnion() {
        relation.setSubsettedDerivedUnion("");
        assertFalse(relation.isSubsetOfADerivedUnion());
        relation.setSubsettedDerivedUnion("someContainerRelation");
        assertTrue(relation.isSubsetOfADerivedUnion());
    }

    public void testIsSubsetOfDerivedUnion_Union() throws CoreException {
        assertFalse(relation.isSubsetOfDerivedUnion(null, ipsProject));

        IAssociation union = this.productType.newAssociation();
        union.setTargetRoleSingular("Target");
        union.setDerivedUnion(true);
        assertFalse(relation.isSubsetOfDerivedUnion(union, ipsProject));
        
        relation.setSubsettedDerivedUnion(union.getName());
        assertTrue(relation.isSubsetOfDerivedUnion(union, ipsProject));
        
        // check if the method returns false if the container relation has the same name but belongs to a 
        // different type
        IType otherType = newProductCmptType(ipsProject, "OtherType");
        IAssociation otherRelation = otherType.newAssociation();
        otherRelation.setDerivedUnion(true);
        otherRelation.setTargetRoleSingular("Target");
        assertFalse(relation.isSubsetOfDerivedUnion(otherRelation, ipsProject));
    }
    
    public void testValidate_TargetDoesNotExist() throws CoreException {
        MessageList ml = new MessageList();

        relation.setTarget("UnknownTarget");
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_DOES_NOT_EXIST));
        
        relation.setTarget(targetType.getQualifiedName());
        ml = relation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_DOES_NOT_EXIST));
    }
    
    public void testValidate_TargetRoleSingularMustBeSet() throws CoreException {
        relation.setTargetRoleSingular("");
        MessageList ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET));
        
        relation.setTargetRoleSingular("Coverage");
        ml = relation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET));
    }
    
    public void testValidate_MaxCardinalityMustBeAtLeast1() throws CoreException {
        relation.setMaxCardinality(0);
        MessageList ml = relation.validate();
        assertNotNull(ml.getMessageByCode(Association.MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1));
        
        relation.setMaxCardinality(1);
        ml = relation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1));
    }

    public void testValidate_MaxCardinalityIsLessThanMin() throws CoreException {
        relation.setMinCardinality(3);
        relation.setMaxCardinality(2);
        MessageList ml = relation.validate();
        assertNotNull(ml.getMessageByCode(Association.MSGCODE_MAX_IS_LESS_THAN_MIN));
        
        relation.setMaxCardinality(4);
        ml = relation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_MAX_IS_LESS_THAN_MIN));
    }
    
    public void testValidateMaxCardinalityForContainerRelationTooLow() throws Exception {
        MessageList ml = relation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_MAX_CARDINALITY_FOR_DERIVED_UNION_TOO_LOW));
        
        relation.setMaxCardinality(1);
        relation.setDerivedUnion(true);
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_MAX_CARDINALITY_FOR_DERIVED_UNION_TOO_LOW));
    }

    public void testValidationTargetRolePlural_EqualsTargetRoleSingular() throws Exception {
        relation.setMaxCardinality(10);
        relation.setTargetRoleSingular("role1");
        relation.setTargetRolePlural("role2");
        MessageList ml = relation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR));
        
        relation.setTargetRolePlural("role1");
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR));

        // even if the plural form is not needed, the rolenames shouldn't be equal.
        relation.setMaxCardinality(1);
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR));
        
        relation.setTargetRolePlural("");
        relation.setTargetRoleSingular("");
        ml = relation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR));
    }

    public void testValidationTargetRolePluralMustBeSet() throws Exception {
        TestIpsArtefactBuilderSet builderset = new TestIpsArtefactBuilderSet();
        builderset.setRoleNamePluralRequiredForTo1Relations(false);
        setArtefactBuildset(relation.getIpsProject(), builderset);

        relation.setMinCardinality(1);
        relation.setMaxCardinality(1);
        relation.setTargetRolePlural("");
        MessageList ml = relation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));
        
        relation.setMaxCardinality(2);
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));
        
        relation.setTargetRolePlural("role1");
        ml = relation.validate();
        assertNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));
        
        builderset.setRoleNamePluralRequiredForTo1Relations(true);
        IpsPlugin.getDefault().getIpsModel().clearValidationCache();
        relation.setMaxCardinality(1);
        relation.setTargetRolePlural("");
        ml = relation.validate();
        assertNotNull(ml.getMessageByCode(IAssociation.MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET));
    }
    
    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#toXml(org.w3c.dom.Document)}.
     */
    public void testToXml() {
        relation.setTarget("pack1.CoverageType");
        relation.setTargetRoleSingular("CoverageType");
        relation.setTargetRolePlural("CoverageTypes");
        relation.setMinCardinality(2);
        relation.setMaxCardinality(4);
        relation.setDerivedUnion(true);
        relation.setSubsettedDerivedUnion("BaseCoverageType");
        
        Element el = relation.toXml(newDocument());
        relation = productType.newAssociation();
        relation.initFromXml(el);
        
        assertEquals("pack1.CoverageType", relation.getTarget());
        assertEquals("CoverageType", relation.getTargetRoleSingular());
        assertEquals("CoverageTypes", relation.getTargetRolePlural());
        assertEquals(2, relation.getMinCardinality());
        assertEquals(4, relation.getMaxCardinality());
        assertTrue(relation.isDerivedUnion());
        assertEquals("BaseCoverageType", relation.getSubsettedDerivedUnion());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initFromXml(org.w3c.dom.Element)}.
     */
    public void testInitFromXmlElement() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, 0);
        relation.initFromXml(el);
        assertEquals(AggregationKind.SHARED, relation.getAggregationKind());
        assertEquals("pack1.CoverageType", relation.getTarget());
        assertEquals("CoverageType", relation.getTargetRoleSingular());
        assertEquals("CoverageTypes", relation.getTargetRolePlural());
        assertEquals(1, relation.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, relation.getMaxCardinality());
        assertTrue(relation.isDerivedUnion());
        assertEquals("BaseCoverageType", relation.getSubsettedDerivedUnion());
        assertEquals("blabla", relation.getDescription());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#findTarget()}.
     * @throws CoreException 
     */
    public void testFindTarget() throws CoreException {
        relation.setTarget("");
        assertNull(relation.findTarget(ipsProject));
        
        relation.setTarget("unknown");
        assertNull(relation.findTarget(ipsProject));
        
        relation.setTarget(targetType.getQualifiedName());
        assertEquals(targetType, relation.findTarget(ipsProject));
    }

    public void testSetTarget() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_TARGET, relation, "newTarget");
    }

    public void testSetTargetRoleSingular() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_TARGET_ROLE_SINGULAR, relation, "newRole");
    }

    public void testSetTargetRolePlural() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_TARGET_ROLE_PLURAL, relation, "newRoles");
    }

    public void testSetMinCardinality() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_MIN_CARDINALITY, relation, new Integer(42));
    }

    public void testSetMaxCardinality() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_MAX_CARDINALITY, relation, new Integer(42));
    }

    public void testSetDerivedUnion() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_SUBSETTED_DERIVED_UNION, relation, "SomeUnion");
    }

    public void testSetSubsettedDerivedUnion() {
        super.testPropertyAccessReadWrite(Association.class, IAssociation.PROPERTY_DERIVED_UNION, relation, Boolean.valueOf(!relation.isDerivedUnion()));
    }

}
