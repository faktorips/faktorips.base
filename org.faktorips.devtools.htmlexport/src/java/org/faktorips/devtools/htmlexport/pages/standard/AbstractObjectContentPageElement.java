package org.faktorips.devtools.htmlexport.pages.standard;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.helper.DocumentorUtil;
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.MessageListTablePageElement;
import org.faktorips.util.message.MessageList;

/**
 * <p>
 * The AbstractObjectContentPageElement represents a complete Page for an
 * {@link IIpsObject}. Use the {@link ContentPageUtil} to choose the right
 * subclass.
 * </p>
 * 
 * @author dicker
 * 
 * @param <T>
 */
public abstract class AbstractObjectContentPageElement<T extends IIpsObject> extends AbstractRootPageElement {

	private T ipsObject;
	private DocumentorConfiguration config;

	/**
	 * creates a page, which represents the given ipsObject according to the
	 * given config
	 * 
	 * @param ipsObject
	 * @param config
	 */
	protected AbstractObjectContentPageElement(T ipsObject, DocumentorConfiguration config) {
		this.ipsObject = ipsObject;
		this.config = config;
		setTitle(ipsObject.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement
	 * #build()
	 */
	@Override
	public void build() {
		super.build();

		addPageElements(new WrapperPageElement(WrapperType.BLOCK, new LinkPageElement("index", "_top", //$NON-NLS-1$ //$NON-NLS-2$
				Messages.AbstractObjectContentPageElement_overviewProject + " " + getConfig().getIpsProject().getName()))); //$NON-NLS-1$

		addPageElements(new LinkPageElement(getIpsObject().getIpsPackageFragment(), "classes", DocumentorUtil //$NON-NLS-1$
				.getIpsPackageName(getIpsObject().getIpsPackageFragment()), true));
		addPageElements(new TextPageElement(getIpsObject().getIpsObjectType().getDisplayName() + " " //$NON-NLS-1$
				+ getIpsObject().getName(), TextType.HEADING_1));

		addTypeHierarchy();

		addPageElements(new TextPageElement(getIpsObject().getName(), TextType.HEADING_2));
		addStructureData();

		if (!getIpsObject().getIpsProject().equals(getConfig().getIpsProject())) {
			addPageElements(TextPageElement.createParagraph(Messages.AbstractObjectContentPageElement_project + ": " //$NON-NLS-1$
					+ getIpsObject().getIpsProject().getName()));
		}
		addPageElements(TextPageElement.createParagraph(Messages.AbstractObjectContentPageElement_projectFolder + ": " //$NON-NLS-1$
				+ getIpsObject().getIpsSrcFile().getIpsPackageFragment()));

		addPageElements(new TextPageElement(Messages.description, TextType.HEADING_2));
		addPageElements(new TextPageElement(
				StringUtils.isBlank(getIpsObject().getDescription()) ? Messages.AbstractObjectContentPageElement_noDescription
						: getIpsObject().getDescription(), TextType.BLOCK));

		addValidationErrors();
	}

	/*
	 * adds a table with all validation messages of the {@link IpsObject}.
	 * Nothing will be shown, if there are no messages.
	 */
	private void addValidationErrors() {
		try {

			MessageList messageList = getIpsObject().validate(getIpsObject().getIpsProject());
			if (messageList.isEmpty())
				return;

			WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
			wrapper.addPageElements(new TextPageElement(Messages.AbstractObjectContentPageElement_validationErrors,
					TextType.HEADING_2));

			TablePageElement tablePageElement = new MessageListTablePageElement(messageList);

			wrapper.addPageElements(tablePageElement);

			addPageElements(wrapper);

		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * adds {@link PageElement}s for structural data like fitting ProductCmpt
	 * for a PolicyCmptType
	 */
	protected void addStructureData() {
	}

	/**
	 * adds {@link PageElement}s for hierarchical data like super- and
	 * subclasses
	 */
	protected void addTypeHierarchy() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement
	 * #getPathToRoot()
	 */
	@Override
	public String getPathToRoot() {
		return PathUtilFactory.createPathUtil(getIpsObject()).getPathToRoot();
	}

	/**
	 * returns a table, if there is data within it, or an alternative text
	 * 
	 * @param tablePageElement
	 * @param alternativeText
	 * @return
	 */
	protected PageElement getTableOrAlternativeText(AbstractSpecificTablePageElement tablePageElement,
			String alternativeText) {
		if (tablePageElement.isEmpty()) {
			return new TextPageElement(alternativeText);
		}
		return tablePageElement;
	}

	/**
	 * returns the ipsObject
	 * 
	 * @return
	 */
	protected T getIpsObject() {
		return ipsObject;
	}

	/**
	 * returns the config
	 * 
	 * @return
	 */
	protected DocumentorConfiguration getConfig() {
		return config;
	}
}
