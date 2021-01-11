/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractRuntimeRepository;
import org.faktorips.runtime.jaxb.IpsJAXBContext;

/**
 * Provides static factory methods to create a JAXBContext that can marshal / unmarshal all model
 * classes defined in a given runtime repository.
 * 
 * @see JAXBContext
 * @see IRuntimeRepository
 * 
 * @deprecated use {@link IRuntimeRepository#newJAXBContext()} instead; that method uses a
 *             {@link IpsJAXBContext} that includes all necessary adapters.
 */
@Deprecated
public final class JAXBContextFactory {

    private JAXBContextFactory() {
        /* no instances */
    }

    /**
     * Creates a new JAXBContext that can marshal / unmarshal all model classes defined in the given
     * repository. If the repository references other repositories (directly or indirectly), the
     * context can also handle the classes defined in these other repositories.
     * 
     * Note: The repository contains the model classed by name, so this method needs to actually
     * load the classes. If the repository gives access to a class loader (e.g.
     * ClassloaderRuntimeRepository does), the class loader provided by the repository is taken.
     * Otherwise the class loader that has loaded the class the repository is an instance of, is
     * used.
     * 
     * @param repository The repository that contains the information about the model classes
     * 
     * @throws ClassNotFoundException If one of the model classes was not found.
     * @throws JAXBException The exception thrown by {@link JAXBContext#newInstance(Class...)}
     * @throws NullPointerException if <code>repository</code> is <code>null</code>.
     */
    public static JAXBContext newContext(IRuntimeRepository repository) throws JAXBException, ClassNotFoundException {
        ClassLoader cl = repository.getClass().getClassLoader();
        if (repository instanceof AbstractRuntimeRepository) {
            cl = ((AbstractRuntimeRepository)repository).getClassLoader();
        }
        return newContext(repository, cl);
    }

    /**
     * Creates a new JAXBContext that can marshal / unmarshal all model classes defined in the given
     * repository. If the repository references other repositories (directly or indirectly), the
     * context can also handle the classes defined in these other repositories.
     * 
     * Note: The repository contains the model classed by name, so this method needs to actually
     * load the classes. This method uses the given class loader do load the classes.
     * 
     * @param repository The repository that contains the information about the model classes
     * @param cl The class loader to load the model classes.
     * 
     * @throws ClassNotFoundException If one of the model classes was not found.
     * @throws JAXBException The exception thrown by {@link JAXBContext#newInstance(Class...)}
     * @throws NullPointerException if one of the parameters is <code>null</code>.
     */
    public static JAXBContext newContext(IRuntimeRepository repository, ClassLoader cl)
            throws JAXBException, ClassNotFoundException {
        return repository.newJAXBContext();
    }
}
