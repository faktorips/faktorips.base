package org.faktorips.devtools.htmlexport.pages.elements;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterVisitingMode;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.pages.PageElement;
import org.faktorips.devtools.htmlexport.pages.PageElementType;

public abstract class CompositePageElement extends AbstractPageElement {
    protected List<PageElement> elements = new ArrayList<PageElement>();
    protected String title;
    protected final LayouterWrapperType wrapperType = LayouterWrapperType.NONE;

    public void acceptLayouter(ILayouter layoutVisitor, LayouterWrapperType wrapperType) {
        layoutVisitor.layoutPageElement(this, wrapperType, LayouterVisitingMode.INIT);
        for (PageElement element : elements) {
            element.build();
            element.acceptLayouter(layoutVisitor, getWrapperType());
        }
        layoutVisitor.layoutPageElement(this, wrapperType, LayouterVisitingMode.FINALIZE);
    }

    public PageElementType getPageElementType() {
        return PageElementType.COMPOSITE;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public abstract void build();
    
    public void reset() {
        elements = new ArrayList<PageElement>();
    }
    
    public void addPageElement(PageElement pageElement) {
        elements.add(pageElement);
    }

    protected LayouterWrapperType getWrapperType() {
        return wrapperType;
    }
    
    
}
