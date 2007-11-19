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

package org.faktorips.devtools.core.internal.model.testcase;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.util.message.MessageList;

public class IpsTestRunnerTest extends AbstractIpsPluginTest {

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
        if (message == null){
            assertTrue(!ml.containsErrorMsg());
        } else {
            assertTrue(ml.getMessageByCode(message) != null);
        }
    }
}
