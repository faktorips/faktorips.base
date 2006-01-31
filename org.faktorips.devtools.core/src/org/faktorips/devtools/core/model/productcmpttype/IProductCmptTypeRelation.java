package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;

/**
 * A relation between two product component types.
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptTypeRelation extends IIpsObjectPart {

    /**
     * Returns the product component type this relation belongs to.
     */
    public IProductCmptType getProductCmptType();
    
    /**
     * Returns the corresponding policy component type relation or <code>null</code> if no
     * such relation is found.
     * 
     * @throws CoreException if an error occurs while searching for the relation.
     */
    public IRelation findPolicyCmptTypeRelation() throws CoreException;
    
    /**
     * Returns true if the relation type is abstract, otherwise <code>false</code>.
     * If the relation type is abstract no relation is allowed between product components
     * based on this relation type.
     */
    public boolean isAbstract() throws CoreException;
    
    /**
     * Returns the relation's type indication if it's an association or
     * aggregation. 
     */
    public RelationType getRelationType();
    
    /**
     * Returns the target product component type or <code>null</code> if either this relation hasn't got a target
     * or the target does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */
    public IProductCmptType findTarget() throws CoreException;
    
    /**
     * Sets the qualified name of the target prodict component type.
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
     * Returns <code>true</code> if this is an abstract container relation. 
     * otherwise <code>false</code>.
     */
    public boolean isAbstractContainer();
    
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
    
	
	
	
}
