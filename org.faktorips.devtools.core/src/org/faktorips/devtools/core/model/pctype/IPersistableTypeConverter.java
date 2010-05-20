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

package org.faktorips.devtools.core.model.pctype;

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
    public Annotation getAnnotationForDatatype(Datatype datatype);

    /**
     * Returns an annotation suitable to persist a temporal type as a time stamp.
     */
    public Annotation getAnnotationForTimestampDatatype();

    /**
     * Returns an annotation suitable to persist a temporal type using both the date and time part.
     */
    public Annotation getAnnotationForDateAndTimeDatatype();

    /**
     * Returns an annotation suitable to persist a temporal type using only the date part.
     */
    public Annotation getAnnotationForDateOnlyDatatype();

}
