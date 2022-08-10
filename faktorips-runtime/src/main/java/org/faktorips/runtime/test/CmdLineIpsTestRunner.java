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

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepository;

/**
 * Command line test runner. Writes the result of the test run to the command line.
 * 
 * @author Joerg Ortmann
 */
public class CmdLineIpsTestRunner extends AbstractIpsTestRunner {

    /**
     * @param repositoryPackages package name of the classpath repository
     */
    public CmdLineIpsTestRunner(String repositoryPackages) {
        super();
        setRepositoryPackages(repositoryPackages);
    }

    /**
     * The entry point for the command line test runner. The arguments are: args[0]: package name of
     * the classpath repository args[1]: Name of the testsuite to run args[2]: additional classpath
     * repositories (to find objects in the runtime environment)
     */
    public static void main(String[] args) {
        CmdLineIpsTestRunner runner = new CmdLineIpsTestRunner(args[0]);
        try {
            runner.run(args[1]);
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            e.printStackTrace();
        }
    }

    @Override
    protected List<IRuntimeRepository> createRepositories() throws ParserConfigurationException {
        List<String> repositoryNameList = getRepositoryListFromInputString(getRepositoryPackages());
        List<IRuntimeRepository> runtimeRepositories = new ArrayList<>(repositoryNameList.size());
        for (String repositoryName : repositoryNameList) {
            runtimeRepositories.add(ClassloaderRuntimeRepository.create(repositoryName, getClassLoader()));
        }
        return runtimeRepositories;
    }

    @Override
    public void testStarted(IpsTest2 test) {
        System.out.println("Test " + test.getQualifiedName() + " started.");
    }

    @Override
    public void testFinished(IpsTest2 test) {
        System.out.println("Test " + test.getQualifiedName() + " finished.");
    }

    @Override
    public void testFailureOccured(IpsTestFailure failure) {
        System.out.println(
                "Test failed. Expected " + failure.getExpectedValue() + " but was " + failure.getActualValue());
    }

}
