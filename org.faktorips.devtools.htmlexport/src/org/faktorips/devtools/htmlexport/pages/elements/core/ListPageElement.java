/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.List;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.WrapperType;

/**
 * A {@link PageElement} representing a list.
 * 
 * @author dicker
 * 
 */
public class ListPageElement extends AbstractCompositePageElement {
    private boolean ordered = true;

    public ListPageElement() {
        super();
    }

    /**
     * @param listElements
     */
    public ListPageElement(List<? extends PageElement> listElements) {
        super();
        getSubElements().addAll(listElements);
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
        layouter.layoutListPageElement(this);
    }

    /**
     * @return true, if list is ordered
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * sets list ordered if ordered is true
     * 
     * @param ordered
     */
    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement#
     * visitSubElements(org.faktorips.devtools.htmlexport.generators.ILayouter)
     */
    @Override
    public void visitSubElements(ILayouter layouter) {
        for (PageElement subElement : getSubElements()) {
            layouter.layoutWrapperPageElement(new WrapperPageElement(WrapperType.LISTITEM, subElement));
        }
    }
}
