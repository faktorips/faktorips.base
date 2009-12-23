package org.faktorips.devtools.htmlexport.pages.elements;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

public class LinkPageElement extends AbstractCompositePageElement {
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

    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutLinkPageElement(this);
    }

    @Override
    public void build() {
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
