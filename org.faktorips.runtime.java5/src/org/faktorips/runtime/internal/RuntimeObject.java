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

package org.faktorips.runtime.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.faktorips.runtime.IRuntimeObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Baseclass for all runtime objects
 * 
 */
public class RuntimeObject implements IRuntimeObject {

    private Map<String, String> extPropertyValues = new HashMap<String, String>();

    public RuntimeObject() {
        super();
    }

    protected void initExtensionPropertiesFromXml(Element cmptElement) {
        NodeList nl = cmptElement.getElementsByTagName("ExtensionProperties");
        if (nl == null || nl.getLength() == 0) {
            return;
        }
        nl = ((Element)nl.item(0)).getElementsByTagName("Value");
        for (int i = 0; i < nl.getLength(); i++) {
            Element childElement = (Element)nl.item(i);
            String id = childElement.getAttribute("id");
            if (Boolean.parseBoolean(childElement.getAttribute("isNull"))) {
                extPropertyValues.put(id, null);
            } else {
                String value = XmlUtil.getCDATAorTextContent(childElement);
                extPropertyValues.put(id, value);
            }
        }
    }

    protected void writeExtensionPropertiesToXml(Element element) {
        writeExtensionPropertiesToXml(element, extPropertyValues);
    }

    protected void writeExtensionPropertiesToXml(Element element, Map<String, String> extPropertyMap) {
        Element extPropRootElement = null;
        if (!extPropertyMap.isEmpty()) {
            extPropRootElement = element.getOwnerDocument().createElement("ExtensionProperties");
            element.appendChild(extPropRootElement);
        }
        for (Entry<String, String> extPropEntry : extPropertyMap.entrySet()) {
            Element extPropElement = ValueToXmlHelper.addValueAndReturnElement(extPropEntry.getValue(),
                    extPropRootElement, "Value");
            extPropElement.setAttribute("id", extPropEntry.getKey());
        }
    }

    public Set<String> getExtensionPropertyIds() {
        return extPropertyValues.keySet();
    }

    public Object getExtensionPropertyValue(String propertyId) {
        return extPropertyValues.get(propertyId);
    }

}
