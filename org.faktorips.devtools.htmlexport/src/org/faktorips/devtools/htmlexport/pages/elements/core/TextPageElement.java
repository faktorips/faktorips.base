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

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * A IPageElement representing a text<br/>
 * 
 * The {@link TextPageElement} contains a {@link TextType}, which marks the text e.g. as a complete
 * block or as a heading
 * 
 * @author dicker
 * 
 */
public class TextPageElement extends AbstractPageElement {
    /**
     * Text, which is represented
     */
    private String text;
    /**
     * Type of the text e.g headline, block
     */
    private TextType type;

    /**
     * creates a {@link TextPageElement} representing a paragraph
     * 
     */
    public static TextPageElement createParagraph(String text) {
        return new TextPageElement(text, TextType.BLOCK);
    }

    /**
     * @throws NullPointerException if text is null
     */
    public TextPageElement(String text, Set<Style> styles, TextType type) {
        super(styles);
        /*
         * TODO wieder einkommentieren sobald moeglich if (text == null) { throw new
         * NullPointerException("text must not be null"); //$NON-NLS-1$ }
         */
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

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * sets the given text
     * 
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
     * 
     */
    public void setType(TextType type) {
        this.type = type;
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutTextPageElement(this);
    }

    @Override
    public void makeBlock() {
        if (getType().isBlockType()) {
            return;
        }
        type = TextType.BLOCK;
    }

}
