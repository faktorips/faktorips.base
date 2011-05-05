/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
        this.ruleName = name;
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
     *         otherwise.
     */
    public boolean isActive() {
        return active;
    }

}
