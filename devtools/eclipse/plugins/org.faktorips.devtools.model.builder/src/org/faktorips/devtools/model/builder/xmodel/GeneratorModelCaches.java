/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductClass;
import org.faktorips.util.ClassToInstancesMap;

/**
 * This cache is used for {@link AbstractGeneratorModelNode} objects to cache references to other
 * {@link AbstractGeneratorModelNode}. The list of cached references is identified by their classes.
 * <p>
 * For example a {@link XProductClass} needs to cache the references to its {@link XAttribute}. This
 * cache should not be stored in the {@link XProductClass} because it needs to be stateless as far
 * we do not recreate the generator model nodes on any time. Hence to cache these references the
 * cache is stored in the generator model context which is statefull and does renew the caches for
 * every build cycle.
 * 
 * @author dirmeier
 */
public class GeneratorModelCaches {

    private final Map<AbstractGeneratorModelNode, ClassToInstancesMap<AbstractGeneratorModelNode>> caches = new HashMap<>();

    /**
     * Getting the list of cached nodes. The sourceNode is the object which needs to cache the nodes
     * of the specified type.
     * 
     * @param sourceNode The source node that has cached the nodes
     * @param type The type of the cached nodes
     * 
     * @return The list of cached nodes of the specified type for the sourceNode. If there is no
     *             cache for the specified parameter this method returns an empty list, never
     *             returns null.
     */
    public <T extends AbstractGeneratorModelNode> List<T> getCachedNodes(AbstractGeneratorModelNode sourceNode,
            Class<T> type) {
        ClassToInstancesMap<AbstractGeneratorModelNode> classToInstancesMap = caches.get(sourceNode);
        return classToInstancesMap.get(type);
    }

    public <T extends AbstractGeneratorModelNode> boolean isCached(AbstractGeneratorModelNode sourceNodee,
            Class<T> type) {
        ClassToInstancesMap<AbstractGeneratorModelNode> classToInstanceMap = getClassToInstanceMap(sourceNodee, false);
        return classToInstanceMap.containsValuesOf(type);
    }

    private ClassToInstancesMap<AbstractGeneratorModelNode> getClassToInstanceMap(AbstractGeneratorModelNode node,
            boolean putNew) {
        ClassToInstancesMap<AbstractGeneratorModelNode> classToInstancesMap = caches.get(node);
        if (classToInstancesMap == null) {
            classToInstancesMap = new ClassToInstancesMap<>();
            if (putNew) {
                caches.put(node, classToInstancesMap);
            }
        }
        return classToInstancesMap;
    }

    /**
     * Putting a new object to the list of cached nodes.
     * 
     * @param objectToCache The object you want to put to the cache
     * @param sourceNode The node which want to hold the cached object
     */
    public <T extends AbstractGeneratorModelNode> void put(T objectToCache, AbstractGeneratorModelNode sourceNode) {
        ClassToInstancesMap<AbstractGeneratorModelNode> classToInstancesMap = getClassToInstanceMap(sourceNode, true);
        classToInstancesMap.put(objectToCache);
    }

}
