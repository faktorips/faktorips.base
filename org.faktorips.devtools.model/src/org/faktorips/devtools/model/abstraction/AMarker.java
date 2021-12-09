/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstraction;

import static java.util.Objects.requireNonNull;
import static org.faktorips.devtools.model.abstraction.Wrappers.get;
import static org.faktorips.devtools.model.abstraction.Wrappers.run;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.resources.IMarker;
import org.faktorips.devtools.model.abstraction.AResource.PlainJavaResource;
import org.faktorips.runtime.Severity;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * A marker represents information about a resource, where they can be
 * {@link AResource#createMarker(String) added},
 * {@link AResource#findMarkers(String, boolean, org.faktorips.devtools.model.abstraction.AResource.AResourceTreeTraversalDepth)
 * queried} and
 * {@link AResource#deleteMarkers(String, boolean, org.faktorips.devtools.model.abstraction.AResource.AResourceTreeTraversalDepth)
 * removed}.
 * <p>
 * Every marker has a unique {@link #getId() ID} and a type that is used to handle markers of the
 * same type on different resources as well as optionally some attributes, each identified by a name
 * and depending on the type.
 */
public interface AMarker extends AAbstraction {

    /**
     * Returns this marker's unique ID.
     */
    long getId();

    /**
     * Returns this marker's type.
     */
    String getType();

    /**
     * Convenience method that returns whether this marker has a 'severity' attribute marking it as
     * an 'error'.
     */
    boolean isError();

    /**
     * Returns the value of the given attribute. Attributes may be strings, integers or booleans.
     * When an attribute is not set {@code null} is returned.
     *
     * @param attributeName an attribute's name
     * @return the value associated with the given attribute
     */
    @CheckForNull
    Object getAttribute(String attributeName);

    /**
     * Returns the value of the given attribute of type String. When the attribute is not set the
     * given default value is returned.
     *
     * @param attributeName a String attribute's name
     * @return the value associated with the given attribute or the default value
     */
    String getAttribute(String attributeName, String defaultValue);

    /**
     * Returns the value of the given attribute of type boolean. When the attribute is not set the
     * given default value is returned.
     *
     * @param attributeName a boolean attribute's name
     * @return the value associated with the given attribute or the default value
     */
    boolean getAttribute(String attributeName, boolean defaultValue);

    /**
     * Returns the value of the given attribute of type int. When the attribute is not set the given
     * default value is returned.
     *
     * @param attributeName an integer attribute's name
     * @return the value associated with the given attribute or the default value
     */
    int getAttribute(String attributeName, int defaultValue);

    /**
     * Sets the value of the given attribute. Attributes may be strings, integers or booleans. An
     * attribute may be removed by setting its value to {@code null}.
     *
     * @param attributeName an attribute's name
     * @param value the value to be associated with the given attribute
     */
    void setAttribute(String attributeName, @CheckForNull Object value);

    /**
     * Sets the values of the given attributes. Attributes may be strings, integers or booleans. An
     * attribute may be removed by setting its value to {@code null}.
     *
     * @param attributeNames an array of attribute's name
     * @param values the values to be associated with the given attribute, must be of the same
     *            length as the array of attribute names
     */
    void setAttributes(String[] attributeNames, Object[] values);

    /**
     * Deletes this marker from its associated resource.
     */
    void delete();

    public static class AEclipseMarker extends AWrapper<IMarker> implements AMarker {

        AEclipseMarker(IMarker marker) {
            super(marker);
        }

        IMarker marker() {
            return unwrap();
        }

        @Override
        public void delete() {
            run(marker()::delete);
        }

        @Override
        public Object getAttribute(String attributeName) {
            return get(() -> marker().getAttribute(attributeName));
        }

        @Override
        public String getAttribute(String attributeName, String defaultValue) {
            return get(() -> marker().getAttribute(attributeName, defaultValue));
        }

        @Override
        public int getAttribute(String attributeName, int defaultValue) {
            return get(() -> marker().getAttribute(attributeName, defaultValue));
        }

        @Override
        public boolean getAttribute(String attributeName, boolean defaultValue) {
            return get(() -> marker().getAttribute(attributeName, defaultValue));
        }

        @Override
        public void setAttribute(String attributeName, Object value) {
            run(() -> marker().setAttribute(attributeName, value));
        }

        @Override
        public void setAttributes(String[] attributeNames, Object[] values) {
            run(() -> marker().setAttributes(attributeNames, values));
        }

        @Override
        public boolean isError() {
            return marker().getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR) == IMarker.SEVERITY_ERROR;
        }

        @Override
        public String getType() {
            return get(marker()::getType);
        }

        @Override
        public long getId() {
            return marker().getId();
        }

    }

    // TODO Marker im Workspace persistieren?
    public static class PlainJavaMarker extends AWrapper<PlainJavaMarkerImpl> implements AMarker {

        public PlainJavaMarker(PlainJavaResource resource, String type) {
            this(new PlainJavaMarkerImpl(resource, type));
        }

        public PlainJavaMarker(PlainJavaMarkerImpl original) {
            super(original);
        }

        private PlainJavaMarkerImpl marker() {
            return unwrap();
        }

        @Override
        public void delete() {
            marker().getResource().deleteMarker(this);
        }

        @Override
        public Object getAttribute(String attributeName) {
            return marker().getAttribute(attributeName);
        }

        @Override
        public String getAttribute(String attributeName, String defaultValue) {
            return marker().getAttribute(attributeName, defaultValue);
        }

        @Override
        public int getAttribute(String attributeName, int defaultValue) {
            return marker().getAttribute(attributeName, defaultValue);
        }

        @Override
        public boolean getAttribute(String attributeName, boolean defaultValue) {
            return marker().getAttribute(attributeName, defaultValue);
        }

        @Override
        public void setAttribute(String attributeName, Object value) {
            marker().setAttribute(attributeName, value);
        }

        @Override
        public void setAttributes(String[] attributeNames, Object[] values) {
            marker().setAttributes(attributeNames, values);
        }

        @Override
        public boolean isError() {
            return marker().isError();
        }

        @Override
        public String getType() {
            return marker().getType();
        }

        @Override
        public long getId() {
            return marker().getId();
        }

    }

    public static class PlainJavaMarkerImpl {

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

}
