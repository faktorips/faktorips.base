package org.faktorips.devtools.htmlexport.pages.elements;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

public class RootPageElement extends AbstractCompositePageElement {

    @Override
    public void build() {
        reset();
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutRootPageElement(this);
    }
}
