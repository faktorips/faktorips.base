/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.faktorips.runtime.validation.DefaultGenericAttributeValidationConfiguration;
import org.faktorips.runtime.validation.IGenericAttributeValidationConfiguration;

/**
 * Default implementation of {@link IValidationContext}.
 */
public class ValidationContext implements IValidationContext {

    private final Locale locale;

    private final Map<String, Object> propertyValues = new HashMap<>();

    private final ClassLoader resourceClassLoader;

    private final IGenericAttributeValidationConfiguration genericAttributeValidationConfiguration;

    /**
     * Creates a new validation context with the specified locale, resource {@link ClassLoader} and
     * {@link IGenericAttributeValidationConfiguration}.
     * 
     * @param locale Setting the locale of this context
     * @param resourceClassLoader setting the {@link ClassLoader} used to load resources
     * 
     * @throws NullPointerException if one of the specified parameters is {@code null}
     */
    public ValidationContext(Locale locale, ClassLoader resourceClassLoader,
            IGenericAttributeValidationConfiguration genericAttributeValidationConfiguration) {
        this.locale = requireNonNull(locale, "The parameter locale cannot be null.");
        this.resourceClassLoader = requireNonNull(resourceClassLoader,
                "The parameter resourceClassLoader cannot be null.");
        this.genericAttributeValidationConfiguration = requireNonNull(genericAttributeValidationConfiguration,
                "The parameter genericAttributeValidationConfiguration cannot be null.");
    }

    /**
     * Creates a new validation context with the specified locale, resource {@link ClassLoader} and
     * a {@link DefaultGenericAttributeValidationConfiguration}.
     * 
     * @param locale Setting the locale of this context
     * @param resourceClassLoader setting the {@link ClassLoader} used to load resources
     * 
     * @throws NullPointerException if one of the specified parameters is {@code null}
     */
    public ValidationContext(Locale locale, ClassLoader resourceClassLoader) {
        this(locale, resourceClassLoader, new DefaultGenericAttributeValidationConfiguration(locale));
    }

    /**
     * Creates a new validation context with the specified locale and a
     * {@link DefaultGenericAttributeValidationConfiguration}.
     * 
     * @throws NullPointerException if the specified locale is {@code null}
     */
    public ValidationContext(Locale locale) {
        this(locale, ValidationContext.class.getClassLoader());
    }

    /**
     * Creates a new validation context with the default locale ({@link java.util.Locale}
     * .getDefault()) and a {@link DefaultGenericAttributeValidationConfiguration}.
     */
    public ValidationContext() {
        this(Locale.getDefault());
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Object getValue(String propertyName) {
        return propertyValues.get(propertyName);
    }

    /**
     * Sets the value of the specified property.
     * 
     * @param propertyName the name of the property
     * @param value the value of the property
     */
    public void setValue(String propertyName, Object value) {
        propertyValues.put(propertyName, value);
    }

    /**
     * @return Returns the resourceClassLoader.
     */
    public ClassLoader getResourceClassLoader() {
        return resourceClassLoader;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation returns the configuration passed to the
     *               {@link #ValidationContext(Locale, ClassLoader, IGenericAttributeValidationConfiguration)
     *               constructor}.
     */
    @Override
    public IGenericAttributeValidationConfiguration getGenericAttributeValidationConfiguration() {
        return genericAttributeValidationConfiguration;
    }

}
