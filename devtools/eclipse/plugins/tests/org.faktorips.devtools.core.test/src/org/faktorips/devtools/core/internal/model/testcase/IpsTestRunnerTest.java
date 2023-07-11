/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import static org.faktorips.testsupport.IpsMatchers.containsNoErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.junit.Test;

public class IpsTestRunnerTest extends AbstractIpsPluginTest {

    @Test
    public void testValidateTestCaseName() {
        assertThat(IpsTestRunner.validateTestCaseName("testCase"), containsNoErrorMessage());
        assertThat(IpsTestRunner.validateTestCaseName("test,Case"), hasMessageCode(IpsTestRunner.INVALID_NAME));
        assertThat(IpsTestRunner.validateTestCaseName("test[Case"), hasMessageCode(IpsTestRunner.INVALID_NAME));
        assertThat(IpsTestRunner.validateTestCaseName("test]Case"), hasMessageCode(IpsTestRunner.INVALID_NAME));
        assertThat(IpsTestRunner.validateTestCaseName("test{Case"), hasMessageCode(IpsTestRunner.INVALID_NAME));
        assertThat(IpsTestRunner.validateTestCaseName("test}Case"), hasMessageCode(IpsTestRunner.INVALID_NAME));
        assertThat(IpsTestRunner.validateTestCaseName("test:Case"), hasMessageCode(IpsTestRunner.INVALID_NAME));
    }
}
