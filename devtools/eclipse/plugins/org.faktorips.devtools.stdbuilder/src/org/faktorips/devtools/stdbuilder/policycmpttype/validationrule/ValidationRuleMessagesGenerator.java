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

import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.builder.propertybuilder.AbstractLocalizedProperties;
import org.faktorips.devtools.model.builder.propertybuilder.AbstractPropertiesGenerator;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.pctype.IValidationRuleMessageText;
import org.faktorips.values.LocalizedString;

public class ValidationRuleMessagesGenerator extends AbstractPropertiesGenerator {

    public ValidationRuleMessagesGenerator(AFile messagesPropertiesFile, ISupportedLanguage supportedLanguage,
            ValidationRuleMessagesPropertiesBuilder builder) {
        super(messagesPropertiesFile, supportedLanguage, builder, new ValidationRuleMessageProperties(
                supportedLanguage.isDefaultLanguage()));
    }

    public void generate(IPolicyCmptType ipsObject) {
        IPolicyCmptType policyCmptType = ipsObject;
        deleteMessagesForDeletedRules(policyCmptType);
        addValidationRuleMessages(policyCmptType);
    }

    void addValidationRuleMessages(IPolicyCmptType policyCmptType) {
        addValidationRuleMessages(policyCmptType, getValidationMessages());
    }

    /**
     * Getting the message text from {@link IValidationRule} and convert the replace parameters to
     * match java {@link MessageFormat}
     * 
     * @param validationRule The validationRule holding the message text
     * @return the text of validationRule with converted replacement parameters
     */
    String getMessageText(IValidationRule validationRule) {
        IValidationRuleMessageText internationalString = validationRule.getMessageText();
        LocalizedString localizedString = internationalString.get(getLocale());
        String messageText = localizedString.getValue();
        StringBuilder result = new StringBuilder();

        Matcher matcher = IValidationRuleMessageText.REPLACEMENT_PARAMETER_REGEXT.matcher(messageText);
        int lastEnd = 0;
        LinkedHashSet<String> parameterNames = internationalString.getReplacementParameters();
        while (matcher.find()) {
            result.append(messageText.substring(lastEnd, matcher.start()));
            String parameterName = matcher.group();
            Integer argumentIndex = getIndexOf(parameterName, parameterNames);
            result.append(argumentIndex);
            lastEnd = matcher.end();
        }
        result.append(messageText.substring(lastEnd));
        return result.toString();
    }

    private Integer getIndexOf(String parameterName, LinkedHashSet<String> parameterNames) {
        int i = 0;
        for (String string : parameterNames) {
            if (string.equals(parameterName)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * Getting the name of the policy component type, a list of rules, stored in this policy
     * component types and a list of ruleNames that were stored in the type during last build. This
     * method deletes the message for every rule that is located in the ruleNameSet but not in the
     * list of validation rules.
     * 
     * @param policyCmptType The name of the policy component type used to get the message key
     */
    void deleteMessagesForDeletedRules(IPolicyCmptType policyCmptType) {
        deleteMessagesForDeletedParts(policyCmptType.getQualifiedNameType(), createLocalizedProperties(policyCmptType));
    }

    protected AbstractLocalizedProperties createLocalizedProperties(IPolicyCmptType pcType) {
        ValidationRuleMessageProperties validationRuleMessageProperties = new ValidationRuleMessageProperties(
                getSupportedLanguage().isDefaultLanguage());
        addValidationRuleMessages(pcType, validationRuleMessageProperties);
        return validationRuleMessageProperties;
    }

    private void addValidationRuleMessages(IPolicyCmptType policyCmptType,
            ValidationRuleMessageProperties validationRuleMessageProperties) {
        List<IValidationRule> validationRules = policyCmptType.getValidationRules();
        for (IValidationRule validationRule : validationRules) {
            addValidationRuleMessage(validationRule, validationRuleMessageProperties);
        }
    }

    private void addValidationRuleMessage(IValidationRule validationRule,
            ValidationRuleMessageProperties validationRuleMessageProperties) {
        String messageText = getMessageText(validationRule);
        validationRuleMessageProperties.put(validationRule, messageText);
    }

    /**
     * @return Returns the validationMessages.
     */
    public ValidationRuleMessageProperties getValidationMessages() {
        return (ValidationRuleMessageProperties)getLocalizedProperties();
    }

}
