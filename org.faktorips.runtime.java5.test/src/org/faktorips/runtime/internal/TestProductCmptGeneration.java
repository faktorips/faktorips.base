/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.List;
import java.util.Map;

import org.faktorips.runtime.FormulaExecutionException;
import org.w3c.dom.Element;

/**
 * ProductComponentGeneration for testing purposes.
 * 
 * @author Jan Ortmann
 */
public class TestProductCmptGeneration extends ProductComponentGeneration {

    public TestProductCmptGeneration(ProductComponent productCmpt) {
        super(productCmpt);
    }

    @Override
    protected void doInitPropertiesFromXml(Map<String, Element> map) {
        // only for test
    }

    @Override
    protected void doInitReferencesFromXml(Map<String, List<Element>> map) {
        // only for test
    }

    public int computeTestFormula(int parameter_1, String parameter_2) throws FormulaExecutionException {
        return (Integer)getFormulaEvaluator().evaluate("computeTestFormula", parameter_1, parameter_2);
    }

}
