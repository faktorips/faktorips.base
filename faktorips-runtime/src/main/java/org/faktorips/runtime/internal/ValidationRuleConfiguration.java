/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import org.w3c.dom.Element;

/**
 * Class representing a validation rule configuration at runtime.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class ValidationRuleConfiguration {

    /**
     * The name of the configured validation rule
     */
    private final String ruleName;

    /**
     * Flag indicating whether the validation rule is configured as active.
     */
    private final boolean active;

    /**
     * Creates a {@link ValidationRuleConfiguration} with the given name and activation state.
     * 
     * @param name the name of the configured rule.
     * @param active a flag indicating whether this rule is configured as active or not.
     */
    public ValidationRuleConfiguration(String name, boolean active) {
        ruleName = name;
        this.active = active;
    }

    /**
     * Creates a {@link ValidationRuleConfiguration} and initializes it with the data provided by
     * the given element.
     * 
     * @param element the XML element containing the new validation rule configuration's state
     */
    public ValidationRuleConfiguration(Element element) {
        this(element.getAttribute("ruleName"), Boolean.parseBoolean(element.getAttribute("active")));
    }

    /**
     * @return the name of the configured validation rule
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * Flag indicating whether the validation rule is configured as active.
     * 
     * @return <code>true</code> if the validation rule is configured as active, <code>false</code>
     *             otherwise.
     */
    public boolean isActive() {
        return active;
    }

}
