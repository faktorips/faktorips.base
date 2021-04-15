/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

    private Map<String, String> extPropertyValues = new HashMap<>();

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

    @Override
    public Set<String> getExtensionPropertyIds() {
        return extPropertyValues.keySet();
    }

    @Override
    public Object getExtensionPropertyValue(String propertyId) {
        return extPropertyValues.get(propertyId);
    }

}
