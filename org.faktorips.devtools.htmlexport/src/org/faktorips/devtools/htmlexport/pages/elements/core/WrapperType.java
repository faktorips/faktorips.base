/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * Classifies the kind of a {@link WrapperPageElement} for the {@link ILayouter}
 * 
 * @author dicker
 * 
 */
public enum WrapperType {
    LISTITEM,
    TABLEROW,
    TABLECELL,
    BLOCK,
    NONE
}
