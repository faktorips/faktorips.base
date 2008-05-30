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

import org.w3c.dom.Element;

class MyTestCase extends IpsTestCase2 {

    private Object expectedValue;
    private Object actualValue;
    
    
    public MyTestCase(String qName) {
        super(qName);
    }

    public MyTestCase(String qName, Object expectedValue, Object actualValue) {
        super(qName);
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
    }

    /**
     * {@inheritDoc}
     */
    protected void initInputFromXml(Element inputEl) {
        
    }

    /**
     * {@inheritDoc}
     */
    protected void initExpectedResultFromXml(Element resultEl) {
        
    }

    /**
     * {@inheritDoc}
     */
    public void executeBusinessLogic() throws Exception {
        
    }

    /**
     * {@inheritDoc}
     */
    public void executeAsserts(IpsTestResult result) throws Exception {
        assertEquals(expectedValue, actualValue, result, "TestObject", "TestedAttribute");
    }
}