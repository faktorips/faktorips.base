package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AttributesTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.MethodsTablePageElement;

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

	public AbstractTypeContentPageElement(IType object, DocumentorConfiguration config) {
		super(object, config);
	}

	@Override
	public void build() {
		super.build();

		// Attribute
		addPageElements(createAttributesTable());

		// Assozationen
		addPageElements(createAssociationsTable());
		
		// Methoden
		addPageElements(createMethodsTable());
	}

	protected PageElement createMethodsTable() {
		WrapperPageElement wrapper = new WrapperPageElement(LayouterWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Methoden", TextType.HEADING_2));
		
		if (object.getMethods().length == 0) {
			wrapper.addPageElements(new TextPageElement("keine Methoden vorhanden"));
			return wrapper;
		}
		wrapper.addPageElements(getMethodsTablePageElement());
		return wrapper;
	}

	protected MethodsTablePageElement getMethodsTablePageElement() {
		return new MethodsTablePageElement(object);
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
				IType type = (IType) srcFile.getIpsObject();
				if (type.getSupertype().equals(object.getQualifiedName())) {
					subTypes.add(new LinkPageElement(object, type, "content", new TextPageElement(type
							.getQualifiedName())));
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		if (subTypes.size() == 0)
			return;

		addPageElements(new WrapperPageElement(LayouterWrapperType.BLOCK, new PageElement[] {
				new TextPageElement("Unterklassen"), new ListPageElement(subTypes) }));
	}

	protected void addSuperTypeHierarchie() {
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
	}

	// TODO extrahieren in eigene Klasse
	protected PageElement createAssociationsTable() {
		IAssociation[] associations = object.getAssociations();

		WrapperPageElement wrapper = new WrapperPageElement(LayouterWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Beziehungen", TextType.HEADING_2));

		if (associations.length == 0) {
			wrapper.addPageElements(new TextPageElement("keine Beziehungen"));
			return wrapper;
		}

		TablePageElement table = new TablePageElement();

		List<String> headLine = createAssociationsHeadline();

		TableRowPageElement headerRow = new TableRowPageElement(PageElementUtils.createTextPageElements(headLine,
				null, TextType.WITHOUT_TYPE));

		table.addPageElements(headerRow);

		for (int i = 0; i < associations.length; i++) {
			List<PageElement> associationLine = createAssociationsValueLine(associations[i]);
			table.addPageElements(new TableRowPageElement(associationLine.toArray(new PageElement[associationLine.size()])));
		}

		wrapper.addPageElements(table);

		return wrapper;
	}

	protected List<String> createAssociationsHeadline() {
		List<String> properties = new ArrayList<String>();

		properties.add(IAssociation.PROPERTY_NAME);
		properties.add(IAssociation.PROPERTY_TARGET);
		properties.add(IAssociation.PROPERTY_DESCRIPTION);
		properties.add(IAssociation.PROPERTY_ASSOCIATION_TYPE);
		properties.add(IAssociation.PROPERTY_AGGREGATION_KIND);
		properties.add(IAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
		properties.add(IAssociation.PROPERTY_TARGET_ROLE_PLURAL);
		properties.add(IAssociation.PROPERTY_MIN_CARDINALITY);
		properties.add(IAssociation.PROPERTY_MAX_CARDINALITY);
		properties.add(IAssociation.PROPERTY_DERIVED_UNION);
		properties.add(IAssociation.PROPERTY_SUBSETTED_DERIVED_UNION);
		properties.add(IAssociation.PROPERTY_QUALIFIED);

		//TODO Type-spezifisch ableiten
		
		return properties;
	}

	protected List<PageElement> createAssociationsValueLine(IAssociation association) {
		List<PageElement> propertyValues = new ArrayList<PageElement>();

		addPropertyValue(propertyValues, association.getName());

		try {
			IIpsObject target = object.getIpsProject().findIpsObject(object.getIpsObjectType(), association.getTarget());
			propertyValues.add(new LinkPageElement(object, target, "content", new TextPageElement(target.getQualifiedName())));
		} catch (CoreException e) {
			propertyValues.add(new TextPageElement(""));
		}
		
		addPropertyValue(propertyValues, association.getDescription());
		addPropertyValue(propertyValues, association.getAssociationType().getName());
		addPropertyValue(propertyValues, association.getAggregationKind().getName());
		addPropertyValue(propertyValues, association.getTargetRoleSingular());
		addPropertyValue(propertyValues, association.getTargetRolePlural());
		addPropertyValue(propertyValues, Integer.toString(association.getMinCardinality()));
		addPropertyValue(propertyValues, Integer.toString(association.getMaxCardinality()));
		addPropertyValue(propertyValues, association.isDerivedUnion() ? "X" : "-");
		addPropertyValue(propertyValues, association.isSubsetOfADerivedUnion() ? "X" : "-");
		addPropertyValue(propertyValues, association.isQualified() ? "X" : "-");

		//TODO Type-spezifisch ableiten
		
		return propertyValues;
	}

	protected void addPropertyValue(List<PageElement> propertyValues, String name) {
		propertyValues.add(new TextPageElement(name));
	}

	protected PageElement createAttributesTable() {
		IAttribute[] attributes;
		try {
			attributes = object.findAllAttributes(object.getIpsProject());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

		WrapperPageElement wrapper = new WrapperPageElement(LayouterWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Attribute", TextType.HEADING_2));

		if (attributes.length == 0) {
			wrapper.addPageElements(new TextPageElement("keine Attribute"));
			return wrapper;
		}

		TablePageElement table = getAttributesTablePageElement();

		wrapper.addPageElements(table);

		return wrapper;
	}

	protected AttributesTablePageElement getAttributesTablePageElement() {
		return new AttributesTablePageElement(object);
	}
}