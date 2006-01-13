package org.faktorips.devtools.core.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.faktorips.devtools.core.model.IIpsObjectPart;


/**
 * 
 */
public interface IRelation extends IIpsObjectPart {
    
    // String constants for the relation class' properties according
    // to the Java beans standard.
    public final static String PROPERTY_RELATIONTYPE = "relationType";
    public final static String PROPERTY_TARGET = "target";
    public final static String PROPERTY_TARGET_ROLE_SINGULAR = "targetRoleSingular";
    public final static String PROPERTY_TARGET_ROLE_PLURAL = "targetRolePlural";
    public final static String PROPERTY_MIN_CARDINALITY = "minCardinality";
    public final static String PROPERTY_MAX_CARDINALITY = "maxCardinality";
    public final static String PROPERTY_PRODUCT_RELEVANT = "productRelevant";
    public final static String PROPERTY_CONTAINER_RELATION = "containerRelation";
    public final static String PROPERTY_REVERSE_RELATION = "reverseRelation";
    public final static String PROPERTY_READONLY_CONTAINER = "readOnlyContainer";
    
    // type constant for the Java field in the java policy component type impl and the product component type impl.
    public final static int JAVA_PCTYPE_FIELD = 0;
    public final static int JAVA_PRODUCTCMPT_FIELD = 1;
    
    // type constants for corresponding Java methods
    // 1. max cardinality is 1
    
    //Alle Konstanten mit dem Prefix JAVA_ sind deprecated!!!!!!!!!!!!!1
    public final static int JAVA_GETTER_METHOD = 1;
    public final static int JAVA_PCTYPE_GETTER_METHOD_IMPLEMENATION = IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE << 8 | JAVA_GETTER_METHOD;
    public final static int JAVA_PCTYPE_GETTER_METHOD_INTERFACE = IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE << 8| JAVA_GETTER_METHOD;
    public final static int JAVA_PRODUCTCMPT_GETTER_METHOD_IMPLEMENTATION = IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE << 8| JAVA_GETTER_METHOD;
    public final static int JAVA_PRODUCTCMPT_GETTER_METHOD_INTERFACE = IPolicyCmptType.JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE << 8| JAVA_GETTER_METHOD;

    public final static int JAVA_SETTER_METHOD = 2;
    public final static int JAVA_PCTYPE_SETTER_METHOD_IMPLEMENATION = IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE << 8 | JAVA_SETTER_METHOD;
    public final static int JAVA_PCTYPE_SETTER_METHOD_INTERFACE = IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE << 8 | JAVA_SETTER_METHOD;
    
    // 2. max cardinality is greater than 1
    public final static int JAVA_ADD_METHOD = 3;
    public final static int JAVA_PCTYPE_ADD_METHOD_IMPLEMENATION = IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE << 8 | JAVA_ADD_METHOD;
    public final static int JAVA_PCTYPE_ADD_METHOD_INTERFACE = IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE << 8 | JAVA_ADD_METHOD;

    public final static int JAVA_REMOVE_METHOD = 4;
    public final static int JAVA_PCTYPE_REMOVE_METHOD_IMPLEMENATION = IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE << 8 | JAVA_REMOVE_METHOD;
    public final static int JAVA_PCTYPE_REMOVE_METHOD_INTERFACE = IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE << 8 | JAVA_REMOVE_METHOD;
    
    public final static int JAVA_GETALL_METHOD = 5;
    public final static int JAVA_PCTYPE_GETALL_METHOD_IMPLEMENATION = IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE << 8 | JAVA_GETALL_METHOD;
    public final static int JAVA_PCTYPE_GETALL_METHOD_INTERFACE = IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE << 8 | JAVA_GETALL_METHOD;    
    public final static int JAVA_PRODUCTCMPT_GETALL_METHOD_IMPLEMENATION = IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE << 8 | JAVA_GETALL_METHOD;
    public final static int JAVA_PRODUCTCMPT_GETALL_METHOD_INTERFACE = IPolicyCmptType.JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE << 8 | JAVA_GETALL_METHOD;    
    
    public final static int JAVA_NUMOF_METHOD = 6;
    public final static int JAVA_PCTYPE_NUMOF_METHOD_IMPLEMENATION = IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE << 8 | JAVA_NUMOF_METHOD;
    public final static int JAVA_PCTYPE_NUMOF_METHOD_INTERFACE = IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE << 8 | JAVA_NUMOF_METHOD;
    public final static int JAVA_PRODUCTCMPT_NUMOF_METHOD_IMPLEMENATION = IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE << 8 | JAVA_NUMOF_METHOD;
    public final static int JAVA_PRODUCTCMPT_NUMOF_METHOD_INTERFACE = IPolicyCmptType.JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE << 8 | JAVA_NUMOF_METHOD;
    
    
    
    
	public static final String CARDINALITY_ONE = "1";
	public static final String CARDINALITY_MANY = "*";
    
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
     * If the number is not limited an asterix ('*') is returned. 
     */
    public String getMaxCardinality();
    
    /**
     * Returns true if this is a 1 to many relation. This is the case if
     * the max cardinality is greater than 1.
     */
    public boolean is1ToMany();
    
    /**
     * Sets the maxmium number of target instances allowed in this relation.
     * An unlimited number is represented by an asterix ('*'). 
     */
    public void setMaxCardinality(String newValue);
    
    /**
     * Returns true if this relation is can be customized during product definition.
     */
    public boolean isProductRelevant();
    
    /**
     * Sets if this relation can be customized during product definition.
     */
    public void setProductRelevant(boolean newValue);
    
    /**
     * Returns the qualified name of the read-only container relation.
     * <p>
     * Example:
     * <br>
     * A <code>Policy</code> class has a 1-many relation to it's <code>PolicyPart</code>s (PolicyPartRelation).
     * Derived from <code>Policy</code> is a <code>MotorPolicy</code>. Derived from <code>PolicyPart</code>
     * is a <code>MotorCollisionPart</code>. There exists a 1-1 relation between
     * <code>MotorPolicy</code> and <code>MotorCollisionPart</code>.
     * To express that the a motor policy instance returns the collision part
     * when all it's parts (PolicyPartRelation) are requested, the policy part relation
     * has to be defined as superrelation of the 0-1 relation between the motor policy
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
     * Returns the Java method that corresponds to the relation and is of
     * the indicated type.
     * 
     * @param type A type constant identifying the type of method.
     * @return The corresponding Java method. Note that the method might not
     * exists!
     * @deprecated
     * @throws IllegalArgumentException if the type constant is illegal.   
     */
    public IMethod getJavaMethod(int type) throws CoreException;

    /**
     * Returns the Java field that corresponds to the relation and is of
     * the indicated type.
     * 
     * @param type A type constant identifying the type of field.
     * @return The corresponding Java field. Note that the field might not
     * exists!
     * @deprecated
     * @throws IllegalArgumentException if the type constant is illegal.
     */
    public IField getJavaField(int type) throws CoreException;

    /**
     * Searches the reverse relation and returns its container relation, if it exists and is of type reverse composition. 
     * Returns <code>null</code> otherwise.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IRelation findContainerRelationOfTypeReverseComposition() throws CoreException;

    
}
