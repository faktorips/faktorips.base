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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Specification of an ips test runner.
 * 
 * @author Joerg Ortmann
 */
public interface IIpsTestRunner {

    /**
     * Run the given ips test.
     * 
     * @param classpathRepository the name of the repository in the classpath which contains 
     *                            the to be tested testsuite.
     * @param testsuite the name of the testsuite which will be executed.
     * 
     * @throws CoreException if an error occured.
     */
	public void run(String classpathRepository, String testsuite) throws CoreException;
	
    /**
     * Terminates the currently lauched test run process.
     * 
     * @throws CoreException if an error occurs.
     */
    public void terminate() throws CoreException;
    
    /**
     * Adds the given ips test run listener to the collection of listeners
     */
    public void addIpsTestRunListener(IIpsTestRunListener newListener);
    
	/**
	 * Removes the given ips test run listener from the collection of listeners
	 */
	public void removeIpsTestRunListener(IIpsTestRunListener listener);
	
	/**
	 * Returns all registered ips test run listener.
	 */
	public List getIpsTestRunListener();
	
	/**
	 * Sets the java project the test which will be started by this runner belongs to.
	 */
	public void setJavaProject(IJavaProject project);
	
	/**
	 * Returns the java project.
	 */
	public IJavaProject getJavaProject();	
	
	/**
	 * Starts a new job for running tests.
	 */
	public void startTestRunnerJob(String classpathRepository, String testsuite);
}
