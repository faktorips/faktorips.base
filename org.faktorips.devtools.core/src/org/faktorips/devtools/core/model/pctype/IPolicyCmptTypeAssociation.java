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

package org.faktorips.devtools.core.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;

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
    public final static AssociationType[] APPLICABLE_ASSOCIATION_TYPES = new AssociationType[] {
            AssociationType.COMPOSITION_MASTER_TO_DETAIL, AssociationType.COMPOSITION_DETAIL_TO_MASTER,
            AssociationType.ASSOCIATION };

    public final static String PROPERTY_PRODUCT_RELEVANT = "productRelevant"; //$NON-NLS-1$
    public final static String PROPERTY_INVERSE_ASSOCIATION = "inverseAssociation"; //$NON-NLS-1$
    public final static String PROPERTY_SUBSETTING_DERIVED_UNION_APPLICABLE = "containerRelationApplicable"; //$NON-NLS-1$

    public final static String PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE = "targetRoleSingularProductSide"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE = "targetRolePluralProductSide"; //$NON-NLS-1$
    public final static String PROPERTY_MIN_CARDINALITY_PRODUCTSIDE = "minCardinalityProductSide"; //$NON-NLS-1$
    public final static String PROPERTY_MAX_CARDINALITY_PRODUCTSIDE = "maxCardinalityProductSide"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "PolicyCmptTypeRelation-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a reverse composition's max cardinality is not 1.
     */
    public final static String MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION = MSGCODE_PREFIX
            + "MaxCardinalityMustBe1ForReverseCombosition"; //$NON-NLS-1$

    /**
     * Validation message code to indicate the inverse relation definition is inconsistent with the
     * container relation.
     */
    public final static String MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION = MSGCODE_PREFIX
            + "InverseAssociationInconsistentWithDerivedUnion"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an association and it's inverse association must be
     * marked as container relations (or not).
     */
    public final static String MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER = MSGCODE_PREFIX
            + "ReverseRelationOfContainerRelationMustBeContainerRelationToo"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that given a relation with an inverse relation, this
     * inverse relation can be found but it does not specify the first relation as it's inverse
     * relation. This applies to associations only, as detail-to-master composition don't specify a
     * reverse relation.
     */
    public final static String MSGCODE_INVERSE_RELATION_MISMATCH = MSGCODE_PREFIX + "InverseRelationMismatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the reverse relation does not specify this relation
     * as its reverse one.
     */
    public final static String MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET = MSGCODE_PREFIX
            + "ReverseRelationNotInTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the reverse relation of an association must be an
     * association.
     */
    public final static String MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH = MSGCODE_PREFIX
            + "InverseAssociationTypeMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the inverse master to detail composition must be a
     * detail to master composition.
     */
    public final static String MSGCODE_INVERSE_MASTER_TO_DETAIL_TYPE_MISSMATCH = MSGCODE_PREFIX
            + "InverseMasterToDetailTypeMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the inverse detail to mater composition must be a
     * master to detail composition.
     */
    public final static String MSGCODE_INVERSE_DETAIL_TO_MASTER_TYPE_MISSMATCH = MSGCODE_PREFIX
            + "InverseDetailToMasterTypeMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that in case of detail to master associations the inverse
     * is always given.
     */
    public final static String MSGCODE_INVERSE_ASSOCIATION_MUST_BE_SET_IF_TYPE_IS_DETAIL_TO_MASTER = MSGCODE_PREFIX
            + "InverseAssociationMustBeSetIfTypeIsDetailToMaster"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that if the inverse of a derived union exists then the
     * inverse of all subsetted derived union must be specified
     */
    public final static String MSGCODE_SUBSETTED_DERIVED_UNION_INVERSE_MUST_BE_EXISTS_IF_INVERSE_DERIVED_UNION_EXISTS = MSGCODE_PREFIX
            + "SubsettedDerivedUnionInverseMustBeExistsIfInverseDerivedUnionExists"; //$NON-NLS-1$

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
     * Returns <code>true</code> if this relation can be marked as container relation or can
     * implement a container relation. This is the case for associations and master-to-detail
     * composites.
     */
    public boolean isContainerRelationApplicable();

    /**
     * Returns the qualified name of the target policy component class.
     */
    public String getTarget();

    /**
     * Returns the target policy component type or <code>null</code> if either this relation hasn't
     * got a target or the target does not exists.
     * 
     * @param ipsProject The ips project which ips object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */
    public IPolicyCmptType findTargetPolicyCmptType(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the qualified name of the target policy component class.
     */
    public void setTarget(String newTarget);

    /**
     * Returns the role of the target in this relation.
     */
    public String getTargetRoleSingular();

    /**
     * Sets the role of the target in this relation. The role is specified in singular form, e.g.
     * policy and not policies. The distinction is more relevant in other languages than English,
     * where you can't derive the plural from the singular form.
     */
    public void setTargetRoleSingular(String newRole);

    /**
     * Returns a default role name (singular form) for the target based on the target's name.
     */
    public String getDefaultTargetRoleSingular();

    /**
     * Returns the role of the target in this relation. The role is specified in plural form.
     */
    public String getTargetRolePlural();

    /**
     * Sets the new role in plural form of the target in this relation.
     */
    public void setTargetRolePlural(String newRole);

    /**
     * Returns if the target role plural is required (or not) based on the relation's max
     * cardinality and the artefact builderset's information if it needs the plural form for to 1
     * relations.
     */
    public boolean isTargetRolePluralRequired();

    /**
     * Returns a default role name (plural form) for the target based on the target's name.
     */
    public String getDefaultTargetRolePlural();

    /**
     * Returns <code>true</code> if this allowed links for this association are constrained by the
     * product structure. n this case {@link #findMatchingProductCmptTypeAssociation(IIpsProject)}
     * returns the matching association on the product side of the model. See the method's Javadoc
     * for a detailed description.
     * 
     * @param ipsProject The ips project which ips object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */
    public boolean isConstrainedByProductStructure(IIpsProject ipsProject) throws CoreException;

    /**
     * Searches for a matching association in the product side of the model.
     * <p>
     * Example: We have two policy component types called 'Policy' and 'Coverage' with a composition
     * relationship between them. A policy contains several coverages. Policy is configured by the
     * product component type 'Product' and coverage by 'CoverageType'. There is also an association
     * between product and coverage type. This association is the matching association for the
     * Policy-Coverage composition.
     * 
     * @param ipsProject The ips project which ips object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */
    public IProductCmptTypeAssociation findMatchingProductCmptTypeAssociation(IIpsProject ipsProject)
            throws CoreException;

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
     * Searches the inverse relation and returns it, if it exists. Returns <code>null</code> if the
     * inverse relation exists. For detail-to-master relations the method always(!) returns
     * <code>null</code> as several master-to-detail relations can have the same detail-to-master
     * relation.
     * 
     * @param ipsProject The ips project which ips object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IPolicyCmptTypeAssociation findInverseAssociation(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets whether this association is qualified or not.
     */
    public void setQualified(boolean newValue);

    /**
     * Returns <code>true</code> if it is possible to mark this association as being qualified,
     * otherwise <code>false</code>. It is possible to mark an association as being qualified if the
     * following conditions hold true:
     * <p>
     * <ul>
     * <li>The association is a composition (master-to-detail)</li>
     * <li>Te target policy component type exists and is configurable by a product component type.
     * </ul>
     * 
     * @param ipsProject The ips project which ips object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     * 
     * @see #setQualified(boolean)
     */
    public boolean isQualificationPossible(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns <code>true</code> if this association is the inverse of a derived union association,
     * otherwise <code>false</code>.
     * 
     * @throws CoreException if an error occurs while searching for the derived union association.
     */
    public boolean isInverseOfDerivedUnion() throws CoreException;

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
     * @param ipsProject The ips project which ips object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */
    public String findQualifierCandidate(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the product component type that qualifies this association. Returns <code>null</code>
     * if either the association is not qualified or the qualifier can't be found.
     * 
     * @param ipsProject The ips project which ips object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */
    public IProductCmptType findQualifier(IIpsProject ipsProject) throws CoreException;

    /**
     * Creates a new inverse association. Returns the newly created association.
     * 
     * CoreException if an error occurs while creating the association, e.g. if the target of this
     * association wasn't found.
     */
    public IPolicyCmptTypeAssociation newInverseAssociation() throws CoreException;

}
