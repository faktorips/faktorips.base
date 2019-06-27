/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.productcmpt;

import com.google.common.base.Function;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.inputformat.AnyValueSetFormat;

/**
 * Formats the value of {@link IPropertyValue PropertyValues}. e.g. formats the value represented by
 * the {@link IValueHolder} of an {@link IAttributeValue} according to data type and current locale.
 */
public class PropertyValueFormatter {

    public static final Function<IAttributeValue, String> ATTRIBUTE_VALUE = new Function<IAttributeValue, String>() {
        @Override
        public String apply(IAttributeValue attributeValue) {
            return attributeValue != null ? AttributeValueFormatter.format(attributeValue) : StringUtils.EMPTY;
        }
    };

    public static final Function<IConfiguredValueSet, String> CONFIGURED_VALUESET = new Function<IConfiguredValueSet, String>() {

        @Override
        public String apply(IConfiguredValueSet configuredValueSet) {
            return configuredValueSet != null ? AnyValueSetFormat.newInstance(configuredValueSet).format(
                    configuredValueSet.getValueSet()) : StringUtils.EMPTY;
        }
    };

    public static final Function<IConfiguredDefault, String> CONFIGURED_DEFAULT = new Function<IConfiguredDefault, String>() {

        @Override
        public String apply(IConfiguredDefault configuredDefault) {
            UIDatatypeFormatter datatypeFormatter = IpsUIPlugin.getDefault().getDatatypeFormatter();
            return configuredDefault != null ? datatypeFormatter.formatValue(
                    configuredDefault.findValueDatatype(configuredDefault.getIpsProject()),
                    configuredDefault.getValue()) : StringUtils.EMPTY;
        }
    };

    public static final Function<ITableContentUsage, String> TABLE_CONTENT_USAGE = new Function<ITableContentUsage, String>() {

        @Override
        public String apply(ITableContentUsage tableContentUsage) {
            return tableContentUsage != null ? getValueOrNullPresentation(tableContentUsage.getTableContentName())
                    : StringUtils.EMPTY;
        }
    };

    public static final Function<IFormula, String> FORMULA = new Function<IFormula, String>() {

        @Override
        public String apply(IFormula formula) {
            return formula != null ? getValueOrNullPresentation(formula.getExpression()) : StringUtils.EMPTY;
        }

    };
    public static final Function<IValidationRuleConfig, String> VALIDATION_RULE_CONFIG = new Function<IValidationRuleConfig, String>() {

        @Override
        public String apply(IValidationRuleConfig ruleConfig) {
            if (ruleConfig == null) {
                return StringUtils.EMPTY;
            }
            if (ruleConfig.isActive()) {
                return Messages.ValidationRuleConfigEditComposite_activated;
            } else {
                return Messages.ValidationRuleConfigEditComposite_deactivated;
            }
        }
    };

    // Utility class that should not be instantiated
    private PropertyValueFormatter() {
        super();
    }

    /**
     * @return the formatted value of the provided {@link IPropertyValue}
     * @throws NullPointerException if property value is <code>null</code>.
     * @throws IllegalStateException if the {@link PropertyValueType} is unknown.
     */
    public static String format(IPropertyValue propertyValue) {
        if (propertyValue == null) {
            throw new NullPointerException("Cannot format null"); //$NON-NLS-1$
        }
        if (propertyValue.getPropertyValueType() == PropertyValueType.ATTRIBUTE_VALUE) {
            return ATTRIBUTE_VALUE.apply((IAttributeValue)propertyValue);
        } else if (propertyValue.getPropertyValueType() == PropertyValueType.CONFIGURED_VALUESET) {
            return CONFIGURED_VALUESET.apply((IConfiguredValueSet)propertyValue);
        } else if (propertyValue.getPropertyValueType() == PropertyValueType.CONFIGURED_DEFAULT) {
            return CONFIGURED_DEFAULT.apply((IConfiguredDefault)propertyValue);
        } else if (propertyValue.getPropertyValueType() == PropertyValueType.TABLE_CONTENT_USAGE) {
            return TABLE_CONTENT_USAGE.apply((ITableContentUsage)propertyValue);
        } else if (propertyValue.getPropertyValueType() == PropertyValueType.FORMULA) {
            return FORMULA.apply((IFormula)propertyValue);
        } else if (propertyValue.getPropertyValueType() == PropertyValueType.VALIDATION_RULE_CONFIG) {
            return VALIDATION_RULE_CONFIG.apply((IValidationRuleConfig)propertyValue);
        }
        throw new IllegalStateException(PropertyValueFormatter.class.getName()
                + ": Unknown property value type " + propertyValue.getPropertyValueType()); //$NON-NLS-1$
    }

    private static String getValueOrNullPresentation(String value) {
        if (StringUtils.isEmpty(value)) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        } else {
            return value;
        }
    }

}
