package org.faktorips.devtools.htmlexport.generators.html.objects;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.WrapperPageElement;

public class PolicyCmptContentPageElement extends AbstractTypeContentPageElement<PolicyCmptType> {

	PolicyCmptContentPageElement(PolicyCmptType object, DocumentorConfiguration config) {
		super(object, config);
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
	
	@Override
	protected void addStructureData() {
		super.addStructureData();

		try {
			IProductCmptType to = object.getIpsProject().findProductCmptType(getPolicyCmptType().getProductCmptType());
			if (to == null) {
				addPageElements(TextPageElement.newBlock("Produktbausteinklasse: keine"));
				return;
			}
			addPageElements(new WrapperPageElement(LayouterWrapperType.BLOCK, new PageElement[] {
					new TextPageElement("Produktbausteinklasse: "),
					new LinkPageElement(object, to, "content", new TextPageElement(to.getName())) }));
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	private IPolicyCmptType getPolicyCmptType() {
		return ((IPolicyCmptType) object);
	}

}
