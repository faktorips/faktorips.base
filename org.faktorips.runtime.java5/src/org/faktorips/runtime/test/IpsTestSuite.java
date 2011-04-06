/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.test;

import java.util.ArrayList;
import java.util.List;

/**
 * A test suite is a container for tests.
 * 
 * @author Jan Ortmann
 */
public class IpsTestSuite extends IpsTest2 {

    private List<IpsTest2> tests = new ArrayList<IpsTest2>();

    public IpsTestSuite(String qName) {
        super(qName);
    }

    /**
     * Adds the test to the suite.
     */
    public void addTest(IpsTest2 test) {
        tests.add(test);
    }

    /**
     * Removes the test from the suite.
     */
    public void removeTest(IpsTest2 test) {
        tests.remove(test);
    }

    /**
     * Returns all tests in the suite.
     */
    public List<IpsTest2> getTests() {
        return new ArrayList<IpsTest2>(tests);
    }

    /**
     * Returns the number of tests in the suite. Note that if this suite contains another suite this
     * method does not add the number of tests in this other suite to this suite's size.
     */
    public int size() {
        return tests.size();
    }

    @Override
    public int countTestCases() {
        int count = 0;
        for (IpsTest2 test : tests) {
            count += test.countTestCases();
        }
        return count;
    }

    /**
     * Returns the test with the given name. This method does not search recursively in suites that
     * are contained in this suite.
     * 
     * @param name The unqualified name of the test that identifies the test in this suite.
     */
    public IpsTest2 getTest(String name) {
        for (IpsTest2 test : tests) {
            if (test.getName().equals(name)) {
                return test;
            }
        }
        return null;
    }

    @Override
    public void run(IpsTestResult result) {
        for (IpsTest2 test : tests) {
            test.run(result);
        }
    }

    @Override
    public String toString() {
        return "TestSuite " + getQualifiedName();
    }

}
