/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.testcase;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcase.IIpsTestRunListener;

/**
 * Specification of an IPS test runner.
 * 
 * @author Joerg Ortmann
 */
public interface IIpsTestRunner {

    /**
     * Terminates the currently launched test run process.
     * 
     * @throws IpsException if an error occurs.
     */
    public void terminate() throws IpsException;

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
     * @throws IpsException if an error occurred.
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
     * @throws IpsException if an error occurred.
     */
    public void startTestRunnerJob(String classpathRepository, String testPackage, String mode, ILaunch launch)
            throws CoreException;

    /**
     * Returns <code>true</code> if a new test can be started.
     */
    public boolean isRunningTestRunner();

}
