/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
    public void layoutRootPageElement(AbstractRootPageElement pageElement);

    /**
     * Layout for the Text
     * 
     * @param pageElement TextPageElement
     */
    public void layoutTextPageElement(TextPageElement pageElement);

    /**
     * Layout for a Link
     * 
     * @param pageElement LinkPageElement
     */
    public void layoutLinkPageElement(LinkPageElement pageElement);

    /**
     * Layout for a List
     * 
     * @param pageElement ListPageElement
     */
    public void layoutListPageElement(ListPageElement pageElement);

    /**
     * Layout for a Table
     * 
     * @param pageElement TablePageElement
     */
    public void layoutTablePageElement(TablePageElement pageElement);

    /**
     * Layout for a WrapperElement
     * 
     * @param pageElement WrapperPageElement
     */
    public void layoutWrapperPageElement(AbstractCompositePageElement pageElement);

    /**
     * clears the data
     */
    public void clear();

    /**
     * returns the {@link LayoutResource} for the Page
     */
    public Set<LayoutResource> getLayoutResources();

    /**
     * Layout for an Image
     * 
     * @param pageElement ImagePageElement
     */
    public void layoutImagePageElement(ImagePageElement pageElement);
}
