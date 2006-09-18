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

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.testcase.IIpsTestRunListener;

/**
 * Special ips test run listener to listen for failures and forward this failures as
 * new expected result to the test case editor.
 * 
 * @author Joerg Ortmann
 */
public class RunAndStoreExpectedResultListener implements IIpsTestRunListener {
    private TestCaseSection testCaseSection;
    
    private List failureDetailsList;
    
    public RunAndStoreExpectedResultListener(TestCaseSection testCaseSection) {
        this.testCaseSection = testCaseSection;
        failureDetailsList = new ArrayList();
    }

    /**
     * A faiure will be used to fetch the expected result and stores it in the test case editor.
     * 
     * {@inheritDoc}
     */
    public void testFailureOccured(String testFailureOccured, String[] failureDetails) {
        synchronized (failureDetailsList) {
            failureDetailsList.add(failureDetails);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void testFinished(String qualifiedTestName) {
        // inform the test case editor about the end of the test run
        testCaseSection.testFailureOccuredToStoreExpResult(qualifiedTestName, failureDetailsList);
        testCaseSection.testRunEndedToStoreExpResult(qualifiedTestName);
    }
    
    /**
     * {@inheritDoc}
     */
    public void testRunStarted(int testCount, String classpathRepository, String testPackage) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void testStarted(String qualifiedTestName) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void testTableEntry(String qualifiedName, String fullPath) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void testTableEntries(String[] qualifiedName, String[] fullPath) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void testErrorOccured(String qualifiedTestName, String[] errorDetails) {
        testCaseSection.testErrorOccured(qualifiedTestName, errorDetails);
    }

    /**
     * {@inheritDoc}
     */
    public void testRunEnded(String elapsedTime) {
        // nothing to do
    }    
}
