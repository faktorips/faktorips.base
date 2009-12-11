package org.faktorips.devtools.htmlexport.pages;

import java.util.List;

public interface Page extends Iterable<PageElement> {
    public List<PageElement>getPageElement();
    
    
}
