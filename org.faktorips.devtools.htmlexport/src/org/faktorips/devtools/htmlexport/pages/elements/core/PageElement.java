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
     * adds {@link Style}s
     * 
     * @param style
     * @return this
     */
    public PageElement addStyles(Style... style);

    /**
     * checks, whether PageElement has the Style
     * 
     * @param style
     * @return boolean
     */
    public boolean hasStyle(Style style);

    /**
     * removes the given {@link Style}s
     * 
     * @param style
     */
    public void removeStyles(Style... style);

    /**
     * accepts an {@link ILayouter}
     * 
     * @param layouter
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
}
