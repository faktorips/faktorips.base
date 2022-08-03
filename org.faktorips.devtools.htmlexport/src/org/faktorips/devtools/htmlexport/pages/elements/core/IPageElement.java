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

import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * The IPageElement is a node in the tree, which represents the exported information
 * 
 * @author dicker
 * 
 */
public interface IPageElement {

    /**
     * returns a Set of {@link Style}s, which define the Style of the Node
     * 
     * @return Set of Styles
     */
    Set<Style> getStyles();

    /**
     * adds {@link Style}s and returns this
     * 
     */
    IPageElement addStyles(Style... style);

    /**
     * checks, whether IPageElement has the Style
     * 
     */
    boolean hasStyle(Style style);

    /**
     * removes the given {@link Style}s
     * 
     */
    void removeStyles(Style... style);

    /**
     * accepts an {@link ILayouter}
     * 
     */
    void acceptLayouter(ILayouter layouter);

    /**
     * builds the content of the node
     */
    void build();

    /**
     * changes the IPageElement to a block
     */
    void makeBlock();

    String getId();

    void setId(String id);

    String getAnchor();

    void setAnchor(String anchor);
}
