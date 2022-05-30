/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.test;

import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.enumtype.EnumAttribute;
import org.faktorips.runtime.model.enumtype.EnumType;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsTestFailure {

    private IpsTestCaseBase testCase;
    private Throwable throwable;
    private Object expectedValue;
    private Object actualValue;

    private String testObject;
    private String testedAttribute;
    private String message;

    /**
     * Creates a new test error. The given test case was aborted because the indicated Throwable has
     * been thrown.
     */
    public IpsTestFailure(IpsTestCaseBase test, Throwable t) {
        this.testCase = test;
        this.throwable = t;
    }

    /**
     * Creates a new test failure that was caused because the given actual value is not equal to the
     * expected value.
     */
    public IpsTestFailure(IpsTestCaseBase test, Object expectedValue, Object actualValue) {
        this.testCase = test;
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
    }

    /**
     * Creates a new test failure that was caused because the given actual value is not equal to the
     * expected value.
     */
    public IpsTestFailure(IpsTestCaseBase test, Object expectedValue, Object actualValue, String testObject,
            String testedAttribute, String message) {
        this(test, expectedValue, actualValue);
        this.testObject = testObject;
        this.testedAttribute = testedAttribute;
        this.message = message;
    }

    /**
     * Creates a new test failure that was caused because an assertion has failed.
     */
    public IpsTestFailure(IpsTestCaseBase test) {
        this.testCase = test;
    }

    /**
     * Returns the test case that has failed.
     */
    public IpsTestCaseBase getTestCase() {
        return testCase;
    }

    /**
     * If this is a failed assertion failure then the method returns the path to the test object of
     * the expected result that contains the expected value that is not equal to the actual value.
     * <p>
     * Path format: TestParamName/TestParamName .. Example: Policy/Coverage
     * 
     * <p>
     * If this is an error the method returns <code>null</code>.
     */
    public String getTestObject() {
        return testObject;
    }

    /**
     * Returns the name of the attribute in the test object where the actual value is not the one
     * expected. If this is an error the method returns <code>null</code>.
     */
    public String getTestedAttribute() {
        return testedAttribute;
    }

    /**
     * Returns the message of the test failure. Return <code>null</code> if the failure has no
     * message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns <code>true</code> if the test was aborted because a throwable was thrown.
     */
    public boolean isError() {
        return throwable != null;
    }

    /**
     * Returns <code>true</code> if the test failed because an assertion failed.
     */
    public boolean isFailedAssertion() {
        return !isError();
    }

    /**
     * Returns the value that is expected to be the result of the business logic execution.
     */
    public Object getExpectedValue() {
        return expectedValue;
    }

    /**
     * Returns the actual value found after executing the business logic.
     * 
     * @see #getActualValueAsString()
     */
    public Object getActualValue() {
        return actualValue;
    }

    /**
     * Returns a string representing the actual value. This value should be used instead of
     * {@code getActualValue().toString()} when serializing and deserializing, as {@code toString()}
     * might return additional formatting. If the actual value is {@code null}, {@code "<null>"} is
     * returned.
     * 
     * @see #getActualValue()
     */
    public String getActualValueAsString() {
        if (actualValue == null) {
            return "<null>";
        }

        if (IpsModel.isEnumType(actualValue.getClass())) {
            EnumType enumType = IpsModel.getEnumType(actualValue);
            EnumAttribute idAttribute = enumType.getIdAttribute();
            Object value = idAttribute.getValue(actualValue);
            return String.valueOf(value);
        } else {
            return String.valueOf(actualValue);
        }
    }

    /**
     * Returns the throwable object (any kind of exception or error which occurs during the test
     * run).
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Returns a string representation of the IPS test failure.
     */
    @Override
    public String toString() {
        return "Failure in: " + testCase.getQualifiedName() + ", Object: " + testObject + ", Attribute: "
                + testedAttribute + ", Actual value: " + actualValue + ", Expected value: " + expectedValue;
    }

}
