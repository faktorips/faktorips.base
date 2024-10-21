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

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;

/**
 * Adapter between JUnit 5 test suites and Faktor-IPS test suites.
 * <p>
 * Code example to show how the adapter can be integrated:
 *
 * <pre>
 * <code>
 * import org.junit.jupiter.api.DynamicTest;
 * import org.junit.jupiter.api.TestFactory;
 *
 * public class HomeInsuranceJUnitTest extends IpsTestSuiteJUnit5Adapter {
 *      &#64;TestFactory
 *       public Stream&#60;DynamicTest&#62; getTests() {
 *           IRuntimeRepository repository = [...];
 *           return createTests(repository.getIpsTest(""));
 *      }
 * }
 * </code>
 * </pre>
 */
@SuppressWarnings("exports")
public class IpsTestSuiteJUnit5Adapter {

    public Stream<DynamicTest> createTests(IpsTest2 test) {
        if (test instanceof IpsTestCaseBase) {
            return Stream.of(createTest((IpsTestCase2)test));
        } else if (test instanceof IpsTestSuite testSuite) {
            return testSuite.getTests().stream().flatMap(this::createTests);
        } else {
            throw new RuntimeException("Unknown type " + test.getClass());
        }
    }

    public DynamicTest createTest(IpsTestCase2 testCase) {
        return dynamicTest(testCase.getName(), new IpsTestCaseJUnit5Adapter(testCase)::runTest);
    }

}
