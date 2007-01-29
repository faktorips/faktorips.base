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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSetConfig;
import org.w3c.dom.Document;
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
     * Creates and returns an ips artefact builder set configuration instance from the provided dom element.
     */
    public final void initFromXml(Element el){
        properties = new HashMap();
        NodeList nl = el.getElementsByTagName("Property"); //$NON-NLS-1$
        for (int i = 0; i < nl.getLength(); i++) {
            Element propertyEl = (Element)nl.item(i);
            String key = propertyEl.getAttribute("name"); //$NON-NLS-1$
            String value = propertyEl.getAttribute("value"); //$NON-NLS-1$
            properties.put(key, value);
        }
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
    public boolean getBooleanPropertyValue(String propertName, boolean defaultValue) {
        String value = getPropertyValue(propertName);
        if(StringUtils.isEmpty(value)){
            return false;
        }
        return Boolean.valueOf(value).booleanValue();
    }
    
    public final Element toXml(Document doc) {
        Element root = doc.createElement(XML_ELEMENT);
        Set keys = properties.keySet();
        for (Iterator iter = keys.iterator();iter.hasNext();) {
            String key = (String)iter.next();
            String value = (String)properties.get(key);
            Element prop = doc.createElement("Property"); //$NON-NLS-1$
            root.appendChild(prop);
            prop.setAttribute("name", key); //$NON-NLS-1$
            prop.setAttribute("value", value); //$NON-NLS-1$
        }
        return root;
    }

}
