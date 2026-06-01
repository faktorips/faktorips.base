/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.valueset.IntegerRange;
import org.w3c.dom.Element;

/**
 * ProductComponentGeneration for testing purposes.
 *
 * @author Jan Ortmann
 */
public class TestProductCmptGeneration extends ProductComponentGeneration {

    private final Map<String, IntegerRange> policyLinkCardinalities = new HashMap<>();

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

    @Override
    protected void doInitPolicyLinkCardinalitiesFromXml(Map<String, Element> cardinalityElements) {
        policyLinkCardinalities.clear();
        cardinalityElements.forEach(
                (association, element) -> policyLinkCardinalities.put(association, parseCardinalityRange(element)));
    }

    public Map<String, IntegerRange> getPolicyLinkCardinalities() {
        return policyLinkCardinalities;
    }

    public void setPolicyLinkCardinality(String association, IntegerRange cardinality) {
        policyLinkCardinalities.put(association, cardinality);
    }

    @Override
    protected void writePropertiesToXml(Element generationElement) {
        // no-op for test
    }

    @Override
    protected void writePolicyLinkCardinalitiesToXml(Element element) {
        policyLinkCardinalities.forEach((association, cardinality) -> {
            Element cardinalityElement = element.getOwnerDocument().createElement("PolicyLinkCardinality");
            cardinalityElement.setAttribute("association", association);
            cardinalityElement.setAttribute("minCardinality", Integer.toString(cardinality.getLowerBound()));
            String max = cardinality.getUpperBound() == Integer.MAX_VALUE
                    ? "*"
                    : Integer.toString(cardinality.getUpperBound());
            cardinalityElement.setAttribute("maxCardinality", max);
            element.appendChild(cardinalityElement);
        });
    }

    @Override
    public void doInitFormulaFromXml(Element genElement) {
        super.doInitFormulaFromXml(genElement);
    }

    public int computeTestFormula(int parameter1, String parameter2) throws FormulaExecutionException {
        return (Integer)getFormulaEvaluator().evaluate("computeTestFormula", parameter1, parameter2);
    }

}
