package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.PageElementWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.HierarchyPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AssociationTablePageElement;
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
		WrapperPageElement wrapper = new WrapperPageElement(PageElementWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Methoden", TextType.HEADING_2));

		wrapper.addPageElements(getTableOrAlternativeText(getMethodsTablePageElement(), "keine Methoden vorhanden"));
		return wrapper;
	}

	protected MethodsTablePageElement getMethodsTablePageElement() {
		return new MethodsTablePageElement(object);
	}

	@Override
	protected void addTypeHierarchie() {
		addPageElements(new TextPageElement("Hierarchie", TextType.HEADING_2));
		addSuperTypeHierarchie();
		addSubTypeHierarchie();
	}

	protected void addSubTypeHierarchie() {

		List<PageElement> subTypes = new ArrayList<PageElement>();
		for (IIpsSrcFile srcFile : config.getLinkedSource(object.getIpsObjectType())) {
			try {
				IType type = (IType) srcFile.getIpsObject();
				if (type.getSupertype().equals(object.getQualifiedName())) {
					subTypes.add(new LinkPageElement(type, "content", type.getQualifiedName(), true));
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		if (subTypes.size() == 0)
			return;

		addPageElements(new WrapperPageElement(PageElementWrapperType.BLOCK, new ListPageElement(subTypes)));
	}

	protected void addSuperTypeHierarchie() {
		SupertypeHierarchieVisitor hier = new SupertypeHierarchieVisitor(object.getIpsProject());
		try {
			hier.start(object);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		List<IType> superTypes = hier.getSuperTypes();

		if (superTypes.size() == 1) {
			addPageElements(new TextPageElement("keine Superklasse"));
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

	@Override
	protected void addStructureData() {
		super.addStructureData();

		try {
			IType to = object.findSupertype(object.getIpsProject());
			if (to == null)
				return;

			addPageElements(new WrapperPageElement(PageElementWrapperType.BLOCK, new PageElement[] {
					new TextPageElement("Erweitert "), new LinkPageElement(to, "content", to.getName(), true) }));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	protected PageElement createAssociationsTable() {
		WrapperPageElement wrapper = new WrapperPageElement(PageElementWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Beziehungen", TextType.HEADING_2));

		wrapper.addPageElements(getTableOrAlternativeText(new AssociationTablePageElement(object),
				"keine Beziehungen vorhanden"));

		return wrapper;
	}

	protected PageElement createAttributesTable() {
		WrapperPageElement wrapper = new WrapperPageElement(PageElementWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Attribute", TextType.HEADING_2));

		wrapper
				.addPageElements(getTableOrAlternativeText(getAttributesTablePageElement(), "keine Attribute vorhanden"));

		return wrapper;
	}

	protected AttributesTablePageElement getAttributesTablePageElement() {
		return new AttributesTablePageElement(object);
	}
}