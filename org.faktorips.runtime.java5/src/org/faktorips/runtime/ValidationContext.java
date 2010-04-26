/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

    private Locale locale;
    private Map<String, Object> propertyValues = new HashMap<String, Object>();

    /**
     * Creates a new validation context with the specified local.
     * 
     * @throws NullPointerException if the specified parameter is null
     */
    public ValidationContext(Locale locale) {
        if (locale == null) {
            throw new NullPointerException("The parameter locale cannot be null.");
        }
        this.locale = locale;
    }

    /**
     * Creates a new validation context with the default locale ({@link java.util.Locale}
     * .getDefault()).
     */
    public ValidationContext() {
        this(Locale.getDefault());
    }

    /**
     * {@inheritDoc}
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * {@inheritDoc}
     */
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
}
