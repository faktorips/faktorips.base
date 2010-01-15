package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

public interface PageElement {

    public Set<Style> getStyles();

    public void addStyles(Style... style);

    public void removeStyles(Style... style);

    public void acceptLayouter(ILayouter layouter);
    
    public void build();
}
