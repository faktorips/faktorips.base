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

/**
 * Enum for marking a {@link TextPageElement} as heading, block, inline etc. element
 * 
 * @author dicker
 * 
 */
public enum TextType {
    HEADING_1(true),
    HEADING_2(true),
    HEADING_3(true),
    HEADING_4(true),
    HEADING_5(true),
    HEADING_6(true),
    BLOCK(true),
    INLINE(false),
    WITHOUT_TYPE(false);

    private final boolean blockType;

    private TextType(boolean blockType) {
        this.blockType = blockType;
    }

    public boolean isBlockType() {
        return blockType;
    }
}
