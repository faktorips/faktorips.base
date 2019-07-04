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
    public Set<Style> getStyles();

    /**
     * adds {@link Style}s and returns this
     * 
     */
    public IPageElement addStyles(Style... style);

    /**
     * checks, whether IPageElement has the Style
     * 
     */
    public boolean hasStyle(Style style);

    /**
     * removes the given {@link Style}s
     * 
     */
    public void removeStyles(Style... style);

    /**
     * accepts an {@link ILayouter}
     * 
     */
    public void acceptLayouter(ILayouter layouter);

    /**
     * builds the content of the node
     */
    public void build();

    /**
     * changes the IPageElement to a block
     */
    public void makeBlock();

    public String getId();

    public void setId(String id);

    public String getAnchor();

    public void setAnchor(String anchor);
}
