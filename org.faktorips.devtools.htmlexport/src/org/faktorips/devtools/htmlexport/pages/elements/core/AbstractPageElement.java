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
    public void removeStyles(Style... style) {
        styles.removeAll(Arrays.asList(style));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.PageElement#build()
     */
    public void build() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.elements.core.PageElement#acceptLayouter(org.faktorips
     * .devtools.htmlexport.generators.ILayouter)
     */
    public abstract void acceptLayouter(ILayouter layouter);

}