package org.faktorips.devtools.htmlexport.helper.html;

import java.io.IOException;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.faktorips.devtools.htmlexport.generators.AbstractLayouter;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayoutResource;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.helper.DocumentorUtil;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;

/**
 * Html-Layouter: Layouts the PageElements as Html
 * @author dicker
 *
 */
public class HtmlLayouter extends AbstractLayouter implements ILayouter {

	/*
	 * Name of the css-File 
	 */
	private static final String HTML_BASE_CSS = "html/base.css";
	/*
	 * path to the resource in the generated site
	 */
	private String resourcePath;
	/*
	 * path from the actual RootPageElement to the root-folder of the site 
	 */
	private String pathToRoot;

	public HtmlLayouter(String resourcePath) {
		super();
		this.resourcePath = resourcePath;
		initBaseResources();
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.generators.ILayouter#layoutLinkPageElement(org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement)
	 */
	public void layoutLinkPageElement(LinkPageElement pageElement) {
		String linkBase = pathToRoot + pageElement.getPathFromRoot() + ".html";

		append(HtmlUtil.createLinkOpenTag(linkBase, pageElement.getTarget(), getClasses(pageElement)));
		visitSubElements(pageElement);
		append(HtmlUtil.createHtmlElementCloseTag("a"));
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.generators.ILayouter#layoutListPageElement(org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement)
	 */
	public void layoutListPageElement(ListPageElement pageElement) {
		String listBaseHtmlTag = pageElement.isOrdered() ? "ul" : "ol";
		append(HtmlUtil.createHtmlElementOpenTag(listBaseHtmlTag, getClasses(pageElement)));
		visitSubElements(pageElement);
		append(HtmlUtil.createHtmlElementCloseTag(listBaseHtmlTag));
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.generators.ILayouter#layoutTablePageElement(org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement)
	 */
	public void layoutTablePageElement(TablePageElement pageElement) {
		append(HtmlUtil.createHtmlElementOpenTag("table", getClasses(pageElement)));
		visitSubElements(pageElement);
		append(HtmlUtil.createHtmlElementCloseTag("table"));
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.generators.ILayouter#layoutWrapperPageElement(org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement)
	 */
	public void layoutWrapperPageElement(WrapperPageElement wrapperPageElement) {
		WrapperType wrapperType = wrapperPageElement.getWrapperType();
		if (wrapperType == WrapperType.NONE && wrapperPageElement.getStyles().isEmpty()) {
			visitSubElements(wrapperPageElement);
			return;
		}
		String wrappingElement = getHtmlElementByWrappingType(wrapperType);
		append(HtmlUtil.createHtmlElementOpenTag(wrappingElement, getClasses(wrapperPageElement)));
		visitSubElements(wrapperPageElement);
		append(HtmlUtil.createHtmlElementCloseTag(wrappingElement));
	}

	/**
	 * returns name of the html-element for the given {@link WrapperType}
	 * @param wrapper
	 * @return String
	 */
	private String getHtmlElementByWrappingType(WrapperType wrapper) {
		if (wrapper == WrapperType.LISTITEM)
			return "li";
		if (wrapper == WrapperType.TABLEROW)
			return "tr";
		if (wrapper == WrapperType.TABLECELL)
			return "td";
		if (wrapper == WrapperType.BLOCK)
			return "div";
		return "span";
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.generators.ILayouter#layoutRootPageElement(org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement)
	 */
	public void layoutRootPageElement(AbstractRootPageElement pageElement) {
		initRootPage(pageElement);
		
		append(HtmlUtil.createHtmlHead(pageElement.getTitle(), pathToRoot + getStyleDefinitionPath()));
		visitSubElements(pageElement);

		append(HtmlUtil.createHtmlFoot());
	}

	/**
	 * sets the pathToRoot and clears the content
	 * @param pageElement
	 */
	private void initRootPage(AbstractRootPageElement pageElement) {
		pathToRoot = pageElement.getPathToRoot();
		clear();
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.generators.ILayouter#layoutTextPageElement(org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement)
	 */
	public void layoutTextPageElement(TextPageElement pageElement) {
		if (pageElement.getType() == TextType.WITHOUT_TYPE && pageElement.getStyles().isEmpty()) {
			append(HtmlUtil.getHtmlText(pageElement.getText()));
			return;
		}
		append(HtmlUtil.createHtmlElement(identifyTagName(pageElement), pageElement.getText(), getClasses(pageElement)));
	}

	/**
	 * puts all {@link Style}s of the {@link PageElement} in a String for the html-class-attribute.
	 * @param pageElement
	 * @return
	 */
	private String getClasses(PageElement pageElement) {

		Set<Style> styles = pageElement.getStyles();
		if (styles == null || styles.isEmpty())
			return null;

		StringBuilder classes = new StringBuilder();

		for (Style style : styles) {
			classes.append(style);
			classes.append(' ');
		}
		return classes.toString().trim();
	}

	/**
	 * returns the name of an html-element according to the given {@link TextPageElement}
	 * @param textPageElement
	 * @return
	 */
	private String identifyTagName(TextPageElement textPageElement) {
		return HtmlTextType.getHtmlTextTypeByTextType(textPageElement.getType()).getTagName();
	}

	/**
	 * The Layouter visits the subelements of the given {@link ICompositePageElement} 
	 * @param compositePageElement
	 */
	protected void visitSubElements(ICompositePageElement compositePageElement) {
		compositePageElement.visitSubElements(this);
	}

	/**
	 * initializes the basic resources e.g. the external css-stylesheet-definitions
	 */
	protected void initBaseResources() {
		try {
			LayoutResource cssResource = new LayoutResource(getStyleDefinitionPath(), FileHandler.readFile(
					"org.faktorips.devtools.htmlexport", HTML_BASE_CSS));

			addLayoutResource(cssResource);

		} catch (IOException e) {
			System.out.println("Resources aren't loaded correctly: " + e.getMessage());
		}
	}

	/*
	 * return relative path from root to the external css-stylesheet-definitions
	 */
	private String getStyleDefinitionPath() {
		return resourcePath + '/' + HTML_BASE_CSS;
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.generators.ILayouter#layoutImagePageElement(org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement)
	 */
	public void layoutImagePageElement(ImagePageElement imagePageElement) {

		String path = resourcePath + "/images/" + imagePageElement.getFileName() + ".png";
		addLayoutResource(new LayoutResource(path, DocumentorUtil.convertImageDataToByteArray(imagePageElement.getImageData(), SWT.IMAGE_PNG)));
		
		append(HtmlUtil.createImage(pathToRoot + path, imagePageElement.getTitle()));
	}

}
