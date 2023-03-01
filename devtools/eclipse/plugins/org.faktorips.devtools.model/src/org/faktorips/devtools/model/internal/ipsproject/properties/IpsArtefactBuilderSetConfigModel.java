/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.properties;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
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

    private static final String PROPERTY_XML_TAG = "Property"; //$NON-NLS-1$

    private static final String NAME_XML_ATTR = "name"; //$NON-NLS-1$

    private static final String VALUE_XML_ATTR = "value"; //$NON-NLS-1$

    private Map<String, String> properties;
    private Map<String, String> propertiesDescription;

    /**
     * Creates an empty IPS artifact builder set configuration instances.
     */
    public IpsArtefactBuilderSetConfigModel() {
        properties = new LinkedHashMap<>();
        propertiesDescription = new LinkedHashMap<>();
    }

    /**
     * This constructor is for test purposes only.
     */
    public IpsArtefactBuilderSetConfigModel(Map<String, String> properties) {
        ArgumentCheck.notNull(properties);
        this.properties = properties;
        propertiesDescription = new LinkedHashMap<>();
    }

    /**
     * Creates and returns an IPS artifact builder set configuration instance from the provided dom
     * element.
     */
    @Override
    public final void initFromXml(Element el) {
        properties = new LinkedHashMap<>();
        propertiesDescription = new LinkedHashMap<>();
        NodeList nl = el.getChildNodes();
        String commentBeforeElement = IpsStringUtils.EMPTY;
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Comment) {
                commentBeforeElement = ((Comment)node).getData();
            } else if (node.getNodeName().equals(PROPERTY_XML_TAG)) {
                Element propertyEl = (Element)node;
                String key = propertyEl.getAttribute(NAME_XML_ATTR);
                String value = propertyEl.getAttribute(VALUE_XML_ATTR);
                if (!commentBeforeElement.isEmpty()) {
                    propertiesDescription.put(key, commentBeforeElement);
                    commentBeforeElement = IpsStringUtils.EMPTY;
                } else {
                    readOldStyleComment(propertyEl, key);
                }
                properties.put(key, value);
            }
        }
    }

    /**
     * Before IPS 3.22 the comment was nested as a child element of the property element. This
     * method reads these comments if they still exists.
     */
    private void readOldStyleComment(Element propertyEl, String key) {
        NodeList propertyElNodeList = propertyEl.getChildNodes();
        for (int j = 0; j < propertyElNodeList.getLength(); j++) {
            Node child = propertyElNodeList.item(j);
            if (child instanceof Comment comment) {
                propertiesDescription.put(key, comment.getData());
            }
        }
    }

    @Override
    public String getPropertyDescription(String propertyName) {
        return propertiesDescription.get(propertyName);
    }

    @Override
    public String getPropertyValue(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * Sets the value of a property
     * 
     * @param propertyName the name of the property
     * @param value the value of the property
     */
    @Override
    public void setPropertyValue(String propertyName, String value, String description) {
        ArgumentCheck.notNull(propertyName);
        ArgumentCheck.notNull(value);
        properties.put(propertyName, value);
        if (description != null) {
            propertiesDescription.put(propertyName, description);
        }
    }

    public void removeProperty(String propertyName) {
        properties.remove(propertyName);
    }

    @Override
    public final Element toXml(Document doc) {
        Element root = doc.createElement(XML_ELEMENT);
        Set<String> keys = properties.keySet();
        for (String key : keys) {
            String value = properties.get(key);
            String description = propertiesDescription.get(key);
            if (description != null) {
                Comment comment = doc.createComment(description);
                root.appendChild(comment);
            }
            Element prop = doc.createElement(PROPERTY_XML_TAG);
            root.appendChild(prop);
            prop.setAttribute(NAME_XML_ATTR, key);
            prop.setAttribute(VALUE_XML_ATTR, value);
        }
        return root;
    }

    @Override
    public String[] getPropertyNames() {
        return properties.keySet().toArray(new String[properties.size()]);
    }

    @Override
    public IIpsArtefactBuilderSetConfig create(IIpsProject ipsProject, IIpsArtefactBuilderSetInfo builderSetInfo) {
        Map<String, Object> parsedValueMap = new LinkedHashMap<>();
        for (String name : properties.keySet()) {
            IIpsBuilderSetPropertyDef propertyDef = builderSetInfo.getPropertyDefinition(name);
            if (propertyDef == null) {
                /*
                 * Ignore properties without property definition. Let a migration remove them as
                 * necessary.
                 */
                continue;
            }
            String valueAsString = properties.get(name);
            if (IpsStringUtils.isNotBlank(valueAsString)) {
                Object value = propertyDef.parseValue(valueAsString);
                parsedValueMap.put(name, value);
            }
        }
        IIpsBuilderSetPropertyDef[] propertyDefs = builderSetInfo.getPropertyDefinitions();
        for (IIpsBuilderSetPropertyDef propertyDef : propertyDefs) {
            Object value = parsedValueMap.get(propertyDef.getName());
            if (value == null) {
                parsedValueMap.put(propertyDef.getName(),
                        propertyDef.parseValue(propertyDef.getDisableValue(ipsProject)));
            }
        }
        return new IpsArtefactBuilderSetConfig(parsedValueMap);
    }

    @Override
    public MessageList validate(IIpsProject ipsProject, IIpsArtefactBuilderSetInfo builderSetInfo) {
        return builderSetInfo.validateIpsArtefactBuilderSetConfig(ipsProject, this);
    }

}
