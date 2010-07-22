/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeAsserts(IpsTestResult result) throws Exception {
        for (int i = 0; i < failures; i++) {
            fail("test", "failure", result, "dummy", "formulaTest", "Tested failure");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeBusinessLogic() throws Exception {
        // do nothing
    }

    public void addDummyFailures(int failures) {
        this.failures += failures;
    }
}
