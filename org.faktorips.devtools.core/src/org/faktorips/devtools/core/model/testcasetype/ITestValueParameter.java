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
import org.faktorips.datatype.ValueDatatype;

/**
 *  Specification of a test value parameter.
 *  
 * @author Joerg Ortmann
 */
public interface ITestValueParameter extends ITestParameter {

	/** Property names */
    public final static String PROPERTY_VALUEDATATYPE = "valueDatatype"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTVALUEPARAMETER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value datatype not exists.
     */
    public final static String MSGCODE_VALUEDATATYPE_NOT_FOUND = MSGCODE_PREFIX + "ValueDatatypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is an unsupported role.
     */
    public final static String MSGCODE_WRONG_ROLE = MSGCODE_PREFIX + "WrongRole"; //$NON-NLS-1$

    /**
     * Returns the datatype.
     */
    public String getValueDatatype();
	
	/**
	 * Sets the datatype.
	 */
	public void setValueDatatype(String datatype);
	
    /**
     * Returns the datatype or <code>null</code> if the object does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the datatype.
     */			
	public ValueDatatype findValueDatatype() throws CoreException;
}
