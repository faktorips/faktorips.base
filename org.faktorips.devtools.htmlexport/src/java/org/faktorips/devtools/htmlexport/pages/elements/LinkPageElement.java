package org.faktorips.devtools.htmlexport.pages.elements;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

public class LinkPageElement extends AbstractCompositePageElement {
    private IIpsElement from;
    private IIpsElement to;
    private String target;

    public LinkPageElement(IIpsElement from, IIpsElement to, String target, PageElement... pageElements) {
        super();
        this.from = from;
        this.to = to;
        this.target = target;
        addPageElements(pageElements);
    }

    public LinkPageElement(IIpsElement from, IIpsElement to, PageElement...pageElements) {
        this(from, to, null, pageElements);
    }

    public String getTarget() {
        return target;
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
