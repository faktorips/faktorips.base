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

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * This model service is used to create any {@link AbstractGeneratorModelNode} object.
 * 
 * @author dirmeier
 */
public class ModelService {

    public ModelService() {
    }

    public <T extends AbstractGeneratorModelNode> T createModelNode(IIpsObjectPartContainer ipsObjectPartContainer,
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
