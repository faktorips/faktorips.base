/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.changelistener;

/**
 * The type of change that caused a change event to be fired.
 * 
 * @author Daniel Hohenberger
 */
public final class ChangeEventType {

    /**
     * Type that indicates that the model object has changed. No further information about which
     * property or child is available.
     */
    public static final ChangeEventType OBJECT_HAS_CHANGED = new ChangeEventType("OBJECT_HAS_CHANGED");

    /**
     * Type that indicates that a mutable property of the model object has changed.
     */
    public final static ChangeEventType MUTABLE_PROPERTY_CHANGED = new ChangeEventType("MUTABLE_PROPERTY_CHANGED");

    /**
     * Type that indicates that a derived property of the model object has changed.
     */
    public final static ChangeEventType DERIVED_PROPERTY_CHANGED = new ChangeEventType("DERIVED_PROPERTY_CHANGED");

    /**
     * Type that indicates that an object was added to one of the model object's associations.
     */
    public final static ChangeEventType ASSOCIATION_OBJECT_ADDED = new ChangeEventType("ASSOCIATION_OBJECT_ADDED");

    /**
     * Type that indicates that an object was removed from one of the model object's associations.
     */
    public final static ChangeEventType ASSOCIATION_OBJECT_REMOVED = new ChangeEventType("ASSOCIATION_OBJECT_REMOVED");

    /**
     * Type that indicates that the referenced object has changed for one of the model object's 1-1
     * associations.
     */
    public final static ChangeEventType ASSOCIATION_OBJECT_CHANGED = new ChangeEventType("ASSOCIATION_OBJECT_CHANGED");

    private final String name;

    private ChangeEventType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
