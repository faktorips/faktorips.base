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

import java.util.Set;

import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;

/**
 * An {@link ILayouter} provides Methods for layouting the exported documentation
 * 
 * @author dicker
 */
public interface ILayouter extends IGenerator {

    /**
     * Layout for the Root of a Page
     * 
     * @param pageElement AbstractRootPageElement
     */
    void layoutRootPageElement(AbstractRootPageElement pageElement);

    /**
     * Layout for the Text
     * 
     * @param pageElement TextPageElement
     */
    void layoutTextPageElement(TextPageElement pageElement);

    /**
     * Layout for a Link
     * 
     * @param pageElement LinkPageElement
     */
    void layoutLinkPageElement(LinkPageElement pageElement);

    /**
     * Layout for a List
     * 
     * @param pageElement ListPageElement
     */
    void layoutListPageElement(ListPageElement pageElement);

    /**
     * Layout for a Table
     * 
     * @param pageElement TablePageElement
     */
    void layoutTablePageElement(TablePageElement pageElement);

    /**
     * Layout for a WrapperElement
     * 
     * @param pageElement WrapperPageElement
     */
    void layoutWrapperPageElement(AbstractCompositePageElement pageElement);

    /**
     * clears the data
     */
    void clear();

    /**
     * returns the {@link LayoutResource} for the Page
     */
    Set<LayoutResource> getLayoutResources();

    /**
     * Layout for an Image
     * 
     * @param pageElement ImagePageElement
     */
    void layoutImagePageElement(ImagePageElement pageElement);
}
