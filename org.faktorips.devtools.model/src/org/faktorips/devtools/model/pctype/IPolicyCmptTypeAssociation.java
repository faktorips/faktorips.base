/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.pctype;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAssociationInfo;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;

/**
 * Represents a directed(!) relation between two types. Bidirectional relations are represented in
 * Faktor-IPS as two corresponding directed relations. Given a relation the other corresponding
 * relation is called the inverse relation.
 * <p>
 * In conceptual models bidirectional relations can be either associations or compositions. A
 * bidirectional association is represented in Faktor-IPS by two relations of type association. A
 * bidirectional composition is represented in Faktor-IPS by one master to detail composition and
 * one detail to master composition.
 * 
 * @author Jan Ortmann
 */
public interface IPolicyCmptTypeAssociation extends IAssociation {

    /**
     * The list of applicable types. For policy component types, aggregations are not supported.
     */
    public static final AssociationType[] APPLICABLE_ASSOCIATION_TYPES = new AssociationType[] {
            AssociationType.COMPOSITION_MASTER_TO_DETAIL, AssociationType.COMPOSITION_DETAIL_TO_MASTER,
            AssociationType.ASSOCIATION };

    public static final String PROPERTY_INVERSE_ASSOCIATION = "inverseAssociation"; //$NON-NLS-1$

    public static final String PROPERTY_SHARED_ASSOCIATION = "sharedAssociation"; //$NON-NLS-1$

    public static final String PROPERTY_SUBSETTING_DERIVED_UNION_APPLICABLE = "derivedUnionApplicable"; //$NON-NLS-1$

    public static final String PROPERTY_MATCHING_ASSOCIATION_SOURCE = "matchingAssociationSource"; //$NON-NLS-1$

    public static final String PROPERTY_MATCHING_ASSOCIATION_NAME = "matchingAssociationName"; //$NON-NLS-1$

    public static final String PROPERTY_CONFIGURABLE = "configurable"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "PolicyCmptTypeRelation-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a reverse composition's max cardinality is not 1.
     */
    public static final String MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION = MSGCODE_PREFIX
            + "MaxCardinalityMustBe1ForReverseCombosition"; //$NON-NLS-1$

    /**
     * Validation message code to indicate the inverse relation definition is inconsistent with the
     * container relation.
     */
    public static final String MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION = MSGCODE_PREFIX
            + "InverseAssociationInconsistentWithDerivedUnion"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an association and it's inverse association must be
     * marked as container relations (or not).
     */
    public static final String MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER = MSGCODE_PREFIX
            + "ReverseRelationOfContainerRelationMustBeContainerRelationToo"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that given a relation with an inverse relation, this
     * inverse relation can be found but it does not specify the first relation as it's inverse
     * relation. This applies to associations only, as detail-to-master composition don't specify a
     * reverse relation.
     */
    public static final String MSGCODE_INVERSE_RELATION_MISMATCH = MSGCODE_PREFIX + "InverseRelationMismatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the reverse relation does not specify this relation
     * as its reverse one.
     */
    public static final String MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET = MSGCODE_PREFIX
            + "ReverseRelationNotInTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the reverse relation of an association must be an
     * association.
     */
    public static final String MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH = MSGCODE_PREFIX
            + "InverseAssociationTypeMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the inverse master to detail composition must be a
     * detail to master composition.
     */
    public static final String MSGCODE_INVERSE_MASTER_TO_DETAIL_TYPE_MISSMATCH = MSGCODE_PREFIX
            + "InverseMasterToDetailTypeMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the inverse detail to mater composition must be a
     * master to detail composition.
     */
    public static final String MSGCODE_INVERSE_DETAIL_TO_MASTER_TYPE_MISSMATCH = MSGCODE_PREFIX
            + "InverseDetailToMasterTypeMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that in case of detail to master associations the inverse
     * is always given.
     */
    public static final String MSGCODE_INVERSE_ASSOCIATION_MUST_BE_SET_IF_TYPE_IS_DETAIL_TO_MASTER = MSGCODE_PREFIX
            + "InverseAssociationMustBeSetIfTypeIsDetailToMaster"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that in case of detail to master associations the inverse
     * is always given.
     */
    public static final String MSGCODE_SHARED_ASSOCIATION_INVALID = MSGCODE_PREFIX + "invalidSharedAssociation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that if the inverse of a derived union exists then the
     * inverse of all subsetted derived union must be specified
     */
    public static final String MSGCODE_SUBSETTED_DERIVED_UNION_INVERSE_MUST_BE_EXISTS_IF_INVERSE_DERIVED_UNION_EXISTS = MSGCODE_PREFIX
            + "SubsettedDerivedUnionInverseMustBeExistsIfInverseDerivedUnionExists"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the specified matching association was not found
     */
    public static final String MSGCODE_MATCHING_ASSOCIATION_INVALID_SOURCE = MSGCODE_PREFIX
            + "MatchingAssociationInvalidSource"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the specified matching association was not found
     */
    public static final String MSGCODE_MATCHING_ASSOCIATION_NOT_FOUND = MSGCODE_PREFIX + "MatchingAssociationNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the specified matching association is invalid
     */
    public static final String MSGCODE_MATCHING_ASSOCIATION_INVALID = MSGCODE_PREFIX + "MatchingAssociationIsInvalid"; //$NON-NLS-1$

    public static final String MSGCODE_CONSTRAINED_QUALIFIER_MISMATCH = MSGCODE_PREFIX + "ConstrainQualifierNotMatch"; //$NON-NLS-1$

    /**
     * Returns the policy component type this relation belongs to.
     */
    public IPolicyCmptType getPolicyCmptType();

    /**
     * Returns <code>true</code> if this relation is either a master-to-detail or detail-to-master
     * composition, otherwise <code>false</code>.
     */
    public boolean isComposition();

    /**
     * Returns <code>true</code> if this is a composition from the master type to the detail type,
     * otherwise <code>false</code>.
     */
    public boolean isCompositionMasterToDetail();

    /**
     * Returns <code>true</code> if this is a composition from the detail type to the master type,
     * otherwise <code>false</code>.
     */
    public boolean isCompositionDetailToMaster();

    /**
     * Returns <code>true</code> if this association can be marked as derived union or can be a
     * subset of a derived union association. This is the case for associations and master-to-detail
     * composites.
     * 
     * @since 3.8
     */
    public boolean isDerivedUnionApplicable();

    /**
     * Returns the qualified name of the target policy component class.
     */
    @Override
    public String getTarget();

    /**
     * Returns the target policy component type or <code>null</code> if either this relation hasn't
     * got a target or the target does not exists.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     */
    public IPolicyCmptType findTargetPolicyCmptType(IIpsProject ipsProject);

    /**
     * Sets the qualified name of the target policy component class.
     */
    @Override
    public void setTarget(String newTarget);

    /**
     * Returns the role of the target in this relation.
     */
    @Override
    public String getTargetRoleSingular();

    /**
     * Sets the role of the target in this relation. The role is specified in singular form, e.g.
     * policy and not policies. The distinction is more relevant in other languages than English,
     * where you can't derive the plural from the singular form.
     */
    @Override
    public void setTargetRoleSingular(String newRole);

    /**
     * Returns a default role name (singular form) for the target based on the target's name.
     */
    @Override
    public String getDefaultTargetRoleSingular();

    /**
     * Returns the role of the target in this relation. The role is specified in plural form.
     */
    @Override
    public String getTargetRolePlural();

    /**
     * Sets the new role in plural form of the target in this relation.
     */
    @Override
    public void setTargetRolePlural(String newRole);

    /**
     * Returns if the target role plural is required (or not) based on the relation's max
     * cardinality and the artifact builderset's information if it needs the plural form for to 1
     * relations.
     */
    @Override
    public boolean isTargetRolePluralRequired();

    /**
     * Returns a default role name (plural form) for the target based on the target's name.
     */
    @Override
    public String getDefaultTargetRolePlural();

    /**
     * Returns <code>true</code> if this allowed links for this association are constrained by the
     * product structure. n this case {@link #findMatchingProductCmptTypeAssociation(IIpsProject)}
     * returns the matching association on the product side of the model. See the method's Javadoc
     * for a detailed description.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     */
    public boolean isConstrainedByProductStructure(IIpsProject ipsProject);

    /**
     * Returns the name of the reverse relation.
     */
    public String getInverseAssociation();

    /**
     * Returns <code>true</code> if this relation has a reverse relation otherwise
     * <code>false</code>.
     */
    public boolean hasInverseAssociation();

    /**
     * Sets the name of the inverse relation.
     */
    public void setInverseAssociation(String relation);

    /**
     * Searches the inverse association and returns it, if it exists. Returns <code>null</code> if
     * no inverse association exists.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     * 
     * @throws IpsException if an error occurs while searching.
     */
    public IPolicyCmptTypeAssociation findInverseAssociation(IIpsProject ipsProject) throws IpsException;

    /**
     * Sets whether this association is qualified or not.
     */
    public void setQualified(boolean newValue);

    /**
     * Returns <code>true</code> if it is possible to mark this association as being qualified,
     * otherwise <code>false</code>. It is possible to mark an association as being qualified if the
     * following conditions hold true:
     * <ul>
     * <li>The association is a composition (master-to-detail)</li>
     * <li>The target policy component type exists and is configurable by a product component type.
     * </ul>
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     * 
     * @throws IpsException if an error occurs while searching for the target.
     * 
     * @see #setQualified(boolean)
     */
    public boolean isQualificationPossible(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns <code>true</code> if this association is the inverse of a derived union association,
     * otherwise <code>false</code>.
     * 
     * @throws IpsException if an error occurs while searching for the derived union association.
     */
    public boolean isInverseOfDerivedUnion() throws IpsException;

    /**
     * Returns the (fully qualified) name of the product component type that can qualify this
     * association. Candidate for a qualifier is *the* product component type that configures the
     * target of this association. In contrast to {@link #findQualifier(IIpsProject)} this method
     * returns the name, even when this association is not marked as qualified and/or the product
     * component type isn't found. However it is a finder() method (not a getter) because at least
     * the target policy component type has to be found.
     * <p>
     * Returns am empty String if either this association can't be qualified.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     * 
     * @throws IpsException if an error occurs while searching for the target.
     */
    public String findQualifierCandidate(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the product component type that qualifies this association. Returns <code>null</code>
     * if either the association is not qualified or the qualifier can't be found.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     * 
     * @throws IpsException if an error occurs while searching for the target.
     */
    public IProductCmptType findQualifier(IIpsProject ipsProject) throws IpsException;

    /**
     * Creates a new inverse association. Returns the newly created association.
     * 
     * CoreException if an error occurs while creating the association, e.g. if the target of this
     * association wasn't found.
     */
    public IPolicyCmptTypeAssociation newInverseAssociation() throws IpsException;

    /**
     * Returns the object containing information about how to persist this policy component type
     * association into a relational database table.
     * 
     * @return <code>null</code> if the persistence information is not available, e.g. when the
     *         corresponding IPS project this type belongs to does not support persistence.
     * 
     * @see org.faktorips.devtools.model.ipsproject.IIpsProject#isPersistenceSupportEnabled
     */
    public IPersistentAssociationInfo getPersistenceAssociatonInfo();

    /**
     * Setting this association as shared association. Only valid for detail-to-master association
     * with the same name as another detail-to-master association in the super type which has an
     * inverse association and the optional constraint
     * {@link IIpsProjectProperties#isSharedDetailToMasterAssociations()} is enabled.
     * 
     * 
     * @param sharedAssociation The sharedAssociation to set.
     */
    void setSharedAssociation(boolean sharedAssociation);

    /**
     * Checking whether this association is a shared association or not. When the optional
     * constraint {@link IIpsProjectProperties#isSharedDetailToMasterAssociations()} is enabled, a
     * detail-to-master association could be marked as shared association. That means the
     * {@link #getPolicyCmptType()} does not know exactly its parent model object class. Hence the
     * {@link #getInverseAssociation()} of this association is empty. To get the correct inverse
     * association the name of this association must be the same as the detail-to-master association
     * in a super type.
     * <p>
     * Also read the discussion of FIPS-85.
     * 
     * @return Returns the true if this is an detail-to-master composition that is marked as shared
     *         association and and the optional constraint
     *         {@link IIpsProjectProperties#isSharedDetailToMasterAssociations()} is enabled.
     */
    boolean isSharedAssociation();

    /**
     * This method looks for an association with the same name in the super type hierarchy that is
     * not a shared association by itself. The found association must have the same target as this
     * association. The found association could be the shared association host of this association.
     * However this method does not check if this association is marked as shared association!
     * 
     * @param ipsProject The project used to search from
     * @return an association with the same name and target found in the super type hierarchy
     * @throws IpsException in case of a core exception in the finder methods
     */
    IPolicyCmptTypeAssociation findSharedAssociationHost(IIpsProject ipsProject) throws IpsException;

    /**
     * Searches for a matching association in the product side of the model. The matching
     * association could be specified explicitly by setting
     * {@link #setMatchingAssociationSource(String)} and {@link #setMatchingAssociationName(String)}
     * . If at least one of these fields are empty, this method try to find the matching association
     * automatically by comparing associations targets and order.
     * <p>
     * Example: We have two policy component types called 'Policy' and 'Coverage' with a composition
     * relationship between them. A policy contains several coverages. Policy is constraining by the
     * product component type 'Product' and coverage by 'CoverageType'. There is also an association
     * between product and coverage type. This association is the matching association for the
     * Policy-Coverage composition.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     */
    public IProductCmptTypeAssociation findMatchingProductCmptTypeAssociation(IIpsProject ipsProject);

    /**
     * Finding the default matching product component association. The dafault is used when no
     * explicit matching association is set.
     * 
     * @see #findMatchingProductCmptTypeAssociation(IIpsProject)
     * 
     * @param ipsProject The project used to find the association
     * @return the default matching association
     */
    IProductCmptTypeAssociation findDefaultMatchingProductCmptTypeAssociation(IIpsProject ipsProject)
            throws IpsException;

    /**
     * Setting the source product component of the explicitly specified association that constrains
     * this {@link IPolicyCmptTypeAssociation}. The name of the association is set by
     * {@link #setMatchingAssociationName(String)}. If the matching association should be found
     * automatically you have to set this field to {@link StringUtils#EMPTY}.
     * 
     * @param matchingAssociationSource The qualified name of the source {@link IProductCmptType}
     */
    void setMatchingAssociationSource(String matchingAssociationSource);

    /**
     * Getting the qualified name for the source {@link IProductCmptType} of the
     * {@link IProductCmptTypeAssociation} that constrains this association. The name of the
     * association is returned by {@link #getMatchingAssociationName()}. If the matching association
     * should be found automatically this method returns {@link StringUtils#EMPTY}.
     * 
     * @return The qualified name of the source {@link IProductCmptType} that's association should
     *         constrains this one
     */
    String getMatchingAssociationSource();

    /**
     * Setting the name of the association that constrains this one. The source component of this
     * association is set by {@link #setMatchingAssociationSource(String)}. If the matching
     * association should be found automatically you have to set this field to
     * {@link StringUtils#EMPTY}.
     * 
     * @param matchingAssociationName The name of the association that constrains this one
     */
    void setMatchingAssociationName(String matchingAssociationName);

    /**
     * Getting the name of the {@link IProductCmptTypeAssociation} that constrains this association.
     * The qualified name of the source {@link IProductCmptType} is returned by
     * {@link #getMatchingAssociationSource()}. If the matching association should be found
     * automatically this method returns {@link StringUtils#EMPTY}.
     * 
     * @return The name of the association that constrains this one
     */
    String getMatchingAssociationName();

    /**
     * Setting whether this association is configured by product component or not.
     * 
     * @param configurable True to mark this association to be configured by product component
     */
    void setConfigurable(boolean configurable);

    /**
     * Getting whether this association is configured by product component or not. A
     * {@link IPolicyCmptTypeAssociation} could only be configured if there is a matching
     * {@link IProductCmptTypeAssociation}. Hence if you realy want to know if this association is
     * configured you have to check {@link #isConstrainedByProductStructure(IIpsProject)} also.
     * 
     * @return true if this association is marked as being configured by product component
     */
    boolean isConfigurable();

}
