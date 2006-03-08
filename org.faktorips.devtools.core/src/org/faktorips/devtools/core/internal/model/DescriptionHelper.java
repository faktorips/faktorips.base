/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.w3c.dom.Element;


/**
 * @author Jan Ortmann
 */
public class DescriptionHelper {
    
    public final static String XML_ATTRIBUTE_NAME = "description"; //$NON-NLS-1$
    
    /**
     * Adds the description to the element.
     */
    public final static void setDescription(Element parentElement, String description) {
        parentElement.setAttribute(XML_ATTRIBUTE_NAME, description);
    }

    /**
     * Returns the description from the xml element. Returns an empty string if the element
     * does not contain a description. 
     */
    public final static String getDescription(Element element) {
        return element.getAttribute(XML_ATTRIBUTE_NAME);
    }

    private DescriptionHelper() {
    }

}
