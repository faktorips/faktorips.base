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

import java.util.ArrayList;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * {@link AbstractRootPageElement} is the abstract implementation of the root of the page
 * 
 * @author dicker
 * 
 */
public abstract class AbstractRootPageElement extends AbstractCompositePageElement {

    public AbstractRootPageElement(DocumentationContext context) {
        super(context);
    }

    @Override
    protected void buildInternal() {
        createId();
        setSubElements(new ArrayList<IPageElement>());
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutRootPageElement(this);
    }

    /**
     * @return path to the root (used for setting the right relative path in links from the page
     */
    public abstract String getPathToRoot();

    /**
     * @return true, if this PageElement represents a unit of content
     */
    public abstract boolean isContentUnit();
}
