/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype;

import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;

import org.faktorips.devtools.model.internal.InternationalString;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.pctype.IValidationRuleMessageText;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.LocalizedString;

public class ValidationRuleMessageText extends InternationalString implements IValidationRuleMessageText {

    public ValidationRuleMessageText() {
        super();
    }

    public ValidationRuleMessageText(PropertyChangeListener listener) {
        super(listener);
    }

    @Override
    public LinkedHashSet<String> getReplacementParameters() {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (LocalizedString localizedString : values()) {
            String text = localizedString.getValue();
            result.addAll(extractParameters(text));
        }
        return result;
    }

    public void validateReplacementParameters(IIpsProject ipsProject, MessageList list) {
        LocalizedString defaultLocaleString = get(ipsProject.getReadOnlyProperties().getDefaultLanguage().getLocale());
        LinkedHashSet<String> defaultLocalParameters = extractParameters(defaultLocaleString.getValue());

        for (LocalizedString localizedString : values()) {
            if (localizedString.equals(defaultLocaleString)) {
                continue;
            }
            LinkedHashSet<String> thisLocaleParameters = extractParameters(localizedString.getValue());
            for (String param : thisLocaleParameters) {
                if (defaultLocalParameters.add(param)) {
                    list.add(new Message(IValidationRule.MSGCODE_MESSAGE_TEXT_PARAMETER_INVALID, MessageFormat.format(
                            Messages.ValidationRuleMessageText_warning_invalidParameter, param, localizedString
                                    .getLocale().getDisplayLanguage()),
                            Message.WARNING));
                }
            }
        }
    }

    private LinkedHashSet<String> extractParameters(String text) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        Matcher matcher = REPLACEMENT_PARAMETER_REGEXT.matcher(text);
        while (matcher.find()) {
            String parameterName = matcher.group();
            result.add(parameterName);
        }
        return result;
    }

}
