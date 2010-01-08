package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TableRowPageElement;

/**
 * Tabelle zur Darstellung von Validierungsregeln
 * @author dicker
 *
 */
public class ValidationRuleTablePageElement extends AbstractSpecificTablePageElement {
	protected IPolicyCmptType policyCmptType;
	
	public ValidationRuleTablePageElement(IPolicyCmptType policyCmptType) {
		super();
		this.policyCmptType = policyCmptType;
	}

	@Override
	protected void addDataRows() {
		IValidationRule[] rules = policyCmptType.getRules();
		for (IValidationRule rule : rules) {
			addValidationRule(rule);
		}
	}

	protected void addValidationRule(IValidationRule rule) {
		List<String> ruleData = new ArrayList<String>();

		ruleData.add(rule.getName());
		ruleData.add(rule.getMessageCode());
		ruleData.add(rule.getMessageSeverity().getName());
		ruleData.add(rule.getMessageText());
		ruleData.add(StringUtils.join(rule.getValidatedAttributes(), '\n'));
		ruleData.add(rule.getDescription());

		subElements.add(new TableRowPageElement(PageElementUtils.createTextPageElements(ruleData)));
		
	}

	@Override
	protected List<String> getHeadline() {
		List<String> headline = new ArrayList<String>();
		
		headline.add(IValidationRule.PROPERTY_NAME);
		headline.add(IValidationRule.PROPERTY_MESSAGE_CODE);
		headline.add(IValidationRule.PROPERTY_MESSAGE_SEVERITY);
		headline.add(IValidationRule.PROPERTY_MESSAGE_TEXT);
		headline.add(IValidationRule.PROPERTY_VALIDATED_ATTRIBUTES);
		headline.add(IValidationRule.PROPERTY_DESCRIPTION);
		
		return headline;
	}

}
