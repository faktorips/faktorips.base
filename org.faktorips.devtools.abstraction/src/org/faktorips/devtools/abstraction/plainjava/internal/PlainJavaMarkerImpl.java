/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.faktorips.runtime.Severity;

public class PlainJavaMarkerImpl {

    public static final String SEVERITY = "severity"; //$NON-NLS-1$

    private final PlainJavaResource resource;
    private final String type;
    private final long id;
    private final Map<String, Object> attributes;

    public PlainJavaMarkerImpl(PlainJavaResource resource, String type) {
        this.resource = resource;
        this.type = type;
        id = resource.getWorkspace().getNextMarkerId();
        attributes = new LinkedHashMap<>();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(id);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof PlainJavaMarkerImpl)) {
            return false;
        }
        PlainJavaMarkerImpl other = (PlainJavaMarkerImpl)obj;
        return id == other.id;
    }

    /**
     * Returns whether this marker's type equals the given type (or one of its subtypes)
     *
     * @param type the marker type to compare with
     * @param includeSubTypes whether to check exact matches or also subtype matches
     * @return whether this marker's type equals the given type (or one of its subtypes)
     */
    // TODO document how subtypes are defined
    public boolean equalsType(String type, boolean includeSubTypes) {
        if (this.type == null && type == null) {
            return true;
        }
        if (this.type == null || type == null) {
            return false;
        }
        return this.type.equals(type);
        // TODO handle subtypes
    }

    public Object getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }

    public String getAttribute(String attributeName, String defaultValue) {
        return (String)attributes.getOrDefault(attributeName, defaultValue);
    }

    public int getAttribute(String attributeName, int defaultValue) {
        return (int)attributes.getOrDefault(attributeName, defaultValue);
    }

    public boolean getAttribute(String attributeName, boolean defaultValue) {
        return (boolean)attributes.getOrDefault(attributeName, defaultValue);
    }

    public void setAttribute(String attributeName, Object value) {
        if (value == null) {
            attributes.remove(attributeName);
        } else {
            attributes.put(attributeName, value);
        }
    }

    public void setAttributes(String[] attributeNames, Object[] values) {
        if (requireNonNull(attributeNames, "attributeNames must not be null").length != requireNonNull(values, //$NON-NLS-1$
                "values must not be null").length) { //$NON-NLS-1$
            throw new IllegalArgumentException("The number of attribute names(" + attributeNames.length //$NON-NLS-1$
                    + ") does not match the number of values(" + values.length + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        for (int i = 0; i < attributeNames.length; i++) {
            setAttribute(attributeNames[i], values[i]);
        }
    }

    public boolean isError() {
        return attributes.getOrDefault(SEVERITY, Severity.ERROR).equals(Severity.ERROR);
    }

    public String getType() {
        return type;
    }

    public long getId() {
        return id;
    }

    public PlainJavaResource getResource() {
        return resource;
    }

}