/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.model.plugin.IpsStatus;

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
    private List<IPageElement> subElements = new ArrayList<>();
    private String title;
    private WrapperType wrapperType = WrapperType.NONE;

    public AbstractCompositePageElement(DocumentationContext context) {
        super(context);
    }

    @Override
    public abstract void acceptLayouter(ILayouter layouter);

    @Override
    public void build() {
        try {
            buildInternal();
            // CSOFF: IllegalCatch
        } catch (RuntimeException e) {
            IpsStatus ipsStatus = new IpsStatus(IStatus.ERROR, "A problem occured while procesing an object", e); //$NON-NLS-1$
            getContext().addStatus(ipsStatus);
            Set<Style> textStyles = new HashSet<>();
            textStyles.add(Style.BOLD);
            addPageElements(new TextPageElement(
                    "An error occured while processing current object: " + e.getClass().getSimpleName(), textStyles, //$NON-NLS-1$
                    getContext()));
        }
        // CSON: IllegalCatch
    }

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
     * Sets the List with subElements.
     * 
     * @param subElements the List with {@link IPageElement}s
     */
    protected void setSubElements(List<IPageElement> subElements) {
        this.subElements = subElements;
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
