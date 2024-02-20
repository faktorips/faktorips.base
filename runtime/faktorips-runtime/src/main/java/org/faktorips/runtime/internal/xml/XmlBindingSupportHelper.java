/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.internal.xml;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractRuntimeRepository;
import org.faktorips.runtime.xml.IIpsXmlAdapter;
import org.faktorips.runtime.xml.IXmlBindingSupport;

/**
 * Abstract implementation of the {@link IXmlBindingSupport} to avoid duplication of code in the
 * JAXB/Jakarta XML Binding implementations.
 */
public class XmlBindingSupportHelper<JAXBContext> implements IXmlBindingSupport<JAXBContext> {

    private final Class<? extends Annotation> xmlRootAnnotationClass;
    private final Function<Class<?>[], JAXBContext> jaxbContextConstructor;
    private final XmlBindingContextConstructor<JAXBContext> xmlBindingContextConstructor;

    public XmlBindingSupportHelper(Class<? extends Annotation> xmlRootAnnotationClass,
            Function<Class<?>[], JAXBContext> jaxbContextConstructor,
            XmlBindingContextConstructor<JAXBContext> xmlBindingContextConstructor) {
        this.xmlRootAnnotationClass = xmlRootAnnotationClass;
        this.jaxbContextConstructor = jaxbContextConstructor;
        this.xmlBindingContextConstructor = xmlBindingContextConstructor;
    }

    @Override
    public JAXBContext newJAXBContext(IRuntimeRepository repository) {
        ClassLoader tccl = null;
        try {
            Class<?>[] classes = collectAnnotatedClasses(repository, xmlRootAnnotationClass);

            tccl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(repository.getClassLoader());

            JAXBContext ctx = jaxbContextConstructor.apply(classes);

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
        ((AbstractRuntimeRepository)repository).addAllEnumXmlAdapters(adapters, repository);

        for (IRuntimeRepository runtimeRepository : repository.getAllReferencedRepositories()) {
            AbstractRuntimeRepository refRepository = (AbstractRuntimeRepository)runtimeRepository;
            refRepository.addAllEnumXmlAdapters(adapters, repository);
        }
        return xmlBindingContextConstructor.invoke(ctx, adapters, repository);
    }

    private static Class<?>[] collectAnnotatedClasses(IRuntimeRepository repository,
            Class<? extends Annotation> xmlRootAnnotationClass) throws ClassNotFoundException {
        Set<String> classNames = repository.getAllModelTypeImplementationClasses();
        Set<Class<?>> classes = new LinkedHashSet<>(classNames.size());
        for (String className : classNames) {
            Class<?> clazz = repository.getClassLoader().loadClass(className);
            if (isAnnotatedXmlRootElement(clazz, xmlRootAnnotationClass)) {
                classes.add(clazz);
            }
        }
        return classes.toArray(Class[]::new);
    }

    private static boolean isAnnotatedXmlRootElement(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            if (c.isAnnotationPresent(annotationClass)) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface XmlBindingContextConstructor<JAXBContext> {
        JAXBContext invoke(JAXBContext wrappedCtx,
                List<? extends IIpsXmlAdapter<?, ?>> enumXmlAdapters,
                IRuntimeRepository repository);
    }

}
