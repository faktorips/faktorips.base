/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition2;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExtensionPropertyHandler {

    /** Map containing extension property IDs as keys and their values. */
    private final ExtensionPropertyMap extPropertyValues = new ExtensionPropertyMap();

    private final IpsObjectPartContainer ipsObjectPartContainer;

    public ExtensionPropertyHandler(IpsObjectPartContainer ipsObjectPartContainer) {
        this.ipsObjectPartContainer = ipsObjectPartContainer;
    }

    ExtensionPropertyMap getExtPropertyValues() {
        return extPropertyValues;
    }

    public Object getExtPropertyValue(String propertyId) {
        checkExtProperty(propertyId);
        initMissingExtProperties();
        return getExtPropertyValues().get(propertyId);
    }

    public boolean isExtPropertyDefinitionAvailable(String propertyId) {
        return ipsObjectPartContainer.getExtensionPropertyDefinition(propertyId) != null;
    }

    public void setExtPropertyValue(String propertyId, Object value) {
        checkExtProperty(propertyId);
        IExtensionPropertyDefinition property = ipsObjectPartContainer.getExtensionPropertyDefinition(propertyId);
        if (!property.beforeSetValue(ipsObjectPartContainer, value)) {
            // veto to set the new value by the property definition
            return;
        }
        initMissingExtProperties();
        if (!ObjectUtils.equals(value, getExtPropertyValue(propertyId))) {
            getExtPropertyValues().put(propertyId, value);
            ipsObjectPartContainer.objectHasChanged();
        }
        property.afterSetValue(ipsObjectPartContainer, value);
    }

    /* private */void checkExtProperty(String propertyId) {
        if (!isExtPropertyDefinitionAvailable(propertyId)) {
            throw new IllegalArgumentException("Extension property " + propertyId + " is not defined for type " //$NON-NLS-1$ //$NON-NLS-2$
                    + getClass().getName());
        }
    }

    /* private */void initMissingExtProperties() {
        Collection<IExtensionPropertyDefinition> properties = ipsObjectPartContainer.getExtensionPropertyDefinitions();
        for (IExtensionPropertyDefinition property : properties) {
            Object defaultValue = getExtensionPropertyDefaultValue(property);
            getExtPropertyValues().putIfAbsent(property.getPropertyId(), defaultValue);
        }
    }

    private Object getExtensionPropertyDefaultValue(IExtensionPropertyDefinition property) {
        if (property instanceof IExtensionPropertyDefinition2) {
            return ((IExtensionPropertyDefinition2)property).getDefaultValue(ipsObjectPartContainer);
        } else {
            return property.getDefaultValue();
        }
    }

    public void extPropertiesToXml(Element element) {
        Collection<IExtensionPropertyDefinition> propertyDefinitions = ipsObjectPartContainer
                .getExtensionPropertyDefinitions();
        if (propertyDefinitions.isEmpty()) {
            return;
        }
        initMissingExtProperties();
        Document doc = element.getOwnerDocument();
        Element extPropertiesEl = doc.createElement(IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        element.appendChild(extPropertiesEl);
        for (IExtensionPropertyDefinition propertyDefinition : propertyDefinitions) {
            extPropertyToXml(extPropertiesEl, propertyDefinition);
        }
        extPropertiesWithMissingDefinitionsToXml(extPropertiesEl, propertyDefinitions);
    }

    private void extPropertyToXml(Element extPropertiesEl, IExtensionPropertyDefinition propertyDefinition) {
        Element valueEl = extPropertiesEl.getOwnerDocument().createElement(IpsObjectPartContainer.XML_VALUE_ELEMENT);
        extPropertiesEl.appendChild(valueEl);
        String propertyId = propertyDefinition.getPropertyId();
        valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, propertyId);
        Object value = getExtPropertyValues().get(propertyId);
        valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL, value == null ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
        if (value != null) {
            propertyDefinition.valueToXml(valueEl, value);
        }
    }

    /**
     * Writes all extension property values without {@link ExtensionPropertyDefinition} to XML. The
     * String value is written to XML without being modified.
     * 
     * @param extPropertiesEl the element the ext-property values should be added to
     * @param propertyDefinitions the list of available extension property definitions.
     */
    private void extPropertiesWithMissingDefinitionsToXml(Element extPropertiesEl,
            Collection<IExtensionPropertyDefinition> propertyDefinitions) {
        Map<String, Object> extpropMap = getExtPropertyValueMapForMissingDefinitions(propertyDefinitions);
        for (String propertyId : extpropMap.keySet()) {
            ExtensionPropertyDefinition propertyDefinition = new StringExtensionPropertyDefinition();
            propertyDefinition.setPropertyId(propertyId);
            extPropertyToXml(extPropertiesEl, propertyDefinition);
        }
    }

    private Map<String, Object> getExtPropertyValueMapForMissingDefinitions(Collection<IExtensionPropertyDefinition> propertyDefinitions) {
        Map<String, Object> extpropMap = new HashMap<String, Object>(getExtPropertyValues().internalMap);
        for (IExtensionPropertyDefinition propertyDefinition : propertyDefinitions) {
            extpropMap.remove(propertyDefinition.getPropertyId());
        }
        return extpropMap;
    }

    /**
     * Add the given extension property value identified by the given property id. If the extension
     * property not exists as definitions then the property will be ignored.
     * <p>
     * Note: Better do not use this method. The extension property should be initialized by
     * {@link #initExtPropertyFromXml(Element)}.
     * 
     * @param propertyId id of the extension property
     * @param extPropertyValue extension property value
     */
    protected void addExtensionPropertyValue(String propertyId, String extPropertyValue) {
        initMissingExtProperties();
        Object value = null;
        if (extPropertyValue != null) {
            IExtensionPropertyDefinition property = ipsObjectPartContainer.getExtensionPropertyDefinition(propertyId);
            if (property == null) {
                return;
            }
            value = property.getValueFromString(extPropertyValue);
        }
        getExtPropertyValues().put(propertyId, value);
    }

    /**
     * The method is called by the initFromXml() method to retrieve the values of the extension
     * properties.
     * 
     * @param containerEl The &lt;ExtensionProperties&gt; element.
     */
    public void initExtPropertiesFromXml(Element containerEl) {
        getExtPropertyValues().clear();
        initMissingExtProperties();
        Element extPropertiesEl = XmlUtil.getFirstElement(containerEl,
                IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        if (extPropertiesEl == null) {
            return;
        }
        NodeList nl = extPropertiesEl.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && node.getNodeName().equals(IpsObjectPartContainer.XML_VALUE_ELEMENT)) {
                initExtPropertyFromXml((Element)node);
            }
        }
    }

    /**
     * The method is called by the initFromXml() method to retrieve the values of the extension
     * properties.
     */
    public void initExtPropertyFromXml(Element valueElement) {
        String propertyId = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID);
        Object value = null;
        String isNull = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        if (StringUtils.isEmpty(isNull) || !Boolean.valueOf(isNull).booleanValue()) {
            IExtensionPropertyDefinition property = ipsObjectPartContainer.getExtensionPropertyDefinition(propertyId);
            if (property == null) {
                /*
                 * Load property values even if ExtPropDefinition is missing. In that case load
                 * values as strings without modifying or interpreting them. See jira FIPS-772.
                 */
                IpsPlugin.log(new IpsStatus(IStatus.WARNING, "Extension property " + propertyId + " for " + this //$NON-NLS-1$ //$NON-NLS-2$
                        + " is unknown")); //$NON-NLS-1$
                property = new StringExtensionPropertyDefinition();
            }
            value = property.getValueFromXml(valueElement);
        }
        getExtPropertyValues().put(propertyId, value);
    }

    /**
     * Validates the extension property values.
     * 
     * @param ml The message list to which messages generated during the validation are added.
     * 
     * @throws CoreException if an error occurs while validation the extension properties.
     */
    public void validateExtensionProperties(MessageList ml) throws CoreException {
        Collection<IExtensionPropertyDefinition> properties = ipsObjectPartContainer.getExtensionPropertyDefinitions();
        for (IExtensionPropertyDefinition propertie : properties) {
            Object value = getExtPropertyValue(propertie.getPropertyId());
            MessageList newList = propertie.validate(ipsObjectPartContainer, value);
            if (newList != null) {
                ml.add(newList);
            }
        }
    }

    /**
     * This map wraps a {@link ConcurrentHashMap} and additionally allows to add <code>null</code>
     * as a value. This is achieved by adding a null-object instead of <code>null</code>. The getter
     * checks for the null-object and does return real <code>null</code> instead.
     * 
     */
    static class ExtensionPropertyMap {

        private static final Object EXT_PROPERTY_NULL_OBJECT = new Object();

        private final ConcurrentHashMap<String, Object> internalMap = new ConcurrentHashMap<String, Object>();

        public Object get(Object key) {
            Object object = internalMap.get(key);
            if (object == EXT_PROPERTY_NULL_OBJECT) {
                return null;
            } else {
                return object;
            }
        }

        public void clear() {
            internalMap.clear();
        }

        public Object put(String key, Object value) {
            if (value == null) {
                return internalMap.put(key, EXT_PROPERTY_NULL_OBJECT);
            } else {
                return internalMap.put(key, value);
            }
        }

        public Object putIfAbsent(String key, Object value) {
            if (value == null) {
                return internalMap.putIfAbsent(key, EXT_PROPERTY_NULL_OBJECT);
            } else {
                return internalMap.putIfAbsent(key, value);
            }
        }

    }

}