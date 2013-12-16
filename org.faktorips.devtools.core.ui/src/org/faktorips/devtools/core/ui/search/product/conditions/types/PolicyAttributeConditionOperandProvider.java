/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.List;

import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.core.model.type.IAttribute;

final class PolicyAttributeConditionOperandProvider implements IOperandProvider {

    private final IAttribute attribute;

    public PolicyAttributeConditionOperandProvider(IAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public Object getSearchOperand(IProductPartsContainer productPartsContainer) {
        List<IConfigElement> configElements = productPartsContainer.getProductParts(IConfigElement.class);
        for (IConfigElement configElement : configElements) {
            if (configElement.getPolicyCmptTypeAttribute().equals(attribute.getName())) {
                return configElement.getValueSet();
            }
        }
        return null;
    }

}