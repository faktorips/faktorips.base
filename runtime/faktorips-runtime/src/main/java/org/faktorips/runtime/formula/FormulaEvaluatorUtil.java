/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.formula;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.annotation.UtilityClass;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;

/**
 * Provides methods that can be used in formula evaluation.
 * <p>
 * Some code in formulas does not need to be generated again and again but is implemented in static
 * methods in this class that are used by the formulas.
 *
 * @since 3.6
 * @see IFormulaEvaluator
 */
@UtilityClass
public enum FormulaEvaluatorUtil {
    /* no instances */;

    /**
     * Returns the (first) {@link IConfigurableModelObject} in the list that is configured by a
     * {@link IProductComponent} with the given ID, {@code null} if no such object is found in the
     * list.
     *
     * @param <T> the type of {@link IModelObject} returned by this method and expected in the list
     * @param modelObjects a list of model objects of type T
     * @param id the runtime ID this method searches
     * @see FormulaEvaluatorUtil#getModelObjectById(IModelObject, String)
     * @return the (first) {@link IConfigurableModelObject} in the list that is configured by a
     *             {@link IProductComponent} with the given ID, {@code null} if no such object is
     *             found in the list
     */
    public static <T extends IModelObject, R extends T> R getModelObjectById(List<? extends T> modelObjects,
            String id) {
        for (T modelObject : modelObjects) {
            if (modelObject instanceof IConfigurableModelObject configurableModelObject
                    && configurableModelObject.getProductComponent().getId().equals(id)) {
                return castModelObject(modelObject);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends IModelObject, R extends T> R castModelObject(T modelObject) {
        return (R)modelObject;
    }

    /**
     * Returns all {@link IConfigurableModelObject} in the list that is configured by a
     * {@link IProductComponent} with the given ID, the list is empty if no such object is found in
     * the list.
     *
     * @param <T> the type of {@link IModelObject} returned by this method and expected in the list
     * @param modelObjects a list of model objects of type T
     * @param id the runtime ID this method searches
     * @see FormulaEvaluatorUtil#getModelObjectById(IModelObject, String)
     * @return the all {@link IConfigurableModelObject} in the list that is configured by a
     *             {@link IProductComponent} with the given ID, the list is empty if no such object
     *             is found in the list
     */
    public static <T extends IModelObject, R extends T> List<? extends R> getListModelObjectById(
            List<? extends T> modelObjects,
            String id) {
        List<R> returnList = new ArrayList<>();
        for (T modelObject : modelObjects) {
            if (modelObject instanceof IConfigurableModelObject configurableModelObject
                    && configurableModelObject.getProductComponent().getId().equals(id)) {
                @SuppressWarnings("unchecked")
                R castedModelObject = (R)modelObject;
                returnList.add(castedModelObject);
            }
        }
        return returnList;
    }

    /**
     * Returns the {@link IModelObject} if it is a {@link IConfigurableModelObject} configured by a
     * {@link IProductComponent} with the given ID, {@code null} otherwise.
     *
     * @param <T> the type of {@link IModelObject} returned and expected by this method
     * @param modelObject a model object of type T
     * @param id the runtime ID this method searches
     * @see FormulaEvaluatorUtil#getModelObjectById(List, String)
     * @return the {@link IModelObject} if it is a {@link IConfigurableModelObject} configured by a
     *             {@link IProductComponent} with the given ID, {@code null} otherwise
     */
    public static <T extends IModelObject, R extends T> R getModelObjectById(T modelObject, String id) {
        if (modelObject instanceof IConfigurableModelObject configurableModelObject
                && configurableModelObject.getProductComponent().getId().equals(id)) {
            return castModelObject(modelObject);
        }
        return null;
    }

    /**
     * Returns the value of the {@code Boolean} object as a boolean primitive. If the
     * {@link Boolean} object is {@code null}, {@code false} is returned.
     *
     * @return the primitive {@code boolean} value of the given {@link Boolean}.
     */
    public static boolean toPrimitiveBoolean(Boolean b) {
        return Boolean.TRUE.equals(b);
    }

    /**
     * Helper class to encapsulate a function call to verify the existence of an object that could
     * result in an {@link Exception} (for example a {@link NullPointerException} or
     * {@link IndexOutOfBoundsException} when navigating over associations in {@link IModelObject
     * model objects}) which should just be treated as a {@code false} result.
     * <p>
     * <strong>This class is intended to be subclassed in compiled formulas only.</strong>
     * </p>
     */
    public abstract static class ExistsHelper {
        public boolean exists() {
            try {
                return existsInternal();
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                // CSON: IllegalCatch
                return false;
            }
        }

        protected abstract boolean existsInternal();
    }

    /**
     * Helper class for navigating 1-to-many associations from a list of source objects of type
     * {@code <S>} to target objects of type {@code <T>}.
     *
     * @param <S> the type of the source objects
     * @param <T> the type of the association targets
     */
    public abstract static class AssociationToManyHelper<S extends IModelObject, T extends IModelObject> {

        /**
         * Returns a {@link List} of target {@link IModelObject model objects} found by calling
         * {@link #getTargetsInternal(IModelObject)} for every object in the {@code sourceObjects}
         * {@link List}.
         *
         * @param sourceObjects the {@link IModelObject model objects} on which
         *            {@link #getTargetsInternal(IModelObject)} will be called
         * @return a {@link List} of target {@link IModelObject model objects}
         */
        public List<? extends T> getTargets(List<? extends S> sourceObjects) {
            List<T> targets = new ArrayList<>();
            for (S sourceObject : sourceObjects) {
                List<? extends T> foundTargets = getTargetsInternal(sourceObject);
                for (T target : foundTargets) {
                    if (!targets.contains(target)) {
                        targets.add(target);
                    }
                }
            }
            return targets;
        }

        /**
         * Returns a {@link List} of target {@link IModelObject model objects} by following a
         * 1-to-many association from the given {@link IModelObject source object}.
         *
         * @param sourceObject the {@link IModelObject} source for the association
         * @return a {@link List} of target {@link IModelObject model objects}
         */
        protected abstract List<? extends T> getTargetsInternal(S sourceObject);
    }

    /**
     * Helper class for navigating 1-to-1 associations from a list of source objects of type
     * {@code <S>} to target objects of type {@code <T>}.
     *
     * @param <S> the type of the source objects
     * @param <T> the type of the association targets
     */
    public abstract static class AssociationTo1Helper<S extends IModelObject, T extends IModelObject> {
        /**
         * Returns a {@link List} of target {@link IModelObject model objects} found by calling
         * {@link #getTargetInternal(IModelObject)} for every object in the {@code sourceObjects}
         * {@link List}.
         *
         * @param sourceObjects the {@link IModelObject model objects} on which
         *            {@link #getTargetInternal(IModelObject)} will be called
         * @return a {@link List} of target {@link IModelObject model objects}
         */
        public List<? extends T> getTargets(List<? extends S> sourceObjects) {
            List<T> targets = new ArrayList<>();
            for (S sourceObject : sourceObjects) {
                T target = getTargetInternal(sourceObject);
                if (!targets.contains(target)) {
                    targets.add(target);
                }
            }
            return targets;
        }

        /**
         * Returns the target {@link IModelObject} by following a 1-to-1 association from the given
         * {@link IModelObject source object}.
         *
         * @param sourceObject the {@link IModelObject} source for the association
         * @return the target {@link IModelObject}
         */
        protected abstract T getTargetInternal(S sourceObject);
    }

    /**
     * Helper class to get the values from a list of source objects of type {@code <S>} to target
     * type {@code <E>}.
     *
     * @param <S> the type of the source objects
     * @param <E> the type of the values
     */
    public abstract static class AttributeAccessorHelper<S extends IModelObject, E> {
        /**
         * Returns a {@link List} of values from {@link IModelObject model objects} found by calling
         * {@link #getValueInternal(IModelObject)} for every object in the {@code sourceObjects}
         * {@link List}.
         *
         * @param objectList the {@link List} of {@link IModelObject model objects} on which
         *            {@link #getValueInternal(IModelObject)} will be called.
         *
         * @return a {@link List} of Values
         */
        public List<E> getAttributeValues(List<? extends S> objectList) {
            return objectList.stream().map(this::getValueInternal).toList();
        }

        /**
         * Returns the value of the property from the {@link IModelObject} by calling the getter
         * method of the property.
         *
         * @param sourceObject the {@link IModelObject} source
         * @return the value of property of the {@link IModelObject}
         */
        protected abstract E getValueInternal(S sourceObject);
    }

    /**
     * Base class for functions working on list arguments. Subclasses define the function by
     * overwriting {@link FunctionWithListAsArgumentHelper#getPreliminaryResult(Object, Object)} and
     * {@link FunctionWithListAsArgumentHelper#getFallBackValue()}.
     *
     * @author HBaagil
     */
    public abstract static class FunctionWithListAsArgumentHelper<E> {

        /**
         * Returns the result of this function given a list of values.
         *
         * @param listOfValues A List of values.
         */
        public E getResult(List<E> listOfValues) {
            return listOfValues == null ? getFallBackValue()
                    : listOfValues.stream().reduce(this::getPreliminaryResult).orElse(getFallBackValue());
        }

        /**
         * Returns the value this function defaults to if an empty list or null is given as an
         * argument. E.g. for the function sum, the fall back value is 0.
         */
        public abstract E getFallBackValue();

        /**
         * Calculates a preliminary result based on the current result and the next value in the
         * list.
         *
         * @param currentResult The current result calculated up to this point.
         * @param nextValue The value to be considered next.
         * @return The result of the operation defined by subclasses.
         */
        public abstract E getPreliminaryResult(E currentResult, E nextValue);

    }

}
