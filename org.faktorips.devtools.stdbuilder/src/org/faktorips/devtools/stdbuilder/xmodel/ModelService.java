/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.util.ArgumentCheck;

/**
 * This model service is used to create any {@link AbstractGeneratorModelNode} object.
 * 
 * @author dirmeier
 */
public class ModelService {

    private HashMap<IIpsObjectPartContainer, LinkedHashSet<AbstractGeneratorModelNode>> generatorModelNodes = new HashMap<>();

    public ModelService() {
    }

    public void clear() {
        generatorModelNodes.clear();
    }

    /**
     * Getting the model node for the given {@link IIpsObjectPartContainer} of the given type. The
     * object may be already instantiated and cached. If not this method will create a new object
     * and store it in the cache.
     * <p>
     * This caching mechanism is NOT thread safe! That means you may get two instances for the same
     * combination of {@link IIpsObjectPartContainer} and {@link Class node class}.
     * 
     * @param ipsObjectPartContainer The element associated with the model node
     * @param nodeClass the type of the model node
     * @param modelContext the {@link GeneratorModelContext} set in the model node (not reset when
     *            already instantiated)
     * @return The model node either newly instantiated or from cache.
     */
    public <T extends AbstractGeneratorModelNode> T getModelNode(IIpsObjectPartContainer ipsObjectPartContainer,
            Class<T> nodeClass,
            GeneratorModelContext modelContext) {
        ArgumentCheck.notNull(ipsObjectPartContainer);
        ArgumentCheck.notNull(nodeClass);
        LinkedHashSet<AbstractGeneratorModelNode> nodes = generatorModelNodes.computeIfAbsent(ipsObjectPartContainer,
                $ -> new LinkedHashSet<>());
        for (AbstractGeneratorModelNode generatorModelNode : nodes) {
            if (nodeClass.equals(generatorModelNode.getClass())) {
                return castGeneratorModelNode(generatorModelNode);
            }
        }

        T newModelNode = newModelNode(ipsObjectPartContainer, nodeClass, modelContext);
        nodes.add(newModelNode);
        return newModelNode;
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractGeneratorModelNode> T castGeneratorModelNode(
            AbstractGeneratorModelNode generatorModelNode) {
        return (T)generatorModelNode;
    }

    private <T> T newModelNode(IIpsObjectPartContainer ipsObjectPartContainer,
            Class<T> nodeClass,
            GeneratorModelContext modelContext) {
        try {
            Constructor<?>[] constructors = nodeClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 3) {
                    if (parameterTypes[0].isAssignableFrom(ipsObjectPartContainer.getClass())
                            && parameterTypes[1].isAssignableFrom(GeneratorModelContext.class)
                            && parameterTypes[2].isAssignableFrom(ModelService.class)) {
                        return castModelNode(ipsObjectPartContainer, modelContext, constructor);
                    }
                }
            }
            throw new RuntimeException(
                    "No matching constructor found for element "
                            + ipsObjectPartContainer
                            + " and class "
                            + nodeClass
                            + ".\nNeed Constructor with following arguments: IIpsObjectPartContainer, GeneratorModelContext, ModelService ");
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T castModelNode(IIpsObjectPartContainer ipsObjectPartContainer,
            GeneratorModelContext modelContext,
            Constructor<?> constructor)
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
        return (T)constructor.newInstance(ipsObjectPartContainer, modelContext, this);
    }

    public Set<AbstractGeneratorModelNode> getAllModelNodes(IIpsObjectPartContainer ipsObjectPartContainer) {
        Set<AbstractGeneratorModelNode> nodes = generatorModelNodes.get(ipsObjectPartContainer);
        if (nodes == null) {
            nodes = new LinkedHashSet<>();
        }
        return nodes;
    }

}
