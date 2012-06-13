/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * This model service is used to create any {@link AbstractGeneratorModelNode} object.
 * 
 * @author dirmeier
 */
public class ModelService {

    private HashMap<IIpsObjectPartContainer, AbstractGeneratorModelNode> generatorModelNodes = new HashMap<IIpsObjectPartContainer, AbstractGeneratorModelNode>();

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
        AbstractGeneratorModelNode generatorModelNode = generatorModelNodes.get(ipsObjectPartContainer);
        if (generatorModelNode != null && nodeClass.isAssignableFrom(generatorModelNode.getClass())) {
            @SuppressWarnings("unchecked")
            // valid cast because checked before
            T castedGeneratorModelNode = (T)generatorModelNode;
            return castedGeneratorModelNode;
        } else {
            T newModelNode = newModelNode(ipsObjectPartContainer, nodeClass, modelContext);
            generatorModelNodes.put(ipsObjectPartContainer, newModelNode);
            return newModelNode;
        }
    }

    private <T> T newModelNode(IIpsObjectPartContainer ipsObjectPartContainer,
            Class<T> nodeClass,
            GeneratorModelContext modelContext) {
        try {
            Constructor<?>[] constructors = nodeClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes[0].isAssignableFrom(ipsObjectPartContainer.getClass())
                        && parameterTypes[1].isAssignableFrom(GeneratorModelContext.class)
                        && parameterTypes[2].isAssignableFrom(ModelService.class)) {
                    @SuppressWarnings("unchecked")
                    // safe cast, have a look at java doc of getConstructors()
                    T newInstance = (T)constructor.newInstance(ipsObjectPartContainer, modelContext, this);
                    return newInstance;
                }
            }
            throw new RuntimeException(
                    "No matching constructor found for "
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
}
