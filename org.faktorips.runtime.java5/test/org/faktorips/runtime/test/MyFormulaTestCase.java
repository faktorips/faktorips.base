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
    public void executeAsserts(IpsTestResult result) throws Exception {
        for (int i = 0; i < failures; i++) {
            fail("test", "failure", result, "dummy", "formulaTest", "Tested failure");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void executeBusinessLogic() throws Exception {
    }
    
    public void addDummyFailures(int failures){
        this.failures += failures;
    }
}
