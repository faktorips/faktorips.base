/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
    }

}
