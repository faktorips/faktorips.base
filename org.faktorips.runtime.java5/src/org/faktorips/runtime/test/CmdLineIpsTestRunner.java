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
     * The entry point for the command line test runner. The arguments are: args[0]: package name of
     * the classpath repository args[1]: Name of the testsuite to run args[2]: additional classpath
     * repositories (to find objects in the runtime environment)
     */
    public static void main(String[] args) {
        CmdLineIpsTestRunner runner = new CmdLineIpsTestRunner(args[0]);
        try {
            runner.run(args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param repositoryPackage package name of the classpath repository
     */
    public CmdLineIpsTestRunner(String repositoryPackages) {
        super();
        setRepositoryPackages(repositoryPackages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<IRuntimeRepository> createRepositories() throws ParserConfigurationException {
        List<String> repositoryNameList = getRepositoryListFromInputString(getRepositoryPackages());
        List<IRuntimeRepository> runtimeRepositories = new ArrayList<IRuntimeRepository>(repositoryNameList.size());
        for (String repositoryName : repositoryNameList) {
            runtimeRepositories.add(ClassloaderRuntimeRepository.create((String)repositoryName, classLoader));
        }
        return runtimeRepositories;
    }

    /**
     * {@inheritDoc}
     */
    public void testStarted(IpsTest2 test) {
        System.out.println("Test " + test.getQualifiedName() + " started.");
    }

    /**
     * {@inheritDoc}
     */
    public void testFinished(IpsTest2 test) {
        System.out.println("Test " + test.getQualifiedName() + " finished.");
    }

    /**
     * {@inheritDoc}
     */
    public void testFailureOccured(IpsTestFailure failure) {
        System.out.println("Test failed. Expected " + failure.getExpectedValue() + " but was "
                + failure.getActualValue());
    }

}
