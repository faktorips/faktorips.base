/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import java.util.List;

import org.faktorips.devtools.model.pctype.IValidationRule;

/**
 * Container for {@link IValidationRuleConfig IValidationRuleConfigs}.
 * 
 * @since 3.22
 */
public interface IValidationRuleConfigContainer {

    /**
     * Returns the number of validation rules defined (or configured respectively) in this
     * generation.
     */
    int getNumOfValidationRules();

    /**
     * Returns the validation with the given name if defined in this generation. Returns
     * {@code null} if no validation rule with the given name can be found or if the given name is
     * {@code null}.
     */
    IValidationRuleConfig getValidationRuleConfig(String validationRuleName);

    /**
     * Returns the validation rules defined in this generation. Returns an empty array if this
     * generation does not configure any validation rules.
     */
    List<IValidationRuleConfig> getValidationRuleConfigs();

    /**
     * Creates a new validation rule that configures the given {@link IValidationRule}. If signature
     * is {@code null} the validation rule configuration is still created, but no reference to an
     * {@link IValidationRule} is set.
     */
    IValidationRuleConfig newValidationRuleConfig(IValidationRule ruleToBeConfigured);

}
