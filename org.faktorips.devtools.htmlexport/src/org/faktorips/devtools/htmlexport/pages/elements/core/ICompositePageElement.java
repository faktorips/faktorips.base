/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

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
    public void visitSubElements(ILayouter layouter);

    /**
     * adds one or more {@link IPageElement}s and returns this
     * 
     */
    public ICompositePageElement addPageElements(IPageElement... pageElements);
}
