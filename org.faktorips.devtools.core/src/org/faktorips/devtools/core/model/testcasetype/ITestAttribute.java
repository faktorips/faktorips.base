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

package org.faktorips.devtools.core.model.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IAttribute;

/**
 * Specification of a test attribute.
 * 
 * @author Joerg Ortmann
 */
public interface ITestAttribute extends IIpsObjectPart {
	
	/** Property names */
    public final static String PROPERTY_ATTRIBUTE = "attribute"; //$NON-NLS-1$
    
    public final static String PROPERTY_TEST_ATTRIBUTE_ROLE = "testAttributeRole"; //$NON-NLS-1$
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute which is related by the test attribute not exists.
     */
    public final static String MSGCODE_ATTRIBUTE_NOT_FOUND = MSGCODE_PREFIX
        + "AttributeNotFound"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that there is an unsupported role.
     */
    public final static String MSGCODE_WRONG_ROLE = MSGCODE_PREFIX + "WrongRole"; //$NON-NLS-1$
    
    /**
     * Returns the attribute's name.
     * {@inheritDoc}
     */
    public String getName();

    /**
     * Sets the attribute's name.
     */
    public void setName(String newName);
    
    /**
     * Returns the attribute.
     */
	public String getAttribute();
	
	/**
	 * Sets the given attribute.
	 */
	public void setAttribute(String attribute);
	
    /**
     * Returns the model attribute or <code>null</code> if the attribute does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the attribute.
     */	
	public IAttribute findAttribute() throws CoreException;
	
	/**
	 * Returns <code>true</code> if the test attribute is an input attribute, 
	 * otherwise <code>false</code>.
	 */
	public boolean isInputAttribute();
	
	/**
	 * Returns <code>true</code> if the test attribute is an expected result attribute, 
	 * otherwise <code>false</code>.
	 */
	public boolean isExpextedResultAttribute();
}
