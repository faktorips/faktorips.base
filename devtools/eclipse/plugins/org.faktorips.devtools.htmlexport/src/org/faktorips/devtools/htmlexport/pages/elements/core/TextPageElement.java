/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * A IPageElement representing a text<br>
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
     * @param context the current {@link DocumentationContext}
     * @throws NullPointerException if text is null
     */
    public TextPageElement(String text, Set<Style> styles, TextType type, DocumentationContext context) {
        super(styles, context);
        /*
         * TODO wieder einkommentieren sobald moeglich if (text == null) { throw new
         * NullPointerException("text must not be null"); //$NON-NLS-1$ }
         */
        this.text = text;
        this.type = type;
    }

    public TextPageElement(String text, TextType type, DocumentationContext context) {
        this(text, new LinkedHashSet<>(), type, context);
    }

    public TextPageElement(String text, Set<Style> styles, DocumentationContext context) {
        this(text, styles, TextType.WITHOUT_TYPE, context);
    }

    public TextPageElement(String text, DocumentationContext context) {
        this(text, new LinkedHashSet<>(), TextType.WITHOUT_TYPE, context);
    }

    /**
     * creates a {@link TextPageElement} representing a paragraph
     * 
     * @param context the current {@link DocumentationContext}
     * 
     */
    public static TextPageElement createParagraph(String text, DocumentationContext context) {
        return new TextPageElement(text, TextType.BLOCK, context);
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

    @Override
    public int hashCode() {
        return Objects.hash(text, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        TextPageElement other = (TextPageElement)obj;
        return Objects.equals(text, other.text) && type == other.type;
    }

    @Override
    public String toString() {
        return "TextPageElement:" + text; //$NON-NLS-1$
    }

    @Override
    protected void buildInternal() {
        // do nothing
    }
}
