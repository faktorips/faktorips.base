/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

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
    private WrapperType wrapperType = WrapperType.NONE;

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
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public abstract void build();

    /**
     * adds the given {@link PageElement}s
     * 
     * @return a reference to this ipsObject.
     * @throws ClassCastException if the given pageElements don't match restrictions for the
     *             CompositePageElement
     */
    @Override
    public ICompositePageElement addPageElements(PageElement... pageElements) {
        for (PageElement pageElement : pageElements) {
            addSubElement(pageElement);
        }
        return this;
    }

    /**
     * adds a {@link PageElement}. Overwrite to check restrictions for subelements (e.g. a table
     * just should take tableRows)
     * 
     */
    protected void addSubElement(PageElement pageElement) {
        subElements.add(pageElement);
    }

    @Override
    public void visitSubElements(ILayouter layouter) {
        for (PageElement subElement : subElements) {
            subElement.build();
            subElement.acceptLayouter(layouter);
        }
    }

    /**
     * returns a list of the subelements
     * 
     */
    public List<PageElement> getSubElements() {
        return subElements;
    }

    public WrapperType getWrapperType() {
        return wrapperType;
    }

    protected void setWrapperType(WrapperType wrapperType) {
        this.wrapperType = wrapperType;
    }

    public boolean isEmpty() {
        return subElements.isEmpty();
    }

}
