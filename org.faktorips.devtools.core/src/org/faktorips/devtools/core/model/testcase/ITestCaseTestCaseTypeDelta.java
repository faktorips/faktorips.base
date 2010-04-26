/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.testcase;

import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;

/**
 * A test case / test case type delta describes the difference between what a test case based on
 * specific test case type should contain and what it actually contains.
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
     * Returns true if the delta is empty. The test case conforms to the test case type it is based
     * on.
     */
    public boolean isEmpty();

    /**
     * Returns <code>true</code> if the sort order of the test parameter changed.
     */
    public boolean isDifferentTestParameterOrder();

    //
    // Missing test case type side objects
    //

    /**
     * Test Case Side: Returns the test policy components with missing test policy cmpt type
     * parameter.
     */
    public ITestPolicyCmpt[] getTestPolicyCmptsWithMissingTypeParam();

    /**
     * Test Case Side: Returns the test policy components link with missing test policy cmpt type
     * parameter.
     */
    public ITestPolicyCmptLink[] getTestPolicyCmptLinkWithMissingTypeParam();

    /**
     * Test Case Side: Returns the test attribute value with missing test attribute.
     */
    public ITestAttributeValue[] getTestAttributeValuesWithMissingTestAttribute();

    /**
     * Test Case Side: Returns the test value with missing test value parameter
     */
    public ITestValue[] getTestValuesWithMissingTestValueParam();

    /**
     * Test Case Side: Returns the test rules with missing test value parameter
     */
    public ITestRule[] getTestRulesWithMissingTestValueParam();

    //
    // Missing test case type side objects
    //

    /**
     * Test Case Type Side: Returns the root test policy cmpt type parameter with missing test
     * policy cmpt.
     */
    public ITestPolicyCmptTypeParameter[] getTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt();

    /**
     * Test Case Type Side: Returns the test attribute with missing test attribute value.
     */
    public ITestAttribute[] getTestAttributesWithMissingTestAttributeValue();

    /**
     * Test Case Type Side: Returns the test value parameter with missing test value.
     */
    public ITestValueParameter[] getTestValueParametersWithMissingTestValue();

    /**
     * Returns the test policy cmpts for the missing test attribute.<br>
     * Returns <code>null</code> if the given test attribute has no corresponding test policy cmpt.
     */
    public ITestPolicyCmpt[] getTestPolicyCmptForMissingTestAttribute(ITestAttribute testAttribute);

    /**
     * Returns the test policy cmpts which have a different sort order compared to their
     * corresponding test policy cmpt type parameter.
     */
    public ITestPolicyCmpt[] getTestPolicyCmptWithDifferentSortOrder();

    /**
     * Returns the test policy cmpts which have a different sort order of their test attribute
     * values compared to their corresponding test policy cmpt type parameter.
     */
    public ITestPolicyCmpt[] getTestPolicyCmptWithDifferentSortOrderTestAttr();
}
