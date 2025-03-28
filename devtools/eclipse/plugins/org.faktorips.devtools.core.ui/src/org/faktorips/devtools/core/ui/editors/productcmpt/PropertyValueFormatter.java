/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.function.Function;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.inputformat.AnyValueSetFormat;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Formats the value of {@link IPropertyValue PropertyValues}. e.g. formats the value represented by
 * the {@link IValueHolder} of an {@link IAttributeValue} according to data type and current locale.
 */
public class PropertyValueFormatter {

    public static final Function<IAttributeValue, String> ATTRIBUTE_VALUE = attributeValue -> attributeValue != null
            ? AttributeValueFormatter.format(attributeValue)
            : IpsStringUtils.EMPTY;

    public static final Function<IConfiguredValueSet, String> CONFIGURED_VALUESET = configuredValueSet -> configuredValueSet != null
            ? AnyValueSetFormat.newInstance(configuredValueSet).format(configuredValueSet.getValueSet())
            : IpsStringUtils.EMPTY;

    public static final Function<IConfiguredDefault, String> CONFIGURED_DEFAULT = configuredDefault -> {
        UIDatatypeFormatter datatypeFormatter = IpsUIPlugin.getDefault().getDatatypeFormatter();
        return configuredDefault != null ? datatypeFormatter.formatValue(
                configuredDefault.findValueDatatype(configuredDefault.getIpsProject()),
                configuredDefault.getValue()) : IpsStringUtils.EMPTY;
    };

    public static final Function<ITableContentUsage, String> TABLE_CONTENT_USAGE = tableContentUsage -> tableContentUsage != null
            ? getValueOrNullPresentation(tableContentUsage.getTableContentName())
            : IpsStringUtils.EMPTY;

    public static final Function<IFormula, String> FORMULA = formula -> formula != null
            ? getValueOrNullPresentation(formula.getExpression())
            : IpsStringUtils.EMPTY;
    public static final Function<IValidationRuleConfig, String> VALIDATION_RULE_CONFIG = ruleConfig -> {
        if (ruleConfig == null) {
            return IpsStringUtils.EMPTY;
        }
        if (ruleConfig.isActive()) {
            return Messages.ValidationRuleConfigEditComposite_activated;
        } else {
            return Messages.ValidationRuleConfigEditComposite_deactivated;
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
        return switch (propertyValue.getPropertyValueType()) {
            case ATTRIBUTE_VALUE -> ATTRIBUTE_VALUE.apply((IAttributeValue)propertyValue);
            case CONFIGURED_VALUESET -> CONFIGURED_VALUESET.apply((IConfiguredValueSet)propertyValue);
            case CONFIGURED_DEFAULT -> CONFIGURED_DEFAULT.apply((IConfiguredDefault)propertyValue);
            case TABLE_CONTENT_USAGE -> TABLE_CONTENT_USAGE.apply((ITableContentUsage)propertyValue);
            case FORMULA -> FORMULA.apply((IFormula)propertyValue);
            case VALIDATION_RULE_CONFIG -> VALIDATION_RULE_CONFIG.apply((IValidationRuleConfig)propertyValue);
            case null, default -> throw new IllegalStateException(
                    PropertyValueFormatter.class.getName() + ": Unknown property value type " //$NON-NLS-1$
                            + propertyValue.getPropertyValueType());
        };
    }

    private static String getValueOrNullPresentation(String value) {
        if (IpsStringUtils.isEmpty(value)) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        } else {
            return value;
        }
    }

}
