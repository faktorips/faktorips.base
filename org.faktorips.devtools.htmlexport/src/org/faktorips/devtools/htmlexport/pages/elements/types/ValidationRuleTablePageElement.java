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
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRule;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

/**
 * Represents a table with the {@link ValidationRule}s of an {@link IPolicyCmptType} as rows and the
 * attributes of the {@link IPolicyCmptType} as columns
 * 
 * @author dicker
 * 
 */
public class ValidationRuleTablePageElement extends AbstractSpecificTablePageElement {
    protected IPolicyCmptType policyCmptType;

    /**
     * Creates a {@link ValidationRuleTablePageElement} for the specified {@link IPolicyCmptType}
     * 
     * @param policyCmptType
     */
    public ValidationRuleTablePageElement(IPolicyCmptType policyCmptType) {
        super();
        this.policyCmptType = policyCmptType;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#
     * addDataRows()
     */
    @Override
    protected void addDataRows() {
        IValidationRule[] rules = policyCmptType.getRules();
        for (IValidationRule rule : rules) {
            addValidationRule(rule);
        }
    }

    /**
     * adds a row for the given validation rule
     * 
     * @param rule
     */
    protected void addValidationRule(IValidationRule rule) {
        List<String> ruleData = new ArrayList<String>();

        ruleData.add(rule.getName());
        ruleData.add(rule.getMessageCode());
        ruleData.add(rule.getMessageSeverity().getName());
        ruleData.add(rule.getMessageText());
        ruleData.add(StringUtils.join(rule.getValidatedAttributes(), '\n'));
        ruleData.add(rule.getDescription());

        addSubElement(new TableRowPageElement(PageElementUtils.createTextPageElements(ruleData)));

    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#
     * getHeadline()
     */
    @Override
    protected List<String> getHeadline() {
        List<String> headline = new ArrayList<String>();

        headline.add(Messages.ValidationRuleTablePageElement_headlineName);
        headline.add(Messages.ValidationRuleTablePageElement_headlineMessageCode);
        headline.add(Messages.ValidationRuleTablePageElement_headlineMessageSeverity);
        headline.add(Messages.ValidationRuleTablePageElement_headlineMessageText);
        headline.add(Messages.ValidationRuleTablePageElement_headlineValidatedAttributes);
        headline.add(Messages.ValidationRuleTablePageElement_headlineDescription);

        return headline;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.DataPageElement#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return ArrayUtils.isEmpty(policyCmptType.getRules());
    }

}
