package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.PageElementWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
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
					subTypes.add(new LinkPageElement(object, type, "content", type.getQualifiedName(), true));
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
			superListe.addPageElements(new LinkPageElement(object, type, "content", type.getQualifiedName(), true));
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

			addPageElements(new WrapperPageElement(PageElementWrapperType.BLOCK, new PageElement[] {
					new TextPageElement("Erweitert "),
					new LinkPageElement(object, to, "content", to.getName(), true) }));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	// TODO extrahieren in eigene Klasse
	protected PageElement createAssociationsTable() {
		WrapperPageElement wrapper = new WrapperPageElement(PageElementWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Beziehungen", TextType.HEADING_2));
		
		if (object.getAssociations().length == 0) {
			wrapper.addPageElements(new TextPageElement("keine Beziehungen vorhanden"));
			return wrapper;
		}
		wrapper.addPageElements(getAssociationTablePageElement());
		return wrapper;
	}
		

	protected AssociationTablePageElement getAssociationTablePageElement() {
		return new AssociationTablePageElement(object);
	}

	protected PageElement createAttributesTable() {
		IAttribute[] attributes;
		try {
			attributes = object.findAllAttributes(object.getIpsProject());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

		WrapperPageElement wrapper = new WrapperPageElement(PageElementWrapperType.BLOCK);
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