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

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.LinkedHashSet;
import java.util.Observer;
import java.util.regex.Matcher;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.InternationalString;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.IValidationRuleMessageText;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class ValidationRuleMessageText extends InternationalString implements IValidationRuleMessageText {

    public ValidationRuleMessageText() {
        super();
    }

    public ValidationRuleMessageText(Observer observer) {
        super(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LinkedHashSet<String> getReplacementParameters() {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        for (ILocalizedString localizedString : values()) {
            String text = localizedString.getValue();
            result.addAll(extractParameters(text));
        }
        return result;
    }

    public void validateReplacementParameters(IIpsProject ipsProject, MessageList list) {
        ILocalizedString defaultLocaleString = get(ipsProject.getProperties().getDefaultLanguage().getLocale());
        if (defaultLocaleString == null) {
            return;
        }
        LinkedHashSet<String> defaultLocalParameters = extractParameters(defaultLocaleString.getValue());

        for (ILocalizedString localizedString : values()) {
            if (localizedString.equals(defaultLocaleString)) {
                continue;
            }
            LinkedHashSet<String> thisLocaleParameters = extractParameters(localizedString.getValue());
            for (String param : thisLocaleParameters) {
                if (defaultLocalParameters.add(param)) {
                    list.add(new Message(IValidationRule.MSGCODE_MESSAGE_TEXT_PARAMETER_INVALID, NLS.bind(
                            Messages.ValidationRuleMessageText_warning_invalidParameter, param, localizedString
                                    .getLocale().getDisplayLanguage()), Message.WARNING));
                }
            }
        }
    }

    private LinkedHashSet<String> extractParameters(String text) {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        Matcher matcher = REPLACEMENT_PARAMETER_REGEXT.matcher(text);
        while (matcher.find()) {
            String parameterName = matcher.group();
            result.add(parameterName);
        }
        return result;
    }

}
