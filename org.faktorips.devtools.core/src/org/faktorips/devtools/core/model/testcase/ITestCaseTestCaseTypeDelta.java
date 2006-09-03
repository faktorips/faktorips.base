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

import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;

/**
 * A product test case / test case type delta describes the difference
 * between what a test case based on specific test case type
 * should contain and what it actually contains. 
 */
public interface ITestCaseTestCaseTypeDelta {

    /**
     * Returns the test case type this delta was computed for.
     */
    public ITestCaseType getTestCaseType();
    
    /**
     * Returns the test case this delta was computed for.
     */
    public ITestCase getTestCase();
    
    /**
     * Returns true if the delta is empty. The test case conforms to
     * the test case type it is based on. 
     */
    public boolean isEmpty();
    
    /**
     * Test Case Side: Returns the test policy components relation with missing test policy cmpt
     * type parameter.
     */
    public ITestPolicyCmptRelation[] getTestPolicyCmptRelationsWithMissingTestPolicyCmptTypeParam();

    /**
     * Test Case Side: Returns the test attribute value with missing test attribute.
     */
    public ITestAttributeValue[] getTestAttributeValuesWithMissingTestAttribute();

    /**
     * Test Case Side: Returns the test value with missing test value parameter
     */
    public ITestValue[] getTestValuesWithMissingTestValueParameter();
    
    
    /**
     *  Test Case Type Side: Returns the test attribute with missing test attribute value. 
     */
    public ITestAttribute[] getTestAttributesWithMissingTestAttributeValue();
    
    /**
     * Test Case Type Side: Returns the test value parameter with missing test value.
     */
    public ITestValueParameter[] getTestValueParametersWithMissingTestValue();
}
