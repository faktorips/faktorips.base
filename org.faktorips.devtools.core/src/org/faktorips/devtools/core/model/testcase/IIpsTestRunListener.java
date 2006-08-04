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
 	 */
	public void testRunStarted(int testCount);
	
 	/**
 	 * A test has started for the given full qualified test name.
 	 */
    public void testStarted(String qualifiedTestName);

	/**
 	 * A test has ended.
	 */
    public void testFinished(String qualifiedTestName);
    
	/**
	 * An test has failed.
	 */    
    public void testFailureOccured(String[] failureDetails);

	/**
	 * Information about a member of the test suite that is about to be run.
	 * The format of the string is: 
	 * <pre>
	 * ""testName","isSuite","testcount"
	 * 
	 * testName: the full qualified name of the ips test
	 * isSuite: true or false depending on whether the test is a suite
	 * testCount: an integer indicating the number of tests 
	 * 
	 * Example: "testPass1,false,1"
	 * </pre>
	 */ 
    public void testTableEntry(String treeEntry);
    
    /** 
     * A test run has ended.
     */
    public void testRunEnded();
}
