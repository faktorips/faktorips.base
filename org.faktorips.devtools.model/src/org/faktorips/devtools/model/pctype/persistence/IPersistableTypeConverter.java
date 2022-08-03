/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.pctype.persistence;

import java.lang.annotation.Annotation;

import org.faktorips.datatype.Datatype;

/**
 * Converter between the database representation and the internal representation of an IPS data type
 * (representing a policy component type attribute for instance).
 * <p>
 * As of version 2 JPA (Java Persistence API) does not support the concept of converters at all. One
 * has to use persistence provider specific extensions, named "custom user types" in Hibernate. This
 * class encapsulates these extensions.
 * 
 * @author Roman Grutza
 */
public interface IPersistableTypeConverter {

    /**
     * Returns the annotation to use on attributes of the given data type.
     */
    Annotation getAnnotationForDatatype(Datatype datatype);

    /**
     * Returns an annotation suitable to persist a temporal type as a time stamp.
     */
    Annotation getAnnotationForTimestampDatatype();

    /**
     * Returns an annotation suitable to persist a temporal type using both the date and time part.
     */
    Annotation getAnnotationForDateAndTimeDatatype();

    /**
     * Returns an annotation suitable to persist a temporal type using only the date part.
     */
    Annotation getAnnotationForDateOnlyDatatype();

}
