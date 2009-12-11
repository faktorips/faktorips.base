package org.faktorips.devtools.htmlexport.pages.elements;

import org.faktorips.devtools.htmlexport.pages.PageElementType;

public class RootPageElement extends CompositePageElement {

    @Override
    public PageElementType getPageElementType() {
        return PageElementType.ROOT;
    }

    @Override
    public void build() {
        reset();
    }
    
    
}
