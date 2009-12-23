package org.faktorips.devtools.htmlexport.helper.html;

import org.faktorips.devtools.htmlexport.generators.AbstractLayouter;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileTypes;
import org.faktorips.devtools.htmlexport.pages.elements.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.WrapperPageElement;

public class HtmlLayouter extends AbstractLayouter implements ILayouter {

    public void layoutLinkPageElement(LinkPageElement pageElement) {
        String linkBase = HtmlUtil.createLinkBase(pageElement.getFrom(), pageElement.getTo(), LinkedFileTypes
                .getLinkedFileTypeByIpsElement(pageElement.getTo()));

        append(HtmlUtil.createLinkOpenTag(linkBase, pageElement.getTarget(), ""));
        visitSubElements(pageElement);
        append(HtmlUtil.createHtmlElementCloseTag("a"));
    }

    public void layoutListPageElement(ListPageElement pageElement) {
        String listBaseHtmlTag = pageElement.isOrdered() ? "ul" : "ol";
        append(HtmlUtil.createHtmlElementOpenTag(listBaseHtmlTag));
        visitSubElements(pageElement);
        append(HtmlUtil.createHtmlElementCloseTag(listBaseHtmlTag));
    }

    public void layoutWrapperPageElement(WrapperPageElement wrapperPageElement) {
        LayouterWrapperType wrapperType = wrapperPageElement.getWrapperType();
        if (wrapperType == LayouterWrapperType.NONE) {
            visitSubElements(wrapperPageElement);
            return;
        }
        String wrappingElement = getHtmlElementByWrappingType(wrapperType);
        append(HtmlUtil.createHtmlElementOpenTag(wrappingElement));
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
        return "span";
    }

    public void layoutRootPageElement(RootPageElement pageElement) {
        clean();
        append(HtmlUtil.createHtmlHead(pageElement.getTitle()));
        visitSubElements(pageElement);
        append(HtmlUtil.createHtmlFoot());
    }

    public void layoutTextPageElement(TextPageElement pageElement) {
        if (pageElement.getType() == TextType.WITHOUT_TYPE) {
            append(pageElement.getText());
            return;
        }
        append(HtmlUtil.createHtmlElement(identifyTagName(pageElement), pageElement.getText()));
    }

    private String identifyTagName(TextPageElement textPageElement) {
        return HtmlTextType.getHtmlTextTypeByTextType(textPageElement.getType()).getTagName();
    }

    protected void visitSubElements(ICompositePageElement pageElement) {
        pageElement.visitSubElements(this);
    }
}
