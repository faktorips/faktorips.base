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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Ips test case adapter. Adapter between JUnit test cases and ips test cases.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestCaseJUnitAdapter extends TestCase implements IpsTestListener {
    // The ips test case this adapter works with
    private IpsTestCaseBase ipsTestCase;

    // Contains all failures occurred during the test run for this test case
    private List<IpsTestFailure> failures = new ArrayList<IpsTestFailure>();

    /*
     * Format of the failure entries
     */
    private static final String FAILUREFORMAT_FAILUREIN = "Failure in: \"{0}\"";
    private static final String FAILUREFORMAT_OBJECT = ", Object: \"{1}\",";
    private static final String FAILUREFORMAT_ATTRIBUTE = ", Attribute: \"{2}\".";
    private static final String FAILUREFORMAT_EXPECTED = ", expected: \"{3}\"";
    private static final String FAILUREFORMAT_ACTUAL = " but was: \"{4}\"";

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
     * 
     * {@inheritDoc}
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
            StringBuffer failureBuffer = new StringBuffer(failures.size() * 40);
            for (IpsTestFailure failure : failures) {
                if (failure.isError()) {
                    throw failure.getThrowable();
                } else {
                    if (failureBuffer.length() > 0) {
                        failureBuffer.append(System.getProperty("line.separator"));
                    }
                    failureBuffer.append(failureToString(failure));
                }
            }
            fail(failureBuffer.toString());
        }
    }

    /*
     * Creates a string representing the given ips test failure.
     */
    private String failureToString(IpsTestFailure failure) {
        String failureFormat = FAILUREFORMAT_FAILUREIN;
        String failureActual = FAILUREFORMAT_ACTUAL;
        String failureExpected = FAILUREFORMAT_EXPECTED;
        String failureFormatAttribute = FAILUREFORMAT_ATTRIBUTE;
        String failureFormatObject = FAILUREFORMAT_OBJECT;

        List<String> failureDetails = new ArrayList<String>(5);
        failureDetails.add(failure.getTestCase() != null ? failure.getTestCase().getQualifiedName() : null);
        failureDetails.add(failure.getTestObject());
        failureDetails.add(failure.getTestedAttribute());
        failureDetails.add(failure.getExpectedValue() != null ? failure.getExpectedValue().toString() : null);
        failureDetails.add(failure.getActualValue() != null ? failure.getActualValue().toString() : null);

        if (failureDetails.size() > 3) {
            failureFormat = failureFormat + (failureDetails.get(2) != null ? failureExpected : "");
        }
        if (failureDetails.size() > 4) {
            failureFormat = failureFormat + (failureDetails.get(3) != null ? failureActual : "");
        }
        if (failureDetails.size() > 1) {
            failureFormat = failureFormat + (failureDetails.get(0) != null ? failureFormatObject : "");
        }
        if (failureDetails.size() > 2) {
            failureFormat = failureFormat + (failureDetails.get(1) != null ? failureFormatAttribute : "");
        }

        return MessageFormat.format(failureFormat, failureDetails.toArray());
    }

    /**
     * {@inheritDoc}
     */
    public void testStarted(IpsTest2 test) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void testFinished(IpsTest2 arg0) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void testFailureOccured(IpsTestFailure f) {
        failures.add(f);
    }
}
