package org.faktorips.devtools.core.model.product;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsPackageFragment;


/**
 *
 */
public interface IProductCmptGeneration extends IIpsObjectGeneration {

    /**
     * The Java type that contains the implementation.
     */
    public final static int JAVA_IMPLEMENTATION_TYPE = IIpsPackageFragment.JAVA_PACK_IMPLEMENTATION;
    
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
     * Returns the relations that belong to the given type relation or
     * an empty array if no such relations are found.
     * 
     * @param typeRelation The target role of a type relation.
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
     * Returns the Java type that correspond to this product component generation and is of the indicated kind.
     * If this generation contains no formula config element, the implementation class returned by the 
     * corresponding policy component type's getJavaMethod(IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE) is
     * returned. If the generation contains a formula config element, the method a special class is corresponds to
     * this generation. This special class is a subclass of the class returned for the case that the generation hasn't
     * got any formula element.
     * 
     * @param kind A kind constant identifying the type of compilation unit.
     * @return The corresponding compilation unit. Note that the unit might not
     * exists!
     * @throws IllegalArgumentException if the kind constant is illegal.
     * @throws CoreException if an error occurs while searching the IType. For example if this generation does not
     * contain a formula and the corresponding policy component type can't be found, a CoreException is thrown.   
     */
    public IType getJavaType(int kind) throws CoreException;
    
    /**
     * Returns all Java types that correspond to this product component generation. Note that
     * none of the returned Java types must exist.
     */
    public IType[] getAllJavaTypes() throws CoreException;
    

    
}
