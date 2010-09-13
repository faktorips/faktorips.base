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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * A kind of Map (so not implementing the {@link Map} interface) that manages custom validations per
 * type. This is a typesafe heterogeneous container as described in 'Effective Java, 2 Edition, Item
 * 29'. As descriped in Effective Java, Java's generics support doesn't allows to implement the
 * needed functionality wihout using unchecked assignments.
 * 
 * @author ortmann
 */
public class CustomValidationsPerType {

    // map with model types (T class<? extends IIpsOpsObjectPartContainer) as keys and sets of
    // ICustomValidations<T> as values. The map contains only the validations defined exactly for
    // the type. The inheritance
    // hierarchy is not resolved.
    // see class Javadoc why suppress warnings is neccessary
    @SuppressWarnings("unchecked")
    private Map<Class<? extends IIpsObjectPartContainer>, Set> backingMap = new HashMap<Class<? extends IIpsObjectPartContainer>, Set>();

    // map with model types (T class<? extends IIpsOpsObjectPartContainer) as keys and sets of
    // ICustomValidations<T> as values. The inheritance hierarhcy is resolved. The set of
    // validations for a given type, contains all
    // validations for the type itself and all super types and interfaces.
    // see class Javadoc why suppress warnings is neccessary
    @SuppressWarnings("unchecked")
    private Map<Class<? extends IIpsObjectPartContainer>, Set> resolvedMap = new HashMap<Class<? extends IIpsObjectPartContainer>, Set>();

    // see class Javadoc why suppress warnings is neccessary
    @SuppressWarnings("unchecked")
    public static CustomValidationsPerType createFromExtensions() {
        CustomValidationsPerType customValidationsPerType = new CustomValidationsPerType();
        ExtensionPoints extensionPoints = new ExtensionPoints(IpsPlugin.PLUGIN_ID);
        List<ICustomValidation> allValidations = extensionPoints.createExecutableExtensions(
                ExtensionPoints.CUSTOM_VALIDATION, "customValidation", "validationClass", ICustomValidation.class); //$NON-NLS-1$ //$NON-NLS-2$
        for (ICustomValidation validation : allValidations) {
            customValidationsPerType.addInternal(validation);
        }
        customValidationsPerType.resolveInheritanceHierarchy();
        return customValidationsPerType;
    }

    // see class Javadoc why suppress warnings is neccessary
    @SuppressWarnings("unchecked")
    private boolean addInternal(ICustomValidation validation) {
        // see class Javadoc
        Set validations = backingMap.get(validation.getExtendedClass());
        if (validations == null) {
            validations = new HashSet(2);
            backingMap.put(validation.getExtendedClass(), validations);
        }
        return validations.add(validation);
    }

    @SuppressWarnings("unchecked")
    private void resolveInheritanceHierarchy() {
        resolvedMap.clear();
        Set<Class<? extends IIpsObjectPartContainer>> types = backingMap.keySet();
        for (Class<? extends IIpsObjectPartContainer> type : types) {
            Set validations = collectAllCustomValidationsInInheritanceHierarchy(type);
            resolvedMap.put(type, validations);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends IIpsObjectPartContainer> Set collectAllCustomValidationsInInheritanceHierarchy(Class<T> type) {
        Set validations = new HashSet();
        collectCustomValidations(type, validations);
        return validations;
    }

    @SuppressWarnings("unchecked")
    private <T extends IIpsObjectPartContainer> void collectCustomValidations(Class<? extends T> type, Set validations) {
        Set<ICustomValidation> typeValidations = backingMap.get(type);
        if (typeValidations != null) {
            for (ICustomValidation validation : typeValidations) {
                validations.add(validation);
            }
        }
        if (type.getSuperclass() != null && IIpsObjectPartContainer.class.isAssignableFrom(type.getSuperclass())) {
            collectCustomValidations((Class<? extends IIpsObjectPartContainer>)type.getSuperclass(), validations);
        }
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> interface1 : interfaces) {
            if (IIpsObjectPartContainer.class.isAssignableFrom(interface1)) {
                collectCustomValidations((Class<? extends IIpsObjectPartContainer>)interface1, validations);
            }
        }
    }

    /**
     * Returns an unmodifiable set of custom validations for the given type.
     * 
     * @param <T> A model class or interface that derives from {@link IIpsObjectPartContainer}
     * @param type A model class or interface that derives from {@link IIpsObjectPartContainer}
     * @return the set of custom validations.
     */
    public <T extends IIpsObjectPartContainer> Set<ICustomValidation<T>> getCustomValidations(Class<T> type) {
        // see class Javadoc
        @SuppressWarnings("unchecked")
        Set<ICustomValidation<T>> validations = resolvedMap.get(type);
        if (validations == null) {
            return new HashSet<ICustomValidation<T>>(0);
        }
        return Collections.unmodifiableSet(validations);
    }

    /**
     * Adds the given custom validation for the given type to this map.
     * 
     * @param validation The validation to add.
     */
    public <T extends IIpsObjectPartContainer> boolean addCustomValidation(ICustomValidation<T> validation) {
        boolean rc = addInternal(validation);
        resolveInheritanceHierarchy();
        return rc;
    }
}
