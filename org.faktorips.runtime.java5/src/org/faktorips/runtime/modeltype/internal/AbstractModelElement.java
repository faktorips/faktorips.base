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

package org.faktorips.runtime.modeltype.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.IModelTypeLabel;

/**
 * 
 * @author Daniel Hohenberger
 */
public class AbstractModelElement implements IModelElement {

    private Map<String, Object> extPropertyValues;

    private String name;

    private IRuntimeRepository repository;

    protected Map<Locale, IModelTypeLabel> labelsByLocale = new HashMap<Locale, IModelTypeLabel>();

    public AbstractModelElement(IRuntimeRepository repository) {
        this.repository = repository;
    }

    public String getLabel(Locale locale) {
        IModelTypeLabel label = labelsByLocale.get(locale);
        return label == null || label.getValue().length() == 0 ? getName() : label.getValue();
    }

    public Object getExtensionPropertyValue(String propertyId) {
        if (extPropertyValues == null) {
            return null;
        }
        return extPropertyValues.get(propertyId);
    }

    /**
     * Sets the value of the extension property <code>propertyId</code>.
     */
    public void setExtensionPropertyValue(String propertyId, Object value) {
        if (extPropertyValues == null) {
            extPropertyValues = new HashMap<String, Object>(5);
        }
        extPropertyValues.put(propertyId, value);
    }

    public String getName() {
        return name;
    }

    public void initFromXml(XMLStreamReader parser) throws XMLStreamException {
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals(PROPERTY_NAME)) {
                this.name = parser.getAttributeValue(i);
            }
        }
        for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals(IModelTypeLabel.XML_WRAPPER_TAG)) {
                        initModelTypeLabelsFromXml(parser);
                    }
                    break;
            }
        }
    }

    private void initModelTypeLabelsFromXml(XMLStreamReader parser) throws XMLStreamException {
        for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals(IModelTypeLabel.XML_TAG)) {
                        IModelTypeLabel label = new ModelTypeLabel(this);
                        label.initFromXml(parser);
                        labelsByLocale.put(label.getLocale(), label);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals(IModelTypeLabel.XML_WRAPPER_TAG)) {
                        return;
                    }
                    break;
            }
        }
    }

    public Set<String> getExtensionPropertyIds() {
        if (extPropertyValues == null) {
            return new HashSet<String>(0);
        }
        return extPropertyValues.keySet();
    }

    public void initExtPropertiesFromXml(XMLStreamReader parser) throws XMLStreamException {
        for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals(EXTENSION_PROPERTIES_XML_TAG)) {
                        initExtPropertyValueFromXml(parser);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals(EXTENSION_PROPERTIES_XML_WRAPPER_TAG)) {
                        return;
                    }
                    break;
            }
        }
    }

    private void initExtPropertyValueFromXml(XMLStreamReader parser) throws XMLStreamException {
        String id = null;
        boolean isNull = true;
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals(EXTENSION_PROPERTIES_PROPERTY_ID)) {
                id = parser.getAttributeValue(i);
            } else if (parser.getAttributeLocalName(i).equals(EXTENSION_PROPERTIES_PROPERTY_NULL)) {
                isNull = Boolean.valueOf(parser.getAttributeValue(i)).booleanValue();
            }
        }
        if (isNull) {
            setExtensionPropertyValue(id, null);
        } else {
            for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
                switch (event) {
                    case XMLStreamConstants.CHARACTERS:
                        value.append(parser.getText().trim());
                        break;
                    case XMLStreamConstants.CDATA:
                        value.append(parser.getText().trim());
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (parser.getLocalName().equals(EXTENSION_PROPERTIES_XML_TAG)) {
                            setExtensionPropertyValue(id, value.toString());
                            return;
                        }
                        break;
                }
            }
        }
    }

    public IRuntimeRepository getRepository() {
        return repository;
    }

    /**
     * Loads the class indicated by the given name using the repository's class loader.
     */
    Class<?> loadClass(String className) {
        try {
            return Class.forName(className, true, repository.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
