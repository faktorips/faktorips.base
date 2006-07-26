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

import org.faktorips.devtools.core.model.IIpsObject;

/**
 * Specification of a test case type.
 * 
 * @author Jan Ortmann
 */
public interface ITestCaseType extends IIpsObject {

	/**
	 * Creates a new test input value parameter.
	 */
	public ITestValueParameter newInputValueParameter();

	/**
	 * Creates a new test input policy component type parameter.
	 */
	public ITestPolicyCmptTypeParameter newInputPolicyCmptTypeParameter();
	
	/**
	 * Creates a new test expected result value parameter.
	 */
	public ITestValueParameter newExpectedResultValueParameter();

	/**
	 * Creates a new test expected result policy component type parameter.
	 */
	public ITestPolicyCmptTypeParameter newExpectedResultPolicyCmptParameter();
	
	
	/**
	 * Returns all input parametes or an empty array if the test case type hasn't got
	 * any input parameters.
	 */
	public ITestParameter[] getInputParameters();
	
	/**
	 * Returns the input test value parameter or <code>null</code> if not found.
	 */
	public ITestValueParameter getInputTestValueParameter(String inputTestValueParameter);
	
	/**
	 * Returns the input test policy component type parameter or <code>null</code> if not found.
	 */
	public ITestPolicyCmptTypeParameter getInputTestPolicyCmptTypeParameter(String inputTestPolicyCmptTypeParameter);	
	
	/**
	 * Returns all expected result parametes or an empty array if the test case type hasn't got
	 * any result parameters.
	 */
	public ITestParameter[] getExpectedResultParameter();

	/**
	 * Returns the expected result test value parameter or <code>null</code> if not found.
	 */
	public ITestValueParameter getExpectedResultTestValueParameter(String expResultTestValueParameter);
	
	/**
	 * Returns the expected result test policy component type parameter or <code>null</code> if not found.
	 */
	public ITestPolicyCmptTypeParameter getExpectedResultTestPolicyCmptTypeParameter(String expResultTestPolicyCmptTypeParameter);
}
