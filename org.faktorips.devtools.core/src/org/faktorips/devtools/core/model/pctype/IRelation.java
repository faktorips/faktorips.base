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
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;


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
public interface IRelation extends IIpsObjectPart {
    
    // String constants for the relation class' properties according
    // to the Java beans standard.
    public final static String PROPERTY_RELATIONTYPE = "relationType"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET_ROLE_SINGULAR = "targetRoleSingular"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET_ROLE_PLURAL = "targetRolePlural"; //$NON-NLS-1$
    public final static String PROPERTY_MIN_CARDINALITY = "minCardinality"; //$NON-NLS-1$
    public final static String PROPERTY_MAX_CARDINALITY = "maxCardinality"; //$NON-NLS-1$
    public final static String PROPERTY_PRODUCT_RELEVANT = "productRelevant"; //$NON-NLS-1$
    public final static String PROPERTY_CONTAINER_RELATION = "containerRelation"; //$NON-NLS-1$
    public final static String PROPERTY_INVERSE_RELATION = "inverseRelation"; //$NON-NLS-1$
    public final static String PROPERTY_READONLY_CONTAINER = "readOnlyContainer"; //$NON-NLS-1$
    public final static String PROPERTY_INVERSE_RELATION_APPLICABLE = "inverseRelationApplicable"; //$NON-NLS-1$
    public final static String PROPERTY_CONTAINER_RELATION_APPLICABLE = "containerRelationApplicable"; //$NON-NLS-1$
    
    public final static String PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE = "targetRoleSingularProductSide"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE = "targetRolePluralProductSide"; //$NON-NLS-1$
    public final static String PROPERTY_MIN_CARDINALITY_PRODUCTSIDE = "minCardinalityProductSide"; //$NON-NLS-1$
    public final static String PROPERTY_MAX_CARDINALITY_PRODUCTSIDE = "maxCardinalityProductSide"; //$NON-NLS-1$

	public static final int CARDINALITY_ONE = 1;
	public static final int CARDINALITY_MANY = Integer.MAX_VALUE;

    public final static RelationType DEFAULT_RELATION_TYPE = RelationType.ASSOCIATION; 
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "PolicyCmptTypeRelation-"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the target does not exist.
     */
    public final static String MSGCODE_TARGET_DOES_NOT_EXIST = MSGCODE_PREFIX + "TargetDoesNotExists"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the target role singular must be set and it's not.
     */
    public final static String MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET = MSGCODE_PREFIX + "TargetRoleSingularMustBeSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the target role plural must be set if the max cardinality is
     * greater than 1.
     */
    public final static String MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET = MSGCODE_PREFIX + "TargetRolePluralMustBeSet"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that a relation has the same rolename singular and plural
     */
    public final static String MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR = 
        MSGCODE_PREFIX + "TargetRoleSingularEqualsTargetRoleSingular"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the max cardinality must be at least 1 and it's not.
     */
    public final static String MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1 = MSGCODE_PREFIX + "MaxCardinalityMustBeAtLeast1"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that for container relations the max cardinality must be greater than 1,
     * but it's not.
     */
    public final static String MSGCODE_MAX_CARDINALITY_FOR_CONTAINERRELATION_TOO_LOW = MSGCODE_PREFIX + "MaxCardinalityForContainerRelationTooLow"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the max cardinality is less than min, but it must be
     * greater or equal than min.
     */
    public final static String MSGCODE_MAX_IS_LESS_THAN_MIN = MSGCODE_PREFIX + "MaxIsLessThanMin"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an relation implementing a container relation
     * must have the same value for the product relevant property as it's container relation.
     */
    public final static String MSGCODE_IMPLEMENTATION_MUST_HAVE_SAME_PRODUCT_RELEVANT_VALUE = MSGCODE_PREFIX + "MustHaveSameProductRelevantValue"; //$NON-NLS-1$
	
    /**
     * Validation message code to indicate that a reverse composition's max cardinality is not 1.
     */
    public final static String MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION = MSGCODE_PREFIX + "MaxCardinalityMustBe1ForReverseCombosition"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that a reverse composition cant be marked as product relevant.
     */
    public final static String MSGCODE_REVERSE_COMPOSITION_CANT_BE_MARKED_AS_PRODUCT_RELEVANT = MSGCODE_PREFIX + "ReverseCompositionCantBeMarkedAsProductRelevant"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a relation can only be product relevant
     * if the type is configurable by product.
     */
    public final static String MSGCODE_RELATION_CAN_ONLY_BE_PRODUCT_RELEVANT_IF_THE_TYPE_IS = 
    	MSGCODE_PREFIX + "RelationoNLYCanBeProductRelevantIfTheTypeIs"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a relation can only be product relevant
     * if the target type is configurable by product.
     */
    public final static String MSGCODE_RELATION_CAN_ONLY_BE_PRODUCT_RELEVANT_IF_THE_TARGET_TYPE_IS = 
        MSGCODE_PREFIX + "RelationCanOnlyBeProductRelevantIfTheTargetIs"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a relation marked as product relevant
     * is missing the target role singular at product side.
     */
    public final static String MSGCODE_NO_TARGET_ROLE_SINGULAR_PRODUCTSIDE = 
    	MSGCODE_PREFIX + "NoTargetRoleSingularProductSide"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a relation marked as product relevant
     * is missing the target role plural at product side.
     */
    public final static String MSGCODE_NO_TARGET_ROLE_PLURAL_PRODUCTSIDE = 
    	MSGCODE_PREFIX + "NoTargetRolePluralProductSide"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a relation marked as product relevant
     * has the same target role for singular and plural for the product side.
     */
    public final static String MSGCODE_TARGET_ROLE_PLURAL_PRODUCTSIDE_EQUALS_TARGET_ROLE_SINGULAR_PRODUCTSIDE = 
    	MSGCODE_PREFIX + "TargetRoleSingularProductSideEqualsTargetRoleSingularProductSide"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the container relation does not exist in the supertype hirachy.
     */
    public final static String MSGCODE_CONTAINERRELATION_NOT_IN_SUPERTYPE = MSGCODE_PREFIX + "ContainerRelationNotInSupertype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the container relation this relation implements is not marked as such.
     */
    public final static String MSGCODE_NOT_MARKED_AS_CONTAINERRELATION = MSGCODE_PREFIX + "NotMarkedAsContainerRelation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the specified container relation does not exist.
     */
    public final static String MSGCODE_CONTAINERRELATION_TARGET_DOES_NOT_EXIST = MSGCODE_PREFIX + "ContainerRelationTargetDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an implementation relation's the target class is not a a subclass 
     * (or the same class) as the container relations target.
     */
    public final static String MSGCODE_TARGET_CLASS_NOT_A_SUBCLASS = MSGCODE_PREFIX + "TargetClassNotASubclass"; //$NON-NLS-1$

    /**
     * Validation message code to indicate the inverse relation definition is inkonsistent with the container
     * relation.
     */
    public final static String MSGCODE_INVERSE_RELATION_INCONSTENT_WITH_DEFINITION_CONTAINER_RELATION = MSGCODE_PREFIX + "InverseRelationConsistentWithDefinitionContainerRelation"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the relation has same plural rolename like another relation in supertype hirarchy.
     */
    public final static String MSGCODE_SAME_PLURAL_ROLENAME = MSGCODE_PREFIX + "RelationHasSamePluralRolename"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the relation has the same singular rolename like another relation in supertype hirachy.
     */
    public final static String MSGCODE_SAME_SINGULAR_ROLENAME = MSGCODE_PREFIX + "RelationHasSameSingularRolename"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the relation has the same plural rolename product side like another relation in supertype hirachy.
     */
    public final static String MSGCODE_SAME_PLURAL_ROLENAME_PRODUCTSIDE = MSGCODE_PREFIX + "RelationHasSamePluralRolenameProductSide"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the relation has the same singular rolename product side like another relation in supertype hirachy.
     */
    public final static String MSGCODE_SAME_SINGULAR_ROLENAME_PRODUCTSIDE = MSGCODE_PREFIX + "RelationHasSameSingularRolenameProductSide"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an association and it's inverse assoication must be marked as 
     * container relations (or not).
     */
    public final static String MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER = MSGCODE_PREFIX + "ReverseRelationOfContainerRelationMustBeContainerRelationToo"; //$NON-NLS-1$

    /**
     * A reference to an inverse relation isn't needed for composition. 
     */
    public final static String MSGCODE_INVERSE_RELATION_INFO_NOT_NEEDED = MSGCODE_PREFIX + "InverseRelationNotNeeded";
    
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
     * Returns <code>true</code> if this is an assoziation otherwise <code>false</code>.
     */
    public boolean isAssoziation();
    
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
    public boolean isInverseRelationApplicable();
    
    /**
     * Returns <code>true</code> if this relation can be marked as container relation or can implement a
     * container relation. This is the case for associations and master-to-detail composites.
     */
    public boolean isContainerRelationApplicable();
    
    /**
     * Returns the relation's type indication if it's an association or
     * aggregation. 
     */
    public RelationType getRelationType();
    
    /**
     * Sets the relation's type.
     */
    public void setRelationType(RelationType newType);
    
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
     * @deprecated use {@link #findTarget(IIpsProject)}
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
    public IPolicyCmptType findTarget(IIpsProject ipsProject) throws CoreException;
    
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
    public boolean isContrainedByProductStructure(IIpsProject ipsProject) throws CoreException;
    
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
     * Returns <code>true</code> if this is an abstract, read-only container relation. 
     * otherwise false.
     */
    public boolean isReadOnlyContainer();
    
    /**
     * Sets the information if this is an abstract read-only container relation or not.
     */
    public void setReadOnlyContainer(boolean flag);
    
    /**
     * Returns the minmum number of target instances required in this relation.   
     */
    public int getMinCardinality();
    
    /**
     * Sets the minmum number of target instances required in this relation.   
     */
    public void setMinCardinality(int newValue);
    
    /**
     * Returns the maxmium number of target instances allowed in this relation.
     * If the number is not limited, CARDINALITY_MANY is returned. 
     */
    public int getMaxCardinality();
    
    /**
     * Returns true if this is a 1 (or 0) to many relation. This is the case if
     * the max cardinality is greater than 1.
     */
    public boolean is1ToMany();
    
    /**
     * Returns true if this is a 1 (or 0) to 1 relation. This is the case if
     * the max cardinality is 1.
     */
    public boolean is1To1();
    
    /**
     * Sets the maxmium number of target instances allowed in this relation.
     * An unlimited number is represented by CARDINALITY_MANY.
     */
    public void setMaxCardinality(int newValue);
    
    /**
     * Returns true if this relation is can be customized during product definition.
     */
    public boolean isProductRelevant();
    
    /**
     * Sets if this relation can be customized during product definition.
     */
    public void setProductRelevant(boolean newValue);
    
    /**
     * Returns the name of the container relation.
     * <p>
     * Example:
     * <br>
     * A <code>Policy</code> class has a 1-many relation to it's <code>PolicyPart</code>s (PolicyPartRelation).
     * Derived from <code>Policy</code> is a <code>MotorPolicy</code>. Derived from <code>PolicyPart</code>
     * is a <code>MotorCollisionPart</code>. There exists a 1-1 relation between
     * <code>MotorPolicy</code> and <code>MotorCollisionPart</code>.
     * To express that the a motor policy instance returns the collision part
     * when all it's parts (PolicyPartRelation) are requested, the policy part relation
     * has to be defined as container relation of the 0-1 relation between the motor policy
     * and the motor collision part.
     */
    public String getContainerRelation();     

    /**
     * Returns <code>true</code> if this relation is based on a container relation.
     */
    public boolean hasContainerRelation();
    
    /**
     * Sets the container relation. See <code>getContainerRelation()</code> for further
     * details.
     */
    public void setContainerRelation(String containerRelation);
    
    /**
     * Searches the container relation object and returns it, if it exists. Returns <code>null</code> if the container
     * relation does not exists.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IRelation findContainerRelation() throws CoreException;
    
    /**
     * Returns the name of the reverse relation.
     */
    public String getInverseRelation();
    
    /**
     * Returns <code>true</code> if this relation has a reverse relation otherwise <code>false</code>.
     */
    public boolean hasInverseRelation();
    
    /**
     * Sets the name of the inverse relation.
     */
    public void setInverseRelation(String relation);
    
    /**
     * Searches the inverse relation and returns it, if it exists. Returns <code>null</code> if the inverse
     * relation exists. For detail-to-master relations the method always(!) returns <code>null</code>
     * as severall master-to-detail relations can have the same detail-to-master relation.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IRelation findInverseRelation() throws CoreException;
    
    /**
     * Returns <code>true</code> if this relation implements a container relation, otherwise <code>false</code>.
     */
    public boolean isContainerRelationImplementation();

    /**
     * Returns <code>true</code> if this relation implements the given container relation, otherwise <code>false</code>.
     * 
     * @throws CoreException if the given container relation is not marked as such.
     */
    public boolean isContainerRelationImplementation(IRelation containerRelation) throws CoreException;

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
     * Searches for container relations - in the supertype hierarchy of the policy cmpt this
     * relation belongs to - which are canditates of a corresponding container relation for this
     * relation. See <code>getContainerRelation()</code> for further details. 
     * Returns and emty array if no such container relation exists.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IRelation[] findContainerRelationCandidates(IIpsProject ipsProject) throws CoreException;    
}
