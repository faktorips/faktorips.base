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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.faktorips.values.ObjectUtil;

import junit.framework.TestCase;

/**
 * Ips test case adapter. Adapter between JUnit test cases and ips test cases.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestCaseJUnitAdapter extends TestCase implements IpsTestListener {

    /*
     * Format of the failure entries
     */
    private static final String FAILUREFORMAT_FAILUREIN = "Failure in: \"{0}\"";
    private static final String FAILUREFORMAT_OBJECT = ", Object: \"{0}\"";
    private static final String FAILUREFORMAT_ATTRIBUTE = ", Attribute: \"{0}\".";
    private static final String FAILUREFORMAT_EXPECTED = ", expected: \"{0}\"";
    private static final String FAILUREFORMAT_ACTUAL = " but was: \"{0}\"";

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
        // if no ips test case found then suppress this test,
        // the test is only executable if an ips test case was given
        if (ipsTestCase == null) {
            return;
        }

        IpsTestResult ipsTestResult = new IpsTestResult();
        ipsTestResult.addListener(this);
        ipsTestCase.run(ipsTestResult);

        if (failures.size() > 0) {
            StringBuilder sb = new StringBuilder(failures.size() * 40);
            for (IpsTestFailure failure : failures) {
                if (failure.isError()) {
                    throw failure.getThrowable();
                } else {
                    if (sb.length() > 0) {
                        sb.append(System.lineSeparator());
                    }
                    sb.append(failureToString(failure));
                }
            }
            fail(sb.toString());
        }
    }

    /*
     * Creates a string representing the given ips test failure.
     */
    private String failureToString(IpsTestFailure failure) {
        StringBuilder failureMessage = new StringBuilder();
        failureMessage.append(MessageFormat.format(FAILUREFORMAT_FAILUREIN,
                failure.getTestCase() != null ? failure.getTestCase().getQualifiedName() : null));
        appendFormatted(failureMessage, FAILUREFORMAT_EXPECTED, failure.getExpectedValue());
        appendFormatted(failureMessage, FAILUREFORMAT_ACTUAL, failure.getActualValue());
        appendFormatted(failureMessage, FAILUREFORMAT_OBJECT, failure.getTestObject());
        appendFormatted(failureMessage, FAILUREFORMAT_ATTRIBUTE, failure.getTestedAttribute());
        return failureMessage.toString();
    }

    private void appendFormatted(StringBuilder sb, String pattern, Object value) {
        if (value == null) {
            sb.append(MessageFormat.format(pattern, "<null>"));
        } else if (ObjectUtil.isNullObject(value)) {
            sb.append(MessageFormat.format(pattern, value.toString()));
        } else {
            sb.append(MessageFormat.format(pattern, value));
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
