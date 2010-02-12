package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

public interface ICompositePageElement extends PageElement {
    public void visitSubElements(ILayouter layouter);
    
    public <T extends ICompositePageElement> T addPageElements(PageElement... pageElements);
}
