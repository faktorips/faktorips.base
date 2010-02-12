package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AttributesTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.ValidationRuleTablePageElement;

public class PolicyCmptTypeContentPageElement extends AbstractTypeContentPageElement<PolicyCmptType> {

	PolicyCmptTypeContentPageElement(IPolicyCmptType object, DocumentorConfiguration config) {
		super(object, config);
	}

	@Override
	public void build() {
		super.build();

		// Regeln hinzufügen
		addPageElements(createValidationRuleTable());
	}

	private PageElement createValidationRuleTable() {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Regeln", TextType.HEADING_2));

		wrapper.addPageElements(getTableOrAlternativeText(new ValidationRuleTablePageElement(getPolicyCmptType()), "keine Regeln vorhanden"));
		
		return wrapper;
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

				addHeadlineAndColumnLayout(headline, PolicyCmptTypeAttribute.PROPERTY_PRODUCT_RELEVANT, Style.CENTER);
				
				headline.add(PolicyCmptTypeAttribute.PROPERTY_ATTRIBUTE_TYPE);

				addHeadlineAndColumnLayout(headline, PolicyCmptTypeAttribute.PROPERTY_OVERWRITES, Style.CENTER);

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
			addPageElements(new WrapperPageElement(WrapperType.BLOCK, new PageElement[] {
					new TextPageElement("Produktbausteinklasse: "),
					new LinkPageElement(to, "content", to.getName(), true) }));
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	private IPolicyCmptType getPolicyCmptType() {
		return ((IPolicyCmptType) object);
	}
}
