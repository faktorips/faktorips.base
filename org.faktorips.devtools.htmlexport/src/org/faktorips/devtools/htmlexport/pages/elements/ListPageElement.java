package org.faktorips.devtools.htmlexport.pages.elements;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.pages.PageElement;
import org.faktorips.devtools.htmlexport.pages.PageElementType;

public class ListPageElement extends CompositePageElement {
    
    protected List<PageElement> listElements = new ArrayList<PageElement>();

    public ListPageElement() {
        super();
    }

    public ListPageElement(List<PageElement> listElements) {
        super();
        this.listElements = listElements;
    }

    @Override
    public void build() {
        for (PageElement listElement : listElements) {
            addPageElement(listElement);
        }
    }
    
    public List<PageElement> getListElements() {
        return listElements;
    }

    public void addListElements(PageElement listElement) {
        listElements.add(listElement);
    }

    
    @Override
    protected LayouterWrapperType getWrapperType() {
        return LayouterWrapperType.LISTELEMENT;
    }

    @Override
    public PageElementType getPageElementType() {
        return PageElementType.LIST;
    }
}    
    

