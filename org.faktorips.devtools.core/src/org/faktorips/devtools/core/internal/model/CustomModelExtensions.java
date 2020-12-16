/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ICustomModelExtensions;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition.RetentionPolicy;
import org.faktorips.devtools.core.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategyFactory;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of {@link ICustomModelExtensions}.
 * 
 * @author Jan Ortmann
 */
public class CustomModelExtensions implements ICustomModelExtensions {

    /** extension properties per IPS object (or part) class object, e.g. IAttribute.class. */
    private final Map<Class<?>, List<IExtensionPropertyDefinition>> typeExtensionPropertiesMap;

    private final CustomValidationsResolver customValidationsResolver;

    private final Map<String, IProductCmptNamingStrategyFactory> productCmptNamingStrategies;

    private final IpsModel ipsModel;

    public CustomModelExtensions(IpsModel ipsModel) {
        ArgumentCheck.notNull(ipsModel);
        this.ipsModel = ipsModel;
        customValidationsResolver = CustomValidationsResolver.createFromExtensions();
        typeExtensionPropertiesMap = new ConcurrentHashMap<Class<?>, List<IExtensionPropertyDefinition>>(8, 0.75f, 1);
        initExtensionPropertiesFromConfiguration();
        productCmptNamingStrategies = new ConcurrentHashMap<String, IProductCmptNamingStrategyFactory>(4, 0.9f, 1);
        initProductCmptNamingStrategies();
    }

    private void initProductCmptNamingStrategies() {
        ExtensionPoints extensionPoints = new ExtensionPoints(IpsPlugin.PLUGIN_ID);
        List<IProductCmptNamingStrategyFactory> strategyFactories = extensionPoints.createExecutableExtensions(
                ExtensionPoints.PRODUCT_COMPONENT_NAMING_STRATEGY, ExtensionPoints.PRODUCT_COMPONENT_NAMING_STRATEGY,
                "factoryClass", IProductCmptNamingStrategyFactory.class); //$NON-NLS-1$
        for (IProductCmptNamingStrategyFactory factory : strategyFactories) {
            productCmptNamingStrategies.put(factory.getExtensionId(), factory);
        }
    }

    @Override
    public Set<IExtensionPropertyDefinition> getExtensionPropertyDefinitions(Class<?> type,
            boolean includeSupertypesAndInterfaces) {
        Set<IExtensionPropertyDefinition> result = new LinkedHashSet<IExtensionPropertyDefinition>();
        getIpsObjectExtensionProperties(type, includeSupertypesAndInterfaces, result);
        return result;
    }

    @Override
    public IExtensionPropertyDefinition getExtensionPropertyDefinition(Class<?> type,
            String propertyId,
            boolean includeSupertypesAndInterfaces) {

        Set<IExtensionPropertyDefinition> props = new HashSet<IExtensionPropertyDefinition>();
        getIpsObjectExtensionProperties(type, includeSupertypesAndInterfaces, props);
        for (Object name2 : props) {
            IExtensionPropertyDefinition prop = (IExtensionPropertyDefinition)name2;
            if (prop.getPropertyId().equals(propertyId)) {
                return prop;
            }
        }
        return null;
    }

    @Override
    public Map<String, IExtensionPropertyDefinition> getExtensionPropertyDefinitions(IIpsObjectPartContainer object) {
        HashMap<String, IExtensionPropertyDefinition> result = new HashMap<String, IExtensionPropertyDefinition>();

        for (Class<?> key : typeExtensionPropertiesMap.keySet()) {
            if (key.isAssignableFrom(object.getClass())) {
                List<IExtensionPropertyDefinition> propertiesPerClass = typeExtensionPropertiesMap.get(key);
                for (IExtensionPropertyDefinition extensionPropertyDefinition : propertiesPerClass) {
                    if (extensionPropertyDefinition.isApplicableFor(object)) {
                        result.put(extensionPropertyDefinition.getPropertyId(), extensionPropertyDefinition);
                    }
                }
            }
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * Adds the extension property. For testing purposes. During normal execution the available
     * extension properties are discovered by extension point lookup.
     */
    public void addIpsObjectExtensionProperty(IExtensionPropertyDefinition property) {
        List<IExtensionPropertyDefinition> props = typeExtensionPropertiesMap
                .computeIfAbsent(property.getExtendedType(), $ -> new ArrayList<IExtensionPropertyDefinition>());
        props.add(property);
        Collections.sort(props);
    }

    private void getIpsObjectExtensionProperties(Class<?> type,
            boolean includeSupertypesAndInterfaces,
            Set<IExtensionPropertyDefinition> result) {

        List<IExtensionPropertyDefinition> props = typeExtensionPropertiesMap.get(type);
        if (props != null) {
            result.addAll(props);
        }
        if (!includeSupertypesAndInterfaces) {
            return;
        }
        if (type.getSuperclass() != null) {
            getIpsObjectExtensionProperties(type.getSuperclass(), true, result);
        }
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> interface1 : interfaces) {
            getIpsObjectExtensionProperties(interface1, true, result);
        }
    }

    private void initExtensionPropertiesFromConfiguration() {
        IExtensionRegistry registry = IpsPlugin.getDefault().getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(IpsPlugin.PLUGIN_ID, "objectExtensionProperty"); //$NON-NLS-1$
        IExtension[] extensions = point.getExtensions();

        for (IExtension extension : extensions) {
            IExtensionPropertyDefinition property = createExtensionProperty(extension);
            if (property != null) {
                List<IExtensionPropertyDefinition> props = typeExtensionPropertiesMap.computeIfAbsent(
                        property.getExtendedType(), $ -> new ArrayList<IExtensionPropertyDefinition>());
                props.add(property);
            }
        }
        sortExtensionProperties();
    }

    private void sortExtensionProperties() {
        Collection<List<IExtensionPropertyDefinition>> typeLists = typeExtensionPropertiesMap.values();
        for (List<IExtensionPropertyDefinition> propList : typeLists) {
            Collections.sort(propList);
        }
    }

    protected ExtensionPropertyDefinition createExtensionProperty(IExtension extension) {
        IConfigurationElement[] configElements = extension.getConfigurationElements();
        if (configElements.length != 1 || !"property".equalsIgnoreCase(configElements[0].getName())) { //$NON-NLS-1$
            IpsPlugin.log(new IpsStatus("Illegal definition of external property " //$NON-NLS-1$
                    + extension.getUniqueIdentifier()));
            return null;
        }
        IConfigurationElement element = configElements[0];
        Object propertyInstance = null;
        try {
            propertyInstance = element.createExecutableExtension("class"); //$NON-NLS-1$
        } catch (CoreException e) {
            IpsPlugin.log(new IpsStatus("Unable to create extension property " //$NON-NLS-1$
                    + extension.getUniqueIdentifier() + ". Reason: Can't instantiate " //$NON-NLS-1$
                    + element.getAttribute("class"), e)); //$NON-NLS-1$
            return null;
        }
        if (!(propertyInstance instanceof ExtensionPropertyDefinition)) {
            IpsPlugin.log(new IpsStatus("Unable to create extension property " //$NON-NLS-1$
                    + extension.getUniqueIdentifier() + element.getAttribute("class") + " does not derived from " //$NON-NLS-1$ //$NON-NLS-2$
                    + ExtensionPropertyDefinition.class));
            return null;
        }
        ExtensionPropertyDefinition extProperty = (ExtensionPropertyDefinition)propertyInstance;
        extProperty.setPropertyId(extension.getUniqueIdentifier());
        extProperty.setName(extension.getLabel());
        extProperty.setDefaultValue(element.getAttribute("defaultValue")); //$NON-NLS-1$
        extProperty.setPosition(element.getAttribute("position")); //$NON-NLS-1$
        String retentionString = element.getAttribute("retention"); //$NON-NLS-1$
        if (StringUtils.isEmpty(retentionString)) {
            extProperty.setRetention(RetentionPolicy.RUNTIME);
        } else {
            extProperty.setRetention(RetentionPolicy.valueOf(retentionString));
        }
        if (StringUtils.isNotEmpty(element.getAttribute("order"))) { //$NON-NLS-1$
            extProperty.setSortOrder(Integer.parseInt(element.getAttribute("order"))); //$NON-NLS-1$
        }
        String extType = element.getAttribute("extendedType"); //$NON-NLS-1$
        try {
            extProperty.setExtendedType(extProperty.getClass().getClassLoader().loadClass(extType));
        } catch (ClassNotFoundException e) {
            IpsPlugin.log(new IpsStatus("Extended type " + extType //$NON-NLS-1$
                    + " not found for extension property " //$NON-NLS-1$
                    + extProperty.getPropertyId(), e));
            return null;
        }
        return extProperty;
    }

    @Override
    public <T extends IIpsObjectPartContainer> Set<ICustomValidation<?>> getCustomValidations(Class<T> type) {
        return customValidationsResolver.getCustomValidations(type);
    }

    /**
     * Adds the given custom validation to the model validations.
     * 
     * @param validation The validation to add.
     */
    public void addCustomValidation(ICustomValidation<? extends IIpsObjectPartContainer> validation) {
        ipsModel.clearValidationCache();
        customValidationsResolver.addCustomValidation(validation);
    }

    @Override
    public IProductCmptNamingStrategyFactory getProductCmptNamingStrategyFactory(String extensionId) {
        ArgumentCheck.notNull(extensionId);
        return productCmptNamingStrategies.get(extensionId);
    }

}
