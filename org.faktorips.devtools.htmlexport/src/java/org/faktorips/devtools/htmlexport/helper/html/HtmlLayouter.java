package org.faktorips.devtools.htmlexport.helper.html;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.AbstractLayouter;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileTypes;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;

public class HtmlLayouter extends AbstractLayouter implements ILayouter {

	String styleDefinitions;

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
		LayouterWrapperType wrapperType = wrapperPageElement.getWrapperType();
		if (wrapperType == LayouterWrapperType.NONE && wrapperPageElement.getStyles().isEmpty()) {
			visitSubElements(wrapperPageElement);
			return;
		}
		String wrappingElement = getHtmlElementByWrappingType(wrapperType);
		append(HtmlUtil.createHtmlElementOpenTag(wrappingElement, getClasses(wrapperPageElement)));
		visitSubElements(wrapperPageElement);
		append(HtmlUtil.createHtmlElementCloseTag(wrappingElement));
	}

	private String getHtmlElementByWrappingType(LayouterWrapperType wrapper) {
		if (wrapper == LayouterWrapperType.LISTITEM)
			return "li";
		if (wrapper == LayouterWrapperType.TABLEROW)
			return "tr";
		if (wrapper == LayouterWrapperType.TABLECELL)
			return "td";
		if (wrapper == LayouterWrapperType.BLOCK)
			return "div";
		return "span";
	}

	public void layoutRootPageElement(RootPageElement pageElement) {
		clean();
		append(HtmlUtil.createHtmlHead(pageElement.getTitle(), getStyleDefinitions()));
		visitSubElements(pageElement);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		append(HtmlUtil.createHtmlElement("small", "Created " + sdf.format(new Date())));

		append(HtmlUtil.createHtmlFoot());
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

	protected String getStyleDefinitions() {
		if (styleDefinitions == null) {
			try {
				styleDefinitions = FileHandler.readFile("org.faktorips.devtools.htmlexport", "html/base.css");
			} catch (IOException e) {
				styleDefinitions = "";
			}
		}
		return styleDefinitions;
	}

}
