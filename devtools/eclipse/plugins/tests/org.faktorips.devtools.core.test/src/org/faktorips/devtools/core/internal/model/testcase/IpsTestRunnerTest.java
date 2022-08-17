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

import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

public class IpsTestRunnerTest extends AbstractIpsPluginTest {

    @Test
    public void testValidateTestCaseName() {
        assertContainsMessage(null, IpsTestRunner.validateTestCaseName("testCase"));
        assertContainsMessage(IpsTestRunner.INVALID_NAME, IpsTestRunner.validateTestCaseName("test,Case"));
        assertContainsMessage(IpsTestRunner.INVALID_NAME, IpsTestRunner.validateTestCaseName("test[Case"));
        assertContainsMessage(IpsTestRunner.INVALID_NAME, IpsTestRunner.validateTestCaseName("test]Case"));
        assertContainsMessage(IpsTestRunner.INVALID_NAME, IpsTestRunner.validateTestCaseName("test{Case"));
        assertContainsMessage(IpsTestRunner.INVALID_NAME, IpsTestRunner.validateTestCaseName("test}Case"));
        assertContainsMessage(IpsTestRunner.INVALID_NAME, IpsTestRunner.validateTestCaseName("test:Case"));
    }

    private void assertContainsMessage(String message, MessageList ml) {
        if (message == null) {
            assertTrue(!ml.containsErrorMsg());
        } else {
            assertTrue(ml.getMessageByCode(message) != null);
        }
    }
}
