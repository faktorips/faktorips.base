/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;

/**
 * With this class its possible to define different default values in the
 * {@link IIpsProjectProperties} for new and existing Ips-Projects. Basically if a property is not
 * read in from the .ipsProject file, we assume it is an existing project and therefore needs an
 * different default value.
 * <ul>
 * <li>add a property with its setter method and the default value</li>
 * <li>while reading in the .ipsProject file, check if the property was added</li>
 * <li>for all added properties that were not found apply the provide setter with the new default
 * value</li>
 * </ul>
 * 
 */
public class IpsProjectPropertiesForOldVersion {

    private final Map<String, PropertiesHolder<?>> properties = new ConcurrentHashMap<>();

    /**
     * Add an property name with it's setter method and the new value.
     * 
     * @param propertyName The name of the property
     * @param setter The setter method of the property
     * @param newValue The value to set
     */
    public <T> void add(String propertyName, BiConsumer<IpsProjectProperties, T> setter, T newValue) {
        properties.put(propertyName, new PropertiesHolder<>(false, setter, newValue));
    }

    /**
     * While reading in the settings, from the .ipsProject file, check if the specific setting was
     * added with {@link #add(String, BiConsumer, Object)}.
     * 
     * @param propertyName The property name to check
     * @return {@code true} if the property was found
     */
    public boolean checkIfFound(String propertyName) {
        return properties.computeIfPresent(propertyName, ($, v) -> v.setFound(true)) != null;
    }

    /**
     * After reading in the settings, from the .ipsProject file, check if an added property was not
     * found with the check method and apply the setter with the new default value.
     * 
     * @param projectSettings The project settings at which the setter should be called
     */
    @SuppressWarnings("unchecked")
    public <T> void applyNewValue(IpsProjectProperties projectSettings) {
        properties.forEach(($, property) -> {
            if (property.isNotFound()) {
                ((BiConsumer<IpsProjectProperties, T>)property.getSetter()).accept(projectSettings,
                        (T)property.getNewValue());
            }
        });
    }

    private class PropertiesHolder<T> {
        private final BiConsumer<IpsProjectProperties, T> setter;
        private final T newValue;
        private boolean found;

        protected PropertiesHolder(boolean found, BiConsumer<IpsProjectProperties, T> setter, T newValue) {
            this.newValue = newValue;
            this.found = found;
            this.setter = setter;
        }

        protected T getNewValue() {
            return newValue;
        }

        protected BiConsumer<IpsProjectProperties, T> getSetter() {
            return setter;
        }

        protected PropertiesHolder<T> setFound(boolean found) {
            this.found = found;
            return this;
        }

        protected boolean isNotFound() {
            return !found;
        }

    }
}
