/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The default implementation of the IIpsArtefactBuilderSetConfig interface
 * 
 * @author Peter Erzberger
 */
public class IpsArtefactBuilderSetConfigModel implements IIpsArtefactBuilderSetConfigModel {

    private Map<String, String> properties;
    private Map<String, String> propertiesDescription;

    /**
     * Creates an empty IPS artifact builder set configuration instances.
     */
    public IpsArtefactBuilderSetConfigModel() {
        properties = new LinkedHashMap<String, String>();
        propertiesDescription = new LinkedHashMap<String, String>();
    }

    /**
     * This constructor is for test purposes only.
     */
    public IpsArtefactBuilderSetConfigModel(Map<String, String> properties) {
        ArgumentCheck.notNull(properties);
        this.properties = properties;
        propertiesDescription = new LinkedHashMap<String, String>();
    }

    /**
     * Creates and returns an IPS artifact builder set configuration instance from the provided dom
     * element.
     */
    public final void initFromXml(Element el) {
        properties = new LinkedHashMap<String, String>();
        propertiesDescription = new LinkedHashMap<String, String>();
        NodeList nl = el.getElementsByTagName("Property"); //$NON-NLS-1$
        for (int i = 0; i < nl.getLength(); i++) {
            Element propertyEl = (Element)nl.item(i);
            String key = propertyEl.getAttribute("name"); //$NON-NLS-1$
            String value = propertyEl.getAttribute("value"); //$NON-NLS-1$
            NodeList propertyElNodeList = propertyEl.getChildNodes();
            for (int j = 0; j < propertyElNodeList.getLength(); j++) {
                Node node = propertyElNodeList.item(j);
                if (node instanceof Comment) {
                    Comment comment = (Comment)node;
                    propertiesDescription.put(key, comment.getData());
                }
            }
            properties.put(key, value);
        }
    }

    public String getPropertyDescription(String propertyName) {
        return propertiesDescription.get(propertyName);
    }

    public String getPropertyValue(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * Sets the value of a property
     * 
     * @param propertyName the name of the property
     * @param value the value of the property
     */
    public void setPropertyValue(String propertyName, String value, String description) {
        ArgumentCheck.notNull(propertyName);
        ArgumentCheck.notNull(value);
        properties.put(propertyName, value);
        if (description != null) {
            propertiesDescription.put(propertyName, description);
        }
    }

    public final Element toXml(Document doc) {
        Element root = doc.createElement(XML_ELEMENT);
        Set<String> keys = properties.keySet();
        for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
            String key = iter.next();
            String value = properties.get(key);
            Element prop = doc.createElement("Property"); //$NON-NLS-1$
            String description = propertiesDescription.get(key);
            if (description != null) {
                Comment comment = doc.createComment(description);
                prop.appendChild(comment);
            }
            root.appendChild(prop);
            prop.setAttribute("name", key); //$NON-NLS-1$
            prop.setAttribute("value", value);
        }
        return root;
    }

    public String[] getPropertyNames() {
        return properties.keySet().toArray(new String[properties.size()]);
    }

    public IIpsArtefactBuilderSetConfig create(IIpsProject ipsProject, IIpsArtefactBuilderSetInfo builderSetInfo) {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        for (Iterator<String> it = this.properties.keySet().iterator(); it.hasNext();) {
            String name = it.next();
            IIpsBuilderSetPropertyDef propertyDef = builderSetInfo.getPropertyDefinition(name);
            if (propertyDef == null) {
                throw new IllegalStateException("The property: " + name
                        + " of this builder set configuration is not defined in the provided for the builder set: "
                        + builderSetInfo.getBuilderSetId());
            }
            String valueAsString = this.properties.get(name);
            if (!propertyDef.isAvailable(ipsProject)) {
                properties.put(name, propertyDef.parseValue(propertyDef.getDisableValue(ipsProject)));
                continue;
            }
            Object value = propertyDef.parseValue(valueAsString);
            properties.put(name, value);
        }
        IIpsBuilderSetPropertyDef[] propertyDefs = builderSetInfo.getPropertyDefinitions();
        for (int i = 0; i < propertyDefs.length; i++) {
            Object value = properties.get(propertyDefs[i].getName());
            if (value == null) {
                properties.put(propertyDefs[i].getName(), propertyDefs[i].parseValue(propertyDefs[i]
                        .getDisableValue(ipsProject)));
            }
        }
        return new IpsArtefactBuilderSetConfig(properties);
    }

    public MessageList validate(IIpsProject ipsProject, IpsArtefactBuilderSetInfo builderSetInfo) {
        return builderSetInfo.validateIpsArtefactBuilderSetConfig(ipsProject, this);
    }

}
