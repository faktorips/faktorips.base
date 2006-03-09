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
import org.faktorips.devtools.core.model.IIpsObjectGeneration;


/**
 *
 */
public interface IProductCmptGeneration extends IIpsObjectGeneration {

	
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "PRODUCTCMPTGEN-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the generation contains less 
     * relations of a specific relation type than required by the relation type.
     * E.g. a motor product must contain at least one collision coverage type, but
     * the motor product does not contain a relation to a collision coverage type.
     * <p>
     * Note that the message returned by the validate method contains two (Invalid)ObjectProperties.
     * The first one contains the generation and the second one the relation type as string.
     * In both cases the property part of the ObjectProperty is empty.
     */
    public final static String MSGCODE_NOT_ENOUGH_RELATIONS = MSGCODE_PREFIX + "NotEnoughRelations"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the generation contains more 
     * relations of a specific relation type than specified by the relation type.
     * E.g. a motor product can contain only one collision coverage type, but
     * the motor product contains two relations to a collision coverage type.
     * <p>
     * Note that the message returned by the validate method contains two (Invalid)ObjectProperties.
     * The first one contains the generation and the second one the relation type as string.
     * In both cases the property part of the ObjectProperty is empty.
     */
    public final static String MSGCODE_TOO_MANY_RELATIONS = MSGCODE_PREFIX + "ToManyRelations"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the template for the product this generation
     * is for could not be found.
     */
    public final static String MSGCODE_NO_TEMPLATE = MSGCODE_PREFIX + "NoTemplate"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that more than one relation of a specific type have the same target.
     */
    public final static String MSGCODE_DUPLICATE_RELATION_TARGET = MSGCODE_PREFIX + "DuplicateRelationTarget"; //$NON-NLS-1$

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
