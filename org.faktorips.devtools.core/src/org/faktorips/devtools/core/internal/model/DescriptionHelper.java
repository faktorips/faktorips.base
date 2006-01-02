package org.faktorips.devtools.core.internal.model;

import org.w3c.dom.Element;


/**
 * @author Jan Ortmann
 */
public class DescriptionHelper {
    
    public final static String XML_ATTRIBUTE_NAME = "description";
    
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
