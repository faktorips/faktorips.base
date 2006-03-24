/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.product;

import java.util.Locale;

import org.faktorips.util.message.MessageList;

/**
 * The product component name includes a version id and a constant part that
 * is constant over the different versions. E.g. given FullCoverage.2005-01-01  
 * the version id is 2005-01-01 and the constant part is FullCoverage. 
 * The product component naming strategy defines how the name is constructed
 * from a given constant and version id and vice versa. It also defines how to
 * derive the next version id.
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptNamingStrategy {

    /**
     * Prefix for all message codes for classes implementing the interface.
     */
    public final static String MSGCODE_PREFIX = "ProductCmptNamingStrategy-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name contains illegal characters.
     */
    public final static String MSGCODE_ILLEGAL_VERSION_ID = MSGCODE_PREFIX + "IllegalVersionId"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the version separator is missing.
     */
    public final static String MSGCODE_MISSING_VERSION_SEPARATOR = MSGCODE_PREFIX + "VersionSeparatorIsMissing"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that multiple separator chars are found in the
     * name (but only 1 is allowd).
     */
    public final static String MSGCODE_ONYL_1_OCCURENCE_OF_SEPARATOR_ALLOWED = MSGCODE_PREFIX + "Only1OccurenceOfSeperatorAllowed"; //$NON-NLS-1$

    /**
	 * Returns the strategy's identification.
	 */
	public String getId();
	
	/**
	 * Returns the strategy's name, used to present it to the user.
	 */
	public String getName(Locale locale);

	/**
	 * Returns the unqualified product component name defined by the constant part and the
	 * version id. Returns <code>null</code> if constant part and version id are
	 * <code>null</code>. If only of the two arguments is <code>null</code>, the
	 * method returns the other.
	 */
	public String getProductCmptName(String constantPart, String versionId);
	
	/**
	 * Returns the product component name's constant part, that is the name without 
	 * the version id. Returns <code>null</code> if qName is <code>null</code>.
	 * 
	 * @param productCmptName The unqualified product component name.
	 * @throws IllegalArgumentException if the constant part can't be extracted
	 * from the name. 
	 */
	public String getConstantPart(String productCmptName);
	
	/**
	 * Returns the version id included in the qualified product component name.
	 * 
	 * @param productCmptName The unqualified product component name.
	 * @throws IllegalArgumentException if the versionId can't be extracted
	 * from the unqualified name. 
	 */
	public String getVersionId(String productCmptName);
	
	/**
	 * Returns the next version id. 
	 *  
	 * @param versionId A version id that can be used to derive the next one.
	 */
	public String getNextVersionId(IProductCmpt productCmpt);
	
	/**
	 * Returns the name that is constructed from the given product component's
	 * name's constant part and the next version id.
	 */
	public String getNextName(IProductCmpt productCmpt);
	
	/**
	 * Checks if the version id and the constant part can be derived from the given
	 * product component name and if they have the correct format. 
	 * The strategy has to ensure that if two names would result in the same Java identifier
	 * only one of them is considered as valid by the validate() method.
	 */
	public MessageList validate(String name);
	
	/**
	 * Checks if the version id has the correct format. 
	 * 
	 * @throws NullPointerException if versionId is <code>null</code>.
	 */
	public MessageList validateVersionId(String versionId);

	/**
	 * Checks if the constant part has the correct format. 
	 * 
	 * @throws NullPointerException if constantPartName is <code>null</code>.
	 */
	public MessageList validateConstantPart(String constantPartName);

	/**
	 * Returns a valid Java class identifier for the given name. For example - and .
	 * are not allowed in Java identifiers but are typically used in the name for example
	 * to separate constant part and version id.
	 * <p>
	 * The strategy has to ensure that if two names would result in the same Java identifier
	 * only one of them is considered as valid by the validate() method.
	 * <p>
	 * A good strategy is for example to replace the character - by two underscores, 
	 * . by three underscores and to disallow names with two or more unseparated underscores.  
	 * <p>
	 * Returns <code>null</code> if if name is <code>null</code>.
	 * 
	 * @throws IllegalArgumentException if the name can't be transformed to a valid Java
	 * class name as it contains special characters that can't be handled by this strategy.
	 */
	public String getJavaClassIdentifier(String name);
}
