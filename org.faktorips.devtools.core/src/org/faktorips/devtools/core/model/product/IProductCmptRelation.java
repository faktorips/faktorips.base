package org.faktorips.devtools.core.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;


/**
 *
 */
public interface IProductCmptRelation extends IIpsObjectPart {
    
    public final static String PROPERTY_TARGET = "target";
    public final static String PROPERTY_PCTYPE_RELATION = "pcTypeRelation";
    public final static String PROPERTY_MIN_CARDINALITY = "minCardinality";
    public final static String PROPERTY_MAX_CARDINALITY = "maxCardinality";
    
    /**
     * Returns the product component generation this config element belongs to.
     */
    public IProductCmpt getProductCmpt();
    
    /**
     * Returns the product component generation this config element belongs to.
     */
    public IProductCmptGeneration getProductCmptGeneration();
    
    /**
     * Returns the name of the product component type relation this
     * relation is based on.
     */
    public String getProductCmptTypeRelation();
    
    /**
     * Returns the target product component.
     */
    public String getTarget();
    
    /**
     * Sets the target product component.
     */
    public void setTarget(String newTarget);

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
     * Sets the maxmium number of target instances allowed in this relation.
     * An unlimited number is represented by an asterix ('*'). 
     */
    public void setMaxCardinality(String newValue);
    
    /**
     * Finds the corresponding relation in the product component type this
     * product component is based on. Note the method searches not only the direct
     * product component type this product component is based on, but also it's 
     * super type hierarchy hierarchy.
     * 
     * @return the corresponding relation or <code>null</code> if no such
     * relation exists.
     * 
     * @throws CoreException if an exception occurs while searching the relation. 
     */
    public IProductCmptTypeRelation findProductCmptTypeRelation() throws CoreException;
    
}
