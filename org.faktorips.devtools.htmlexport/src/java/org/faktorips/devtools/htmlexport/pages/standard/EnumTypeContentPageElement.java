package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.enums.EnumTypeHierachyVisitor;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.PageElementWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.HierarchyPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;

public class EnumTypeContentPageElement extends AbstractObjectContentPageElement<IEnumType> {

	protected class EnumAttributesTablePageElement extends AbstractSpecificTablePageElement {

		protected IEnumType type;

		public EnumAttributesTablePageElement(IEnumType type) {
			super();
			this.type = type;
		}

		@Override
		protected void addDataRows() {
			List<IEnumAttribute> attributes;
			try {
				attributes = type.findAllEnumAttributesIncludeSupertypeOriginals(true, type.getIpsProject());
			} catch (CoreException e) {
				return;
			}
			for (IEnumAttribute attribute : attributes) {
				addAttributeRow(attribute);
			}
		}

		protected void addAttributeRow(IEnumAttribute attribute) {
			addSubElement(new TableRowPageElement(PageElementUtils.createTextPageElements(getAttributeData(attribute))));
		}

		protected List<String> getAttributeData(IEnumAttribute attribute) {
			List<String> attributeData = new ArrayList<String>();

			attributeData.add(attribute.getName());
			attributeData.add(attribute.getDatatype());
			attributeData.add(attribute.isIdentifier() ? "X" : "-");
			attributeData.add(attribute.isUsedAsNameInFaktorIpsUi() ? "X" : "-");
			attributeData.add(attribute.isUnique() ? "X" : "-");
			attributeData.add(attribute.isInherited() ? "X" : "-");

			attributeData.add(attribute.getDescription());

			return attributeData;
		}

		@Override
		protected List<String> getHeadline() {
			List<String> headline = new ArrayList<String>();

			headline.add(IEnumAttribute.PROPERTY_NAME);
			headline.add(IEnumAttribute.PROPERTY_DATATYPE);
			addHeadlineAndColumnLayout(headline, IEnumAttribute.PROPERTY_IDENTIFIER, Style.CENTER);
			addHeadlineAndColumnLayout(headline, IEnumAttribute.PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI, Style.CENTER);
			addHeadlineAndColumnLayout(headline, IEnumAttribute.PROPERTY_UNIQUE, Style.CENTER);
			addHeadlineAndColumnLayout(headline, IEnumAttribute.PROPERTY_INHERITED, Style.CENTER);
			headline.add(IEnumAttribute.PROPERTY_DESCRIPTION);

			return headline;
		}
	}

	
	protected class EnumValuesTablePageElement extends AbstractSpecificTablePageElement {

		protected IEnumType type;
		private List<IEnumAttribute> enumAttributes;

		public EnumValuesTablePageElement(IEnumType type) {
			super();
			this.type = type;
			try {
				enumAttributes = type.findAllEnumAttributesIncludeSupertypeOriginals(true, type.getIpsProject());
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void addDataRows() {
			List<IEnumValue> values;
			values = type.getEnumValues();
			for (IEnumValue value : values) {
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
	}

	private class SupertypeHierarchieVisitor extends EnumTypeHierachyVisitor {
		List<IEnumType> superTypes = new ArrayList<IEnumType>();

		public SupertypeHierarchieVisitor(IIpsProject ipsProject) {
			super(ipsProject);
		}

		@Override
		protected boolean visit(IEnumType currentType) throws CoreException {
			superTypes.add(currentType);
			return true;
		}

		public List<IEnumType> getSuperTypes() {
			ArrayList<IEnumType> revertedList = new ArrayList<IEnumType>(superTypes);
			Collections.reverse(revertedList);
			return revertedList;
		}
	}

	protected EnumTypeContentPageElement(IEnumType object, DocumentorConfiguration config) {
		super(object, config);
	}

	@Override
	protected void addTypeHierarchie() {
		addSuperTypeHierarchie();
		addSubTypeHierarchie();
	}

	protected void addSubTypeHierarchie() {
		List<IIpsSrcFile> allClasses = new ArrayList<IIpsSrcFile>();
		try {
			object.getIpsProject().findAllIpsSrcFiles(allClasses, new IpsObjectType[] { object.getIpsObjectType() });
		} catch (CoreException e) {
			return;
		}

		List<PageElement> subTypes = new ArrayList<PageElement>();
		for (IIpsSrcFile srcFile : allClasses) {
			try {
				IEnumType type = (IEnumType) srcFile.getIpsObject();
				if (type.getSuperEnumType().equals(object.getQualifiedName())) {
					subTypes.add(new LinkPageElement(type, "content", type.getQualifiedName(), true));
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		if (subTypes.size() == 0)
			return;

		addPageElements(new WrapperPageElement(PageElementWrapperType.BLOCK, new PageElement[] {
				new TextPageElement("Unterklassen"), new ListPageElement(subTypes) }));
	}

	protected void addSuperTypeHierarchie() {
		SupertypeHierarchieVisitor hier = new SupertypeHierarchieVisitor(object.getIpsProject());
		try {
			hier.start(object);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		List<IEnumType> superTypes = hier.getSuperTypes();

		if (superTypes.size() > 2) {
			System.out.println("los gehts!");
		}

		if (superTypes.size() == 1) {
			addPageElements(new TextPageElement(object.getName()));
			return;
		}

		HierarchyPageElement baseElement = new HierarchyPageElement(new HierarchyPageElement(new LinkPageElement(
				superTypes.get(0), "content", superTypes.get(0).getQualifiedName(), true)));
		HierarchyPageElement element = baseElement;

		for (int i = 1; i < superTypes.size(); i++) {
			if (superTypes.get(i) == object) {
				element.addPageElements(new TextPageElement(object.getName()));
				break;
			}
			HierarchyPageElement subElement = new HierarchyPageElement(new LinkPageElement(superTypes.get(i),
					"content", superTypes.get(i).getName(), true));
			element.addPageElements(subElement);
			element = subElement;
		}
		addPageElements(baseElement);
	}

	protected PageElement createAttributesTable() {
		List<IEnumAttribute> attributes;
		try {
			attributes = object.findAllEnumAttributesIncludeSupertypeOriginals(true, object.getIpsProject());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

		WrapperPageElement wrapper = new WrapperPageElement(PageElementWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Attribute", TextType.HEADING_2));

		if (attributes.size() == 0) {
			wrapper.addPageElements(new TextPageElement("keine Attribute"));
			return wrapper;
		}

		TablePageElement table = getAttributesTablePageElement();

		wrapper.addPageElements(table);

		return wrapper;
	}

	protected EnumAttributesTablePageElement getAttributesTablePageElement() {
		return new EnumAttributesTablePageElement(object);
	}

	@Override
	public void build() {
		super.build();
		
		// Attribute
		addPageElements(createAttributesTable());
		
		// Werte
		addPageElements(createValuesTable());
	}

	protected PageElement createValuesTable() {
		List<IEnumValue> values = object.getEnumValues();

		WrapperPageElement wrapper = new WrapperPageElement(PageElementWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Werte", TextType.HEADING_2));

		if (values.size() == 0) {
			wrapper.addPageElements(new TextPageElement("keine Werte"));
			return wrapper;
		}

		TablePageElement table = getValuesTablePageElement();

		wrapper.addPageElements(table);

		return wrapper;
	}

	protected EnumValuesTablePageElement getValuesTablePageElement() {
		return new EnumValuesTablePageElement(object);
	}

}
