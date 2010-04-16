package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
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
import org.faktorips.devtools.htmlexport.pages.elements.types.AttributesTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.ValidationRuleTablePageElement;

/**
 * A page representing an {@link IPolicyCmptType}
 * 
 * @author dicker
 * 
 */
public class PolicyCmptTypeContentPageElement extends AbstractTypeContentPageElement<IPolicyCmptType> {

	/**
	 * creates a page for the given object according to the given config
	 * 
	 * @param object
	 * @param config
	 */
	PolicyCmptTypeContentPageElement(IPolicyCmptType object, DocumentorConfiguration config) {
		super(object, config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.faktorips.devtools.htmlexport.pages.standard.
	 * AbstractTypeContentPageElement#build()
	 */
	@Override
	public void build() {
		super.build();

		// Regeln hinzufügen
		addValidationRuleTable();
	}

	private void addValidationRuleTable() {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement(Messages.PolicyCmptTypeContentPageElement_rules, TextType.HEADING_2));

		wrapper.addPageElements(getTableOrAlternativeText(new ValidationRuleTablePageElement(getDocumentedIpsObject()),
				Messages.PolicyCmptTypeContentPageElement_noValidationrules));

		addPageElements(wrapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.faktorips.devtools.htmlexport.pages.standard.
	 * AbstractTypeContentPageElement#getAttributesTablePageElement()
	 */
	@Override
	protected AttributesTablePageElement getAttributesTablePageElement() {
		return new AttributesTablePageElement(getDocumentedIpsObject()) {

			@Override
			protected List<String> getAttributeData(IAttribute attribute) {
				List<String> attributeData = super.getAttributeData(attribute);

				PolicyCmptTypeAttribute polAttribute = (PolicyCmptTypeAttribute) attribute;

				attributeData.add(polAttribute.isProductRelevant() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
				attributeData.add(polAttribute.getAttributeType().getName());
				attributeData.add(polAttribute.isOverwrite() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$

				return attributeData;
			}

			@Override
			protected List<String> getHeadline() {
				List<String> headline = super.getHeadline();

				addHeadlineAndColumnLayout(headline, Messages.PolicyCmptTypeContentPageElement_productRelevant, Style.CENTER);

				headline.add(Messages.PolicyCmptTypeContentPageElement_attributeType);

				addHeadlineAndColumnLayout(headline, Messages.PolicyCmptTypeContentPageElement_overwrite, Style.CENTER);

				return headline;
			}

		};
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.standard.AbstractTypeContentPageElement#addStructureData()
	 */
	@Override
	protected void addStructureData() {
		super.addStructureData();

		addPageElements(TextPageElement.createParagraph("Abstract Type" + ": " + (getDocumentedIpsObject().isAbstract() ? "X" : "-"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		try {
			
			
			IProductCmptType to = getDocumentedIpsObject().getIpsProject().findProductCmptType(
					getDocumentedIpsObject().getProductCmptType());
			if (to == null) {
				addPageElements(TextPageElement.createParagraph(IpsObjectType.POLICY_CMPT_TYPE.getDisplayName() + ": " + Messages.PolicyCmptTypeContentPageElement_none)); //$NON-NLS-1$
				return;
			}
			addPageElements(new WrapperPageElement(WrapperType.BLOCK, new PageElement[] {
					new TextPageElement(IpsObjectType.POLICY_CMPT_TYPE.getDisplayName() + ": "), //$NON-NLS-1$
					new LinkPageElement(to, "content", to.getName(), true) })); //$NON-NLS-1$
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}
}
