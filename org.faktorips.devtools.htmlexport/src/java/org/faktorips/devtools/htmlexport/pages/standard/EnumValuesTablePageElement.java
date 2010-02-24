/**
 * 
 */
package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;

class EnumValuesTablePageElement extends AbstractSpecificTablePageElement {

	private List<IEnumAttribute> enumAttributes;
	private List<IEnumValue> enumValues;

	public EnumValuesTablePageElement(IEnumType type) {
		super();
		getEnumAttributes(type);
		enumValues = type.getEnumValues();
	}

	public EnumValuesTablePageElement(IEnumContent content) {
		super();
		try {
			getEnumAttributes(content.findEnumType(content.getIpsProject()));
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		enumValues = content.getEnumValues();
	}

	private void getEnumAttributes(IEnumType type) {
		try {
			enumAttributes = type.findAllEnumAttributesIncludeSupertypeOriginals(true, type.getIpsProject());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void addDataRows() {
		for (IEnumValue value : enumValues) {
			addValueRow(value);
		}
	}

	protected void addValueRow(IEnumValue value) {
		addSubElement(new TableRowPageElement(PageElementUtils.createTextPageElements(getValueData(value))));
	}

	protected List<String> getValueData(IEnumValue value) {
		List<String> valueData = new ArrayList<String>();

		for (IEnumAttribute enumAttribute : enumAttributes) {
			valueData.add(value.getEnumAttributeValue(enumAttribute).getValue());
		}

		return valueData;
	}

	@Override
	protected List<String> getHeadline() {
		List<String> headline = new ArrayList<String>();

		for (IEnumAttribute enumAttribute : enumAttributes) {
			headline.add(enumAttribute.getName());
		}

		return headline;
	}

	public boolean isEmpty() {
		return enumAttributes.isEmpty() || enumValues.isEmpty();
	}
}