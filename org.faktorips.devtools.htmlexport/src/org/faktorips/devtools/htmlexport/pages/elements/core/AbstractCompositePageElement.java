/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

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
    protected List<IPageElement> subElements = new ArrayList<IPageElement>();

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
     * adds the given {@link IPageElement}s
     * 
     * @return a reference to this ipsObject.
     * @throws ClassCastException if the given pageElements don't match restrictions for the
     *             CompositePageElement
     */
    @Override
    public ICompositePageElement addPageElements(IPageElement... pageElements) {
        for (IPageElement pageElement : pageElements) {
            addSubElement(pageElement);
        }
        return this;
    }

    /**
     * adds a {@link IPageElement}. Override to check restrictions for subelements (e.g. a table
     * just should take tableRows)
     * 
     */
    protected void addSubElement(IPageElement pageElement) {
        subElements.add(pageElement);
    }

    @Override
    public void visitSubElements(ILayouter layouter) {
        for (IPageElement subElement : subElements) {
            subElement.build();
            subElement.acceptLayouter(layouter);
        }
    }

    /**
     * returns a list of the subelements
     * 
     */
    public List<IPageElement> getSubElements() {
        return subElements;
    }

    /**
     * returns the subelement t the spedified position
     * 
     */
    public IPageElement getSubElement(int index) {
        return subElements.get(index);
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

    public int size() {
        return subElements.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName()).append(": "); //$NON-NLS-1$
        for (IPageElement pageElement : getSubElements()) {
            sb.append(pageElement).append(';');
        }
        return getClass().getName();
    }

}
