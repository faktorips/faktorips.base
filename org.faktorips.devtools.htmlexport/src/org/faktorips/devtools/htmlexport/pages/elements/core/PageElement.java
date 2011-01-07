/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * The PageElement is a node in the tree, which represents the exported information
 * 
 * @author dicker
 * 
 */
public interface PageElement {

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
    public PageElement addStyles(Style... style);

    /**
     * checks, whether PageElement has the Style
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
     * changes the PageElement to a block
     */
    public void makeBlock();

    public String getId();

    public void setId(String id);

    public String getAnchor();

    public void setAnchor(String anchor);
}
