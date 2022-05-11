/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.formula.groovy;

import java.util.List;
import java.util.Map;

import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
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

    public int computeTestFormula(int parameter1, String parameter2) {
        return (Integer)getFormulaEvaluator().evaluate("computeTestFormula", parameter1, parameter2);
    }
}
