/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.testcase.IIpsTestRunListener;

/**
 * Special ips test run listener to listen for failures and forward this failures as new expected
 * result to the test case editor.
 * 
 * @author Joerg Ortmann
 */
public class RunAndStoreExpectedResultListener implements IIpsTestRunListener {

    private TestCaseSection testCaseSection;

    private List<String[]> failureDetailsList;

    public RunAndStoreExpectedResultListener(TestCaseSection testCaseSection) {
        this.testCaseSection = testCaseSection;
        failureDetailsList = new ArrayList<>();
    }

    /**
     * A faiure will be used to fetch the expected result and stores it in the test case editor.
     */
    @Override
    public void testFailureOccured(String testFailureOccured, String[] failureDetails) {
        synchronized (failureDetailsList) {
            failureDetailsList.add(failureDetails);
        }
    }

    @Override
    public void testFinished(String qualifiedTestName) {
        // inform the test case editor about the end of the test run
        testCaseSection.testFailureOccuredToStoreExpResult(qualifiedTestName, failureDetailsList);
        testCaseSection.testRunEndedToStoreExpResult(qualifiedTestName);
    }

    @Override
    public void testRunStarted(int testCount, String classpathRepository, String testPackage) {
        // nothing to do
    }

    @Override
    public void testStarted(String qualifiedTestName) {
        // nothing to do
    }

    @Override
    public void testTableEntry(String qualifiedName, String fullPath) {
        // nothing to do
    }

    @Override
    public void testTableEntries(String[] qualifiedName, String[] fullPath) {
        // nothing to do
    }

    @Override
    public void testErrorOccured(String qualifiedTestName, String[] errorDetails) {
        testCaseSection.testErrorOccured(qualifiedTestName, errorDetails);
    }

    @Override
    public void testRunEnded(String elapsedTime) {
        // nothing to do
    }

    @Override
    public boolean canNavigateToFailure() {
        return false;
    }

}
