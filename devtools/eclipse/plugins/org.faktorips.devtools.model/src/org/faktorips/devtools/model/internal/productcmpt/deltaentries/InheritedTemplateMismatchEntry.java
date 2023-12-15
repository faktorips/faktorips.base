/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import java.text.MessageFormat;
import java.util.function.Function;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.type.IProductCmptProperty;

/**
 * A property value should inherit its configuration from a template but the configuration differs.
 *
 * @since 24.1.1
 */
public class InheritedTemplateMismatchEntry extends AbstractDeltaEntryForProperty {

    private IProductCmptProperty property;
    private IPropertyValue propertyValue;
    private IPropertyValue templatePropertyValue;
    private Object internalValue;
    private Object templateValue;
    private PropertyValueType propertyValueType;

    public InheritedTemplateMismatchEntry(IProductCmptProperty property, IPropertyValue propertyValue,
            IPropertyValue templatePropertyValue, Object internalValue, Object templateValue) {
        super(propertyValue);
        this.property = property;
        this.propertyValue = propertyValue;
        this.templatePropertyValue = templatePropertyValue;
        this.internalValue = internalValue;
        this.templateValue = templateValue;
        propertyValueType = propertyValue.getPropertyValueType();
    }

    @Override
    public void fix() {
        propertyValueType.copyValue(templatePropertyValue, propertyValue);
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.INHERITED_TEMPLATE_MISMATCH;
    }

    @Override
    public String getDescription() {
        Function<Object, String> valueToString = propertyValueType.getValueToString();
        return MessageFormat.format(Messages.InheritedTemplateMismatchEntry_desc,
                IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(property),
                valueToString.apply(internalValue),
                valueToString.apply(templateValue));
    }

}
