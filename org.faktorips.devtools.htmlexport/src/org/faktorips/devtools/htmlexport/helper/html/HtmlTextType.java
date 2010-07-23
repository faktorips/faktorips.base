/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
     * returns the HtmlTextType, which represents the {@link TextType}
     * 
     * @param textType
     * @return
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
        if (textType == TextType.INLINE) {
            return SPAN;
        }
        return SPAN;
    }

    /**
     * returns tagName of the HtmlTextType
     * 
     * @return
     */
    public String getTagName() {
        return name().toLowerCase();
    }
}
