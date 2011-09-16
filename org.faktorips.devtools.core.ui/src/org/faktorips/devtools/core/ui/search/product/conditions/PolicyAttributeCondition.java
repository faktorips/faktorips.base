/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.product.conditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

public class PolicyAttributeCondition extends AbstractAttributeCondition {

    public static class PolicyAttributeConditionOperandProvider implements IOperandProvider {

        private final IAttribute attribute;

        public PolicyAttributeConditionOperandProvider(IAttribute attribute) {
            this.attribute = attribute;
        }

        @Override
        public Object getSearchOperand(IProductCmptGeneration productComponentGeneration) {
            IConfigElement configElement = productComponentGeneration.getConfigElement(attribute.getName());

            return configElement.getValueSet();
        }

    }

    @Override
    public List<? extends IIpsElement> getSearchableElements(IProductCmptType productCmptType) throws CoreException {
        List<IPolicyCmptTypeAttribute> policyCmptTypeAttributes = new ArrayList<IPolicyCmptTypeAttribute>();

        IPolicyCmptType policyCmptType = productCmptType.findPolicyCmptType(productCmptType.getIpsProject());
        if (policyCmptType == null) {
            return Collections.emptyList();
        }

        List<IAttribute> attributes = policyCmptType.findAllAttributes(policyCmptType.getIpsProject());

        for (IAttribute attribute : attributes) {
            if (!(attribute instanceof IPolicyCmptTypeAttribute)) {
                continue;
            }
            IPolicyCmptTypeAttribute policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)attribute;
            if (policyCmptTypeAttribute.getProductCmptPropertyType() == ProductCmptPropertyType.DEFAULT_VALUE_AND_VALUESET
                    && policyCmptTypeAttribute.isProductRelevant()) {
                policyCmptTypeAttributes.add(policyCmptTypeAttribute);
            }
        }

        return policyCmptTypeAttributes;
    }

    @Override
    public List<? extends ISearchOperatorType> getSearchOperatorTypes(IIpsElement elementPart) {
        AllowanceSearchOperatorType[] values = AllowanceSearchOperatorType.values();
        return Arrays.asList(values);
    }

    @Override
    public IOperandProvider createOperandProvider(IIpsElement elementPart) {
        IAttribute attribute = (IAttribute)elementPart;
        return new PolicyAttributeConditionOperandProvider(attribute);
    }

    @Override
    public String getName() {
        return Messages.PolicyAttributeCondition_conditionName;
    }

    @Override
    public String getNoSearchableElementsMessage(IProductCmptType productCmptType) {
        return NLS.bind(Messages.PolicyAttributeCondition_noSearchableElementMessage,
                productCmptType.getLabel(new MultiLanguageSupport().getLocalizationLocale()));
    }

}
