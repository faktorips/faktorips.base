/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.formula;

import java.util.ArrayList;
import java.util.List;

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
 * @author Daniel Schwering, Faktor Zehn AG
 */
public class FormulaEvaluatorUtil {

    /**
     * Returns the (first) {@link IConfigurableModelObject} in the list that is configured by a
     * {@link IProductComponent} with the given ID, {@code null} if no such object is found in the
     * list.
     * 
     * @param <T> the type of {@link IModelObject} returned by this method and expected in the list
     * @param modelObjects a list of model objects of type <T>
     * @param id the runtime ID this method searches
     * @see FormulaEvaluatorUtil#getModelObjectById(IModelObject, String)
     * @return the (first) {@link IConfigurableModelObject} in the list that is configured by a
     *         {@link IProductComponent} with the given ID, {@code null} if no such object is found
     *         in the list
     */
    public static <T extends IModelObject> T getModelObjectById(List<T> modelObjects, String id) {
        for (T modelObject : modelObjects) {
            if (modelObject instanceof IConfigurableModelObject) {
                if (((IConfigurableModelObject)modelObject).getProductComponent().getId().equals(id)) {
                    return modelObject;
                }
            }
        }
        return null;
    }

    /**
     * Returns the {@link IModelObject} if it is a {@link IConfigurableModelObject} configured by a
     * {@link IProductComponent} with the given ID, {@code null} otherwise.
     * 
     * @param <T> the type of {@link IModelObject} returned and expected by this method
     * @param modelObject a model object of type <T>
     * @param id the runtime ID this method searches
     * @see FormulaEvaluatorUtil#getModelObjectById(List, String)
     * @return the {@link IModelObject} if it is a {@link IConfigurableModelObject} configured by a
     *         {@link IProductComponent} with the given ID, {@code null} otherwise
     */
    public static <T extends IModelObject> T getModelObjectById(T modelObject, String id) {
        if (modelObject instanceof IConfigurableModelObject
                && ((IConfigurableModelObject)modelObject).getProductComponent().getId().equals(id)) {
            return modelObject;
        }
        return null;
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
    public static abstract class ExistsHelper {
        public boolean exists() {
            try {
                return existsInternal();
            } catch (Exception e) {
                return false;
            }
        }

        abstract protected boolean existsInternal();
    }

    /**
     * Helper class for navigating 1-to-many associations from a list of source objects of type
     * {@code <S>} to target objects of type {@code <T>}.
     * 
     * @param <S> the type of the source objects
     * @param <T> the type of the association targets
     */
    public static abstract class AssociationToManyHelper<S extends IModelObject, T extends IModelObject> {

        /**
         * Returns a {@link List} of target {@link IModelObject model objects} found by calling
         * {@link #getTargetsInternal(IModelObject)} for every object in the {@code sourceObjects}
         * {@link List}.
         * 
         * @param sourceObjects the {@link IModelObject model objects} on which
         *            {@link #getTargetsInternal(IModelObject)} will be called
         * @return a {@link List} of target {@link IModelObject model objects}
         */
        public List<T> getTargets(List<S> sourceObjects) {
            List<T> targets = new ArrayList<T>();
            for (S sourceObject : sourceObjects) {
                List<T> foundTargets = getTargetsInternal(sourceObject);
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
        abstract protected List<T> getTargetsInternal(S sourceObject);
    }

    /**
     * Helper class for navigating 1-to-1 associations from a list of source objects of type
     * {@code <S>} to target objects of type {@code <T>}.
     * 
     * @param <S> the type of the source objects
     * @param <T> the type of the association targets
     */
    public static abstract class AssociationTo1Helper<S extends IModelObject, T extends IModelObject> {
        /**
         * Returns a {@link List} of target {@link IModelObject model objects} found by calling
         * {@link #getTargetInternal(IModelObject)} for every object in the {@code sourceObjects}
         * {@link List}.
         * 
         * @param sourceObjects the {@link IModelObject model objects} on which
         *            {@link #getTargetInternal(IModelObject)} will be called
         * @return a {@link List} of target {@link IModelObject model objects}
         */
        public List<T> getTargets(List<S> sourceObjects) {
            List<T> targets = new ArrayList<T>();
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
        abstract protected T getTargetInternal(S sourceObject);
    }
}
