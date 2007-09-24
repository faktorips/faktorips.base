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
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeAssociation;


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
     * Validation message code to indicate that the product component type for this generation
     * contains an attribute that has no corresponding config element configured in this generation.
     */
    public final static String MSGCODE_ATTRIBUTE_WITH_MISSING_CONFIG_ELEMENT = MSGCODE_PREFIX + "AttributeWithMissingConfigElement"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that more than one relation of a specific type have the same target.
     */
    public final static String MSGCODE_DUPLICATE_RELATION_TARGET = MSGCODE_PREFIX + "DuplicateRelationTarget"; //$NON-NLS-1$

    /**
     * Returns the product component this generation belongs to.
     */
    public IProductCmpt getProductCmpt();
    
    /**
     * Searches the product component type this product component generation is based on.
     *  
     * @return The product component type this product component generation is based on 
     * or <code>null</code> if the product component type can't be found.
     *  
     * @throws CoreException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is <code>null</code>. 
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException;
    
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
    public IProductCmptLink[] getLinks();
    
    /**
     * Returns the links that are instances of the given product component type association or
     * an empty array if no such link is found.
     * 
     * @param association The name (=target role singular) of an association.
     * @throws IllegalArgumentException if type relation is null. 
     */
    public IProductCmptLink[] getLinks(String association);

    /**
     * Returns the number of relations.
     */
    public int getNumOfLinks();
    
    /**
     * Creates a new link that is an instance of the product component type association
     * identified by the given association name.
     * 
     * @throws NullPointerException if associationName is <code>null</code>.
     */
    public IProductCmptLink newLink(String associationName);
    
    /**
     * Creates a new link  that is an instance of the product component type association.
     * 
     * @throws NullPointerException if association is <code>null</code>.
     */
    public IProductCmptLink newLink(IProductCmptTypeAssociation association);
    
    /**
     * Creates a new link that is an instance of the given association. 
     * The new link is placed before the given one.
     */
    public IProductCmptLink newLink(String association, IProductCmptLink insertBefore);
    
    /**
     * Checks whether a new link as instance of the given product component type association and 
     * the gven target will be valid.
     * 
     * @param ipsProject The project which ips object path is used for the searched.
     *                   This is not neccessarily the project this component is part of. 
     *                   
     * @return <code>true</code> if a new relation with the given values will be valid, <code>false</code> otherwise.
     * 
     * @throws CoreException if a problem occur during the search of the type hierarchy.
     */
	public boolean canCreateValidLink(IProductCmpt target, String association, IIpsProject ipsProject) throws CoreException;

	/**
     * Moves the first given relation in front of the second one.
     */
    public void moveLink(IProductCmptLink toMove, IProductCmptLink moveBefore);
    
    /**
     * @return A new table content usage. 
     */
    public ITableContentUsage newTableContentUsage();

    /**
     * @param rolename The rolename for the required content usage.
     * @return The table content usage for the table structure usage with the given rolename.
     */
    public ITableContentUsage getTableContentUsage(String rolename);
    
    /**
     * @return All table content usages defined by this generation.
     */
    public ITableContentUsage[] getTableContentUsages();
    
    /**
     * Returns the numer of formulas defined in the generation. 
     */
    public int getNumOfFormulas();

    /**
     * Returns the formulas defined in the generation. Returns an empty array if the generation
     * hasn't got a formula. 
     */
    public IFormula[] getFormulas();
    
    /**
     * Creates a new formula.
     */
    public IFormula newFormula();
}
