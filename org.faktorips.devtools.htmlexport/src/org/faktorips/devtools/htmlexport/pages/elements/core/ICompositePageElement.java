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

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * An {@link ICompositePageElement} contains other PageElements
 * 
 * @author dicker
 * 
 */
public interface ICompositePageElement extends IPageElement {

    /**
     * lets the {@link ILayouter} visit the subElements
     * 
     */
    void visitSubElements(ILayouter layouter);

    DocumentationContext getContext();

    /**
     * adds one or more {@link IPageElement}s and returns this
     * 
     */
    ICompositePageElement addPageElements(IPageElement... pageElements);
}
