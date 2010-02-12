package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

public class TextPageElement extends AbstractPageElement {
    private String text;
    private TextType type;

    public static TextPageElement createParagraph(String text) {
    	return new TextPageElement(text, TextType.BLOCK);
    }
    
    public TextPageElement(String text, Set<Style> styles, TextType type) {
        super(styles);
        this.text = text;
        this.type = type;
    }

    public TextPageElement(String text, TextType type) {
        this(text, new LinkedHashSet<Style>(), type);
    }

    public TextPageElement(String text, Set<Style> styles) {
        this(text, styles, TextType.WITHOUT_TYPE);
    }

    public TextPageElement(String text) {
        this(text, new LinkedHashSet<Style>(), TextType.WITHOUT_TYPE);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TextType getType() {
        return type;
    }

    public void setType(TextType type) {
        this.type = type;
    }

    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutTextPageElement(this);
    }
    
    public static TextPageElement newBlock(String text) {
    	return new TextPageElement(text, TextType.BLOCK);
    }
}
