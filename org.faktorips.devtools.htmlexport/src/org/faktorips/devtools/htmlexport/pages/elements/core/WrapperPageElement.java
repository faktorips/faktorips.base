package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.WrapperType;

/**
 * {@link WrapperPageElement} represents element in complex structures like tables, lists, trees
 * e.g. listitems, tablerows etc.
 * 
 * @author dicker
 * 
 */
public class WrapperPageElement extends AbstractCompositePageElement {
    protected WrapperType wrapperType;

    /**
     * creates an empty {@link WrapperPageElement}
     * 
     * @param wrapperType
     */
    public WrapperPageElement(WrapperType wrapperType) {
        this.wrapperType = wrapperType;
    }

    /**
     * creates an {@link WrapperPageElement} with the given {@link PageElement}s
     * 
     * @param wrapperType
     * @param pageElements
     */
    public WrapperPageElement(WrapperType wrapperType, PageElement... pageElements) {
        this(wrapperType, null, pageElements);
    }

    /**
     * creates an {@link WrapperPageElement} with the given {@link PageElement}s and the given
     * styles
     * 
     * @param wrapperType
     * @param styles
     * @param pageElements
     */
    public WrapperPageElement(WrapperType wrapperType, Set<Style> styles, PageElement... pageElements) {
        super();
        addPageElements(pageElements);
        if (styles != null) {
            addStyles(styles.toArray(new Style[styles.size()]));
        }
        this.wrapperType = wrapperType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement#build()
     */
    @Override
    public void build() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement#acceptLayouter
     * (org.faktorips.devtools.htmlexport.generators.ILayouter)
     */
    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutWrapperPageElement(this);
    }

    /**
     * @return the wrapperType
     */
    public WrapperType getWrapperType() {
        return wrapperType;
    }

    /**
     * sets the {@link WrapperType}
     * 
     * @param wrapperType
     */
    public void setWrapperType(WrapperType wrapperType) {
        this.wrapperType = wrapperType;
    }

    @Override
    public void makeBlock() {
        if (getWrapperType() == WrapperType.NONE) {
            setWrapperType(WrapperType.BLOCK);
        }
    }
}
