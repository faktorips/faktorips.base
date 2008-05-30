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

package org.faktorips.runtime.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Ips test suite adapter. Adapter between JUnit test suites and ips test suites.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestSuiteJUnitAdapter extends TestSuite {
    /**
     * Creates an adapter test for the given ips test.
     */
    public static Test createJUnitTest(IpsTest2 ipsTest) {
        if (ipsTest instanceof IpsTestCaseBase) {
            return new IpsTestCaseJUnitAdapter((IpsTestCaseBase)ipsTest);
        } else if (ipsTest instanceof IpsTestSuite) {
            return new IpsTestSuiteJUnitAdapter((IpsTestSuite)ipsTest);
        } else{
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
