/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.internal.delta;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.faktorips.runtime.IDeltaComputationOptions;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectDelta;
import org.faktorips.runtime.internal.ModelObjectDelta;

/**
 * Internal utility class to create {@link ModelObjectDelta} for associations.
 * <p>
 * Note that this is an internal utility class whose API may not stable and thus should not be
 * called by client code directly.
 */
public class ChildDeltaCreator {

    private final String association;
    private final IDeltaComputationOptions options;

    /**
     * Instantiates the creator for the given association name, kind and
     * {@link IDeltaComputationOptions}.
     * 
     * @param association The name of the association, will be provided to the created delta
     * @param options The {@link IDeltaComputationOptions} that configures some behavior of delta
     *            computation
     */
    public ChildDeltaCreator(String association, IDeltaComputationOptions options) {
        this.association = association;
        this.options = options;
    }

    /**
     * Create and add a child delta for the comparison of original and reference model object.
     * <p>
     * The objects are compared according to the {@link IDeltaComputationOptions}. If one of the
     * objects no longer exists, {@code null} is given to the appropriate parameter.
     * 
     * @param delta The delta to which the child deltas are added
     * @param original The associated object of the original model object or {@code null} if there
     *            is no object references
     * @param refObject The associated object of the reference model object or {@code null} if there
     *            is no object references
     */
    public void createChildDeltas(ModelObjectDelta delta, IModelObject original, IModelObject refObject) {
        createChildDeltas(delta, listOf(original), listOf(refObject));
    }

    private static List<IModelObject> listOf(IModelObject item) {
        if (item != null) {
            return Collections.singletonList(item);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Create and add all child deltas for the list of associated objects. The objects are compared
     * according to the {@link IDeltaComputationOptions}.
     * 
     * @param delta The delta to which the child deltas are added
     * @param originals The list of associated objects of the original model object
     * @param refObjects The list of associated objects of the reference model object
     */
    public void createChildDeltas(ModelObjectDelta delta,
            List<? extends IModelObject> originals,
            List<? extends IModelObject> refObjects) {

        if (delta == null) {
            return;
        } else if (options.getMethod(association) == IDeltaComputationOptions.ComputationMethod.BY_POSITION) {
            createChildDeltasPerPosition(delta, originals, refObjects);
        } else if (options.getMethod(association) == IDeltaComputationOptions.ComputationMethod.BY_OBJECT) {
            createChildDeltasPerObject(delta, originals, refObjects);
        } else {
            throw new RuntimeException("Unknown delta computation method " + options.getMethod(association));
        }
    }

    private void createChildDeltasPerPosition(ModelObjectDelta delta,
            List<? extends IModelObject> originals,
            List<? extends IModelObject> refObjects) {
        int max = Math.max(originals.size(), refObjects.size());
        for (int i = 0; i < max; i++) {
            if (hasObject(originals, i)) {
                IModelObject original = originals.get(i);
                if (hasObject(refObjects, i)) {
                    IModelObject refObject = refObjects.get(i);
                    if (options.isSame(original, refObject)) {
                        delta.addChildDelta(((IDeltaSupport)original).computeDelta(refObject, options));
                    } else {
                        delta.addChildDelta(ModelObjectDelta.newDifferentObjectAtPositionChangedDelta(original,
                                refObject, association));
                    }
                } else {
                    delta.addChildDelta(ModelObjectDelta.newRemoveDelta(original, association, options));
                }
            } else {
                if (hasObject(refObjects, i)) {
                    delta.addChildDelta(ModelObjectDelta.newAddDelta(refObjects.get(i), association, options));
                } else {
                    throw new RuntimeException(
                            "Error in delta computation. Both objects null in assocation " + association);
                }
            }
        }
    }

    private <T> boolean hasObject(Collection<T> collection, int index) {
        return index < collection.size();
    }

    private void createChildDeltasPerObject(ModelObjectDelta delta,
            List<? extends IModelObject> originals,
            List<? extends IModelObject> refObjects) {
        int removeCounter = 0;
        int size = originals.size();
        for (int i = 0; i < size; i++) {
            IModelObjectDelta childDelta = createRemoveMoveOrChangeDelta(originals.get(i), i, refObjects);
            delta.addChildDelta(childDelta);
            if (childDelta.isRemoved()) {
                removeCounter++;
            }
        }
        int refSize = refObjects.size();
        if (size - removeCounter == refSize) {
            // nothing has been added
            return;
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
                delta.addChildDelta(ModelObjectDelta.newAddDelta(refObjects.get(i), association, options));
            }
        }
    }

    private final IModelObjectDelta createRemoveMoveOrChangeDelta(IModelObject original,
            int position,
            List<? extends IModelObject> refObjects) {

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
        return ModelObjectDelta.newRemoveDelta(original, association, options);
    }

}
