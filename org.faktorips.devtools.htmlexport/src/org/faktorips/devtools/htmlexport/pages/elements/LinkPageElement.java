package org.faktorips.devtools.htmlexport.pages.elements;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.pages.PageElement;
import org.faktorips.devtools.htmlexport.pages.PageElementType;

public class LinkPageElement extends AbstractPageElement {
    private IIpsElement from;
    private IIpsElement to;
    private String target;
    private PageElement text;

    public LinkPageElement(IIpsElement from, IIpsElement to, PageElement text, String target) {
        super();
        this.from = from;
        this.to = to;
        this.text = text;
        this.target = target;
    }

    public LinkPageElement(IIpsElement from, IIpsElement to, PageElement text) {
        this(from, to, text, null);
    }

    public PageElementType getPageElementType() {
        return PageElementType.LINK;
    }

    public String getTarget() {
        return target;
    }

    public PageElement getText() {
        return text;
    }

    public IIpsElement getFrom() {
        return from;
    }

    public IIpsElement getTo() {
        return to;
    }

    public void acceptLayouter(ILayouter layoutVisitor) {
        // TODO Auto-generated method stub
        
    }

    public void acceptLayouter(ILayouter layoutVisitor, LayouterWrapperType wrapperType) {
        // TODO Auto-generated method stub
        
    }
}
