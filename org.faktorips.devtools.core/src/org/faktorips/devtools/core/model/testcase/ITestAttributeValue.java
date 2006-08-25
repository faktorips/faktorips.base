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

package org.faktorips.devtools.core.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;

/**
 *  Specification of a test attribute value.
 *  
 * @author Joerg Ortmann
 */
public interface ITestAttributeValue extends IIpsObjectPart{
	
	/** Property names */
    public final static String PROPERTY_ATTRIBUTE = "testAttribute"; //$NON-NLS-1$
    public final static String PROPERTY_VALUE = "value"; //$NON-NLS-1$
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTATTRIBUTEVALUE-"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that the corresponding test attribute not exists.
	 */
	public final static String MSGCODE_TESTATTRIBUTE_NOT_FOUND = MSGCODE_PREFIX
		+ "TestAttributeNotFound"; //$NON-NLS-1$
	
    /**
	 * Validation message code to indicate that the datatype of the attribute not exists.
	 */
	public final static String MSGCODE_DATATYPE_NOT_FOUND = MSGCODE_PREFIX
		+ "DatatypeNotFound"; //$NON-NLS-1$
	
    /**
	 * Validation message code to indicate that the datatype value is not parsable.
	 */
	public final static String MSGCODE_DATATYPEVALUE_NOT_PARSABLE = MSGCODE_PREFIX
		+ "DatatypeValueNotParsable"; //$NON-NLS-1$
	
    /**
     * Returns the attribute. 
     */
	public String getTestAttribute();
	
	/**
	 * Sets the given attribute.
	 */
	public void setTestAttribute(String attribute);
	
    /**
     * Returns the test attribute or <code>null</code> if the test attribute does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the test attribute.
     */		
	public ITestAttribute findTestAttribute() throws CoreException;
	
	/**
	 * Returns value of the attribute.
	 */
	public String getValue();
	
	/**
	 * Sets the value of the attribute.
	 */
	public void setValue(String newValue);
    
    /**
     * Returns <code>true</code> if the test attribute value is an input attribute, 
     * otherwise <code>false</code>.
     */
    public boolean isInputAttribute();
    
    /**
     * Returns <code>true</code> if the test attribute value is an expected result attribute, 
     * otherwise <code>false</code>.
     */
    public boolean isExpextedResultAttribute();
    
}
