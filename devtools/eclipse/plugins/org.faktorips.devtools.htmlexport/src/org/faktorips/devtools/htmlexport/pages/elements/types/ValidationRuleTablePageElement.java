/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.values.LocalizedString;

/**
 * Represents a table with the {@link IValidationRule}s of an {@link IPolicyCmptType} as rows and
 * the attributes of the {@link IPolicyCmptType} as columns
 * 
 * @author dicker
 * 
 */
public class ValidationRuleTablePageElement extends AbstractIpsObjectPartsContainerTablePageElement<IValidationRule> {

    /**
     * Creates a {@link ValidationRuleTablePageElement} for the specified {@link IPolicyCmptType}
     * 
     */
    public ValidationRuleTablePageElement(IPolicyCmptType policyCmptType, DocumentationContext context) {
        super(policyCmptType.getValidationRules(), context);
        setId(policyCmptType.getName() + "_validationrules"); //$NON-NLS-1$
    }

    @Override
    protected List<IPageElement> createRowWithIpsObjectPart(IValidationRule rule) {
        List<String> ruleData = new ArrayList<>();

        ruleData.add(getContext().getLabel(rule));
        ruleData.add(rule.getMessageCode());
        ruleData.add(rule.getMessageSeverity().getId());
        String value = getLocalizedStringValue(rule);
        ruleData.add(value == null ? "" : value); //$NON-NLS-1$

        ruleData.add(StringUtils.join(rule.getValidatedAttributes(), '\n'));
        ruleData.add(rule.isConfigurableByProductComponent() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
        ruleData.add(rule.isActivatedByDefault() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
        ruleData.add(getContext().getDescription(rule));

        return Arrays.asList(new PageElementUtils(getContext()).createTextPageElements(ruleData));

    }

    protected String getLocalizedStringValue(IValidationRule rule) {
        IInternationalString messageText = rule.getMessageText();
        String emptyRule = ""; //$NON-NLS-1$

        LocalizedString localizedString = messageText.get(getContext().getDocumentationLocale());
        String value = localizedString.getValue();
        return value == null ? emptyRule : value;
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = new ArrayList<>();

        headline.add(getContext().getMessage(HtmlExportMessages.ValidationRuleTablePageElement_headlineName));
        headline.add(getContext().getMessage(HtmlExportMessages.ValidationRuleTablePageElement_headlineMessageCode));
        headline.add(
                getContext().getMessage(HtmlExportMessages.ValidationRuleTablePageElement_headlineMessageSeverity));
        headline.add(getContext().getMessage(HtmlExportMessages.ValidationRuleTablePageElement_headlineMessageText));
        headline.add(getContext().getMessage(
                HtmlExportMessages.ValidationRuleTablePageElement_headlineValidatedAttributes));

        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.ValidationRuleTablePageElement_headlineProductRelevant),
                Style.CENTER);

        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.ValidationRuleTablePageElement_headlineActivatedByDefault),
                Style.CENTER);

        headline.add(getContext().getMessage(HtmlExportMessages.ValidationRuleTablePageElement_headlineDescription));

        return headline;
    }
}
