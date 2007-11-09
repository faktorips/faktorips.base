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

package org.faktorips.devtools.core.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;


/**
 * Represents a directed(!) relation between two types. Bidirectional relations are represented in faktorips 
 * as two corresponding directed relations. Given a relation the other corresponding relation is called
 * the inverse relation.
 * <p>
 * In conceptual models bidirectional relations can be either assoziations or compositions. A bidirectional assoziation
 * is represented in FaktorIPS by two relations of type assoziation. A bidirectional composition is represented in 
 * FaktorIPS by one master to detail composition and one detail to master composition.
 *
 * @author Jan Ortmann
 */
public interface IPolicyCmptTypeAssociation extends IAssociation {

    /**
     * The list of applicable types. For policy component types, aggregations are not supported.
     */
    public final static AssociationType[] APPLICABLE_ASSOCIATION_TYPES = new AssociationType[] { 
        AssociationType.COMPOSITION_MASTER_TO_DETAIL, AssociationType.COMPOSITION_DETAIL_TO_MASTER, AssociationType.ASSOCIATION}; 
    
    public final static String PROPERTY_PRODUCT_RELEVANT = "productRelevant"; //$NON-NLS-1$
    public final static String PROPERTY_INVERSE_ASSOCIATION = "inverseAssociation"; //$NON-NLS-1$
    public final static String PROPERTY_INVERSE_ASSOCIATION_APPLICABLE = "inverseAssociationApplicable"; //$NON-NLS-1$
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
    public final static String MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION = MSGCODE_PREFIX + "MaxCardinalityMustBe1ForReverseCombosition"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate the inverse relation definition is inkonsistent with the container
     * relation.
     */
    public final static String MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION = MSGCODE_PREFIX + "InverseAssociationInconsistentWithDerivedUnion"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the relation has same plural rolename like another relation in supertype hirarchy.
     */
    public final static String MSGCODE_SAME_PLURAL_ROLENAME = MSGCODE_PREFIX + "RelationHasSamePluralRolename"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the relation has the same singular rolename like another relation in supertype hirachy.
     */
    public final static String MSGCODE_SAME_SINGULAR_ROLENAME = MSGCODE_PREFIX + "RelationHasSameSingularRolename"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an association and it's inverse assoication must be marked as 
     * container relations (or not).
     */
    public final static String MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER = MSGCODE_PREFIX + "ReverseRelationOfContainerRelationMustBeContainerRelationToo"; //$NON-NLS-1$

    /**
     * A reference to an inverse relation isn't needed for composition. 
     */
    public final static String MSGCODE_INVERSE_RELATION_INFO_NOT_NEEDED = MSGCODE_PREFIX + "InverseRelationNotNeeded"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that given a relation with an inreverse relation,
     * this inverse relation can be found but it does not specify the first relation as it's
     * inverse relation. 
     * This applies to associations only, as detail-to-master composition don't specify a 
     * reverse relation.
     */
    public final static String MSGCODE_INVERSE_RELATION_MISMATCH = MSGCODE_PREFIX + "InverseRelationMismatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the reverse relation does not specify this relation as its reverse one.
     */
    public final static String MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET = MSGCODE_PREFIX + "ReverseRelationNotInTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the reverse relation of an association must be an assoziation.
     */
    public final static String MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH = MSGCODE_PREFIX + "InverseAssociationTypeMissmatch"; //$NON-NLS-1$

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
     * Returns <code>true</code> if the definition of an inverse relation makes sense for the
     * this relation. This is the case for
     * <ul>
     * <li>biderectional associations and</li>
     * <li>master-to-detail compositions if the artefact builder set needs this information</li>
     * </ul>
     * Returns <code>false</code> if the definition of an inverse relation is superfluous. 
     * 
     * @see IIpsArtefactBuilderSet#isInverseRelationLinkRequiredFor2WayCompositions()
     */
    public boolean isInverseAssociationApplicable();
    
    /**
     * Returns <code>true</code> if this relation can be marked as container relation or can implement a
     * container relation. This is the case for associations and master-to-detail composites.
     */
    public boolean isContainerRelationApplicable();
    
    /**
     * Returns the qualified name of the target policy component class.
     */
    public String getTarget();
    
    /**
     * Returns the target policy component type or <code>null</code> if either this relation hasn't got a target
     * or the target does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     * 
     * @deprecated use {@link #findTargetPolicyCmptType(IIpsProject)}
     */
    public IPolicyCmptType findTarget() throws CoreException;
    
    /**
     * Returns the target policy component type or <code>null</code> if either this relation hasn't got a target
     * or the target does not exists.
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
     * Sets the role of the target in this relation. The role is specified in singular form, e.g. policy and not
     * policies. The distinction is more relevant in other languages than English, where you can't derive the 
     * plural from the singular form.
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
     * Returns if the target role plural is required (or not) based on the relation's max cardinality
     * and the aretfact builderset's information if it needs the plural form for to 1 relations.
     */
    public boolean isTargetRolePluralRequired();
    
    /**
     * Returns a default role name (plural form) for the target based on the target's name.
     */
    public String getDefaultTargetRolePlural();
    
    /**
     * Returns <code>true</code> if this allowed links for this association are contrained
     * by the product structure. n this case {@link #findMatchingProductCmptTypeAssociation(IIpsProject)} returns
     * the matching association on the product side of the model. See the method's Javadoc for a detailed description.
     * 
     * @param ipsProject The ips project which ips object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching for the target.     
     */
    public boolean isConstrainedByProductStructure(IIpsProject ipsProject) throws CoreException;
    
    /**
     * Searches for a matching association in the product side of the model.
     * <p>
     * Example:
     * We have two policy component types called 'Policy' and 'Coverage' with a composition relationship
     * between them. A policy contains severall coverages. Policy ist configured by the product component type
     * 'Product' and coverage by 'CoverageType'. There is also an association between product and coverage type.
     * This association is the matching association for the Policy-Coverage composition. 
     * 
     * @param ipsProject The ips project which ips object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching for the target.     
     */
    public IProductCmptTypeAssociation findMatchingProductCmptTypeAssociation(IIpsProject ipsProject) throws CoreException;
    
    /**
     * Returns true if this relation is can be customized during product definition.
     */
    public boolean isProductRelevant();
    
    /**
     * Sets if this relation can be customized during product definition.
     */
    public void setProductRelevant(boolean newValue);
    
    /**
     * Returns the name of the reverse relation.
     */
    public String getInverseAssociation();
    
    /**
     * Returns <code>true</code> if this relation has a reverse relation otherwise <code>false</code>.
     */
    public boolean hasInverseAssociation();
    
    /**
     * Sets the name of the inverse relation.
     */
    public void setInverseAssociation(String relation);
    
    /**
     * Searches the inverse relation and returns it, if it exists. Returns <code>null</code> if the inverse
     * relation exists. For detail-to-master relations the method always(!) returns <code>null</code>
     * as severall master-to-detail relations can have the same detail-to-master relation.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IPolicyCmptTypeAssociation findInverseAssociation() throws CoreException;

    /**
     * Returns the role of the target in this relation on the product side.
     */
    public String getTargetRoleSingularProductSide();
    
    /**
     * Sets the role of the target in this relation on the product side. The role is specified in singular form, 
     * e.g. policy and not policies. The distinction is more relevant in other languages than English, where you can't derive the 
     * plural from the singular form.
     */
    public void setTargetRoleSingularProductSide(String newRole);
    
    /**
     * Returns the role of the target in this relation on the product side. The role is specified in plural form.
     */
    public String getTargetRolePluralProductSide();
    
    /**
     * Returns if the target role plural for the productsie is required (or not) based on the relation's max cardinality
     * and the aretfact builderset's information if it needs the plural form for to 1 relations.
     */
    public boolean isTargetRolePluralRequiredProductSide();
    
    /**
     * Returns a default role name plural form for the productside.
     */
    public String getDefaultTargetRolePluralProductSide();

    /**
     * Sets the new role in plural form of the target in this relation on the product sided.
     */
    public void setTargetRolePluralProductSide(String newRole);
    
    /**
     * Returns the minmum number of product components required in this relation
     * on the product side.
     * <p>
     * Note that the minimum cardinality on the product side needn't be the same as the
     * one on the policy side. If the min cardinality on the policy side is 0, the
     * min cardinality on the product side can be 1. It might be optional to include
     * a coverage in a policy, but if it should be possible for all products than
     * the minimum cardinality on the product side is 1.
     */
    public int getMinCardinalityProductSide();
    
    /**
     * Sets the minmum number of product components in this relation.   
     */
    public void setMinCardinalityProductSide(int newValue);
    
    /**
     * Returns the maxmium number of product components allowed in this relation.
     * If the number is not limited CARDINALITY_MANY is returned. 
     * <p>
     * Note that the maximum cardinality on the product side needn't be the same as the
     * one on the policy side. If the max cardinality on the policy side is 1, the
     * max cardinality on the product side can be *. In this case the meaning is a kind 
     * of multi-choice. One of the product components can be used when creating a policy (but only one).
     * <p>
     * Example:<p>
     * A home policy has exactly one glas coverage. However on the product side a basic and a premium
     * glas coverage type is defined. A concrete home police can either contain a coverage based on the
     * basic or the premium coverage type. 
     * <p>
     * Also the opposite is possible. On the policy side multiple coverages of the same type might by
     * included but they must all be based on the same coverage type. In this case the max
     * cardinality on the policy side is * but 1 on the product side.
     */
    public int getMaxCardinalityProductSide();
    
    /**
     * Sets the maxmium number of target instances allowed in this relation.
     * An unlimited number is represented by CARDINALITY_MANY. 
     */
    public void setMaxCardinalityProductSide(int newValue);
    
    /**
     * Sets whether this association is qualified or not.
     */
    public void setQualified(boolean newValue);
    
    /**
     * Returns <code>true</code> if it is possible to mark this association as beeing qualified, otherwise <code>false</code>.
     * It is possible to mark an association as beeing qualified if the following conditions hold true:
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
     * Returns the (fully qualified) name of the product component type that can qualify this association.
     * Candidate for a qualifier is *the* product component type that configures the target of this association. 
     * In contrast to {@link #findQualifier(IIpsProject)} this method returns the name, even when this association 
     * is not marked as qualified and/or the product component type isn't found. However it is a finder() method (not a getter)
     * because at least the target policy component type has to be found. 
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
     * Returns the corresponding association type depending on the own association type:<br>
     * <ul>
     * <li>iASSOZIATION => out: ASSOZIATION
     * <li>COMPOSITION_MASTER_TO_DETAIL => out: COMPOSITION_DETAIL_TO_MASTER
     * <li>COMPOSITION_DETAIL_TO_MASTER => out: COMPOSITION_MASTER_TO_DETAIL
     * </ul>
     * Returns <code>null</code> if the association type is <code>null</code> or no corresponding type exists.
     */
    public AssociationType getCorrespondingAssociationType();
    
    /**
     * Creates a new inverse association. Returns the newly created association.
     * 
     * CoreException if an error occurs while creating the association, e.g. if the target 
     * of this association wasn't found.
     */
    public IPolicyCmptTypeAssociation newInverseAssociation() throws CoreException;
}
