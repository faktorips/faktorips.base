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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.StringUtils;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;

/**
 * This utility class provides methods that can be used in formula evaluation.
 * 
 * @author schwering
 */
public class FormulaEvaluatorUtil {

    /**
     * Returns an array of {@link IModelObject}s found by following the
     * {@link IModelTypeAssociation} identified by the given association name from all
     * {@link IModelObject}s in the source array.
     * 
     * @param sourceObjects the {@link IModelObject}s searched for the given association name
     * @param associationName the name of the {@link IModelTypeAssociation}
     * @param repository the {@link IRuntimeRepository} used to find model type information
     * @return an array of {@link IModelObject}s found by following the
     *         {@link IModelTypeAssociation} identified by the given association name
     */
    public static IModelObject[] getTargets(IModelObject[] sourceObjects,
            String associationName,
            IRuntimeRepository repository) {
        List<IModelObject> targets = findTargets(sourceObjects, associationName, repository);
        return targets.toArray(new IModelObject[targets.size()]);
    }

    private static List<IModelObject> findTargets(IModelObject[] sourceObjects,
            String associationName,
            IRuntimeRepository repository) {
        return findTargets(sourceObjects, associationName, repository, null);
    }

    private static List<IModelObject> findTargets(IModelObject[] sourceObjects,
            String associationName,
            IRuntimeRepository repository,
            String targetId) {
        List<IModelObject> targets = new ArrayList<IModelObject>();
        for (IModelObject sourceObject : sourceObjects) {
            targets.addAll(findTargets(sourceObject, associationName, repository, targetId));
        }
        return targets;
    }

    private static List<IModelObject> findTargets(IModelObject sourceObject,
            String associationName,
            IRuntimeRepository repository,
            String targetId) {
        if (sourceObject == null) {
            return Collections.emptyList();
        }
        IModelType modelType = repository.getModelType(sourceObject);
        IModelTypeAssociation association = modelType.getAssociation(associationName);
        List<IModelObject> targetObjects = association.getTargetObjects(sourceObject);
        filterTargets(targetObjects, targetId);
        return targetObjects;
    }

    private static void filterTargets(List<IModelObject> targets, String targetId) {
        if (StringUtils.isEmpty(targetId)) {
            return;
        }
        for (Iterator<IModelObject> iterator = targets.iterator(); iterator.hasNext();) {
            IModelObject target = iterator.next();
            if (!(target instanceof IConfigurableModelObject)
                    || !((IConfigurableModelObject)target).getProductComponent().getId().equals(targetId)) {
                iterator.remove();
            }
        }
    }
}
