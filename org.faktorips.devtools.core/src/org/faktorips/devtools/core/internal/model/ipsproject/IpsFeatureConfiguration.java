/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.faktorips.devtools.core.model.ipsproject.IIpsFeatureConfiguration;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public class IpsFeatureConfiguration implements IIpsFeatureConfiguration {

    static final String FEATURE_CONFIGURATION_ELEMENT = "FeatureConfiguration"; //$NON-NLS-1$
    static final String PROPERTY_ELEMENT = "Property"; //$NON-NLS-1$
    static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
    static final String VALUE_ATTRIBUTE = "value"; //$NON-NLS-1$

    private final Map<String, String> properties = new LinkedHashMap<String, String>();

    @Override
    public @CheckForNull String get(String key) {
        return properties.get(key);
    }

    /**
     * Sets the property identified by the given name to the given value. If the given value is
     * {@code null}, the property is removed.
     */
    public void set(String name, @CheckForNull String value) {
        ArgumentCheck.notNull(name, "Property name must not be null"); //$NON-NLS-1$
        if (value == null) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
    }

    Element toXml(Document doc) {
        Element featureConfigurationElement = doc.createElement(FEATURE_CONFIGURATION_ELEMENT);
        for (Entry<String, String> property : properties.entrySet()) {
            String value = property.getValue();
            if (!IpsStringUtils.isBlank(value)) {
                Element propertyElement = doc.createElement(PROPERTY_ELEMENT);
                propertyElement.setAttribute(NAME_ATTRIBUTE, property.getKey());
                propertyElement.setAttribute(VALUE_ATTRIBUTE, value);
                featureConfigurationElement.appendChild(propertyElement);
            }
        }
        return featureConfigurationElement;
    }

    void initFromXml(Element featureConfigurationElement) {
        properties.clear();
        NodeList propertyElements = featureConfigurationElement.getElementsByTagName(PROPERTY_ELEMENT);
        for (int i = 0; i < propertyElements.getLength(); i++) {
            Element propertyElement = (Element)propertyElements.item(i);
            String name = propertyElement.getAttribute(NAME_ATTRIBUTE);
            String value = propertyElement.getAttribute(VALUE_ATTRIBUTE);
            set(name, value);
        }
    }

}
