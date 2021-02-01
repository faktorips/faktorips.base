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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Default implementation of {@link IValidationContext}.
 * 
 * @author Peter Erzberger
 */
public class ValidationContext implements IValidationContext {

    private final Locale locale;

    private final Map<String, Object> propertyValues = new HashMap<String, Object>();

    private final ClassLoader resourceClassLoader;

    /**
     * Creates a new validation context with the specified local.
     * 
     * @param locale Setting the locale of this context
     * @param resourceClassLoader setting the {@link ClassLoader} used to load resources
     * 
     * @throws NullPointerException if one of the specified parameters is null
     */
    public ValidationContext(Locale locale, ClassLoader resourceClassLoader) {
        if (locale == null) {
            throw new NullPointerException("The parameter locale cannot be null.");
        }
        if (resourceClassLoader == null) {
            throw new NullPointerException("The parameter resourceClassLoader cannot be null.");
        }
        this.locale = locale;
        this.resourceClassLoader = resourceClassLoader;
    }

    /**
     * Creates a new validation context with the specified local.
     * 
     * @throws NullPointerException if the specified parameter is null
     */
    public ValidationContext(Locale locale) {
        this(locale, ValidationContext.class.getClassLoader());
    }

    /**
     * Creates a new validation context with the default locale ({@link java.util.Locale}
     * .getDefault()).
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

}
