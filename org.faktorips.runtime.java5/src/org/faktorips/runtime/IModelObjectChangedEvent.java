/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * Notification event that a model object has changed.
 * 
 * <p><strong>
 * The listener support is experimental in this version.
 * The API might change without notice until it is finalized in a future version.
 * </strong>
 * 
 * 
 * @author Jan Ortmann
 */
public interface IModelObjectChangedEvent {
    
    public enum Type{

    /**
     * Type that indicates that the model object has changed. No further information
     * about which property or child is available.
     */
    OBJECT_HAS_CHANGED,

    /**
     * Type that indicates that a mutable property of the model object has changed.
     */
    MUTABLE_PROPERTY_CHANGED,
    
    /**
     * Type that indicates that a derived property of the model object has changed.
     */
    DERIVED_PROPERTY_CHANGED,

    /**
     * Type that indicates that an object was added to one of the model object's relations.
     */
    RELATION_OBJECT_ADDED,

    /**
     * Type that indicates that an object was removed from one of the model object's relations.
     */
    RELATION_OBJECT_REMOVED,

    /**
     * Type that indicates that the referenced object has changed for one of the model
     * object's 1-1 relations.
     */
    RELATION_OBJECT_CHANGED;
    }

    /**
     * Type that indicates that the model object has changed. No further information
     * about which property or child is available.
     */
    public static final Type OBJECT_HAS_CHANGED = Type.OBJECT_HAS_CHANGED;

    /**
     * Type that indicates that a mutable property of the model object has changed.
     */
    public static final Type MUTABLE_PROPERTY_CHANGED = Type.MUTABLE_PROPERTY_CHANGED;
    
    /**
     * Type that indicates that a derived property of the model object has changed.
     */
    public static final Type DERIVED_PROPERTY_CHANGED = Type.DERIVED_PROPERTY_CHANGED;

    /**
     * Type that indicates that an object was added to one of the model object's relations.
     */
    public static final Type RELATION_OBJECT_ADDED = Type.RELATION_OBJECT_ADDED;

    /**
     * Type that indicates that an object was removed from one of the model object's relations.
     */
    public static final Type RELATION_OBJECT_REMOVED = Type.RELATION_OBJECT_REMOVED;

    /**
     * Type that indicates that the referenced object has changed for one of the model
     * object's 1-1 relations.
     */
    public static final Type RELATION_OBJECT_CHANGED = Type.RELATION_OBJECT_CHANGED;

    /**
     * Returns the model object that has changed.
     */
    public IModelObject getChangedObject();

    /**
     * Returns one of the type constant that indicate what aspect of the object has changed.
     * 
     * @see Type#MUTABLE_PROPERTY_CHANGED
     * @see Type#DERIVED_PROPERTY_CHANGED
     * @see Type#RELATION_OBJECT_ADDED
     * @see Type#RELATION_OBJECT_CHANGED
     * @see Type#RELATION_OBJECT_REMOVED
     */
    public Type getType();
    
    /**
     * Returns <code>true</code> if a property (mutable or derived) has changed,
     * otherwise <code>false</code>.
     * 
     * @see Type#MUTABLE_PROPERTY_CHANGED
     * @see Type#DERIVED_PROPERTY_CHANGED
     */
    public boolean propertyChanged();
    
    /**
     * Returns the name of the property that has changed.
     */
    public String getPropertyName();

    /**
     * Returns the child that was either added or removed. Returns <code>null</code> if this is
     * not an event that notifies about child addition or removal.
     */
	public IModelObject getChildObject();
}
