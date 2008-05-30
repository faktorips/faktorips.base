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
 * Class to execute and test formulas on product cmpts.
 * 
 * @author Joerg Ortmann
 */
public abstract class IpsFormulaTestCase extends IpsTestCaseBase {
    
    public IpsFormulaTestCase(String qName) {
        super(qName);
    }

    /**
     * {@inheritDoc}
     */
    public int countTestCases() {
        return 1;
    }
}
