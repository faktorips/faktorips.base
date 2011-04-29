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

    public IpsTestSuiteJUnitAdapter(IpsTestSuite suite) {
        super(suite.getName());
        for (IpsTest2 ipsTest : suite.getTests()) {
            addTest(createJUnitTest(ipsTest));
        }
    }

}
