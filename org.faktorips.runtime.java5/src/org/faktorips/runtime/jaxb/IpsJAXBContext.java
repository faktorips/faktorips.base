/**
 * 
 */
package org.faktorips.runtime.jaxb;

import java.io.IOException;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import org.faktorips.runtime.IRuntimeRepository;
import org.w3c.dom.Node;


/**
 * Wraps a given JAXBContext and provides marshallers/unmarshallers that resolve the reference from
 * configurable model objects to product components. See {@link #createMarshaller()} and {@link #createUnmarshaller()} 
 * for more details. All other methods just delegate to the wrapped context. 
 * 
 * @author Jan Ortmann
 */
public class IpsJAXBContext extends JAXBContext {

    private JAXBContext wrappedCtx;
    private IRuntimeRepository repository;
    
    public IpsJAXBContext(JAXBContext wrappedCtx, IRuntimeRepository repository) {
        super();
        this.wrappedCtx = wrappedCtx;
        this.repository = repository;
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
        marshaller.setAdapter(new ProductComponentXmlAdapter(repository));
        return marshaller;
    }

    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = wrappedCtx.createUnmarshaller();
        unmarshaller.setAdapter(new ProductComponentXmlAdapter(repository));
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
