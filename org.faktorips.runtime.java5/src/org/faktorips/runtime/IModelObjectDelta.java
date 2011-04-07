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

package org.faktorips.runtime;

import java.util.List;

/**
 * A model object delta represents the delta between two model objects. The two model objects must
 * be instances of the same model class. One typical use case is that the two (technical) objects
 * represent the same business object but at two different points in time.
 * 
 * @author Jan Ortmann
 */
public interface IModelObjectDelta {

    /**
     * Constant indicating that the delta is empty, no difference exists between the two objects.
     * <p>
     * If the object is a root object, this means that all properties are equal, no children have
     * been removed, added or have changed their position.
     * <p>
     * If this object is a dependent object (it belongs to a parent), this means that all properties
     * are equal and this object has neither been added to or removed from its parent nor has the
     * position of the object changed.
     */
    public static final int EMPTY = 0;

    /**
     * Delta kind constant indicating that the model object has been added to it's parent.
     */
    public static final int ADDED = 16;

    /**
     * Delta kind constant indicating that the model object has been removed from it's parent.
     */
    public static final int REMOVED = 8;

    /**
     * Delta kind constant indicating that the model object has been moved, meaning it's position in
     * the ordered list of objects has been changed. This type of delta is only returned if the
     * computation method is {@link IDeltaComputationOptions.ComputationMethod#BY_OBJECT}.
     * <p>
     * Note that an object can be moved and changed! In this case getKind() returns MOVED & CHANGED.
     */
    public static final int MOVED = 4;

    /**
     * Delta kind constant indicating that a different model object is now at a given position. This
     * type of delta is only returned if the computation method is
     * {@link IDeltaComputationOptions.ComputationMethod#BY_POSITION}.
     */
    public static final int DIFFERENT_OBJECT_AT_POSITION = 2;

    /**
     * Delta kind constant indicating that the model object has changed somehow. If the object has
     * changed, the type of change is further specified with the following constants. Note that
     * these constants are bit masks as more than one type of change can occur.
     * <p>
     * Note that an object can be moved and changed! In this case getKind() returns MOVED & CHANGED.
     * 
     * @see #STRUCTURE_CHANGED
     * @see #PROPERTY_CHANGED
     * @see #CHILD_CHANGED
     */
    public static final int CHANGED = 1;

    /**
     * Delta change type constant indicating that the object's structure has changed, that means
     * either a child has been added, removed or changed its position (if the computation method is
     * {@link IDeltaComputationOptions.ComputationMethod#BY_OBJECT} or a different object is now at
     * a given position (if the computation method is
     * {@link IDeltaComputationOptions.ComputationMethod#BY_OBJECT}.
     */
    public static final int STRUCTURE_CHANGED = 1;

    /**
     * Delta change type constant indicating that at least one of the object's properties has
     * changed.
     */
    public static final int PROPERTY_CHANGED = 2;

    /**
     * Delta change type constant indicating that at least one child (that has neither been added or
     * removed or just moved) has changed. Either one of the child's properties has changed or it's
     * structure (or both).
     */
    public static final int CHILD_CHANGED = 4;

    /**
     * Delta change type constant indicating that the class of the object has changed.
     */
    public static final int CLASS_CHANGED = 8;

    /**
     * The model object this delta is computed for. If this is a delta for a a child that was added
     * to the new object, the method returns <code>null</code>.
     */
    public IModelObject getOriginalObject();

    /**
     * The model object that is taken as a reference to which the orginal model object is compared
     * to.
     * <p>
     * If this is a delta for a a child that was removed from the orginial model object, the method
     * returns <code>null</code>.
     */
    public IModelObject getReferenceObject();

    /**
     * Returns the kind of change. Note that an object can be moved and changed! To allow this type
     * of information to be returned, a bit mask is used!
     * 
     * @see #EMPTY
     * @see #ADDED
     * @see #REMOVED
     * @see #MOVED
     * @see #DIFFERENT_OBJECT_AT_POSITION
     * @see #CHANGED
     */
    public int getKind();

    /**
     * If delta's kind is {@link #CHANGED}, this method returns the kind of changed defined by the
     * constant listed below. Not that the kind of changes are not mututally excluded, so the
     * returned kinds are bit masked. The method returns 0, if the delta's kind is not
     * {@link #CHANGED}.
     * 
     * @see #PROPERTY_CHANGED
     * @see #STRUCTURE_CHANGED
     * @see #CHILD_CHANGED
     */
    public int getKindOfChange();

    /**
     * Returns <code>true</code> if the delta is empty, the two objects are the same, otherwise
     * <code>false</code>.
     */
    public boolean isEmpty();

    /**
     * Returns <code>true</code> if this model object has been added to its parent,
     * <code>false</code> otherwise.
     */
    public boolean isAdded();

    /**
     * Returns <code>true</code> if this model object has been removed from its parent,
     * <code>false</code> otherwise.
     */
    public boolean isRemoved();

    /**
     * Returns <code>true</code> if this model object has changed its position in the ordered
     * association. This type of delta is only returned if the computation method is
     * {@link IDeltaComputationOptions.ComputationMethod#BY_OBJECT}.
     */
    public boolean isMoved();

    /**
     * Returns <code>true</code> if a different model object is at a position, otherwise
     * <code>false</code>. This type of delta is only returned if the computation method is
     * {@link IDeltaComputationOptions.ComputationMethod#BY_POSITION}.
     */
    public boolean isDifferentObjectAtPosition();

    /**
     * Returns <code>true</code> if this model object has been changed.
     */
    public boolean isChanged();

    /**
     * Returns <code>true</code> if this model object's structure has changed. That means either a
     * child has been added or removed or a child has been been moved.
     */
    public boolean isStructureChanged();

    /**
     * Returns <code>true</code> if the model object's Java class has "changed". (Technically its of
     * course just an instance of another class). Returns <code>false</code> otherwise.
     */
    public boolean isClassChanged();

    /**
     * Returns <code>true</code> if at least one the model object's properties has changed,
     * otherwise <code>false</code>.
     */
    public boolean isPropertyChanged();

    /**
     * Returns <code>true</code> if at least one model object's children has been changed.
     */
    public boolean isChildChanged();

    /**
     * The name of the association to that the object has been added / has been removed from or is
     * moved in.
     */
    public String getAssociation();

    /**
     * Returns the properties that have a different a value in the model object and the reference
     * model object. Returns an empty array if no such property exists.
     */
    public List<String> getChangedProperties();

    /**
     * Returns <code>true</code> if the model object contains a different value for the given
     * property than then reference model object. Returns <code>false</code> otherwise. Returns
     * <code>false</code> if propertyName is <code>null</code>.
     */
    public boolean isPropertyChanged(String propertyName);

    /**
     * Returns deltas for all children of the model object which were added, removed, or changed.
     * Returns an empty array if there are no affected children.
     */
    public List<IModelObjectDelta> getChildDeltas();

    /**
     * Returns deltas for all children of this model object whose kind is included in the given
     * mask. Kind masks are formed by the bitwise or of <code>IModelObjectDelta</code> kind
     * constants. Returns an empty array if there are no affected children.
     */
    public List<IModelObjectDelta> getChildDeltas(int kind);

    /**
     * Accepts the given visitor.
     * <p>
     * The visitor's <code>visit</code> method is called. If the visitor returns <code>true</code>,
     * the delta's children are also visited.
     * 
     * @param visitor the visitor
     * 
     * @throws NullPointerException if visitor is <code>null</code>.
     * 
     * @see IModelObjectDeltaVisitor#visit(IModelObjectDelta)
     */
    public void accept(IModelObjectDeltaVisitor visitor);

}
