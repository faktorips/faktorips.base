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

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.testcase.IIpsTestRunListener;

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
        failureDetailsList = new ArrayList<String[]>();
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
