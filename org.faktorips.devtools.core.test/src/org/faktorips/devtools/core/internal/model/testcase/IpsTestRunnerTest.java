/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
        if (message == null) {
            assertTrue(!ml.containsErrorMsg());
        } else {
            assertTrue(ml.getMessageByCode(message) != null);
        }
    }
}
