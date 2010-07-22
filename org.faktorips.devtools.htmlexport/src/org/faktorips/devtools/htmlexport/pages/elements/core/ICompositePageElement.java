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

import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * An {@link ICompositePageElement} contains other PageElements
 * 
 * @author dicker
 * 
 */
public interface ICompositePageElement extends PageElement {

    /**
     * lets the {@link ILayouter} visit the subElements
     * 
     * @param layouter
     */
    public void visitSubElements(ILayouter layouter);

    /**
     * adds one or more {@link PageElement}s
     * 
     * @param pageElements
     * @return this
     */
    public ICompositePageElement addPageElements(PageElement... pageElements);
}
