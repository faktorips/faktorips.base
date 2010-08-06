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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * @author dicker
 * 
 */
public abstract class AbstractPageElement implements PageElement {

    protected Set<Style> styles = new LinkedHashSet<Style>();

    public AbstractPageElement() {
        super();
    }

    public AbstractPageElement(Set<Style> styles) {
        super();
        this.styles = styles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.PageElement#getStyles()
     */
    @Override
    public Set<Style> getStyles() {
        if (styles == null) {
            return Collections.emptySet();
        }
        return new HashSet<Style>(styles);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.elements.core.PageElement#addStyles(org.faktorips
     * .devtools.htmlexport.pages.elements.core.Style[])
     */
    @Override
    public PageElement addStyles(Style... style) {
        styles.addAll(Arrays.asList(style));
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.elements.core.PageElement#removeStyles(org.faktorips
     * .devtools.htmlexport.pages.elements.core.Style[])
     */
    @Override
    public void removeStyles(Style... style) {
        styles.removeAll(Arrays.asList(style));
    }

    @Override
    public boolean hasStyle(Style style) {
        return getStyles().contains(style);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.PageElement#build()
     */
    @Override
    public void build() {
        // override in subclass
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.elements.core.PageElement#acceptLayouter(org.faktorips
     * .devtools.htmlexport.generators.ILayouter)
     */
    @Override
    public abstract void acceptLayouter(ILayouter layouter);

    @Override
    public void makeBlock() {
        addStyles(Style.BLOCK);
    }

}
