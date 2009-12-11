package org.faktorips.devtools.htmlexport.pages.elements;

import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterVisitingMode;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.pages.PageElement;

public abstract class AbstractPageElement implements PageElement {

    public Set<Style> styles = new LinkedHashSet<Style>();

    public AbstractPageElement() {
        super();
    }

    public AbstractPageElement(Set<Style> styles) {
        super();
        this.styles = styles;
    }

    public Set<Style> getStyles() {
        return styles;
    }

    public void setStyles(Set<Style> styles) {
        this.styles = styles;
    }

    public void addStyle(Style style) {
        styles.add(style);
    }

    public void build() {
    }

    public void acceptLayouter(ILayouter layoutVisitor, LayouterWrapperType wrapperType) {
        layoutVisitor.layoutPageElement(this, wrapperType, LayouterVisitingMode.COMPLETE);
    }

    public void acceptLayouter(ILayouter layoutVisitor) {
        acceptLayouter(layoutVisitor, LayouterWrapperType.NONE);
    }
}