/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.jaxb;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.values.xml.DecimalXmlAdapter;
import org.faktorips.values.xml.MoneyXmlAdapter;
import org.w3c.dom.Node;

/**
 * Wraps a given JAXBContext and provides marshallers/unmarshallers that resolve the reference from
 * configurable model objects to product components. Additionally it provides the marshalling of
 * Faktor-IPS enumerations that defer their values to an enumeration content that is hold by a
 * Faktor-IPS IRuntimeRepository. See {@link #createMarshaller()} and {@link #createUnmarshaller()}
 * for more details. All other methods just delegate to the wrapped context.
 * 
 * @deprecated for removal since 23.6; use {@code org.faktorips.runtime.xml.javax.IpsJAXBContext} or
 *                 {@code org.faktorips.runtime.xml.jakarta.IpsJAXBContext} instead
 */
@Deprecated
public class IpsJAXBContext extends JAXBContext {

    private JAXBContext wrappedCtx;
    private List<? extends XmlAdapter<?, ?>> enumXmlAdapters;
    private IRuntimeRepository repository;

    public IpsJAXBContext(JAXBContext wrappedCtx, List<? extends XmlAdapter<?, ?>> enumXmlAdapters,
            IRuntimeRepository repository) {
        super();
        this.wrappedCtx = wrappedCtx;
        this.repository = repository;
        this.enumXmlAdapters = enumXmlAdapters;
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
        marshaller.setAdapter(new ProductConfigurationXmlAdapter(repository));
        marshaller.setAdapter(new LocalDateAdapter());
        marshaller.setAdapter(new LocalDateTimeAdapter());
        marshaller.setAdapter(new LocalTimeAdapter());
        marshaller.setAdapter(new MonthDayAdapter());
        marshaller.setAdapter(new MonthAdapter());
        marshaller.setAdapter(new DecimalXmlAdapter());
        marshaller.setAdapter(new MoneyXmlAdapter());
        for (XmlAdapter<?, ?> xmlAdapter : enumXmlAdapters) {
            marshaller.setAdapter(xmlAdapter);
        }
        return marshaller;
    }

    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = wrappedCtx.createUnmarshaller();
        unmarshaller.setAdapter(new ProductConfigurationXmlAdapter(repository));
        unmarshaller.setAdapter(new LocalDateAdapter());
        unmarshaller.setAdapter(new LocalDateTimeAdapter());
        unmarshaller.setAdapter(new LocalTimeAdapter());
        unmarshaller.setAdapter(new MonthDayAdapter());
        unmarshaller.setAdapter(new MonthAdapter());
        unmarshaller.setAdapter(new DecimalXmlAdapter());
        unmarshaller.setAdapter(new MoneyXmlAdapter());
        for (XmlAdapter<?, ?> xmlAdapter : enumXmlAdapters) {
            unmarshaller.setAdapter(xmlAdapter);
        }
        return unmarshaller;
    }

    @Override
    public Validator createValidator() throws JAXBException {
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
