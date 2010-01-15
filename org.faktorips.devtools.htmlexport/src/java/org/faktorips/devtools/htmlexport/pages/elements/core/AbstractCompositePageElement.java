package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;

public abstract class AbstractCompositePageElement extends AbstractPageElement implements ICompositePageElement {
    private List<PageElement> subElements = new ArrayList<PageElement>();
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
        for (PageElement pageElement : pageElements) {
        	addSubElement(pageElement);
        }
    }

    /**
     * Fügt eine {@link PageElement} hinzu. Kann überschrieben werden, um Typ zu überprüfen, um Parent zu setzen und Styles durchzureichen etc. 
     * @param pageElement
     */
	protected void addSubElement(PageElement pageElement) {
		subElements.add(pageElement);
	}

    public void visitSubElements(ILayouter layouter) {
        for (PageElement subElement : subElements) {
            subElement.build();
            subElement.acceptLayouter(layouter);
        }
    }

	public List<PageElement> getSubElements() {
		return subElements;
	}
}
