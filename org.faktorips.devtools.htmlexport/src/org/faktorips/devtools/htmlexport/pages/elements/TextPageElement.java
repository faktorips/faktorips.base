package org.faktorips.devtools.htmlexport.pages.elements;

import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.htmlexport.pages.PageElementType;

public class TextPageElement extends AbstractPageElement {
    private String text;
    private TextType type;

    public TextPageElement(String text, Set<Style> styles, TextType type) {
        super(styles);
        this.text = text;
        this.type = type;
    }

    public TextPageElement(String text, TextType type) {
        this(text, new LinkedHashSet<Style>(), type);
    }

    public TextPageElement(String text, Set<Style> styles) {
        this(text, styles, TextType.INLINE);
    }

    public TextPageElement(String text) {
        this(text, new LinkedHashSet<Style>(), TextType.INLINE);
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

    public PageElementType getPageElementType() {
        return PageElementType.TEXT;
    }

    
    

}
