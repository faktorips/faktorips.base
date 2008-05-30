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
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.modeltype.IModelElement;

/**
 * 
 * @author Daniel Hohenberger
 */
public class AbstractModelElement implements IModelElement {

    private Map<String, Object> extPropertyValues = new HashMap<String, Object>();
    private String name = null;

    /**
     * {@inheritDoc}
     */
    public Object getExtensionPropertyValue(String propertyId) {
        return extPropertyValues.get(propertyId);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    // TODO init

    /*
    public void initFromXml(Element objectEl, IRuntimeRepository productRepository) {
        NodeList extProps = ((Element)objectEl.getElementsByTagName("ExtensionProperties").item(0)).getChildNodes();
        for (int i = 0; i < extProps.getLength(); i++) {
            Element valueElement = (Element)extProps.item(i);
            String extPropId = valueElement.getAttribute("id");
            String isNull = valueElement.getAttribute("isNull");
            if (StringUtils.isEmpty(isNull) || !Boolean.valueOf(isNull).booleanValue()) {
                extPropertyValues.put(extPropId, XmlUtil.getCDATAorTextContent(valueElement));
            } else {
                extPropertyValues.put(extPropId, null);
            }
        }
    }*/

    /**
     * {@inheritDoc}
     */
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException {
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals("name")) {
                this.name = parser.getAttributeValue(i);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getExtensionPropertyIds() {
        return extPropertyValues.keySet();
    }

    /**
     * {@inheritDoc}
     */
    public void initExtPropertiesFromXml(XMLStreamReader parser) throws XMLStreamException {
        for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals("ExtensionProperty")) {
                        initExtPropertyValueFromXML(parser);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals("ExtensionProperties")) {
                        return;
                    }
                    break;
            }
        }
    }

    private void initExtPropertyValueFromXML(XMLStreamReader parser) throws XMLStreamException {
        String id = null;
        boolean isNull = true;
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals("id")) {
                id = parser.getAttributeValue(i);
            } else if (parser.getAttributeLocalName(i).equals("isNull")) {
                isNull = Boolean.valueOf(isNull).booleanValue();
            }
        }
        if (isNull) {
            extPropertyValues.put(id, null);
        }else{
            for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
                switch (event) {
                    case XMLStreamConstants.CHARACTERS:
                        value.append(parser.getText());
                        break;
                    case XMLStreamConstants.CDATA:
                        value.append(parser.getText());
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (parser.getLocalName().equals("ExtensionProperty")) {
                            extPropertyValues.put(id, value.toString());
                            return;
                        }
                        break;
                }
            }
        }
    }

}
