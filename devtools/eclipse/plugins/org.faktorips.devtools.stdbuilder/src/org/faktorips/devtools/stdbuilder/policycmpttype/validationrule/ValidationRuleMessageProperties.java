/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

import java.util.Collection;
import java.util.Objects;

import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.pctype.IValidationRule;
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
    public Collection<PropertyKey> getKeysForPolicyCmptType(QualifiedNameType qualifiedNameType) {
        return (Collection<PropertyKey>)getKeysForIpsObject(qualifiedNameType);
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
            return Objects.hash(key, pcTypeName, ruleName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            RuleKeyParts other = (RuleKeyParts)obj;
            return Objects.equals(key, other.key)
                    && Objects.equals(pcTypeName, other.pcTypeName)
                    && Objects.equals(ruleName, other.ruleName);
        }

        @Override
        public QualifiedNameType getIpsObjectQNameType() {
            return new QualifiedNameType(pcTypeName, IpsObjectType.POLICY_CMPT_TYPE);
        }

        public String getIpsObjectQname() {
            return pcTypeName;
        }

        @Override
        public String toString() {
            return key;
        }

    }

}
