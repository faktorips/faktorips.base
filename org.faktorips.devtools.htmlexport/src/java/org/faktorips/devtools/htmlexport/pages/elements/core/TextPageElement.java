package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * A PageElement representing a text<br/>
 * 
 * The {@link TextPageElement} contains a {@link TextType}, which marks the text e.g. as a complete block or as a heading
 * 
 * @author dicker
 *
 */
public class TextPageElement extends AbstractPageElement {
    private String text;
    private TextType type;

    /**
     * creates a {@link TextPageElement} representing a paragraph
     * @param text
     * @return 
     */
    public static TextPageElement createParagraph(String text) {
    	return new TextPageElement(text, TextType.BLOCK);
    }
    
    /**
     * @param text
     * @param styles
     * @param type
     */
    public TextPageElement(String text, Set<Style> styles, TextType type) {
        super(styles);
        this.text = text;
        this.type = type;
    }

    /**
     * @param text
     * @param type
     */
    public TextPageElement(String text, TextType type) {
        this(text, new LinkedHashSet<Style>(), type);
    }

    /**
     * @param text
     * @param styles
     */
    public TextPageElement(String text, Set<Style> styles) {
        this(text, styles, TextType.WITHOUT_TYPE);
    }

    /**
     * @param text
     */
    public TextPageElement(String text) {
        this(text, new LinkedHashSet<Style>(), TextType.WITHOUT_TYPE);
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * sets the given text
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the {@link TextType}
     */
    public TextType getType() {
        return type;
    }

    /**
     * sets the given {@link TextType}
     * @param type
     */
    public void setType(TextType type) {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement#acceptLayouter(org.faktorips.devtools.htmlexport.generators.ILayouter)
     */
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutTextPageElement(this);
    }
}
