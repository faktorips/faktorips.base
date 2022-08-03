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

/**
 * A listener interface for observing the execution of IPS test runs.
 * 
 * @author Joerg Ortmann
 */
public interface IIpsTestRunListener {

    /**
     * A test run has started.
     * 
     * @param testCount the number of individual tests that will be run
     * @param classpathRepository the repository where the test are selected from
     * @param testPackage the package including the tests
     */
    void testRunStarted(int testCount, String classpathRepository, String testPackage);

    /**
     * A test has started for the given full qualified test name.
     */
    void testStarted(String qualifiedTestName);

    /**
     * A test has ended.
     */
    void testFinished(String qualifiedTestName);

    /**
     * An test has failed. Message format:
     * qualifiedTestName|testObject|testedAttribute|expectedValue|actualValue|message
     */
    void testFailureOccured(String qualifiedTestName, String[] failureDetails);

    /**
     * Information about a member that this test case is about to be run.
     * 
     * @param qualifiedName qualified name of the test case
     * @param fullPath full path of the corresponding ipstestcase file
     */
    void testTableEntry(String qualifiedName, String fullPath);

    /**
     * Information about a member that this test cases are about to be run.
     * 
     * @param qualifiedName qualified names of the test case
     * @param fullPath full paths of the corresponding ipstestcase file
     */
    void testTableEntries(String[] qualifiedName, String[] fullPath);

    /**
     * A test run has ended.
     * 
     * @param elapsedTime contains the elapsed time in milliseconds
     */
    void testRunEnded(String elapsedTime);

    /**
     * A error (exception) occurred while executing the test.
     */
    void testErrorOccured(String qualifiedTestName, String[] errorDetails);

    /**
     * Returns <code>true</code> if the listener supports navigate to the corresponding failure
     * object. E.g. the test case editor can set the focus to the corresponding failure attribute
     * field.
     */
    boolean canNavigateToFailure();

}
