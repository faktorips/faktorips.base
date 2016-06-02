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

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractLocalizedProperties;
import org.faktorips.devtools.stdbuilder.propertybuilder.PropertyKey;

public class ValidationRuleMessageProperties extends AbstractLocalizedProperties {

    public ValidationRuleMessageProperties(boolean defaultLang) {
        super(defaultLang);
    }

    @Override
    protected PropertyKey createPropertyEntry(String key) {
        return RuleKeyParts.create(key);
    }

    public void put(IValidationRule validationRule, String messageText) {
        String pcTypeName = validationRule.getIpsObject().getQualifiedName();
        String ruleName = validationRule.getName();
        String key = validationRule.getQualifiedRuleName();
        RuleKeyParts ruleKeyParts = new RuleKeyParts(pcTypeName, ruleName, key);
        put(ruleKeyParts, messageText);
    }

    @SuppressWarnings("unchecked")
    public Collection<RuleKeyParts> getKeysForPolicyCmptType(String qname) {
        return (Collection<RuleKeyParts>)getKeysForIpsObject(qname);
    }

    static class RuleKeyParts implements PropertyKey {

        private final String pcTypeName;

        private final String ruleName;

        private final String key;

        public RuleKeyParts(String pcTypeName, String ruleName, String key) {
            this.pcTypeName = pcTypeName;
            this.ruleName = ruleName;
            this.key = key;
        }

        public static RuleKeyParts create(String key) {
            String[] split = key.split(IValidationRule.QNAME_SEPARATOR);
            if (split.length == 2) {
                return new RuleKeyParts(split[0], split[1], key);
            } else {
                return null;
            }
        }

        public String getRuleName() {
            return ruleName;
        }

        @Override
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

        @Override
        public String getIpsObjectQname() {
            return pcTypeName;
        }

        @Override
        public String toString() {
            return key;
        }

    }

}
