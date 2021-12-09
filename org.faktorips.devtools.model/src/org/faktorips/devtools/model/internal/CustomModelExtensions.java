/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.faktorips.devtools.model.ICustomModelExtensions;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.abstraction.Abstractions;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategyFactory;
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

    private final IIpsModel ipsModel;

    public CustomModelExtensions(IIpsModel ipsModel) {
        ArgumentCheck.notNull(ipsModel);
        this.ipsModel = ipsModel;
        customValidationsResolver = CustomValidationsResolver.createFromExtensions();
        typeExtensionPropertiesMap = new ConcurrentHashMap<>(8, 0.75f, 1);
        initExtensionPropertiesFromConfiguration();
        productCmptNamingStrategies = new ConcurrentHashMap<>(4, 0.9f, 1);
        initProductCmptNamingStrategies();
    }

    private void initProductCmptNamingStrategies() {
        if (Abstractions.isEclipseRunning()) {
            ExtensionPoints extensionPoints = new ExtensionPoints(IpsModelActivator.PLUGIN_ID);
            List<IProductCmptNamingStrategyFactory> strategyFactories = extensionPoints.createExecutableExtensions(
                    ExtensionPoints.PRODUCT_COMPONENT_NAMING_STRATEGY,
                    ExtensionPoints.PRODUCT_COMPONENT_NAMING_STRATEGY,
                    "factoryClass", IProductCmptNamingStrategyFactory.class); //$NON-NLS-1$
            for (IProductCmptNamingStrategyFactory factory : strategyFactories) {
                productCmptNamingStrategies.put(factory.getExtensionId(), factory);
            }
        }
    }

    @Override
    public Set<IExtensionPropertyDefinition> getExtensionPropertyDefinitions(Class<?> type,
            boolean includeSupertypesAndInterfaces) {
        Set<IExtensionPropertyDefinition> result = new LinkedHashSet<>();
        getIpsObjectExtensionProperties(type, includeSupertypesAndInterfaces, result);
        return result;
    }

    @Override
    public IExtensionPropertyDefinition getExtensionPropertyDefinition(Class<?> type,
            String propertyId,
            boolean includeSupertypesAndInterfaces) {

        Set<IExtensionPropertyDefinition> props = new HashSet<>();
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
        HashMap<String, IExtensionPropertyDefinition> result = new HashMap<>();

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
        List<IExtensionPropertyDefinition> props = typeExtensionPropertiesMap.get(property.getExtendedType());
        if (props == null) {
            props = new ArrayList<>();
            typeExtensionPropertiesMap.put(property.getExtendedType(), props);
        }
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
        typeExtensionPropertiesMap.putAll(IIpsModelExtensions.get().getExtensionPropertyDefinitions());
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
