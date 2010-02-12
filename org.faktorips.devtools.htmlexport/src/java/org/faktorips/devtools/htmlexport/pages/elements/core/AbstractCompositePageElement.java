package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.WrapperType;

public abstract class AbstractCompositePageElement extends AbstractPageElement implements ICompositePageElement {
    private List<PageElement> subElements = new ArrayList<PageElement>();
    protected String title;
    protected final WrapperType wrapperType = WrapperType.NONE;

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
     * @return a reference to this object.
     * @throws ClassCastException wenn nur bestimmte Typen an Elemente zugelassen werden
     */
    public ICompositePageElement addPageElements(PageElement... pageElements) {
        for (PageElement pageElement : pageElements) {
        	addSubElement(pageElement);
        }
		return this;
    }

    /**
     * Fügt ein {@link PageElement} hinzu. Kann überschrieben werden, um Typ zu überprüfen, um Parent zu setzen und Styles durchzureichen etc. 
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
