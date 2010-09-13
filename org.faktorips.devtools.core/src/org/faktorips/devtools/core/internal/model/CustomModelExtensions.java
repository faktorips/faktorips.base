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

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ICustomModelExtensions;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of {@link ICustomModelExtensions}.
 * 
 * @author Jan Ortmann
 */
public class CustomModelExtensions implements ICustomModelExtensions {

    /** extension properties per ips object (or part) type, e.g. IAttribute. */
    private Map<Class<?>, List<IExtensionPropertyDefinition>> typeExtensionPropertiesMap = null;

    private CustomValidationsPerType customValidationsPerType = null;

    private IpsModel ipsModel;

    public CustomModelExtensions(IpsModel ipsModel) {
        ArgumentCheck.notNull(ipsModel);
        this.ipsModel = ipsModel;
        customValidationsPerType = CustomValidationsPerType.createFromExtensions();
    }

    @Override
    public Set<IExtensionPropertyDefinition> getExtensionPropertyDefinitions(Class<?> type,
            boolean includeSupertypesAndInterfaces) {
        if (typeExtensionPropertiesMap == null) {
            initExtensionPropertiesFromConfiguration();
        }
        SortedSet<IExtensionPropertyDefinition> result = new TreeSet<IExtensionPropertyDefinition>(
                new Comparator<IExtensionPropertyDefinition>() {

                    @Override
                    public int compare(IExtensionPropertyDefinition def1, IExtensionPropertyDefinition def2) {
                        return def1.getPropertyId().compareTo(def2.getPropertyId());
                    }

                });

        getIpsObjectExtensionProperties(type, includeSupertypesAndInterfaces, result);
        return result;
    }

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

    /**
     * Adds the extension property. For testing purposes. During normal execution the available
     * extension properties are discovered by extension point lookup.
     */
    public void addIpsObjectExtensionProperty(IExtensionPropertyDefinition property) {
        if (typeExtensionPropertiesMap == null) {
            typeExtensionPropertiesMap = new HashMap<Class<?>, List<IExtensionPropertyDefinition>>();
        }
        List<IExtensionPropertyDefinition> props = typeExtensionPropertiesMap.get(property.getExtendedType());
        if (props == null) {
            props = new ArrayList<IExtensionPropertyDefinition>();
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
        typeExtensionPropertiesMap = new HashMap<Class<?>, List<IExtensionPropertyDefinition>>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(IpsPlugin.PLUGIN_ID, "objectExtensionProperty"); //$NON-NLS-1$
        IExtension[] extensions = point.getExtensions();

        for (IExtension extension : extensions) {
            IExtensionPropertyDefinition property = createExtensionProperty(extension);
            if (property != null) {
                List<IExtensionPropertyDefinition> props = typeExtensionPropertiesMap.get(property.getExtendedType());
                if (props == null) {
                    props = new ArrayList<IExtensionPropertyDefinition>();
                    typeExtensionPropertiesMap.put(property.getExtendedType(), props);
                }
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

    private IExtensionPropertyDefinition createExtensionProperty(IExtension extension) {
        IConfigurationElement[] configElements = extension.getConfigurationElements();
        if (configElements.length != 1 || !configElements[0].getName().equalsIgnoreCase("property")) { //$NON-NLS-1$
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
    public <T extends IIpsObjectPartContainer> Set<ICustomValidation<T>> getCustomValidations(Class<T> type) {
        return customValidationsPerType.getCustomValidations(type);
    }

    /**
     * Adds the given custom validation to the model validations.
     * 
     * @param validation The validation to add.
     */
    public void addCustomValidation(ICustomValidation<? extends IIpsObjectPartContainer> validation) {
        ipsModel.clearValidationCache();
        customValidationsPerType.addCustomValidation(validation);
    }

}
