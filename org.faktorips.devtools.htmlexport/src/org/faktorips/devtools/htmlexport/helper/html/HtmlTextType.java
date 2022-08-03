/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.html;

import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;

/**
 * enum for html-text-elements
 * 
 * @author dicker
 * 
 */
public enum HtmlTextType {
    H1,
    H2,
    H3,
    H4,
    H5,
    H6,
    DIV,
    SPAN;

    /**
     * returns the HtmlTextType, which represents the given {@link TextType}
     * 
     */
    public static HtmlTextType getHtmlTextTypeByTextType(TextType textType) {
        if (textType == TextType.HEADING_1) {
            return H1;
        }
        if (textType == TextType.HEADING_2) {
            return H2;
        }
        if (textType == TextType.HEADING_3) {
            return H3;
        }
        if (textType == TextType.HEADING_4) {
            return H4;
        }
        if (textType == TextType.HEADING_5) {
            return H5;
        }
        if (textType == TextType.HEADING_6) {
            return H6;
        }
        if (textType == TextType.BLOCK) {
            return DIV;
        }
        return SPAN;
    }

    /**
     * returns tagName of the HtmlTextType
     * 
     */
    public String getTagName() {
        return name().toLowerCase();
    }
}
