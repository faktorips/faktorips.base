package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.WrapperType;

/**
 * {@link AbstractCompositePageElement} provides basic functionality for implementations of the
 * {@link ICompositePageElement}
 * 
 * @author dicker
 * 
 */
public abstract class AbstractCompositePageElement extends AbstractPageElement implements ICompositePageElement {
    /**
     * the subElements of the CompositePageElement
     */
    protected List<PageElement> subElements = new ArrayList<PageElement>();

    protected String title;
    protected final WrapperType wrapperType = WrapperType.NONE;

    @Override
    public abstract void acceptLayouter(ILayouter layouter);

    /**
     * @return title of the CompositePageElement
     */
    public String getTitle() {
        return title;
    }

    /**
     * sets title of the CompositePageElement
     * 
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement#build()
     */
    @Override
    public abstract void build();

    /**
     * adds new {@link PageElement}s
     * 
     * @param pageElements
     * @return a reference to this ipsObject.
     * @throws ClassCastException if the given pageElements don't match restrictions for the
     *             CompositePageElement
     */
    public ICompositePageElement addPageElements(PageElement... pageElements) {
        for (PageElement pageElement : pageElements) {
            addSubElement(pageElement);
        }
        return this;
    }

    /**
     * adds a {@link PageElement}. Overwrite to check restrictions for subelements
     * 
     * @param pageElement
     */
    protected void addSubElement(PageElement pageElement) {
        subElements.add(pageElement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement#visitSubElements
     * (org.faktorips.devtools.htmlexport.generators.ILayouter)
     */
    public void visitSubElements(ILayouter layouter) {
        for (PageElement subElement : subElements) {
            subElement.build();
            subElement.acceptLayouter(layouter);
        }
    }

    /**
     * returns a list of the subelements
     * 
     * @return
     */
    public List<PageElement> getSubElements() {
        return subElements;
    }
}
