package org.faktorips.devtools.htmlexport.helper.html;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.faktorips.devtools.htmlexport.generators.AbstractLayouter;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileTypes;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;

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

    
    
    public void layoutTablePageElement(TablePageElement pageElement) {
		HtmlAttribute[] attributes = {new HtmlAttribute("border", "1")};
		append(HtmlUtil.createHtmlElementOpenTag("table", attributes));
		visitSubElements(pageElement);
		append(HtmlUtil.createHtmlElementCloseTag("table"));
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
        if (wrapper == LayouterWrapperType.BLOCK)
            return "div";
        return "span";
    }

    public void layoutRootPageElement(RootPageElement pageElement) {
        clean();
        append(HtmlUtil.createHtmlHead(pageElement.getTitle()));
        visitSubElements(pageElement);
        
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        append(HtmlUtil.createHtmlElement("small", "Created " + sdf.format(new Date())));

        
        append(HtmlUtil.createHtmlFoot());
    }

    public void layoutTextPageElement(TextPageElement pageElement) {
        if (pageElement.getType() == TextType.WITHOUT_TYPE) {
            append(HtmlUtil.prepareText(pageElement.getText()));
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
