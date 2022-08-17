/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype.validationrule;

import org.faktorips.devtools.model.pctype.IValidationRule;

public enum ValidationRuleIdentification {

    QUALIFIED_RULE_NAME {

        @Override
        public String getIdentifier(IValidationRule validationRule) {
            return validationRule.getQualifiedRuleName();
        }
    },

    MESSAGE_CODE {

        @Override
        public String getIdentifier(IValidationRule validationRule) {
            return validationRule.getMessageCode();
        }
    };

    public abstract String getIdentifier(IValidationRule validationRule);

}
