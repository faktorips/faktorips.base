package org.faktorips.devtools.core.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObjectPart;


/**
 * 
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
    public final static String PROPERTY_REVERSE_RELATION = "reverseRelation"; //$NON-NLS-1$
    public final static String PROPERTY_READONLY_CONTAINER = "readOnlyContainer"; //$NON-NLS-1$

    public final static String PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE = "targetRoleSingularProductSide"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE = "targetRolePluralProductSide"; //$NON-NLS-1$
    public final static String PROPERTY_MIN_CARDINALITY_PRODUCTSIDE = "minCardinalityProductSide"; //$NON-NLS-1$
    public final static String PROPERTY_MAX_CARDINALITY_PRODUCTSIDE = "maxCardinalityProductSide"; //$NON-NLS-1$
    

	public static final int CARDINALITY_ONE = 1;
	public static final int CARDINALITY_MANY = Integer.MAX_VALUE;

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "PolicyCmptTypeRelation-"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that an relation implementing a container relation
     * must have the same value for the product relevant property as it's container relation.
     */
    public final static String MSGCODE_IMPLEMENTATION_MUST_HAVE_SAME_PRODUCT_RELEVANT_VALUE = MSGCODE_PREFIX + "MustHaveSameProductRelevantValue"; //$NON-NLS-1$
    // TODO implement, test case
	
    /**
     * Validation message code to indicate that a reverse composition has a max cardinality greater
     * than 1 (it should be 1).
     */
    public final static String MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION = MSGCODE_PREFIX + "MustHaveSameProductRelevantValue"; //$NON-NLS-1$
    // TODO implement, test case
    
    /**
     * Returns the policy component type this relation belongs to.
     */
    public IPolicyCmptType getPolicyCmptType();
    
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
     */
    public IPolicyCmptType findTarget() throws CoreException;
    
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
     * Returns the role of the target in this relation. The role is specified in plural form.
     */
    public String getTargetRolePlural();
    
    /**
     * Sets the new role in plural form of the target in this relation.
     */
    public void setTargetRolePlural(String newRole);
    
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
     * Returns true if this is a 1 to many relation. This is the case if
     * the max cardinality is greater than 1.
     */
    public boolean is1ToMany();
    
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
     * Returns the qualified name of the container relation.
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
     * Returns true if this relation bases on a container relation.
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
    public String getReverseRelation();
    
    /**
     * Sets the name of the reverse relation.
     */
    public void setReverseRelation(String relation);
    
    /**
     * Searches the reverse relation and returns it, if it exists. Returns <code>null</code> if the reverse
     * relation does not exists.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IRelation findReverseRelation() throws CoreException;
    
    /**
     * Returns <code>true</code> if this relation implements a container relation, otherwise <code>false</code>.
     */
    public boolean implementsContainerRelation() throws CoreException;

    /**
     * Searches the reverse relation and returns its container relation, if it exists and is of type reverse composition. 
     * Returns <code>null</code> otherwise.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IRelation findContainerRelationOfTypeReverseComposition() throws CoreException;
    
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
    
    
}
