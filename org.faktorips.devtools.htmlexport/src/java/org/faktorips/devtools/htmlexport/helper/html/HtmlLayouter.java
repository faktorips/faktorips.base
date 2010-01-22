package org.faktorips.devtools.htmlexport.helper.html;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.faktorips.devtools.htmlexport.generators.AbstractLayouter;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayoutResource;
import org.faktorips.devtools.htmlexport.generators.PageElementWrapperType;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.helper.Util;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileTypes;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;

public class HtmlLayouter extends AbstractLayouter implements ILayouter {

	private static final String HTML_BASE_CSS = "html/base.css";
	private String resourcePath;
	private String pathToRoot;

	public HtmlLayouter(String resourcePath) {
		super();
		this.resourcePath = resourcePath;
		initBaseResources();
	}

	public void layoutLinkPageElement(LinkPageElement pageElement) {
		String linkBase = HtmlUtil.createLinkBase(pageElement.getFrom(), pageElement.getTo(), LinkedFileTypes
				.getLinkedFileTypeByIpsElement(pageElement.getTo()));

		append(HtmlUtil.createLinkOpenTag(linkBase, pageElement.getTarget(), getClasses(pageElement)));
		visitSubElements(pageElement);
		append(HtmlUtil.createHtmlElementCloseTag("a"));
	}

	public void layoutListPageElement(ListPageElement pageElement) {
		String listBaseHtmlTag = pageElement.isOrdered() ? "ul" : "ol";
		append(HtmlUtil.createHtmlElementOpenTag(listBaseHtmlTag, getClasses(pageElement)));
		visitSubElements(pageElement);
		append(HtmlUtil.createHtmlElementCloseTag(listBaseHtmlTag));
	}

	public void layoutTablePageElement(TablePageElement pageElement) {
		append(HtmlUtil.createHtmlElementOpenTag("table", getClasses(pageElement)));
		visitSubElements(pageElement);
		append(HtmlUtil.createHtmlElementCloseTag("table"));
	}

	public void layoutWrapperPageElement(WrapperPageElement wrapperPageElement) {
		PageElementWrapperType wrapperType = wrapperPageElement.getWrapperType();
		if (wrapperType == PageElementWrapperType.NONE && wrapperPageElement.getStyles().isEmpty()) {
			visitSubElements(wrapperPageElement);
			return;
		}
		String wrappingElement = getHtmlElementByWrappingType(wrapperType);
		append(HtmlUtil.createHtmlElementOpenTag(wrappingElement, getClasses(wrapperPageElement)));
		visitSubElements(wrapperPageElement);
		append(HtmlUtil.createHtmlElementCloseTag(wrappingElement));
	}

	private String getHtmlElementByWrappingType(PageElementWrapperType wrapper) {
		if (wrapper == PageElementWrapperType.LISTITEM)
			return "li";
		if (wrapper == PageElementWrapperType.TABLEROW)
			return "tr";
		if (wrapper == PageElementWrapperType.TABLECELL)
			return "td";
		if (wrapper == PageElementWrapperType.BLOCK)
			return "div";
		return "span";
	}

	public void layoutRootPageElement(AbstractRootPageElement pageElement) {
		initRootPage(pageElement);
		
		append(HtmlUtil.createHtmlHead(pageElement.getTitle(), pathToRoot + getStyleDefinitionPath()));
		visitSubElements(pageElement);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		append(HtmlUtil.createHtmlElement("small", "Created " + sdf.format(new Date())));

		append(HtmlUtil.createHtmlFoot());
	}

	private void initRootPage(AbstractRootPageElement pageElement) {
		pathToRoot = pageElement.getPathToRoot();
		clean();
	}

	public void layoutTextPageElement(TextPageElement pageElement) {
		if (pageElement.getType() == TextType.WITHOUT_TYPE && pageElement.getStyles().isEmpty()) {
			append(HtmlUtil.prepareText(pageElement.getText()));
			return;
		}
		append(HtmlUtil.createHtmlElement(identifyTagName(pageElement), pageElement.getText(), getClasses(pageElement)));
	}

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

	private String identifyTagName(TextPageElement textPageElement) {
		return HtmlTextType.getHtmlTextTypeByTextType(textPageElement.getType()).getTagName();
	}

	protected void visitSubElements(ICompositePageElement pageElement) {
		pageElement.visitSubElements(this);
	}

	protected void initBaseResources() {
		try {
			LayoutResource cssResource = new LayoutResource(getStyleDefinitionPath(), FileHandler.readFile(
					"org.faktorips.devtools.htmlexport", HTML_BASE_CSS));

			addLayoutResource(cssResource);

		} catch (IOException e) {
			System.out.println("Resourcen nicht korrekt geladen: " + e.getMessage());
		}
	}

	private String getStyleDefinitionPath() {
		return resourcePath + '/' + HTML_BASE_CSS;
	}

	public void layoutImagePageElement(ImagePageElement imagePageElement) {

		String path = resourcePath + "/images/" + imagePageElement.getPath() + ".png";
		addLayoutResource(new LayoutResource(path, Util.convertImageDataToByteArray(imagePageElement.getImageData(), SWT.IMAGE_PNG)));
		
		append(HtmlUtil.createImage(pathToRoot + path, imagePageElement.getTitle()));
	}

}
