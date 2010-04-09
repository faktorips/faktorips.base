package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.enums.EnumTypeHierachyVisitor;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.TreeNodePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;

/**
 * A complete page representing an {@link IEnumType}
 * 
 * @author dicker
 * 
 */
public class EnumTypeContentPageElement extends AbstractObjectContentPageElement<IEnumType> {

	/**
	 * a table representing {@link IEnumAttribute}s of a given {@link IEnumType}
	 * 
	 * @author dicker
	 * 
	 */
	private class EnumAttributesTablePageElement extends AbstractSpecificTablePageElement {

		protected IEnumType type;
		private List<IEnumAttribute> attributes;

		public EnumAttributesTablePageElement(IEnumType type) {
			super();
			this.type = type;
			this.attributes = findAllEnumAttributes();
		}

		@Override
		protected void addDataRows() {
			for (IEnumAttribute attribute : attributes) {
				addAttributeRow(attribute);
			}
		}

		private List<IEnumAttribute> findAllEnumAttributes() {
			try {
				return type.findAllEnumAttributesIncludeSupertypeOriginals(true, type.getIpsProject());
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}

		protected void addAttributeRow(IEnumAttribute attribute) {
			addSubElement(new TableRowPageElement(PageElementUtils.createTextPageElements(getAttributeData(attribute))));
		}

		protected List<String> getAttributeData(IEnumAttribute attribute) {
			List<String> attributeData = new ArrayList<String>();

			attributeData.add(attribute.getName());
			attributeData.add(attribute.getDatatype());
			attributeData.add(attribute.isIdentifier() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
			attributeData.add(attribute.isUsedAsNameInFaktorIpsUi() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
			attributeData.add(attribute.isUnique() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
			attributeData.add(attribute.isInherited() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$

			attributeData.add(attribute.getDescription());

			return attributeData;
		}

		@Override
		protected List<String> getHeadline() {
			List<String> headline = new ArrayList<String>();

			headline.add(Messages.EnumTypeContentPageElement_headlineName);
			headline.add(Messages.EnumTypeContentPageElement_headlineDatatype);
			addHeadlineAndColumnLayout(headline, Messages.EnumTypeContentPageElement_headlineIdentifier, Style.CENTER);
			addHeadlineAndColumnLayout(headline, Messages.EnumTypeContentPageElement_headlineUsedAsNameInFaktorIpsUi, Style.CENTER);
			addHeadlineAndColumnLayout(headline, Messages.EnumTypeContentPageElement_headlineUnique, Style.CENTER);
			addHeadlineAndColumnLayout(headline, Messages.EnumTypeContentPageElement_headlineInherited, Style.CENTER);
			headline.add(Messages.EnumTypeContentPageElement_headlineDescription);

			return headline;
		}

		public boolean isEmpty() {
			return attributes.isEmpty();
		}
	}

	/**
	 * A visitor to get the supertypes of the given enumType
	 * 
	 * @author dicker
	 * 
	 */
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

	/**
	 * 
	 * creates a page, which represents the given enumType according to the
	 * given config
	 * 
	 * @param object
	 * @param config
	 */
	protected EnumTypeContentPageElement(IEnumType object, DocumentorConfiguration config) {
		super(object, config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.faktorips.devtools.htmlexport.pages.standard.
	 * AbstractObjectContentPageElement#addTypeHierarchy()
	 */
	@Override
	protected void addTypeHierarchy() {
		addSuperTypeHierarchy();
		addSubTypeHierarchy();
	}

	/**
	 * adds the subclasses of the enumType
	 */
	protected void addSubTypeHierarchy() {
		List<IIpsSrcFile> allClasses = getConfig().getLinkedSource(getEnumType().getIpsObjectType());

		List<PageElement> subTypes = new ArrayList<PageElement>();
		for (IIpsSrcFile srcFile : allClasses) {
			try {
				IEnumType type = (IEnumType) srcFile.getIpsObject();
				if (type.getSuperEnumType().equals(getEnumType().getQualifiedName())) {
					subTypes.add(new LinkPageElement(type, "content", type.getQualifiedName(), true)); //$NON-NLS-1$
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		if (subTypes.size() == 0)
			return;

		addPageElements(new WrapperPageElement(WrapperType.BLOCK, new PageElement[] {
				new TextPageElement(Messages.EnumTypeContentPageElement_headline16), new ListPageElement(subTypes) }));
	}

	/**
	 * returns the enumType
	 * 
	 * @return
	 */
	private IEnumType getEnumType() {
		return getIpsObject();
	}

	/**
	 * adds the hierarchy of superclasses
	 */
	protected void addSuperTypeHierarchy() {
		SupertypeHierarchieVisitor hier = new SupertypeHierarchieVisitor(getEnumType().getIpsProject());
		try {
			hier.start(getEnumType());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		List<IEnumType> superTypes = hier.getSuperTypes();

		if (superTypes.size() == 1) {
			addPageElements(new TextPageElement(getEnumType().getName()));
			return;
		}

		TreeNodePageElement baseElement = new TreeNodePageElement(new TreeNodePageElement(new LinkPageElement(
				superTypes.get(0), Messages.EnumTypeContentPageElement_headline17, superTypes.get(0).getQualifiedName(), true)));
		TreeNodePageElement element = baseElement;

		for (int i = 1; i < superTypes.size(); i++) {
			if (superTypes.get(i) == getEnumType()) {
				element.addPageElements(new TextPageElement(getEnumType().getName()));
				break;
			}
			TreeNodePageElement subElement = new TreeNodePageElement(new LinkPageElement(superTypes.get(i), Messages.EnumTypeContentPageElement_headline18,
					superTypes.get(i).getName(), true));
			element.addPageElements(subElement);
			element = subElement;
		}
		addPageElements(baseElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.faktorips.devtools.htmlexport.pages.standard.
	 * AbstractObjectContentPageElement#build()
	 */
	@Override
	public void build() {
		super.build();

		addAttributesTable();

		if (getEnumType().isContainingValues())
			addValuesTable();
		else {
			addEnumContentsList();
		}

	}

	/**
	 * adds table representing the attributes of the enumType
	 */
	protected void addAttributesTable() {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement(Messages.EnumTypeContentPageElement_headline19, TextType.HEADING_2));

		wrapper.addPageElements(getTableOrAlternativeText(new EnumAttributesTablePageElement(getEnumType()),
				Messages.EnumTypeContentPageElement_headline20));

		addPageElements(wrapper);
	}

	/**
	 * adds list representing the enumContents of the enumType
	 */
	protected void addEnumContentsList() {
		try {
			IEnumContent enumContent = getEnumType().findEnumContent(getConfig().getIpsProject());

			ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK)
					.addPageElements(new TextPageElement(Messages.EnumTypeContentPageElement_headline21, TextType.HEADING_2));

			if (enumContent == null) {
				addPageElements(wrapper.addPageElements(TextPageElement.createParagraph(Messages.EnumTypeContentPageElement_headline22)));
				return;
			}
			addPageElements(wrapper.addPageElements(new LinkPageElement(enumContent, Messages.EnumTypeContentPageElement_headline23, enumContent
					.getQualifiedName(), true)));

		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * adds table representing the values of the enumType
	 */
	protected void addValuesTable() {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement(Messages.EnumTypeContentPageElement_headline24, TextType.HEADING_2));

		wrapper.addPageElements(getTableOrAlternativeText(new EnumValuesTablePageElement(getEnumType()), Messages.EnumTypeContentPageElement_headline25));

		addPageElements(wrapper);
	}

}
