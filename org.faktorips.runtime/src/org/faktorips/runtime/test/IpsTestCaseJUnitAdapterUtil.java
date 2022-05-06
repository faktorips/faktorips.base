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
import java.util.List;

import org.faktorips.values.ObjectUtil;

/**
 * Utility class for the adapter between ips test cases and JUnit tests.
 */
public class IpsTestCaseJUnitAdapterUtil {

    /*
     * Format of the failure entries
     */
    private static final String FAILUREFORMAT_FAILUREIN = "Failure in: \"{0}\"";
    private static final String FAILUREFORMAT_OBJECT = ", Object: \"{0}\"";
    private static final String FAILUREFORMAT_ATTRIBUTE = ", Attribute: \"{0}\".";
    private static final String FAILUREFORMAT_EXPECTED = ", expected: \"{0}\"";
    private static final String FAILUREFORMAT_ACTUAL = " but was: \"{0}\"";

    private IpsTestCaseJUnitAdapterUtil() {
        // util class
    }

    /**
     * Runs the ips test case.
     */
    public static void runTestCase(IpsTestCaseBase ipsTestCase, IpsTestListener listener) {
        // if no ips test case found then suppress this test,
        // the test is only executable if an ips test case was given
        if (ipsTestCase == null) {
            return;
        }

        IpsTestResult ipsTestResult = new IpsTestResult();
        ipsTestResult.addListener(listener);
        ipsTestCase.run(ipsTestResult);
    }

    /**
     * Creates failure entries.
     */
    public static String createFailureEntries(List<IpsTestFailure> failures) {
        StringBuilder sb = new StringBuilder(failures.size() * 40);
        for (IpsTestFailure failure : failures) {
            if (failure.isError()) {
                throw new RuntimeException(failure.getThrowable());
            } else {
                if (sb.length() > 0) {
                    sb.append(System.lineSeparator());
                }
                sb.append(failureToString(failure));
            }
        }
        return sb.toString();
    }

    /**
     * Creates a string representing the given ips test failure.
     */
    private static String failureToString(IpsTestFailure failure) {
        StringBuilder failureMessage = new StringBuilder();
        failureMessage.append(MessageFormat.format(FAILUREFORMAT_FAILUREIN,
                failure.getTestCase() != null ? failure.getTestCase().getQualifiedName() : null));
        appendFormatted(failureMessage, FAILUREFORMAT_EXPECTED, failure.getExpectedValue());
        appendFormatted(failureMessage, FAILUREFORMAT_ACTUAL, failure.getActualValue());
        appendFormatted(failureMessage, FAILUREFORMAT_OBJECT, failure.getTestObject());
        appendFormatted(failureMessage, FAILUREFORMAT_ATTRIBUTE, failure.getTestedAttribute());
        return failureMessage.toString();
    }

    private static void appendFormatted(StringBuilder sb, String pattern, Object value) {
        if (value == null) {
            sb.append(MessageFormat.format(pattern, "<null>"));
        } else if (ObjectUtil.isNullObject(value)) {
            sb.append(MessageFormat.format(pattern, value.toString()));
        } else {
            sb.append(MessageFormat.format(pattern, value));
        }
    }
}
