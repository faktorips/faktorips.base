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

    TextType(boolean blockType) {
        this.blockType = blockType;
    }

    public boolean isBlockType() {
        return blockType;
    }
}
