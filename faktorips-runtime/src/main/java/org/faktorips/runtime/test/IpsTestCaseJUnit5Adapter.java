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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter between JUnit 5 test cases and Faktor-IPS test cases.
 */
public class IpsTestCaseJUnit5Adapter implements IpsTestListener {

    // The ips test case this adapter works with
    private IpsTestCaseBase ipsTestCase;

    // Contains all failures occurred during the test run for this test case
    private List<IpsTestFailure> failures = new ArrayList<>();

    public IpsTestCaseJUnit5Adapter(IpsTestCaseBase ipsTestCase) {
        this.ipsTestCase = ipsTestCase;
    }

    /**
     * Runs the ips test case.
     */
    public void runTest() {
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