package org.faktorips.devtools.htmlexport.helper.html;

import org.apache.commons.lang.NotImplementedException;
import org.faktorips.devtools.htmlexport.generators.AbstractLayouter;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterVisitingMode;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.helper.html.path.LinkedFileTypes;
import org.faktorips.devtools.htmlexport.pages.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextType;

public class HtmlLayouter extends AbstractLayouter implements ILayouter {

    public void layoutPageElement(PageElement pageElement, LayouterWrapperType wrapper, LayouterVisitingMode mode) {
        System.out.println(pageElement.getPageElementType() + "\t" + wrapper + "\t" + mode);
        switch (pageElement.getPageElementType()) {
            case TEXT:
                createText((TextPageElement) pageElement, wrapper);
                break;

            case ROOT:
                createRoot((RootPageElement) pageElement, mode);
                break;

            case LIST:
                createList((ListPageElement) pageElement, wrapper, mode);
                break;

            case LINK:
                createLink((LinkPageElement) pageElement, wrapper, mode);
                break;

            default:
                throw new NotImplementedException(pageElement.getPageElementType().toString() + " " + pageElement.getClass().getName());
        }
    }

    private void createLink(LinkPageElement pageElement, LayouterWrapperType wrapper, LayouterVisitingMode mode) {
        HtmlUtil.createLinkBase(pageElement.getFrom(), pageElement.getTo(), LinkedFileTypes.PACKAGE_CLASSES_OVERVIEW);
        
    }

    private void createList(ListPageElement pageElement, LayouterWrapperType wrapper, LayouterVisitingMode mode) {
        if (mode == LayouterVisitingMode.INIT) {
            append(HtmlUtil.createHtmlElementOpenTag("ul"));
        }
        append(HtmlUtil.createHtmlElementCloseTag("ul"));
    }

    private void createRoot(RootPageElement pageElement, LayouterVisitingMode mode) {
        if (mode == LayouterVisitingMode.INIT) {
            reset();
            append(HtmlUtil.createHtmlHead(pageElement.getTitle()));
            return;
        }
        append(HtmlUtil.createHtmlFoot());

    }

    protected void createText(TextPageElement pageElement, LayouterWrapperType wrapper) {
        if (pageElement.getType() == TextType.WITHOUT_TYPE) {
            append(pageElement.getText());
            return;
        }
        append(HtmlUtil.createHtmlElement(identifyTagName(pageElement), pageElement.getText()));
    }

    private String identifyTagName(TextPageElement textPageElement) {
        return HtmlTextType.getHtmlTextTypeByTextType(textPageElement.getType()).getTagName();
    }
    
    protected void append(String text, LayouterWrapperType wrapper, LayouterVisitingMode mode) {
        if (wrapper == LayouterWrapperType.NONE) {
            append(text);
            return;
        }
        String wrappingElement = getWrappingHtmlElement(wrapper);
        if (mode != LayouterVisitingMode.FINALIZE) append(HtmlUtil.createHtmlElementOpenTag(wrappingElement));
        append(text);
        if (mode != LayouterVisitingMode.INIT) append(HtmlUtil.createHtmlElementCloseTag(wrappingElement));
    }

    private String getWrappingHtmlElement(LayouterWrapperType wrapper) {
        if (wrapper == LayouterWrapperType.LISTELEMENT) return "li";
        if (wrapper == LayouterWrapperType.TABLECELL) return "td";
        return "span";
    }
}
