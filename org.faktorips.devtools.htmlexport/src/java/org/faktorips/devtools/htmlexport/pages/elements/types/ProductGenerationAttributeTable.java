package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

public class ProductGenerationAttributeTable extends AbstractSpecificTablePageElement {

	private final IProductCmpt productCmpt;
	private final IAttribute[] attributes;
	private final DocumentorConfiguration config;


	public ProductGenerationAttributeTable(IProductCmpt productCmpt, IProductCmptType productCmptType,
			DocumentorConfiguration config) {
		this.productCmpt = productCmpt;
		this.attributes = findAttributes(productCmptType);
		this.config = config;
	}

	private IAttribute[] findAttributes(IProductCmptType productCmptType) {
		try {
			return productCmptType.findAllAttributes(productCmpt.getIpsProject());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void addDataRows() {
		for (IAttribute attribute : attributes) {
			addAttributeRow(attribute);
		}
	}

	private void addAttributeRow(IAttribute attribute) {
		PageElement[] cells = new PageElement[productCmpt.getNumOfGenerations() + 1];

		String name = attribute.getName();
		cells[0] = new TextPageElement(name);

		for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
			IAttributeValue attributeValue = productCmpt.getProductCmptGeneration(i).getAttributeValue(name);
			String value = attributeValue == null ? "[undefiniert]" : attributeValue.getValue();
			cells[i + 1] = new TextPageElement(value);
		}

		addSubElement(new TableRowPageElement(cells));

	}

	@Override
	protected List<String> getHeadline() {
		List<String> headline = new ArrayList<String>();

		headline.add("Generation ab:");

		for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
			headline.add(config.getSimpleDateFormat().format(
					productCmpt.getProductCmptGeneration(i).getValidFrom().getTime()));
		}
		return headline;
	}

	
	public boolean isEmpty() {
		return ArrayUtils.isEmpty(attributes) || productCmpt.getNumOfGenerations() == 0;
	}
	

}
