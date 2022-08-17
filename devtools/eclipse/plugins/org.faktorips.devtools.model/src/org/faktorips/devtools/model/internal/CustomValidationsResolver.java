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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.model.internal.type.Attribute;
import org.faktorips.devtools.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.type.IAttribute;

/**
 * Manages custom all validations for each type of {@link IIpsObjectPartContainer}.
 * <p>
 * Custom validations returned by this map may be responsible for the requested class but also a
 * super class or implemented interface of the requested class. For example, when searching for all
 * custom validations for {@link PolicyCmptTypeAttribute PolicyCmptTypeAttribute.class}, this map
 * returns the custom validations for {@link PolicyCmptTypeAttribute}, but also custom validations
 * responsible for {@link IPolicyCmptTypeAttribute}, {@link Attribute} and {@link IAttribute}.
 * <p>
 * Results are cached, also for classes for which no custom validations have been registered. Adding
 * new custom validations clears the cache, however.
 */
public class CustomValidationsResolver {

    /**
     * Maps from classes to custom validation instances directly, disregarding class hierarchy.
     */
    private CustomValidationsMap backingMap = new CustomValidationsMap();

    /**
     * Cache for custom validations responsible for a class or its super classes and super
     * interfaces. This map stores all resolved custom validations on calls to
     * {@link #getCustomValidations(Class)}. It is cleared on each call to
     * {@link #addCustomValidation(ICustomValidation)}.
     */
    private CustomValidationsMap resolvedMap = new CustomValidationsMap();

    private Set<Class<? extends IIpsObjectPartContainer>> resolvedTypes = new LinkedHashSet<>();

    /**
     * Returns an unmodifiable set of custom validations for the given type.
     * 
     * @param <T> A model class or interface that derives from {@link IIpsObjectPartContainer}
     * @param type A model class or interface that derives from {@link IIpsObjectPartContainer}
     * @return the set of custom validations.
     */
    public <T extends IIpsObjectPartContainer> Set<ICustomValidation<?>> getCustomValidations(Class<T> type) {
        if (!isResolved(type)) {
            resolveCustomValidationsForClassHierarchy(type);
        }
        return resolvedMap.get(type);
    }

    private <T> Boolean isResolved(Class<T> type) {
        return resolvedTypes.contains(type);
    }

    /**
     * Protected for testing purposes.
     * 
     */
    protected <T extends IIpsObjectPartContainer> void resolveCustomValidationsForClassHierarchy(Class<T> type) {
        List<ICustomValidation<?>> validations = resolveAllCustomValidationsFor(type);
        resolvedMap.putAll(type, validations);
        markAsResolved(type);
    }

    private <T extends IIpsObjectPartContainer> List<ICustomValidation<?>> resolveAllCustomValidationsFor(
            Class<T> type) {
        List<ICustomValidation<?>> validations = new ArrayList<>();
        collectCustomValidations(type, validations);
        return validations;
    }

    private <T extends IIpsObjectPartContainer> void collectCustomValidations(Class<T> type,
            List<ICustomValidation<?>> validations) {
        collectValidationsForSameType(type, validations);
        collectValidationsForSuperclass(type, validations);
        collectValidationsForImplementedInterfaces(type, validations);
    }

    private <T extends IIpsObjectPartContainer> void collectValidationsForSameType(Class<T> type,
            List<ICustomValidation<?>> validations) {
        Set<ICustomValidation<?>> typeValidations = backingMap.get(type);
        if (typeValidations != null) {
            validations.addAll(typeValidations);
        }
    }

    private <T extends IIpsObjectPartContainer> void collectValidationsForImplementedInterfaces(Class<T> type,
            List<ICustomValidation<?>> validations) {
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> interface1 : interfaces) {
            if (IIpsObjectPartContainer.class.isAssignableFrom(interface1)) {
                @SuppressWarnings("unchecked")
                Class<? extends IIpsObjectPartContainer> implementedInterface = (Class<? extends T>)interface1;
                collectCustomValidations(implementedInterface, validations);
            }
        }
    }

    private <T extends IIpsObjectPartContainer> void collectValidationsForSuperclass(Class<T> type,
            List<ICustomValidation<?>> validations) {
        if (type.getSuperclass() != null && IIpsObjectPartContainer.class.isAssignableFrom(type.getSuperclass())) {
            @SuppressWarnings("unchecked")
            Class<? extends T> superclass = (Class<? extends T>)type.getSuperclass();
            collectCustomValidations(superclass, validations);
        }
    }

    private <T extends IIpsObjectPartContainer> void markAsResolved(Class<T> type) {
        resolvedTypes.add(type);
    }

    /**
     * Adds the given custom validation for the given type to this map.
     * 
     * @param validation The validation to add.
     */
    public <T extends IIpsObjectPartContainer> void addCustomValidation(ICustomValidation<T> validation) {
        addInternal(validation);
        clearCache();
    }

    private <T extends IIpsObjectPartContainer> void addInternal(ICustomValidation<T> validation) {
        backingMap.put(validation);
    }

    private void clearCache() {
        resolvedMap.clear();
        resolvedTypes.clear();
    }

    /**
     * Loads all custom validations registered via extension point (XML).
     * 
     * @return an instance of this map containing all registered custom validation instances.
     */
    @SuppressWarnings({ "rawtypes" })
    public static <T extends IIpsObjectPartContainer> CustomValidationsResolver createFromExtensions() {
        List<ICustomValidation<?>> typeSafe = new ArrayList<>();
        if (Abstractions.isEclipseRunning()) {
            ExtensionPoints extensionPoints = new ExtensionPoints(IpsModelActivator.PLUGIN_ID);
            List<ICustomValidation> allValidations = extensionPoints.createExecutableExtensions(
                    ExtensionPoints.CUSTOM_VALIDATION, "customValidation", "validationClass", ICustomValidation.class); //$NON-NLS-1$ //$NON-NLS-2$
            for (ICustomValidation customValidation : allValidations) {
                typeSafe.add(customValidation);
            }
        }
        return createFromList(typeSafe);
    }

    /**
     * @return an instance of this map containing the given custom validation instances.
     */
    public static <T extends IIpsObjectPartContainer> CustomValidationsResolver createFromList(
            List<ICustomValidation<?>> allValidations) {
        CustomValidationsResolver customValidationsPerType = new CustomValidationsResolver();
        for (ICustomValidation<?> validation : allValidations) {
            customValidationsPerType.addCustomValidation(validation);
        }
        return customValidationsPerType;

    }

}
