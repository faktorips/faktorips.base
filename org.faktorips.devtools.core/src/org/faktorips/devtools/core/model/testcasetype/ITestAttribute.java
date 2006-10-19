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
    
    public final static String PROPERTY_TEST_ATTRIBUTE_TYPE = "testAttributeType"; //$NON-NLS-1$
    
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
     * Validation message code to indicate that there is an unsupported type.
     */
    public final static String MSGCODE_WRONG_TYPE = MSGCODE_PREFIX + "WrongType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the type doesn't matches the parent type.<br>
     * E.g. the parent defines the input type and the attribute has the expected result type.
     */
    public final static String MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE = MSGCODE_PREFIX + "TypeDoesNotMatchParentType"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the attribute name is duplicate.
     */
    public final static String MSGCODE_DUPLICATE_TEST_ATTRIBUTE_NAME = MSGCODE_PREFIX + "DuplicateTestAttributeName"; //$NON-NLS-1$

//  TODO Joerg: remove if not necessary
//    /**
//     * Validation message code to indicate that computed or derived attributes have type expected result.
//     */
//    public final static String MSGCODE_DERIVED_OR_COMPUTED_BUT_NOT_EXPECTED_RES = MSGCODE_PREFIX + "ExpectedOrComputedButNotExpectedRes"; //$NON-NLS-1$
//    
    /**
     * Validation message code to indicate that the attribute and the attribute type is already given.<br>
     * E.g. The attribute "a" could be added as input and expected result but not as input twice.
     */
    public final static String MSGCODE_DUPLICATE_ATTRIBUTE_AND_TYPE = MSGCODE_PREFIX + "DuplicateAttributeAndType"; //$NON-NLS-1$
    
    /**
     * Sets the attribute's name.
     */
    public void setName(String newName);
    
    /**
     * Returns the attribute's name.
     * {@inheritDoc}
     */
    public String getName();
    
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
     * Returns the parameter type of the test attribute.
     */
    public TestParameterType getTestAttributeType();
    
	/**
	 * Returns <code>true</code> if the test attribute is an expected result attribute, 
	 * otherwise <code>false</code>.
	 */
	public boolean isExpextedResultAttribute();
    
    /**
     * Sets the type of the test attribute. The following types could be set.
     * <p><ul>
     * <li>INPUT: the test attribute specifies test attribute input objects
     * <li>EXPECTED_RESULT: the test attribute specifies test attribute expected result objects
     * </ul>
     */
    public void setTestAttributeType(TestParameterType type);    
}
