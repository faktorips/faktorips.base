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

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.runtime.model.type.ValidationRule;

public interface IValidationRuleConfig extends IPropertyValue {

    public static final String TAG_NAME = "ValidationRuleConfig"; //$NON-NLS-1$

    public static final String TAG_NAME_ACTIVE = "active"; //$NON-NLS-1$
    public static final String TAG_NAME_RULE_NAME = "ruleName"; //$NON-NLS-1$

    public static final String PROPERTY_ACTIVE = "active"; //$NON-NLS-1$

    /**
     * Returns whether the configured {@link IValidationRule} is active.
     * 
     * @return <code>true</code> if the rule is active, <code>false</code> otherwise.
     */
    public boolean isActive();

    /**
     * Defines whether the {@link IValidationRule} configured by this {@link IValidationRuleConfig}
     * is active.
     */
    public void setActive(boolean active);

    /**
     * Returns the {@link IValidationRule} which is configured by this {@link IValidationRuleConfig}
     * . Returns <code>null</code> if no validation rule can be found.
     * <p>
     * This method searches the super type hierarchy.
     * 
     * @throws IpsException if an error occurs while searching.
     */
    public IValidationRule findValidationRule(IIpsProject ipsProject) throws IpsException;

    /**
     * Sets the new name of the referenced {@link ValidationRule}
     */
    public void setValidationRuleName(String validationRuleName);

    /**
     * Returns the name of the referenced {@link ValidationRule}
     */
    public String getValidationRuleName();

    /**
     * Overrides {@link IPropertyValue#findTemplateProperty(IIpsProject)} to return co-variant
     * {@code IValidationRuleConfig}.
     * 
     * @see IPropertyValue#findTemplateProperty(IIpsProject)
     */
    @Override
    public IValidationRuleConfig findTemplateProperty(IIpsProject ipsProject);
}
