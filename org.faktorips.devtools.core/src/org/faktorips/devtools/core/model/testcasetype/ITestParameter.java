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

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 *  Specification of a test parameter.
 *  
 * @author Joerg Ortmann
 */
public interface ITestParameter extends IIpsObjectPart {
	
	/** Property names */
    public final static String PROPERTY_TEST_PARAMETER_TYPE = "testParameterType"; //$NON-NLS-1$
    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTPARAMETER-"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the test parameter name is duplicate.
     */
    public final static String MSGCODE_DUPLICATE_NAME = MSGCODE_PREFIX + "DuplicateName"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate the correct name format.
     */
    public static final String MSGCODE_INVALID_NAME = MSGCODE_PREFIX + "InvalidName"; //$NON-NLS-1$
    
	/**
	 * Returns the parameters's name, e.g. 'effectiveDate'.
	 * {@inheritDoc}
	 */
	public String getName();

	/**
	 * Sets the parameters's name, e.g. 'effectiveDate'.
	 */
	public void setName(String newName);
	
    /**
     * Sets the datatype.
     */
    public void setDatatype(String datatype);
    
    /**
     * Returns the datatype.
     */
    public String getDatatype();
    
	/**
	 * Returns <code>true</code> if the test parameter is an input parameter, 
	 * otherwise <code>false</code>.
     * Note if the parameter is a child parameter then the type (input, expected result, or combined) 
     * of the root test parameter will be checked. All childs inherit the type of the root parameter.
	 */
	public boolean isInputParameter();
	
	/**
	 * Returns <code>true</code> if the parameter is an expected result parameter, 
	 * otherwise <code>false</code>.
     * Note if the parameter is a child parameter then the type (input, expected result, or combined) 
     * of the root test parameter will be checked. All childs inherit the type of the root parameter.
	 */
	public boolean isExpextedResultParameter();

	/**
	 * Returns <code>true</code> if the test parameter is a combined parameter, 
	 * otherwise <code>false</code>. A combined test parameter is a parameter that specifies
	 * both input objects and expected results objects.
     * Note if the parameter is a child parameter then the type (input, expected result, or combined) 
     * of the root test parameter will be checked. All childs inherit the type of the root parameter.
	 */
	public boolean isCombinedParameter();
	
    /**
     * Returns the type of the test parameter.
     */
	public TestParameterType getTestParameterType();

    /**
     * Sets the parameter type of the test parameter.
     */
    public void setTestParameterType(TestParameterType testParameterType);
    
    /**
     * Returns the root test parameter.
     */
    public ITestParameter getRootParameter();
    
    /**
     * Returns <code>true</code> if the test parameter is a root parameter 
     * or <code>false</code> if the parameter is a child of another object.
     */
    public boolean isRoot();
}
