/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.testrepository;

import java.util.List;
import java.util.Map;

import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.w3c.dom.Element;

public class PnCProductGen extends ProductComponentGeneration {

    private Decimal taxRate;
    private Money fixedCosts;

    public PnCProductGen(ProductComponent productCmpt) {
        super(productCmpt);
    }

    public Decimal getTaxRate() {
        return taxRate;
    }

    public Money getFixedCosts() {
        return fixedCosts;
    }

    @Override
    protected void doInitPropertiesFromXml(Map<String, Element> map) {
        Element taxRateElement = map.get("taxRate");
        taxRate = Decimal.valueOf(taxRateElement.getAttribute("value"));
        Element fixedCostsElement = map.get("fixedCosts");
        fixedCosts = Money.valueOf(fixedCostsElement.getAttribute("value"));
    }

    @Override
    protected void doInitReferencesFromXml(Map<String, List<Element>> map) {
        // do nothing
    }

}
