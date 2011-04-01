/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRule;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;

/**
 * Represents a table with the {@link ValidationRule}s of an {@link IPolicyCmptType} as rows and the
 * attributes of the {@link IPolicyCmptType} as columns
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
    protected List<? extends PageElement> createRowWithIpsObjectPart(IValidationRule rule) {
        List<String> ruleData = new ArrayList<String>();

        ruleData.add(rule.getName());
        ruleData.add(rule.getMessageCode());
        ruleData.add(rule.getMessageSeverity().getName());
        ruleData.add(rule.getMessageText());
        ruleData.add(StringUtils.join(rule.getValidatedAttributes(), '\n'));
        ruleData.add(getContext().getDescription(rule));

        return Arrays.asList(new PageElementUtils().createTextPageElements(ruleData));

    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = new ArrayList<String>();

        headline.add(getContext().getMessage(HtmlExportMessages.ValidationRuleTablePageElement_headlineName));
        headline.add(getContext().getMessage(HtmlExportMessages.ValidationRuleTablePageElement_headlineMessageCode));
        headline.add(getContext().getMessage(HtmlExportMessages.ValidationRuleTablePageElement_headlineMessageSeverity));
        headline.add(getContext().getMessage(HtmlExportMessages.ValidationRuleTablePageElement_headlineMessageText));
        headline.add(getContext().getMessage(
                HtmlExportMessages.ValidationRuleTablePageElement_headlineValidatedAttributes));
        headline.add(getContext().getMessage(HtmlExportMessages.ValidationRuleTablePageElement_headlineDescription));

        return headline;
    }
}
