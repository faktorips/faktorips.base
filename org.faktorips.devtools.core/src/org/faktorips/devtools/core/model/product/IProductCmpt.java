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

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.IFixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;


/**
 * 
 */
public interface IProductCmpt extends ITimedIpsObject, IFixDifferencesToModelSupport {
    
    /**
     * The name of the product component type property
     */
    public final static String PROPERTY_PRODUCT_CMPT_TYPE = "productCmptType"; //$NON-NLS-1$

    public final static String PROPERTY_RUNTIME_ID = "runtimeId"; //$NON-NLS-1$
    
    public final static String MSGCODE_PREFIX = "PRODUCT_CMPT-"; //$NON-NLS-1$
    
    /**
     * Validation message code that indicates that the product component type the product component is an instance of
     * is missing.
     */
    public final static String MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE = MSGCODE_PREFIX + "MissingProductCmptType"; //$NON-NLS-1$

    /**
     * Validation message code that indicates if the type's hierarchy the product component
     * is based on is inconsistent.  
     */
    public final static String MSGCODE_INCONSISTENT_TYPE_HIERARCHY = MSGCODE_PREFIX + "InconsistTypeHierarchy"; //$NON-NLS-1$

    /**
     * Returns the product component's generation at the specified index.
     * 
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public IProductCmptGeneration getProductCmptGeneration(int index);

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
     * Searches the policy component type this product component is based on.
     *  
     * @return The policy component type this product component is based on 
     * or <code>null</code> if the policy component type can't be found.
     *  
     * @throws CoreException if an exception occurs while searching for the type.
     */
    public IPolicyCmptType findPolicyCmptType() throws CoreException;
    
    /**
     * Returns the qualified name of the product component type this product component
     * is based on.
     */
    public String getProductCmptType();
    
    /**
     * Sets the qualified name of the product component type this product component
     * is based on.
     * 
     * @throws NullPointerException if newType is <code>null</code>.
     */
    public void setProductCmptType(String newType);
    
    /**
     * Searches the product component type this product component is based on.
     *  
     * @param ipsProject The project which ips object path is used for the searched.
     *                   This is not neccessarily the project this component is part of. 
     *      
     * @return The product component type this product component is based on 
     * or <code>null</code> if the product component type can't be found.
     *  
     * @throws CoreException if an exception occurs while searching for the type.
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException;

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
    public IPolicyCmptTypeAssociation findPcTypeRelation(String relationName) throws CoreException;
    
    /**
     * Returns <code>true</code> if any of the generations contain at least one formula.
     * Returns <code>false</code> otherwise.
     */
    public boolean containsFormula();
    
    /**
     * Returns <code>true</code> if any of the generations contain at least one formula with at
     * least one formula test case. Returns <code>false</code> otherwise.
     */
    public boolean containsFormulaTest();
    
    /**
     * Returns the product component structure representing the structure defined by relations. 
     * The relations are evaluated for the date defined by the user-set working date. The 
     * structure is rootet at this product.
     * 
     * @param ipsProject The project which ips object path is used for the searched.
     *                   This is not neccessarily the project this component is part of. 
     * 
     * @throws CycleException If a circle is detected.
     */
    public IProductCmptStructure getStructure(IIpsProject project) throws CycleException;
    
    /**
     * Returns the product component structure representing the structure defined by relations. 
     * The relations are evaluted for the given daten. The 
     * structure is rootet at this product.
     * 
     * @param ipsProject The project which ips object path is used for the searched.
     *                   This is not neccessarily the project this component is part of. 
     *                   
     * @throws CycleException If a circle is detected.
     */
    public IProductCmptStructure getStructure(GregorianCalendar date, IIpsProject project) throws CycleException;
    
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
