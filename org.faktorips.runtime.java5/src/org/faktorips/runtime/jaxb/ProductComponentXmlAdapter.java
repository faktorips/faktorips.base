package org.faktorips.runtime.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IRuntimeRepository;

/**
 * Custom JAXB marshaling/unmarshaling for product components.
 * 
 * <p>
 * When marshaling/unmarshaling a configurable policy component, the link to its product component
 * is preserved in XML by the means of the product component ID.
 * </p>
 */
public class ProductComponentXmlAdapter extends XmlAdapter<String, IProductComponent> {

    private IRuntimeRepository repository;

    public ProductComponentXmlAdapter() {
        // nothing to do
    }

    public ProductComponentXmlAdapter(IRuntimeRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     * 
     * Returns the ID for the specified product component.
     */
    @Override
    public String marshal(IProductComponent v) throws Exception {
        if (v == null) {
            return null;
        }

        return v.getId();
    }

    /**
     * {@inheritDoc}
     * 
     * Returns the product component for the specified ID.
     */
    @Override
    public IProductComponent unmarshal(String id) throws Exception {
        if (id == null) {
            return null;
        }

        return repository.getProductComponent(id);
    }

}
