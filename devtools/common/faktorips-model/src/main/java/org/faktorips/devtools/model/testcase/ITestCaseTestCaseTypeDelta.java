/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.testcase;

import org.faktorips.devtools.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;

/**
 * A test case / test case type delta describes the difference between what a test case based on
 * specific test case type should contain and what it actually contains.
 */
public interface ITestCaseTestCaseTypeDelta extends IFixDifferencesComposite {

    /**
     * Returns the test case type this delta was computed for.
     */
    ITestCaseType getTestCaseType();

    /**
     * Returns the test case this delta was computed for.
     */
    ITestCase getTestCase();

    /**
     * Returns true if the delta is empty. The test case conforms to the test case type it is based
     * on.
     */
    @Override
    boolean isEmpty();

    /**
     * Returns <code>true</code> if the sort order of the test parameter changed.
     */
    boolean isDifferentTestParameterOrder();

    /**
     * Test Case Side: Returns the test policy components with missing test policy component type
     * parameter.
     */
    ITestPolicyCmpt[] getTestPolicyCmptsWithMissingTypeParam();

    /**
     * Test Case Side: Returns the test policy components link with missing test policy component
     * type parameter.
     */
    ITestPolicyCmptLink[] getTestPolicyCmptLinkWithMissingTypeParam();

    /**
     * Test Case Side: Returns the test attribute value with missing test attribute.
     */
    ITestAttributeValue[] getTestAttributeValuesWithMissingTestAttribute();

    /**
     * Test Case Side: Returns the test value with missing test value parameter
     */
    ITestValue[] getTestValuesWithMissingTestValueParam();

    /**
     * Test Case Side: Returns the test rules with missing test value parameter
     */
    ITestRule[] getTestRulesWithMissingTestValueParam();

    /**
     * Test Case Type Side: Returns the root test policy component type parameter with missing test
     * policy component.
     */
    ITestPolicyCmptTypeParameter[] getTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt();

    /**
     * Test Case Type Side: Returns the test attribute with missing test attribute value.
     */
    ITestAttribute[] getTestAttributesWithMissingTestAttributeValue();

    /**
     * Test Case Type Side: Returns the test value parameter with missing test value.
     */
    ITestValueParameter[] getTestValueParametersWithMissingTestValue();

    /**
     * Returns the test policy components for the missing test attribute.<br>
     * Returns <code>null</code> if the given test attribute has no corresponding test policy cmpt.
     */
    ITestPolicyCmpt[] getTestPolicyCmptForMissingTestAttribute(ITestAttribute testAttribute);

    /**
     * Returns the test policy components which have a different sort order compared to their
     * corresponding test policy component type parameter.
     */
    ITestPolicyCmpt[] getTestPolicyCmptWithDifferentSortOrder();

    /**
     * Returns the test policy components which have a different sort order of their test attribute
     * values compared to their corresponding test policy component type parameter.
     */
    ITestPolicyCmpt[] getTestPolicyCmptWithDifferentSortOrderTestAttr();

}
