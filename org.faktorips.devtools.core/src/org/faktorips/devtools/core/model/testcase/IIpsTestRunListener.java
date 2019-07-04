/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.testcase;

/**
 * A listener interface for observing the execution of ips test runs.
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
    public void testRunStarted(int testCount, String classpathRepository, String testPackage);

    /**
     * A test has started for the given full qualified test name.
     */
    public void testStarted(String qualifiedTestName);

    /**
     * A test has ended.
     */
    public void testFinished(String qualifiedTestName);

    /**
     * An test has failed. Message format:
     * qualifiedTestName|testObject|testedAttribute|expectedValue|actualValue|message
     */
    public void testFailureOccured(String qualifiedTestName, String[] failureDetails);

    /**
     * Information about a member that this test case is about to be run.
     * 
     * @param qualifiedName qualified name of the test case
     * @param fullPath full path of the corresponding ipstestcase file
     */
    public void testTableEntry(String qualifiedName, String fullPath);

    /**
     * Information about a member that this test cases are about to be run.
     * 
     * @param qualifiedName qualified names of the test case
     * @param fullPath full paths of the corresponding ipstestcase file
     */
    public void testTableEntries(String[] qualifiedName, String[] fullPath);

    /**
     * A test run has ended.
     * 
     * @param elapsedTime contains the elapsed time in milliseconds
     */
    public void testRunEnded(String elapsedTime);

    /**
     * A error (exception) occurred while executing the test.
     */
    public void testErrorOccured(String qualifiedTestName, String[] errorDetails);

    /**
     * Returns <code>true</code> if the listener supports navigate to the corresponding failure
     * object. E.g. the test case editor can set the focus to the corresponding failure attribute
     * field.
     */
    public boolean canNavigateToFailure();

}
