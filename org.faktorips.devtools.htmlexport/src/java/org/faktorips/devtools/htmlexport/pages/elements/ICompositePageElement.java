package org.faktorips.devtools.htmlexport.pages.elements;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

public interface ICompositePageElement {
    public void visitSubElements(ILayouter layouter);
}
