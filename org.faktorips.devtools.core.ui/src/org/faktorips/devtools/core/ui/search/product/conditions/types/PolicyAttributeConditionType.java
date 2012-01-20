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

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.runtime.IProductComponent;

/**
 * A condition for product relevant {@link IAttribute IAttributes} of a {@link IPolicyCmptType}.
 * <p>
 * The condition tests, whether the argument matches the {@link IValueSet}, which is defined in the
 * {@link IProductComponent} for the product relevant Attribute of the {@link IPolicyCmptType}.
 * <p>
 * The conditions only uses the {@link ValueSetSearchOperatorType AllowanceSearchOperatorTypes}
 * 
 * @author dicker
 */
public class PolicyAttributeConditionType extends AbstractAttributeConditionType {

    private static final class PolicyAttributeConditionOperandProvider implements IOperandProvider {

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
    public List<IIpsElement> getSearchableElements(IProductCmptType productCmptType) {
        List<IIpsElement> policyCmptTypeAttributes = new ArrayList<IIpsElement>();

        IPolicyCmptType policyCmptType;
        try {
            policyCmptType = productCmptType.findPolicyCmptType(productCmptType.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        if (policyCmptType == null) {
            return Collections.emptyList();
        }

        List<IAttribute> attributes;
        try {
            attributes = policyCmptType.findAllAttributes(policyCmptType.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        for (IAttribute attribute : attributes) {
            if (!(attribute instanceof IPolicyCmptTypeAttribute)) {
                continue;
            }
            IPolicyCmptTypeAttribute policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)attribute;
            if (policyCmptTypeAttribute.getProductCmptPropertyType() == ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE
                    && policyCmptTypeAttribute.isProductRelevant()) {
                policyCmptTypeAttributes.add(policyCmptTypeAttribute);
            }
        }

        return policyCmptTypeAttributes;
    }

    @Override
    public List<? extends ISearchOperatorType> getSearchOperatorTypes(IIpsElement searchableElement) {
        ValueSetSearchOperatorType[] values = ValueSetSearchOperatorType.values();
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
}
