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

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * {@link AbstractRootPageElement} is the abstract implementation of the root of the page
 * 
 * @author dicker
 * 
 */
public abstract class AbstractRootPageElement extends AbstractCompositePageElement {

    private String id;

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.elements.core.
     * AbstractCompositePageElement#build()
     */
    @Override
    public void build() {
        createId();
        subElements = new ArrayList<PageElement>();
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.elements.core. AbstractCompositePageElement
     * #acceptLayouter(org.faktorips.devtools.htmlexport.generators.ILayouter)
     */
    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutRootPageElement(this);
    }

    /**
     * @return path to the root (used for setting the right relative path in links from the page
     */
    public abstract String getPathToRoot();

    /**
     * creates the Id of a page e.g. for internal links
     * 
     */
    protected void createId() {
    };

    public boolean hasId() {
        return StringUtils.isNotBlank(getId());
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

}
