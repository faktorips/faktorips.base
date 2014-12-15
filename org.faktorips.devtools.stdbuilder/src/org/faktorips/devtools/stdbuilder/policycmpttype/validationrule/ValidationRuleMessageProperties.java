/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.MessagesProperties;
import org.faktorips.util.MultiMap;

public class ValidationRuleMessageProperties {

    private final MultiMap<String, RuleKeyParts> generatedMessages = MultiMap.createWithSetsAsValues();

    private final MessagesProperties messagesProperties = new MessagesProperties();

    /**
     * Default constructor creating a new {@link Properties} object.
     */
    public ValidationRuleMessageProperties() {
    }

    public void deleteAllMessagesFor(String policyCmptType) {
        Collection<RuleKeyParts> rules = generatedMessages.get(policyCmptType);
        for (RuleKeyParts rule : rules) {
            messagesProperties.remove(rule.getKey());
        }
        generatedMessages.remove(policyCmptType);
    }

    /**
     * Clear all existing elements and load new properties form stream.
     * 
     * @param stream The {@link InputStream} to load, @see {@link Properties#load(InputStream)}
     */
    public void load(InputStream stream) {
        messagesProperties.load(stream);
        initMessagesForPcTypes();
    }

    void initMessagesForPcTypes() {
        generatedMessages.clear();
        for (Object keyObject : messagesProperties.keySet()) {
            if (keyObject instanceof String) {
                String key = (String)keyObject;
                String[] split = key.split(IValidationRule.QNAME_SEPARATOR);
                if (split.length == 2) {
                    String pcType = split[0];
                    String ruleName = split[1];
                    generatedMessages.put(pcType, new RuleKeyParts(pcType, ruleName, key));
                }
            }
        }
    }

    public void store(OutputStream outputStream, String comments) {
        messagesProperties.store(outputStream, comments);
    }

    public void put(IValidationRule validationRule, String messageText) {
        String pcTypeName = validationRule.getIpsObject().getQualifiedName();
        String ruleName = validationRule.getName();
        String key = validationRule.getQualifiedRuleName();
        RuleKeyParts ruleKeyParts = new RuleKeyParts(pcTypeName, ruleName, key);
        generatedMessages.put(pcTypeName, ruleKeyParts);
        messagesProperties.put(key, messageText);
    }

    public void remove(RuleKeyParts ruleNameAndKey) {
        generatedMessages.remove(ruleNameAndKey.pcTypeName, ruleNameAndKey);
        messagesProperties.remove(ruleNameAndKey.key);
    }

    public int size() {
        return messagesProperties.size();
    }

    public void clear() {
        messagesProperties.clear();
        generatedMessages.clear();
    }

    public boolean isModified() {
        return messagesProperties.isModified();
    }

    Collection<RuleKeyParts> getKeysForPolicyCmptType(String pcTypeName) {
        return generatedMessages.get(pcTypeName);
    }

    String getMessage(String qualifiedRuleName) {
        return messagesProperties.getMessage(qualifiedRuleName);
    }

    static class RuleKeyParts {

        private final String pcTypeName;

        private final String ruleName;

        private final String key;

        public RuleKeyParts(String pcTypeName, String ruleName, String key) {
            this.pcTypeName = pcTypeName;
            this.ruleName = ruleName;
            this.key = key;
        }

        public String getRuleName() {
            return ruleName;
        }

        public String getKey() {
            return key;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((pcTypeName == null) ? 0 : pcTypeName.hashCode());
            result = prime * result + ((ruleName == null) ? 0 : ruleName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RuleKeyParts other = (RuleKeyParts)obj;
            return ObjectUtils.equals(key, other.key) && ObjectUtils.equals(pcTypeName, other.pcTypeName)
                    && ObjectUtils.equals(ruleName, other.ruleName);
        }

    }

}
