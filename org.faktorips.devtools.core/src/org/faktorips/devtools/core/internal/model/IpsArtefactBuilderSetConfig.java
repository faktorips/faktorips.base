/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.devtools.core.model.IIpsArtefactBuilderSetConfig;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The default implementation of the IIpsArtefactBuilderSetConfig interface
 * 
 * @author Peter Erzberger
 */
public class IpsArtefactBuilderSetConfig implements IIpsArtefactBuilderSetConfig {

    private Map properties;
    
    /**
     * Creates an empty ips artefact builder set configuration instances.
     */
    public IpsArtefactBuilderSetConfig() {
        properties = new HashMap();
    }

    /**
     * Creates an ips artefact builder set configuration instance with the properties contained in the provided map. 
     */
    private IpsArtefactBuilderSetConfig(Map propertyValues) {
        super();
        this.properties = propertyValues;
    }

    /**
     * Creates and returns an ips artefact builder set configuration instance from the provided dom element.
     */
    public final static IpsArtefactBuilderSetConfig createFromXml(Element el){
        HashMap properties = new HashMap();
        NodeList nl = el.getElementsByTagName("Property");
        for (int i = 0; i < nl.getLength(); i++) {
            Element propertyEl = (Element)nl.item(i);
            String key = propertyEl.getAttribute("name");
            String value = propertyEl.getAttribute("value");
            properties.put(key, value);
        }
        return new IpsArtefactBuilderSetConfig(properties);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getPropertyValue(String propertyName) {
        return (String)properties.get(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    public Boolean getBooleanPropertyValue(String propertName) {
        String value = getPropertyValue(propertName);
        if(value == null){
            return null;
        }
        return Boolean.valueOf(value);
    }

}
