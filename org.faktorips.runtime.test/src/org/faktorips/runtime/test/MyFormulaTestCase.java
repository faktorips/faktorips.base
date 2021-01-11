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

/**
 * Test class to test the assert of formula tests.
 * 
 * @author Joerg Ortmann
 */
public class MyFormulaTestCase extends IpsFormulaTestCase {

    private int failures = 0;

    public MyFormulaTestCase(String qName) {
        super(qName);
    }

    @Override
    public void executeAsserts(IpsTestResult result) throws Exception {
        for (int i = 0; i < failures; i++) {
            fail("test", "failure", result, "dummy", "formulaTest", "Tested failure");
        }
    }

    @Override
    public void executeBusinessLogic() throws Exception {
        // do nothing
    }

    public void addDummyFailures(int failures) {
        this.failures += failures;
    }

}
