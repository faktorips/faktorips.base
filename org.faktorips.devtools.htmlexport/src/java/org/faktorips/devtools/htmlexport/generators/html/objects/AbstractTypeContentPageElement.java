package org.faktorips.devtools.htmlexport.generators.html.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.WrapperPageElement;

public abstract class AbstractTypeContentPageElement<T extends IType> extends AbstractObjectContentPageElement<IType> {

	private class SupertypeHierarchieVisitor extends TypeHierarchyVisitor {
		List<IType> superTypes = new ArrayList<IType>();

		public SupertypeHierarchieVisitor(IIpsProject ipsProject) {
			super(ipsProject);
		}

		@Override
		protected boolean visit(IType currentType) throws CoreException {
			superTypes.add(currentType);
			return true;
		}

		public List<IType> getSuperTypes() {
			ArrayList<IType> revertedList = new ArrayList<IType>(superTypes);
			Collections.reverse(revertedList);
			return revertedList;
		}
	}

	public AbstractTypeContentPageElement(IType object) {
		super(object);
	}

	@Override
	public void build() {
		super.build();

		// Attribute
		addPageElements(createAttributesTable());
	}

	@Override
	protected void addTypeHierarchie() {
		ListPageElement superListe = new ListPageElement();

		SupertypeHierarchieVisitor hier = new SupertypeHierarchieVisitor(object.getIpsProject());
		try {
			hier.start(object);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		List<IType> superTypes = hier.getSuperTypes();

		for (IType type : superTypes) {
			if (type == object) {
				superListe.addPageElements(new TextPageElement(type.getQualifiedName()));
				continue;
			}
			superListe.addPageElements(new LinkPageElement(object, type, "content", new TextPageElement(type
					.getQualifiedName())));
		}
		addPageElements(superListe);
	}

	@Override
	protected void addStructureData() {
		super.addStructureData();

		try {
			IType to = object.findSupertype(object.getIpsProject());
			if (to == null)
				return;

			addPageElements(new WrapperPageElement(LayouterWrapperType.BLOCK, new PageElement[] {
					new TextPageElement("Erweitert "),
					new LinkPageElement(object, to, "content", new TextPageElement(to.getName())) }));
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		addPageElements(new TextPageElement("VERTRAG <-> PRODUKT", TextType.HEADING_1));
	}

	protected PageElement createAttributesTable() {
		IAttribute[] attributes;
		try {
			attributes = object.findAllAttributes(object.getIpsProject());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

		WrapperPageElement element = new WrapperPageElement(LayouterWrapperType.BLOCK);
		element.addPageElements(new TextPageElement("Attribute", TextType.HEADING_2));

		if (attributes.length == 0) {
			element.addPageElements(new TextPageElement("keine Attribute"));
			return element;
		}

		TablePageElement table = new TablePageElement();

		List<String> headLine = createAttributesHeadline();

		TableRowPageElement headerRow = new TableRowPageElement(PageElementUtils.createTextPageElements(null,
				TextType.WITHOUT_TYPE, headLine));

		table.addPageElements(headerRow);

		for (int i = 0; i < attributes.length; i++) {
			List<String> attributeLine = createAttributeValueLine(attributes[i]);
			table.addPageElements(new TableRowPageElement(PageElementUtils.createTextPageElements(null,
					TextType.WITHOUT_TYPE, attributeLine)));
		}

		element.addPageElements(table);

		return element;
	}

	protected List<String> createAttributeValueLine(IAttribute attribute) {
		List<String> propertyValues = new ArrayList<String>();

		propertyValues.add(attribute.getName());
		propertyValues.add(attribute.getDatatype());
		propertyValues.add(attribute.getModifier().getName());
		propertyValues.add(attribute.getDefaultValue());
		propertyValues.add(attribute.getDescription());

		return propertyValues;
	}

	protected List<String> createAttributesHeadline() {
		List<String> properties = new ArrayList<String>();

		properties.add(IAttribute.PROPERTY_NAME);
		properties.add(IAttribute.PROPERTY_DATATYPE);
		properties.add(IAttribute.PROPERTY_MODIFIER);
		properties.add(IAttribute.PROPERTY_DEFAULT_VALUE);
		properties.add(IAttribute.PROPERTY_DESCRIPTION);

		return properties;
	}

}