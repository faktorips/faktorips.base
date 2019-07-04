/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IDeltaComputationOptions;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectDelta;
import org.faktorips.runtime.IModelObjectDeltaVisitor;
import org.faktorips.runtime.ITimedConfigurableModelObject;
import org.faktorips.runtime.internal.delta.ChildDeltaCreator;

/**
 * IModelObjectDelta implementation.
 * 
 * @author Jan Ortmann
 */
public class ModelObjectDelta implements IModelObjectDelta {

    private static final int STRUCTURAL_CHANGES = IModelObjectDelta.ADDED | IModelObjectDelta.REMOVED
            | IModelObjectDelta.MOVED | IModelObjectDelta.DIFFERENT_OBJECT_AT_POSITION;

    private final IModelObject original;
    private final IModelObject referenceObject;
    private Class<?> modelClass;
    private int kind;
    private int kindOfChange;
    private String association;
    private SortedSet<String> changedProperties = null;

    private final List<IModelObjectDelta> children = new ArrayList<IModelObjectDelta>(0);

    /**
     * @throws NullPointerException if modelObject and referenceModelObject are both
     *             <code>null</code>.
     */
    private ModelObjectDelta(IModelObject original, IModelObject referenceModelObject, int kind, String association) {
        this(original, referenceModelObject, kind, 0);
        this.association = association;
    }

    private ModelObjectDelta(IModelObject original, IModelObject referenceModelObject, int deltaKind,
            int kindOfChange) {
        this.original = original;
        referenceObject = referenceModelObject;
        if (original != null) {
            modelClass = original.getClass();
        } else {
            modelClass = referenceModelObject.getClass();
        }
        kind = deltaKind;
        this.kindOfChange = kindOfChange;
    }

    public static final ModelObjectDelta newDelta(IModelObject object,
            IModelObject refObject,
            IDeltaComputationOptions options) {
        if (object != null && refObject != null) {
            if (!object.getClass().equals(refObject.getClass())) {
                return new ModelObjectDelta(object, refObject, CHANGED, CLASS_CHANGED);
            }
        }

        ModelObjectDelta delta = newEmptyDelta(object, refObject);

        if (object instanceof IConfigurableModelObject && refObject != null) {
            IConfigurableModelObject confObject = (IConfigurableModelObject)object;
            IConfigurableModelObject confRefObject = (IConfigurableModelObject)refObject;
            delta.checkPropertyChange(IConfigurableModelObject.PROPERTY_PRODUCT_COMPONENT,
                    confObject.getProductComponent(), confRefObject.getProductComponent(), options);
        }

        if (object instanceof ITimedConfigurableModelObject && refObject != null) {
            ITimedConfigurableModelObject confObject = (ITimedConfigurableModelObject)object;
            ITimedConfigurableModelObject confRefObject = (ITimedConfigurableModelObject)refObject;
            delta.checkPropertyChange(ITimedConfigurableModelObject.PROPERTY_PRODUCT_CMPT_GENERATION,
                    confObject.getProductCmptGeneration(), confRefObject.getProductCmptGeneration(), options);
        }

        return delta;
    }

    public static final ModelObjectDelta newEmptyDelta(IModelObject object, IModelObject refObject) {
        return new ModelObjectDelta(object, refObject, IModelObjectDelta.EMPTY, 0);
    }

    public static final void createChildDeltas(ModelObjectDelta delta,
            IModelObject original,
            IModelObject refObject,
            String association,
            IDeltaComputationOptions options) {
        new ChildDeltaCreator(association, options).createChildDeltas(delta, original, refObject);
    }

    public static final void createChildDeltas(ModelObjectDelta delta,
            List<? extends IModelObject> originals,
            List<? extends IModelObject> refObjects,
            String association,
            IDeltaComputationOptions options) {
        new ChildDeltaCreator(association, options).createChildDeltas(delta, originals, refObjects);
    }

    public static final ModelObjectDelta newAddDelta(IModelObject addedObject,
            String association,
            IDeltaComputationOptions options) {
        ModelObjectDelta addDelta = new ModelObjectDelta(null, addedObject, IModelObjectDelta.ADDED, association);
        createSubtreeDeltaIfNeeded(addedObject, addDelta, options);
        return addDelta;
    }

    public static final ModelObjectDelta newRemoveDelta(IModelObject removedObject,
            String association,
            IDeltaComputationOptions options) {
        ModelObjectDelta delta = new ModelObjectDelta(removedObject, null, IModelObjectDelta.REMOVED, association);
        createSubtreeDeltaIfNeeded(removedObject, delta, options);
        return delta;
    }

    private static void createSubtreeDeltaIfNeeded(IModelObject existingObject,
            ModelObjectDelta delta,
            IDeltaComputationOptions options) {
        if (options != null && options.isCreateSubtreeDelta()) {
            createSubtreeDelta(existingObject, delta, options);
        }
    }

    private static void createSubtreeDelta(IModelObject existingObject,
            ModelObjectDelta delta,
            IDeltaComputationOptions options) {
        Class<? extends IModelObject> addedObjectClass = existingObject.getClass();
        try {
            Constructor<? extends IModelObject> constructor = addedObjectClass.getConstructor();
            IModelObject newInstance = constructor.newInstance();
            IModelObjectDelta childDelta;
            if (delta.isAdded()) {
                childDelta = ((IDeltaSupport)newInstance).computeDelta(existingObject, options);
            } else if (delta.isRemoved()) {
                childDelta = ((IDeltaSupport)existingObject).computeDelta(newInstance, options);
            } else {
                throw new IllegalArgumentException("Illegal delta type " + delta);
            }
            for (IModelObjectDelta childAddedDelta : childDelta.getChildDeltas()) {
                delta.addChildDelta(childAddedDelta);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "Error in delta computation. Cannot find default construcor for " + addedObjectClass.getName(), e);
        } catch (InstantiationException e) {
            throw new RuntimeException(
                    "Error in delta computation. Cannot create instance of " + addedObjectClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Error in delta computation. Cannot access constructor of " + addedObjectClass.getName(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(
                    "Error in delta computation. Exception while calling constructor of" + addedObjectClass.getName(),
                    e);
        }
    }

    public static final ModelObjectDelta newDifferentObjectAtPositionChangedDelta(IModelObject original,
            IModelObject refObject,
            String association) {
        return new ModelObjectDelta(original, refObject, IModelObjectDelta.DIFFERENT_OBJECT_AT_POSITION, association);
    }

    public static final ModelObjectDelta newChangeDelta(IModelObject original,
            IModelObject refObject,
            int kindOfChange) {
        return new ModelObjectDelta(original, refObject, IModelObjectDelta.CHANGED, kindOfChange);
    }

    /**
     * Adds the child delta to this delta and sets this delta's kindOfChange accordingly. E.g. if
     * the child delta is of kind ADD, this delta's kind of change is marked as structure changed.
     * <p>
     * Note this method ignores the childDelta if it is <code>null</code> or empty!!!
     */
    public void addChildDelta(IModelObjectDelta childDelta) {
        if (childDelta == null || childDelta.isEmpty()) {
            return;
        }
        if ((childDelta.getKind() & STRUCTURAL_CHANGES) > 0) {
            kindOfChange |= STRUCTURE_CHANGED;
        }
        if (childDelta.isChanged()) {
            kindOfChange |= CHILD_CHANGED;
        }
        kind |= CHANGED;
        children.add(childDelta);
    }

    @Override
    public IModelObject getOriginalObject() {
        return original;
    }

    @Override
    public IModelObject getReferenceObject() {
        return referenceObject;
    }

    @Override
    public int getKind() {
        return kind;
    }

    @Override
    public int getKindOfChange() {
        return kindOfChange;
    }

    @Override
    public String getAssociation() {
        return association;
    }

    public void checkPropertyChange(String property, Object value1, Object value2, IDeltaComputationOptions options) {
        if (options.ignore(modelClass, property)) {
            return;
        }
        if (!options.areValuesEqual(modelClass, property, value1, value2)) {
            markPropertyChanged(property);
        }

    }

    public void checkPropertyChange(String property, int value1, int value2, IDeltaComputationOptions options) {
        if (options.ignore(modelClass, property)) {
            return;
        }
        if (value1 != value2) {
            markPropertyChanged(property);
        }
    }

    public void checkPropertyChange(String property, boolean value1, boolean value2, IDeltaComputationOptions options) {
        if (options.ignore(modelClass, property)) {
            return;
        }
        if (value1 != value2) {
            markPropertyChanged(property);
        }
    }

    public void checkPropertyChange(String property, double value1, double value2, IDeltaComputationOptions options) {
        if (options.ignore(modelClass, property)) {
            return;
        }
        if (value1 != value2) {
            markPropertyChanged(property);
        }
    }

    public void checkPropertyChange(String property, float value1, float value2, IDeltaComputationOptions options) {
        if (options.ignore(modelClass, property)) {
            return;
        }
        if (value1 != value2) {
            markPropertyChanged(property);
        }
    }

    public void checkPropertyChange(String property, char value1, char value2, IDeltaComputationOptions options) {
        if (options.ignore(modelClass, property)) {
            return;
        }
        if (value1 != value2) {
            markPropertyChanged(property);
        }
    }

    /**
     * Marks the given property as having a different value in the model object and the reference
     * model object.
     */
    public void markPropertyChanged(String property) {
        if (changedProperties == null) {
            changedProperties = new TreeSet<String>();
        }
        changedProperties.add(property);
        kind |= CHANGED;
        kindOfChange |= PROPERTY_CHANGED;
    }

    @Override
    public boolean isClassChanged() {
        return (kindOfChange & CLASS_CHANGED) > 0;
    }

    @Override
    public boolean isPropertyChanged() {
        return (kindOfChange & PROPERTY_CHANGED) > 0;
    }

    @Override
    public List<String> getChangedProperties() {
        if (changedProperties == null) {
            return new ArrayList<String>(0);
        }
        return new ArrayList<String>(changedProperties);
    }

    @Override
    public boolean isPropertyChanged(String propertyName) {
        if (changedProperties == null || propertyName == null) {
            return false;
        }
        return changedProperties.contains(propertyName);
    }

    /**
     * Marks the delta as one where the object has changed its position.
     */
    public void markMoved() {
        kind |= MOVED;
    }

    @Override
    public boolean isMoved() {
        return (kind & MOVED) > 0;
    }

    @Override
    public boolean isDifferentObjectAtPosition() {
        return (kind & DIFFERENT_OBJECT_AT_POSITION) > 0;
    }

    @Override
    public boolean isAdded() {
        return (kind & ADDED) > 0;
    }

    @Override
    public boolean isChanged() {
        return (kind & CHANGED) > 0;
    }

    @Override
    public boolean isChildChanged() {
        return (kindOfChange & CHILD_CHANGED) > 0;
    }

    @Override
    public boolean isEmpty() {
        return kind == EMPTY;
    }

    @Override
    public boolean isRemoved() {
        return (kind & REMOVED) > 0;
    }

    @Override
    public boolean isStructureChanged() {
        return (kindOfChange & STRUCTURE_CHANGED) > 0;
    }

    @Override
    public List<IModelObjectDelta> getChildDeltas() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public List<IModelObjectDelta> getChildDeltas(int kind) {
        final List<IModelObjectDelta> childrenOfKind = new ArrayList<IModelObjectDelta>();
        for (IModelObjectDelta child : children) {
            if ((child.getKind() & kind) > 0) {
                childrenOfKind.add(child);
            }
        }
        return childrenOfKind;
    }

    @Override
    public void accept(IModelObjectDeltaVisitor visitor) {
        if (visitor.visit(this)) {
            for (int i = 0; i < children.size(); i++) {
                (children.get(i)).accept(visitor);
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        toString(buffer, "");
        return buffer.toString();
    }

    // CSOFF: CyclomaticComplexity
    public void toString(StringBuffer buffer, String indentation) {
        buffer.append(indentation);
        if (isAdded()) {
            buffer.append("+");
            buffer.append(referenceObject);
        } else if (isRemoved()) {
            buffer.append("-");
            buffer.append(original);
        } else if (isDifferentObjectAtPosition()) {
            buffer.append("differentObject");
        } else {
            buffer.append(isChanged() ? "*" : "empty ");
            buffer.append(original);
        }
        if (isMoved()) {
            buffer.append(" (moved)");
        }
        if (changedProperties != null) {
            boolean first = true;
            for (String changedProperty : changedProperties) {
                if (first) {
                    buffer.append(" [");
                    first = false;
                } else {
                    buffer.append(", ");
                }
                buffer.append(changedProperty);
            }
            if (!first) {
                buffer.append(']');
            }
        }
        buffer.append(System.getProperty("line.separator"));
        for (IModelObjectDelta delta : getChildDeltas()) {
            ((ModelObjectDelta)delta).toString(buffer, indentation + "    ");
        }
    }
    // CSON: CyclomaticComplexity

}
