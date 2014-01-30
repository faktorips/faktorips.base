/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * {@link WrapperPageElement} represents element in complex structures like tables, lists, trees
 * e.g. listitems, tablerows etc.
 * 
 * @author dicker
 * 
 */
public class WrapperPageElement extends AbstractCompositePageElement {

    /**
     * creates an empty {@link WrapperPageElement} with the given {@link WrapperType} (e.g.
     * LISTITEM, TABLECELL, BLOCK)
     * 
     */
    public WrapperPageElement(WrapperType wrapperType) {
        setWrapperType(wrapperType);
    }

    /**
     * creates an {@link WrapperPageElement} with the given {@link IPageElement}s
     * 
     */
    public WrapperPageElement(WrapperType wrapperType, IPageElement... pageElements) {
        this(wrapperType, null, pageElements);
    }

    /**
     * creates an {@link WrapperPageElement} with the given {@link IPageElement}s and the given
     * styles
     * 
     */
    public WrapperPageElement(WrapperType wrapperType, Set<Style> styles, IPageElement... pageElements) {
        super();
        addPageElements(pageElements);
        if (styles != null) {
            addStyles(styles.toArray(new Style[styles.size()]));
        }
        setWrapperType(wrapperType);
    }

    @Override
    public void build() {
        // could be overridden
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutWrapperPageElement(this);
    }

    @Override
    public void makeBlock() {
        if (getWrapperType() == WrapperType.NONE) {
            setWrapperType(WrapperType.BLOCK);
        }
    }
}
