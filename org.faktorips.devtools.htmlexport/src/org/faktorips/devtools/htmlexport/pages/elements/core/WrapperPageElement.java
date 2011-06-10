/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
