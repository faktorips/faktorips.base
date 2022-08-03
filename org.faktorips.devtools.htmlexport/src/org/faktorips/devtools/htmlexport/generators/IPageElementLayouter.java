/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators;

import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;

/**
 * Interface for Layouting of {@link IPageElement}s
 * 
 * @author dicker
 */
public interface IPageElementLayouter<T extends IPageElement> {

    /**
     * layouts the {@link IPageElement}
     */
    void layout();

}
