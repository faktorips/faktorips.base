/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import java.util.Optional;

import org.faktorips.runtime.Severity;
import org.faktorips.runtime.model.annotation.IpsConfiguredValidationRule;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsValidationRule;

public class ValidationRule extends TypePart {

    private final IpsValidationRule validationRuleAnnotation;

    private final IpsConfiguredValidationRule validationConfigurationRule;

    public ValidationRule(Type type, IpsValidationRule validationRuleAnnotation,
            IpsConfiguredValidationRule validationConfigurationRule, IpsExtensionProperties extensionProperties, Optional<Deprecation> deprecation) {
        super(validationRuleAnnotation.name(), type, extensionProperties, deprecation);
        this.validationRuleAnnotation = validationRuleAnnotation;
        this.validationConfigurationRule = validationConfigurationRule;
    }

    /**
     * Returns whether this validation rule is changing over time.
     * 
     * @return <code>true</code> if this validation rule is changing over time, otherwise
     *         <code>false</code>
     */
    public boolean isChangingOverTime() {
        if (validationConfigurationRule != null) {
            return validationConfigurationRule.changingOverTime();
        }
        return false;
    }

    /**
     * Returns whether this validation rule is activated by default.
     * 
     * @return <code>true</code> if this validation rule is activated by default,otherwise
     *         <code>false</code>
     */
    public boolean isActivatedByDefault() {
        if (validationConfigurationRule != null) {
            return validationConfigurationRule.defaultActivated();
        }
        return false;
    }

    /**
     * Returns whether this validation rule can be configured by a product component.
     * 
     * @return <code>true</code> if this validation rule can be configured by a product component,
     *         otherwise <code>false</code>
     */
    public boolean isProductRelevant() {
        if (validationConfigurationRule != null) {
            return true;
        }
        return false;
    }

    /**
     * Returns the name of the validation rule.
     * 
     * @return the name of the validation rule
     */
    @Override
    public String getName() {
        return validationRuleAnnotation.name();
    }

    /**
     * Returns the message code of the validation rule.
     * 
     * @return the message code of the validation rule
     */
    public String getMsgCode() {
        return validationRuleAnnotation.msgCode();
    }

    /**
     * Returns the {@link Severity} of the validation rule.
     * 
     * @return {@link Severity} of the validation rule
     */
    public Severity getSeverity() {
        return validationRuleAnnotation.severity();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("@IpsValidationRule( name = '");
        sb.append(getName());
        sb.append("', msgCode = '");
        sb.append(getMsgCode());
        sb.append("', severity = '");
        sb.append(getSeverity());
        sb.append("')");
        if (isProductRelevant()) {
            sb.append("\n");
            sb.append("@IpsConfiguredValidationRule(changeOverTime = ");
            sb.append(isChangingOverTime());
            sb.append(", defaultActivated = ");
            sb.append(isActivatedByDefault());
            sb.append(")");
        }
        return sb.toString();
    }
}
