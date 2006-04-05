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
import org.faktorips.devtools.core.model.CycleException;
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
    
    public final static String PROPERTY_RUNTIME_ID = "runtimeId"; //$NON-NLS-1$
    
    public final static String MSGCODE_PREFIX = "PRODUCT_CMPT-"; //$NON-NLS-1$
    
    /**
     * Validation message code that indicates if an error exists in the type hierarchy of the
     * policy cmpt type that is referenced by this product component.  
     */
    public final static String MSGCODE_INCONSISTENCY_IN_POLICY_CMPT_TYPE_HIERARCHY = MSGCODE_PREFIX + "InconsistencyInPolicyCmptTypeHierarchy"; //$NON-NLS-1$
    
    /**
     * Returns the product component's kind or <code>null</code> if the kind can't be
     * found.
     * 
     *  @throws CoreException if an error occurs while searching for the kind.
     */
    public IProductCmptKind findProductCmptKind() throws CoreException;
    
    /**
     * Returns the product component's version id. The version id is extracted from the
     * components name with the product component naming stratgey defined in the
     * project.
     * 
     * @throws CoreException if an exception occurs while accessing the project properties
     * to get the naming strategy or the version id can't be derived from the component's
     * name. 
     */
    public String getVersionId() throws CoreException;
    
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
     * @throws CycleException If a circle is detected.
     */
    public IProductCmptStructure getStructure() throws CycleException;
    
    /**
     * Returns the id this object is identified by at runtime. 
     */
    public String getRuntimeId();
    
    /**
	 * Sets the given runtimeId for this product component.
	 * 
	 * Be aware of the problems that can be caused by setting a new runtime id
	 * to an object where allready data with references to the old runtime id
	 * exists...
	 */
    public void setRuntimeId(String runtimeId);
}
