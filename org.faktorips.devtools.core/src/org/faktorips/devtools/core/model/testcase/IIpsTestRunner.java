/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.testcase;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Specification of an IPS test runner.
 * 
 * @author Joerg Ortmann
 */
public interface IIpsTestRunner {

    /**
     * Terminates the currently launched test run process.
     * 
     * @throws CoreException if an error occurs.
     */
    public void terminate() throws CoreException;

    /**
     * Adds the given IPS test run listener to the collection of listeners
     */
    public void addIpsTestRunListener(IIpsTestRunListener newListener);

    /**
     * Removes the given IPS test run listener from the collection of listeners
     */
    public void removeIpsTestRunListener(IIpsTestRunListener listener);

    /**
     * Returns all registered IPS test run listener.
     */
    public List<IIpsTestRunListener> getIpsTestRunListener();

    /**
     * Sets the java project the test which will be started by this runner belongs to.
     */
    public void setIpsProject(IIpsProject ipsProject);

    /**
     * Returns the IPS project.
     */
    public IIpsProject getIpsProject();

    /**
     * Starts a new job for running tests.
     * 
     * @param classpathRepository the repository where the test are selected from
     * @param testPackage the package including the tests
     * 
     * @throws CoreException if an error occurred.
     */
    public void startTestRunnerJob(String classpathRepository, String testPackage) throws CoreException;

    /**
     * Starts a new job for running tests.
     * 
     * @param classpathRepository the repository where the test are selected from
     * @param testPackage the package including the tests
     * @param mode The mode to run the test with @see
     *            org.eclipse.debug.core.ILaunchManager#DEBUG_MODE/RUN_MODE
     * @param launch An existing launch to run/debug the test runner with
     * 
     * @throws CoreException if an error occurred.
     */
    public void startTestRunnerJob(String classpathRepository, String testPackage, String mode, ILaunch launch)
            throws CoreException;

    /**
     * Returns <code>true</code> if a new test can be started.
     */
    public boolean isRunningTestRunner();

}
