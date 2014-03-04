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
import org.faktorips.devtools.core.internal.model.ipsobject.extensionpropertyrepresentation.InvalidExtensionPropertyRepresentation;
import org.faktorips.devtools.core.internal.model.ipsobject.extensionpropertyrepresentation.InvalidExtensionPropertyStringRepresentation;
import org.faktorips.devtools.core.internal.model.ipsobject.extensionpropertyrepresentation.InvalidExtensionPropertyXMLRepresentation;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition2;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is responsible for all purposes of an {@link IIpsObjectPartContainer} that relates to
 * extension properties.
 * <p>
 * Extension properties are additional properties that could be registered via extension point in
 * other plugIns. They are used to store additional information at specific sections of the model or
 * of the product component.
 * <p>
 * Every {@link IpsObjectPartContainer} has to instantiate its own {@link ExtensionPropertyHandler}.
 * 
 * @author dirmeier
 */
public class ExtensionPropertyHandler {

    /** Map containing extension property IDs as keys and their values. */
    private final ExtensionPropertyMap extPropertyValues = new ExtensionPropertyMap();

    private final IpsObjectPartContainer ipsObjectPartContainer;

    /**
     * Map containing invalid extension property IDs as keys and their values. Only used to not
     * loose these information. They are initialized while reading the xml and stored when writing -
     * nothing else.
     */
    private Map<String, InvalidExtensionPropertyRepresentation> invalidPropertiesMap = new HashMap<String, InvalidExtensionPropertyRepresentation>();

    /**
     * Create a new {@link ExtensionPropertyHandler} for the given {@link IIpsObjectPartContainer}
     * 
     */
    public ExtensionPropertyHandler(IpsObjectPartContainer ipsObjectPartContainer) {
        this.ipsObjectPartContainer = ipsObjectPartContainer;
    }

    /**
     * This gives access to the internal {@link ExtensionPropertyMap}. Normally this is not needed.
     * It should only be used for testing the content.
     * 
     */
    ExtensionPropertyMap getExtPropertyValues() {
        return extPropertyValues;
    }

    /**
     * Get the value for the extension property specified by the ID. If no value is set so far, the
     * default value is returned.
     * 
     * @param propertyId The id of the extension property for which you want to get the value
     * @return The value object depending on the extension property definition
     * @throws IllegalArgumentException if the given property id specifies no valid extension
     *             property definition
     */
    public Object getExtPropertyValue(String propertyId) {
        checkExtProperty(propertyId);
        initMissingExtProperties();
        return getExtPropertyValues().get(propertyId);
    }

    /**
     * Checks whether the extension property with the given ID is defined.
     */
    public boolean isExtPropertyDefinitionAvailable(String propertyId) {
        return ipsObjectPartContainer.getExtensionPropertyDefinition(propertyId) != null;
    }

    /**
     * Set a new value for the extension property specified by the propertyId. The kind of object
     * depends on the extension property definition.
     * <p>
     * Before setting the value
     * {@link IExtensionPropertyDefinition#beforeSetValue(IIpsObjectPartContainer, Object)} is
     * called. If this method returns false the method does nothing more and returns.
     * <p>
     * The value is only set if it is not already stored, using the objects equal method to compare
     * both objects. In case of a new value a change event is triggered on the
     * {@link IpsObjectPartContainer}.
     * <p>
     * After setting the new value, even if it was not a new one, the method
     * {@link IExtensionPropertyDefinition#afterSetValue(IIpsObjectPartContainer, Object)} is
     * called.
     * 
     * @param propertyId The id of the extension property for which the value should be set
     * @param value The value that should be set for the extension property
     * @throws IllegalArgumentException if the given property id specifies no valid extension
     *             property definition
     */
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

    /**
     * Stores all the extension property values to the given {@link Element}. Extension property
     * values without a valid {@link IExtensionPropertyDefinition} are stored as they are read.
     * 
     */
    public void toXml(Element element) {
        Collection<IExtensionPropertyDefinition> propertyDefinitions = ipsObjectPartContainer
                .getExtensionPropertyDefinitions();
        if (propertyDefinitions.isEmpty() && invalidPropertiesMap.isEmpty()) {
            return;
        }
        initMissingExtProperties();
        Document doc = element.getOwnerDocument();
        Element extPropertiesEl = doc.createElement(IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        element.appendChild(extPropertiesEl);
        for (IExtensionPropertyDefinition propertyDefinition : propertyDefinitions) {
            propertyToXml(extPropertiesEl, propertyDefinition);
        }
        invalidPropertiestoXML(extPropertiesEl);
        extPropertiesWithMissingDefinitionsToXml(extPropertiesEl, propertyDefinitions);
    }

    private void invalidPropertiestoXML(Element extPropertiesEl) {
        for (InvalidExtensionPropertyRepresentation value : invalidPropertiesMap.values()) {
            value.saveElementInXML(extPropertiesEl);
        }
    }

    private void propertyToXml(Element extPropertiesEl, IExtensionPropertyDefinition propertyDefinition) {
        String propertyId = propertyDefinition.getPropertyId();
        Object value = getExtPropertyValues().get(propertyId);
        propertyToXml(propertyId, propertyDefinition, value, extPropertiesEl);
    }

    public static void propertyToXml(String propertyId,
            IExtensionPropertyDefinition propertyDefinition,
            Object value,
            Element extPropertiesEl) {
        Element valueEl = extPropertiesEl.getOwnerDocument().createElement(IpsObjectPartContainer.XML_VALUE_ELEMENT);
        extPropertiesEl.appendChild(valueEl);
        valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, propertyId);
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
        ExtensionPropertyMap extpropMap = getExtPropertyValueMapForMissingDefinitions(propertyDefinitions);
        for (String propertyId : extpropMap.internalMap.keySet()) {
            ExtensionPropertyDefinition propertyDefinition = new StringExtensionPropertyDefinition();
            propertyDefinition.setPropertyId(propertyId);
            propertyToXml(extPropertiesEl, propertyDefinition);
        }
    }

    private ExtensionPropertyMap getExtPropertyValueMapForMissingDefinitions(Collection<IExtensionPropertyDefinition> propertyDefinitions) {
        ExtensionPropertyMap invalidExtProps = new ExtensionPropertyMap(extPropertyValues);
        for (IExtensionPropertyDefinition propertyDefinition : propertyDefinitions) {
            invalidExtProps.remove(propertyDefinition.getPropertyId());
        }
        return invalidExtProps;
    }

    /**
     * Add the given extension property value identified by the given property id. If the extension
     * property doesn't exists as definitions then the property will be ignored.
     * <p>
     * Note: Better do not use this method. The extension property should be initialized by
     * {@link #initPropertyFromXml(Element)}.
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
                InvalidExtensionPropertyStringRepresentation extensionProperty = new InvalidExtensionPropertyStringRepresentation(
                        propertyId, extPropertyValue);
                invalidPropertiesMap.put(propertyId, extensionProperty);
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
    public void initFromXml(Element containerEl) {
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
                initPropertyFromXml((Element)node);
            }
        }
    }

    /**
     * The method is called by the initFromXml() method to retrieve the values of the extension
     * properties.
     */
    private void initPropertyFromXml(Element valueElement) {
        String propertyId = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID);
        Object value = null;
        String isNull = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        IExtensionPropertyDefinition property = ipsObjectPartContainer.getExtensionPropertyDefinition(propertyId);
        if (StringUtils.isEmpty(isNull) || !Boolean.valueOf(isNull).booleanValue()) {
            if (property == null) {
                /*
                 * Load property values even if ExtPropDefinition is missing. In that case load
                 * values as strings without modifying or interpreting them. See jira FIPS-772.
                 */
                IpsPlugin.log(new IpsStatus(IStatus.WARNING, "Extension property " + propertyId + " for " + this //$NON-NLS-1$ //$NON-NLS-2$
                        + " is unknown")); //$NON-NLS-1$
                InvalidExtensionPropertyXMLRepresentation invalidProperty = new InvalidExtensionPropertyXMLRepresentation(
                        valueElement);
                invalidPropertiesMap.put(propertyId, invalidProperty);
                return;
            }
            value = property.getValueFromXml(valueElement);
        }
        getExtPropertyValues().put(propertyId, value);
        getExtPropertyValues().put(propertyId, value);
    }

    /**
     * Validates the extension property values.
     * 
     * @throws CoreException if an error occurs while validating the extension properties.
     */
    public MessageList validate() throws CoreException {
        MessageList ml = new MessageList();
        Collection<IExtensionPropertyDefinition> properties = ipsObjectPartContainer.getExtensionPropertyDefinitions();
        for (IExtensionPropertyDefinition propertie : properties) {
            Object value = getExtPropertyValue(propertie.getPropertyId());
            MessageList newList = propertie.validate(ipsObjectPartContainer, value);
            if (newList != null) {
                ml.add(newList);
            }
        }
        return ml;
    }

    /**
     * This map wraps a {@link ConcurrentHashMap} and additionally allows to add <code>null</code>
     * as a value. This is achieved by adding a null-object instead of <code>null</code>. The getter
     * checks for the null-object and does return a real <code>null</code> instead.
     * 
     */
    static class ExtensionPropertyMap {

        private static final Object EXT_PROPERTY_NULL_OBJECT = new Object();

        private final ConcurrentHashMap<String, Object> internalMap;

        public ExtensionPropertyMap() {
            internalMap = new ConcurrentHashMap<String, Object>();
        }

        /**
         * Creates a new {@link ExtensionPropertyMap} by copying the values of the given
         * {@link ExtensionPropertyMap}
         */
        public ExtensionPropertyMap(ExtensionPropertyMap extensionPropertyMap) {
            internalMap = new ConcurrentHashMap<String, Object>(extensionPropertyMap.internalMap);
        }

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

        public void remove(String propertyId) {
            internalMap.remove(propertyId);
        }

    }

}