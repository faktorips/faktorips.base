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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.xml.IIpsXmlAdapter;
import org.w3c.dom.Node;

/**
 * Wraps a given {@link JAXBContext} and provides marshallers/unmarshallers that resolve the
 * reference from configurable model objects to product components. Additionally it provides the
 * marshalling of Faktor-IPS enumerations that defer their values to an enumeration content that is
 * held by a Faktor-IPS {@link IRuntimeRepository}. See {@link #createMarshaller()} and
 * {@link #createUnmarshaller()} for more details. All other methods just delegate to the wrapped
 * context.
 */
public class IpsJAXBContext extends JAXBContext {

    private JAXBContext wrappedCtx;
    private List<XmlAdapter<?, ?>> xmlAdapters;
    private IRuntimeRepository repository;

    public IpsJAXBContext(JAXBContext wrappedCtx, List<? extends IIpsXmlAdapter<?, ?>> enumXmlAdapters,
            IRuntimeRepository repository) {
        super();
        this.wrappedCtx = wrappedCtx;
        this.repository = repository;
        xmlAdapters = new ArrayList<>();
        xmlAdapters.add(new ProductConfigurationXmlAdapter(repository));
        xmlAdapters.add(new LocalDateAdapter());
        xmlAdapters.add(new LocalDateTimeAdapter());
        xmlAdapters.add(new LocalTimeAdapter());
        xmlAdapters.add(new MonthDayAdapter());
        xmlAdapters.add(new MonthAdapter());
        enumXmlAdapters.stream()
                .map((IIpsXmlAdapter<?, ?> a) -> JaxbSupport.wrap(a))
                .forEach(xmlAdapters::add);
    }

    /**
     * Returns the Faktor-IPS runtime repository.
     */
    public IRuntimeRepository getRepository() {
        return repository;
    }

    @Override
    public Marshaller createMarshaller() throws JAXBException {
        Marshaller marshaller = wrappedCtx.createMarshaller();
        for (XmlAdapter<?, ?> xmlAdapter : xmlAdapters) {
            marshaller.setAdapter(xmlAdapter);
        }
        return marshaller;
    }

    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = wrappedCtx.createUnmarshaller();
        for (XmlAdapter<?, ?> xmlAdapter : xmlAdapters) {
            unmarshaller.setAdapter(xmlAdapter);
        }
        return unmarshaller;
    }

    @Deprecated
    @Override
    public javax.xml.bind.Validator createValidator() throws JAXBException {
        return wrappedCtx.createValidator();
    }

    @Override
    public Binder<Node> createBinder() {
        return wrappedCtx.createBinder();
    }

    @Override
    public <T> Binder<T> createBinder(Class<T> domType) {
        return wrappedCtx.createBinder(domType);
    }

    @Override
    public JAXBIntrospector createJAXBIntrospector() {
        return wrappedCtx.createJAXBIntrospector();
    }

    @Override
    public void generateSchema(SchemaOutputResolver outputResolver) throws IOException {
        wrappedCtx.generateSchema(outputResolver);
    }

}
