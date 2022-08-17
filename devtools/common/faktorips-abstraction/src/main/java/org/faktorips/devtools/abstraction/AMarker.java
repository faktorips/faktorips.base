/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * A marker represents information about a resource, where they can be
 * {@link AResource#createMarker(String) added},
 * {@link AResource#findMarkers(String, boolean, org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth)
 * queried} and
 * {@link AResource#deleteMarkers(String, boolean, org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth)
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

}
