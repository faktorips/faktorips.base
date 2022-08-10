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

import static org.faktorips.runtime.test.IpsTestCaseJUnitAdapterUtil.createFailureEntries;
import static org.faktorips.runtime.test.IpsTestCaseJUnitAdapterUtil.runTestCase;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Adapter between JUnit 3/4 test cases and Faktor-IPS test cases.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestCaseJUnitAdapter extends TestCase implements IpsTestListener {

    // The ips test case this adapter works with
    private IpsTestCaseBase ipsTestCase;

    // Contains all failures occurred during the test run for this test case
    private List<IpsTestFailure> failures = new ArrayList<>();

    public IpsTestCaseJUnitAdapter(IpsTestCaseBase ipsTestCase) {
        super(ipsTestCase.getName());
        this.ipsTestCase = ipsTestCase;
    }

    /**
     * Dummy constructor to meet the contract to JUnits test case, this prevents the JUnit warning
     * "has no public constructor TestCase(String name)". By using this constructor the runTest will
     * suppressed. To run this kind of test cases the constructor
     * IpsTestCaseJUnitAdapter(IpsTestCaseBase ipsTestCase) has to be called.
     */
    public IpsTestCaseJUnitAdapter(String name) {
        super(name);
    }

    /**
     * Dummy test do prevent JUnit "no test found" warning if selecting this test case.
     */
    public void testNothing() {
        // do nothing
    }

    /**
     * Runs the ips test case.
     */
    @Override
    public void runTest() throws Throwable {
        runTestCase(ipsTestCase, this);
        if (failures.size() > 0) {
            fail(createFailureEntries(failures));
        }
    }

    @Override
    public void testStarted(IpsTest2 test) {
        // nothing to do
    }

    @Override
    public void testFinished(IpsTest2 arg0) {
        // nothing to do
    }

    @Override
    public void testFailureOccured(IpsTestFailure f) {
        failures.add(f);
    }

}
