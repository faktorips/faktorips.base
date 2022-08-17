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

/**
 * A {@link DataPageElement} is a {@link IPageElement} representing simply structured data like
 * lists or tables
 * 
 * @author dicker
 * 
 */
public interface DataPageElement {
    /**
     * @return true, if there is no data to show
     */
    boolean isEmpty();
}
