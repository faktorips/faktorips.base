package org.faktorips.devtools.core.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;


/**
 * 
 */
public interface IProductCmpt extends ITimedIpsObject {
    
    /**
     * The name of the policy component type property
     */
    public final static String PROPERTY_POLICY_CMPT_TYPE = "policyCmptType";
    
    /**
     * Returns the qualified name of the policy component type this product component
     * is based on.
     */
    public String getPolicyCmptType();
    
    /**
     * Sets the qualified name of the policy component type this product component
     * is based on.
     */
    public void setPolicyCmptType(String newPcType);
    
    /**
     * Searches the policy component type this product component is based on.
     *  
     * @return The policy component type this product component is based on 
     * or <code>null</code> if the policy component type can't be found.
     *  
     * @throws CoreException if an exception occurs while searching for the type.
     */
    public IPolicyCmptType findPolicyCmptType() throws CoreException;
    
    /**
     * Searches the relation with the given name in the policy component type 
     * this product component is based on.
     *  
     * @return The relation with the given name in the policy component type this 
     * product component is based on or <code>null</code> if either the policy component type 
     * or a relation with the given name can't be found. 
     *  
     * @throws CoreException if an exception occurs while searching for the type.
     */
    public IRelation findPcTypeRelation(String relationName) throws CoreException;
    
    /**
     * Returns true if a Java type must be generated for this product component. This is
     * the case if the generations contain at least one formula (that has to be compiled to
     * Java sourcecode). Returns false, if no Java sourcecode has to be generated for this
     * product component.
     * @deprecated
     */
    public boolean javaTypeMustBeGenerated();
    

    
    
    
}
