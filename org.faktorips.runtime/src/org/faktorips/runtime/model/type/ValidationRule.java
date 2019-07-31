/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.type;

import org.faktorips.runtime.Severity;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsValidationRule;

public class ValidationRule extends TypePart {

    private final IpsValidationRule validationRuleAnnotation;

    public ValidationRule(Type type, IpsValidationRule validationRuleAnnotation,
            IpsExtensionProperties extensionProperties) {
        super(validationRuleAnnotation.name(), type, extensionProperties);
        this.validationRuleAnnotation = validationRuleAnnotation;
    }

    /**
     * Returns the name of the validation rule
     */
    @Override
    public String getName() {
        return validationRuleAnnotation.name();
    }

    /**
     * Returns the message code
     */
    public String getMsgCode() {
        return validationRuleAnnotation.msgCode();
    }

    /**
     * Returns the {@link Severity}
     */
    public Severity getSeverity() {
        return validationRuleAnnotation.severity();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("@IpsValidationRule");
        sb.append("(");
        sb.append(getName());
        sb.append(", ");
        sb.append(getMsgCode());
        sb.append(", ");
        sb.append(getSeverity());
        sb.append(")");
        return sb.toString();
    }
}
