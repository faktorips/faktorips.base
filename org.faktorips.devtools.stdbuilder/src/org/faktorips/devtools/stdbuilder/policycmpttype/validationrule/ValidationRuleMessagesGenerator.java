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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.IValidationRuleMessageText;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;
import org.faktorips.devtools.stdbuilder.policycmpttype.validationrule.ValidationRuleMessageProperties.RuleKeyParts;
import org.faktorips.values.LocalizedString;

public class ValidationRuleMessagesGenerator {

    private final ValidationRuleMessageProperties validationMessages = new ValidationRuleMessageProperties();

    private final IFile messagesPropertiesFile;

    private final ISupportedLanguage supportedLanguage;

    private final ValidationRuleMessagesPropertiesBuilder builder;

    public ValidationRuleMessagesGenerator(IFile messagesPropertiesFile, ISupportedLanguage supportedLanguage,
            ValidationRuleMessagesPropertiesBuilder builder) {
        this.messagesPropertiesFile = messagesPropertiesFile;
        this.supportedLanguage = supportedLanguage;
        this.builder = builder;
        try {
            if (messagesPropertiesFile.exists()) {
                getValidationMessages().load(messagesPropertiesFile.getContents());
            }
        } catch (CoreException e) {
            StdBuilderPlugin.log(e);
        }
    }

    /**
     * Saving the properties to the file adding the given comment. The file must already exists.
     * 
     * @param comment The comment for the properties file
     * @return true if file was modified otherwise false
     * @throws CoreException in case of any exception during writing to file
     */
    public boolean saveIfModified(String comment) throws CoreException {
        if (getValidationMessages().isModified()) {
            IFile file = getMessagesPropertiesFile();
            if (!file.exists()) {
                file.create(new ByteArrayInputStream("".getBytes()), true, null); //$NON-NLS-1$
                file.setDerived(builder.buildsDerivedArtefacts()
                        && builder.getBuilderSet().isMarkNoneMergableResourcesAsDerived());
            }
            storeMessagesToFile(file, getValidationMessages(), comment);
            return true;
        } else {
            return false;
        }
    }

    public void clearMessages() {
        getValidationMessages().clear();
    }

    private void storeMessagesToFile(IFile propertyFile, ValidationRuleMessageProperties messages, String comments)
            throws CoreException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        messages.store(outputStream, comments);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        builder.writeToFile(propertyFile, inputStream, true, true);
    }

    public void loadMessages() throws CoreException {
        if (messagesPropertiesFile.exists()) {
            getValidationMessages().load(messagesPropertiesFile.getContents());
        } else {
            getValidationMessages().clear();
        }
    }

    /**
     * @return Returns the messagesPropertiesFile.
     */
    public IFile getMessagesPropertiesFile() {
        return messagesPropertiesFile;
    }

    public void generate(IPolicyCmptType ipsObject) {
        IPolicyCmptType policyCmptType = ipsObject;
        deleteMessagesForDeletedRules(policyCmptType);
        List<IValidationRule> validationRules = policyCmptType.getValidationRules();
        for (IValidationRule validationRule : validationRules) {
            addValidationRuleMessage(validationRule);
        }
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
        HashSet<RuleKeyParts> existingKeys = new HashSet<RuleKeyParts>(
                validationMessages.getKeysForPolicyCmptType(policyCmptType.getQualifiedName()));
        for (RuleKeyParts ruleNameAndKey : existingKeys) {
            if (policyCmptType.getValidationRule(ruleNameAndKey.getRuleName()) == null) {
                validationMessages.remove(ruleNameAndKey);
            }
        }
    }

    public void deleteAllMessagesFor(String pcTypeName) {
        validationMessages.deleteAllMessagesFor(pcTypeName);
    }

    void addValidationRuleMessage(IValidationRule validationRule) {
        String messageText = getMessageText(validationRule);
        if (messageText.isEmpty() && !supportedLanguage.isDefaultLanguage()) {
            return;
        }
        getValidationMessages().put(validationRule, messageText);
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
        LocalizedString localizedString = internationalString.get(supportedLanguage.getLocale());
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
     * @return Returns the validationMessages.
     */
    public ValidationRuleMessageProperties getValidationMessages() {
        return validationMessages;
    }

}
