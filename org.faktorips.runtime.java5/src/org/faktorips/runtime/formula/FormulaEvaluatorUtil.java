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
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;

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
     * Returns an array of {@link IModelObject}s found by following the
     * {@link IModelTypeAssociation} identified by the given association name from all
     * {@link IModelObject}s in the source array.
     * 
     * @param sourceObjects the {@link IModelObject}s searched for the given association name
     * @param associationName the name of the {@link IModelTypeAssociation}
     * @param targetClass the class for the association's target type, used for the type of the
     *            returned list
     * @param repository the {@link IRuntimeRepository} used to find model type information
     * @return an array of {@link IModelObject}s found by following the
     *         {@link IModelTypeAssociation} identified by the given association name
     */
    public static <S extends IModelObject, T extends IModelObject> List<T> getTargets(List<S> sourceObjects,
            String associationName,
            Class<T> targetClass,
            IRuntimeRepository repository) {
        List<T> targets = new ArrayList<T>();
        for (S sourceObject : sourceObjects) {
            List<T> foundTargets = findTargets(sourceObject, associationName, targetClass, repository);
            for (T target : foundTargets) {
                if (!targets.contains(target)) {
                    targets.add(target);
                }
            }
        }
        return targets;
    }

    private static <S extends IModelObject, T extends IModelObject> List<T> findTargets(S sourceObject,
            String associationName,
            Class<T> targetClass,
            IRuntimeRepository repository) {
        IModelType modelType = repository.getModelType(sourceObject);
        IModelTypeAssociation association = modelType.getAssociation(associationName);
        List<IModelObject> targets = association.getTargetObjects(sourceObject);
        List<T> targetObjects = new ArrayList<T>(targets.size());
        for (IModelObject target : targets) {
            if (targetClass.isInstance(target)) {
                @SuppressWarnings("unchecked")
                T castTarget = (T)target;
                targetObjects.add(castTarget);
            }
        }
        return targetObjects;
    }

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
}
