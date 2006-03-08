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

package org.faktorips.devtools.core.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;


/**
 * 
 */
public interface IProductCmpt extends ITimedIpsObject {
    
    /**
     * The name of the policy component type property
     */
    public final static String PROPERTY_POLICY_CMPT_TYPE = "policyCmptType"; //$NON-NLS-1$
    
    public final static String MSGCODE_PREFIX = "PRODUCT_CMPT-";
    
    /**
     * Validation message code that indicates if an error exists in the type hierarchy of the
     * policy cmpt type that is referenced by this product component.
     */
    //TODO not implemented yet
    public final static String MSGCODE_INCONSISTENCY_IN_POLICY_CMPT_TYPE_HIERARCHY = MSGCODE_PREFIX + "InconsistencyInPolicyCmptTypeHierarchy";
    
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
     * Searches the product component type this product component is based on.
     *  
     * @return The product component type this product component is based on 
     * or <code>null</code> if the product component type can't be found.
     *  
     * @throws CoreException if an exception occurs while searching for the type.
     */
    public IProductCmptType findProductCmptType() throws CoreException;

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
     * Returns <code>true</code> if any of the generations contain at least one formula.
     * Returns <code>false</code> otherwise.
     */
    public boolean containsFormula();
    
    /**
     * Returns the product component structure representing the structure defined by relations. The 
     * structure is rootet at this product.
     */
    public IProductCmptStructure getStructure();
    
    
    
}
