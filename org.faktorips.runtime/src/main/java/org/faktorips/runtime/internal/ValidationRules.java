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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Container for the {@link ValidationRuleConfiguration} map used by both {@link IProductComponent}
 * and {@link IProductComponentGeneration}.
 * 
 * @since 3.22
 */
class ValidationRules {

    private static final String XML_ELEMENT_VALIDATION_RULE_CONFIG = "ValidationRuleConfig";

    private final Map<String, ValidationRuleConfiguration> validationRulesConfigsByName = new LinkedHashMap<>();
    private final IProductObject productObject;

    public ValidationRules(IProductObject productObject) {
        this.productObject = productObject;
    }

    private IRuntimeRepository getRepository() {
        return productObject.getRepository();
    }

    /**
     * Creates a map containing the validation rule configurations found in the indicated XML
     * element. For each validation rule configuration the map contains an entry with the rule name
     * as a key and a {@link ValidationRuleConfiguration} instance as value.
     * 
     * @param element an XML element containing a product component( generation)'s data
     * @throws NullPointerException if element is <code>null</code>.
     */
    void doInitValidationRuleConfigsFromXml(Element element) {
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && XML_ELEMENT_VALIDATION_RULE_CONFIG.equals(node.getNodeName())) {
                Element childElement = (Element)nl.item(i);
                ValidationRuleConfiguration config = new ValidationRuleConfiguration(childElement);
                validationRulesConfigsByName.put(config.getRuleName(), config);
            }
        }
    }

    void writeValidationRuleConfigsToXml(Element element) {
        for (Entry<String, ValidationRuleConfiguration> vRuleEntry : validationRulesConfigsByName.entrySet()) {
            Element vRuleElement = element.getOwnerDocument().createElement(XML_ELEMENT_VALIDATION_RULE_CONFIG);
            vRuleElement.setAttribute("ruleName", vRuleEntry.getKey());
            ValidationRuleConfiguration vRule = vRuleEntry.getValue();
            vRuleElement.setAttribute("active", Boolean.toString(vRule.isActive()));
            element.appendChild(vRuleElement);
        }
    }

    /**
     * Enables or disables validation for a specific rule.
     * 
     * @param ruleName the name of the rule in question
     * @param active indicating whether the validation rule is configured as active
     * @throws IllegalRepositoryModificationException if the {@link IRuntimeRepository} containing
     *             this {@link IProductObject} is not {@link IRuntimeRepository#isModifiable()
     *             modifiable}
     */
    void setValidationRuleActivated(String ruleName, boolean active) {
        if (getRepository() != null && !getRepository().isModifiable()) {
            throw new IllegalRepositoryModificationException();
        }
        validationRulesConfigsByName.put(ruleName, new ValidationRuleConfiguration(ruleName, active));
    }

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
     */
    boolean isValidationRuleActivated(String ruleName) {
        ValidationRuleConfiguration ruleConfig = validationRulesConfigsByName.get(ruleName);
        return ruleConfig != null && ruleConfig.isActive();
    }

}
