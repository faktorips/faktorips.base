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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Ips test suite adapter. Adapter between JUnit test suites and ips test suites.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestSuiteJUnitAdapter extends TestSuite {

    public IpsTestSuiteJUnitAdapter() {
        super();
    }

    public IpsTestSuiteJUnitAdapter(String name) {
        super(name);
    }

    public IpsTestSuiteJUnitAdapter(IpsTestSuite suite) {
        super(suite.getName());
        for (IpsTest2 ipsTest : suite.getTests()) {
            addTest(createJUnitTest(ipsTest));
        }
    }

    /**
     * Dummy test method to avoid a warning when running all JUnit tests in this project. Without
     * this method, Eclpise's JUnit support would create a warning that his suite hasn't got any
     * tests.
     */
    public void testDummy() {
        // do nothing
    }

    /**
     * Creates an adapter test for the given ips test.
     */
    public static Test createJUnitTest(IpsTest2 ipsTest) {
        if (ipsTest instanceof IpsTestCaseBase) {
            return new IpsTestCaseJUnitAdapter((IpsTestCaseBase)ipsTest);
        } else if (ipsTest instanceof IpsTestSuite) {
            return new IpsTestSuiteJUnitAdapter((IpsTestSuite)ipsTest);
        } else {
            throw new RuntimeException("Unknown type " + ipsTest.getClass());
        }
    }

}
