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

package org.faktorips.runtime.internal;

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

/**
 * IModelObjectDelta implementation.
 * 
 * @author Jan ortmann
 */
public class ModelObjectDelta implements IModelObjectDelta {

    private final static int STRUCTURAL_CHANGES = IModelObjectDelta.ADDED | IModelObjectDelta.REMOVED
            | IModelObjectDelta.MOVED | IModelObjectDelta.DIFFERENT_OBJECT_AT_POSITION;

    public final static ModelObjectDelta newDelta(IModelObject object,
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
            delta.checkPropertyChange(IConfigurableModelObject.PROPERTY_PRODUCT_COMPONENT, confObject
                    .getProductComponent(), confRefObject.getProductComponent(), options);
            delta.checkPropertyChange(IConfigurableModelObject.PROPERTY_PRODUCT_CMPT_GENERATION, confObject
                    .getProductCmptGeneration(), confRefObject.getProductCmptGeneration(), options);
        }
        return delta;
    }

    public final static ModelObjectDelta newEmptyDelta(IModelObject object, IModelObject refObject) {
        return new ModelObjectDelta(object, refObject, IModelObjectDelta.EMPTY, 0);
    }

    public final static void createChildDeltas(ModelObjectDelta delta,
            IModelObject original,
            IModelObject refObject,
            String association,
            IDeltaComputationOptions options) {

        if (delta == null) {
            return;
        }
        if (original == null) {
            if (refObject != null) {
                delta.addChildDelta(newAddDelta(refObject, association));
            }
            return;
        }
        if (refObject == null) {
            delta.addChildDelta(newRemoveDelta(original, association));
            return;
        }
        if (options.isSame(original, refObject)) {
            IModelObjectDelta childDelta = ((IDeltaSupport)original).computeDelta(refObject, options);
            delta.addChildDelta(childDelta);
            return;
        }
        if (options.getMethod(association) == IDeltaComputationOptions.ComputationMethod.BY_POSITION) {
            delta.addChildDelta(newDifferentObjectAtPositionChangedDelta(original, refObject, association));
        } else if (options.getMethod(association) == IDeltaComputationOptions.ComputationMethod.BY_OBJECT) {
            delta.addChildDelta(newRemoveDelta(original, association));
            delta.addChildDelta(newAddDelta(refObject, association));
        } else {
            throw new RuntimeException("Unknown delta computation method " + options.getMethod(association));
        }
    }

    public final static void createChildDeltas(ModelObjectDelta delta,
            List<? extends IModelObject> originals,
            List<? extends IModelObject> refObjects,
            String association,
            IDeltaComputationOptions options) {

        if (delta == null) {
            return;
        }
        if (options.getMethod(association) == IDeltaComputationOptions.ComputationMethod.BY_POSITION) {
            createChildDeltasPerPosition(delta, originals, refObjects, association, options);
        } else if (options.getMethod(association) == IDeltaComputationOptions.ComputationMethod.BY_OBJECT) {
            createChildDeltasPerObject(delta, originals, refObjects, association, options);
        } else {
            throw new RuntimeException("Unknown delta computation method " + options.getMethod(association));
        }
    }

    private static void createChildDeltasPerPosition(ModelObjectDelta delta,
            List<? extends IModelObject> originals,
            List<? extends IModelObject> refObjects,
            String association,
            IDeltaComputationOptions options) {
        int max = Math.max(originals.size(), refObjects.size());
        for (int i = 0; i < max; i++) {
            IModelObject original = getModelObject(originals, i);
            IModelObject refObject = getModelObject(refObjects, i);
            if (original != null) {
                if (refObject == null) {
                    delta.addChildDelta(newRemoveDelta(original, association));
                } else {
                    if (options.isSame(original, refObject)) {
                        IModelObjectDelta childDelta = ((IDeltaSupport)original).computeDelta(refObject, options);
                        delta.addChildDelta(childDelta);
                    } else {
                        delta.addChildDelta(newDifferentObjectAtPositionChangedDelta(original, refObject, association));
                    }
                }
            } else {
                if (refObject != null) {
                    delta.addChildDelta(newAddDelta(refObject, association));
                } else {
                    throw new RuntimeException("Error in delta computation. Both objects null in assocation "
                            + association);
                }
            }
        }
    }

    private static IModelObject getModelObject(List<? extends IModelObject> originals, int index) {
        if (index < originals.size()) {
            return originals.get(index);
        }
        return null;
    }

    private static void createChildDeltasPerObject(ModelObjectDelta delta,
            List<? extends IModelObject> originals,
            List<? extends IModelObject> refObjects,
            String association,
            IDeltaComputationOptions options) {
        int removeCounter = 0;
        int size = originals.size();
        for (int i = 0; i < size; i++) {
            IModelObjectDelta childDelta = createRemoveMoveOrChangeDelta(originals.get(i), i, refObjects, association,
                    options);
            delta.addChildDelta(childDelta);
            if (childDelta.isRemoved()) {
                removeCounter++;
            }
        }
        int refSize = refObjects.size();
        if (size - removeCounter == refSize) {
            return; // nothing has been added
        }
        for (int i = 0; i < refSize; i++) {
            boolean exists = false;
            for (int j = 0; j < size; j++) {
                if (options.isSame(originals.get(j), refObjects.get(i))) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                delta.addChildDelta(newAddDelta(refObjects.get(i), association));
            }
        }
    }

    private final static IModelObjectDelta createRemoveMoveOrChangeDelta(IModelObject original,
            int position,
            List<? extends IModelObject> refObjects,
            String association,
            IDeltaComputationOptions options) {

        int refSize = refObjects.size();
        if (position < refSize && options.isSame(original, refObjects.get(position))) {
            IModelObjectDelta childDelta = ((IDeltaSupport)original).computeDelta(refObjects.get(position), options);
            return childDelta;
        }
        // check for moved object
        for (int i = 0; i < refSize; i++) {
            if (i != position) {
                IModelObject refModelObject = refObjects.get(i);
                if (options.isSame(original, refModelObject)) {
                    IModelObjectDelta childDelta = ((IDeltaSupport)original).computeDelta(refModelObject, options);
                    ((ModelObjectDelta)childDelta).markMoved();
                    return childDelta;
                }
            }
        }
        return newRemoveDelta(original, association);
    }

    public final static ModelObjectDelta newAddDelta(IModelObject addedObject, String association) {
        return new ModelObjectDelta(null, addedObject, IModelObjectDelta.ADDED, association);
    }

    public final static ModelObjectDelta newRemoveDelta(IModelObject removedObject, String association) {
        return new ModelObjectDelta(removedObject, null, IModelObjectDelta.REMOVED, association);
    }

    public final static ModelObjectDelta newDifferentObjectAtPositionChangedDelta(IModelObject original,
            IModelObject refObject,
            String association) {
        return new ModelObjectDelta(original, refObject, IModelObjectDelta.DIFFERENT_OBJECT_AT_POSITION, association);
    }

    public final static ModelObjectDelta newChangeDelta(IModelObject original, IModelObject refObject, int kindOfChange) {
        return new ModelObjectDelta(original, refObject, IModelObjectDelta.CHANGED, kindOfChange);
    }

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

    private ModelObjectDelta(IModelObject original, IModelObject referenceModelObject, int deltaKind, int kindOfChange) {
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

    /**
     * {@inheritDoc}
     */
    public IModelObject getOriginalObject() {
        return original;
    }

    /**
     * {@inheritDoc}
     */
    public IModelObject getReferenceObject() {
        return referenceObject;
    }

    /**
     * {@inheritDoc}
     */
    public int getKind() {
        return kind;
    }

    /**
     * {@inheritDoc}
     */
    public int getKindOfChange() {
        return kindOfChange;
    }

    /**
     * {@inheritDoc}
     */
    public String getAssociation() {
        return association;
    }

    public void checkPropertyChange(String property, Object value1, Object value2, IDeltaComputationOptions options) {
        if (options.ignore(modelClass, property)) {
            return;
        }
        if (!ObjectUtil.equals(value1, value2)) {
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

    /**
     * {@inheritDoc}
     */
    public boolean isClassChanged() {
        return (kindOfChange & CLASS_CHANGED) > 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPropertyChanged() {
        return (kindOfChange & PROPERTY_CHANGED) > 0;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getChangedProperties() {
        if (changedProperties == null) {
            return new ArrayList<String>(0);
        }
        return new ArrayList<String>(changedProperties);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public boolean isMoved() {
        return (kind & MOVED) > 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDifferentObjectAtPosition() {
        return (kind & DIFFERENT_OBJECT_AT_POSITION) > 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAdded() {
        return (kind & ADDED) > 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isChanged() {
        return (kind & CHANGED) > 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isChildChanged() {
        return (kindOfChange & CHILD_CHANGED) > 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return kind == EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRemoved() {
        return (kind & REMOVED) > 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isStructureChanged() {
        return (kindOfChange & STRUCTURE_CHANGED) > 0;
    }

    /**
     * {@inheritDoc}
     */
    public List<IModelObjectDelta> getChildDeltas() {
        return Collections.unmodifiableList(children);
    }

    /**
     * {@inheritDoc}
     */
    public List<IModelObjectDelta> getChildDeltas(int kind) {
        final List<IModelObjectDelta> childrenOfKind = new ArrayList<IModelObjectDelta>();
        for (IModelObjectDelta child : children) {
            if ((child.getKind() & kind) > 0) {
                childrenOfKind.add(child);
            }
        }
        return childrenOfKind;
    }

    /**
     * {@inheritDoc}
     */
    public void accept(IModelObjectDeltaVisitor visitor) {
        if (visitor.visit(this)) {
            for (int i = 0; i < children.size(); i++) {
                (children.get(i)).accept(visitor);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        toString(buffer, "");
        return buffer.toString();
    }

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
}
