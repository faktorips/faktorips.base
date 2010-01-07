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
import org.faktorips.devtools.htmlexport.pages.elements.types.AttributesTablePageElement;

public class PolicyCmptContentPageElement extends AbstractTypeContentPageElement<PolicyCmptType> {

	PolicyCmptContentPageElement(PolicyCmptType object, DocumentorConfiguration config) {
		super(object, config);
	}

	
	
	@Override
	protected AttributesTablePageElement getAttributesTablePageElement() {
		return new AttributesTablePageElement(object) {

			@Override
			protected List<String> getAttributeData(IAttribute attribute) {
				List<String> attributeData = super.getAttributeData(attribute);

				PolicyCmptTypeAttribute polAttribute = (PolicyCmptTypeAttribute) attribute;

				attributeData.add(polAttribute.isProductRelevant() ? "X" : "-");
				attributeData.add(polAttribute.getAttributeType().getName());
				attributeData.add(polAttribute.isOverwrite() ? "X" : "-");

				return attributeData;
			}

			@Override
			protected List<String> getHeadline() {
				List<String> headline = super.getHeadline();
				
				headline.add(PolicyCmptTypeAttribute.PROPERTY_PRODUCT_RELEVANT);
				headline.add(PolicyCmptTypeAttribute.PROPERTY_ATTRIBUTE_TYPE);
				headline.add(PolicyCmptTypeAttribute.PROPERTY_OVERWRITES);

				return headline;
			}
			
		};
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
