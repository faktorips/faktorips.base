/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractRuntimeRepository;
import org.faktorips.runtime.modeltype.IModelElement;

/**
 * 
 * @author Daniel Hohenberger
 */
public class AbstractModelElement implements IModelElement {

    private Map<String, Object> extPropertyValues = new HashMap<String, Object>();
    private String name = null;
    private AbstractRuntimeRepository repository;

    public AbstractModelElement(AbstractRuntimeRepository repository) {
        this.repository = repository;
    }

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
                    if (parser.getLocalName().equals("Value")) {
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
                isNull = Boolean.valueOf(parser.getAttributeValue(i)).booleanValue();
            }
        }
        if (isNull) {
            extPropertyValues.put(id, null);
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
                        if (parser.getLocalName().equals("Value")) {
                            extPropertyValues.put(id, value.toString());
                            return;
                        }
                        break;
                }
            }
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    public IRuntimeRepository getRepository() {
        return repository;
    }

    protected AbstractRuntimeRepository getAbstractRepository() {
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
