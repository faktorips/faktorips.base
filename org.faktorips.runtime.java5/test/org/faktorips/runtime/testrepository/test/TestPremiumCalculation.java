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

package org.faktorips.runtime.testrepository.test;

import org.faktorips.runtime.internal.XmlUtil;
import org.faktorips.runtime.test.IpsTestCase2;
import org.faktorips.runtime.test.IpsTestResult;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class TestPremiumCalculation extends IpsTestCase2 {

    private String inputSumInsured;
    private String expResultPremium;
    
    public TestPremiumCalculation(String qName) {
        super(qName);
    }

    /**
     * {@inheritDoc}
     */
    protected void initInputFromXml(Element inputEl) {
        Element el = XmlUtil.getFirstElement(inputEl);
        inputSumInsured = el.getAttribute("value");
    }

    /**
     * {@inheritDoc}
     */
    protected void initExpectedResultFromXml(Element resultEl) {
        Element el = XmlUtil.getFirstElement(resultEl);
        expResultPremium = el.getAttribute("value");
    }

    /**
     * @return Returns the expResultPremium.
     */
    public String getExpResultPremium() {
        return expResultPremium;
    }

    /**
     * @return Returns the inputSumInsured.
     */
    public String getInputSumInsured() {
        return inputSumInsured;
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
    }

    public void testDummy(){
    }
}
