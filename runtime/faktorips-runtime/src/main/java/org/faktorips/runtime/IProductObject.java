/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.Date;
import java.util.TimeZone;

import org.faktorips.runtime.internal.DateTime;

/**
 * Common interface for {@link IProductComponent} and {@link IProductComponentGeneration}.
 */
public interface IProductObject extends IProductComponentLinkSource {

    String PROPERTY_VALID_FROM = "validFrom";

    /**
     * Creates a new policy component that is configured by this product component generation. After
     * creating the policy component it is automatically initialized. The new policy component is
     * not added to any parent structure.
     *
     * @throws RuntimeException if this product component does not configure a policy component.
     */
    IConfigurableModelObject createPolicyComponent();

    /**
     * Returns the date from which this generation is valid.
     *
     * @return The valid from date of this generation
     */
    DateTime getValidFrom();

    /**
     * Returns the point in time this generation is valid from in the given time zone. This method
     * never returns <code>null</code>.
     *
     * @throws NullPointerException if zone is <code>null</code>.
     */
    Date getValidFrom(TimeZone zone);

    /**
     * Returns whether the validation rule with the given name is configured as active in this
     * {@link IProductObject}. If there is no configuration for the given rule, <code>false</code>
     * is returned.
     * <p>
     * Please be aware that only one of {@link IProductComponent} or
     * {@link IProductComponentGeneration} can configure any given rule depending on its
     * changing-over-time configuration.
     *
     * @param ruleName the name of the rule in question
     * @return <code>true</code> if the rule was activated, <code>false</code> else.
     * @since 3.22
     */
    boolean isValidationRuleActivated(String ruleName);

    /**
     * Enables or disables validation for a specific rule.
     *
     * @param ruleName the name of the rule in question
     * @param active indicating whether the validation rule is configured as active
     * @throws IllegalRepositoryModificationException if the {@link IRuntimeRepository} containing
     *             this {@link IProductObject} is not {@link IRuntimeRepository#isModifiable()
     *             modifiable}
     * @since 3.22
     */
    void setValidationRuleActivated(String ruleName, boolean active);

}
