package org.faktorips.devtools.htmlexport.pages;

import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterVisitingMode;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.Style;

public interface PageElement {

    public Set<Style> getStyles();

    public void setStyles(Set<Style> styles);

    public void addStyle(Style style);
    
    public void acceptLayouter(ILayouter layoutVisitor);
    public void acceptLayouter(ILayouter layoutVisitor, LayouterWrapperType wrapperType);
    
    public PageElementType getPageElementType();
    
    public void build();
}
