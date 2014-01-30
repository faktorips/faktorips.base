/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.util.ArgumentCheck;

/**
 * This model service is used to create any {@link AbstractGeneratorModelNode} object.
 * 
 * @author dirmeier
 */
public class ModelService {

    private HashMap<IIpsObjectPartContainer, LinkedHashSet<AbstractGeneratorModelNode>> generatorModelNodes = new HashMap<IIpsObjectPartContainer, LinkedHashSet<AbstractGeneratorModelNode>>();

    public ModelService() {
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
        LinkedHashSet<AbstractGeneratorModelNode> nodes = generatorModelNodes.get(ipsObjectPartContainer);
        if (nodes == null) {
            nodes = new LinkedHashSet<AbstractGeneratorModelNode>();
            generatorModelNodes.put(ipsObjectPartContainer, nodes);
        }
        for (AbstractGeneratorModelNode generatorModelNode : nodes) {
            if (nodeClass.isAssignableFrom(generatorModelNode.getClass())) {
                @SuppressWarnings("unchecked")
                // valid cast because checked before
                T castedGeneratorModelNode = (T)generatorModelNode;
                return castedGeneratorModelNode;
            }
        }

        T newModelNode = newModelNode(ipsObjectPartContainer, nodeClass, modelContext);
        nodes.add(newModelNode);
        return newModelNode;
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
                        @SuppressWarnings("unchecked")
                        // safe cast, have a look at java doc of getConstructors()
                        T newInstance = (T)constructor.newInstance(ipsObjectPartContainer, modelContext, this);
                        return newInstance;
                    }
                }
            }
            throw new RuntimeException(
                    "No matching constructor found for element "
                            + ipsObjectPartContainer
                            + " and class "
                            + nodeClass
                            + ".\nNeed Constructor with following arguments: IIpsObjectPartContainer, GeneratorModelContext, ModelService ");
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<AbstractGeneratorModelNode> getAllModelNodes(IIpsObjectPartContainer ipsObjectPartContainer) {
        Set<AbstractGeneratorModelNode> nodes = generatorModelNodes.get(ipsObjectPartContainer);
        if (nodes == null) {
            nodes = new LinkedHashSet<AbstractGeneratorModelNode>();
        }
        return nodes;
    }

}
