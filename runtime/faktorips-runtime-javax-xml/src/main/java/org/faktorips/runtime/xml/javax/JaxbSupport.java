/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.xml.javax;

import java.security.PrivilegedAction;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractRuntimeRepository;
import org.faktorips.runtime.xml.IIpsXmlAdapter;
import org.faktorips.runtime.xml.IXmlBindingSupport;

/**
 * Classic {@link JAXB} version of the {@link IXmlBindingSupport}, to be used with Java 8 and Java
 * EE up to 8.
 */
public enum JaxbSupport implements IXmlBindingSupport<JAXBContext> {
    INSTANCE;

    @Override
    public JAXBContext newJAXBContext(IRuntimeRepository repository) {
        ClassLoader tccl = null;
        try {
            Set<String> classNames = repository.getAllModelTypeImplementationClasses();
            Set<Class<?>> classes = new LinkedHashSet<>(classNames.size());
            for (String className : classNames) {
                Class<?> clazz = repository.getClassLoader().loadClass(className);
                if (isAnnotatedXmlRootElement(clazz)) {
                    classes.add(clazz);
                }
            }

            tccl = getPrivilegedCurrentThreadContextClassLoader();
            Thread.currentThread().setContextClassLoader(repository.getClassLoader());

            JAXBContext ctx = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));

            return newJAXBContext(ctx, repository);
            // CSOFF: IllegalCatch
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            // CSON: IllegalCatch
            throw new RuntimeException(e);
        } finally {
            if (tccl != null) {
                Thread.currentThread().setContextClassLoader(tccl);
            }
        }
    }

    @Override
    public JAXBContext newJAXBContext(JAXBContext ctx, IRuntimeRepository repository) {
        LinkedList<IIpsXmlAdapter<?, ?>> adapters = new LinkedList<>();
        for (IRuntimeRepository runtimeRepository : repository.getAllReferencedRepositories()) {
            AbstractRuntimeRepository refRepository = (AbstractRuntimeRepository)runtimeRepository;
            refRepository.addAllEnumXmlAdapters(adapters, repository);
        }
        return new IpsJAXBContext(ctx, adapters, repository);
    }

    @SuppressWarnings("unchecked")
    public static <ValueType, BoundType> XmlAdapter<ValueType, BoundType> wrap(
            IIpsXmlAdapter<ValueType, BoundType> xmlAdapter) {
        if (xmlAdapter instanceof XmlAdapter) {
            return (XmlAdapter<ValueType, BoundType>)xmlAdapter;
        }
        return new XmlAdapter<ValueType, BoundType>() {

            @Override
            public BoundType unmarshal(ValueType v) throws Exception {
                return xmlAdapter.unmarshal(v);
            }

            @Override
            public ValueType marshal(BoundType v) throws Exception {
                return xmlAdapter.marshal(v);
            }
        };
    }

    /**
     * JAXB uses the class loader from the thread context. By default, the thread context class
     * loader is not aware of OSGi and thus doesn't see any of the classes imported in the bundle.
     * 
     * If a {@link SecurityManager} is used the {@link ClassLoader} is loaded with
     * {@link java.security.AccessController#doPrivileged(java.security.PrivilegedAction)}.
     * 
     * @return the context {@code ClassLoader} for this thread, or {@code null}
     */
    private static ClassLoader getPrivilegedCurrentThreadContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return java.security.AccessController.doPrivileged(
                    (PrivilegedAction<ClassLoader>)Thread.currentThread()::getContextClassLoader);
        }
    }

    private static boolean isAnnotatedXmlRootElement(Class<?> clazz) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            if (c.isAnnotationPresent(XmlRootElement.class)) {
                return true;
            }
        }
        return false;
    }

}
