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

import org.faktorips.runtime.IRuntimeRepository;

/**
 * Abstract class for all ips test runner implementation.
 *
 * @author Joerg Ortmann
 */
public abstract class AbstractIpsTestRunner implements IpsTestListener {

    // Contains the repository packages names
    private String repositoryPackages;

    // Contains additional repository packages to obtain objects during runtime
    private String additionalRepositoryPackages;

    // Contains the created repository packages
    private List<IRuntimeRepository> repositories;

    private ClassLoader classLoader = getClass().getClassLoader();

    // contains the cached test suite
    private IpsTestSuite testSuite = null;

    public AbstractIpsTestRunner() {
        // nothing to do
    }

    /**
     * Counts all ips test cases in the given packages.<br>
     * The format of the input string is:<br>
     *
     * <pre>
     * {packageName in repository1}{packageName in repository2}{...}
     * </pre>
     *
     * @throws Exception if an error occurs.
     */
    public int countTests(String names) throws Exception {
        IpsTest2 test = getTest(names);
        if (test == null) {
            return 0;
        }

        if (test instanceof IpsTestSuite testSuite) {
            return testSuite.countTestCases();
        } else {
            return 1;
        }
    }

    /**
     * Returns all tests.
     */
    public List<IpsTest2> getTests() {
        if (testSuite == null) {
            return List.of();
        }

        return testSuite.getTests();
    }

    /**
     * Returns all stored repositories.<br>
     * If the repositories currently not exists in memory create the repositories first.<br>
     * The repository will be created by the to be implemented method:
     * <code>createRepositories()</code>
     *
     * @throws Exception if an error occurs.
     */
    protected List<IRuntimeRepository> getRepositories() throws Exception {
        if (repositories == null) {
            repositories = createRepositories();
        }
        return repositories;
    }

    protected ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Sets the classloader to find the test and instantiate the corresponding classes.
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Sets the repository packages names
     */
    public void setRepositoryPackages(String repositoryPackages) {
        this.repositoryPackages = repositoryPackages;
    }

    /**
     * Returns the repository packages names
     */
    public String getRepositoryPackages() {
        return repositoryPackages;
    }

    /**
     * Sets additional packages, will be used to create the ClassloaderRuntimeRepository which could
     * be used in the runtime environment to obtain necessary objects. Format:
     * {package1}{package2}{...}{packageN}
     */
    protected void setAdditionalRepositoryPackages(String additionalRepositoryPackages) {
        this.additionalRepositoryPackages = additionalRepositoryPackages;
    }

    /**
     * Run the all ips test cases in the given packages.<br>
     * The format of the input string is:<br>
     *
     * <pre>
     * {packageName in repository1}{packageName in repository2}{...}
     * </pre>
     *
     * @throws Exception if an error occurs.
     */
    public void run(String names) throws Exception {
        IpsTest2 test = getTest(names);
        IpsTestResult result = new IpsTestResult();
        result.addListener(this);
        result.run(test);
    }

    /**
     * Returns a list of repository names from a given string of repositories.<br>
     * The format of the input string is:<br>
     *
     * <pre>
     * {repositoryName1}{repositoryName2}{...}
     * </pre>
     */
    protected List<String> getRepositoryListFromInputString(String repositoryPackages) {
        return extractListFromString(repositoryPackages);
    }

    /*
     * Extract a list from the given string. Format: {elem1}{elem2}{...}{elem3}
     */
    public static List<String> extractListFromString(String input) {
        ArrayList<String> repositoryNameList = new ArrayList<>();
        if (input != null) {
            String restOfInput = input;
            while (restOfInput.length() > 0) {
                repositoryNameList.add(restOfInput.substring(restOfInput.indexOf("{") + 1, restOfInput.indexOf("}")));
                restOfInput = restOfInput.substring(restOfInput.indexOf("}") + 1);
            }
        }
        return repositoryNameList;
    }

    /*
     * Pack as String from the given list. Format: {elem1}{elem2}{...}{elem3}
     */
    public static String toStringFromList(List<String> input) {
        String result = "";
        for (String element : input) {
            result += "{" + element + "}";
        }
        return result;
    }

    /*
     * Returns the first name. format: {name1}{name2}{...}{namen}
     */
    private String getFirstTest(String names) {
        String name = "";
        if (names.indexOf("{") >= 0) {
            name = names.substring(names.indexOf("{") + 1, names.indexOf("}"));
        }
        return name;
    }

    /*
     * Returns the names without the first name. format: in: {name1}{name2}{...}{namen}, out:
     * {name2}{...}{namen}
     */
    private String getNextNames(String names) {
        return names.substring(names.indexOf("}") + 1);
    }

    /*
     * Returns an ips test object containing all tests inside the given package.
     *
     * @params names contains the names in all repositories, format: {<name in repository1>}{<name
     * in repository2>}{<...>}...
     */
    private IpsTest2 getTest(String names) throws Exception {
        if (testSuite != null) {
            // return the cached test object
            return testSuite;
        }
        List<String> repositoryNameList = getRepositoryListFromInputString(repositoryPackages);

        // create the ClassloaderRuntimeRepository repository which could be used in runtime to
        // obtain objects which are used in the test case
        // note: only one classloader repository will be created, it depends on the given ips
        // project this repository is the same for all test cases which itself are searched in
        // different repositories (see below)
        List<String> runtimePackages = extractListFromString(additionalRepositoryPackages);

        IRuntimeRepository additionalRepositories = TocHierarchyCreator
                .createRuntimeRepository(runtimePackages, getClassLoader());

        // creates the root suite which contains all tests,
        // necessary to show the correct hierarchy tree in the JUnit view
        testSuite = new IpsTestSuite("root");

        // create and get the repositories for searching the tests
        String nextNames = names;
        List<IRuntimeRepository> createdRepositories = getRepositories();
        for (int i = 0; i < createdRepositories.size(); i++) {
            // add additional runtime repositories if given
            if (additionalRepositories != null) {
                if (!runtimePackages.contains(repositoryNameList.get(i))) {
                    additionalRepositories.addDirectlyReferencedRepository(createdRepositories.get(i));
                }
                // else: the package which will be used to search the tests is already included in
                // the list of additional repositories
            }

            // search for tests in the repository
            (testSuite).addTest(createdRepositories.get(i).getIpsTest(getFirstTest(nextNames), additionalRepositories));
            nextNames = getNextNames(nextNames);
        }

        return testSuite;
    }

    /**
     * Creates the repositories. Where the test will be searched.
     */
    protected abstract List<IRuntimeRepository> createRepositories() throws Exception;

}
