package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

/**
 * Represents a table with the attributes of an {@link IType} as rows and the attributes of the attribute as columns
 * @author dicker
 *
 */
public class AttributesTablePageElement extends AbstractSpecificTablePageElement {

	protected IType type;
	
	/**
	 * Creates an {@link AttributesTablePageElement} for the specified {@link IType}
	 * @param type
	 */
	public AttributesTablePageElement(IType type) {
		super();
		this.type = type;
	}


	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#addDataRows()
	 */
	@Override
	protected void addDataRows() {
		IAttribute[] attributes = type.getAttributes();
		for (IAttribute attribute : attributes) {
			addAttributeRow(attribute);
		}
	}
	

	/**
	 * adds a row for the given {@link IAttribute}
	 * @param attribute
	 */
	protected void addAttributeRow(IAttribute attribute) {
		addSubElement(new TableRowPageElement(PageElementUtils.createTextPageElements(getAttributeData(attribute))));
	}

	
	/**
	 * returns a list with the values of the attributes of the attribute
	 * @param attribute
	 * @return
	 */
	protected List<String> getAttributeData(IAttribute attribute) {
		List<String> attributeData = new ArrayList<String>();

		attributeData.add(attribute.getName());
		attributeData.add(attribute.getDatatype());
		attributeData.add(attribute.getModifier().getName());
		attributeData.add(attribute.getDefaultValue());
		attributeData.add(attribute.getDescription());

		return attributeData;
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#getHeadline()
	 */
	@Override
	protected List<String> getHeadline() {
		List<String> headline = new ArrayList<String>();

		headline.add(Messages.AttributesTablePageElement_headlineName);
		headline.add(Messages.AttributesTablePageElement_headlineDatatype);
		headline.add(Messages.AttributesTablePageElement_headlineModifier);
		headline.add(Messages.AttributesTablePageElement_headlineDefaultValue);
		headline.add(Messages.AttributesTablePageElement_headlineDescription);

		return headline;
	}
	
	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.core.DataPageElement#isEmpty()
	 */
	public boolean isEmpty() {
		return ArrayUtils.isEmpty(type.getAttributes());
	}

}
