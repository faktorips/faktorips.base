/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.modeltype.IModelElement;

/**
 * 
 * @author Daniel Hohenberger
 */
public class AbstractModelElement implements IModelElement {

    private final Map<Locale, String> labelsByLocale = new HashMap<Locale, String>();

    private final Map<Locale, String> descriptionsByLocale = new HashMap<Locale, String>();

    private Map<String, Object> extPropertyValues;

    private final String name;

    public AbstractModelElement(String name) {
        this.name = name;
    }

    @Override
    public String getLabel(Locale locale) {
        String label = labelsByLocale.get(locale);
        return IpsStringUtils.isEmpty(label) ? getName() : label;
    }

    @Override
    public String getDescription(Locale locale) {
        String description = descriptionsByLocale.get(locale);
        return IpsStringUtils.isEmpty(description) ? IpsStringUtils.EMPTY : description;
    }

    @Override
    public Object getExtensionPropertyValue(String propertyId) {
        if (extPropertyValues == null) {
            return null;
        }
        return extPropertyValues.get(propertyId);
    }

    /**
     * Sets the value of the extension property <code>propertyId</code>.
     */
    public void setExtensionPropertyValue(String propertyId, Object value) {
        if (extPropertyValues == null) {
            extPropertyValues = new HashMap<String, Object>(5);
        }
        extPropertyValues.put(propertyId, value);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getExtensionPropertyIds() {
        if (extPropertyValues == null) {
            return new HashSet<String>(0);
        }
        return extPropertyValues.keySet();
    }
}
