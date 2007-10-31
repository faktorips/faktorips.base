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
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;


/**
 * A link between two product components. A link is an instance of an association between 
 * product component types.
 * 
 * @see IProductCmptTypeAssociation
 */
public interface IProductCmptLink extends IIpsObjectPart {
    
    public final static String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    public final static String PROPERTY_ASSOCIATION = "association"; //$NON-NLS-1$
    public final static String PROPERTY_MIN_CARDINALITY = "minCardinality"; //$NON-NLS-1$
    public final static String PROPERTY_MAX_CARDINALITY = "maxCardinality"; //$NON-NLS-1$
    
    public final static int CARDINALITY_MANY = IPolicyCmptTypeAssociation.CARDINALITY_MANY;
    
    /**
     * The name of the xml-tag used if this object is saved to xml.
     */
	public final static String TAG_NAME = "Relation"; //$NON-NLS-1$
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "PRODUCTCMPT_RELATION-"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the asscoation in the model can't be found.
     */
    public final static String MSGCODE_UNKNWON_ASSOCIATION = MSGCODE_PREFIX + "UnknownAssociation"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the target product component does not exist.
     */
    public final static String MSGCODE_UNKNWON_TARGET = MSGCODE_PREFIX + "UnknownTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is missing.
     */
    public final static String MSGCODE_MISSING_MAX_CARDINALITY = MSGCODE_PREFIX + "MissingMaxCardinality"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than 1.
     */
    public final static String MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1 = MSGCODE_PREFIX + "MaxCardinalityIsLessThan1"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality exceeds the 
     * maximum cardinality defined in the model.
     */
    public final static String MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX = MSGCODE_PREFIX + "MaxCardinalityExceedsModelMax"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than 
     * the min cardinality.
     */
    public final static String MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN = MSGCODE_PREFIX + "MaxCardinalityIsLessThanMin"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than 
     * the min cardinality.
     */
    public final static String MSGCODE_INVALID_TARGET = MSGCODE_PREFIX + "InvalidTarget"; //$NON-NLS-1$
    
    /**
     * Returns the product component generation this config element belongs to.
     */
    public IProductCmpt getProductCmpt();
    
    /**
     * Returns the product component generation this config element belongs to.
     */
    public IProductCmptGeneration getProductCmptGeneration();
    
    /**
     * Returns the name of the product component type association this link is an instance of.
     */
    public String getAssociation();
    
    /**
     * Finds the product component type association this link is an instance of. 
     * Note that the method searches not only the direct product component type this product component is based on, 
     * but also it's super type hierarchy.
     * 
     * @param ipsProject The project which ips object path is used for the searched.
     *                   This is not neccessarily the project this type is part of. 
     * 
     * @return the association or <code>null</code> if no such association exists.
     * 
     * @throws CoreException if an exception occurs while searching the relation. 
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public IProductCmptTypeAssociation findAssociation(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the target product component.
     */
    public String getTarget();
    
    /**
     * Sets the target product component.
     */
    public void setTarget(String newTarget);

    /**
     * Retursn the product component which is the target of this association or <code>null</code>, 
     * if no (valid) target name is set.
     * 
     * @param ipsProject The project which ips object path is used for the searched.
     *                   This is not neccessarily the project this component is part of. 
     *      
     * @throws CoreException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public IProductCmpt findTarget(IIpsProject ipsProject) throws CoreException;

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
     * If the number is not limited CARDINALITY_MANY is returned. 
     */
    public int getMaxCardinality();
    
    /**
     * Sets the maxmium number of target instances allowed in this relation.
     * An unlimited number is represented by CARDINALITY_MANY. 
     */
    public void setMaxCardinality(int newValue);
    
    /**
     * Returns weather or not this Relation is mandatory. A Relation is mandatory if both minimum
     * and maximum-cardinality are equal to 1.
     * @return <code>true</code> if this Relation is mandatory, else <code>false</code>.
     */
    public boolean isMandatory();

    /**
     * Returns weather or not this Relation is optional. A Relation is optional if the minimum
     * cardinality equals 0 and the maximum cardinality equals 1.
     * @return <code>true</code> if this Relation is optinal, else <code>false</code>.
     */
    public boolean isOptional();
}
