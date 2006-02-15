package org.faktorips.devtools.core.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;


/**
 *
 */
public interface IProductCmptGeneration extends IIpsObjectGeneration {

    /**
     * Returns the product component this generation belongs to.
     */
    public IProductCmpt getProductCmpt();
    
    /**
     * Returns the delta between this product component and it's policy
     * component type.
     * 
     * @throws CoreException
     */
    public IProductCmptGenerationPolicyCmptTypeDelta computeDeltaToPolicyCmptType() throws CoreException;
    
    /**
     * Fixes all differences that are described in the delta.   
     */
    public void fixDifferences(IProductCmptGenerationPolicyCmptTypeDelta delta) throws CoreException;
    
    /**
     * Returns the configuration elements.
     */
    public IConfigElement[] getConfigElements();
    
    /**
     * Returns the configuration elements that have the indicated type.
     */
    public IConfigElement[] getConfigElements(ConfigElementType type);
    
    /**
     * Returns the config element that correspongs to the attribute with the
     * given name. Returns <code>null</code> if no suich element exists.
     */
    public IConfigElement getConfigElement(String attributeName);
    
    /**
     * Creates a new configuration element.
     */
    public IConfigElement newConfigElement();
    
    /**
     * Returns the number of configuration elements.
     */
    public int getNumOfConfigElements();
    
    /**
     * Returns the product component's relations to other product components.
     */
    public IProductCmptRelation[] getRelations();
    
    /**
     * Returns the relations that belong to the given policy component type relation or
     * an empty array if no such relations are found.
     * 
     * @param typeRelation The target role of a policy component type relation.
     * @throws IllegalArgumentException if type relation is null. 
     */
    public IProductCmptRelation[] getRelations(String typeRelation);

    /**
     * Returns the number of relations.
     */
    public int getNumOfRelations();
    
    /**
     * Creates a new relation.
     */
    public IProductCmptRelation newRelation(String relation);
    
    /**
     * Creates a new relation. The relation is placed before the given one.
     */
    public IProductCmptRelation newRelation(String relation, IProductCmptRelation insertBefore);
    
    /**
     * Moves the first given relation in front of the second one.
     */
    public void moveRelation(IProductCmptRelation toMove, IProductCmptRelation moveBefore);

}
