package org.faktorips.devtools.htmlexport.generators.html.objects;

import java.util.List;

import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;

public class PolicyCmptContentPageElement extends AbstractTypeContentPageElement<PolicyCmptType> {

	PolicyCmptContentPageElement(PolicyCmptType object) {
		super(object);
	}
	
	@Override
	protected List<String> createAttributesHeadline() {
		List<String> attributesHeadline = super.createAttributesHeadline();

		attributesHeadline.add(PolicyCmptTypeAttribute.PROPERTY_PRODUCT_RELEVANT);
		attributesHeadline.add(PolicyCmptTypeAttribute.PROPERTY_ATTRIBUTE_TYPE);
		attributesHeadline.add(PolicyCmptTypeAttribute.PROPERTY_OVERWRITES);
		
		return attributesHeadline;
	}

	@Override
	protected List<String> createAttributeValueLine(IAttribute attribute) {
		List<String> attributesValueLine = super.createAttributeValueLine(attribute);

		PolicyCmptTypeAttribute polAttribute = (PolicyCmptTypeAttribute) attribute;
		
		attributesValueLine.add(polAttribute.isProductRelevant() ? "X" : "-");
		attributesValueLine.add(polAttribute.getAttributeType().getName());
		attributesValueLine.add(polAttribute.isOverwrite() ? "X" : "-");
		
		return attributesValueLine;
	}
	
	
}
