/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.xml;

import org.faktorips.runtime.IRuntimeRepository;

/**
 * Abstraction to provide access to classic JAXB and Jakarta XML Binding.
 * <p>
 * You can use the methods {@link #newJAXBContext(IRuntimeRepository)} and
 * {@link #newJAXBContext(Object, IRuntimeRepository)} on the {@code INSTANCE} of the
 * {@code JaxbSupport} implementation from the package {@code org.faktorips.runtime.jaxb} or
 * {@code org.faktorips.runtime.jakarta.xml} like this:
 * 
 * <pre>
 * {@code
 * JAXBContext context = JaxbSupport.INSTANCE.newJAXBContext(repo);
 * }
 * </pre>
 * 
 * @param <JAXBContext> the {@code JAXBContext} implementation with the package
 *            {@code javax.xml.bind} or {@code jakarta.xml.bind}
 */
public interface IXmlBindingSupport<JAXBContext> {

    /**
     * Creates a new {@code JAXBContext} that can marshal / unmarshal all model classes defined in
     * the given repository. If the repository references other repositories (directly or
     * indirectly), the context can also handle the classes defined in those.
     * 
     * @throws RuntimeException Exceptions that are thrown while trying to load a class from the
     *             class loader or creating the jakarta context are wrapped into a runtime exception
     */
    JAXBContext newJAXBContext(IRuntimeRepository repository);

    /**
     * Creates a {@code JAXBContext} that wraps the provided context and extends the marshaling
     * methods to provide marshaling of Faktor-IPS enumerations and model objects configured by
     * product components.
     */
    JAXBContext newJAXBContext(JAXBContext ctx, IRuntimeRepository repository);

    /**
     * Returns the {@link IXmlBindingSupport} implementation matching the expected
     * {@code JAXBContext}.
     * 
     * @param <JAXBContext> the {@code JAXBContext} implementation with the package
     *            {@code javax.xml.bind} or {@code jakarta.xml.bind}
     */
    @SuppressWarnings("unchecked")
    static <JAXBContext> IXmlBindingSupport<JAXBContext> get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            return (IXmlBindingSupport<JAXBContext>)classLoader
                    .loadClass("org.faktorips.runtime.xml.jakarta3.JaxbSupport").getField("INSTANCE")
                    .get(null);
        } catch (ClassNotFoundException | SecurityException | IllegalAccessException | IllegalArgumentException
                | NoSuchFieldException | UnsupportedClassVersionError e) {
            try {
                return (IXmlBindingSupport<JAXBContext>)classLoader
                        .loadClass("org.faktorips.runtime.xml.javax.JaxbSupport")
                        .getField("INSTANCE").get(null);
            } catch (ClassNotFoundException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | NoSuchFieldException e2) {
                RuntimeException runtimeException = new RuntimeException("Can't find JAXBContext on the classpath", e2);
                runtimeException.addSuppressed(e);
                throw runtimeException;
            }
        }
    }
}
