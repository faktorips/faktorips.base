/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 **************************************************************************************************/
package org.faktorips.runtime.internal.java5;

import org.faktorips.runtime.internal.Range;
import org.w3c.dom.Element;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ValueToXmlHelper {
    /**
     * Adds the value to the given xml element. Takes care of proper null handling. By value we mean
     * a value of a datatype, e.g. 42EUR is a value of the datatype money.
     * 
     * @param value the string representation of the value
     * @param el the xml element.
     * @param tagName the tag name for the element that stored the value
     */
    public final static void addValueToElement(String value, Element el, String tagName) {
        org.faktorips.runtime.internal.ValueToXmlHelper.addValueToElement(value, el, tagName);
    }

    /**
     * Returns the string representation of the value stored in the child element of the given
     * element with the indicated name. Returns <code>null</code> if the value is null or no such
     * child element exists.
     * 
     * <pre>
     *     &lt;Parent&gt;
     *         &lt;Property isNull=&quot;false&quot;&gt;42&lt;/Property&gt;
     *     &lt;/Parent&gt;
     * </pre>
     * 
     * @param el The xml element that is the parent of the element storing the value.
     * @param tagName The name of the child
     */
    public final static String getValueFromElement(Element el, String tagName) {
        return org.faktorips.runtime.internal.ValueToXmlHelper.getValueFromElement(el, tagName);
    }

    public final static Range getRangeFromElement(Element el, String tagName) {
        return org.faktorips.runtime.internal.ValueToXmlHelper.getRangeFromElement(el, tagName);
    }

    public final static EnumValues getEnumValueSetFromElement(Element el, String tagName) {
        return new EnumValues(org.faktorips.runtime.internal.ValueToXmlHelper.getEnumValueSetFromElement(el, tagName));
    }

    private ValueToXmlHelper() {
    }
}
