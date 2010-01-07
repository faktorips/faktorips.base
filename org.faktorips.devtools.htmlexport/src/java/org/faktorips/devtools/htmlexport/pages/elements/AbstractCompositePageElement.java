package org.faktorips.devtools.htmlexport.pages.elements;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;

public abstract class AbstractCompositePageElement extends AbstractPageElement implements ICompositePageElement {
    protected List<PageElement> subElements = new ArrayList<PageElement>();
    protected String title;
    protected final LayouterWrapperType wrapperType = LayouterWrapperType.NONE;

    public abstract void acceptLayouter(ILayouter layouter);

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public abstract void build();

    public void reset() {
        subElements = new ArrayList<PageElement>();
    }

    /**
     * fuegt dem Composite neue Elemente hinzu
     * @param pageElements
     * @throws ClassCastException wenn nur bestimmte Typen an Elemente zugelassen werden
     */
    public void addPageElements(PageElement... pageElements) {
    	// TODO check auf richtigen Typen anders lösen (z.B. Filter) und Rückgabewert
        for (PageElement pageElement : pageElements) {
            checkPageElementType(pageElement);
        }
        for (PageElement pageElement : pageElements) {
        	subElements.add(pageElement);
        }
    }

    public void visitSubElements(ILayouter layouter) {
        for (PageElement subElement : subElements) {
            subElement.build();
            subElement.acceptLayouter(layouter);
        }
    }
    
    /**
     * wirft eine ClassCastException, wenn dem Composite ein Element vom falschen Typ uebergeben wird
     * @param pageElement
     */
    protected void checkPageElementType(PageElement pageElement) {}
}
