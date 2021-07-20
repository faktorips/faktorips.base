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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.model.internal.type.Attribute;
import org.faktorips.devtools.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;

/**
 * Maps an {@link IIpsObjectPartContainer} subclass (key) to a set of {@link ICustomValidation}
 * instances of arbitrary type (value). Custom validations returned by this map may be responsible
 * for the requested class but also a super class or implemented interface of the requested class.
 * For example, when searching for all custom validations for {@link PolicyCmptTypeAttribute
 * PolicyCmptTypeAttribute.class}, this map returns the custom validations for
 * {@link PolicyCmptTypeAttribute}, but also custom validations responsible for
 * {@link IPolicyCmptTypeAttribute}, {@link Attribute} and {@link IAttribute}.
 * 
 * Type safety is ensured at runtime. Each time a custom validation is added, its extended type is
 * checked against the class that should map to it. A {@link RuntimeException} is thrown in case of
 * a mismatch.
 */
public class CustomValidationsMap {

    private final ConcurrentHashMap<Class<? extends IIpsObjectPartContainer>, Set<ICustomValidation<? extends IIpsObjectPartContainer>>> internalMap = new ConcurrentHashMap<>();

    /**
     * Returns the set of custom validations stored for the requested class. If there is no element
     * for the requested class, an empty list is returned. Never returns <code>null</code>.
     * <p>
     * Returns a defensive copy.
     * 
     * @param key The class of the instances you want to get
     * @return A list of instances of the given class
     */
    public <K extends IIpsObjectPartContainer> Set<ICustomValidation<?>> get(Class<K> key) {
        return new LinkedHashSet<>(getInternal(key));
    }

    /**
     * Gets the set of custom validations stored for the given key. Creates a new set if absent.
     * <p>
     * Returns the very set instance that is stored in the map for the given key (not a defensive
     * copy). Thus adding values to that set changes this map.
     * <p>
     * This method is thread safe. Ensures that only a single set instance is created for each key,
     * and that multiple threads will share these instances (and not overwrite each other's values).
     * 
     * @param key The {@link IIpsObjectPartContainer} sub class to return {@link ICustomValidation
     *            custom validations} for.
     * @type K a sub class of {@link IIpsObjectPartContainer}.
     */
    private <K extends IIpsObjectPartContainer> Set<ICustomValidation<?>> getInternal(Class<K> key) {
        return internalMap.computeIfAbsent(key,
                $ -> new LinkedHashSet<>());
    }

    /**
     * Adds the custom validation to this map. The custom validations extended class will be used as
     * a key.
     * 
     * @param validation the value you want to add
     */
    public <K extends IIpsObjectPartContainer> void put(ICustomValidation<K> validation) {
        Class<? extends K> extendedClass = validation.getExtendedClass();
        put(extendedClass, validation);
    }

    /**
     * Adds the custom validation to this map. The added validation is also returned for subclasses
     * of the class the validation is responsible for.
     * 
     * @param key the class the validation should be found for
     * @param validation the validation you want to add
     */
    public <K extends IIpsObjectPartContainer> void put(Class<K> key, ICustomValidation<?> validation) {
        putWithRuntimeCheck(key, validation);
    }

    /**
     * Checks whether the key is a super class or the same class as the one the validation is
     * responsible for. Adds the validation if valid. Throws a runtime exception if the classes do
     * not match.
     * 
     * @param key The class the validation should be found for
     * @param validation the validation you want to add to this map
     */
    private <T extends IIpsObjectPartContainer> void putWithRuntimeCheck(Class<? extends T> key,
            ICustomValidation<?> validation) {
        if (validation.getExtendedClass().isAssignableFrom(key)) {
            putInternal(key, validation);
        } else {
            String message = Messages.CustomValidationsMap_MsgCannotAddValidation_classesDoNotMatch;
            throw new RuntimeException(NLS.bind(message,
                    new Object[] { validation, validation.getExtendedClass(), key }));
        }
    }

    private <K extends IIpsObjectPartContainer> void putInternal(Class<K> key, ICustomValidation<?> validation) {
        Set<ICustomValidation<?>> set = getInternal(key);
        set.add(validation);
    }

    /**
     * Adds all custom validations to this map and lets the given key map to them.
     * 
     * @param key the key that should map to all given elements.
     * @param validations the list of custom validations to add.
     */
    public <K extends IIpsObjectPartContainer> void putAll(Class<K> key,
            Collection<ICustomValidation<? extends IIpsObjectPartContainer>> validations) {
        for (ICustomValidation<? extends IIpsObjectPartContainer> customValidation : validations) {
            put(key, customValidation);
        }
    }

    /**
     * Returns whether this map is empty.
     * 
     * @return true if the map is empty or false if there is at least one element.
     */
    public boolean isEmpty() {
        return valuesInternal().isEmpty();
    }

    /**
     * This method returns all values in one collection but does not guaranty any order of the
     * values.
     * 
     * @return All values stored in this map in one collection.
     */
    private Set<ICustomValidation<? extends IIpsObjectPartContainer>> valuesInternal() {
        Set<ICustomValidation<? extends IIpsObjectPartContainer>> result = new LinkedHashSet<>();
        for (Set<ICustomValidation<? extends IIpsObjectPartContainer>> list : internalMap.values()) {
            result.addAll(list);
        }
        return result;
    }

    /**
     * Returns whether this map contains custom validations for the given key.
     * 
     * @param key the class the validations are responsible for
     */
    public <K extends IIpsObjectPartContainer> boolean containsValidationsFor(Class<K> key) {
        return !get(key).isEmpty();
    }

    /**
     * Returns all class instances for which custom validations have been stored in this map.
     */
    public Set<Class<? extends IIpsObjectPartContainer>> getKeys() {
        return internalMap.keySet();
    }

    /**
     * Remove every object in the map.
     */
    public void clear() {
        internalMap.clear();
    }

}
