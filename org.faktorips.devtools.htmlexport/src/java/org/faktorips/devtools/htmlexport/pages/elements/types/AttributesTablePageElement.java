package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

public class AttributesTablePageElement extends AbstractSpecificTablePageElement {

	protected IType type;
	
	public AttributesTablePageElement(IType type) {
		super();
		this.type = type;
	}


	@Override
	protected void addDataRows() {
		IAttribute[] attributes = type.getAttributes();
		for (IAttribute attribute : attributes) {
			addAttributeRow(attribute);
		}
	}
	

	protected void addAttributeRow(IAttribute attribute) {
		addSubElement(new TableRowPageElement(PageElementUtils.createTextPageElements(getAttributeData(attribute))));
	}

	
	protected List<String> getAttributeData(IAttribute attribute) {
		List<String> attributeData = new ArrayList<String>();

		attributeData.add(attribute.getName());
		attributeData.add(attribute.getDatatype());
		attributeData.add(attribute.getModifier().getName());
		attributeData.add(attribute.getDefaultValue());
		attributeData.add(attribute.getDescription());

		return attributeData;
	}

	@Override
	protected List<String> getHeadline() {
		List<String> headline = new ArrayList<String>();

		headline.add(IAttribute.PROPERTY_NAME);
		headline.add(IAttribute.PROPERTY_DATATYPE);
		headline.add(IAttribute.PROPERTY_MODIFIER);
		headline.add(IAttribute.PROPERTY_DEFAULT_VALUE);
		headline.add(IAttribute.PROPERTY_DESCRIPTION);

		return headline;
	}
}
